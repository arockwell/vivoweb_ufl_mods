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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetter;

public class HomePageController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(HomePageController.class);
    private static final String PAGE_TEMPLATE = "page-home.ftl";
    private static final String BODY_TEMPLATE = "home.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) { 
        
        Map<String, Object> body = new HashMap<String, Object>();    
//        VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache( getServletContext() );
//        List<VClassGroup> vClassGroups =  vcgc.getGroups(vreq.getPortalId());
//        body.put("vClassGroups", vClassGroups);
        
        PageDataGetter dataGetter =
            PageController.getPageDataGetterMap(getServletContext())
            .get(DisplayVocabulary.HOME_PAGE_TYPE);        
        if( dataGetter != null ){
            String uriOfPageInDisplayModel = "not defined";            
            Map<String, Object> pageData = 
                dataGetter.getData(getServletContext(), vreq, 
                        uriOfPageInDisplayModel, body, 
                        DisplayVocabulary.HOME_PAGE_TYPE);
            if(pageData != null)
                body.putAll(pageData);            
        }

        // Get the home tab content for themes that display deprecated tabs
        body.put("homeTabContent", getHomeTabContent(vreq));
        
        return new TemplateResponseValues(BODY_TEMPLATE, body);
    }

    // Get the home tab content for themes that display deprecated tabs
    private String getHomeTabContent(VitroRequest vreq) {
        Portal portal = vreq.getPortal();
        int tabId = portal.getRootTabId();
        Tab tab = vreq.getWebappDaoFactory().getTabDao().getTab(tabId,0,vreq.getAppBean());
        String body = tab.getBody();
        if (body == null) {
            body = "";
        }
        return body;
    }
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return siteName;
    }

    @Override
    protected String getPageTemplateName() {
        return PAGE_TEMPLATE;
    }
}
