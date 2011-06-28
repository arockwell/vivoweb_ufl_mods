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

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;

public class UrlBuilderTest extends AbstractTestClass {
    
    @Test
    public void testGetUrl() {
        UrlBuilder.contextPath = "/vivo";
        
        String path1 = "/individual";
        Assert.assertEquals("/vivo/individual", UrlBuilder.getUrl(path1));
        
        int portalId = 1;
        String path2 = "/individual?home=" + portalId;
        Assert.assertEquals("/vivo/individual?home=1", UrlBuilder.getUrl(path2));
    }
    
    @Test
    public void testGetUrlWithEmptyContext() {
        UrlBuilder.contextPath = "";
        String path = "/individual";
        Assert.assertEquals(path, UrlBuilder.getUrl(path));
    }
    
    @Test
    public void testGetUrlWithParams() {
        UrlBuilder.contextPath = "/vivo";
        String path = "/individual";
        ParamMap params = new ParamMap();
        int portalId = 1;
        params.put("home", "" + portalId);
        params.put("name", "Tom");
        Assert.assertEquals("/vivo/individual?home=1&name=Tom", UrlBuilder.getUrl(path, params));
    }

    @Test
    public void testEncodeUrl() {
        UrlBuilder.contextPath = "/vivo";
        String path = "/individuallist";
        ParamMap params = new ParamMap();
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember";
        params.put("vclassId", vClassUri);
        Assert.assertEquals("/vivo/individuallist?vclassId=http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember", UrlBuilder.getUrl(path, params));    
    }
    
    @Test
    public void testDecodeUrl() {
        String vClassUri = "http://vivoweb.org/ontology/core#FacultyMember";
        String vClassUriEncoded = "http%3A%2F%2Fvivoweb.org%2Fontology%2Fcore%23FacultyMember";
        Assert.assertEquals(vClassUri, UrlBuilder.urlDecode(vClassUriEncoded));          
    }
    
}
