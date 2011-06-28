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

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Write the merged data to an HTML page.
 */
public class OutputMarshaller {
	private final List<TestResultsFileData> reportData;
	private final PrintWriter w;
	private final List<String> testNames;

	public OutputMarshaller(List<TestResultsFileData> reportData, PrintWriter w) {
		this.reportData = reportData;
		this.w = w;
		this.testNames = assembleListOfTestNames();
	}

	public void marshall() {
		writePageHeader();
		writeTestDataTable();
		writePageFooter();
	}

	private List<String> assembleListOfTestNames() {
		Set<String> names = new TreeSet<String>();
		for (TestResultsFileData filedata : reportData) {
			names.addAll(filedata.getTestMap().keySet());
		}
		return new ArrayList<String>(names);
	}

	private void writePageHeader() {
		w.println("<html>");
		w.println("<head>");
		w.println("  <link REL='STYLESHEET' TYPE='text/css' HREF='./mergedResults.css'>");
		w.println("</head>");
		w.println("<body>");
	}

	private void writeTestDataTable() {
		w.println("<table class='testData' cellspacing='0'>");
		writeTestDataHeader();
		for (String testName : testNames) {
			writeTestDataRow(testName);
		}
		w.println("</table>");
	}

	private void writeTestDataHeader() {
		w.println("  <tr>");
		w.println("    <th>&nbsp;</th>");
		for (TestResultsFileData fileData : reportData) {
			w.println("    <th colspan='3'>" + fileData.getVivoVersion()
					+ "<br/>" + fileData.getResultsFilename() + "<br/>"
					+ formatDate(fileData.getCreated()) + "</th>");
		}
		w.println("  </tr>");

		w.println("  <tr>");
		w.println("    <th>Test Name</th>");
		for (TestResultsFileData fileData : reportData) {
			w.println("    <th>iterations</th>");
			w.println("    <th>time (min/max)</th>");
			w.println("    <th>ratio</th>");
		}
		w.println("  </tr>");
	}

	private void writeTestDataRow(String testName) {
		w.println("  <tr>");
		w.println("    <td class='left'>" + testName + "</td>");
		for (TestResultsFileData fileData : reportData) {
			writeTestDataCellForFile(fileData, testName);
		}
		w.println("  </tr>");
	}

	private void writeTestDataCellForFile(TestResultsFileData fileData,
			String testName) {
		TestResultInfo testData = fileData.getTestMap().get(testName);
		TestResultInfo baselineTestData = reportData.get(0).getTestMap()
				.get(testName);

		String count = (testData == null) ? "&nbsp;" : ("" + testData
				.getCount());
		String averageTime = (testData == null) ? "&nbsp;"
				: ("" + formatTime(testData.getAverageTime()));
		String minTime = (testData == null) ? "&nbsp;"
				: ("" + formatTime(testData.getMinTime()));
		String maxTime = (testData == null) ? "&nbsp;"
				: ("" + formatTime(testData.getMaxTime()));

		String ratioWithBaseline = "&nbsp";
		if ((testData != null) && (baselineTestData != null)) {
			ratioWithBaseline = percentage(testData.getAverageTime(),
					baselineTestData.getAverageTime());
		}

		w.println("    <td class='open'>" + count + "</td>");
		w.println("    <td>");
		w.println("      <table class='oneResult middle' cellspacing=0>");
		w.println("        <tr>");
		w.println("          <td rowspan='2'>" + averageTime + "</td>");
		w.println("          <td class='minmax'>" + minTime + "</td>");
		w.println("        </tr>");
		w.println("        <tr>");
		w.println("          <td class='minmax'>" + maxTime + "</td>");
		w.println("        </tr>");
		w.println("      </table>");
		w.println("    <td class='close'>" + ratioWithBaseline + "</td>");
		w.println("    </td>");
	}

	private String percentage(float value, float baseline) {
		float ratio = value / baseline;
		return String.format("%1$8.2f%%", ratio * 100.0);
	}

	public String formatTime(float time) {
		return String.format("%1$8.3f", time);
	}

	public String formatDate(long date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date(date));
	}

	private void writePageFooter() {
		w.println("</body>");
		w.println("</html>");
	}

}
