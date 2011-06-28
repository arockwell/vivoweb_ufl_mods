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

package edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

/**
 * A wrapper for a servlet request that does not hold multipart content. Pass
 * all parameter-related requests to the delegate, and give simple answers to
 * all file-related requests.
 */
class SimpleHttpServletRequestWrapper extends FileUploadServletRequest {

	SimpleHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		request.setAttribute(FILE_ITEM_MAP, Collections.EMPTY_MAP);
	}

	// ----------------------------------------------------------------------
	// Not a multipart request, so there are no files or upload exceptions.
	// ----------------------------------------------------------------------

	@Override
	public boolean isMultipart() {
		return false;
	}

	@Override
	public Map<String, List<FileItem>> getFiles() {
		return Collections.emptyMap();
	}

	@Override
	public FileItem getFileItem(String string) {
		return null;
	}

	@Override
	public FileUploadException getFileUploadException() {
		return null;
	}

	@Override
	public boolean hasFileUploadException() {
		return false;
	}

	// ----------------------------------------------------------------------
	// Since this is not a multipart request, the parameter methods can be
	// delegated.
	// ----------------------------------------------------------------------

	@Override
	public String getParameter(String name) {
		return getDelegate().getParameter(name);
	}

	@Override
	public Map<?, ?> getParameterMap() {
		return getDelegate().getParameterMap();
	}

	@Override
	public Enumeration<?> getParameterNames() {
		return getDelegate().getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		return getDelegate().getParameterValues(name);
	}

}
