/*
Copyright (c) 2010, Cornell University
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

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.search.IndexingException;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import freemarker.template.Configuration;

/**
 * Accepts requests to rebuild or update the search index.  It uses
 * an IndexBuilder and finds that IndexBuilder from the servletContext using
 * the key "edu.cornel.mannlib.vitro.search.indexing.IndexBuilder"
 *
 * That IndexBuilder will be associated with a object that implements the IndexerIface.
 *
 * An example of the IndexerIface is LuceneIndexer.
 * An example of the IndexBuilder and LuceneIndexer getting setup is in LuceneSetup.
 *
 * @author bdc34
 *
 */
public class IndexController extends FreeMarkerHttpServlet {
	
	private static final Log log = LogFactory.getLog(IndexController.class.getName());
	
//    public void doPost(HttpServletRequest request, HttpServletResponse response)
//    throws ServletException,IOException {
//        doGet(request, response);
//    }
//
//    public void doGet( HttpServletRequest request, HttpServletResponse response )
//    throws IOException, ServletException {
//        
//        Object obj = request.getSession().getAttribute("loginHandler");        
//        LoginFormBean loginHandler = null;
//        if( obj != null && obj instanceof LoginFormBean )
//            loginHandler = ((LoginFormBean)obj);
//        if( loginHandler == null ||
//            ! "authenticated".equalsIgnoreCase(loginHandler.getLoginStatus()) ||
//             Integer.parseInt(loginHandler.getLoginRole()) <= 5 ){       
//            
//            String redirectURL=request.getContextPath() + Controllers.SITE_ADMIN + "?login=block";
//            response.sendRedirect(redirectURL);
//            return;
//        }
//        
//        long start = System.currentTimeMillis();
//        try {
//            IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
//            if( request.getParameter("update") != null ){
//                builder.doUpdateIndex();
//            }else{
//                builder.doIndexRebuild();
//            }
//            
//        } catch (IndexingException e) {
//            log.error("IndexController -- Error building index: " + e);
//        }
//        long delta = System.currentTimeMillis() - start;
//        String msg = "Search index complete. Elapsed time " + delta + " msec.";
//    }
    
    
    protected String getTitle(String siteName) {
        return "Full Search Index Rebuild";
    }
    
    protected String getBody(VitroRequest request, Map<String, Object> body, Configuration config) {       
        
        Object obj = request.getSession().getAttribute("loginHandler");        
        LoginFormBean loginHandler = null;
        if( obj != null && obj instanceof LoginFormBean )
            loginHandler = ((LoginFormBean)obj);
        if( loginHandler == null ||
            ! "authenticated".equalsIgnoreCase(loginHandler.getLoginStatus()) ||
             Integer.parseInt(loginHandler.getLoginRole()) <= 5 ){       
            
            body.put("message","You must login to rebuild the search index."); 
            return mergeBodyToTemplate("message.ftl", body, config);
        }
        
        long start = System.currentTimeMillis();
        try {
            IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
            if( request.getParameter("update") != null ){
                builder.doUpdateIndex();
            }else{
                builder.doIndexRebuild();
            }
            
        } catch (IndexingException e) {
        	log.error("Error rebuilding search index",e);
        	body.put("errorMessage", "There was an error while rebuilding the search index. " + e.getMessage());
        	return mergeBodyToTemplate("errorMessage.ftl", body, config);            
        }
        
         body.put("message","Rebuilding of index started."); 
        return mergeBodyToTemplate("message.ftl", body, config);
    }
}
