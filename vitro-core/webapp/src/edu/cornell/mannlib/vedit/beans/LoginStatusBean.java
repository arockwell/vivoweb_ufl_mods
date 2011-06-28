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

package edu.cornell.mannlib.vedit.beans;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An immutable object that records the user's login info as a session
 * attribute.
 */
public class LoginStatusBean {
	private static final Log log = LogFactory.getLog(LoginStatusBean.class);

	/**
	 * Security level when the user has not logged in. Also used as a minimum
	 * level when we want to include every user, logged in or not.
	 */
	public static final int ANYBODY = 0;

	/** Security level when a user with no privileges is logged in. */
	public static final int NON_EDITOR = 1;

	/** Security level when an authorized editor is logged in. */
	public static final int EDITOR = 4;

	/** Security level when an authorized curator is logged in. */
	public static final int CURATOR = 5;

	/** Security level when a system administrator is logged in. */
	public static final int DBA = 50;

	/** A bean to return when the user has not logged in. */
	private static final LoginStatusBean DUMMY_BEAN = new LoginStatusBean("",
			"", ANYBODY, AuthenticationSource.UNKNOWN);

	/** The bean is attached to the session by this name. */
	private static final String ATTRIBUTE_NAME = "loginStatus";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Attach this bean to the session - this means you are logged in.
	 */
	public static void setBean(HttpSession session, LoginStatusBean lsb) {
		session.setAttribute(ATTRIBUTE_NAME, lsb);
	}

	/**
	 * Get the bean from this request, or a dummy bean if the user is not logged
	 * in. Never returns null.
	 */
	public static LoginStatusBean getBean(HttpServletRequest request) {
		if (request == null) {
			return DUMMY_BEAN;
		}

		HttpSession session = request.getSession(false);
		if (session == null) {
			return DUMMY_BEAN;
		}

		return getBean(session);
	}

	/**
	 * Get the bean from this session, or a dummy bean if the user is not logged
	 * in. Never returns null.
	 */
	public static LoginStatusBean getBean(HttpSession session) {
		if (session == null) {
			return DUMMY_BEAN;
		}

		Object o = session.getAttribute(ATTRIBUTE_NAME);
		if (o == null) {
			return DUMMY_BEAN;
		}

		if (!(o instanceof LoginStatusBean)) {
			log.warn("Tried to get login status bean, but found an instance of "
					+ o.getClass().getName() + ": " + o);
			return DUMMY_BEAN;
		}

		return (LoginStatusBean) o;
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	public enum AuthenticationSource {
		UNKNOWN, INTERNAL, EXTERNAL
	}

	private final String userURI;
	private final String username;
	private final int securityLevel;
	private final AuthenticationSource authenticationSource;

	public LoginStatusBean(String userURI, String username, int securityLevel,
			AuthenticationSource authenticationSource) {
		this.userURI = userURI;
		this.username = username;
		this.securityLevel = securityLevel;
		this.authenticationSource = authenticationSource;
	}

	public String getUserURI() {
		return userURI;
	}

	public String getUsername() {
		return username;
	}

	public int getSecurityLevel() {
		return securityLevel;
	}

	public AuthenticationSource getAuthenticationSource() {
		return authenticationSource;
	}

	public boolean isLoggedIn() {
		return securityLevel > ANYBODY;
	}

	public boolean isLoggedInExactly(int level) {
		return securityLevel == level;
	}

	public boolean isLoggedInAtLeast(int minimumLevel) {
		return securityLevel >= minimumLevel;
	}

	public boolean hasExternalAuthentication() {
		return authenticationSource == AuthenticationSource.EXTERNAL;
	}

	@Override
	public String toString() {
		return "LoginStatusBean[userURI=" + userURI + ", username=" + username
				+ ", securityLevel=" + securityLevel
				+ ", authenticationSource=" + authenticationSource + "]";
	}

}
