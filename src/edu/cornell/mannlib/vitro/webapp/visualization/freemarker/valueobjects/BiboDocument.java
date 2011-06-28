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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;

/**
 * @author cdtank
 *
 */
public class BiboDocument extends Individual {

	private String publicationYear;
	private String publicationDate;
	private String parsedPublicationYear = VOConstants.DEFAULT_PUBLICATION_YEAR;

	public BiboDocument(String documentURL) {
		super(documentURL);
	}
	
	public String getDocumentURL() {
		return this.getIndividualURI();
	}	
	
	private String parsePublicationYear(String documentBlurb) {

		/*
		 * This pattern will match all group of numbers which have only 4 digits
		 * delimited by the word boundary.
		 * */
		String pattern = "(?<!-)\\b\\d{4}\\b(?=[^-])";
		
		Pattern yearPattern = Pattern.compile(pattern);
		String publishedYear = VOConstants.DEFAULT_PUBLICATION_YEAR;

		Matcher yearMatcher = yearPattern.matcher(documentBlurb);

		while (yearMatcher.find()) {

            String yearCandidate = yearMatcher.group();

            Integer candidateYearInteger = Integer.valueOf(yearCandidate);

            /*
             * Published year has to be equal or less than the current year
             * and more than a minimum default year.
             * */
			if (candidateYearInteger <= VOConstants.CURRENT_YEAR
					&& candidateYearInteger >= VOConstants.MINIMUM_PUBLICATION_YEAR) {
            	publishedYear = candidateYearInteger.toString();
            }

		}

		return publishedYear;
	}

	/**
	 * This method will be called to get the final/inferred year for the publication. 
	 * The 3 choices, in order, are,
	 * 		1. parsed year from xs:DateTime object saved in core:dateTimeValue 
	 * 		2. core:year which was property used in vivo 1.1 ontology
	 * 		3. Default Publication Year 
	 * @return
	 */
	public String getParsedPublicationYear() {
		
		if (publicationDate != null) { 
			
			DateTime validParsedDateTimeObject = UtilityFunctions.getValidParsedDateTimeObject(publicationDate);
			
			if (validParsedDateTimeObject != null) {
				return String.valueOf(validParsedDateTimeObject.getYear());
			} else {
				return publicationYear != null ? publicationYear : VOConstants.DEFAULT_PUBLICATION_YEAR;
			}
			
		} else {

			/*
			 * If all else fails return default unknown year identifier if publicationYear is
			 * not mentioned.
			 * */
			return publicationYear != null ? publicationYear : VOConstants.DEFAULT_PUBLICATION_YEAR;
		} 
	}

	/*
	 * This publicationYear value is directly from the data supported by the ontology. 
	 * If this is empty only then use the parsedPublicationYear.
	 * 
	 * @Deprecated Use getParsedPublicationYear() instead.
	 * */
	@Deprecated
	public String getPublicationYear() {
		if (publicationYear != null && isValidPublicationYear(publicationYear)) {
			return publicationYear;
		} else {
			return null;
		}
		
	}

	public void setPublicationYear(String publicationYear) {
		this.publicationYear = publicationYear;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}
	
	private boolean isValidPublicationYear(String testPublicationYear) {
		
		if (testPublicationYear.length() != 0 
				&& testPublicationYear.trim().length() == VOConstants.NUM_CHARS_IN_YEAR_FORMAT
				&& testPublicationYear.matches("\\d+")
				&& Integer.parseInt(testPublicationYear) >= VOConstants.MINIMUM_PUBLICATION_YEAR) {
			return true;
		}
		
		return false;
	}

}
