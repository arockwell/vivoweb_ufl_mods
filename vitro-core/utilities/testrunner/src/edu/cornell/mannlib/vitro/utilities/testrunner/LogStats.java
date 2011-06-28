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

package edu.cornell.mannlib.vitro.utilities.testrunner;

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

/**
 * Extract any summary information from the log file.
 */
public class LogStats {
	public static LogStats EMPTY_LOG_STATS = new LogStats();

	private static final Pattern SUITE_NAME_PATTERN = Pattern
			.compile("Running suite (.*)");
	private static final Pattern ERROR_PATTERN = Pattern
			.compile("ERROR\\s+(.*)");
	private static final Pattern WARNING_PATTERN = Pattern
			.compile("WARN\\s+(.*)");

	/**
	 * Factory method.
	 */
	public static LogStats parse(File logFile) {
		return new LogStats(logFile);
	}

	private final List<String> suiteNames = new ArrayList<String>();
	private final List<String> errors = new ArrayList<String>();
	private final List<String> warnings = new ArrayList<String>();

	private LogStats() {
		// Nothing to initialize for empty instance.
	}

	private LogStats(File logFile) {

		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(new FileReader(logFile));
			while (null != (line = reader.readLine())) {
				Matcher m;
				m = SUITE_NAME_PATTERN.matcher(line);
				if (m.matches()) {
					suiteNames.add(m.group(1));
				} else {
					m = ERROR_PATTERN.matcher(line);
					if (m.matches()) {
						errors.add(m.group(1));
					} else {
						m = WARNING_PATTERN.matcher(line);
						if (m.matches()) {
							warnings.add(m.group(1));
						}
					}
				}
			}

		} catch (IOException e) {
			// Can't give up - I need to create as much output as I can.
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public Collection<String> getErrors() {
		return Collections.unmodifiableCollection(errors);
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public Collection<String> getWarnings() {
		return Collections.unmodifiableCollection(warnings);
	}

}
