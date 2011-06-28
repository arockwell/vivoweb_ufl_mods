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

package edu.cornell.mannlib.vitro.utilities.testrunner.datamodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.utilities.testrunner.FileHelper;

/**
 * Parses the actual HTML test suite file, and holds the results.
 */
public class SuiteContents {
	/**
	 * If the file doesn't contain a line that includes this pattern, it is not
	 * a suite contents file.
	 */
	private static final Pattern TITLE_LINE_PATTERN = Pattern
			.compile("<title>Test Suite</title>");
	private static final Pattern TEST_PATTERN = Pattern
			.compile("<tr><td><a\\s+href=[^>]*>([^<]+)</a></td></tr>(?m)");

	private static final String SUITE_FILE_NAME = "Suite.html";

	/**
	 * Parse the test names fields from this file and attempt to produce a
	 * {@link SuiteContents} object. If this is not an appropriate file, just
	 * return null.
	 */
	public static SuiteContents parse(File suiteDirectory) {
		StringBuilder fileContents = new StringBuilder();
		BufferedReader reader = null;
		try {
			File suiteFile = new File(suiteDirectory, SUITE_FILE_NAME);
			if (!suiteFile.exists()) {
				return null;
			}

			reader = new BufferedReader(new FileReader(suiteFile));
			String line;
			while (null != (line = reader.readLine())) {
				fileContents.append(line).append('\n');
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// If it doesn't contain the title line, it's not a suite file.
		if (!TITLE_LINE_PATTERN.matcher(fileContents).find()) {
			return null;
		}

		// Accumulate all of the test names.
		List<String> testNames = new ArrayList<String>();
		Matcher m = TEST_PATTERN.matcher(fileContents);
		int lookHere = 0;
		while (m.find(lookHere)) {
			testNames.add(m.group(1));
			lookHere = m.end();
		}

		return new SuiteContents(FileHelper.baseName(suiteDirectory), testNames);
	}

	private final String name;
	private final Collection<String> testNames;

	public SuiteContents(String name, Collection<String> testNames) {
		this.name = name;
		this.testNames = Collections
				.unmodifiableCollection(new ArrayList<String>(testNames));
	}

	public String getName() {
		return name;
	}

	public Collection<String> getTestNames() {
		return testNames;
	}

}
