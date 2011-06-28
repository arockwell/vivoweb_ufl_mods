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

import com.hp.hpl.jena.rdf.model.Literal;

public class StartDateBeforeEndDate implements N3Validator {
	private String startFieldName;
	private String endFieldName;
	
	public StartDateBeforeEndDate(String startFieldName, String endFieldName){
		this.startFieldName = startFieldName;
		this.endFieldName = endFieldName;
	}
	public Map<String, String> validate(EditConfiguration editConfig,
			EditSubmission editSub) {
		Map<String, Literal> existingLiterals = editConfig.getLiteralsInScope();
		Literal existingStartDate = existingLiterals.get(startFieldName);
		Literal existingEndDate = existingLiterals.get(endFieldName);

		Map<String, Literal> literalsFromForm = editSub.getLiteralsFromForm();
		Literal formStartDate = literalsFromForm.get(startFieldName);
		Literal formEndDate = literalsFromForm.get(endFieldName);

		Map<String, String> errors = new HashMap<String, String>();

		if (formStartDate != null && formEndDate != null) {
			errors.putAll(checkDateLiterals(formStartDate, formEndDate));
		} else if (formStartDate != null && existingEndDate != null) {
			errors.putAll(checkDateLiterals(formStartDate, existingEndDate));
		} else if (existingStartDate != null && formEndDate != null) {
			errors.putAll(checkDateLiterals(existingStartDate, formEndDate));
		} else if (existingStartDate != null && existingEndDate != null) {
			errors
					.putAll(checkDateLiterals(existingStartDate,
					        existingEndDate));
		}

		if (errors.size() != 0)
			return errors;
		else
			return null;
	}

	private Map<String, String> checkDateLiterals(Literal startLit,
			Literal endLit) {
		Map<String, String> errors = new HashMap<String, String>();
        Calendar startDate = getDateFromLiteral(startLit);
        Calendar endDate = getDateFromLiteral(endLit);
		try {
		    if (startDate.compareTo(endDate) > 0) {
                errors.put(startFieldName, "Start date cannot follow end date");
                errors.put(endFieldName, "End date cannot precede start date");		        
		    }
		} catch (NullPointerException npe){ 
		    log.error("Cannot compare date to null.");
		    
		} catch (IllegalArgumentException iae) {
		    log.error("IllegalArgumentException");
		}
		return errors;
	}
	
	private Calendar getDateFromLiteral(Literal dateLit) {
	    
	    String[] date = dateLit.getLexicalForm().split("-");
	    int year = Integer.parseInt(date[0]);
	    int day = date.length < 3 ? 1 : Integer.parseInt(date[2]);
	    int month = date.length < 2 ? 0 : Integer.parseInt(date[1]);
	    Calendar c = Calendar.getInstance();
	    c.set(year, month, day);
	    return c;
	}

    private Log log = LogFactory.getLog(StartDateBeforeEndDate.class);
}
