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

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.auth.policy.setup.SelfEditingPolicySetup;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;

public class UserRetryController extends BaseEditController {

    private static final String ROLE_PROTOCOL = "role:/";  // this is weird; need to revisit
    private static final Log log = LogFactory.getLog(UserRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
    	
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" encountered exception calling super.doGet()");
        }

        VitroRequest vreq = new VitroRequest(request);

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        epo.setDataAccessObject(vreq.getFullWebappDaoFactory().getVClassDao());

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        UserDao uDao = vreq.getFullWebappDaoFactory().getUserDao();
        epo.setDataAccessObject(uDao);

        User userForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("uri") != null) {
                try {
                    userForEditing = (User)uDao.getUserByURI(request.getParameter("uri"));
                    userForEditing.setRoleURI(ROLE_PROTOCOL+userForEditing.getRoleURI());
                    action = "update";
                    epo.setAction("udpate");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            } else {
                userForEditing = new User();
                userForEditing.setRoleURI(ROLE_PROTOCOL+"1");
            }
            epo.setOriginalBean(userForEditing);
        } else {
            userForEditing = (User) epo.getNewBean();
        }

        populateBeanFromParams(userForEditing, vreq);

        //validators

        //set up any listeners

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }
        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new UserInsertPageForwarder(currPortalId));
        //make a postdelete pageforwarder that will send us to the list of classes
        epo.setPostDeletePageForwarder(new UrlForwarder("listUsers?home="+currPortalId));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(uDao.getClass().getDeclaredMethod("getUserByURI",args));
        } catch (NoSuchMethodException e) {
            log.error(this.getClass().getName()+" could not find the getVClassByURI method");
        }

        HashMap optionMap = new HashMap();

        LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
        List roleOptionList = new LinkedList();
        
        /* bdc34: Datastar needs non-backend-editing users for logging in non-Cornell people*/
        /* SelfEditingPolicySetup.SELF_EDITING_POLICY_WAS_SETUP is set by the SelfEditingPolicySetup context listener */
        boolean selfEditing = (Boolean)getServletContext().getAttribute(SelfEditingPolicySetup.SELF_EDITING_POLICY_WAS_SETUP) == Boolean.TRUE;
        Option nonEditor = new Option(ROLE_PROTOCOL+loginBean.getNonEditor(), "self editor");
        /* self editing should be displayed if we are editing a user account that is already  
         *  self-editing even if self editing is off. */
        if( selfEditing || 
        	( !"insert".equals(action) && userForEditing.getRoleURI().equals(nonEditor.getValue()) )){        	        	
            nonEditor.setSelected(userForEditing.getRoleURI().equals(nonEditor.getValue()));
            if (nonEditor.getSelected() || (Integer.decode(loginBean.getLoginRole()) >= loginBean.getNonEditor()))
                roleOptionList.add(nonEditor); 
        }
        
        Option editor = new Option(ROLE_PROTOCOL+loginBean.getEditor(), "editor");
        editor.setSelected(userForEditing.getRoleURI().equals(editor.getValue()));
        Option curator = new Option(ROLE_PROTOCOL+loginBean.getCurator(), "curator");
        curator.setSelected(userForEditing.getRoleURI().equals(curator.getValue()));
        Option administrator = new Option (ROLE_PROTOCOL+loginBean.getDba(), "system administrator");
        administrator.setSelected(userForEditing.getRoleURI().equals(administrator.getValue()));        
        
        if (editor.getSelected() || (Integer.decode(loginBean.getLoginRole()) >= loginBean.getEditor()))
            roleOptionList.add(editor);
        if (curator.getSelected() || (Integer.decode(loginBean.getLoginRole()) >= loginBean.getCurator()))
            roleOptionList.add(curator);
        if (administrator.getSelected() || (Integer.decode(loginBean.getLoginRole()) >= loginBean.getDba()))
            roleOptionList.add(administrator);

        optionMap.put("Role", roleOptionList);

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());
        foo.setOptionLists(optionMap);
        epo.setFormObject(foo);

        request.setAttribute("formValue",foo.getValues());

        String html = FormUtils.htmlFormFromBean(userForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("user",userForEditing);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        if (userForEditing.getMd5password()==null || userForEditing.getMd5password().equals("")) {
            request.setAttribute("formOnSubmit", "return hashPw(this);");
            request.setAttribute("formOnCancel", "forceCancel(this.form);");
        }
        request.setAttribute("formJsp","/templates/edit/specific/user_retry.jsp");
        request.setAttribute("scripts","/templates/edit/specific/user_retry_head.jsp");
        request.setAttribute("title","User Account Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","User");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    class UserInsertPageForwarder implements PageForwarder {
        private int portalId = 1;

        public UserInsertPageForwarder(int currPortalId) {
            portalId = currPortalId;
        }

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newUserUrl = "userEdit?home="+portalId+"&uri=";
            User u = (User) epo.getNewBean();
            try {
                newUserUrl += URLEncoder.encode(u.getURI(),"UTF-8");
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not use UTF-8 encoding to encode new URL");
            }
            try {
                response.sendRedirect(newUserUrl);
            } catch (IOException ioe) {
                log.error(this.getClass().getName()+" could not send redirect.");
            }
        }
    }

}
