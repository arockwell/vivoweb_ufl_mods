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

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class MenuN3EditController extends FreemarkerHttpServlet {

    protected final static String N3MENU_FORM = "menuN3Edit.ftl"; 
    protected final static String N3MENU_SUCCESS_RESULT = "menuN3Edit.ftl";     
    protected final static String N3MENU_ERROR_RESULT = "menuN3Edit.ftl";
    
    protected final static String N3_PARAM = "navigationN3";
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        String n3 = vreq.getParameter(N3_PARAM); 
        if( n3 != null &&  ! n3.isEmpty()){
            return setNewMenu(vreq);        
        }else{
            return showForm(vreq);
        }
    }

    private ResponseValues showForm(VitroRequest vreq) {
        Map<String,Object> data = new HashMap<String,Object>();
        
        String menuN3;                
        try {
            menuN3 = vreq.getWebappDaoFactory().getDisplayModelDao()
                    .getDisplayModel(getServletContext());
            data.put("menuN3", menuN3);
            data.put("cancelUrl", "/siteAdmin");
        } catch (Exception e) {
            data.put("errorMessage",e.getMessage());
        }        
        return new TemplateResponseValues(N3MENU_FORM, data);
    }

    private ResponseValues setNewMenu(VitroRequest vreq) {
        Map<String,Object> data = new HashMap<String,Object>();
        
        String menuN3 = vreq.getParameter(N3_PARAM);
        
        try {
            vreq.getWebappDaoFactory().getDisplayModelDao()
                .replaceDisplayModel(menuN3, getServletContext());
            data.put("message", "success");
        } catch (Exception e) {
            data.put("errorMessage",e.getMessage());
        }
        
        if( data.containsKey("errorMessage"))            
            return new TemplateResponseValues(N3MENU_ERROR_RESULT,data);
        else
            return new TemplateResponseValues(N3MENU_SUCCESS_RESULT, data);
            
    }

    
}
