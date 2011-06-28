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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This sits in the session to say that a login is in process.
 * 
 * Authenticate sets the flag each time it redirects to the login widget, and
 * the login widget inspects the flag and resets it.
 * 
 * If ever the login widget finds that the flag is already reset, it knows that
 * the user navigated to the widget directly, rather than coming through
 * Authenticate, and so it discards any existing LoginProcessBean as obsolete.
 */
public class LoginInProcessFlag {
	private static final String ATTRIBUTE_NAME = LoginInProcessFlag.class
			.getName();

	/**
	 * Set the flag, saying that a login session is in process.
	 */
	public static void set(HttpServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request may not be null.");
		}
		
		request.getSession().setAttribute(ATTRIBUTE_NAME, Boolean.TRUE);
	}

	/**
	 * Check to see whether the flag is set. Reset it.
	 */
	public static boolean checkAndReset(HttpServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request may not be null.");
		}
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}
		
		Object flag = session.getAttribute(ATTRIBUTE_NAME);
		if (flag == null) {
			return false;
		}

		session.removeAttribute(ATTRIBUTE_NAME);
		return true;
	}
}
