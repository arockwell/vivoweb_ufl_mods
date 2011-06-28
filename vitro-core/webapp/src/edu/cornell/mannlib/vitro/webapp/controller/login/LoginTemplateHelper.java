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

package edu.cornell.mannlib.vitro.webapp.controller.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import freemarker.template.Configuration;

/**
 * A temporary means of displaying the Login templates within the SiteAdmin
 * form.
 * 
 * This class contains stuff that I swiped from {@link Authenticate}. The base
 * class, {@link LoginTemplateHelperBase}, contains stuff that I swiped from
 * {@link FreemarkerHttpServlet}.
 */
public class LoginTemplateHelper extends LoginTemplateHelperBase {
	private static final Log log = LogFactory.getLog(LoginTemplateHelper.class);

	/** If they are logging in, show them this form. */
	public static final String TEMPLATE_LOGIN = "login-form.ftl";

	/** If they are changing their password on first login, show them this form. */
	public static final String TEMPLATE_FORCE_PASSWORD_CHANGE = "login-forcedPasswordChange.ftl";

	/** Show error message */
	public static final String TEMPLATE_SERVER_ERROR = Template.ERROR_MESSAGE.toString();

	public static final String BODY_LOGIN_NAME = "loginName";
	public static final String BODY_FORM_ACTION = "formAction";
	public static final String BODY_INFO_MESSAGE = "infoMessage";
	public static final String BODY_ERROR_MESSAGE = "errorMessage";
	public static final String BODY_CANCEL_URL = "cancelUrl";

	public LoginTemplateHelper(HttpServletRequest req) {
		super(req);
	}

	/** Version for JSP page */
	public String showLoginPage(HttpServletRequest request) {
		VitroRequest vreq = new VitroRequest(request);
		try {
			State state = getCurrentLoginState(vreq);
			log.debug("State on exit: " + state);

			switch (state) {
			case LOGGED_IN:
				return "";
			case FORCED_PASSWORD_CHANGE:
				return doTemplate(vreq, showPasswordChangeScreen(vreq));
			default:
				return doTemplate(vreq, showLoginScreen(vreq));
			}
		} catch (Exception e) {
			log.error(e, e);
			return doTemplate(vreq, showError(e));
		}
	}

	/** Version for Freemarker page */
	public TemplateResponseValues showLoginPanel(VitroRequest vreq) {
		try {

			State state = getCurrentLoginState(vreq);
			log.debug("State on exit: " + state);

			switch (state) {
			// RY Why does this case exist? We don't call this method if a user is logged in.
			case LOGGED_IN:
				return null;
			case FORCED_PASSWORD_CHANGE:
				// return doTemplate(vreq, showPasswordChangeScreen(vreq), body, config);
				return showPasswordChangeScreen(vreq);
			default:
				// return doTemplate(vreq, showLoginScreen(vreq), body, config);
				return showLoginScreen(vreq);
			}
		} catch (Exception e) {
			log.error(e, e);
			return showError(e);
		}
	}

	/**
	 * User is just starting the login process. Be sure that we have a
	 * {@link LoginProcessBean} with the correct status. Show them the login
	 * screen.
	 */
	private TemplateResponseValues showLoginScreen(VitroRequest vreq)
			throws IOException {
		LoginProcessBean bean = LoginProcessBean.getBean(vreq);
		bean.setState(State.LOGGING_IN);
		log.trace("Going to login screen: " + bean);

		TemplateResponseValues trv = new TemplateResponseValues(TEMPLATE_LOGIN);
		trv.put(BODY_FORM_ACTION, getAuthenticateUrl(vreq));
		trv.put(BODY_LOGIN_NAME, bean.getUsername());

		String infoMessage = bean.getInfoMessageAndClear();
		if (!infoMessage.isEmpty()) {
			trv.put(BODY_INFO_MESSAGE, infoMessage);
		}
		String errorMessage = bean.getErrorMessageAndClear();
		if (!errorMessage.isEmpty()) {
			trv.put(BODY_ERROR_MESSAGE, errorMessage);
		}

		return trv;
	}

	/**
	 * The user has given the correct password, but now they are required to
	 * change it (unless they cancel out).
	 */
	private TemplateResponseValues showPasswordChangeScreen(VitroRequest vreq) {
		LoginProcessBean bean = LoginProcessBean.getBean(vreq);
		bean.setState(State.FORCED_PASSWORD_CHANGE);
		log.trace("Going to password change screen: " + bean);

		TemplateResponseValues trv = new TemplateResponseValues(
				TEMPLATE_FORCE_PASSWORD_CHANGE);
		trv.put(BODY_FORM_ACTION, getAuthenticateUrl(vreq));
		trv.put(BODY_CANCEL_URL, getCancelUrl(vreq));

		String errorMessage = bean.getErrorMessageAndClear();
		if (!errorMessage.isEmpty()) {
			trv.put(BODY_ERROR_MESSAGE, errorMessage);
		}
		return trv;
	}

	private TemplateResponseValues showError(Exception e) {
		TemplateResponseValues trv = new TemplateResponseValues(
				TEMPLATE_SERVER_ERROR);
		trv.put(BODY_ERROR_MESSAGE, "Internal server error:<br /> " + e);
		return trv;
	}

	/**
	 * We processed a response, and want to show a template. Version for JSP
	 * page.
	 */
	private String doTemplate(VitroRequest vreq, TemplateResponseValues values) {
		// Set it up like FreeMarkerHttpServlet.doGet() would do.
		Configuration config = getConfig(vreq);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(getPageTemplateValues(vreq));
		map.putAll(values.getMap());

		return processTemplateToString(values.getTemplateName(), map, config, vreq);
	}

	/**
	 * Where are we in the process? Logged in? Not? Somewhere in between?
	 */
	private State getCurrentLoginState(HttpServletRequest request) {
		if (LoginStatusBean.getBean(request).isLoggedIn()) {
			return State.LOGGED_IN;
		} else {
			return LoginProcessBean.getBean(request).getState();
		}
	}

	/** What's the URL for this servlet? */
	private String getAuthenticateUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?login=block";
		return contextPath + "/authenticate" + urlParams;
	}

	/** What's the URL for this servlet, with the cancel parameter added? */
	private String getCancelUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?login=block&cancel=true";
		return contextPath + "/authenticate" + urlParams;
	}
}
