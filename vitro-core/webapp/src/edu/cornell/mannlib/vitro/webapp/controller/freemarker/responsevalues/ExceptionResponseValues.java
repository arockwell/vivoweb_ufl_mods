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

public class ExceptionResponseValues extends TemplateResponseValues {
    private final static String DEFAULT_TEMPLATE_NAME = "error-standard.ftl";
    private final Throwable cause;

    public ExceptionResponseValues(Throwable cause) {
        super(DEFAULT_TEMPLATE_NAME);
        this.cause = cause;
    }

    public ExceptionResponseValues(Throwable cause, int statusCode) {
        super(DEFAULT_TEMPLATE_NAME, statusCode);
        this.cause = cause;
    }
    
    public ExceptionResponseValues(String templateName, Throwable cause) {
        super(templateName);
        this.cause = cause;
    }

    public ExceptionResponseValues(String templateName, Throwable cause, int statusCode) {
        super(templateName, statusCode);
        this.cause = cause;
    }
    
    public ExceptionResponseValues(String templateName, Map<String, Object> map, Throwable cause) {
        super(templateName, map);
        this.cause = cause;
    }

    public ExceptionResponseValues(String templateName, Map<String, Object> map, Throwable cause, int statusCode) {
        super(templateName, map, statusCode);
        this.cause = cause;
    }

    @Override
    public Throwable getException() {
        return cause;
    }       
}
