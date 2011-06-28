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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB.IndividualNotFoundException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;

public class ObjectPropertyStatementDaoSDB extends
		ObjectPropertyStatementDaoJena implements ObjectPropertyStatementDao {

	private DatasetWrapperFactory dwf;
	private SDBDatasetMode datasetMode;
	
	public ObjectPropertyStatementDaoSDB(
	            DatasetWrapperFactory dwf, 
	            SDBDatasetMode datasetMode,
	            WebappDaoFactoryJena wadf) {
		super (dwf, wadf);
		this.dwf = dwf;
		this.datasetMode = datasetMode;
	}
	
	@Override
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity.getURI() == null)
            return entity;
        else {
        	Map<String, ObjectProperty> uriToObjectProperty = new HashMap<String,ObjectProperty>();
        	String query = "CONSTRUCT { \n" +
        			       "   <" + entity.getURI() + "> ?p ?o . \n" +
//        			       "   ?o a ?oType . \n" +
//        			       "   ?o <" + RDFS.label.getURI() + "> ?oLabel .  \n" +
//        			       "   ?o <" + VitroVocabulary.MONIKER + "> ?oMoniker  \n" +
        			       "} WHERE { \n" +
        			       "   { <" + entity.getURI() + "> ?p ?o } \n" +
//        			       "   UNION { <" + entity.getURI() + "> ?p ?o . ?o a ?oType } \n" +
//        			       "   UNION { <" + entity.getURI() + "> ?p ?o . \n" +
//        			       "           ?o <" + RDFS.label.getURI() + "> ?oLabel } \n" +
//        			       "   UNION { <" + entity.getURI() + "> ?p ?o . \n " +
//        			       "           ?o <" + VitroVocabulary.MONIKER + "> ?oMoniker } \n" +
        			       "}";
        	long startTime = System.currentTimeMillis();
        	Model m = null;
        	DatasetWrapper w = dwf.getDatasetWrapper();
        	Dataset dataset = w.getDataset();
        	dataset.getLock().enterCriticalSection(Lock.READ);
        	try {
        		m = QueryExecutionFactory.create(QueryFactory.create(query), dataset).execConstruct();
        	} finally {
        		dataset.getLock().leaveCriticalSection();
        		w.close();
        	}
        	if (log.isDebugEnabled()) {
	        	log.debug("Time (ms) to query for related individuals: " + (System.currentTimeMillis() - startTime));
	        	if (System.currentTimeMillis() - startTime > 1000) {
	        		//log.debug(query);
	        		log.debug("Results size (statements): " + m.size());
	        	}
        	}
        	
        	OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
        	ontModel.enterCriticalSection(Lock.READ);
        	try {
	            Resource ind = ontModel.getResource(entity.getURI());
	            List<ObjectPropertyStatement> objPropertyStmtList = new ArrayList<ObjectPropertyStatement>();
	            ClosableIterator<Statement> propIt = ind.listProperties();
	            try {
	                while (propIt.hasNext()) {
	                    Statement st = (Statement) propIt.next();
	                    if (st.getObject().isResource() && !(NONUSER_NAMESPACES.contains(st.getPredicate().getNameSpace()))) {
	                        try {
	                            ObjectPropertyStatement objPropertyStmt = new ObjectPropertyStatementImpl();
	                            objPropertyStmt.setSubjectURI(entity.getURI());
	                            objPropertyStmt.setSubject(entity);
	                            objPropertyStmt.setObjectURI(((Resource)st.getObject()).getURI());
	                            
	                            objPropertyStmt.setPropertyURI(st.getPredicate().getURI());
                                Property prop = st.getPredicate();
                                if( uriToObjectProperty.containsKey(prop.getURI())){
                                	objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
                                }else{
                                	ObjectProperty p = getWebappDaoFactory().getObjectPropertyDao().getObjectPropertyByURI(prop.getURI());
                                	if( p != null ){
                                		uriToObjectProperty.put(prop.getURI(), p);
                                		objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
                                	}else{
                                		//if ObjectProperty not found in ontology, skip it
                                		continue;
                                	}
                                }                                

	                            if (objPropertyStmt.getObjectURI() != null) {
	                                //this might throw IndividualNotFoundException
	                            	try {
                                         Individual objInd = new IndividualSDB(
                                             objPropertyStmt.getObjectURI(), 
                                             this.dwf, 
                                             datasetMode,
                                             getWebappDaoFactory());
                                         objPropertyStmt.setObject(objInd);	
	                            	} catch (IndividualNotFoundException infe) {
	                            		 log.warn("Individual Not Found for uri: " + objPropertyStmt.getObjectURI());
	                            		 continue;
	                            	}                 
	                            }
	                            
	                            //only add statement to list if it has its values filled out
	                            if (    (objPropertyStmt.getSubjectURI() != null) 
	                                 && (objPropertyStmt.getPropertyURI() != null) 
	                                 && (objPropertyStmt.getObject() != null) ) {
	                                objPropertyStmtList.add(objPropertyStmt);                           
	                            } 
	                            
	                        } catch (Throwable t){
	                            log.error(t,t);
                                continue;
	                        }
	                    }
	                }
	            } finally {
	                propIt.close();
	            }
	            entity.setObjectPropertyStatements(objPropertyStmtList);
        	} finally {
        		ontModel.leaveCriticalSection();
        	}
            return entity;
        }
    }
	
}
