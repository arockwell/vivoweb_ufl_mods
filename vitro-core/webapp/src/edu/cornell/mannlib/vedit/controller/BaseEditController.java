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

package edu.cornell.mannlib.vedit.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class BaseEditController extends VitroHttpServlet {

	public static final boolean FORCE_NEW = true; // when you know you're starting a new edit process
    
    public static final String JSP_PREFIX = "/templates/edit/specific/";
	
    protected static DateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    private static final Log log = LogFactory.getLog(BaseEditController.class.getName());
    private static final String DEFAULT_LANDING_PAGE = Controllers.SITE_ADMIN;
    protected static final String MULTIPLEXED_PARAMETER_NAME = "multiplexedParam";
    private final String EPO_HASH_ATTR = "epoHash";
    private final String EPO_KEYLIST_ATTR = "epoKeylist";
    private final int MAX_EPOS = 5;
    private final Calendar cal = Calendar.getInstance();
    private final Random rand = new Random(cal.getTimeInMillis());

    /* EPO is reused if the controller is passed an epoKey, e.g.
      if a previous form submission failed validation, or the edit is a multistage process. */

    protected EditProcessObject createEpo(HttpServletRequest request) {
    	return createEpo(request, false);
    }
    
    protected EditProcessObject createEpo(HttpServletRequest request, boolean forceNew) {
        /* this is actually a bit of a misnomer, because we will reuse an epo
        if an epoKey parameter is passed */
        EditProcessObject epo = null;
        HashMap epoHash = getEpoHash(request);
        String existingEpoKey = request.getParameter("_epoKey");
        if (!forceNew && existingEpoKey != null && epoHash.get(existingEpoKey) != null)  {
            epo = (EditProcessObject) epoHash.get(existingEpoKey);
            epo.setKey(existingEpoKey);
            epo.setUseRecycledBean(true);
        } else {
            LinkedList epoKeylist = getEpoKeylist(request);
            if (epoHash.size() == MAX_EPOS) {
            	try {
            		epoHash.remove(epoKeylist.getFirst());
            		epoKeylist.removeFirst();
            	} catch (Exception e) {
            		// see JIRA issue VITRO-340, "Odd exception from backend editing"
            		// possible rare concurrency issue here
            		log.error("Error removing old EPO", e);
            	}
            }
            Random rand = new Random();
            String epoKey = createEpoKey();
            while (epoHash.get(epoKey) != null) {
                epoKey+=Integer.toHexString(rand.nextInt());
            }
            epo = new EditProcessObject();
            epoHash.put (epoKey,epo);
            epoKeylist.add(epoKey);
            epo.setKey(epoKey);
            epo.setReferer( (forceNew) ? request.getRequestURL().append('?').append(request.getQueryString()).toString() : request.getHeader("Referer") );
            epo.setSession(request.getSession());
        }
        return epo;
    }

    private LinkedList getEpoKeylist(HttpServletRequest request){
        return (LinkedList) request.getSession().getAttribute(EPO_KEYLIST_ATTR);
    }

    private HashMap getEpoHash(HttpServletRequest request){
        HashMap epoHash = (HashMap) request.getSession().getAttribute(EPO_HASH_ATTR);
        if (epoHash == null) {
            epoHash = new HashMap();
            request.getSession().setAttribute(EPO_HASH_ATTR,epoHash);
            //since we're making a new EPO hash, we should also make a new keylist.
            LinkedList epoKeylist = new LinkedList();
            request.getSession().setAttribute(EPO_KEYLIST_ATTR,epoKeylist);
        }
        return epoHash;
    }

    private String createEpoKey(){
        return Long.toHexString(cal.getTimeInMillis());
    }

    protected boolean checkLoginStatus(HttpServletRequest request, HttpServletResponse response){
        return checkLoginStatus(request, response, null);
    }

    protected boolean checkLoginStatus(HttpServletRequest request, HttpServletResponse response, String postLoginRedirectURI){
        LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
        String loginPage = request.getContextPath() + Controllers.LOGIN;
        if (loginBean == null){
            try{
                if (postLoginRedirectURI == null)
                    request.getSession().setAttribute("postLoginRequest",request.getRequestURI()+"?"+request.getQueryString());
                else
                    request.getSession().setAttribute("postLoginRequest",postLoginRedirectURI+"?"+request.getQueryString());
                response.sendRedirect(loginPage);
                return false;
            } catch (IOException ioe) {
                log.error("checkLoginStatus() could not redirect to login page");
                return false;
            }
        } else {
            if (!loginBean.getLoginStatus().equals("authenticated")) {
                try{
                    response.sendRedirect(loginPage);
                    return false;
                } catch (IOException ioe) {
                    log.error("checkLoginStatus() could not redirect to login page");
                    return false;
                }
            }
        }
        return true;
    }

    protected void setRequestAttributes(HttpServletRequest request, EditProcessObject epo){
        Portal portal = (Portal)request.getAttribute("portalBean");
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("epo",epo);
        request.setAttribute("globalErrorMsg",epo.getAttribute("globalErrorMsg"));
        request.setAttribute("portalBean",portal);
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
    }

    protected void populateBeanFromParams(Object bean, HttpServletRequest request) {
        Map params = request.getParameterMap();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()){
            String key = "";
            try {
                key = (String) paramNames.nextElement();
            } catch (ClassCastException cce) {
                log.error("populateBeanFromParams() could not cast parameter name to String");
            }
            String value = "";
            if (key.equals(MULTIPLEXED_PARAMETER_NAME)) {
                String multiplexedStr = request.getParameterValues(key)[0];
                Map paramMap = FormUtils.beanParamMapFromString(multiplexedStr);
                Iterator paramIt = paramMap.keySet().iterator();
                while (paramIt.hasNext()) {
                    String param = (String) paramIt.next();
                    String demultiplexedValue = (String) paramMap.get(param);
                    FormUtils.beanSet(bean, param, demultiplexedValue);
                }

            } else {
                try {
                    value = (String) request.getParameterValues(key)[0];
                } catch (ClassCastException cce) {
                    try {
                        value = ((Integer) params.get(key)).toString();
                    } catch (ClassCastException ccf) {
                        log.error("populateBeanFromParams() could not cast parameter name to String");
                    }
                }
                FormUtils.beanSet(bean, key, value);
            }
        }
    }
    
    protected String MODEL_ATTR_NAME = "jenaOntModel";
    
    protected OntModel getOntModel( HttpServletRequest request, ServletContext ctx ) {

    	OntModel ontModel = null;
    	
    	try {
    		ontModel = (OntModel) request.getSession().getAttribute(MODEL_ATTR_NAME);
    	} catch (Exception e) {}
    	
    	if ( ontModel == null ) {
    		try {
    			ontModel = (OntModel) ctx.getAttribute(MODEL_ATTR_NAME);
    		} catch (Exception e) {
    			throw new RuntimeException("Could not find OntModel in context attribute '"+MODEL_ATTR_NAME+"'");
    		}
    	}
    	
    	return ontModel;
    	
    }
    
    public String getDefaultLandingPage(HttpServletRequest request) {
    	return(request.getContextPath() + DEFAULT_LANDING_PAGE);
    }

}
