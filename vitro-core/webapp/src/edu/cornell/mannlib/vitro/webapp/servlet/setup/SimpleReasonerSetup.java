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

import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.PelletOptions;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RegeneratingGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SDBGraphGenerator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.reasoner.support.SimpleReasonerTBoxListener;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.TripleStoreType;

public class SimpleReasonerSetup implements ServletContextListener {

	private static final Log log = LogFactory.getLog(SimpleReasonerSetup.class.getName());
	
	// Models used during a full recompute of the ABox
	static final String JENA_INF_MODEL_REBUILD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-rebuild";
	static final String JENA_INF_MODEL_SCRATCHPAD = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf-scratchpad";

	public void contextInitialized(ServletContextEvent sce) {
	    
	    if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
	    
		try {	
		    // set up Pellet reasoning for the TBox	
			
			OntModelSelector assertionsOms = (OntModelSelector) sce.getServletContext().getAttribute("baseOntModelSelector");
			OntModelSelector inferencesOms = (OntModelSelector) sce.getServletContext().getAttribute("inferenceOntModelSelector");
			OntModelSelector unionOms = (OntModelSelector) sce.getServletContext().getAttribute("unionOntModelSelector");

			WebappDaoFactoryJena wadf = (WebappDaoFactoryJena) sce.getServletContext().getAttribute("webappDaoFactory");
			
			if (!assertionsOms.getTBoxModel().getProfile().NAMESPACE().equals(OWL.NAMESPACE.getNameSpace())) {		
				log.error("Not connecting Pellet reasoner - the TBox assertions model is not an OWL model");
				return;
			}
	        
	        // Set various Pellet options for incremental consistency checking, etc.
			PelletOptions.DL_SAFE_RULES = true;
	        PelletOptions.USE_COMPLETION_QUEUE = true;
	        PelletOptions.USE_TRACING = true;
	        PelletOptions.TRACK_BRANCH_EFFECTS = true;
	        PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
	        PelletOptions.USE_INCREMENTAL_DELETION = true;
	         
	        PelletListener pelletListener = new PelletListener(unionOms.getTBoxModel(),assertionsOms.getTBoxModel(),inferencesOms.getTBoxModel(),ReasonerConfiguration.DEFAULT);
	        sce.getServletContext().setAttribute("pelletListener",pelletListener);
	        sce.getServletContext().setAttribute("pelletOntModel", pelletListener.getPelletModel());
	        
	        if (wadf != null) {
	        	wadf.setPelletListener(pelletListener);
	        }
	        
	        log.info("Pellet reasoner connected for the TBox");
     
	       // set up simple reasoning for the ABox
	    	    	
	        ServletContext ctx = sce.getServletContext();
	        BasicDataSource bds = JenaDataSourceSetupBase
	                                .getApplicationDataSource(ctx);
	        String dbType = ConfigurationProperties.getProperty( // database type
                    "VitroConnection.DataSource.dbtype","MySQL");
	        
	        	        
            Model rebuildModel = JenaDataSourceSetupBase.makeDBModel(
                    bds, 
                    JENA_INF_MODEL_REBUILD, 
                    JenaDataSourceSetupBase.DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, 
                    dbType);            
            Model scratchModel = JenaDataSourceSetupBase.makeDBModel(
                    bds, 
                    JENA_INF_MODEL_SCRATCHPAD, 
                    JenaDataSourceSetupBase.DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, 
                    dbType); 
	        
	        
	        // the simple reasoner will register itself as a listener to the ABox assertions
	        SimpleReasoner simpleReasoner = new SimpleReasoner(unionOms.getTBoxModel(), assertionsOms.getABoxModel(), inferencesOms.getABoxModel(), rebuildModel, scratchModel);
	        
	        if (isRecomputeRequired(sce.getServletContext())) {
	            
	            log.info("ABox inference recompute required");
	            
	            int sleeps = 0;
	            while (sleeps < 1000 && pelletListener.isReasoning()) {
	                if ((sleeps % 30) == 0) {
	                    log.info("Waiting for initial TBox reasoning to complete");
	                }
	                Thread.sleep(100);   
	                sleeps++;
	            }
	            
	            simpleReasoner.recompute();
	            
	        }

	        assertionsOms.getTBoxModel().register(new SimpleReasonerTBoxListener(simpleReasoner));
	        
	        sce.getServletContext().setAttribute("simpleReasoner",simpleReasoner);
	        
	        log.info("Simple reasoner connected for the ABox");
	        
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do
	}
	
	private static final String RECOMPUTE_REQUIRED_ATTR = 
	        SimpleReasonerSetup.class.getName() + ".recomputeRequired";
	
	public static void setRecomputeRequired(ServletContext ctx) {
	    ctx.setAttribute(RECOMPUTE_REQUIRED_ATTR, true);
	}
	
	private static boolean isRecomputeRequired(ServletContext ctx) {
	    return (ctx.getAttribute(RECOMPUTE_REQUIRED_ATTR) != null);
	}
  
}
