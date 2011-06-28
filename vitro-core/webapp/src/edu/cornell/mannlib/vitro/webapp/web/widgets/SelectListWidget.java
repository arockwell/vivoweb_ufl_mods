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

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.SelectListGenerator;
import edu.cornell.mannlib.vitro.webapp.web.directives.WidgetDirective;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;

public class SelectListWidget extends Widget {
    private static final Log log = LogFactory.getLog(SelectListWidget.class);
    
    @Override
    protected WidgetTemplateValues process(Environment env, Map params,
            HttpServletRequest request, ServletContext context) {        
        
        Object obj = params.get("fieldName");
        if( obj == null  || !(obj instanceof SimpleScalar)){
            log.error("SelectListWidget must have a parameter 'fieldName'");
            throw new Error("SelectListWidget must have a parameter'fieldName'");
        }
        String fieldName = ((SimpleScalar)obj).getAsString();
        if( fieldName.isEmpty() ){
            log.error("SelectListWidget must have a parameter 'fieldName'");        
            throw new Error("SelectListWidget must have a parameter 'fieldName' of type String");
        }
        
        VitroRequest vreq = new VitroRequest(request);        
        HttpSession session = request.getSession(false);
        EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);                
        
        WebappDaoFactory wdf;
        if (editConfig != null) { 
            wdf = editConfig.getWdfSelectorForOptons().getWdf(vreq,context);
        } else {                
            wdf = vreq.getWebappDaoFactory();
        }
        
        Map<String,String> selectOptions =  SelectListGenerator.getOptions(editConfig, fieldName, wdf);                                  
        Map<String,Object> rmap = new HashMap<String,Object>();
        rmap.put("selectList", selectOptions);
        
        return new WidgetTemplateValues("markup", rmap);
    }

}
