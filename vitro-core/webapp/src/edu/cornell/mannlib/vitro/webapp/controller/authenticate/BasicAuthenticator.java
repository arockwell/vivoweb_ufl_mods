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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy.AuthRole;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LogoutEvent;

/**
 * The "standard" implementation of Authenticator.
 */
public class BasicAuthenticator extends Authenticator {
	/** User roles are recorded in the model like "role:/50", etc. */
	private static final String ROLE_NAMESPACE = "role:/";

	private static final Log log = LogFactory.getLog(BasicAuthenticator.class);

	private final HttpServletRequest request;

	public BasicAuthenticator(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public boolean isExistingUser(String username) {
		return getUserByUsername(username) != null;
	}

	@Override
	public User getUserByUsername(String username) {
		UserDao userDao = getUserDao();
		if (userDao == null) {
			return null;
		}
		return userDao.getUserByUsername(username);
	}

	@Override
	public boolean isCurrentPassword(String username, String clearTextPassword) {
		User user = getUserDao().getUserByUsername(username);
		if (user == null) {
			log.trace("Checking password '" + clearTextPassword
					+ "' for user '" + username + "', but user doesn't exist.");
			return false;
		}

		String md5NewPassword = Authenticate
				.applyMd5Encoding(clearTextPassword);
		return md5NewPassword.equals(user.getMd5password());
	}

	@Override
	public void recordNewPassword(String username, String newClearTextPassword) {
		User user = getUserByUsername(username);
		if (user == null) {
			log.error("Trying to change password on non-existent user: "
					+ username);
			return;
		}
		user.setOldPassword(user.getMd5password());
		user.setMd5password(Authenticate.applyMd5Encoding(newClearTextPassword));
		getUserDao().updateUser(user);
	}

	@Override
	public void recordLoginAgainstUserAccount(String username,
			AuthenticationSource authSource) {
		User user = getUserByUsername(username);
		if (user == null) {
			log.error("Trying to record the login of a non-existent user: "
					+ username);
			return;
		}

		recordLoginOnUserRecord(user);

		String userUri = user.getURI();
		String roleUri = user.getRoleURI();
		int securityLevel = parseUserSecurityLevel(user);
		recordLoginWithOrWithoutUserAccount(username, userUri, roleUri,
				securityLevel, authSource);
	}

	@Override
	public void recordLoginWithoutUserAccount(String username,
			String individualUri, AuthenticationSource authSource) {
		String roleUri = AuthRole.USER.roleUri();
		int securityLevel = LoginStatusBean.NON_EDITOR;
		recordLoginWithOrWithoutUserAccount(username, individualUri, roleUri,
				securityLevel, authSource);
	}

	/** This much is in common on login, whether or not you have a user account. */
	private void recordLoginWithOrWithoutUserAccount(String username,
			String userUri, String roleUri, int securityLevel,
			AuthenticationSource authSource) {
		HttpSession session = request.getSession();
		createLoginFormBean(username, userUri, roleUri, session);
		createLoginStatusBean(username, userUri, securityLevel, authSource,
				session);
		setSessionTimeoutLimit(session);
		recordInUserSessionMap(userUri, session);
		notifyOtherUsers(userUri, session);
	}

	/**
	 * Update the user record to record the login.
	 */
	private void recordLoginOnUserRecord(User user) {
		user.setLoginCount(user.getLoginCount() + 1);
		if (user.getFirstTime() == null) { // first login
			user.setFirstTime(new Date());
		}
		getUserDao().updateUser(user);
	}

	/**
	 * Put the login bean into the session.
	 * 
	 * TODO The LoginFormBean is being phased out.
	 */
	private void createLoginFormBean(String username, String userUri,
			String roleUri, HttpSession session) {
		LoginFormBean lfb = new LoginFormBean();
		lfb.setUserURI(userUri);
		lfb.setLoginStatus("authenticated");
		lfb.setSessionId(session.getId());
		lfb.setLoginRole(roleUri);
		lfb.setLoginRemoteAddr(request.getRemoteAddr());
		lfb.setLoginName(username);
		session.setAttribute("loginHandler", lfb);
	}

	/**
	 * Put the login bean into the session.
	 * 
	 * TODO this should eventually replace the LoginFormBean.
	 */
	private void createLoginStatusBean(String username, String userUri,
			int securityLevel, AuthenticationSource authSource,
			HttpSession session) {
		LoginStatusBean lsb = new LoginStatusBean(userUri, username,
				securityLevel, authSource);
		LoginStatusBean.setBean(session, lsb);
		log.debug("Adding status bean: " + lsb);
	}

	/**
	 * Editors and other privileged users get a longer timeout interval.
	 */
	private void setSessionTimeoutLimit(HttpSession session) {
		if (LoginStatusBean.getBean(session).isLoggedInAtLeast(
				LoginStatusBean.EDITOR)) {
			session.setMaxInactiveInterval(PRIVILEGED_TIMEOUT_INTERVAL);
		} else {
			session.setMaxInactiveInterval(LOGGED_IN_TIMEOUT_INTERVAL);
		}
	}

	/**
	 * Record the login in the user/session map.
	 * 
	 * TODO What is this map used for?
	 */
	private void recordInUserSessionMap(String userUri, HttpSession session) {
		Map<String, HttpSession> userURISessionMap = Authenticate
				.getUserURISessionMapFromContext(session.getServletContext());
		userURISessionMap.put(userUri, session);
	}

	/**
	 * Anyone listening to the model might need to know that another user is
	 * logged in.
	 */
	private void notifyOtherUsers(String userUri, HttpSession session) {
		Authenticate.sendLoginNotifyEvent(new LoginEvent(userUri),
				session.getServletContext(), session);
	}

	@Override
	public List<String> getAssociatedIndividualUris(String username) {
		List<String> uris = new ArrayList<String>();
		uris.addAll(getUrisAssociatedBySelfEditorConfig(username));
		uris.addAll(getUrisAssociatedByMayEditAs(username));
		return uris;
	}

	private List<String> getUrisAssociatedBySelfEditorConfig(String username) {
		if (username == null) {
			return Collections.emptyList();
		}

		IndividualDao iDao = getIndividualDao();
		if (iDao == null) {
			return Collections.emptyList();
		}
		
		String selfEditorUri = SelfEditingConfiguration.getBean(request)
				.getIndividualUriFromUsername(iDao, username);
		if (selfEditorUri == null) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(selfEditorUri);
		}
	}

