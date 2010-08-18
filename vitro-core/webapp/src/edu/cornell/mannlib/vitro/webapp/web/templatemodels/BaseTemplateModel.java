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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Params;

public abstract class BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseTemplateModel.class.getName());
    
    protected static ServletContext servletContext = null;

    // Wrap UrlBuilder method so templates can call ${item.url}
    public String getUrl(String path) {
        return UrlBuilder.getUrl(path);
    }

    // Wrap UrlBuilder method so templates can call ${item.url}
    public String getUrl(String path, Params params) {
        return UrlBuilder.getUrl(path, params);
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(ServletContext context) {
        BaseTemplateModel.servletContext = context;
    }

    /*
     * public static List<?> wrapList(List<?> list, Class cl) 
     * throw error if cl not a child of ViewObject
     * This block of code is going to be repeated a lot:
            List<VClassGroup> groups = // code to get the data
            List<VClassGroupView> vcgroups = new ArrayList<VClassGroupView>(groups.size());
            Iterator<VClassGroup> i = groups.iterator();
            while (i.hasNext()) {
                vcgroups.add(new VClassGroupView(i.next()));
            }
            body.put("classGroups", vcgroups);
    Can we generalize it to a generic method of ViewObject - wrapList() ? 
    static method of ViewObject
    Params: groups, VClassGroupView (the name of the class) - but must be a child of ViewObject
    Return: List<viewObjectType>
     */

}
