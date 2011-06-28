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

package edu.cornell.mannlib.vitro.utilities.loadtesting.reportmerger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A base implementation of ReportsMergerParameters
 */
public class ReportsMergerParametersImpl extends ReportsMergerParameters {

	private final String outputDirectoryPath;
	private final String inputDirectoryPath;
	private final List<String> inputFilenames;

	/**
	 * The first arg is the output directory. The second arg is an input
	 * directory. The third arg is a comma-separated list of input filenames.
	 */
	public ReportsMergerParametersImpl(String[] args) {
		this.outputDirectoryPath = args[0];
		this.inputDirectoryPath = args[1];
		this.inputFilenames = Arrays.asList(args[2].split("[, ]+"));
	}

	@Override
	public List<File> getReportFiles() {
		List<File> files = new ArrayList<File>();
		for (String filename : inputFilenames) {
			files.add(new File(inputDirectoryPath, filename));
		}
		// files.add(new File(
		// "/Development/JIRA issues/NIHVIVO-1129_Load_testing/mergerFiles/LoadTesting/release1.1.1/SecondTests-rel-1-1-1.html"));
		// files.add(new File(
		// "/Development/JIRA issues/NIHVIVO-1129_Load_testing/mergerFiles/LoadTesting/trunkNoSdb/SecondTests-rel-1-2.html"));
		// files.add(new File(
		// "/Development/JIRA issues/NIHVIVO-1129_Load_testing/mergerFiles/LoadTesting/trunkSdb/SecondTests-rel-1-2.html"));
		return files;
	}

	@Override
	public PrintWriter getOutputWriter() {
		try {
			File outputFile = new File(outputDirectoryPath,
					"mergedResults.html");
			return new PrintWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			throw new RuntimeException("Can't open the output writer.", e);
		}
	}

}
