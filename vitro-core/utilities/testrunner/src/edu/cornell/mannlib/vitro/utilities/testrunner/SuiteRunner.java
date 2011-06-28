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
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

/**
 * Run a Selenium TestSuite in a sub-process.
 */
public class SuiteRunner {

	private final SeleniumRunnerParameters parms;
	private final CommandRunner runner;
	private final Listener listener;

	public SuiteRunner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.runner = new CommandRunner(parms);
		this.listener = parms.getListener();
	}

	/**
	 * Run the suite.
	 */
	public void runSuite(File suiteDir) {
		listener.suiteTestingStarted(suiteDir);

		List<String> cmd = new ArrayList<String>();
		cmd.add("java");
		cmd.add("-jar");
		cmd.add(parms.getSeleniumJarPath().getPath());
		cmd.add("-singleWindow");
		cmd.add("-timeout");
		cmd.add(String.valueOf(parms.getSuiteTimeoutLimit()));
		cmd.add("-userExtensions");
		cmd.add(parms.getUserExtensionsFile().getPath());

		// TODO - figure out why the use of a template means running the test
		// twice in simultaneous tabs.
		// if (parms.hasFirefoxProfileDir()) {
		// cmd.add("-firefoxProfileTemplate");
		// cmd.add(parms.getFirefoxProfileDir().getPath());
		// }

		String suiteName = suiteDir.getName();
		File outputFile = new File(parms.getOutputDirectory(), suiteName
				+ ".html");
		File suiteFile = new File(suiteDir, "Suite.html");

		cmd.add("-htmlSuite");
		cmd.add("*firefox");
		cmd.add(parms.getWebsiteUrl());
		cmd.add(suiteFile.getPath());
		cmd.add(outputFile.getPath());

		try {
			runner.run(cmd);
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.suiteFailed(suiteDir, returnCode);
		}

		listener.suiteTestingStopped(suiteDir);
	}

}
