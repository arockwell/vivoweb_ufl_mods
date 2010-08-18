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

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.StringTokenizer;

/**
 *
 *
following text from http://search.cpan.org/dist/Text-Capitalize/Capitalize.pm

This web page:
  http://www.continentallocating.com/World.Literature/General2/LiteraryTitles2.htm

presents some admirably clear rules for capitalizing titles:
  ALL words in EVERY title are capitalized except
  (1) a, an, and the,
  (2) two and three letter conjunctions (and, or, nor, for, but, so, yet),
  (3) prepositions.
  Exceptions:  The first and last words are always capitalized even
  if they are among the above three groups.

But consider the case:
  "It Waits Underneath the Sea"

Should the word "underneath" be downcased because it's a preposition? Most English speakers
would be surprised to see it that way. Consequently, the default list of exceptions to
capitalization in this module only includes the shortest of the common prepositions
(to of by at for but in).

The default entries on the exception list are:
     a an the
     and or nor for but so yet
     to of by at for but in with has
     de von
The observant may note that the last row is not composed of English words. The honorary
"de" has been included in honor of "Honoré de Balzac". And "von" was added for the sake
of equal time.
 */
public class TitleCase {
    static String ignore[] = {"a","an","the","and","or","nor","for","but","so","yet",
            "to","of","by","at","for","but","in","with","has","de","von"};

    public static String toTitleCase(String in){
        if( in == null && in.length() ==0 )
            return in;

        in = in.toLowerCase();
        StringTokenizer st = new StringTokenizer(in);
        StringBuilder out = new StringBuilder();

        int count = 1;
        int last = st.countTokens();

        while(st.hasMoreTokens()){
            String token = st.nextToken();

            //always capatize first and last
            if( count == 1 || count == last ){
                out.append(capitalizeWord(token));
            } else {

                //check if on ignored list
                boolean ignoreToken = false;
                for(String ign:ignore){
                    if( token.equals(ign) )
                        ignoreToken = true;
                }
                if( ignoreToken )
                    out.append(token);
                else
                    out.append(capitalizeWord(token));
            }

            if(st.hasMoreTokens())
                out.append(' ');
            count++;
        }
        return out.toString();
    }

    private static String capitalizeWord(String in){
        if( in == null && in.length() == 0 )
            return in;
        if( in.length() == 1 )
            return in.toUpperCase();

        //not trying too hard to deal with dashes.
        int dash = in.indexOf('-') ;
        if(dash > 0 && in.length() > dash+2 )
            in = in.substring(0, dash)
                + '-'
                + in.substring(dash+1,dash+2).toUpperCase()
                + in.substring(dash+2);

        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }
}
