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

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.N3Validator;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class PersonHasPublicationValidator implements N3Validator {

    private static String MISSING_PUB_TYPE_ERROR = "Must specify a publication type.";
    private static String MISSING_PUB_TITLE_ERROR = "Must specify a publication title."; 
    
    @Override
    public Map<String, String> validate(EditConfiguration editConfig,
            EditSubmission editSub) {

        Map<String,String> urisFromForm = editSub.getUrisFromForm();
        Map<String,Literal> literalsFromForm = editSub.getLiteralsFromForm();

        Map<String,String> errors = new HashMap<String,String>();   
        
        // If there's a pubUri, then we're done. The other fields are disabled and so don't get submitted.
        String pubUri = urisFromForm.get("pubUri");
        if (!StringUtils.isEmpty(pubUri)) {
            return null;
        }
        
        String pubType = urisFromForm.get("pubType");
        if ("".equals(pubType)) {
            pubType = null;
        }
        
        Literal title = literalsFromForm.get("title");
        if (title != null) {
            String titleValue = title.getLexicalForm();
            if (StringUtils.isEmpty(titleValue)) {
                title = null;
            }
        }
        
        if (pubType == null) {
            errors.put("pubType", MISSING_PUB_TYPE_ERROR);
        }
        if (title == null) {
            errors.put("title", MISSING_PUB_TITLE_ERROR);
        }
        
        return errors.size() != 0 ? errors : null;
    }

}