	private List<String> getUrisAssociatedByMayEditAs(String username) {
		if (username == null) {
			return Collections.emptyList();
		}

		UserDao userDao = getUserDao();
		if (userDao == null) {
			return Collections.emptyList();
		}

		User user = userDao.getUserByUsername(username);
		if (user == null) {
			return Collections.emptyList();
		}

		String userUri = user.getURI();
		if (userUri == null) {
			return Collections.emptyList();
		}

		return userDao.getIndividualsUserMayEditAs(userUri);
	}

	@Override
	public void recordUserIsLoggedOut() {
		HttpSession session = request.getSession();
		notifyOtherUsersOfLogout(session);
		session.invalidate();
	}

	private void notifyOtherUsersOfLogout(HttpSession session) {
		LoginStatusBean loginBean = LoginStatusBean.getBean(session);
		if (!loginBean.isLoggedIn()) {
			return;
		}

		UserDao userDao = getUserDao();
		if (userDao == null) {
			return;
		}

		String username = loginBean.getUsername();
		User user = userDao.getUserByUsername(username);
		if (user == null) {
			log.error("Unable to retrieve user " + username + " from model");
			return;
		}

		Authenticate.sendLoginNotifyEvent(new LogoutEvent(user.getURI()),
				session.getServletContext(), session);
	}

	/**
	 * Get a reference to the UserDao, or null.
	 */
	private UserDao getUserDao() {
		WebappDaoFactory wadf = getWebappDaoFactory();
		if (wadf == null) {
			return null;
		}

		UserDao userDao = wadf.getUserDao();
		if (userDao == null) {
			log.error("getUserDao: no UserDao");
		}

		return userDao;
	}

	/**
	 * Get a reference to the IndividualDao, or null.
	 */
	private IndividualDao getIndividualDao() {
		WebappDaoFactory wadf = getWebappDaoFactory();
		if (wadf == null) {
			return null;
		}
		
		IndividualDao individualDao = wadf.getIndividualDao();
		if (individualDao == null) {
			log.error("getIndividualDao: no IndividualDao");
		}
		
		return individualDao;
	}
	
	/**
	 * Get a reference to the WebappDaoFactory, or null.
	 */
	private WebappDaoFactory getWebappDaoFactory() {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}

		ServletContext servletContext = session.getServletContext();
		WebappDaoFactory wadf = (WebappDaoFactory) servletContext
				.getAttribute("webappDaoFactory");
		if (wadf == null) {
			log.error("no WebappDaoFactory");
			return null;
		}

		return wadf;
	}

	/**
	 * Parse the role URI from User. Don't crash if it is not valid.
	 */
	private int parseUserSecurityLevel(User user) {
		String roleURI = user.getRoleURI();
		try {
			if (roleURI.startsWith(ROLE_NAMESPACE)) {
				String roleLevel = roleURI.substring(ROLE_NAMESPACE.length());
				return Integer.parseInt(roleLevel);
			} else {
				return Integer.parseInt(roleURI);
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid RoleURI '" + roleURI + "' for user '"
					+ user.getURI() + "'");
			return 1;
		}
	}

}
