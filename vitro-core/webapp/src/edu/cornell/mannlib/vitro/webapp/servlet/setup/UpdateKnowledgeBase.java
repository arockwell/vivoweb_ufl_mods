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

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.ontology.update.KnowledgeBaseUpdater;
import edu.cornell.mannlib.vitro.webapp.ontology.update.UpdateSettings;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;

/**
 * Invokes process to test whether the knowledge base needs any updating
 * to align with ontology changes.
 * @author bjl23
 *
 */
public class UpdateKnowledgeBase implements ServletContextListener {
	
	private final static Log log = LogFactory.getLog(UpdateKnowledgeBase.class);
	
	private static final String DATA_DIR = "/WEB-INF/ontologies/update/";
	private static final String LOG_DIR = "logs/";
	private static final String CHANGED_DATA_DIR = "changedData/";
	private static final String ASK_QUERY_FILE = DATA_DIR + "ask.sparql";
	private static final String ASK_EMPTY_QUERY_FILE = DATA_DIR + "askEmpty.sparql";
	private static final String ASK_EVER_QUERY_FILE = DATA_DIR + "askEver.sparql";
	private static final String SUCCESS_ASSERTIONS_FILE = DATA_DIR + "success.n3";
	private static final String SUCCESS_RDF_FORMAT = "N3";
	private static final String DIFF_FILE = DATA_DIR + "diff.tab.txt";
	private static final String LOG_FILE = DATA_DIR + LOG_DIR + "knowledgeBaseUpdate.log";
	private static final String ERROR_LOG_FILE = DATA_DIR + LOG_DIR + 	"knowledgeBaseUpdate.error.log";
	private static final String REMOVED_DATA_FILE = DATA_DIR + CHANGED_DATA_DIR + 	"removedData.n3";
	private static final String ADDED_DATA_FILE = DATA_DIR + CHANGED_DATA_DIR + "addedData.n3";
	private static final String SPARQL_CONSTRUCT_ADDITIONS_DIR = DATA_DIR + "sparqlConstructs/additions/";
	private static final String SPARQL_CONSTRUCT_ADDITIONS_PASS2_DIR = DATA_DIR + "sparqlConstructs/additions-pass2/";
	private static final String SPARQL_CONSTRUCT_DELETIONS_DIR = DATA_DIR + "sparqlConstructs/deletions/";
	private static final String MISC_REPLACEMENTS_FILE = DATA_DIR + "miscReplacements.rdf";
	private static final String OLD_TBOX_MODEL_DIR = DATA_DIR + "oldVersion/";
	private static final String NEW_TBOX_MODEL_DIR = "/WEB-INF/submodels/";
	private static final String OLD_TBOX_ANNOTATIONS_DIR = DATA_DIR + "oldAnnotations/";
	private static final String NEW_TBOX_ANNOTATIONS_DIR = "/WEB-INF/ontologies/user";
	
