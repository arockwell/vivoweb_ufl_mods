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

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
    
    private StringUtils() {
        throw new AssertionError();
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    public static String quote(String s) {
        return isEmpty(s) ? "" : '"' + s + '"';
    }
    
    public static String join(List<?> list, boolean quote, String glue) {
        
        if (glue == null) {
            glue = ",";
        }
        
        String joinedList = "";      
        
        int count = 0;
        for (Object o : list) {
            if (o == null) {
                continue;
            }
            String s = o.toString();
            if (count > 0) {
                joinedList += glue;
            }
            count++;
            joinedList += quote ? quote(s) : s;
        }
        
        return joinedList;
    }
    
    public static String join(List<?> list) {
        return join(list, false, ",");
    }
    
    public static String join(List<?> list, String glue) {
        return join(list, false, glue);
    }
    
    public static String join(String glue, String ...strings) {
        List<String> list =  new ArrayList<String>();
        for (String s : strings) {
            list.add(s);
        }
        return join(list, false, glue);        
    }
    
    public static String join(String[] stringArray, boolean quote, String glue) {
        return join(Arrays.asList(stringArray), quote, glue);
    }
    
    public static String join(String[] stringArray) {
        return join(Arrays.asList(stringArray));
    }
    
    public static String join(String[] stringArray, String glue) {
        return join(Arrays.asList(stringArray), false, glue);
    }
    
    public static String quotedList(List<?> list, String glue) {    
        return join(list, true, glue);
    } 
    
    public static String quotedList(String[] stringArray, String glue) {
        return quotedList(Arrays.asList(stringArray), glue);
    }
    
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    public static boolean equalsOneOf(String s, String... strings) {
        
        for (String item : strings) {
            if (item.equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    public static String setNullToEmptyString(String s) {
        return s == null ? "" : s;
    }
    
}
