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

package edu.cornell.mannlib.vitro.webapp.visualization.constants;

public class QueryFieldLabels {
	
	/*
	 * Generic Query related field labels 
	 * */
	public static final String PREDICATE = "predicateLit";
	public static final String OBJECT = "objectLit";
	
	
	/*
	 * Document related field labels 
	 * */
	public static final String DOCUMENT_URL = "documentLit";
	public static final String DOCUMENT_MONIKER = "documentMonikerLit";
	public static final String DOCUMENT_LABEL = "documentLabelLit";
	public static final String DOCUMENT_BLURB = "documentBlurbLit";
	public static final String DOCUMENT_DESCRIPTION = "documentDescriptionLit";
	public static final String DOCUMENT_PUBLICATION_YEAR = "publicationYearLit";
	public static final String DOCUMENT_PUBLICATION_YEAR_MONTH = "publicationYearMonthLit";
	public static final String DOCUMENT_PUBLICATION_DATE = "publicationDateLit";
	
	
	/*
	 * Image related field labels
	 * */
	public static final String THUMBNAIL_LOCATION_URL = "thumbnailDownloadLocationLit";
	public static final String THUMBNAIL_FILENAME = "thumbnailFileNameLit";
	
	/*
	 * Author related field labels
	 * */
	public static final String AUTHOR_URL = "authPersonLit";
	public static final String AUTHOR_LABEL = "authorLabelLit";
	
	/*
	 * Co-Author related field labels
	 * */
	public static final String CO_AUTHOR_URL = "coAuthPersonLit";
	public static final String CO_AUTHOR_LABEL = "coAuthPersonLabelLit";
	
	/*
	 * College related field labels 
	 * */
	public static final String COLLEGE_URL = "collegeLit";
	public static final String COLLEGE_LABEL = "collegeLabelLit";
	
	/*
	 * Department related field labels 
	 * */
	public static final String DEPARTMENT_URL = "departmentLit";
	public static final String DEPARTMENT_LABEL = "departmentLabelLit";
	
	/*
	 * Employee related field labels 
	 * */
	public static final String ACADEMIC_FACULTY_EMPLOYEE_URL = "academicFacultyEmployeeLit";
	public static final String ACADEMIC_STAFF_EMPLOYEE_URL = "academicStaffEmployeeLit";
	
}
