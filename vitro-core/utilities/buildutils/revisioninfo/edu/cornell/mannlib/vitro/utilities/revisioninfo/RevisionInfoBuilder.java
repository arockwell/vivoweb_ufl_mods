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

package edu.cornell.mannlib.vitro.utilities.revisioninfo;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.revisioninfo.ProcessRunner.ProcessException;

/**
 * Get release and revision information to display on screen. Put this
 * information into a single line and append it to the specified file.
 * 
 * Ask Subversion for the information. If Subversion is available, and if this
 * is a working directory, then we can build the info from the responses we get
 * from "svn info" and "svnversion".
 * 
 * If that doesn't work, read the information from the ".revisionInfo" file in
 * the product directory. Presumably, that file was created when the source was
 * exported from Subversion.
 * 
 * If that doesn't work either, return something like this:
 * "productName ~ unknown ~ unknown"
 */
public class RevisionInfoBuilder {

	/**
	 * Indicates a problem with the command-line arguments.
	 */
	private static class UsageException extends Exception {
		UsageException(String message) {
			super(message);
		}
	}

	/**
	 * An object that holds the revision information and a message about how we
	 * obtained it.
	 */
	private static class Results {
		final String message;
		final String infoLine;

		Results(String message, String infoLine) {
			this.message = message;
			this.infoLine = infoLine;
		}

		public String toString() {
			return message + ": " + infoLine;
		}
	}

	private static final String SVN_DIRECTORY_NAME = ".svn";
	private static final String[] SVNVERSION_COMMAND = { "svnversion", "." };
	private static final String[] SVN_INFO_COMMAND = { "svn", "info" };
	private static final String INFO_LINE_DELIMITER = " ~ ";
	private static final String REVISION_INFO_FILENAME = ".revisionInfo";

	private final String productName;
	private final File productDirectory;
	private final File resultFile;

	private Results results;

	public RevisionInfoBuilder(String[] args) throws UsageException {
		if (args.length != 3) {
			throw new UsageException(
					"RevisionInfoBuilder requires 3 arguments, not "
							+ args.length);
		}

		productName = args[0];
		productDirectory = new File(args[1]);
		resultFile = new File(args[2]);

		if (!productDirectory.isDirectory()) {
			throw new UsageException("Directory '"
					+ productDirectory.getAbsolutePath() + "' does not exist.");
		}

		if (!resultFile.getParentFile().exists()) {
			throw new UsageException("Result file '"
					+ resultFile.getAbsolutePath()
					+ "' does not exist, and we can't create it "
					+ "because it's parent directory doesn't exist either.");
		}
	}

	private void buildInfo() {
		results = buildInfoFromSubversion();
		if (results == null) {
			results = buildInfoFromFile();
		}
		if (results == null) {
			results = buildDummyInfo();
		}
	}

	private Results buildInfoFromSubversion() {
		if (!isThisASubversionWorkspace()) {
			System.out.println("Not a Subversion workspace");
			return null;
		}

		String release = assembleReleaseNameFromSubversion();
		if (release == null) {
			System.out.println("Couldn't get release name from Subversion");
			return null;
		}

		String revision = obtainRevisionLevelFromSubversion();
		if (revision == null) {
			System.out.println("Couldn't get revision level from Subversion");
			return null;
		}

		return new Results("Info from Subversion", buildLine(release, revision));
	}

	private boolean isThisASubversionWorkspace() {
		File svnDirectory = new File(productDirectory, SVN_DIRECTORY_NAME);
		return svnDirectory.isDirectory();
	}

	private String assembleReleaseNameFromSubversion() {
		String infoResponse = runSubProcess(SVN_INFO_COMMAND);
		return new InfoResponseParser(infoResponse).parse();
	}

	private String obtainRevisionLevelFromSubversion() {
		String response = runSubProcess(SVNVERSION_COMMAND);
		return (response == null) ? null : response.trim();
	}

	private String runSubProcess(String[] cmdArray) {
		List<String> command = Arrays.asList(cmdArray);
		try {
			ProcessRunner runner = new ProcessRunner();
			runner.setWorkingDirectory(productDirectory);

			runner.run(command);

			int rc = runner.getReturnCode();
			if (rc != 0) {
				throw new ProcessRunner.ProcessException("Return code from "
						+ command + " was " + rc);
			}

			String output = runner.getStdOut();
			// System.err.println(command + " response was '" + output + "'");
			return output;
		} catch (ProcessException e) {
			return null;
		}
	}

	private Results buildInfoFromFile() {
		try {
			File revisionInfoFile = new File(productDirectory,
					REVISION_INFO_FILENAME);
			BufferedReader reader = new BufferedReader(new FileReader(
					revisionInfoFile));

			String release = reader.readLine();
			if (release == null) {
				throw new EOFException("No release line in file.");
			}

			String revision = reader.readLine();
			if (revision == null) {
				throw new EOFException("No revision line in file.");
			}

			return new Results("Info from file", buildLine(release, revision));
		} catch (IOException e) {
			System.out.println("No information from file: " + e);
			return null;
		}
	}

	private Results buildDummyInfo() {
		String line = buildLine(null, null);
		return new Results("Using dummy info", line);
	}

	private String buildLine(String release, String revision) {
		if (release == null) {
			release = "unknown";
		}
		if (revision == null) {
			revision = "unknown";
		}
		return productName + INFO_LINE_DELIMITER + release.trim()
				+ INFO_LINE_DELIMITER + revision.trim();
	}

	private void writeLine() throws IOException {
		Writer writer = null;
		writer = new FileWriter(resultFile, true);
		writer.write(results.infoLine + "\n");
		writer.close();

		System.out.println(results);
	}

	public static void main(String[] args) {
		try {
			RevisionInfoBuilder builder = new RevisionInfoBuilder(args);
			builder.buildInfo();
			builder.writeLine();
		} catch (UsageException e) {
			System.err.println(e);
			System.err.println("usage: RevisionInfoBuilder [product_name] "
					+ "[product_directory] [output_file]");
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
