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

package edu.cornell.mannlib.vitro.utilities.testrunner.output;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.FileHelper;
import edu.cornell.mannlib.vitro.utilities.testrunner.LogStats;
import edu.cornell.mannlib.vitro.utilities.testrunner.SeleniumRunnerParameters;
import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.DataModel;

/**
 * Manages the contents of the output area. Removes old files prior to a run.
 * Creates a unified summary of the test suite outputs.
 */
public class OutputManager {
	private final SeleniumRunnerParameters parms;
	private final OutputDataListener dataListener;

	public OutputManager(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.dataListener = new OutputDataListener();
		parms.addListener(this.dataListener);
	}

	/**
	 * Delete any output files from previous runs.
	 */
	public void cleanOutputDirectory() throws IOException {
		File outputDirectory = parms.getOutputDirectory();
		parms.getListener().cleanOutputStart(outputDirectory);

		try {
			for (File file : outputDirectory.listFiles()) {
				// Skip the log file, since we are already over-writing it.
				if (file.equals(parms.getLogFile())) {
					continue;
				}
				// Skip any hidden files (like .svn)
				if (file.getPath().startsWith(".")) {
					continue;
				}
				// Delete all of the others.
				if (file.isFile()) {
					FileHelper.deleteFile(file);
				} else {
					FileHelper.purgeDirectoryRecursively(file);
				}
			}
		} catch (IOException e) {
			parms.getListener().cleanOutputFailed(outputDirectory, e);
			throw e;
		} finally {
			parms.getListener().cleanOutputStop(outputDirectory);
		}
	}

	/**
	 * Parse each of the output files from the test suites, and create a unified
	 * output file.
	 */
	public void summarizeOutput(DataModel dataModel) {
		try {
			LogStats log = LogStats.parse(parms.getLogFile());

			List<SuiteResults> suiteResults = new ArrayList<SuiteResults>();
			for (File outputFile : parms.getOutputDirectory().listFiles(
					new HtmlFileFilter())) {
				SuiteResults suite = SuiteResults.parse(parms, outputFile);
				if (suite != null) {
					suiteResults.add(suite);
				}
			}

			dataModel.setSuiteResults(suiteResults);
			dataModel.captureDataListener(dataListener);

			OutputSummaryFormatter formatter = new OutputSummaryFormatter(parms);
			formatter.format(log, dataModel);
		} catch (Exception e) {
			// It must be impossible to throw an exception from here, so just
			// print it to sysout.
			e.printStackTrace();
		}
	}

	private static class HtmlFileFilter implements FileFilter {
		public boolean accept(File path) {
			return path.getName().endsWith(".html")
					|| path.getName().endsWith(".htm");
		}

	}
}
