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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Store a message in the session, so it can be displayed on the page. Getting
 * the message also removes it from the session, so the same message is not
 * displayed repeatedly.
 */
public class DisplayMessage {
	private static final Log log = LogFactory.getLog(DisplayMessage.class);

	/** If there is no message, return this instead. */
	private static final String NO_MESSAGE = "";

	private static final String ATTRIBUTE_NAME = DisplayMessage.class.getName();

	/**
	 * Store this message on the session. This will overwrite any previously
	 * stored message.
	 */
	public static void setMessage(HttpServletRequest request, String message) {
		setMessage(request.getSession(), message);
	}

	/**
	 * Store this message on the session. This will overwrite any previously
	 * stored message.
	 */
	public static void setMessage(HttpSession session, String message) {
		session.setAttribute(ATTRIBUTE_NAME, message);
		log.debug("Set message: '" + message + "'");
	}

	/**
	 * Get the current message from the session, and remove it from the session
	 * so it won't be displayed again.
	 * 
	 * If there is no message, return the empty string.
	 */
	public static String getMessageAndClear(HttpServletRequest request) {
		if (request == null) {
			return NO_MESSAGE;
		} else {
			return getMessageAndClear(request.getSession(false));
		}
	}

	/**
	 * Get the current message from the session, and remove it from the session
	 * so it won't be displayed again.
	 * 
	 * If there is no message, return the empty string.
	 */
	public static String getMessageAndClear(HttpSession session) {
	    String message = NO_MESSAGE;
		if (session != null) {
			Object sessionMessage = session.getAttribute(ATTRIBUTE_NAME);
			if (sessionMessage != null) {
    			if (sessionMessage instanceof String) {
    				log.debug("Get message: '" + sessionMessage + "'");    				
    				message = (String) sessionMessage;
    			} 
    			session.removeAttribute(ATTRIBUTE_NAME);	
			} else {
			    log.debug("Get no message.");
			}
		}				
		return message;
	}
}
