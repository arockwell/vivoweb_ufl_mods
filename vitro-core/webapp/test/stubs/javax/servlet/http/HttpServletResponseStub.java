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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple stub for HttpServletResponse
 */
@SuppressWarnings("deprecation")
public class HttpServletResponseStub implements HttpServletResponse {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private String redirectLocation;
	private int status = 200;
	private String errorMessage;
	private StringWriter outputWriter = new StringWriter();

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public int getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public void sendRedirect(String location) throws IOException {
		this.redirectLocation = location;
	}

	@Override
	public void sendError(int status) throws IOException {
		this.status = status;
	}

	@Override
	public void sendError(int status, String message) throws IOException {
		this.status = status;
		this.errorMessage = message;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(outputWriter);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void flushBuffer() throws IOException {
		throw new RuntimeException(
				"HttpServletResponseStub.flushBuffer() not implemented.");
	}

	@Override
	public int getBufferSize() {
		throw new RuntimeException(
				"HttpServletResponseStub.getBufferSize() not implemented.");
	}

	@Override
	public String getCharacterEncoding() {
		throw new RuntimeException(
				"HttpServletResponseStub.getCharacterEncoding() not implemented.");
	}

	@Override
	public String getContentType() {
		throw new RuntimeException(
				"HttpServletResponseStub.getContentType() not implemented.");
	}

	@Override
	public Locale getLocale() {
		throw new RuntimeException(
				"HttpServletResponseStub.getLocale() not implemented.");
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		throw new RuntimeException(
				"HttpServletResponseStub.getOutputStream() not implemented.");
	}

	@Override
	public boolean isCommitted() {
		throw new RuntimeException(
				"HttpServletResponseStub.isCommitted() not implemented.");
	}

	@Override
	public void reset() {
		throw new RuntimeException(
				"HttpServletResponseStub.reset() not implemented.");
	}

	@Override
	public void resetBuffer() {
		throw new RuntimeException(
				"HttpServletResponseStub.resetBuffer() not implemented.");
	}

	@Override
	public void setBufferSize(int arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setBufferSize() not implemented.");
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setCharacterEncoding() not implemented.");
	}

	@Override
	public void setContentLength(int arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setContentLength() not implemented.");
	}

	@Override
	public void setContentType(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setContentType() not implemented.");
	}

	@Override
	public void setLocale(Locale arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setLocale() not implemented.");
	}

	@Override
	public void addCookie(Cookie arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.addCookie() not implemented.");
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.addDateHeader() not implemented.");
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.addHeader() not implemented.");
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.addIntHeader() not implemented.");
	}

	@Override
	public boolean containsHeader(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.containsHeader() not implemented.");
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeRedirectURL() not implemented.");
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeRedirectUrl() not implemented.");
	}

	@Override
	public String encodeURL(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeURL() not implemented.");
	}

	@Override
	public String encodeUrl(String arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.encodeUrl() not implemented.");
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setDateHeader() not implemented.");
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setHeader() not implemented.");
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setIntHeader() not implemented.");
	}

	@Override
	public void setStatus(int arg0) {
		throw new RuntimeException(
				"HttpServletResponseStub.setStatus() not implemented.");
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		throw new RuntimeException(
				"HttpServletResponseStub.setStatus() not implemented.");
	}

}
