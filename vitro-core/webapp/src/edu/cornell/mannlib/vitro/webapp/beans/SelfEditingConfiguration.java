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

package edu.cornell.mannlib.vitro.webapp.beans;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * Holds the configuration properties used in Self-Editing, and some commonly
 * used methods on those properties.
 */
public class SelfEditingConfiguration {
	private static final Log log = LogFactory
			.getLog(SelfEditingConfiguration.class);

	private static final String BEAN_ATTRIBUTE = SelfEditingConfiguration.class
			.getName();

	/**
	 * This configuration property tells us which data property on the
	 * Individual is used to associate it with a net ID.
	 */
	private static final String PROPERTY_SELF_EDITING_ID_MATCHING_PROPERTY = "selfEditing.idMatchingProperty";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * If there is no session, create a bean on the fly. If there is a session,
	 * get the existing bean, or create one and store it for re-use.
	 * 
	 * Never returns null.
	 */
	public static SelfEditingConfiguration getBean(ServletRequest request) {
		if (!(request instanceof HttpServletRequest)) {
			log.trace("Not an HttpServletRequest: " + request);
			return buildBean();
		}

		HttpSession session = ((HttpServletRequest) request).getSession(false);
		if (session == null) {
			log.trace("No session; no need to create one.");
			return buildBean();
		}

		Object attr = session.getAttribute(BEAN_ATTRIBUTE);
		if (attr instanceof SelfEditingConfiguration) {
			log.trace("Found a bean: " + attr);
			return (SelfEditingConfiguration) attr;
		}

		SelfEditingConfiguration bean = buildBean();
		log.debug("Created a bean: " + bean);
		session.setAttribute(BEAN_ATTRIBUTE, bean);
		return bean;
	}

	private static SelfEditingConfiguration buildBean() {
		String selfEditingIdMatchingProperty = ConfigurationProperties
				.getProperty(PROPERTY_SELF_EDITING_ID_MATCHING_PROPERTY);
		return new SelfEditingConfiguration(selfEditingIdMatchingProperty);
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	private final String selfEditingIdMatchingProperty;

	public SelfEditingConfiguration(String selfEditingIdMatchingProperty) {
		this.selfEditingIdMatchingProperty = trimThis(selfEditingIdMatchingProperty);
	}

	private String trimThis(String string) {
		if (string == null) {
			return null;
		} else {
			return string.trim();
		}
	}

	public String getIndividualUriFromUsername(IndividualDao indDao,
			String username) {
		if (indDao == null) {
			log.warn("No IndividualDao");
			return null;
		}
		if (username == null) {
			log.debug("username is null");
			return null;
		}
		if (selfEditingIdMatchingProperty == null) {
			log.debug("selfEditingMatchingProperty is null");
			return null;
		}

		String uri = indDao.getIndividualURIFromNetId(username,
				selfEditingIdMatchingProperty);
		log.debug("Username=" + username + ", individual URI=" + uri);
		return uri;
	}

	@Override
	public String toString() {
		return "SelfEditingConfiguration[selfEditingIdMatchingProperty="
				+ selfEditingIdMatchingProperty + "]";
	}

}
