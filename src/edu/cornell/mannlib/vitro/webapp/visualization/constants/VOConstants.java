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

package edu.cornell.mannlib.vitro.webapp.visualization.constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This contains the constants related to all the value objects.
 * @author cdtank
 */
public class VOConstants {
	
	public static final String DEFAULT_PUBLICATION_YEAR = "Unknown";
	public static final String DEFAULT_GRANT_YEAR = "Unknown";
	
	/*
	 * Employee related constants 
	 * */
	public static enum EmployeeType {
		ACADEMIC_FACULTY_EMPLOYEE, ACADEMIC_STAFF_EMPLOYEE
	} 
	
	public static final int NUM_CHARS_IN_YEAR_FORMAT = 4;
	public static final int MINIMUM_PUBLICATION_YEAR = 1800;
	public static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	
	@SuppressWarnings("serial")
	public static final List<DateTimeFormatter> POSSIBLE_DATE_TIME_FORMATTERS = new ArrayList<DateTimeFormatter>() {{
		
		add(ISODateTimeFormat.dateTimeNoMillis());
		add(ISODateTimeFormat.dateHourMinuteSecond());
		add(ISODateTimeFormat.dateTimeParser());
		
	}};
	

	
}
