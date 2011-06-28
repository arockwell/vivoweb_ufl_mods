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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

/**
 * Resets the RDF-Model to a known state, in preparation for the next Selenium
 * test suite.
 */
public class ModelCleaner {
	public static final String TEST_USER_ONTOLOGY_FILENAME = "test-user-model.owl";
	private final ModelCleanerProperties properties;
	private final TomcatController tomcatController;
	private final CommandRunner runner;
	private final Listener listener;

	public ModelCleaner(SeleniumRunnerParameters parms,
			TomcatController tomcatController) {
		this.properties = parms.getModelCleanerProperties();
		this.listener = parms.getListener();
		this.runner = new CommandRunner(parms);
		this.tomcatController = tomcatController;

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
		tomcatController.stopTheWebapp();
		insertTheUserFile();
		recreateTheDatabase();
		tomcatController.startTheWebapp();
		removeTheUserFile();
	}

	/**
	 * Copy the test data ontology file into the auth area, so we get our
	 * pre-defined admin user.
	 */
	private void insertTheUserFile() {
		InputStream userOntologyStream = this.getClass().getResourceAsStream(
				TEST_USER_ONTOLOGY_FILENAME);
		if (userOntologyStream == null) {
			throw new FatalException(
					"Couldn't find the Test User Ontology file: '"
							+ TEST_USER_ONTOLOGY_FILENAME + "'");
		}

		File userOntologyTarget = figureUserOntologyTarget();
		try {
			FileHelper.copy(userOntologyStream, userOntologyTarget);
			userOntologyStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Drop the database and create it again, empty.
	 */
	private void recreateTheDatabase() {
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
	 * Remove the test data ontology file, so we leave no trace.
	 */
	private void removeTheUserFile() {
		File userOntologyTarget = figureUserOntologyTarget();
		userOntologyTarget.delete();
		if (userOntologyTarget.exists()) {
			listener.logWarning("Failed to delete the test data ontology "
					+ "file: '" + TEST_USER_ONTOLOGY_FILENAME + "'");
		}
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
	 * Figure out where the test data ontology file should go. C:\Program
	 * Files\Apache Software Foundation\Tomcat
	 * 6.0\webapps\vivo\WEB-INF\ontologies\auth
	 */
	private File figureUserOntologyTarget() {
		File webappDirectory = properties.getWebappDirectory();
		File authDirectory = new File(webappDirectory,
				"WEB-INF/ontologies/auth");

		if (!authDirectory.exists()) {
			throw new FatalException("Target directory for the test data "
					+ "ontology file doesn't exist. Webapp directory is '"
					+ webappDirectory + "', target directory is '"
					+ authDirectory + "'");
		}

		return new File(authDirectory, TEST_USER_ONTOLOGY_FILENAME);
	}

	static String convertBackslashes(File file) {
		return file.getPath().replace("\\", "/");
	}
}
