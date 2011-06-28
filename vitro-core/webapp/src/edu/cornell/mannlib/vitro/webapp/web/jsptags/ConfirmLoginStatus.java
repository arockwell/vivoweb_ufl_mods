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

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LogoutRedirector;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

/**
 * JSP tag to generate the HTML of links for edit, delete or add of a Property.
 * 
 * Maybe we should have a mode where it just sets a var to a map with "href" =
 * "edit/editDatapropDispatch.jsp?subjectUri=..." and "type" = "delete"
 * 
 * @author bdc34
 * 
 */
public class ConfirmLoginStatus extends BodyTagSupport {
	private static final Log log = LogFactory.getLog(ConfirmLoginStatus.class);

	int level = LoginStatusBean.NON_EDITOR;
	boolean allowSelfEditing;
	String beanAttributeName;

	public String getLevel() {
		return String.valueOf(level);
	}

	public void setLevel(String levelString) {
		if ("DBA".equals(levelString)) {
			this.level = LoginStatusBean.DBA;
		} else if ("CURATOR".equals(levelString)) {
			this.level = LoginStatusBean.CURATOR;
		} else if ("EDITOR".equals(levelString)) {
			this.level = LoginStatusBean.EDITOR;
		} else if ("NON_EDITOR".equals(levelString)) {
			this.level = LoginStatusBean.NON_EDITOR;
		} else {
			throw new IllegalArgumentException("Level attribute '"
					+ levelString + "' is not valid.");
		}
	}

	public void setAllowSelfEditing(boolean allowSelfEditing) {
		this.allowSelfEditing = allowSelfEditing;
	}

	public boolean getAllowSelfEditing() {
		return this.allowSelfEditing;
	}

	public String getBean() {
		return this.beanAttributeName;
	}

	public void setbean(String beanAttributeName) {
		this.beanAttributeName = beanAttributeName;
	}

	@Override
	public int doEndTag() throws JspException {
		LogoutRedirector.recordRestrictedPageUri(getRequest());

		LoginStatusBean loginBean = LoginStatusBean.getBean(getRequest());
		boolean isLoggedIn = loginBean.isLoggedIn();
		boolean isSufficient = loginBean.isLoggedInAtLeast(level);

		boolean isSelfEditing = VitroRequestPrep.isSelfEditing(getRequest());

		log.debug("loginLevel=" + loginBean.getSecurityLevel()
				+ ", requiredLevel=" + level + ", selfEditingAllowed="
				+ allowSelfEditing + ", isSelfEditing=" + isSelfEditing);

		if (isSufficient || (allowSelfEditing && isSelfEditing)) {
			log.debug("Login status confirmed.");
			return setBeanAndReturn(loginBean);
		} else if (isLoggedIn) {
			log.debug("Logged in, but not sufficient.");
			return showInsufficientAuthorizationMessage();
		} else {
			log.debug("Login status not confirmed.");
			return redirectAndSkipPage();
		}

	}

	private int setBeanAndReturn(LoginStatusBean loginBean) {
		if (beanAttributeName != null) {
			getRequest().setAttribute(beanAttributeName, loginBean);
		}
		return EVAL_PAGE;
	}

	private int showInsufficientAuthorizationMessage() {
		VitroHttpServlet.redirectToInsufficientAuthorizationPage(getRequest(),
				getResponse());
		return SKIP_PAGE;
	}

	private int redirectAndSkipPage() throws JspException {
		VitroHttpServlet.redirectToLoginPage(getRequest(), getResponse());
		return SKIP_PAGE;
	}

	private HttpServletRequest getRequest() {
		return ((HttpServletRequest) pageContext.getRequest());
	}

	private HttpServletResponse getResponse() {
		return (HttpServletResponse) pageContext.getResponse();
	}
}
