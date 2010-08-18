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

package edu.cornell.mannlib.vitro.webapp.web;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

/**
 * Class to find custom class views for individuals
 * @author rjy7
 *
 */
public class ViewFinder {
    
    private static final Log log = LogFactory.getLog(ViewFinder.class.getName());
    
    public enum ClassView { 
        DISPLAY("getCustomDisplayView", "/view-display"),
        // NB this is not the value currently used for custom forms - we use the value on the object property
        FORM("getCustomEntryForm", "/form"), 
        SEARCH("getCustomSearchView", "/view-search"),
        SHORT("getCustomShortView", "/view-short"); 
        
        private static String TEMPLATE_PATH = "/templates/freemarker";
        
        private Method method = null;
        private String path = null;
        
        ClassView(String methodName, String path) {
            Class<VClass> vc = VClass.class;
            this.path = path;
            try {
                method = vc.getMethod(methodName);
            } catch (SecurityException e) {
                log.error("Access denied to method " + methodName + " or class " + vc.getName());   
            } catch (NoSuchMethodException e) {
                log.error("Method " + methodName + " not defined for class " + vc.getName());
            }
        }
        
        protected Method getMethod() {
            return method;
        }
        
        protected String getPath() {
            return TEMPLATE_PATH + path;
        }

    }
    
    private ClassView view;
    
    public ViewFinder(ClassView view) {
        this.view = view;
    }
    
    public String findClassView(Individual individual, ServletContext context) {
        String viewName = "default.ftl"; 
        List<VClass> vclasses = individual.getVClasses();
        Method method = view.getMethod();
        /* RY The logic here is incorrect. The vclasses are
         * returned in a random order, whereas we need to
         * traverse the class hierarchy and find the most
         * specific custom view applicable to the individual.
         * The logic is complex because individuals can belong
         * to multiple classes, and classes can subclass multiple
         * classes. If there are two competing custom views at the 
         * same level of specificity, what should we do? Also, if we
         * are displaying a list of individuals belonging to a certain
         * class, we may want to use only a custom view defined for that 
         * class and NOT a more specific one. See NIHVIVO-568.
         */
        for (VClass vc : vclasses) {
            try {
                String v = (String) method.invoke(vc);
                if (!StringUtils.isEmpty(v)) {
                    String pathToView = context.getRealPath(view.getPath() + "-" + v);
                    File viewFile = new File(pathToView);
                    if (viewFile.isFile() && viewFile.canRead()) {
                        viewName = v;
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                log.error("Incorrect arguments passed to method " + method.getName() + " in findView().");
            } catch (IllegalAccessException e) {
                log.error("Method " + method.getName() + " cannot be accessed in findView().");
            } catch (InvocationTargetException e) {
                log.error("Exception thrown by method " + method.getName() + " in findView().");
            }

        }
        return viewName;
    }

}
