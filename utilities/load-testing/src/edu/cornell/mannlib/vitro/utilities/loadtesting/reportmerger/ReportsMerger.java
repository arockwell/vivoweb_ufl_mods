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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Take two or more reports from JMeter's test results, and merge them into a
 * unified HTML report.
 */
public class ReportsMerger {

	/**
	 * Start with list of filenames in command line For each contributing file,
	 * heading is from parsing the filename. Get the one after LoadTesting, and
	 * the last one, minus the extension.
	 * 
	 * For each file, build a structure with header info and a LinkedMap of the
	 * desired info, testname -> info structure Build a list of these. Build a
	 * unified list of testnames.
	 * 
	 * List<TestResultsFile>
	 * 
	 * TestResultsFile: String version; String filename; Date timestamp;
	 * LinkedMap<String, TestResultInfo> testMap;
	 * 
	 * TestResultInfo: boolean success; int count; float averageTime; float
	 * maxTime; float minTime;
	 */

	private final ReportsMergerParameters parms;
	private List<TestResultsFileData> reportData;

	public ReportsMerger(ReportsMergerParameters parms) {
		this.parms = parms;
	}

	private void parseReports() {
		List<TestResultsFileData> reportData = new ArrayList<TestResultsFileData>();
		for (File reportFile : parms.getReportFiles()) {
			TestResultsFileData fileData = new TestResultsParser(reportFile)
					.parse();
			System.out.println("File data: " + fileData);
			reportData.add(fileData);
		}
		this.reportData = reportData;
	}

	private void produceOutput() {
		PrintWriter writer = parms.getOutputWriter();
		new OutputMarshaller2(reportData, writer).marshall();
		writer.flush();
		writer.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReportsMergerParameters parms = ReportsMergerParameters
				.getInstance(args);
		ReportsMerger rm = new ReportsMerger(parms);
		rm.parseReports();
		rm.produceOutput();
	}

}
