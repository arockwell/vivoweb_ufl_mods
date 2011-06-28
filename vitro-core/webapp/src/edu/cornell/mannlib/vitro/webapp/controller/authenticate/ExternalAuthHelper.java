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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * Capture the properties used by the External Authorization system, and use
 * them to assist in the process.
 * 
 * The first time this bean is requested, it is created from the configuration
 * properties and cached in the session. After that, the cached version is used.
 */
public class ExternalAuthHelper {
	private static final Log log = LogFactory.getLog(ExternalAuthHelper.class);

	private static final ExternalAuthHelper DUMMY_HELPER = new ExternalAuthHelper(
			null, null);

	private static final String BEAN_ATTRIBUTE = ExternalAuthHelper.class
			.getName();

	/** This configuration property points to the external authorization server. */
	private static final String PROPERTY_EXTERNAL_AUTH_SERVER_URL = "externalAuth.serverUrl";

	/** This configuration property says which HTTP header holds the username. */
	public static final String PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER = "externalAuth.netIdHeaderName";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Get the bean from the servlet context. If there is no bean, create one.
	 * 
	 * Never returns null.
	 */
	public static ExternalAuthHelper getHelper(ServletRequest request) {
		if (!(request instanceof HttpServletRequest)) {
			log.trace("Not an HttpServletRequest: " + request);
			return DUMMY_HELPER;
		}

		HttpSession session = ((HttpServletRequest) request).getSession(false);
		if (session == null) {
			log.trace("No session; no need to create one.");
			return DUMMY_HELPER;
		}

		ServletContext context = session.getServletContext();

		Object attr = context.getAttribute(BEAN_ATTRIBUTE);
		if (attr instanceof ExternalAuthHelper) {
			log.trace("Found a bean: " + attr);
			return (ExternalAuthHelper) attr;
		}

		ExternalAuthHelper bean = buildBean();
		log.debug("Created a bean: " + bean);
		setBean(context, bean);
		return bean;
	}

	/** It would be private, but we want to allow calls for faking. */
	protected static void setBean(ServletContext context,
			ExternalAuthHelper bean) {
		context.setAttribute(BEAN_ATTRIBUTE, bean);
	}

	private static ExternalAuthHelper buildBean() {
		String externalAuthServerUrl = ConfigurationProperties
				.getProperty(PROPERTY_EXTERNAL_AUTH_SERVER_URL);
		String externalAuthHeaderName = ConfigurationProperties
				.getProperty(PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER);

		return new ExternalAuthHelper(externalAuthServerUrl,
				externalAuthHeaderName);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final String externalAuthServerUrl;
	private final String externalAuthHeaderName;

	/** It would be private, but we want to allow subclasses for faking. */
	protected ExternalAuthHelper(String externalAuthServerUrl,
			String externalAuthHeaderName) {
		this.externalAuthServerUrl = trimThis(externalAuthServerUrl);
		this.externalAuthHeaderName = trimThis(externalAuthHeaderName);
	}

	private String trimThis(String string) {
		if (string == null) {
			return null;
		} else {
			return string.trim();
		}
	}

	public String buildExternalAuthRedirectUrl(String returnUrl) {
		if (returnUrl == null) {
			log.error("returnUrl is null.");
			return null;
		}

		if (externalAuthServerUrl == null) {
			log.debug("deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_SERVER_URL
					+ "' -- sending directly to '" + returnUrl + "'");
			return returnUrl;
		}

		try {
			String encodedReturnUrl = URLEncoder.encode(returnUrl, "UTF-8");
			String externalAuthUrl = externalAuthServerUrl + "?target="
					+ encodedReturnUrl;
			log.debug("externalAuthUrl is '" + externalAuthUrl + "'");
			return externalAuthUrl;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // No UTF-8? Really?
		}
	}

	public String getExternalUsername(HttpServletRequest request) {
		if (request == null) {
			log.error("request is null.");
			return null;
		}

		if (externalAuthHeaderName == null) {
			log.error("User asked for external authentication, "
					+ "but deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER + "'");
			return null;
		}

		String username = request.getHeader(externalAuthHeaderName);
		log.debug("username=" + username);
		return username;
	}

	@Override
	public String toString() {
		return "ExternalAuthHelper[externalAuthServerUrl="
				+ externalAuthServerUrl + ", externalAuthHeaderName="
				+ externalAuthHeaderName + "]";
	}

}
