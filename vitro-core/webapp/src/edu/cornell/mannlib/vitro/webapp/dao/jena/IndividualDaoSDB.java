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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class IndividualDaoSDB extends IndividualDaoJena {

	private DatasetWrapperFactory dwf;
    private SDBDatasetMode datasetMode;
	private WebappDaoFactoryJena wadf;
	
    public IndividualDaoSDB(DatasetWrapperFactory dwf, 
                            SDBDatasetMode datasetMode, 
                            WebappDaoFactoryJena wadf) {
        super(wadf);
        this.dwf = dwf;
        this.datasetMode = datasetMode;
    }
    
    protected DatasetWrapper getDatasetWrapper() {
    	return dwf.getDatasetWrapper();
    }
    
    protected Individual makeIndividual(String individualURI) {
        try {
            return new IndividualSDB(individualURI, this.dwf, datasetMode, getWebappDaoFactory());
        } catch (IndividualNotFoundException e) {
            // If the individual does not exist, return null.
            return null;
        }
    }

    private static final Log log = LogFactory.getLog(IndividualDaoSDB.class.getName());

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getABoxModel();
    }
    
    private static final boolean SKIP_INITIALIZATION = true;
    
    @Override
    public List getIndividualsByVClassURI(String vclassURI, int offset, int quantity ) {

    	if (vclassURI==null) {
            return null;
        }
        
        List<Individual> ents = new ArrayList<Individual>();
        
        Resource theClass = (vclassURI.indexOf(PSEUDO_BNODE_NS) == 0) 
            ? getOntModel().createResource(new AnonId(vclassURI.split("#")[1]))
            : ResourceFactory.createResource(vclassURI);
    
        if (theClass.isAnon() && theClass.canAs(UnionClass.class)) {
        	UnionClass u = (UnionClass) theClass.as(UnionClass.class);
        	for (OntClass operand : u.listOperands().toList()) {
        		VClass vc = new VClassJena(operand, getWebappDaoFactory());
        		ents.addAll(getIndividualsByVClass(vc));
        	}
        } else {
    	    DatasetWrapper w = getDatasetWrapper();
    	    Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
        	try {
        	    String[] graphVars = {"?g", "?h", "?i"};
        		String query = 
    	    		"SELECT DISTINCT ?ind ?label ?moniker " +
    	    		"WHERE " +
    	    		 "{ \n" +
    	    		 	"{ \n" +
                        "    ?ind a <" + theClass.getURI() + "> . \n" +
    	    		 	"    ?ind  <" + RDFS.label.getURI() + "> ?label \n" +
    	    		 	"} \n" +
    	    		 	"UNION { \n" +
                        "    ?ind a <" + theClass.getURI() + "> . \n" +
    	    		 	"    ?ind  <" + VitroVocabulary.MONIKER + "> ?moniker \n" +
    	    		 	"} \n" +
    	    		 "} ORDER BY ?ind ?label";
        		ResultSet rs =QueryExecutionFactory.create(
        		        QueryFactory.create(query), dataset)
        		        .execSelect();
        		String uri = null;
        		String label = null;
        		String moniker = null;
        		while (rs.hasNext()) {
        		    QuerySolution sol = rs.nextSolution();
        		    Resource currRes = sol.getResource("ind");
        		    if (currRes.isAnon()) {
        		        continue;
        		    }
        		    if (uri != null && !uri.equals(currRes.getURI())) {
        		        Individual ent = makeIndividual(uri, label, moniker);
        		        if (ent != null) {
        		            ents.add(ent);
        		        }
        	            uri = currRes.getURI();
        	            label = null;
        	            moniker = null;
        		    } else if (uri == null) {
        		        uri = currRes.getURI();
        		    }
                    Literal labelLit = sol.getLiteral("label");
                    if (labelLit != null) {
                        label = labelLit.getLexicalForm();
                    }
                    Literal monikerLit = sol.getLiteral("moniker");
                    if (monikerLit != null) {
                        moniker = monikerLit.getLexicalForm();
                    }
                    if (!rs.hasNext()) {
                        Individual ent = makeIndividual(uri, label, moniker);
                        if (ent != null) {
                            ents.add(ent);
                        }
                    }
        		}
        	} finally {
        		dataset.getLock().leaveCriticalSection();
        		w.close();
        	} 
        }
        
        java.util.Collections.sort(ents);
        
        if (quantity > 0 && offset > 0) {
            List<Individual> sublist = new ArrayList<Individual>();
            for (int i = offset - 1; i < ((offset - 1) + quantity); i++) {
                sublist.add(ents.get(i));
            }
            return sublist;
        }
        
        return ents;

    }
    
    private Individual makeIndividual(String uri, String label, String moniker) {
        Individual ent = new IndividualSDB(uri, 
                this.dwf, datasetMode, getWebappDaoFactory(), 
                SKIP_INITIALIZATION);
        ent.setName(label);
        ent.setMoniker(moniker);
        return ent;
    }
	
    @Override
    public Individual getIndividualByURI(String entityURI) {
        if( entityURI == null || entityURI.length() == 0 ) {
            return null;
        } else {
        	return makeIndividual(entityURI);
        }
    }  
    
    /**
     * fills in the Individual objects needed for any ObjectPropertyStatements attached to the specified individual.
     * @param entity
     */
    private void fillIndividualsForObjectPropertyStatements(Individual entity){
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Iterator e2eIt = entity.getObjectPropertyStatements().iterator();
            while (e2eIt.hasNext()) {
                ObjectPropertyStatement e2e = (ObjectPropertyStatement) e2eIt.next();
                e2e.setSubject(makeIndividual(e2e.getSubjectURI()));
                e2e.setObject(makeIndividual(e2e.getObjectURI()));       
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }
    
    /**
     * In Jena it can be difficult to get an object with a given dataproperty if
     * you do not care about the datatype or lang of the literal.  Use this
     * method if you would like to ignore the lang and datatype.  
     */
    @Override
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value){        
        Property prop = null;
        if( RDFS.label.getURI().equals( dataPropertyUri )){
            prop = RDFS.label;
        }else{
            prop = getOntModel().getDatatypeProperty(dataPropertyUri);
        }

        if( prop == null ) {            
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because " + dataPropertyUri + "was not found in model.");
            return Collections.emptyList();
        }

        if( value == null ){
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because value was null");
            return Collections.emptyList();
        }
        
        Literal litv1 = getOntModel().createLiteral(value);        
        Literal litv2 = getOntModel().createTypedLiteral(value);   
        
        //warning: this assumes that any language tags will be EN
        Literal litv3 = getOntModel().createLiteral(value,"EN");        
        
        HashMap<String,Individual> individualsMap = new HashMap<String, Individual>();
                
        getOntModel().enterCriticalSection(Lock.READ);
        int count = 0;
        try{
            StmtIterator stmts
                = getOntModel().listStatements((Resource)null, prop, litv1);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    individualsMap.put(subUri,makeIndividual(subUri));
                }
            }
            
            stmts = getOntModel().listStatements((Resource)null, prop, litv2);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    individualsMap.put(subUri, makeIndividual(subUri));
                }                
            }
            
            stmts = getOntModel().listStatements((Resource)null, prop, litv3);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    individualsMap.put(subUri, makeIndividual(subUri));
                }                
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        
        List<Individual> rv = new ArrayList(individualsMap.size());
        rv.addAll(individualsMap.values());
        return rv;
    }
    
    @Override
    public Iterator getAllOfThisTypeIterator() {
        final List<String> list = 
            new LinkedList<String>();
        
        // get all labeled resources from any non-tbox and non-metadata graphs.
        String query = "SELECT DISTINCT ?ind WHERE { \n" +
                       "  GRAPH ?g { ?ind <" + RDFS.label.getURI() + "> ?label } \n" +
                       "  FILTER (?g != <" + JenaDataSourceSetupBase.JENA_APPLICATION_METADATA_MODEL + "> " +
                       "          && !regex(str(?g),\"tbox\")) \n " +
                       "}";
              
	    Query q = QueryFactory.create(query);
	    DatasetWrapper w = getDatasetWrapper();
	    Dataset dataset = w.getDataset();
	    dataset.getLock().enterCriticalSection(Lock.READ);
	    QueryExecution qe = QueryExecutionFactory.create(q, dataset);
	    try {
	        ResultSet rs = qe.execSelect();
	        while (rs.hasNext()) {
	        	Resource res = rs.next().getResource("ind");
	        	if (!res.isAnon()) {
	        		list.add(res.getURI());
	        	}
	        }
        } finally {
        	qe.close();
        	dataset.getLock().leaveCriticalSection();
        	w.close();
        }
        
        if (list.size() >0){
            log.info("Number of individuals from source: " + list.size());
            return new Iterator(){
                Iterator<String> innerIt = list.iterator();
                public boolean hasNext() { 
                    return innerIt.hasNext();                    
                }
                public Object next() {
                    String indURI = innerIt.next();
                    Individual ind = makeIndividual(indURI);
                    if (ind != null) {
                        return ind;
                    } else {
                        return new IndividualImpl(indURI);
                    }
                }
                public void remove() {
                    //not used
                }            
            };
        }
        else
            return null;
    }  

    @Override
    public Iterator getAllOfThisVClassIterator(String vClassURI) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List ents = new LinkedList();
            OntClass cls = getOntModel().getOntClass(vClassURI);
            Iterator indIt = cls.listInstances();
            while (indIt.hasNext()) {
                com.hp.hpl.jena.ontology.Individual ind = (com.hp.hpl.jena.ontology.Individual) indIt.next();
                Individual ent = makeIndividual(ind.getURI());
                if (ent != null) {
                    ents.add(ent);
                }
            }
            return ents.iterator();
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public Iterator getUpdatedSinceIterator(long updatedSince){
        List ents = new ArrayList();
        Date since = new DateTime(updatedSince).toDate();
        String sinceStr = xsdDateTimeFormat.format(since);
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Property modTimeProp = MODTIME;
            if (modTimeProp == null)
                modTimeProp = getOntModel().getProperty(VitroVocabulary.MODTIME);
            if (modTimeProp == null)
                return null; // throw an exception?
            String queryStr = "PREFIX vitro: <"+ VitroVocabulary.vitroURI+"> " +
                              "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                              "SELECT ?ent " +
                              "WHERE { " +
                              "     ?ent vitro:modTime ?modTime ." +
                              "     FILTER (xsd:dateTime(?modTime) >= \""+sinceStr+"\"^^xsd:dateTime) " +
                              "}";
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(query,getOntModel());
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = (QuerySolution) results.next();
                Resource res = (Resource) qs.get("?ent");
                com.hp.hpl.jena.ontology.Individual ent = getOntModel().getIndividual(res.getURI());
                if (ent != null) {
                    boolean userVisible = false;
                    ClosableIterator typeIt = ent.listRDFTypes(true);
                    try {
                        while (typeIt.hasNext()) {
                            Resource typeRes = (Resource) typeIt.next();
                            if (typeRes.getNameSpace() == null || (!NONUSER_NAMESPACES.contains(typeRes.getNameSpace()))) {
                                userVisible = true;
                                break;
                            }
                        }
                    } finally {
                        typeIt.close();
                    }
                    if (userVisible) {
                        Individual ind = makeIndividual(ent.getURI());
                        if (ind != null) {
                            ents.add(ind);
                        }
                    }
                }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return ents.iterator();
    }

    
}
