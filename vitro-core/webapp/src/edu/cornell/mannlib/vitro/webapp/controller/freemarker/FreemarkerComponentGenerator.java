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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.template.Configuration;

/**
 * TEMPORARY for transition from JSP to FreeMarker. Once transition
 * is complete and no more pages are generated in JSP, this can be removed.
 * 
 * @author rjy7
 *
 */
public class FreemarkerComponentGenerator extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreemarkerComponentGenerator.class);
    
    private static ServletContext context = null;
    
    FreemarkerComponentGenerator(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        Configuration config = getConfig(vreq);

        Map<String, Object> map = getPageTemplateValues(vreq);
        
        request.setAttribute("ftl_head", getHead("head", map, config, vreq));
        request.setAttribute("ftl_identity", get("identity", map, config, vreq));
        request.setAttribute("ftl_menu", get("menu", map, config, vreq));
        request.setAttribute("ftl_search", get("search", map, config, vreq));
        request.setAttribute("ftl_footer", get("footer", map, config, vreq));
        request.setAttribute("ftl_googleAnalytics", get("googleAnalytics", map, config, vreq));
    }

    private String get(String templateName, Map<String, Object> root, Configuration config, HttpServletRequest request) {
        templateName += ".ftl";
        return processTemplate(templateName, root, config, request).toString();
    }
    
    private String getHead(String templateName, Map<String, Object> root, Configuration config, HttpServletRequest request) {
        // The Freemarker head template displays the page title in the <title> tag. Get the value out of the request.
        String title = (String) request.getAttribute("title");
        if (!StringUtils.isEmpty(title)) {
            root.put("title", title);
        }
        return get(templateName, root, config, request);        
    }
    
    // RY We need the servlet context in getConfig(). For some reason using the method inherited from
    // GenericServlet bombs.
    @Override
    public ServletContext getServletContext() {
        return context;
    }
    
    protected static void setServletContext(ServletContext sc) {
        context = sc;
    }

}
