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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;

/** A main menu constructed from persisted tab data
 * 
 * @author rjy7
 *
 */
public class TabMenu extends MainMenu {
    
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TabMenu.class.getName());
    
    private static String TAB_PARAM = "primary";
    private static String PATH = "/index.jsp";
       
    public TabMenu(VitroRequest vreq, int portalId) {
        super(vreq);
        
        //Tabs stored in database
        List<Tab> primaryTabs = vreq.getWebappDaoFactory().getTabDao().getPrimaryTabs(portalId);        
        int tabId = TabWebUtil.getTabIdFromRequest(vreq); 
        int rootId = TabWebUtil.getRootTabId(vreq); 
        List tabLevels = vreq.getWebappDaoFactory().getTabDao().getTabHierarchy(tabId,rootId);
        vreq.setAttribute("tabLevels", tabLevels); 
        Iterator<Tab> primaryTabIterator = primaryTabs.iterator();
        //Iterator tabLevelIterator = tabLevels.iterator();
        Tab tab;
        while (primaryTabIterator.hasNext()) {
            tab = (Tab) primaryTabIterator.next();
            addItem(tab);
            // RY Also need to loop through nested tab levels, but not doing that now.
        }
        
        // Hard-coded tabs. It's not a good idea to have these here, since any menu item that doesn't
        // come from the db should be accessible to the template to change the text. But we need them here
        // (rather than adding directly from the template) to apply the "active" mechanism.
        addItem("Index", "/browse");     
    }
    
    private void addItem(Tab tab) {
        boolean isActive = isActiveItem(tab);
        String text = tab.getTitle();
        String path = UrlBuilder.getPath(PATH, new ParamMap(TAB_PARAM, "" + tab.getTabId()));
        addItem(text, path, isActive);       
    }
    
    private boolean isActiveItem(Tab tab) {
        String requestedTabId = vreq.getParameter(TAB_PARAM); 
        int tabId = tab.getTabId();
        if (requestedTabId == null) {
        	return tabId == vreq.getPortal().getRootTabId() && "true".equals(vreq.getAttribute("homePageRequested"));
        } else {
        	return Integer.parseInt(requestedTabId) == tabId;
        }
    }

}
