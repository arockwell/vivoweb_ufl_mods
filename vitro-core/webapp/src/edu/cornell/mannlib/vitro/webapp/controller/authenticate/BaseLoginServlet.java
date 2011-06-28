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

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;

/**
 * Base class for all Login servlets, whether Shibboleth, CuWebAuth, etc.
 */
public class BaseLoginServlet extends HttpServlet {
	private static final Log log = LogFactory.getLog(BaseLoginServlet.class);

	/** A general purpose error message for the user to see. */
	protected static final Message MESSAGE_LOGIN_FAILED = new LoginProcessBean.Message(
			"External login failed.", LoginProcessBean.MLevel.ERROR);

	protected Authenticator getAuthenticator(HttpServletRequest req) {
		return Authenticator.getInstance(req);
	}

	/**
	 * Store an error message in the login bean and go back where we came from.
	 * 
	 * Remove the referring URL from the session after using it.
	 */
	protected void complainAndReturnToReferrer(HttpServletRequest req,
			HttpServletResponse resp, String sessionAttributeForReferrer,
			Message message, Object... args) throws IOException {
		log.debug(message.getMessageLevel() + ": "
				+ message.formatMessage(args));
		LoginProcessBean.getBean(req).setMessage(message, args);

		String referrer = (String) req.getSession().getAttribute(
				sessionAttributeForReferrer);
		log.debug("returning to referrer: " + referrer);
		if (referrer == null) {
			referrer = figureHomePageUrl(req);
			log.debug("returning to home page: " + referrer);
		}

		req.getSession().removeAttribute(sessionAttributeForReferrer);
		resp.sendRedirect(referrer);
	}

	/**
	 * If we don't have a referrer, send them to the home page.
	 */
	protected String figureHomePageUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		int authLength = url.length() - uri.length();
		String auth = url.substring(0, authLength);
		return auth + req.getContextPath();
	}
}
