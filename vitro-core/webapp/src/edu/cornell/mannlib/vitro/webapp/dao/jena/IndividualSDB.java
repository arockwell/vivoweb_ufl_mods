/*
Copyright (c) 2011, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;

public class IndividualSDB extends IndividualImpl implements Individual {

    private static final Log log = LogFactory.getLog(
                IndividualSDB.class.getName());
    private OntResource ind = null;
    private WebappDaoFactoryJena webappDaoFactory = null;
    private Float _searchBoostJena = null;
    private boolean retreivedNullRdfsLabel = false;
    private DatasetWrapperFactory dwf = null;
    private SDBDatasetMode datasetMode = 
                SDBDatasetMode.ASSERTIONS_AND_INFERENCES;
    private String individualURI = null; 
    private Model model = null;
    private Boolean _hasThumb = null; 
    
    public IndividualSDB(String individualURI, 
                         DatasetWrapperFactory datasetWrapperFactory,
                         SDBDatasetMode datasetMode,
                         WebappDaoFactoryJena wadf,
                         Model initModel) {
    	this.individualURI = individualURI;
    	this.dwf = datasetWrapperFactory;
      
    	try {
	    	initModel.getLock().enterCriticalSection(Lock.READ);
	    	String getStatements = 
	    		"CONSTRUCT \n" +
	    		"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
	    		        "> ?ooo. \n" +
	    		   "<"+individualURI+">  a ?type . \n" +
	    		   "<"+individualURI+"> <" + VitroVocabulary.MONIKER + 
	    		           "> ?moniker \n" +
	    		 "} \n" +
	    		 "WHERE { \n" +
	    		 	"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
	    		 	        "> ?ooo }  \n" +
	    		 	" UNION { <"+individualURI+"> a ?type } \n" +
	    		 	" UNION { <"+individualURI+"> <" + VitroVocabulary.MONIKER +
	    		 	        "> ?moniker } \n" +
	    		 "} "; 
    		this.model = QueryExecutionFactory.create(
    		        QueryFactory.create(getStatements), initModel)
    		                .execConstruct();
    	} finally {
    		initModel.getLock().leaveCriticalSection();
    	}
    	
    	OntModel ontModel = ModelFactory.createOntologyModel(
    	        OntModelSpec.OWL_MEM, model);
    	this.ind = ontModel.createOntResource(individualURI);  
    	setUpURIParts(ind);
        this.webappDaoFactory = wadf;
    }
    
    public IndividualSDB(String individualURI, 
            DatasetWrapperFactory datasetWrapperFactory, 
            SDBDatasetMode datasetMode,
            WebappDaoFactoryJena wadf,
            boolean skipInitialization) {
    	this.individualURI = individualURI;
    	this.datasetMode = datasetMode;
    	this.dwf = datasetWrapperFactory;
    	
    	if (skipInitialization) {
            OntModel ontModel = ModelFactory.createOntologyModel(
                    OntModelSpec.OWL_MEM);
            this.ind = ontModel.createOntResource(individualURI);  
    	} else {
        	DatasetWrapper w = getDatasetWrapper();
        	Dataset dataset = w.getDataset();
        	try {
    	    	dataset.getLock().enterCriticalSection(Lock.READ);
    	    	String getStatements = 
    	    		"CONSTRUCT " +
    	    		"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
    	    		        "> ?ooo. \n" +
//    	    		   "<"+individualURI+">  a ?type . \n" +
    	    		   "<"+individualURI+">  <" + VitroVocabulary.MONIKER + 
    	    		           "> ?moniker \n" +
    	    		 "} WHERE {" +
    	    		 	"{ <"+individualURI+">  <" + RDFS.label.getURI() + 
    	    		 	        "> ?ooo } \n" +
    	    		 	"UNION { <" +
    	    		 	    individualURI+">  <" + VitroVocabulary.MONIKER + 
    	    		 	        "> ?moniker \n" +
    	    		 	"}  \n" +
//    	    		 	"UNION { <"
//    	    		 	    + individualURI + "> a ?type } \n" +
    	    		 "}";
        		model = QueryExecutionFactory.create(
        		        QueryFactory.create(getStatements), dataset)
        		                .execConstruct();
        	} finally {
        		dataset.getLock().leaveCriticalSection();
        		w.close();
        	}
        	
        	OntModel ontModel = ModelFactory.createOntologyModel(
        	        OntModelSpec.OWL_MEM, model);
        	
        	if (model.isEmpty() && noTriplesFor(individualURI)) {
        	    throw new IndividualNotFoundException();
        	}
        	
        	this.ind = ontModel.createOntResource(individualURI);  
    	}
    	setUpURIParts(ind);
        this.webappDaoFactory = wadf;
    }
    
    private boolean noTriplesFor(String individualURI) {
        String ask = "ASK { <" + individualURI + "> ?p ?o }";
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            Query askQuery = QueryFactory.create(ask, Syntax.syntaxARQ);
            QueryExecution qe = QueryExecutionFactory.create(askQuery, dataset);
            try {
                return !qe.execAsk();
            } finally {
                qe.close();
            }
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
        }
    }
    
    static final boolean SKIP_INITIALIZATION = true;
    
    public IndividualSDB(String individualURI, 
            DatasetWrapperFactory datasetWrapperFactory,
            SDBDatasetMode datasetMode,
            WebappDaoFactoryJena wadf) {
        this(individualURI, 
             datasetWrapperFactory, 
             datasetMode, 
             wadf, 
             !SKIP_INITIALIZATION);
    }
    
    public class IndividualNotFoundException extends RuntimeException {}
    
    private void setUpURIParts(OntResource ind) {
        if (ind != null) {
            if (ind.isAnon()) {
                this.setNamespace(VitroVocabulary.PSEUDO_BNODE_NS);
                this.setLocalName(ind.getId().toString());
            } else {
                this.URI = ind.getURI();
                this.namespace = ind.getNameSpace();
                this.localName = ind.getLocalName();
            }
        } else if (individualURI != null) {
            log.warn("Null individual returned for URI " + individualURI);
        }
    }

    private DatasetWrapper getDatasetWrapper() {
        return this.dwf.getDatasetWrapper();
    }
    
    public String getName() { 
        if (this.name != null) {
            return name;
        } else {
        	ind.getOntModel().enterCriticalSection(Lock.READ);
        	
            try {
                this.name = webappDaoFactory.getJenaBaseDao().getLabelOrId(ind);
                if (this.name == null) {
                    this.name = "[null]";
                }
                return this.name;
          
            } finally {
              
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getRdfsLabel() { 
        if (this.rdfsLabel != null) {
            return rdfsLabel;
        } else if( this.rdfsLabel == null && retreivedNullRdfsLabel ){
        	return null;
        } else { 
           
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                this.rdfsLabel = webappDaoFactory.getJenaBaseDao().getLabel(ind);
                retreivedNullRdfsLabel = this.rdfsLabel == null;
                return this.rdfsLabel;
            } finally {
               
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }
    
	public String getVClassURI() { 
        if (this.vClassURI != null) {
            return vClassURI;
        } else {
        	List<VClass> clist = getVClasses(true);
        	return (clist.size() > 0) ? clist.get(0).getURI() : null; 
        }
    }

    public VClass getVClass() {
        if (this.vClass != null) {
            return this.vClass;
        } else {
        	List<VClass> clist = getVClasses(true);
            return (clist.size() > 0) ? clist.get(0) : null ; 
        } 
    }

    public int getFlag1Numeric() { 
        if (flag1Numeric > -1) {
            return flag1Numeric;
        } else {
            doFlag1();
            return flag1Numeric;
        }
    }
 
    public String getFlag1Set() { 
        if (flag1Set != null) {
            return flag1Set;
        } else {
            doFlag1();
            return flag1Set;
        }
    }

    public String getFlag2Set() { 
        if (flag2Set != null) {
            return flag2Set;
        } else {
            doFlag2();
            return flag2Set;
        }
    }

    /* Consider the flagBitMask as a mask to & with flags.
   if flagBitMask bit zero is set then return true if
   the individual is in portal 2,
   if flagBitMask bit 1 is set then return true if
   the individua is in portal 4
   etc.
   Portal uris look like this:
   "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing"
    */
    public boolean doesFlag1Match(int flagBitMask) { 
        Long [] numerics = FlagMathUtils.numeric2numerics(flagBitMask);      
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try{
            for( Long numericPortal : numerics){
                int portalid = FlagMathUtils.numeric2Portalid(numericPortal);
                String portalTypeUri = VitroVocabulary.vitroURI + 
                        "Flag1Value" + portalid + "Thing";
                String Ask = "ASK { <" + this.individualURI + 
                        "> <" +RDF.type+ "> <" + portalTypeUri +">} "; 
                if(!QueryExecutionFactory.create(
                        QueryFactory.create(Ask), dataset).execAsk()) {
                	return false;
                }
            }
        }finally{
        	dataset.getLock().leaveCriticalSection();
        	w.close();
        }
        return true;
    }

    private void doFlag1() {
        if( webappDaoFactory.getPortalDao().isSinglePortal() ){
            flag1Set = "0";
            flag1Numeric = 1;
        }else{
        	String getObjects = null;
            Model tempModel = ModelFactory.createDefaultModel();
            OntModel ontModel = ModelFactory.createOntologyModel(
                    OntModelSpec.OWL_MEM);
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            try {
            	ClosableIterator typeIt = null;
                int portalNumeric = 0;
                String portalSet = "";
                try{
                	getObjects = 
                		"CONSTRUCT{<" + this.individualURI + "> <" + 
                		        RDF.type + "> ?object}" +
            			"WHERE{ <" + this.individualURI + "> <" + 
            			        RDF.type + "> ?object }";
            		tempModel = QueryExecutionFactory.create(
            		        QueryFactory.create(
            		                getObjects), dataset).execConstruct();
            		ontModel.add(tempModel.listStatements());
            	    OntResource ontRes = ontModel.createOntResource(
            	            this.individualURI);
            	    typeIt = ontRes.getOntModel().listStatements(
            	            ontRes, RDF.type ,(String) null);
            	    while (typeIt.hasNext()) {
                        Statement stmt = (Statement) typeIt.next();
                        Resource type = (Resource)stmt.getObject();
                        String typeName = type.getLocalName();
                        if(type.getNameSpace() != null 
                                && type.getNameSpace().equals(
                                        VitroVocabulary.vitroURI) 
                                && typeName.indexOf("Flag1Value")==0) {
                            try {
                                int portalNumber = Integer.decode(
                                        typeName.substring(10,typeName.length()-5));
                                portalNumeric = portalNumeric | (1 << portalNumber);
                                if (portalSet.length() > 0) {
                                    portalSet+=",";
                                }
                                portalSet+=Integer.toString(portalNumber);
                            } catch (Exception e) {
                            	log.warn("could not convert into a " +
                            			"portal id:'" + typeName + "' " + e.getMessage());
                            }
                        }
                    }
                }finally{
                    if( typeIt != null ) typeIt.close() ;
                }
                flag1Set = portalSet;
                flag1Numeric = portalNumeric;
            } finally {
                tempModel.close();
                ontModel.close();
                dataset.getLock().leaveCriticalSection();
            	w.close();
            }        
        }
    }

    
    private void doFlag2() {
        if( webappDaoFactory.getPortalDao().isSinglePortal() ){
            flag2Set = "";
            flag2Numeric = 0 ;
        }else{
        	String getObjects = null;
            Model tempModel = ModelFactory.createDefaultModel();
            OntModel ontModel = ModelFactory.createOntologyModel(
                    OntModelSpec.OWL_MEM);
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
            try {
                ClosableIterator typeIt=null;
                String flagSet = "";
                try{
                	getObjects = 
                		"CONSTRUCT{<" + this.individualURI + "> <" + 
                		        RDF.type + "> ?object}" +
            			"WHERE{ <" + this.individualURI + "> <" + 
            			        RDF.type + "> ?object }";
            		tempModel = QueryExecutionFactory.create(
            		        QueryFactory.create(
            		                getObjects), dataset).execConstruct();
            		ontModel.add(tempModel.listStatements());
            	    OntResource ontRes = ontModel.createOntResource(
            	            this.individualURI);
            	    typeIt = ontRes.getOntModel().listStatements(
            	            ontRes, RDF.type ,(String) null);
            	    while (typeIt.hasNext()) {
                        Statement stmt = (Statement) typeIt.next();
                        Resource type = (Resource)stmt.getObject();
                        String typeName = type.getLocalName();
                        if(type.getNameSpace() != null 
                                && type.getNameSpace().equals(
                                        VitroVocabulary.vitroURI) 
                                && typeName.indexOf("Flag2Value")==0) {
                            try {
                                String flagValue = 
                                        ((WebappDaoFactoryJena) webappDaoFactory)
                                                .getFlag2ClassLabelMap().get(type);
                                if (flagSet.length() > 0) {
                                    flagSet+=",";
                                }
                                flagSet+=flagValue;
                            } catch (Exception e) {
                            	log.error(e,e);
                            }
                        }
                    }
                }finally{
                    if( typeIt != null ) typeIt.close() ;
                }
                flag2Set = flagSet;
            } finally {
            	tempModel.close();
                ontModel.close();
            	dataset.getLock().leaveCriticalSection();
            	w.close();
            }
        }        
    }


    public Date getSunrise() {
        if( this.sunrise == null ){            
            String[] graphVars = { "?g" };
            String queryStr = 
                "SELECT ?value " +
                "WHERE { GRAPH ?g { <" + individualURI + "> " + 
                        "<" + webappDaoFactory.getJenaBaseDao().SUNRISE + "> " + 
                        "?value} \n" + 
                        //WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
                        "}";
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            try{            
                ResultSet rs = QueryExecutionFactory.create(QueryFactory.create(queryStr), dataset)
                    .execSelect();
                if( rs.hasNext()){
                    QuerySolution qs = rs.nextSolution();
                    if( qs.get("value") != null ){
                        Literal value = qs.get("value").asLiteral();
                        RDFDatatype datatype = value.getDatatype();
                        XSDDateTime xsdDt = (XSDDateTime)datatype.parse( value.getLexicalForm() );
                        sunrise = xsdDt.asCalendar().getTime();
                    }
                }            
            }catch(Exception ex){
                log.error("could not get sunrise: " + ex.getMessage(),ex);
            }finally{
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
        }        
        return sunrise;        
    }

    public Date getSunset() {        
        if( this.sunset == null ){            
            String[] graphVars = { "?g" };
            String queryStr = 
                "SELECT ?value " +
                "WHERE { GRAPH ?g { <" + individualURI + "> " + 
                        "<"+webappDaoFactory.getJenaBaseDao().SUNSET+"> ?value} }";
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            try{          
                ResultSet rs = QueryExecutionFactory.create(QueryFactory.create(queryStr), dataset)
                    .execSelect();
                if( rs.hasNext()){
                    QuerySolution qs = rs.nextSolution();
                    if( qs.get("value") != null ){
                        Literal value = qs.get("value").asLiteral();
                        RDFDatatype datatype = value.getDatatype();
                        XSDDateTime xsdDt = (XSDDateTime)datatype.parse( value.getLexicalForm() );
                        sunset = xsdDt.asCalendar().getTime();
                    }
                }
            }catch(Exception ex){
                log.error("could not get sunset: " + ex.getMessage(),ex);
            }finally{
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
        }        
        return sunset;          
    }

    public Date getTimekey() { 
        if (timekey != null) {
            return timekey;
        } else {
            constructProperty(ind, VitroVocabulary.TIMEKEY);
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                timekey = webappDaoFactory.getJenaBaseDao()
                        .getPropertyDateTimeValue(
                                ind,webappDaoFactory.getJenaBaseDao().TIMEKEY);
                return timekey;
            } finally {
              
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public Timestamp getModTime() {
        if (modTime != null) {
            return modTime;
        } else {
           
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                Date modDate = webappDaoFactory.getJenaBaseDao()
                        .getPropertyDateTimeValue(
                                ind,webappDaoFactory.getJenaBaseDao().MODTIME);
                if (modDate != null) {
                    modTime = new Timestamp(modDate.getTime());
                }
                return modTime;
            } finally {
               
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getMoniker() { 
        if (moniker != null) {
            return moniker;
        } else {         
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                moniker = webappDaoFactory.getJenaBaseDao()
                        .getPropertyStringValue(
                                ind,webappDaoFactory.getJenaBaseDao().MONIKER);
                if (moniker == null) {
                    //Changing behavior to moniker because it is taking extra time to get the vclass
                    //alternative if the moniker isn't filled out.  That time is wasted if the vclass alternative isn't desired.
                    //see NIHVIVO-2001
                      moniker = "";
                  }                
                return moniker;
            } finally {
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getBlurb() { 
        if (this.blurb != null) {
            return blurb;
        } else {
            constructProperty(ind, VitroVocabulary.BLURB);
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                blurb = webappDaoFactory.getJenaBaseDao().getPropertyStringValue(ind,webappDaoFactory.getJenaBaseDao().BLURB);
                return blurb;
            } finally {
               
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    public String getDescription() { 
        if (this.description != null) {
            return description;
        } else {
            constructProperty(ind, VitroVocabulary.DESCRIPTION);
        	ind.getOntModel().enterCriticalSection(Lock.READ);
            try {
                description = webappDaoFactory.getJenaBaseDao()
                        .getPropertyStringValue(
                             ind,webappDaoFactory.getJenaBaseDao().DESCRIPTION);
                return description;
            } finally {
                
            	ind.getOntModel().leaveCriticalSection();
            }
        }
    }

    private synchronized void constructProperty(OntResource ind, String propertyURI) {
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String[] graphVars = { "?g" };
            String queryStr = 
                "CONSTRUCT { <"+ind.getURI()+"> <" + propertyURI + "> ?value } \n" +
                    "WHERE { GRAPH ?g {  \n" +
                    "<" + ind.getURI() +"> <" + propertyURI + "> ?value } \n" + 
                    WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
                    "\n} ";
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(
                    query, dataset);
            qe.execConstruct(ind.getModel());
        } finally {
            dataset.getLock().leaveCriticalSection();
            w.close();
        }
    }

    public Float getSearchBoost(){ 
        if( this._searchBoostJena != null ){
            return this._searchBoostJena;
        }else{
            String[] graphVars = { "?g" };
            String getPropertyValue = 
            	"SELECT ?value \n" +
            	"WHERE { GRAPH ?g { \n" +
            	"<" +individualURI+ "> <" +webappDaoFactory.getJenaBaseDao().SEARCH_BOOST_ANNOT+ "> ?value }\n" + 
            	WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) + "\n" +
            	"}";
            
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
            try{
                ResultSet rs = QueryExecutionFactory.create(
                		QueryFactory.create(getPropertyValue),
                		dataset).execSelect();
                
                if(rs.hasNext()){
                	QuerySolution qs = rs.nextSolution();
                	if(qs.get("value") !=null){
                		Literal value = qs.get("value").asLiteral();
                		searchBoost = Float.parseFloat(value.getLexicalForm());
                		return searchBoost;
                	}
                }
                else{
                	    return null;
                }
            } catch (Exception e){
            	log.error(e,e); 
                return null;            	
            }finally{
            	dataset.getLock().leaveCriticalSection();
            	w.close();
            }
        }
        return null;
    }    

	@Override
	public String getMainImageUri() {  
		if (this.mainImageUri != NOT_INITIALIZED) {
			return mainImageUri;
		} else {
			for (ObjectPropertyStatement stmt : getObjectPropertyStatements()) {
				if (stmt.getPropertyURI()
						.equals(VitroVocabulary.IND_MAIN_IMAGE)) {
					mainImageUri = stmt.getObjectURI();
					return mainImageUri;
				}
			}
			return null;
		}
	}

	@Override
	public String getImageUrl() { 
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.instanceFromEntityUri(
			        webappDaoFactory, this);
			log.trace("figured imageInfo for " + getURI() + ": '"
					+ this.imageInfo + "'");
		}
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
			log.trace("imageInfo for " + getURI() + " is empty.");
		}
		return this.imageInfo.getMainImage().getBytestreamAliasUrl();
	}

	@Override
	public String getThumbUrl() { 
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.instanceFromEntityUri(
			        webappDaoFactory, this);
			log.trace("figured imageInfo for " + getURI() + ": '"
					+ this.imageInfo + "'");
		}
		if (this.imageInfo == null) {
			this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
			log.trace("imageInfo for " + getURI() + " is empty.");
		}
		return this.imageInfo.getThumbnail().getBytestreamAliasUrl();
	}

	@Override
	public boolean hasThumb(){
	    if( _hasThumb != null ){
	        return _hasThumb;	    
        }else{            
            String[] graphVars = { "?g" };
            String ask = 
                "ASK { GRAPH ?g  " +
                "  { <" + individualURI + "> <http://vitro.mannlib.cornell.edu/ns/vitro/public#mainImage> ?mainImage . \n" +
                "    ?mainImage <http://vitro.mannlib.cornell.edu/ns/vitro/public#thumbnailImage> ?thumbImage . }\n"  +                      
                        WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
                "}";
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            try{                
                _hasThumb = QueryExecutionFactory.create(QueryFactory.create(ask), dataset).execAsk();                      
            }catch(Exception ex){
                _hasThumb = false;
                log.error(ex,ex);
            }finally{
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
            return _hasThumb;
        }
	}
	
	
	public String getAnchor() { 
        if (this.anchor != null) {
            return anchor;
        } else {
            doUrlAndAnchor();
            return anchor;
        }
    }

    public String getUrl() { 
        if (this.url != null) {
            return url;
        } else {
            doUrlAndAnchor();
            return url;
        }
    }

    private void doUrlAndAnchor() { 
        Model tempModel = ModelFactory.createDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM);
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
        try {
            String graphVars[] = { "?g" };
        	StringBuffer selectPrimaryLinkQueryBuff = new StringBuffer().append(
        		"SELECT ?url ?anchor \n" ).append(
        		"WHERE{ GRAPH ?g { \n " ).append(
        		"    <" + this.individualURI + "> ").append(
        		             "<" + VitroVocabulary.PRIMARY_LINK + "> " ).append(
        		                     "?link . \n").append(
        		"    ?link <" + VitroVocabulary.LINK_URL + "> ?url . \n" 
        		        ).append(
        		"    ?link <" + VitroVocabulary.LINK_ANCHOR + "> ?anchor . \n" 
        		        ).append(
        		"} \n")
        		.append(WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode))
        		.append("}");
        	QueryExecution qexec = QueryExecutionFactory.create(
                    QueryFactory.create(selectPrimaryLinkQueryBuff.toString())
                            , dataset);
        	try {
        	    ResultSet linkResults = qexec.execSelect();
        	    if (linkResults.hasNext()) {
        	        QuerySolution solution = linkResults.next();
        	        this.setUrl(solution.getLiteral("url").getLexicalForm());
        	        this.setAnchor(solution.getLiteral("anchor")
        	                .getLexicalForm());
        	    }
        	} finally {
        	    qexec.close();
        	}            
        } finally {
        	tempModel.close();
        	ontModel.close();
        	dataset.getLock().leaveCriticalSection();
        	w.close();
        }
    }

    public List <Link> getLinksList() { 
        if (this.linksList != null) {
            return this.linksList;
        } else {
            try {
                webappDaoFactory.getLinksDao().addLinksToIndividual(this);
            } catch (Exception e) {
                log.debug(this.getClass().getName() + 
                        " could not addLinksToIndividual for " + this.getURI());
            }
            return this.linksList;
        }
    }

    public Link getPrimaryLink() { 
        if (this.primaryLink != null) {
            return this.primaryLink;
        } else {
            try {
                webappDaoFactory.getLinksDao().addPrimaryLinkToIndividual(this);
            } catch (Exception e) {
                log.debug(this.getClass().getName() + 
                        " could not addPrimaryLinkToIndividual for " + 
                                this.getURI());
            }
            return this.primaryLink;
        }
    }


    public List<String> getKeywords() { 
        if (this.keywords != null) {
            return this.keywords;
        } else {
            try {
                this.setKeywords(webappDaoFactory.getIndividualDao()
                        .getKeywordsForIndividual(this.getURI()));
            } catch (Exception e) {
                log.debug(this.getClass().getName() + 
                        " could not getKeywords for " + this.getURI());
            }
            return this.keywords;
        }
    }
    
    public List<Keyword> getKeywordObjects() {
        if (this.keywordObjects != null) { 
            return this.keywordObjects;
        } else {
            try {
                this.setKeywordObjects(webappDaoFactory.getIndividualDao()
                        .getKeywordObjectsForIndividual(this.getURI()));
            } catch (Exception e) {
                log.error(this.getClass().getName() + 
                        " could not get Keyword Objects for " + this.getURI());
            }
        }
        return this.keywordObjects;
    }

    public List<ObjectPropertyStatement> getObjectPropertyStatements() { 
        if (this.objectPropertyStatements != null) {
            return this.objectPropertyStatements;
        } else {
            try {
                webappDaoFactory.getObjectPropertyStatementDao()
                        .fillExistingObjectPropertyStatements(this);
            } catch (Exception e) {
                log.error(this.getClass().getName() + 
                        " could not fill existing ObjectPropertyStatements for "
                        + this.getURI(), e);
            }
            return this.objectPropertyStatements;
        }
    }
    
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyURI) {
    	if (propertyURI == null) {
    		return null;
    	}
    	List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList
    	        <ObjectPropertyStatement>();
	    Model tempModel = ModelFactory.createDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM);
        DatasetWrapper w = getDatasetWrapper();
        Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
    	try {
    	    String[] graphVars = { "?g" };
    		String valuesOfProperty = 
    			"CONSTRUCT{<" + this.individualURI + "> <" + propertyURI + "> ?object}" +
    			"WHERE{ GRAPH ?g { <" + this.individualURI + "> <" + propertyURI + "> ?object} \n" +
    			WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
    			"}";
    	    tempModel = QueryExecutionFactory.create(QueryFactory.create(valuesOfProperty), dataset).execConstruct();
    	    ontModel.add(tempModel.listStatements());
    	    Resource ontRes = ontModel.getResource(this.individualURI);
    	    StmtIterator sit = ontRes.listProperties(ontRes.getModel().getProperty(propertyURI));
    	    while (sit.hasNext()) {
    			Statement s = sit.nextStatement();
    			if (!s.getSubject().canAs(OntResource.class) || !s.getObject().canAs(OntResource.class)) {
    			    continue;	
    			}
    			Individual subj = new IndividualSDB(((OntResource) s.getSubject().as(OntResource.class)).getURI(), this.dwf, datasetMode, webappDaoFactory);
    			Individual obj = new IndividualSDB(((OntResource) s.getObject().as(OntResource.class)).getURI(), this.dwf, datasetMode, webappDaoFactory);
    			ObjectProperty op = webappDaoFactory.getObjectPropertyDao().getObjectPropertyByURI(s.getPredicate().getURI());
    			if (subj != null && obj != null && op != null) {
    				ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
    				ops.setSubject(subj);
    				ops.setSubjectURI(subj.getURI());
    				ops.setObject(obj);
    				ops.setObjectURI(obj.getURI());
    				ops.setProperty(op);
    				ops.setPropertyURI(op.getURI());
    				objectPropertyStatements.add(ops);
    			}
    		}
     	} finally {
    		tempModel.close();
    		ontModel.close();
     		dataset.getLock().leaveCriticalSection();
     		w.close();
    	}
     	return objectPropertyStatements;
    }

    @Override
    public List<Individual> getRelatedIndividuals(String propertyURI) { 
    	if (propertyURI == null) {
    		return null;
    	}
    	List<Individual> relatedIndividuals = new ArrayList<Individual>();
    	
    	DatasetWrapper w = getDatasetWrapper();
    	Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
    	try {
    	    String[] graphVars = { "?g" };
    		String valuesOfProperty = 
    			"SELECT ?object" +
    			"WHERE{ GRAPH ?g { <" + this.individualURI + "> <" + 
    			        propertyURI + "> ?object} \n" +
    			        WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +       
    			 "}";
    		ResultSet values = QueryExecutionFactory.create(
    		        QueryFactory.create(valuesOfProperty), dataset)
    		                .execSelect();
    		QuerySolution result = null;
    	    while (values.hasNext()) {
    	    	result = values.next();
    	    	RDFNode value = result.get("object");
    	    	if (value.canAs(OntResource.class)) {
        	    	relatedIndividuals.add(
        	    		new IndividualSDB(
        	    		        ((OntResource) value.as(OntResource.class))
        	    		                .getURI(), 
        	    		                this.dwf, 
        	    		                datasetMode, 
        	    		                webappDaoFactory) );  
        	    } 
    	    }
    	} finally {
    		dataset.getLock().leaveCriticalSection();
    		w.close();
    	}
    	return relatedIndividuals;
    }
    
    @Override
    public Individual getRelatedIndividual(String propertyURI) { 
    	if (propertyURI == null) {
    		return null;
    	}
    	DatasetWrapper w = getDatasetWrapper();
    	Dataset dataset = w.getDataset();
    	dataset.getLock().enterCriticalSection(Lock.READ);
    	try {
    	    String[] graphVars = { "?g" };
    		String valueOfProperty = 
    			"SELECT ?object " +
    			"WHERE{ GRAPH ?g { <" + this.individualURI + "> <" + 
    			        propertyURI + "> ?object} \n" +
    			        WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
    			"}";
    		QueryExecution qe = QueryExecutionFactory.create(
                    QueryFactory.create(valueOfProperty), dataset);
    		try {
    		    ResultSet results = qe.execSelect();
        		if (results.hasNext()) {
            		QuerySolution result = results.next();
            		RDFNode value = result.get("object");
            	    if (value != null && value.canAs(OntResource.class)) {
            	    	return new IndividualSDB(
            	    	        ((OntResource) value.as(OntResource.class)).getURI(), 
            	    	                dwf, datasetMode, webappDaoFactory);  
            	    } 
        	    }
        		return null;
    		} finally {
    		    qe.close();
    		}
    	} finally {
    		dataset.getLock().leaveCriticalSection();
    		w.close();
    	}
    }
    
    public List<ObjectProperty> getObjectPropertyList() { 
        if (this.propertyList != null) {
            return this.propertyList;
        } else {
            try {
                webappDaoFactory.getObjectPropertyDao()
                        .fillObjectPropertiesForIndividual( this );
            } catch (Exception e) {
                log.error(this.getClass().getName() + 
                        " could not fillEntityProperties for " + this.getURI());
            }
            return this.propertyList;
        }
    }

    @Override 
    public List<ObjectProperty> getPopulatedObjectPropertyList() {
        if (populatedObjectPropertyList == null) {
            populatedObjectPropertyList = webappDaoFactory
                    .getObjectPropertyDao().getObjectPropertyList(this);
        }
        return populatedObjectPropertyList;       
    }
    
    @Override
    public Map<String,ObjectProperty> getObjectPropertyMap() { 
    	if (this.objectPropertyMap != null) {
    		return objectPropertyMap;
    	} else {
    		Map map = new HashMap<String,ObjectProperty>();
    		if (this.propertyList == null) {
    			getObjectPropertyList();
    		}
    		for (Iterator i = this.propertyList.iterator(); i.hasNext();) { 
    			ObjectProperty op = (ObjectProperty) i.next();
    			if (op.getURI() != null) {
    				map.put(op.getURI(), op);
    			}
    		}
    		this.objectPropertyMap = map;
    		return map;    		
    	}
    }

    public List<DataPropertyStatement> getDataPropertyStatements() { 
        if (this.dataPropertyStatements != null) {
            return this.dataPropertyStatements;
        } else {
            try {
                webappDaoFactory.getDataPropertyStatementDao()
                        .fillExistingDataPropertyStatementsForIndividual(this);
            } catch (Exception e) {
                log.error(this.getClass().getName() + 
                        " could not fill existing DataPropertyStatements for "
                                + this.getURI());
            }
            return this.dataPropertyStatements;
        }
    }

    public List getDataPropertyList() {
        if (this.datatypePropertyList != null) { 
            return this.datatypePropertyList;
        } else {
            try {
                webappDaoFactory.getDataPropertyDao()
                        .fillDataPropertiesForIndividual( this );
            } catch (Exception e) {
                log.error(this.getClass().getName() + 
                        " could not fill data properties for " + this.getURI());
            }
            return this.datatypePropertyList;
        }
    }

    @Override 
    public List<DataProperty> getPopulatedDataPropertyList() {
        if (populatedDataPropertyList == null) {
            populatedDataPropertyList = webappDaoFactory.getDataPropertyDao()
                    .getDataPropertyList(this);
        }
        return populatedDataPropertyList;       
    }
    
    @Override
    public Map<String,DataProperty> getDataPropertyMap() { 
    	if (this.dataPropertyMap != null) {
    		return dataPropertyMap;
    	} else {
    		Map map = new HashMap<String,DataProperty>();
    		if (this.datatypePropertyList == null) {
    			getDataPropertyList();
    		}
    		for (Iterator i = this.datatypePropertyList.iterator(); i.hasNext();) { 
    			DataProperty dp = (DataProperty) i.next();
    			if (dp.getURI() != null) {
    				map.put(dp.getURI(), dp);
    			}
    		}
    		this.dataPropertyMap = map;
    		return map;    		
    	}
    }

    public List<DataPropertyStatement> getExternalIds() { 
        if (this.externalIds != null) {
            return this.externalIds;
        } else {
            try {
                List<DataPropertyStatement> dpsList = 
                        new ArrayList<DataPropertyStatement>();
                dpsList.addAll(webappDaoFactory.getIndividualDao()
                        .getExternalIds(this.getURI(), null));
                this.externalIds = dpsList;
            } catch (Exception e) {
                log.error(this.getClass().getName() + 
                        " could not fill external IDs for " + this.getURI());
            }
            return this.externalIds;
        }
    }
    
    @Override
    public List<VClass> getVClasses() { 
    	return getVClasses(false);
    }
    
    @Override
    public List<VClass> getVClasses(boolean direct) {
    	if (direct) {
    		if (directVClasses != null) {
    			return directVClasses;
    		} else {
    			directVClasses = getMyVClasses(true);
    			return directVClasses;
    		}
    	} else {
    		if (allVClasses != null) {
    			return allVClasses;
    		} else {
    			allVClasses = getMyVClasses(false);
    			return allVClasses;
    		}
    	}
    }
    
    private List<VClass> getMyVClasses(boolean direct) {
		List<VClass> vClassList = new ArrayList<VClass>(); 
		Model tempModel = null;
		if (ind.getModel().contains((Resource) null, RDF.type, (RDFNode) null)){
		    tempModel = ind.getModel();
		} else {
    		String getTypes = 
        		"CONSTRUCT{ <" + this.individualURI + "> <" + RDF.type +
        		        "> ?types }\n" +
        		"WHERE{ GRAPH " + 
        		((direct) 
        		    ? "<http://vitro.mannlib.cornell.edu/default/vitro-kb-2>" 
        		    : "?g") 
        		+ " { <" + this.individualURI +"> <" +RDF.type+ "> ?types } \n" ;
    		
    		    if (!direct) {
    		        String[] graphVars = { "?g" };
    		        getTypes += WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode);
    		    }
    		
        		getTypes += "} \n";        	
        	DatasetWrapper w = getDatasetWrapper();
        	Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
        	try {
        	    tempModel = QueryExecutionFactory.create(
        	            QueryFactory.create(getTypes), dataset).execConstruct();
        	} finally {
        	    dataset.getLock().leaveCriticalSection();
        	    w.close();
        	}
		}
    	StmtIterator stmtItr = tempModel.listStatements(
    	        (Resource) null, RDF.type, (RDFNode) null);
    	LinkedList<String> list = new LinkedList<String>();
    	while(stmtItr.hasNext()){
    		Statement stmt = stmtItr.nextStatement();
    		if (stmt.getObject().isResource() && !stmt.getObject().isAnon()) {
    			list.add(((Resource) stmt.getObject()).getURI());
    		}
    	}
    	Iterator<String> itr = null;
    	VClassDao checkSubClass = this.webappDaoFactory.getVClassDao();
    	boolean directTypes = false;
    	String currentType = null;
	    ArrayList<String> done = new ArrayList<String>();
	    
	    /* Loop for comparing starts here */
	    if(direct){
        	while(!directTypes){
        		 itr = list.listIterator();
        		 
        		do{
        			if(itr.hasNext()){
        		 currentType = itr.next();}
        			else{
        				directTypes = true; // get next element for comparison
        			 break;}
        		}while(done.contains(currentType));
        		
        		if(directTypes)
        			break; 
        		    // check to see if it's all over otherwise start comparing
        		else
        	    itr = list.listIterator();	
        		
            	while(itr.hasNext()){
            		String nextType = itr.next();
            	    if(checkSubClass.isSubClassOf(currentType, nextType) 
            	            && !currentType.equalsIgnoreCase(nextType)){
            	    	itr.remove();
            	    }
            	}
            	
            	done.add(currentType);  // add the uri to done list. 
        	}
	    }
    	
    	/* Loop for comparing ends here */
	    Iterator<String> typeIt = list.iterator();
		
		for (Iterator it = typeIt; it.hasNext();) {
			Resource type = ResourceFactory
			        .createResource(it.next().toString());
			String typeURI = (!type.isAnon()) 
			        ? type.getURI() 
			        : VitroVocabulary.PSEUDO_BNODE_NS 
			                + type.getId().toString();
			if (type.getNameSpace() == null || 
			        (!webappDaoFactory.getNonuserNamespaces()
			                .contains(type.getNameSpace())) ) {
				VClass vc = webappDaoFactory.getVClassDao()
				        .getVClassByURI(type.getURI());
				if (vc != null) {
					vClassList.add(vc);
				}
			}
		}

		try {
			Collections.sort(vClassList);
		} catch (Exception e) {
		    log.error("Unable to sort VClass list", e);
		}
		
		return vClassList;
	}
    
	/**
	 * The base method in {@link IndividualImpl} is adequate if the reasoner is
	 * up to date. 
	 * 
	 * If the base method returns false, check directly to see if
	 * any of the super classes of the direct classes will satisfy this request.
	 */
	@Override
	public boolean isVClass(String uri) { 
    	if (uri == null) {
    		return false;
    	}

		if (super.isVClass(uri)) {
			return true;
		}

		VClassDao vclassDao = webappDaoFactory.getVClassDao();
		for (VClass vClass : getVClasses(true)) {
			for (String superClassUri: vclassDao.getAllSuperClassURIs(
			        vClass.getURI())) {
				if (uri.equals(superClassUri)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isMemberOfClassProhibitedFromSearch(ProhibitedFromSearch pfs) {  

		List<VClass> types =  getVClasses(false);
		Iterator<VClass> itr = types.iterator();

		while(itr.hasNext()) {
			String typeURI = itr.next().getURI();
			if (pfs.isClassProhibited(typeURI)) {
				return true;
			}
		}

		return false;

	}
    /**
     * Overriding the base method so that we can do the sorting by arbitrary property here.  An
     * IndividualSDB has a reference back to the model; everything else is just a dumb bean (for now).
     */
    @Override
    protected void sortEnts2EntsForDisplay(){ 
        if( getObjectPropertyList() == null ) return;

        Iterator it = getObjectPropertyList().iterator();
        while(it.hasNext()){
            ObjectProperty prop = (ObjectProperty)it.next();
        /*  if (prop.getObjectIndividualSortPropertyURI()==null) {
            	prop.sortObjectPropertyStatementsForDisplay(prop,prop.getObjectPropertyStatements());
            } else {*/
            	prop.sortObjectPropertyStatementsForDisplay(prop,prop.getObjectPropertyStatements());
        /*  }*/
        }
    }
    
    private Collator collator = Collator.getInstance();
    
    private void sortObjectPropertyStatementsForDisplay(ObjectProperty prop) { 
    	try {
    		log.info("Doing special sort for "+prop.getDomainPublic());
    		final String sortPropertyURI = prop.getObjectIndividualSortPropertyURI();
            String tmpDir;
            boolean tmpAsc;

            tmpDir = prop.getDomainEntitySortDirection();

            //valid values are "desc" and "asc", anything else will default to ascending
            tmpAsc = !"desc".equalsIgnoreCase(tmpDir);

            final boolean dir = tmpAsc;
            Comparator comp = new Comparator(){
                final boolean cAsc = dir;

                public final int compare(Object o1, Object o2){
                    ObjectPropertyStatement e2e1= (ObjectPropertyStatement)o1, e2e2=(ObjectPropertyStatement)o2;
                    Individual e1 , e2;
                    e1 = e2e1 != null ? e2e1.getObject():null;
                    e2 = e2e2 != null ? e2e2.getObject():null;

                    Object val1 = null, val2 = null;
                    if( e1 != null ){
                        try {
                        	DataProperty dp = e1.getDataPropertyMap().get(sortPropertyURI);
                        	if (dp.getDataPropertyStatements() != null && dp.getDataPropertyStatements().size()>0) {
                        		val1 = dp.getDataPropertyStatements().get(0).getData();
                        	}
                        }
                        catch (Exception e) {
                        	val1 = "";
                        }
                    } else {
                        log.warn( "IndividualSDB.sortObjectPropertiesForDisplay passed object property statement with no range entity.");
                    }

                    if( e2 != null ){
                        try {
                        	DataProperty dp = e2.getDataPropertyMap().get(sortPropertyURI);
                        	if (dp.getDataPropertyStatements() != null && dp.getDataPropertyStatements().size()>0) {
                        		val2 = dp.getDataPropertyStatements().get(0).getData();
                        	}
                        }
                        catch (Exception e) {
                        	val2 = "";
                        }
                    } else {
                        log.warn( "IndividualSDB.sortObjectPropertyStatementsForDisplay() was passed an object property statement with no range entity.");
                    }

                    int rv = 0;
                    try {
                        if( val1 instanceof String )
                        	rv = collator.compare( ((String)val1) , ((String)val2) );
                            //rv = ((String)val1).compareTo((String)val2);
                        else if( val1 instanceof Date ) {
                            DateTime dt1 = new DateTime((Date)val1);
                            DateTime dt2 = new DateTime((Date)val2);
                            rv = dt1.compareTo(dt2);
                        }
                        else
                            rv = 0;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if( cAsc )
                        return rv;
                    else
                        return rv * -1;
                }
            };
            try {
                Collections.sort(getObjectPropertyStatements(), comp);
            } catch (Exception e) {
                log.error("Exception sorting object property statements for object property "+this.getURI());
            }

    		
    	} catch (Exception e) {
    		log.error(e, e);
    	}
    }
    
    
}
