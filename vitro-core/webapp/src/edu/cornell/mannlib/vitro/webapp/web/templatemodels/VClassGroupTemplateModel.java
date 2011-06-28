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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;

public class VClassGroupTemplateModel extends BaseTemplateModel {

	private static final Log log = LogFactory.getLog(VClassGroupTemplateModel.class.getName());
	
    private VClassGroup vClassGroup = null;
    private List<VClassTemplateModel> classes = null;
    
    public VClassGroupTemplateModel(VClassGroup vClassGroup) {
        this.vClassGroup = vClassGroup;
    }

    public int getDisplayRank() {
        return vClassGroup.getDisplayRank();
    }
    
    public String getUri() {
    	return vClassGroup.getURI();
    }
    
    public String getNamespace() {
    	return vClassGroup.getNamespace();
    }
    
    public String getLocalName() {
    	return vClassGroup.getLocalName();
    }

    public String getPublicName() {
    	return vClassGroup.getPublicName();
    }
    
    // Protect the template against a group without a name.
    public String getDisplayName() {
        String displayName = getPublicName();
        if (StringUtils.isBlank(displayName)) {
            displayName = getLocalName().replaceFirst("vitroClassGroup", "");
        }
        return displayName;
    }
    
    public List<VClassTemplateModel> getClasses() {
        if (classes == null) {
            List<VClass> classList = vClassGroup.getVitroClassList();
            classes = new ArrayList<VClassTemplateModel>();
            for (VClass vc : classList) {
                classes.add(new VClassTemplateModel(vc));
            }
        }
        
        return classes;
    }
    
    public int getIndividualCount(){
        if( vClassGroup.isIndividualCountSet() )
            return vClassGroup.getIndividualCount();
        else
            return 0;
    }
    
    public boolean isIndividualCountSet(){
        return vClassGroup.isIndividualCountSet();
    }    
    
}
