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

package edu.cornell.mannlib.vitro.webapp.utils.view;

import java.util.Arrays;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class DateDisplayUtils {

    public static String getDisplayDate(String date) {
        String displayDate = null;
        if (date == null) {
            return displayDate;
        }
        List<String> dateParts = Arrays.asList(date.split("-"));
        int datePartCount = dateParts.size();
        switch (datePartCount) {
        case 2: 
            displayDate = StringUtils.join("/", dateParts.get(1), dateParts.get(0));
            break;
        case 3:
            displayDate = StringUtils.join("/", dateParts.get(1), dateParts.get(2), dateParts.get(0));
            break;
        default: 
            displayDate = date;
        }
        
        return displayDate;     
    }
    
    public static String getDisplayDateRange(String startDate, String endDate) {
        startDate = StringUtils.setNullToEmptyString(startDate);
        endDate = StringUtils.setNullToEmptyString(endDate);
        List<String> dates = Arrays.asList(startDate, endDate);
        return StringUtils.join(dates, " - ");
    }
    
    public static String getDisplayDateRangeFromRawDates(String startDate, String endDate) {
        return getDisplayDateRange(getDisplayDate(startDate), getDisplayDate(endDate));
    }
    

}
