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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.SuiteStats.TestInfo;

/**
 * Creates the summary HTML file.
 */
public class OutputSummaryFormatter {
	public static final String SUMMARY_HTML_FILENAME = "summary.html";
	public static final String SUMMARY_CSS_FILENAME = "summary.css";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final SeleniumRunnerParameters parms;

	private LogStats log;
	private List<SuiteStats> suites;
	private Status runStatus;
	private List<TestInfo> allTests = new ArrayList<TestInfo>();
	private int passingTestCount;
	private List<TestInfo> failingTests = new ArrayList<TestInfo>();
	private List<TestInfo> ignoredTests = new ArrayList<TestInfo>();

	public OutputSummaryFormatter(SeleniumRunnerParameters parms) {
		this.parms = parms;
	}

	/**
	 * Create a summary HTML file from the info contained in this log file and
	 * these suite outputs.
	 */
	public void format(LogStats log, List<SuiteStats> suites) {
		this.log = log;
		this.suites = suites;
		this.runStatus = figureOverallStatus(log, suites);
		tallyTests();

		PrintWriter writer = null;
		try {
			copyCssFile();

			File outputFile = new File(parms.getOutputDirectory(),
					SUMMARY_HTML_FILENAME);
			writer = new PrintWriter(outputFile);

			writeHeader(writer);
			writeStatsSection(writer);
			writeErrorMessagesSection(writer);
			writeFailureSection(writer);
			writeIgnoreSection(writer);
			writeSuitesSection(writer);
			writeAllTestsSection(writer);
			writeFooter(writer);
		} catch (IOException e) {
			// There is no appeal for any problems here. Just report them.
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Copy the CSS file into the output directory.
	 */
	private void copyCssFile() {
		File cssSource = parms.getSummaryCssFile();
		File cssTarget = new File(parms.getOutputDirectory(),
				SUMMARY_CSS_FILENAME);
		try {
			FileHelper.copy(cssSource, cssTarget);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The overall status for the run is the worst status of any component.
	 */
	public Status figureOverallStatus(LogStats log, List<SuiteStats> suites) {
		if (log.hasErrors()) {
			return Status.ERROR;
		}
		boolean hasWarnings = log.hasWarnings();

		for (SuiteStats s : suites) {
			if (s.getStatus() == Status.ERROR) {
				return Status.ERROR;
			} else if (s.getStatus() == Status.WARN) {
				hasWarnings = true;
			}
		}

		if (hasWarnings) {
			return Status.WARN;
		} else {
			return Status.OK;
		}
	}

	private void tallyTests() {
		for (SuiteStats s : suites) {
			for (TestInfo t : s.getTests()) {
				this.allTests.add(t);
				if (t.getStatus() == Status.OK) {
					this.passingTestCount++;
				} else if (t.getStatus() == Status.WARN) {
					this.ignoredTests.add(t);
				} else {
					this.failingTests.add(t);
				}
			}
		}
	}

	private void writeHeader(PrintWriter writer) {
		String startString = formatDateTime(log.getStartTime());

		writer.println("<html>");
		writer.println("<head>");
		writer.println("  <title>Summary of Acceptance Tests " + startString
				+ "</title>");
		writer.println("  <link rel=\"stylesheet\" type=\"text/css\" "
				+ "href=\"summary.css\">");
		writer.println("</head>");
		writer.println("<body>");
		writer.println();
		writer.println("  <div class=\"heading\">");
		writer.println("    Acceptance test results: " + startString);
		writer.println("    <div class=\"" + this.runStatus.getHtmlClass()
				+ " one-word\">" + this.runStatus + "</div>");
		writer.println("  </div>");
	}

	private void writeStatsSection(PrintWriter writer) {
		String passClass = Status.OK.getHtmlClass();
		String failClass = this.failingTests.isEmpty() ? "" : Status.ERROR
				.getHtmlClass();
		String ignoreClass = this.ignoredTests.isEmpty() ? "" : Status.WARN
				.getHtmlClass();

		String start = formatDateTime(log.getStartTime());
		String end = formatDateTime(log.getEndTime());
		String elapsed = formatElapsedTime(log.getElapsedTime());

		writer.println("  <div class=\"section\">Summary</div>");
		writer.println();
		writer.println("  <table class=\"summary\" cellspacing=\"0\">");
		writer.println("    <tr>");
		writer.println("      <td>");
		writer.println("        <table cellspacing=\"0\">");
		writer.println("  	      <tr><td>Start time:</td><td>" + start
				+ "</td></tr>");
		writer.println("  	      <tr><td>End time:</td><td>" + end
				+ "</td></tr>");
		writer.println("  	      <tr><td>Elapsed time</td><td>" + elapsed
				+ "</td></tr>");
		writer.println("  	    </table>");
		writer.println("      </td>");
		writer.println("      <td>");
		writer.println("        <table cellspacing=\"0\">");
		writer.println("  	      <tr><td>Suites</td><td>" + this.suites.size()
				+ "</td></tr>");
		writer.println("  	      <tr><td>Total tests</td><td>"
				+ this.allTests.size() + "</td></tr>");
		writer.println("  	      <tr class=\"" + passClass
				+ "\"><td>Passing tests</td><td>" + this.passingTestCount
				+ "</td></tr>");
		writer.println("  	      <tr class=\"" + failClass
				+ "\"><td>Failing tests</td><td>" + this.failingTests.size()
				+ "</td></tr>");
		writer.println("  	      <tr class=\"" + ignoreClass
				+ "\"><td>Ignored tests</td><td>" + this.ignoredTests.size()
				+ "</td></tr>");
		writer.println("  	    </table>");
		writer.println("      </td>");
		writer.println("    </tr>");
		writer.println("  </table>");
		writer.println();
	}

	private void writeErrorMessagesSection(PrintWriter writer) {
		String errorClass = Status.ERROR.getHtmlClass();
		String warnClass = Status.WARN.getHtmlClass();

		writer.println("  <div class=section>Errors and warnings</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");

		if ((!log.hasErrors()) && (!log.hasWarnings())) {
			writer
					.println("      <tr><td colspan=\"2\">No errors or warnings</td></tr>");
		} else {
			for (String e : log.getErrors()) {
				writer.println("      <tr class=\"" + errorClass
						+ "\"><td>ERROR</td><td>" + e + "</td></tr>");
			}
			for (String w : log.getWarnings()) {
				writer.println("      <tr class=\"" + warnClass
						+ "\"><td>ERROR</td><td>" + w + "</td></tr>");
			}
		}
		writer.println("    </table>");
		writer.println();
	}

	private void writeFailureSection(PrintWriter writer) {
		String errorClass = Status.ERROR.getHtmlClass();

		writer.println("  <div class=section>Failing tests</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");
		writer.println("    <tr><th>Suite name</th><th>Test name</th></tr>\n");
		if (failingTests.isEmpty()) {
			writer.println("    <tr><td colspan=\"2\">No tests failed.</td>"
					+ "</tr>");
		} else {
			for (TestInfo t : failingTests) {
				writer.println("    <tr class=\"" + errorClass + "\">");
				writer.println("      <td>" + t.getSuiteName() + "</td>");
				writer.println("      <td><a href=\"" + t.getOutputLink()
						+ "\">" + t.getTestName() + "</a></td>");
				writer.println("    </tr>");
			}
		}
		writer.println("  </table>");
		writer.println();
	}

	private void writeIgnoreSection(PrintWriter writer) {
		String warnClass = Status.WARN.getHtmlClass();

		writer.println("  <div class=section>Ignored tests</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");
		writer.println("    <tr><th>Suite name</th><th>Test name</th>"
				+ "<th>Reason for ignoring</th></tr>\n");
		if (ignoredTests.isEmpty()) {
			writer.println("    <tr><td colspan=\"3\">No tests ignored.</td>"
					+ "</tr>");
		} else {
			for (TestInfo t : ignoredTests) {
				writer.println("    <tr class=\"" + warnClass + "\">");
				writer.println("      <td>" + t.getSuiteName() + "</td>");
				writer.println("      <td><a href=\"" + t.getOutputLink()
						+ "\">" + t.getTestName() + "</a></td>");
				writer.println("      <td>" + t.getReasonForIgnoring()
						+ "</td>");
				writer.println("    </tr>");
			}
		}
		writer.println("  </table>");
		writer.println();
	}

	private void writeSuitesSection(PrintWriter writer) {
		writer.println("  <div class=section>Suites</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");

		for (SuiteStats s : suites) {
			writer.println("    <tr class=\"" + s.getStatus().getHtmlClass()
					+ "\">");
			writer.println("      <td><a href=\"" + s.getOutputLink() + "\">"
					+ s.getName() + "</a></td>");
			writer.println("    </tr>");
		}

		writer.println("  </table>");
		writer.println();
	}

	private void writeAllTestsSection(PrintWriter writer) {
		writer.println("  <div class=section>All tests</div>");
		writer.println();
		writer.println("  <table cellspacing=\"0\">");

		writer.println("    <tr><th>Suite name</th><th>Test name</th></tr>\n");
		for (TestInfo t : allTests) {
			writer.println("    <tr class=\"" + t.getStatus().getHtmlClass()
					+ "\">");
			writer.println("      <td>" + t.getSuiteName() + "</td>");
			writer.println("      <td><a href=\"" + t.getOutputLink() + "\">"
					+ t.getTestName() + "</a></td>");
			writer.println("    </tr>");
		}

		writer.println("  </table>");
		writer.println();
	}

	private void writeFooter(PrintWriter writer) {
		writer.println("  <div class=section>Log</div>");
		writer.println("  <pre>");

		Reader reader = null;
		try {
			reader = new FileReader(parms.getLogFile());
			char[] buffer = new char[4096];
			int howMany;
			while (-1 != (howMany = reader.read(buffer))) {
				writer.write(buffer, 0, howMany);
			}
		} catch (IOException e) {
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

		writer.println("  </pre>");
		writer.println("</body>");
		writer.println("</html>");
	}

	private String formatElapsedTime(long elapsed) {
		long elapsedSeconds = elapsed / 1000L;
		long seconds = elapsedSeconds % 60L;
		long elapsedMinutes = elapsedSeconds / 60L;
		long minutes = elapsedMinutes % 60L;
		long hours = elapsedMinutes / 60L;

		String elapsedTime = "";
		if (hours > 0) {
			elapsedTime += hours + "h ";
		}
		if (minutes > 0 || hours > 0) {
			elapsedTime += minutes + "m ";
		}
		elapsedTime += seconds + "s";

		return elapsedTime;
	}

	private String formatDateTime(long dateTime) {
		return dateFormat.format(new Date(dateTime));
	}

}
