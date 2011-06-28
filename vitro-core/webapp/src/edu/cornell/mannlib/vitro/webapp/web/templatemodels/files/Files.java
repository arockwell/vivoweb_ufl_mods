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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.files;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class Files extends BaseTemplateModel {
    
    protected LinkedHashSet<String> list = null;
    private String themeDir = null;
    
    @SuppressWarnings("serial")
	private static final Set<String> allowedExternalUrlPatterns = new HashSet<String>() {{
    	add("http://");
    	add("https://");
    }};  
    
    public Files() {
        this.list = new LinkedHashSet<String>();
    }
    
    public Files(String themeDir) {
        this();
        this.themeDir = themeDir;
    }
    
    public Files(LinkedHashSet<String> list) {
        this.list = list;
    }
    
    public void add(String path) {

    	// Allow for an external url
    	for (String currentPattern : allowedExternalUrlPatterns) {
    		if (path.startsWith(currentPattern)) {
    			list.add(path);
    			return;
    		}
    	}

    	// If an external url pattern was not found. 
        list.add(getUrl(path));
    }
    
    public void add(String... paths) {
        for (String path : paths) {
            add(path);
        }
    }
    
    public void addFromTheme(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = themeDir + path;
        add(path);
    }
    
    public void addFromTheme(String... paths) {
        for (String path : paths) {
            addFromTheme(path);
        }
    }
    
    public String getTags() {
        String tags = "";
      
        for (String file : list) {
            tags += getTag(file);
        }
        return tags;
    }
    
    public String dump() {
        return list.toString();
    }

    protected abstract String getTag(String url);
    
}