	public void contextInitialized(ServletContextEvent sce) {
				
	    if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
	    
		try {

			ServletContext ctx = sce.getServletContext();

			OntModelSelector oms = new SimpleOntModelSelector((OntModel) sce.getServletContext().getAttribute(JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME));
			
			UpdateSettings settings = new UpdateSettings();
			settings.setAskQueryFile(getAskQueryPath(ctx));
			settings.setAskEverQueryFile(getAskEverQueryPath(ctx));
			settings.setAskEmptyQueryFile(getAskEmptyQueryPath(ctx));
			settings.setDataDir(ctx.getRealPath(DATA_DIR));
			settings.setSparqlConstructAdditionsDir(ctx.getRealPath(SPARQL_CONSTRUCT_ADDITIONS_DIR));
			settings.setSparqlConstructAdditionsPass2Dir(ctx.getRealPath(SPARQL_CONSTRUCT_ADDITIONS_PASS2_DIR));
			settings.setSparqlConstructDeletionsDir(ctx.getRealPath(SPARQL_CONSTRUCT_DELETIONS_DIR));
			settings.setDiffFile(ctx.getRealPath(DIFF_FILE));
			settings.setSuccessAssertionsFile(ctx.getRealPath(SUCCESS_ASSERTIONS_FILE));
			settings.setSuccessRDFFormat(SUCCESS_RDF_FORMAT);
			settings.setLogFile(ctx.getRealPath(LOG_FILE));
			settings.setErrorLogFile(ctx.getRealPath(ERROR_LOG_FILE));
			settings.setAddedDataFile(ctx.getRealPath(ADDED_DATA_FILE));
			settings.setRemovedDataFile(ctx.getRealPath(REMOVED_DATA_FILE));
			WebappDaoFactory wadf = (WebappDaoFactory) ctx.getAttribute("webappDaoFactory");
			settings.setDefaultNamespace(wadf.getDefaultNamespace());
				
			settings.setOntModelSelector(oms);
			OntModel oldTBoxModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_MODEL_DIR));
			settings.setOldTBoxModel(oldTBoxModel);
			OntModel newTBoxModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_MODEL_DIR));
			settings.setNewTBoxModel(newTBoxModel);
			OntModel oldTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(OLD_TBOX_ANNOTATIONS_DIR));
			settings.setOldTBoxAnnotationsModel(oldTBoxAnnotationsModel);
			OntModel newTBoxAnnotationsModel = loadModelFromDirectory(ctx.getRealPath(NEW_TBOX_ANNOTATIONS_DIR));
			settings.setNewTBoxAnnotationsModel(newTBoxAnnotationsModel);
			
			try {
				
			  KnowledgeBaseUpdater ontologyUpdater = new KnowledgeBaseUpdater(settings);
			  
			  try {
				  if (ontologyUpdater.updateRequired()) {
					  ctx.setAttribute(LuceneSetup.INDEX_REBUILD_REQUESTED_AT_STARTUP, Boolean.TRUE);
					  doMiscAppMetadataReplacements(ctx.getRealPath(MISC_REPLACEMENTS_FILE), oms);
					  reloadDisplayModel(ctx);
				  }
			  } catch (Throwable t){
				  log.warn("Unable to perform miscellaneous application metadata replacements", t);
			  }
			  
			  ontologyUpdater.update();
				
			} catch (IOException ioe) {
				String errMsg = "IOException updating knowledge base " +
					"for ontology changes: ";
				// Tomcat doesn't always seem to print exceptions thrown from
				// context listeners
				System.out.println(errMsg);
				ioe.printStackTrace();
				throw new RuntimeException(errMsg, ioe);
			}	
		
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
	}	
	
	/**
	 * 
	 * Behavior changed from 1.0
	 * 
	 * Replace any triple X P S in the application metadata model
	 * with X P T where X, P, and T are specified in the input file
	 * @param filename containing replacement values
	 * @param OntModelSelector oms
	 */
	private void doMiscAppMetadataReplacements(String filename, OntModelSelector oms) {
		try {
		    Model replacementValues = ModelFactory.createDefaultModel();
		    OntModel applicationMetadataModel = oms.getApplicationMetadataModel();
		    FileInputStream fis = new FileInputStream(new File(filename));
		    replacementValues.read(fis, null);
		    Model retractions = ModelFactory.createDefaultModel();
		    Model additions = ModelFactory.createDefaultModel();
		    StmtIterator replaceIt = replacementValues.listStatements();
		    while (replaceIt.hasNext()) {
		    	Statement replacement = replaceIt.nextStatement();
		    	applicationMetadataModel.enterCriticalSection(Lock.WRITE);
		    	try {
		    		StmtIterator stmtIt = 
		    			    applicationMetadataModel.listStatements( 
		    			    		replacement.getSubject(), 
		    			    		replacement.getPredicate(),
		    			    		(RDFNode) null);
		    		while (stmtIt.hasNext()) {
		    			Statement stmt = stmtIt.nextStatement();
		    			retractions.add(stmt);
		    			additions.add(stmt.getSubject(),
		    					replacement.getPredicate(), 
		    					replacement.getObject());
		    		}
		    		applicationMetadataModel.remove(retractions);
		    		applicationMetadataModel.add(additions);
		    	} finally {
		    		applicationMetadataModel.leaveCriticalSection();
		    	}
		    }
		} catch (FileNotFoundException fnfe) {
			log.info("No miscellaneous application metadata replacements were performed.");
		
		} catch (Exception e) {
			log.error("Error performing miscellaneous application metadata " +
					" replacements.", e);
		}
	}
	
	private void reloadDisplayModel(ServletContext ctx) {
	    log.info("Reloading display model");
	    Object o = ctx.getAttribute("displayOntModel");
	    if (o instanceof OntModel) {
	        OntModel displayModel = (OntModel) o;
	        displayModel.removeAll((Resource) null, (Property) null, (RDFNode) null);
	        if (displayModel.size() != 0) {
	            log.error("Display model not cleared successfully");
	        }
            JenaPersistentDataSourceSetup.readOntologyFilesInPathSet(
                    JenaPersistentDataSourceSetup.APPPATH, ctx, displayModel);
            log.info("Display model reloaded");
            if (displayModel.size() == 0) {
                log.warn("Display model empty after reloading");
            }
	    } else {
	        log.error("No display model found in context");
	    }
	}
	
	private OntModel loadModelFromDirectory(String directoryPath) {
		
		OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			throw new RuntimeException(directoryPath + " must be a directory " +
					"containing RDF files.");
		}
		File[] rdfFiles = directory.listFiles();
		for (int i = 0; i < rdfFiles.length; i++) {
			try {
				File f = rdfFiles[i];
				FileInputStream fis = new FileInputStream(f);
				try {
					if (f.getName().endsWith(".n3")) {
						om.read(fis, null, "N3");
					} else {
						om.read(fis, null, "RDF/XML");
					}
				} catch (Exception e) {
					log.error("Unable to load RDF from " + f.getName(), e); 
				}
			} catch (FileNotFoundException fnfe) {
				log.error(rdfFiles[i].getName() + " not found. Unable to load" +
						" RDF from this location.");
			}
		}
		return om;
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do	
	}
	public static String getAskQueryPath(ServletContext ctx) {
		return ctx.getRealPath(ASK_QUERY_FILE);
	
    }	
	public static String getAskEverQueryPath(ServletContext ctx) {
		return ctx.getRealPath(ASK_EVER_QUERY_FILE);
	
    }
	public static String getAskEmptyQueryPath(ServletContext ctx) {
		return ctx.getRealPath(ASK_EMPTY_QUERY_FILE);
	
    }
}
