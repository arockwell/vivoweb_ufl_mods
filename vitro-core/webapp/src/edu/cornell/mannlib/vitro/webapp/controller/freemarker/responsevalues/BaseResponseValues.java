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

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public abstract class BaseResponseValues implements ResponseValues {

    private int statusCode = 0;
    private ContentType contentType = null;
    
    BaseResponseValues() { }
    
    BaseResponseValues(int statusCode) {
        this.statusCode = statusCode;
    }

    BaseResponseValues(ContentType contentType) {
        this.contentType = contentType;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getTemplateName() {
        throw new UnsupportedOperationException(
                "This is not a template response.");
    }

    @Override
    public Map<String, Object> getMap() {
        throw new UnsupportedOperationException(
                "This is not a template response.");
    }

    @Override
    public String getRedirectUrl() {
        throw new UnsupportedOperationException(
                "This is not a redirect response.");
    }
    
    @Override
    public Map<String, String> getHeader() {
        throw new UnsupportedOperationException(
                "This is not a header response.");
    }
    
    @Override
    public String getForwardUrl() {
        throw new UnsupportedOperationException(
                "This is not a forwarding response.");
    }
    
    @Override
    public Throwable getException() {
        throw new UnsupportedOperationException(
                "This is not an exception response.");
    }

    @Override
    public Model getModel() {
        throw new UnsupportedOperationException(
                "This is not an RDF response.");
    }
}
