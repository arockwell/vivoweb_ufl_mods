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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class PropertyStatementTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyStatementTemplateModel.class); 

    private static enum EditAccess {
        EDIT, DELETE;
    }
    
    // Used for editing
    protected String subjectUri = null;
    protected String propertyUri = null;
    private List<EditAccess> editAccessList = null;
    
    PropertyStatementTemplateModel(String subjectUri, String propertyUri, EditingPolicyHelper policyHelper) {
        
        if (policyHelper != null) { // we're editing
            this.subjectUri = subjectUri;
            this.propertyUri = propertyUri;  
            editAccessList = new ArrayList<EditAccess>(); 
        }
    }
    
    protected void markEditable() {
        editAccessList.add(EditAccess.EDIT);
    }
    
    protected void markDeletable() {
        editAccessList.add(EditAccess.DELETE);
    }
    
    protected boolean isEditable() {
        return editAccessList.contains(EditAccess.EDIT);
    }
    
    protected boolean isDeletable() {
        return editAccessList.contains(EditAccess.DELETE);
    }
}
