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

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.utils.view.DateDisplayUtils;

public class DateDisplayUtilsTest {
    
    @Test
    public void testDisplayDate() {
        String date = "2009-10";
        Assert.assertEquals("10/2009", DateDisplayUtils.getDisplayDate(date));
    }
    
    @Test
    public void testGetDisplayDateRange() {
        String startRaw = "2010-10-11";
        String endRaw = "2010-11-09";
        Assert.assertEquals("10/11/2010 - 11/09/2010", DateDisplayUtils.getDisplayDateRangeFromRawDates(startRaw, endRaw));
        
        String start1 = "1/2/2010";
        String end1 = "3/4/2011";
        Assert.assertEquals("1/2/2010 - 3/4/2011", DateDisplayUtils.getDisplayDateRange(start1, end1));
        
        String empty = "";
        Assert.assertEquals("1/2/2010 - ", DateDisplayUtils.getDisplayDateRange(start1, empty));
        Assert.assertEquals(" - 3/4/2011", DateDisplayUtils.getDisplayDateRange(empty, end1));
        
        Assert.assertEquals("1/2/2010 - ", DateDisplayUtils.getDisplayDateRange(start1, (String)null));
        Assert.assertEquals(" - 3/4/2011", DateDisplayUtils.getDisplayDateRange((String)null, end1));       
    }
    



}
