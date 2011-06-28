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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.Precision;
import edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision;

public class DateTimeIntervalValidation implements N3Validator {
    private static Log log = LogFactory.getLog(DateTimeIntervalValidation.class);
    
    private String startFieldName;
    private String endFieldName;

    private String startValueName;
    private String endValueName;

    private String startPrecisionName;
    private String endPrecisionName;
    
    public DateTimeIntervalValidation(String startFieldName, String endFieldName){
        this.startFieldName = startFieldName;
        this.endFieldName = endFieldName;
        startValueName = startFieldName + ".value";
        endValueName = endFieldName + ".value";
        startPrecisionName = startFieldName + ".precision";
        endPrecisionName = endFieldName + ".precision";
    }
    
    public Map<String, String> validate(EditConfiguration editConfig,
            EditSubmission editSub) {
        Map<String, Literal> existingLiterals = editConfig.getLiteralsInScope();
        Literal existingStartYear = existingLiterals.get(startValueName);
        Literal existingEndYear = existingLiterals.get(endValueName);

        Map<String, Literal> literalsFromForm = editSub.getLiteralsFromForm();
        Literal formStartYear = literalsFromForm.get(startValueName);
        Literal formEndYear = literalsFromForm.get(endValueName);

        VitroVocabulary.Precision startPrecision = getPrecision(startPrecisionName, editConfig, editSub);
        VitroVocabulary.Precision endPrecision = getPrecision(endPrecisionName, editConfig, editSub);
        
        Map<String, String> errors = new HashMap<String, String>();

        // NIHVIVO-2541 Commented out to allow end date with no start date
//      if( formStartYear == null && formEndYear != null ){                               
//          errors.put(startFieldName, "If there is an end date, there should be a start date");
//          return errors;              
//      }
        
        if (formStartYear != null && formEndYear != null) {
            errors.putAll(checkDateLiterals(formStartYear, formEndYear, startPrecision, endPrecision));
        } else if (formStartYear != null && existingEndYear != null) {
            errors.putAll(checkDateLiterals(formStartYear, existingEndYear, startPrecision, endPrecision));
        } else if (existingStartYear != null && formEndYear != null) {
            errors.putAll(checkDateLiterals(existingStartYear, formEndYear, startPrecision, endPrecision));
        } else if (existingStartYear != null && existingEndYear != null) {
            errors.putAll(checkDateLiterals(existingStartYear, existingEndYear, startPrecision, endPrecision));
        }

        if (errors.size() != 0)
            return errors;
        else
            return null;
    }

    private Precision getPrecision(String precisionVarName,
            EditConfiguration editConfig, EditSubmission editSub) {
        if( editSub != null 
                && editSub.getUrisFromForm() != null 
                && editSub.getUrisFromForm().containsKey(precisionVarName)){            
            String precisionStr = editSub.getUrisFromForm().get(precisionVarName);
            VitroVocabulary.Precision precision = DateTimeWithPrecision.toPrecision( precisionStr );
            if( precision == null )
                log.warn("cannot convert " + precisionStr + " to a precision");
            else
                return precision;
        }else if( editConfig != null 
                && editConfig.getUrisInScope() != null 
                && editConfig.getUrisInScope().containsKey(precisionVarName)){
            String precisionStr = editConfig.getUrisInScope().get(precisionVarName);
            VitroVocabulary.Precision precision = DateTimeWithPrecision.toPrecision( precisionStr );
            if( precision == null )
                log.warn("cannot convert " + precisionStr + " to a precision");
            else
                return precision;        
        }
        //this is what is returned if a precision was not found in the config or submission
        return null;
    }

    private Map<String, String> checkDateLiterals(
            Literal startLit, Literal endLit, 
            VitroVocabulary.Precision startPrecision, VitroVocabulary.Precision endPrecision) {                
        Map<String, String> errors = new HashMap<String, String>();        
        
        if( endPrecision == null ){
            //there is no end date, nothing to check
            return errors;
        }             
        
        try{
             XSDDateTime startDate = (XSDDateTime)startLit.getValue();
             XSDDateTime endDate = (XSDDateTime)endLit.getValue();
             if( startDate != null && endDate!= null ){
                 Calendar startCal = startDate.asCalendar();
                 Calendar endCal = endDate.asCalendar();
                                  
                 if( endCal != null ){
                     if( !startCal.before( endCal ) ){
                         if( startPrecision == VitroVocabulary.Precision.YEAR 
                             && endPrecision == VitroVocabulary.Precision.YEAR ){
                             errors.putAll( checkYears(startCal,endCal));
                         }else{
                             errors.put(startFieldName, "Start must be before end");
                             errors.put(endFieldName, "End must be after start");
                         }
                     }
                 }
             }
        }catch(ClassCastException cce){
            errors.put(startFieldName, "could not format star or end date");
            errors.put(endFieldName, "could not format star or end date");
            log.debug("could not format dates " + cce);
        }
            
        return errors;
    }

    private Map<? extends String, ? extends String> checkYears(
            Calendar startCal, Calendar endCal) {
        
        Map<String, String> errors = new HashMap<String, String>();    
     
        if( ! (endCal.get(Calendar.YEAR) >=  startCal.get(Calendar.YEAR) )){
            errors.put(startFieldName, "Start must be before end");
            errors.put(endFieldName, "End must be after start");
        }
        
        return errors;
    }
}
