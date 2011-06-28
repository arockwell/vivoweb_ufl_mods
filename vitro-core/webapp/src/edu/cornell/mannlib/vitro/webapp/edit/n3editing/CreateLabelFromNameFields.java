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

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.XSD;

public class CreateLabelFromNameFields extends BaseEditSubmissionPreprocessor {

    private static final Log log = LogFactory.getLog(CreateLabelFromNameFields.class.getName());
    
    public CreateLabelFromNameFields(EditConfiguration editConfig) {
        super(editConfig);
    }

    // Create label by concatenating first name, middle name, and last name fields as
    // "<last name>, <first name> <middle name>". First name and last name are required;
    // middle name is optional. 
    // rjy7 Using all hard-coded field names for now. If we want to control these, pass in
    // a map of field names when creating the preprocessor object.
    public void preprocess(EditSubmission editSubmission) {
        Map<String, Literal> literalsFromForm = editSubmission.getLiteralsFromForm();
        try {
            // Create the label string
            
            // Assuming last name and first name fields will be on the form
            String lastName = literalsFromForm.get("lastName").getLexicalForm();
            String firstName = literalsFromForm.get("firstName").getLexicalForm();
           
            // The form may or may not have a middle name field
            String middleName = "";
            Literal middleNameLiteral = literalsFromForm.get("middleName");
            if (middleNameLiteral != null) {
                middleName = middleNameLiteral.getLexicalForm();
            }
            
            String label = lastName + ", " + firstName;
            if (!StringUtils.isEmpty(middleName)) {
                label += " " + middleName;
            }

            // Add the label to the form literals
            Field labelField = editConfiguration.getField("label");
            String rangeDatatypeUri = labelField.getRangeDatatypeUri();
            if (StringUtils.isEmpty(rangeDatatypeUri)) {
                rangeDatatypeUri = XSD.xstring.toString();
            }
            String rangeLang = labelField.getRangeLang();
            // RY Had to change createLiteral method to protected - check w/Brian
            Literal labelLiteral = editSubmission.createLiteral(label, rangeDatatypeUri, rangeLang);
            literalsFromForm.put("label", labelLiteral);
            editSubmission.setLiteralsFromForm(literalsFromForm);

        } catch (Exception e) {
            log.error("Error retrieving name values from edit submission.");
        }
        
    }
}
