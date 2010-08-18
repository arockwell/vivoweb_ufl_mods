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

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.filters.PortalPickerFilter;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class UrlBuilder {

    private static final Log log = LogFactory.getLog(UrlBuilder.class.getName());

    protected static String contextPath = null;   
    private static boolean addPortalParam = PortalPickerFilter.isPortalPickingActive();
    
    private Portal portal;
        
    public enum Route {
        ABOUT("/about"),
        BROWSE("/browse"),
        CONTACT("/contact"),
        INDIVIDUAL("/individual"),
        INDIVIDUAL_LIST("/entitylist"), // entitylist individuallist
        SEARCH("/search"),
        TERMS_OF_USE("/termsOfUse"),
        
        // RY put these under /admin/
        LOGIN("/siteAdmin"),
        LOGOUT("/login_process.jsp"),
        SITE_ADMIN("/siteAdmin");
        
        private final String path;
        
        Route(String path) {
            this.path = path;
        }
        
        public String path() {
            return path;
        }

        public String url() {
            return getUrl(path);
        }
        
        public String url(Params params) {
            return getUrl(path, params);
        }
        
        public String toString() {
            return path();
        }        
    }
    
    public enum Css {
        CUSTOM_FORM("/edit/forms/css/customForm.css"),
        JQUERY_UI("/js/jquery-ui/css/smoothness/jquery-ui-1.8.2.custom.css");        

        private final String path;
        
        Css(String path) {
            this.path = path;           
        }
        
        public String path() {
            return path;
        }
        
        public String toString() {
            return path;
        }
    }
    
    public enum JavaScript {
        CUSTOM_FORM_UTILS("/js/customFormUtils.js"),
        JQUERY("/js/jquery.js"),
        JQUERY_UI("/js/jquery-ui/js/jquery-ui-1.8.2.custom.min.js"),
        UTILS("/js/utils.js");
        
        private final String path;
        
        JavaScript(String path) {
            this.path = path;           
        }
        
        public String path() {
            return path;
        }
        
        public String toString() {
            return path;
        }
    }
    
    public UrlBuilder(Portal portal) {
        this.portal = portal;
    }
    
    public String getHomeUrl() {
        String rootBreadCrumbUrl = portal.getRootBreadCrumbURL();
        String path = StringUtils.isEmpty(rootBreadCrumbUrl) ? "" : rootBreadCrumbUrl;
        return getUrl(path);
    }
    
    public String getLogoutUrl() {
        return getPortalUrl(Route.LOGOUT.path(), new Params("loginSubmitMode", "Log Out"));
    }
    
    public Params getPortalParam() {
        return new Params("home", "" + portal.getPortalId());    
    }

    public String getPortalUrl(String path) {
        return addPortalParam ? getUrl(path, getPortalParam()) : getUrl(path);
    }
    
    public String getPortalUrl(String path, Params params) {
        if (addPortalParam) {
            params.putAll(getPortalParam());
        }
        return getUrl(path, params);
    }
    
    public String getPortalUrl(Route route) {
        return getPortalUrl(route.path());
    }
    
    public String getPortalUrl(Route route, Params params) {
        return getPortalUrl(route.path(), params);
    }
    
    public static class Params extends HashMap<String, String> { 
        private static final long serialVersionUID = 1L;
        
        public Params() { }
        
        public Params(String...strings) {
            this();
            int stringCount = strings.length;
            for (int i = 0; i < stringCount; i=i+2) {
                // Skip the last item if there's an odd number
                if (i == stringCount-1) { break; }
                this.put(strings[i], strings[i+1]);
            }
        }       
    }
    
    /********** Static utility methods **********/
    
    public static String getUrl(String path) {
    	
        if ( !path.isEmpty() && !path.startsWith("/") ) {
            path = "/" + path;
        }
        path = contextPath + path;
        return path.isEmpty() ? "/" : path;
    }
    
    public static String getUrl(String path, Params params) {
        path = getPath(path, params);
        return getUrl(path);       
    }
    
    public static String getPath(String path, Params params) {
        String glue = "?";
        for (String key : params.keySet()) {
            path += glue + key + "=" + urlEncode(params.get(key));
            glue = "&";
        }
        return path;       
    }

    public static String urlEncode(String url) {
        String encoding = "ISO-8859-1";
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(url, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding url " + url + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return encodedUrl;
    }

    public static String urlDecode(String url) {
        String encoding = "ISO-8859-1";
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(url, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error decoding url " + url + " with encoding " + encoding + ": Unsupported encoding.");
        }
        return decodedUrl;
    }

}
