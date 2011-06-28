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

package edu.cornell.mannlib.vitro.webapp.edit.elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class BaseEditElement  implements EditElement {
    private static final Log log = LogFactory.getLog(BaseEditElement.class);

    protected Field field;
    
    public BaseEditElement(Field field){
        this.field = field;
    }
    
    /**
     * Utility method for use in EditElements to merge a freemarker template.
     */
    protected String merge(Configuration fmConfig, String templateName, Map map){
        Template template = null;
        try {
            template = fmConfig.getTemplate(templateName);
        } catch (IOException e) {
            log.error("Cannot get template " + templateName);
        }  
         
        StringWriter writer = new StringWriter();
        try {
            template.process(map, writer);
        } catch (TemplateException e) {
            log.error(e,e);
        } catch (IOException e) {
            log.error(e,e);
        }
        return writer.toString();        
    }

    /**
     * Utility method to check if a value from the query parameters is none or a single value.
     * This returns true if the key is there and the value is null.
     * This does not check if the value is the empty string.
     */
    protected boolean hasNoneOrSingle(String key, Map<String, String[]> queryParameters){
        if( queryParameters != null ){
            if( ! queryParameters.containsKey(key) )
                return true; //none            
            String[] vt = queryParameters.get(key);
            return vt == null || vt.length == 0 || vt.length==1;
        }else{
            log.error("passed null queryParameters");
            return false;
        }
    }
    
    protected boolean hasSingleNonNullNonEmptyValueForKey(String key, Map<String, String[]> queryParameters){
        if( queryParameters != null ){
            if( ! queryParameters.containsKey(key) )
                return true; //none            
            String[] vt = queryParameters.get(key);
            return vt != null && vt.length == 1 && vt[0] != null && ! vt[0].isEmpty() ;
        }else{
            log.error("passed null queryParameters");
            return false;
        }
    }
}
