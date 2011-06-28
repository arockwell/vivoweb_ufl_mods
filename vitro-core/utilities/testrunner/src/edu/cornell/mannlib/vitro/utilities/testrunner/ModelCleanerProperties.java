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
import java.util.Properties;

/**
 * Hold the runtime properties that pertain specifically to cleaning the data
 * model.
 */
public class ModelCleanerProperties {
	public static final String PROP_MYSQL_USERNAME = "mysql_username";
	public static final String PROP_MYSQL_PASSWORD = "mysql_password";
	public static final String PROP_MYSQL_DB_NAME = "mysql_db_name";
	public static final String PROP_WEBAPP_DIRECTORY = "vivo_webapp_directory";
	public static final String PROP_TOMCAT_CHECK_READY_COMMAND = "tomcat_check_ready_command";
	public static final String PROP_TOMCAT_STOP_COMMAND = "tomcat_stop_command";
	public static final String PROP_TOMCAT_START_COMMAND = "tomcat_start_command";

	private final String mysqlUsername;
	private final String mysqlPassword;
	private final String mysqlDbName;
	private final File webappDirectory;
	private final String tomcatCheckReadyCommand;
	private final String tomcatStopCommand;
	private final String tomcatStartCommand;

	/**
	 * Confirm that we have the expected properties, and that their values seem
	 * reasonable.
	 */
	public ModelCleanerProperties(Properties props) {
		this.mysqlUsername = getRequiredProperty(props, PROP_MYSQL_USERNAME);
		this.mysqlPassword = getRequiredProperty(props, PROP_MYSQL_PASSWORD);
		this.mysqlDbName = getRequiredProperty(props, PROP_MYSQL_DB_NAME);

		this.webappDirectory = confirmWebappDirectory(props);

		this.tomcatCheckReadyCommand = getRequiredProperty(props,
				PROP_TOMCAT_CHECK_READY_COMMAND);
		this.tomcatStopCommand = getRequiredProperty(props,
				PROP_TOMCAT_STOP_COMMAND);
		this.tomcatStartCommand = getRequiredProperty(props,
				PROP_TOMCAT_START_COMMAND);
	}

	public String getMysqlUsername() {
		return mysqlUsername;
	}

	public String getMysqlPassword() {
		return mysqlPassword;
	}

	public String getMysqlDbName() {
		return mysqlDbName;
	}

	public File getWebappDirectory() {
		return webappDirectory;
	}

	public String getTomcatCheckReadyCommand() {
		return tomcatCheckReadyCommand;
	}

	public String getTomcatStopCommand() {
		return tomcatStopCommand;
	}

	public String getTomcatStartCommand() {
		return tomcatStartCommand;
	}

	/**
	 * Get the value for this property. If there isn't one, or if it's empty,
	 * complain.
	 */
	private String getRequiredProperty(Properties props, String key) {
		String value = props.getProperty(key);
		if ((value == null) || (value.trim().length() == 0)) {
			throw new IllegalArgumentException(
					"Property file must provide a value for '" + key + "'");
		}
		return value;
	}

	/**
	 * The dumpfile parameter must point to an existing directory.
	 */
	private File confirmWebappDirectory(Properties props) {
		String filename = getRequiredProperty(props, PROP_WEBAPP_DIRECTORY);
		File webappDirectory = new File(filename);
		if (!webappDirectory.exists()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_WEBAPP_DIRECTORY + "': directory '" + filename
					+ "' does not exist.");
		}
		if (!webappDirectory.isDirectory()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_WEBAPP_DIRECTORY + "': '" + filename
					+ "' is not a directory.");
		}
		if (!webappDirectory.canWrite()) {
			throw new IllegalArgumentException("Invalid value for '"
					+ PROP_WEBAPP_DIRECTORY + "': directory '" + filename
					+ "' is not writeable.");
		}
		return webappDirectory;
	}

	public String toString() {
		return "\n      mysqlUsername: " + mysqlUsername
				+ "\n      mysqlPassword: " + mysqlPassword
				+ "\n      mysqlDbName: " + mysqlDbName
				+ "\n      webappDirectory: " + webappDirectory;
	}

}
