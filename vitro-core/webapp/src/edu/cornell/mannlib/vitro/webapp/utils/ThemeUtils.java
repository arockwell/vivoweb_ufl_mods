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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletContext;

public class ThemeUtils {
    
    private ThemeUtils() {
        throw new AssertionError();
    }
	
	public static ArrayList<String> getThemes(ServletContext sc, boolean doSort) {

		// Find the themes directory on the file system
        String themesDirName = sc.getRealPath("/themes");          
        File themesDir = new File(themesDirName);
        
        // Get the children of the themes directory and their names
        File[] children = themesDir.listFiles();
        String[] childNames = themesDir.list();
        
        // Create a list of valid themes
        ArrayList<String> themeNames = new ArrayList<String>(childNames.length);
        for (int i = 0; i < children.length; i++) {
        	// Get only directories, not files
        	if (children[i].isDirectory()) {	
        		themeNames.add(childNames[i]);
        	}
        }
        
        // File.list() does not guarantee a specific order, so sort alphabetically
        if (doSort == true) {
        	Collections.sort(themeNames);
        }
        
        return themeNames;
        
	}

}
