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

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A list of tests to be ignored - they are expected to fail, and their failure
 * is logged with a warning, not an error.
 */
public class IgnoredTests {
	private final File file;
	private final List<IgnoredTestInfo> tests;

	/**
	 * <p>
	 * Parse the file of ignored tests.
	 * </p>
	 * <p>
	 * Ignore any blank line, or any line starting with '#' or '!'
	 * </p>
	 * <p>
	 * Each other line describes an ignored test. The line contains the suite
	 * name, a comma (with optional space), the test name (with optional space)
	 * and optionally a comment, starting with a '#'.
	 * </p>
	 */
	public IgnoredTests(File file) {
		this.file = file;
		List<IgnoredTestInfo> tests = new ArrayList<IgnoredTestInfo>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while (null != (line = reader.readLine())) {
				line = line.trim();
				if ((line.length() == 0) || (line.charAt(0) == '#')
						|| (line.charAt(0) == '!')) {
					continue;
				}
				Pattern p = Pattern.compile("([^,#]+),([^,#]+)(#(.*))?");
				Matcher m = p.matcher(line);
				if (m.matches()) {
					tests.add(new IgnoredTestInfo(m.group(1), m.group(2), m
							.group(4)));
				} else {
					throw new FatalException(
							"Bad format on ignored test description: '" + line
									+ "', should be "
									+ "<suite name>, <test name> [# comment]");
				}
			}
		} catch (IOException e) {
			throw new FatalException(
					"Failed to parse the list of ignored tests: '"
							+ file.getPath() + "'", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.tests = Collections.unmodifiableList(tests);
	}

	/**
	 * Is this test ignored or not?
	 */
	public boolean isIgnored(String suiteName, String testName) {
		for (IgnoredTestInfo test : tests) {
			if (test.matches(suiteName, testName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If this test is ignored, what is the reason? If not, return an empty
	 * string.
	 */
	public String getReasonForIgnoring(String suiteName, String testName) {
		for (IgnoredTestInfo test : tests) {
			if (test.matches(suiteName, testName)) {
				return test.comment;
			}
		}
		return "";
	}

	public String toString() {
		String s = "  ignored tests from " + file.getPath() + "\n";
		for (IgnoredTestInfo test : tests) {
			s += "      " + test.suiteName + ", " + test.testName + "\n";
		}
		return s;
	}

	private static class IgnoredTestInfo {
		final String suiteName;
		final String testName;
		final String comment;

		public IgnoredTestInfo(String suiteName, String testName, String comment) {
			this.suiteName = suiteName.trim();
			this.testName = testName.trim();
			this.comment = (comment == null) ? "" : comment.trim();
		}

		public boolean matches(String suiteName, String testName) {
			return this.suiteName.equals(suiteName)
					&& this.testName.equals(testName);
		}

	}

}
