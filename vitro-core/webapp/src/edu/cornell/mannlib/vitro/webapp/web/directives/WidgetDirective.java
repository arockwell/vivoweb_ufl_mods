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

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.widgets.Widget;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class WidgetDirective extends BaseTemplateDirectiveModel {

    private static final Log log = LogFactory.getLog(WidgetDirective.class);
    private static final String WIDGET_PACKAGE = "edu.cornell.mannlib.vitro.webapp.web.widgets";
    
    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                "The dump directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException(
                "The dump directive doesn't allow nested content.");
        }
        
        Object nameParam = params.get("name");
        if ( !(nameParam instanceof SimpleScalar)) {
            throw new TemplateModelException(
               "Value of parameter 'name' must be a string.");     
        }
        String widgetName = nameParam.toString();

        // Optional param
        Object includeParam = params.get("include");
        String methodName;
        // If include param is missing, or something other than "assets", 
        // assign default value "markup"
        if (includeParam == null) {
            methodName = "markup";
        } else {
            methodName = includeParam.toString();
            if ( ! ("assets".equals(methodName)) ) {
                methodName = "markup";
            }
        }
        methodName = "do" + StringUtils.capitalize(methodName);
        
        try {       
            String widgetClassName = WIDGET_PACKAGE + "." + StringUtils.capitalize(widgetName) + "Widget";
            Class<?> widgetClass = Class.forName(widgetClassName); 
            Widget widget = (Widget) widgetClass.newInstance();             
            Method method = widgetClass.getMethod(methodName, Environment.class, Map.class);
            
            // Right now it seems to me that we will always be producing a string for the widget calls. If we need greater
            // flexibility, we can return a ResponseValues object and deal with different types here.
            String output = (String) method.invoke(widget, env, params);

            // If we're in the body template, automatically invoke the doAssets() method, so it
            // doesn't need to be called explicitly from the enclosing template.
            String templateType = env.getDataModel().get("templateType").toString();
            if ("doMarkup".equals(methodName) && FreemarkerHttpServlet.BODY_TEMPLATE_TYPE.equals(templateType)) {
                output += widgetClass.getMethod("doAssets", Environment.class, Map.class).invoke(widget, env, params);
            }
              
            Writer out = env.getOut();
            out.write(output);
            
        } catch  (ClassNotFoundException e) {
            log.error("Widget " + widgetName + " not found.");
        } catch (IOException e) {
                log.error("Error writing output for widget " + widgetName, e);  
        } catch (Exception e) {
            log.error("Error invoking widget " + widgetName, e);
        }
        
    }

    public String help(Environment env) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        String name = getDirectiveName();
        map.put("name", name);
        
        map.put("effect", "Add a reuseable block of markup and functionality to the template, with associated scripts and stylesheets injected into the page &lt;head&gt; element.");
        
        map.put("comments", "From a body template, insert widget directive in desired location with no include value or include=\"markup\". Both assets and markup will be included. " +
                            "From a page template, insert widget directive at top of template with include=\"assets\". Insert widget directive in desired location " +
                            "with no include value or include=\"markup\".");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "name of widget");
        params.put("include", "values: \"assets\" to include scripts and stylesheets associated with the widget; \"markup\" to include the markup. " +
                              "\"markup\" is default value, so does not need to be specified.");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " name=\"login\" /> (use in body and page templates where markup should be inserted)");
        examples.add("<@" + name + " name=\"login\" include=\"markup\" /> (same as example 1)");
        examples.add("<@" + name + " name=\"login\" include=\"assets\" /> (use at top of page template to get scripts and stylesheets inserted into the &lt;head&gt; element)");
        
        map.put("examples", examples);
        
        return mergeToHelpTemplate(map, env);
    }

}
