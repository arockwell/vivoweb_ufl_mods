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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.RevisionInfoController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.SiteAdminController;

public class User extends BaseTemplateModel {
   
    private static final Log log = LogFactory.getLog(User.class);
    
    private enum Access {
        SITE_ADMIN(SiteAdminController.staticRequiredLoginLevel()),
        REVISION_INFO(RevisionInfoController.staticRequiredLoginLevel()),
        FILTER_SECURITY(LoginStatusBean.EDITOR);
        
        private final int requiredLoginLevel;
        
        Access(int requiredLoginLevel) {
            this.requiredLoginLevel = requiredLoginLevel;
        }
        
        int requiredLoginLevel() {
            return this.requiredLoginLevel;
        }
    }
    
    private LoginStatusBean loginBean = null;
    private VitroRequest vreq = null;
    
    public User(VitroRequest vreq) {
        this.vreq = vreq;
        loginBean = LoginStatusBean.getBean(vreq);
    }
    
    public boolean isLoggedIn() {
        return loginBean.isLoggedIn();
    }
    
    public String getLoginName() {
        return loginBean.getUsername();
    }
    
    public boolean getHasSiteAdminAccess() {
        return loginBean.isLoggedInAtLeast(Access.SITE_ADMIN.requiredLoginLevel());
    }
    
    public boolean getHasRevisionInfoAccess() {
        return loginBean.isLoggedInAtLeast(Access.REVISION_INFO.requiredLoginLevel());
    }
    
    public boolean getShowFlag1SearchField() {
        boolean showFlag1SearchField = false;
        if (loginBean.isLoggedInAtLeast(Access.FILTER_SECURITY.requiredLoginLevel)) {
            ApplicationBean appBean = vreq.getAppBean();            
            if (appBean.isFlag1Active()) {
                showFlag1SearchField = true;
            }
        }
        return showFlag1SearchField;
    }
}
