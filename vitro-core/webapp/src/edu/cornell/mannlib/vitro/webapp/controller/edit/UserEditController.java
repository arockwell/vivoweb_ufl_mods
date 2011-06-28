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

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class UserEditController extends BaseEditController {

    private String[] roleNameStr = new String[51];
    private static final Log log = LogFactory.getLog(UserEditController.class.getName());

    public UserEditController() {
        roleNameStr[1] = "self editor";
        roleNameStr[4] = "editor";
        roleNameStr[5] = "curator";
        roleNameStr[50] = "system administrator";
    }

    public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	if (!checkLoginStatus(request,response, LoginStatusBean.DBA))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" caught exception calling doGet()");
        }

        VitroRequest vreq = new VitroRequest(request);
        Portal portal = vreq.getPortal();

        UserDao uDao = vreq.getFullWebappDaoFactory().getUserDao();

        String userURIStr = request.getParameter("uri");
        User u = null;

        if (userURIStr == null) {
            throw new ServletException(this.getClass().getName()+" expects user URI in 'uri' request parameter");
        } else {
            u = uDao.getUserByURI(userURIStr);
        }

        if (u == null) {
            throw new ServletException(this.getClass().getName()+" could not find user "+userURIStr);
        }

        ArrayList<String> results = new ArrayList<String>();
        results.add("Email address");
        results.add("first name");
        results.add("last name");
        results.add("login count");
        results.add("role");

        String EMPTY = "";

        String usernameStr = (u.getUsername() != null) ? u.getUsername() : "";
        results.add(usernameStr);
        String firstNameStr = (u.getFirstName() != null) ? u.getFirstName() : EMPTY;
        results.add(firstNameStr);
        String lastNameStr = (u.getLastName() != null) ? u.getLastName() : EMPTY;
        results.add(lastNameStr);
        String loginCountStr = Integer.toString(u.getLoginCount());
        results.add(loginCountStr);
        String roleStr = "";
        try {
            roleStr = roleNameStr[Integer.decode(u.getRoleURI())];
        } catch (Exception e) {}
        results.add(roleStr);

        request.setAttribute("results",results);

        List<String> mayEditAsUris = uDao.getIndividualsUserMayEditAs(u.getURI());
        if( mayEditAsUris != null && mayEditAsUris.size() > 0 ){
            List<ObjectPropertyStatement> mayEditAsStmts = 
                new ArrayList<ObjectPropertyStatement>(mayEditAsUris.size());
            for(String objURI: mayEditAsUris){
            	Individual editAs = vreq.getFullWebappDaoFactory().getIndividualDao().getIndividualByURI(objURI);
                ObjectPropertyStatement stmt = new ObjectPropertyStatementImpl();
                stmt.setSubjectURI(u.getURI());
                stmt.setPropertyURI(VitroVocabulary.MAY_EDIT_AS);
                stmt.setObjectURI(objURI);
                stmt.setObject(editAs);
                mayEditAsStmts.add(stmt);
            }
            request.setAttribute("mayEditAsStmts", mayEditAsStmts);
        }
        
        /* these are set so that we can use the PropertyEditLinks jsp tags */
        ObjectProperty prop = new ObjectProperty();
        prop.setURI(VitroVocabulary.MAY_EDIT_AS);
        request.setAttribute("mayEditObjProp",prop);        
        Individual entity = new IndividualImpl();
        entity.setURI(u.getURI());
        request.setAttribute("entity", entity);
        
        request.setAttribute("results", results);
        request.setAttribute("columncount", new Integer(5));
        request.setAttribute("suppressquery", "true");

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("user", u);
        request.setAttribute("bodyJsp","/templates/edit/specific/user_edit.jsp");
        request.setAttribute("portalBean",portal);
        request.setAttribute("title","User Account Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doPost(request,response);
    }

}
