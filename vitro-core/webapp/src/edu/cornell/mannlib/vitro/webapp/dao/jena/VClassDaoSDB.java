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

import java.util.List;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode;

public class VClassDaoSDB extends VClassDaoJena {

	private DatasetWrapperFactory dwf;
	private SDBDatasetMode datasetMode;
	
    public VClassDaoSDB(DatasetWrapperFactory datasetWrapperFactory, 
                        SDBDatasetMode datasetMode,
                        WebappDaoFactoryJena wadf) {
        super(wadf);
        this.dwf = datasetWrapperFactory;
        this.datasetMode = datasetMode;
    }
    
    protected DatasetWrapper getDatasetWrapper() {
    	return dwf.getDatasetWrapper();
    }
    
    @Deprecated
    public void addVClassesToGroup(VClassGroup group, boolean includeUninstantiatedClasses, boolean getIndividualCount) {
        
        if (getIndividualCount) {
            group.setIndividualCount( getClassGroupInstanceCount(group));
        } 
        
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            if ((group != null) && (group.getURI() != null)) {                                
                Resource groupRes = ResourceFactory.createResource(group.getURI());
                AnnotationProperty inClassGroup = getOntModel().getAnnotationProperty(VitroVocabulary.IN_CLASSGROUP);
                if (inClassGroup != null) {
                    ClosableIterator annotIt = getOntModel().listStatements((OntClass)null,inClassGroup,groupRes);
                    try {
                        while (annotIt.hasNext()) {
                            try {
                                Statement annot = (Statement) annotIt.next();
                                Resource cls = (Resource) annot.getSubject();
                                VClass vcw = (VClass) getVClassByURI(cls.getURI());
                                if (vcw != null) {
                                    boolean classIsInstantiated = false;
                                    if (getIndividualCount) {                                                                            
                                    	int count = 0;                                    	
                                	    String[] graphVars = { "?g" };
                                		String countQueryStr = "SELECT COUNT(DISTINCT ?s) WHERE \n" +
                                		                       "{ GRAPH ?g { ?s a <" + cls.getURI() + "> } \n" +
                                		                       WebappDaoFactorySDB.getFilterBlock(graphVars, datasetMode) +
                                		                       "} \n";
                                		Query countQuery = QueryFactory.create(countQueryStr, Syntax.syntaxARQ);
                                		DatasetWrapper w = getDatasetWrapper();
                                		Dataset dataset = w.getDataset();
                                		dataset.getLock().enterCriticalSection(Lock.READ);                                    		
                                		try {
                                    		QueryExecution qe = QueryExecutionFactory.create(countQuery, dataset);
                                    		ResultSet rs = qe.execSelect();
                                    		count = Integer.parseInt(((Literal) rs.nextSolution().get(".1")).getLexicalForm());
                                		} finally {
                                		    dataset.getLock().leaveCriticalSection();
                                		    w.close();
                                		}
                                    	vcw.setEntityCount(count);
                                    	classIsInstantiated = (count > 0);
                                    } else if (includeUninstantiatedClasses == false) {
                                        // Note: to support SDB models, may want to do this with 
                                        // SPARQL and LIMIT 1 if SDB can take advantage of it
                                    	Model aboxModel = getOntModelSelector().getABoxModel();
                                    	aboxModel.enterCriticalSection(Lock.READ);
                                    	try {
	                                        ClosableIterator countIt = aboxModel.listStatements(null,RDF.type,cls);
	                                        try {
	                                            if (countIt.hasNext()) {
	                                            	classIsInstantiated = true;
	                                            }
	                                        } finally {
	                                            countIt.close();
	                                        }
                                    	} finally {
                                    		aboxModel.leaveCriticalSection();
                                    	}
                                    }
                                    
                                    if (includeUninstantiatedClasses || classIsInstantiated) {
                                        group.add(vcw);
                                    }
                                }
                            } catch (ClassCastException cce) {cce.printStackTrace();}
                        }
                    } finally {
                        annotIt.close();
                    }
                }
            }
            java.util.Collections.sort(group.getVitroClassList());
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }
        
//    protected void addIndividualCountToGroups( List<VClassGroup> cgList ){
//        for( VClassGroup cg : cgList){           
//            cg.setIndividualCount(getClassGroupInstanceCount(cg));
//        }        
//    }
    
    @Override
    int getClassGroupInstanceCount(VClassGroup vcg){
        int count = 0;               
        try {
            String queryText =              
                "SELECT COUNT( DISTINCT ?instance ) WHERE { \n" +
                "  GRAPH <urn:x-arq:UnionGraph> { \n" + 
                "      ?class <"+VitroVocabulary.IN_CLASSGROUP+"> <"+vcg.getURI() +"> .\n" +                
                "      ?instance a ?class .  \n" +
                "  } \n" +
                "} \n" ;
            
            Query countQuery = QueryFactory.create(queryText, Syntax.syntaxARQ);
            DatasetWrapper w = getDatasetWrapper();
            Dataset dataset = w.getDataset();
            dataset.getLock().enterCriticalSection(Lock.READ);
            try {
                QueryExecution qe = QueryExecutionFactory.create(countQuery, dataset);
                ResultSet rs = qe.execSelect();
                count = Integer.parseInt(((Literal) rs.nextSolution().get(".1")).getLexicalForm());
            } finally {
                dataset.getLock().leaveCriticalSection();
                w.close();
            }
        }catch(Exception ex){
            log.error("error in getClassGroupInstanceCount()", ex);
        }    
        
        return count;
    }

    
}
