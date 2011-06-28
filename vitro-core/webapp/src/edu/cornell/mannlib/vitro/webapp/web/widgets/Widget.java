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

package edu.cornell.mannlib.vitro.webapp.web.widgets;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.core.Environment;
import freemarker.core.Macro;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

public abstract class Widget {

    private static final Log log = LogFactory.getLog(Widget.class);
    
    /* Widget implementations don't get any state when they get constructed so that they 
     * can be reused. */
    public Widget() { }
    
    public String doAssets(Environment env, Map params) {
        String widgetName = params.get("name").toString(); //getWidgetName();
        Macro assetsMacro = getMacroFromTemplate(getAssetsMacroName(), widgetName, env);
        
        // Allow there to be no assets macro in the template
        if (assetsMacro == null) {
            return "";
        }
        TemplateHashModel dataModel = env.getDataModel();  
        Map<String, Object> map = new HashMap<String, Object>(); 
        
        try {
            map.put("stylesheets", dataModel.get("stylesheets"));
            map.put("scripts", dataModel.get("scripts"));
            map.put("headScripts", dataModel.get("headScripts"));
        } catch (TemplateModelException e) {
            log.error("Error getting asset values from data model.");
        }
        
        return processMacroToString(env, widgetName, assetsMacro, map); 
    }
    
    public String doMarkup(Environment env, Map params) {
        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        ServletContext context = (ServletContext) env.getCustomAttribute("context");
        
        WidgetTemplateValues values = null;
        
        try {
            values = process(env, params, request, context); 
        } catch (Exception e) {
            log.error(e, e);
        }
        
        // The widget process() method may determine that nothing should display for the widget:
        // for example, the login widget doesn't display if the user is already logged in. This also
        // applies if process() threw an error.
        if (values == null) {
            return "";
        }
        String widgetName = params.get("name").toString(); // getWidgetName();
        return processMacroToString(env, widgetName, values);
    }

    // Default  template name. Can be overridden by subclasses.
    protected String getTemplateName(String widgetName) {
        return "widget-" + widgetName + ".ftl";
    }
    
    // Default assets macro name. Can be overridden by subclasses.
    protected String getAssetsMacroName() {
        return "assets";
    }
    
    // Default markup macro name. Can be overridden by subclasses, or
    // subclass process() method can select from various markup macros
    // based on widget state. For example, the login widget markup macro will
    // differ depending on login processing state.
    protected String getMarkupMacroName() {
        return "markup";
    }
 
//    private String getWidgetName() {
//        String name = this.getClass().getName();
//        name= name.replaceAll(".*\\.", "");
//        name = name.replaceAll("Widget$", "");
//        name = name.substring(0, 1).toLowerCase() + name.substring(1);
//        return name;
//    }

    protected abstract WidgetTemplateValues process(Environment env, Map params, 
            HttpServletRequest request, ServletContext context) throws Exception;
    
    private String processMacroToString(Environment env, String widgetName, Macro macro, Map<String, Object> map) {   
        StringWriter out = new StringWriter();
        try {
            String templateString = macro.getChildNodes().get(0).toString();
            // NB Using this method of creating a template from a string does not allow the widget template to import
            // other templates (but it can include other templates). We'd need to use a StringTemplateLoader
            // in the config instead. See StringTemplateLoader API doc.
            // The problem is that the StringTemplateLoader has to be added to the config's MultiTemplateLoader.
            // Then to support multi-threading, we can't just add the widget here to the StringTemplateLoader with
            // the same key, e.g., "widgetTemplate", since one putTemplate() call will clobber a previous one.
            // We need to give each widget macro template a unique key in the StringTemplateLoader, and check 
            // if it's already there or else add it. Leave this for later.
            Template template = new Template("widget", new StringReader(templateString), env.getConfiguration());          
            template.process(map, out);
        } catch (Exception e) {
            log.error("Could not process widget " + widgetName, e);
        }
        String output = out.toString(); 
        log.debug("Macro output: " + output);
        return output;       
    }
    
    private String processMacroToString(Environment env, String widgetName, String macroName, Map<String, Object> map) {
        Macro macro = getMacroFromTemplate(macroName, widgetName, env);
        return processMacroToString(env, widgetName, macro, map);
    }
    
    private String processMacroToString(Environment env, String widgetName, WidgetTemplateValues values) {
        return processMacroToString(env, widgetName, values.getMacroName(), values.getMap());
    }
    
    private Macro getMacroFromTemplate(String macroName, String widgetName, Environment env) {
        String templateName = getTemplateName(widgetName);
        Template template = null;
        Macro macro = null;
        try {
            template = env.getConfiguration().getTemplate(templateName);
            macro = (Macro)template.getMacros().get(macroName);
        } catch (IOException e) {
            log.error("Cannot get template " + templateName);
        }  
        return macro;
    }
    
    protected static class WidgetTemplateValues {
        private final String macroName;
        private final Map<String, Object> map;

        public WidgetTemplateValues(String macroName) {
            this.macroName = macroName;
            this.map = new HashMap<String, Object>();            
        }
        
        public WidgetTemplateValues(String macroName, Map<String, Object> map) {
            this.macroName = macroName;
            this.map = map;
        }

        public WidgetTemplateValues put(String key, Object value) {
            this.map.put(key, value);
            return this;
        }

        public Map<String, Object> getMap() {
            return Collections.unmodifiableMap(this.map);
        }

        public String getMacroName() {
            return this.macroName;
        } 
    }
    
    protected class WidgetProcessingException extends Exception {
        WidgetProcessingException(String message) {
            super(message);
        }
    }

}
