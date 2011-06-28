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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import freemarker.template.Configuration;

/**
 *  Controller for comments ("contact us") page
 *  * @author bjl23
 */
public class ContactFormController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ContactFormController.class.getName());
    
    private static final String TEMPLATE_DEFAULT = "contactForm-form.ftl";
    private static final String TEMPLATE_ERROR = "contactForm-error.ftl";
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Feedback Form";
    }
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {

        String templateName;
        Portal portal = vreq.getPortal();
        Map<String, Object> body = new HashMap<String, Object>();
        
        if (!ContactMailServlet.isSmtpHostConfigured()) {
            body.put("errorMessage", 
                     "This application has not yet been configured to send mail. " +
                     "An smtp host has not been specified in the configuration properties file.");
            templateName = TEMPLATE_ERROR;
        }
        
        else if (StringUtils.isEmpty(portal.getContactMail())) {
            body.put("errorMessage", 
            		"The feedback form is currently disabled. In order to activate the form, a site administrator must provide a contact email address in the <a href='editForm?home=1&amp;controller=Portal&amp;id=1'>Site Configuration</a>");
            
            templateName = TEMPLATE_ERROR;          
        }
        
        else {

            ApplicationBean appBean = vreq.getAppBean();          
            String portalType = null;
            int portalId = portal.getPortalId();
            String appName = portal.getAppName();
            
            if ( (appBean.getMaxSharedPortalId()-appBean.getMinSharedPortalId()) > 1
                  && ( (portalId  >= appBean.getMinSharedPortalId()
                  && portalId <= appBean.getMaxSharedPortalId() )
                  || portalId == appBean.getSharedPortalFlagNumeric() ) ) {
                portalType = "CALSResearch";
            } else if (appName.equalsIgnoreCase("CALS Impact")) {
                portalType = "CALSImpact";
            } else if (appName.equalsIgnoreCase("VIVO")){
                portalType = "VIVO";
            } else {
                portalType = "clone";
            }
            body.put("portalType", portalType);
            
            body.put("portalId", portalId);
            body.put("formAction", "submitFeedback");

            if (vreq.getHeader("Referer") == null) {
                vreq.getSession().setAttribute("contactFormReferer","none");
            } else {
                vreq.getSession().setAttribute("contactFormReferer",vreq.getHeader("Referer"));
            }
           
            templateName = TEMPLATE_DEFAULT;
        }
        
        return new TemplateResponseValues(templateName, body);
    }
}
