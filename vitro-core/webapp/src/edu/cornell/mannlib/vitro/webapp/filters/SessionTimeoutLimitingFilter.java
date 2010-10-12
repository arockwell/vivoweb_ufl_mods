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

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;

/**
 * Manipulate the maximum inactive interval on sessions.
 * <ul>
 * <li>Logged in sessions and self-editing sessions already have the correct
 * interval set.</li>
 * <li>Other sessions are trivial, and should have a short interval.</li>
 * </ul>
 */
public class SessionTimeoutLimitingFilter implements Filter {
	/** Maximum inactive interval for a trivial session object, in seconds. */
	private static final int TRIVIAL_SESSION_LIFETIME = 120;

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		filterChain.doFilter(servletRequest, servletResponse);

		limitTrivialSession(servletRequest);
	}

	/**
	 * If this request has a trivial session object -- that is, the user is not
	 * logged in and not self-editing -- then give it a short expiration
	 * interval.
	 */
	private void limitTrivialSession(ServletRequest servletRequest) {
		if (!(servletRequest instanceof HttpServletRequest)) {
			return;
		}
		HttpServletRequest request = (HttpServletRequest) servletRequest;

		// If no session object, nothing to do.
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		// If logged in, leave it alone.
		Object loginBean = session.getAttribute("loginHandler");
		if (loginBean instanceof LoginFormBean) {
			return;
		}

		// If self-editing, leave it alone.
		if (VitroRequestPrep.isSelfEditing(request)) {
			return;
		}

		// Otherwise, it's trivial, so shorten its life-span.
		session.setMaxInactiveInterval(TRIVIAL_SESSION_LIFETIME);
	}

	public void destroy() {
	}
}
