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

package edu.cornell.mannlib.vitro.webapp.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to store urls for different controllers.
 * This is just a place to stick the constants that are urls
 * to servlets and jsps.
 *
 * Maybe it should be pulled out of a properties file?
 * @author bdc34
 *
 */

public class Controllers {
    
    // Servlet urls

    public static final String ABOUT = "/about";
    public static final String CONTACT_URL = "/comments";
    public static final String TERMS_OF_USE_URL = "/termsOfUse";
    public static final String SEARCH_URL = "/search";
    
    public static final String ENTITY = "/entity";
    public static final String ENTITY_PROP_LIST = "/entityPropList";
    public static final String ENTITY_LIST = "/entitylist";

    public static final String BROWSE_CONTROLLER = "browsecontroller";
    public static final String RETRY_URL = "editForm";
    public static final String TAB_ENTITIES = "/TabEntitiesController";  

    public static final String SITE_ADMIN = "/siteAdmin";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String AUTHENTICATE = "/authenticate";
    
    public static final String EXPORT_RDF = "/export";
    
    // jsps go here:
    
    public static final String TAB = "/index.jsp";

    
    public static final String BASIC_JSP = "/templates/page/basicPage.jsp";
    public static final String DEBUG_JSP = "/templates/page/debug.jsp";
    public static final Object BODY_MSG = "/templates/page/bodyMsg.jsp";
    
    public static final String ENTITY_JSP = "/templates/entity/entityBasic.jsp";
    public static final String ENTITY_PROP_LIST_JSP = "templates/entity/entityPropsList.jsp";
    public static final String ENTITY_DATAPROP_LIST_JSP = "templates/entity/entityDatapropsList.jsp";
    public static final String ENTITY_MERGED_PROP_LIST_GROUPED_JSP = "templates/entity/entityMergedPropsList.jsp";
    public static final String DASHBOARD_PROP_LIST_JSP = "edit/dashboardPropsList.jsp";
    public static final String ENTITY_MERGED_PROP_LIST_UNGROUPED_JSP = "templates/entity/entityMergedPropsListUngrouped.jsp";
    
    public static final String ENTITY_KEYWORDS_LIST_JSP = "templates/entity/entityKeywordsList.jsp";

    public static final String ENTITY_EDITABLE_JSP = "templates/entity/entityEditable.jsp";
    public static final String ENTITY_EDITABLE_PROP_LIST_JSP = "templates/entity/entityEditablePropsList.jsp";

    public static final String ENTITY_LIST_JSP = "templates/entity/entityList.jsp";
    public static final String ENTITY_LIST_FOR_TABS_JSP = "templates/entity/entityListForTabs.jsp";
    public static final String TAB_ENTITIES_LIST_GALLERY_JSP = "templates/entity/entityListForGalleryTab.jsp";
    public static final String ENTITY_NOT_FOUND_JSP = "templates/error/entityNotFound.jsp";

    public static final String TAB_BASIC_JSP = "/templates/tabs/tabBasic.jsp";
    public static final String TAB_PRIMARY_JSP = "/templates/tabs/tabprimary.jsp";

    public static final String ALPHA_INDEX_JSP = "/templates/alpha/alphaIndex.jsp";

    public static final String SEARCH_BASIC_JSP = "/templates/search/searchBasic.jsp";
    public static final String SEARCH_PAGED_JSP = "/templates/search/searchPaged.jsp";
    public static final String SEARCH_FAILED_JSP = "/templates/search/searchFailed.jsp";
    public static final String SEARCH_GROUP_JSP = "/templates/search/searchGroup.jsp";
    public static final Object SEARCH_FORM_JSP = "/templates/search/searchForm.jsp";
    public static final Object SEARCH_BAD_QUERY_JSP = "/templates/search/searchBadQuery.jsp";
    
    public static final String BROWSE_GROUP_JSP = "/templates/browse/browseGroup.jsp";

    public static final String HORIZONTAL_JSP = "/templates/edit/fetch/horizontal.jsp";
    public static final String VERTICAL_JSP = "/templates/edit/fetch/vertical.jsp";
    
    public static final String CHECK_DATATYPE_PROPERTIES = "/jsp/checkDatatypeProperties.jsp";
    public static final String EXPORT_SELECTION_JSP = "/jenaIngest/exportSelection.jsp";

    public static final String VCLASS_RETRY_URL = "vclass_retry";

    public static final String TOGGLE_SCRIPT_ELEMENT = "<script language='JavaScript' type='text/javascript' src='js/toggle.js'></script>";

    public static final Object SEARCH_ERROR_JSP = "/templates/parts/search_error.jsp";

    
    //public static final String TAB_ENTITIES_LIST_JSP = "templates/tab/tabEntities.jsp";

    private static List<String> letters = null;
    public static List<String> getLetters() {
        //there must be a better place to put this.
        if (Controllers.letters == null) {
            char c[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            Controllers.letters = new ArrayList<String>(c.length);
            for (int i = 0; i < c.length; i++) {
                letters.add("" + c[i]);
            }
        }
        return Controllers.letters;
    }
}
