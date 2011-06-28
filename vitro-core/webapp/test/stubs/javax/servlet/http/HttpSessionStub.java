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

package stubs.javax.servlet.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple stand-in for the HttpSession, for use in unit tests.
 */
@SuppressWarnings("deprecation")
public class HttpSessionStub implements HttpSession {
	private static final Log log = LogFactory.getLog(HttpSessionStub.class);

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private String id = "arbitraryId";
	private ServletContext context;
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	@SuppressWarnings("unused")
	private int maxInactiveInterval;

	public void setId(String id) {
		this.id = id;
	}

	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (name == null) {
			throw new NullPointerException("name may not be null.");
		}
		if (value == null) {
			removeAttribute(name);
		}
		attributes.put(name, value);
		log.debug("setAttribute: " + name + "=" + value);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
		log.debug("removeAttribute: " + name);
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/**
	 * So far, we don't do anything with this, or even confirm that it has been
	 * set. We just don't throw an exception if someone wants to set it.
	 */
	@Override
	public void setMaxInactiveInterval(int seconds) {
		this.maxInactiveInterval = seconds;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		throw new RuntimeException(
				"HttpSessionStub.getAttributeNames() not implemented.");
	}

	@Override
	public long getCreationTime() {
		throw new RuntimeException(
				"HttpSessionStub.getCreationTime() not implemented.");
	}

	@Override
	public long getLastAccessedTime() {
		throw new RuntimeException(
				"HttpSessionStub.getLastAccessedTime() not implemented.");
	}

	@Override
	public int getMaxInactiveInterval() {
		throw new RuntimeException(
				"HttpSessionStub.getMaxInactiveInterval() not implemented.");
	}

	@Override
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		throw new RuntimeException(
				"HttpSessionStub.getSessionContext() not implemented.");
	}

	@Override
	public Object getValue(String arg0) {
		throw new RuntimeException(
				"HttpSessionStub.getValue() not implemented.");
	}

	@Override
	public String[] getValueNames() {
		throw new RuntimeException(
				"HttpSessionStub.getValueNames() not implemented.");
	}

	@Override
	public void invalidate() {
		throw new RuntimeException(
				"HttpSessionStub.invalidate() not implemented.");
	}

	@Override
	public boolean isNew() {
		throw new RuntimeException("HttpSessionStub.isNew() not implemented.");
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		throw new RuntimeException(
				"HttpSessionStub.putValue() not implemented.");
	}

	public void removeValue(String arg0) {
		throw new RuntimeException(
				"HttpSessionStub.removeValue() not implemented.");
	}

}
