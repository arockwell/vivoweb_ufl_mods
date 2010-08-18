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
import java.util.ArrayList;
import java.util.List;

/**
 * Resets the RDF-Model to a known state, in preparation for the next Selenium
 * test suite.
 */
public class ModelCleaner {
	private final ModelCleanerProperties properties;
	private final CommandRunner runner;
	private final Listener listener;

	public ModelCleaner(SeleniumRunnerParameters parms) {
		this.properties = parms.getModelCleanerProperties();
		this.listener = parms.getListener();
		this.runner = new CommandRunner(parms);

		sanityCheck();
	}

	private void sanityCheck() {
		executeMysqlStatement("show databases;");

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			throw new FatalException(
					"sanityCheck: Failed to execute a MySQL statement: "
							+ "return code=" + returnCode);
		}
	}

	/**
	 * Reset the RDF-Model to a known state, according to the parameters in the
	 * properties file.
	 * 
	 * @throws CommandRunnerException
	 *             if a problem occurs in a sub-process.
	 */
	public void clean() throws CommandRunnerException {
		stopTheWebapp();
		dropDatabase();
		createAndLoadDatabase();
		startTheWebapp();
	}

	/**
	 * Stop Tomcat and wait the prescribed number of seconds for it to clean up.
	 */
	private void stopTheWebapp() throws CommandRunnerException {
		String tomcatStopCommand = properties.getTomcatStopCommand();
		int tomcatStopDelay = properties.getTomcatStopDelay();

		listener.webappStopping(tomcatStopCommand);
		runner.run(parseCommandLine(tomcatStopCommand));

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.webappStopFailed(returnCode);
			// Throw no exception - this can happen if Tomcat isn't running.
		}

		listener.webappWaitingForStop(tomcatStopDelay);
		try {
			Thread.sleep(tomcatStopDelay * 1000L);
		} catch (InterruptedException e) {
			// Just continue.
		}

		listener.webappStopped();
	}

	/**
	 * Delete the database.
	 */
	private void dropDatabase() {
		String mysqlStatement = "drop database " + properties.getMysqlDbName()
				+ "; create database " + properties.getMysqlDbName()
				+ " character set utf8;";

		listener.dropDatabaseStarting(mysqlStatement);
		executeMysqlStatement(mysqlStatement);

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.dropDatabaseFailed(returnCode);
			throw new FatalException("dropDatabase() failed: return code="
					+ returnCode);
		}

		listener.dropDatabaseComplete();
	}

	/**
	 * Rebuild the database.
	 */
	private void createAndLoadDatabase() {
		String mysqlStatement = "source "
				+ convertBackslashes(properties.getMysqlDumpfile()) + ";";

		listener.loadDatabaseStarting(mysqlStatement);
		executeMysqlStatement(mysqlStatement);

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.loadDatabaseFailed(returnCode);
			throw new FatalException("loadDatabase() failed: return code="
					+ returnCode);
		}

		listener.loadDatabaseComplete();
	}

	/**
	 * Start Tomcat and wait for it to initialize.
	 */
	private void startTheWebapp() {
		String tomcatStartCommand = properties.getTomcatStartCommand();
		int tomcatStartDelay = properties.getTomcatStartDelay();

		listener.webappStarting(tomcatStartCommand);
		try {
			runner.runAsBackground(parseCommandLine(tomcatStartCommand));
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.webappStartFailed(returnCode);
			throw new FatalException("startTheWebapp() failed: return code="
					+ returnCode);
		}

		listener.webappWaitingForStart(tomcatStartDelay);
		try {
			Thread.sleep(tomcatStartDelay * 1000L);
		} catch (InterruptedException e) {
			// Just continue.
		}

		listener.webappStarted();
	}

	/**
	 * Tell MySQL to execute this statement. If it fails, throw a fatal
	 * exception.
	 */
	private void executeMysqlStatement(String mysqlStatement) {
		List<String> cmd = new ArrayList<String>();
		cmd.add("mysql");
		cmd.add("--user=" + properties.getMysqlUsername());
		cmd.add("--password=" + properties.getMysqlPassword());
		cmd.add("--database=" + properties.getMysqlDbName());
		cmd.add("--execute=" + mysqlStatement);

		try {
			runner.run(cmd);
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}
	}

	/**
	 * A command line must be broken into separate arguments, where arguments
	 * are delimited by blanks unless the blank (and the argument) is enclosed
	 * in quotes.
	 */
	static List<String> parseCommandLine(String commandLine) {
		List<String> pieces = new ArrayList<String>();
		StringBuilder piece = null;
		boolean inDelimiter = true;
		boolean inQuotes = false;
		for (int i = 0; i < commandLine.length(); i++) {
			char thisChar = commandLine.charAt(i);
			if ((thisChar == ' ') && !inQuotes) {
				if (inDelimiter) {
					// No effect.
				} else {
					inDelimiter = true;
					pieces.add(piece.toString());
				}
			} else if (thisChar == '"') {
				// Quotes are not carried into the parsed strings.
				inQuotes = !inQuotes;
			} else { // Not a blank or a quote.
				if (inDelimiter) {
					inDelimiter = false;
					piece = new StringBuilder();
				}
				piece.append(thisChar);
			}
		}

		// There is an implied delimiter at the end of the command line.
		if (!inDelimiter) {
			pieces.add(piece.toString());
		}

		// Quotes must appear in pairs
		if (inQuotes) {
			throw new IllegalArgumentException(
					"Command line contains mismatched quotes: " + commandLine);
		}

		return pieces;
	}

	static String convertBackslashes(File file) {
		return file.getPath().replace("\\", "/");
	}
}
