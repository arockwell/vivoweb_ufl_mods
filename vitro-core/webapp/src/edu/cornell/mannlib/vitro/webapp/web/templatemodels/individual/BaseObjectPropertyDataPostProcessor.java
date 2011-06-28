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
package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public abstract class BaseObjectPropertyDataPostProcessor implements
        ObjectPropertyDataPostProcessor {

    private static final Log log = LogFactory.getLog(BaseObjectPropertyDataPostProcessor.class); 
    
    protected ObjectPropertyTemplateModel objectPropertyTemplateModel;
    protected WebappDaoFactory wdf;
    
    public BaseObjectPropertyDataPostProcessor(ObjectPropertyTemplateModel optm, WebappDaoFactory wdf) {
        this.objectPropertyTemplateModel = optm;
        this.wdf = wdf;
    }
        
    @Override
    public void process(List<Map<String, String>> data) {
        
        if (data.isEmpty()) {
            log.debug("No data to postprocess for property " + objectPropertyTemplateModel.getUri());
            return;
        }
        
        processList(data);

        for (Map<String, String> map : data) {
            process(map);           
        }
    }
    
    /** Postprocessing that applies to the list as a whole - reordering, removing duplicates, etc. */
    protected void processList(List<Map<String, String>> data) {
        objectPropertyTemplateModel.removeDuplicates(data);
    }
    
    /** Postprocessing that applies to individual list items */
    protected abstract void process(Map<String, String> map);
    

    /* Postprocessor methods callable from any postprocessor */

    protected void addName(Map<String, String> map, String nameKey, String objectKey) {
        String name = map.get(nameKey);
        if (name == null) {
            // getIndividual() could return null
            Individual ind = getIndividual(map.get(objectKey));
            if (ind != null) {
                map.put(nameKey, ind.getName());
            }
        }
    }
    
    /* This is a temporary measure to handle the fact that the current Individual.getMoniker()
     * method returns the individual's VClass if moniker is null. We want to replicate that
     * behavior here, but in future the moniker property (along with other Vitro namespace
     * properties) will be removed. In addition, this type of logic (display x if it exists, otherwise y)
     * will be moved into the display modules (Editing and Display Configuration Improvements).
     */
// rjy7 Now Individual.getMoniker() returns only the moniker, not the VClass, so no reason to call the method
// if the sparql query returns a null moniker.
//    protected void addMoniker(Map<String, String> map, String monikerKey, String objectKey) {
//        String moniker = map.get(monikerKey);
//        if (moniker == null) {
//            Individual ind = getIndividual(map.get(objectKey));
//            if (ind != null) {
//                map.put(monikerKey, ind.getMoniker());
//            }
//        }
//    }
    
    protected Individual getIndividual(String uri) {
        return wdf.getIndividualDao().getIndividualByURI(uri);
    }

}
