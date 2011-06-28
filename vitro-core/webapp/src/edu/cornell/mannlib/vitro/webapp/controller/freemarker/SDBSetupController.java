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
package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupSDB;

public class SDBSetupController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            SDBSetupController.class);
    
    private static final String SDB_SETUP_FTL = "sdbSetup.ftl";
    
    protected ResponseValues processRequest(VitroRequest vreq) { 
        // Due to requiresLoginLevel(), we don't get here unless logged in as DBA
        if (!LoginStatusBean.getBean(vreq)
                .isLoggedInAtLeast(LoginStatusBean.DBA)) {
            return new RedirectResponseValues(UrlBuilder.getUrl(Route.LOGIN));
        }
        Map<String, Object> body = new HashMap<String, Object>();
        
        String messageStr = "";
        try {
        	JenaDataSourceSetupSDB jenaDataSourceSetupSDB = new JenaDataSourceSetupSDB();
        	Boolean done = (Boolean)getServletContext().getAttribute("done");
        	String setupsignal = (String) vreq.getParameter("setupsignal");
                if (done!=null && done) {
                	 messageStr = "SDB is currently being set up.";
                } else{
                	String sdbsetup = (String)getServletContext().getAttribute("sdbsetup");
                	if(sdbsetup == null || sdbsetup.equals("showButton") || setupsignal == null){
                		body.put("link", "show");
                    	messageStr = null;
                    	getServletContext().setAttribute("sdbsetup", "yes");
                    	if(getServletContext().getAttribute("sdbstatus")!=null)
                    		body.put("sdbstatus",getServletContext().getAttribute("sdbstatus"));
                    	else
                    		body.put("sdbstatus"," ");
                	}
                	else if(setupsignal!=null && setupsignal.equals("setup")){
                		new Thread(new SDBSetupRunner(jenaDataSourceSetupSDB)).start();
                		messageStr = "SDB setup started.";
                        getServletContext().setAttribute("sdbsetup", "showButton");
                	}	
                }
        } catch (Exception e) {
            log.error("Error setting up SDB store", e);
            body.put("errorMessage", 
                    "Error setting up SDB store: " + 
                    e.getMessage());
            return new ExceptionResponseValues(
                    SDB_SETUP_FTL, body, e);            
        }
        
        body.put("message", messageStr); 
        return new TemplateResponseValues(SDB_SETUP_FTL, body);
    }
    
    private class SDBSetupRunner implements Runnable {
        
        private JenaDataSourceSetupSDB jenaDataSourceSetupSDB;
        final OntModelSpec MEM_ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
        
        public SDBSetupRunner(JenaDataSourceSetupSDB jenaDataSourceSetupSDB) {
            this.jenaDataSourceSetupSDB = jenaDataSourceSetupSDB;
        }
        
        public void run() {
           Boolean done = true;
           getServletContext().setAttribute("done",done);
           StoreDesc storeDesc = jenaDataSourceSetupSDB.makeStoreDesc();
           BasicDataSource bds = jenaDataSourceSetupSDB.makeDataSourceFromConfigurationProperties();
           Store store = null;
		try {
			store = JenaDataSourceSetupSDB.connectStore(bds, storeDesc);
		} catch (SQLException e) {
			log.error("Error while getting the sdb store with given store description and basic data source", e);
		}
           OntModel memModel = (OntModel)getServletContext().getAttribute("jenaOntModel");
           if (memModel == null) {
               memModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
               log.warn("WARNING: no database connected.  Changes will disappear after context restart.");
           }  
           OntModel inferenceModel = (OntModel)getServletContext().getAttribute("inferenceOntModel");
           if(inferenceModel == null){
        	   inferenceModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
           }
           if (store!=null) {
               log.info("Setting up SDB store.");
               try{
            	   jenaDataSourceSetupSDB.setupSDB(getServletContext(), store, memModel, inferenceModel);
            	   getServletContext().setAttribute("sdbstatus","SDB setup done successfully");
               }
               catch(Exception e){
            	   getServletContext().setAttribute("sdbstatus",e.getMessage());
               }
               log.info("SDB setup complete.");
           }
           done = false;
           getServletContext().setAttribute("done",done);
        }
        
    }
    
}
