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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
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
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.listener.EditPreProcessor;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vedit.validator.Validator;
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
        Validator v = new PairedPasswordValidator();
        HashMap<String, List<Validator>> validatorMap = new HashMap<String, List<Validator>>();
        List<Validator> vList = Collections.singletonList(v);
		validatorMap.put("Md5password", vList);
		validatorMap.put("passwordConfirmation", vList);
        epo.setValidatorMap(validatorMap);

        //preprocessors
        epo.setPreProcessorList(Collections.singletonList(new UserPasswordPreProcessor()));
        
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

        LoginStatusBean loginBean = LoginStatusBean.getBean(request);
        List roleOptionList = new LinkedList();
        
        /* bdc34: Datastar needs non-backend-editing users for logging in non-Cornell people*/
        /* SelfEditingPolicySetup.SELF_EDITING_POLICY_WAS_SETUP is set by the SelfEditingPolicySetup context listener */
        boolean selfEditing = (Boolean)getServletContext().getAttribute(SelfEditingPolicySetup.SELF_EDITING_POLICY_WAS_SETUP) == Boolean.TRUE;
        Option nonEditor = new Option(ROLE_PROTOCOL+LoginStatusBean.NON_EDITOR, "self editor");
        /* self editing should be displayed if we are editing a user account that is already  
         *  self-editing even if self editing is off. */
        if( selfEditing || 
        	( !"insert".equals(action) && userForEditing.getRoleURI().equals(nonEditor.getValue()) )){        	        	
            nonEditor.setSelected(userForEditing.getRoleURI().equals(nonEditor.getValue()));
            if (nonEditor.getSelected() || loginBean.isLoggedInAtLeast(LoginStatusBean.NON_EDITOR))
                roleOptionList.add(nonEditor); 
        }
        
        Option editor = new Option(ROLE_PROTOCOL+LoginStatusBean.EDITOR, "editor");
        editor.setSelected(userForEditing.getRoleURI().equals(editor.getValue()));
        Option curator = new Option(ROLE_PROTOCOL+LoginStatusBean.CURATOR, "curator");
        curator.setSelected(userForEditing.getRoleURI().equals(curator.getValue()));
        Option administrator = new Option (ROLE_PROTOCOL+LoginStatusBean.DBA, "system administrator");
        administrator.setSelected(userForEditing.getRoleURI().equals(administrator.getValue()));        
        
        if (editor.getSelected() || loginBean.isLoggedInAtLeast(LoginStatusBean.EDITOR))
            roleOptionList.add(editor);
        if (curator.getSelected() || loginBean.isLoggedInAtLeast(LoginStatusBean.CURATOR))
            roleOptionList.add(curator);
        if (administrator.getSelected() || loginBean.isLoggedInAtLeast(LoginStatusBean.DBA))
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
            request.setAttribute("formOnSubmit", "return validatePw(this);");
            request.setAttribute("formOnCancel", "forceCancel(this.form);");
        }
       else {
            request.setAttribute("formOnSubmit", "return validateUserFields(this);");
            request.setAttribute("formOnCancel", "forceCancelTwo(this.form);");
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

    /**
     * Create one of these and assign it to both password fields.
     */
    class PairedPasswordValidator implements Validator {
    	private String otherValue;
    	
		/**
		 * Validate the length of this password, and stash it for the other
		 * validator to compare to.
		 * 
		 * This relies on the fact that {@link #validate(Object)} will be called
		 * once for each of the password fields.
		 */
		@Override
		public ValidationObject validate(Object value)
				throws IllegalArgumentException {
			log.trace("validate password pair: " + value + ", " + otherValue);

			// Must be a non-null String
			if (!(value instanceof String)) {
				log.trace("not a string: " + value);
				return ValidationObject.failure(value, "Please enter a value");
			}

			// Must be within the length limits.
			String string = (String) value;
			if ((string.length() < User.MIN_PASSWORD_LENGTH)
					|| (string.length() > User.MAX_PASSWORD_LENGTH)) {
				log.trace("bad length: " + value);
				return ValidationObject.failure(value,
						"Please enter a password between "
								+ User.MIN_PASSWORD_LENGTH + " and "
								+ User.MAX_PASSWORD_LENGTH
								+ " characters long.");
			}

			// If we haven't validate the other yet, just store this value.
			if (otherValue == null) {
				log.trace("first of the pair: " + value);
				otherValue = string;
				return ValidationObject.success(value);
			}

			// Compare this value to the stored one.
			String otherString = otherValue;
			otherValue = null;
			if (string.equals(otherString)) {
				log.trace("values are equal: " + value);
				return ValidationObject.success(value);
			} else {
				log.trace("values are not equal: " + value + ", " + otherValue);
				return ValidationObject.failure(value,
						"The passwords do not match.");
			}
		}
    }
    
	/**
	 * The "Md5password" field from the form is actually in clear text. Pull the
	 * raw version from the {@link User} and replce it with an encoded one.
	 */
	class UserPasswordPreProcessor implements EditPreProcessor {
		@Override
		public void process(Object o, EditProcessObject epo) {
			if (!(o instanceof User)) {
				log.error("Can't apply password encoding without a User object: "
						+ o);
				return;
			}
			User user = (User) o;
			String rawPassword = user.getMd5password();

			String encodedPassword = Authenticate.applyMd5Encoding(rawPassword);

			log.trace("Raw password '" + rawPassword + "', encoded '"
					+ encodedPassword + "'");
			user.setMd5password(encodedPassword);
		}
	}

}
