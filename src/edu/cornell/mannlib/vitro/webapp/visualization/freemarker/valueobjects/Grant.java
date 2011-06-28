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
package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects;

import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;

/**
 * @author bkoniden
 * Deepak Konidena
 *
 */

public class Grant extends Individual {
	
	private String grantStartDate;
	private String grantEndDate;
	
	public Grant(String grantURL, String grantLabel){
		super(grantURL, grantLabel);
	}
	
	public Grant(String grantURL){
		super(grantURL);
	}
	
	public String getGrantURL() {
		return this.getIndividualURI();
	}
	
	public String getGrantLabel(){
		return this.getIndividualLabel();
	}
	
	public void setGrantLabel(String grantLabel) {
		this.setIndividualLabel(grantLabel);
	}
	

	/**
	 * This method will be called to get the inferred start year for the grant. 
	 * The 3 choices, in order, are,
	 * 		1. parsed year from xs:DateTime object saved in core:dateTimeValue 
	 * 		2. core:year which was property used in vivo 1.1 ontology
	 * 		3. Default Grant Start Year 
	 * @return
	 */
	public String getParsedGrantStartYear() {

		if (grantStartDate != null) {

			DateTime validParsedDateTimeObject = UtilityFunctions
					.getValidParsedDateTimeObject(grantStartDate);

			if (validParsedDateTimeObject != null) {
				return String.valueOf(validParsedDateTimeObject.getYear());
			} else {
				return VOConstants.DEFAULT_GRANT_YEAR;
			}
		} else {
			return VOConstants.DEFAULT_GRANT_YEAR;
		}

	}

	@Override
	public boolean equals(Object other){
		boolean result = false;
		if (other instanceof Grant){
			Grant grant = (Grant) other;
			result = (this.getIndividualLabel().equals(grant.getIndividualLabel())
						&& this.getIndividualURI().equals(grant.getIndividualURI()));
		}
		return result;
	}
	
	@Override 
	public int hashCode(){
		return(41*(getIndividualLabel().hashCode() + 41*(getIndividualURI().hashCode())));
	}	

	public String getGrantStartDate() {
		return grantStartDate;
	}

	public void setGrantStartDate(String grantStartDate) {
		this.grantStartDate = grantStartDate;
	}


	public String getGrantEndDate() {
		return grantEndDate;
	}

	public void setGrantEndDate(String grantEndDate) {
		this.grantEndDate = grantEndDate;
	}

	private boolean isValidPublicationYear(String testGrantYear) {
		
		if (testGrantYear.length() != 0 
				&& testGrantYear.trim().length() == VOConstants.NUM_CHARS_IN_YEAR_FORMAT
				&& testGrantYear.matches("\\d+")
				&& Integer.parseInt(testGrantYear) >= VOConstants.MINIMUM_PUBLICATION_YEAR) {
			return true;
		}
		
		return false;
	}

//	/**
//	 * This method will be called when there is no usable core:year value found
//	 * for the core:Grant. It will first check & parse core:yearMonth failing
//	 * which it will try core:date
//	 * @return
//	 */
//	public String getParsedGrantStartYear() {
//		
//		/*
//		 * We are assuming that core:yearMonth has "YYYY-MM-DD" format. This is based 
//		 * off of http://www.w3.org/TR/xmlschema-2/#gYearMonth , which is what
//		 * core:yearMonth points to internally.
//		 * */
//		if (grantStartYearMonth != null 
//				&& grantStartYearMonth.length() >= VOConstants.NUM_CHARS_IN_YEAR_FORMAT
//				&& isValidPublicationYear(grantStartYearMonth.substring(
//													0,
//													VOConstants.NUM_CHARS_IN_YEAR_FORMAT))) {
//			
//			return grantStartYearMonth.substring(0, VOConstants.NUM_CHARS_IN_YEAR_FORMAT); 
//			
//		}
//		
//		if (grantStartDate != null 
//				&& grantStartDate.length() >= VOConstants.NUM_CHARS_IN_YEAR_FORMAT
//				&& isValidPublicationYear(grantStartDate
//												.substring(0,
//														   VOConstants.NUM_CHARS_IN_YEAR_FORMAT))) {
//			
//			return grantStartDate.substring(0, VOConstants.NUM_CHARS_IN_YEAR_FORMAT); 
//		}
//		
//		/*
//		 * If all else fails return default unknown year identifier
//		 * */
//		return VOConstants.DEFAULT_GRANT_YEAR;
//	}	
	
}
