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

import static edu.cornell.mannlib.vitro.utilities.testrunner.SeleniumRunnerParameters.LOGFILE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Run the Selenium test suites. Provide the properties file and perhaps an
 * "interactive" flag.
 */
public class SeleniumRunner {
	private final SeleniumRunnerParameters parms;
	private final Listener listener;
	private final UploadAreaCleaner uploadCleaner;
	private final ModelCleaner modelCleaner;
	private final SuiteRunner suiteRunner;
	private final OutputManager outputManager;

	public SeleniumRunner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.listener = parms.getListener();
		this.uploadCleaner = new UploadAreaCleaner(parms);
		this.modelCleaner = new ModelCleaner(parms);
		this.suiteRunner = new SuiteRunner(parms);
		this.outputManager = new OutputManager(parms);
	}

	public boolean runSelectedSuites() {
		boolean success;
		try {
			listener.runStarted();
			outputManager.cleanOutputDirectory();
			for (File suiteDir : parms.getSelectedSuites()) {
				listener.suiteStarted(suiteDir);
				try {
					if (parms.isCleanModel()) {
						modelCleaner.clean();
					}
					if (parms.isCleanUploads()) {
						uploadCleaner.clean();
					}
					suiteRunner.runSuite(suiteDir);
				} catch (IOException e) {
					listener.suiteFailed(suiteDir, e);
				} catch (CommandRunnerException e) {
					listener.suiteFailed(suiteDir, e);
				}
				listener.suiteStopped(suiteDir);
			}
			listener.runEndTime();
			Status status = outputManager.summarizeOutput();
			success = (status == Status.OK);
		} catch (IOException e) {
			listener.runFailed(e);
			success = false;
			e.printStackTrace();
		} catch (FatalException e) {
			listener.runFailed(e);
			success = false;
			e.printStackTrace();
		}
		listener.runStopped();
		return success;
	}

	private static void selectAllSuites(SeleniumRunnerParameters parms) {
		List<File> suites = new ArrayList<File>();
		for (File parentDir : parms.getSuiteParentDirectories()) {
			suites.addAll(parms.findSuiteDirs(parentDir));
		}
		parms.setSelectedSuites(suites);
	}

	private static void usage(String message) {
		System.out.println(message);
		System.out.println("Usage is: SeleniumRunner <parameters_file> "
				+ "[\"interactive\"]");
		System.exit(1);
	}

	public static void main(String[] args) {
		SeleniumRunnerParameters parms = null;
		boolean interactive = false;
		boolean success = false;

		if ((args.length != 1) && (args.length != 2)) {
			usage("Wrong number of arguments.");
		}

		if (args.length == 2) {
			String option = args[1].trim();
			if (option.length() > 0) {
				if (!"interactive".equalsIgnoreCase(args[1])) {
					usage("Invalid argument '" + args[1] + "'");
				}
				interactive = true;
			}
		}

		try {
			parms = new SeleniumRunnerParameters(args[0]);
		} catch (IOException e) {
			usage("Can't read properties file: " + e.getMessage());
		}

		if (interactive) {
			// TODO hook up the GUI.
			throw new RuntimeException("interactive mode not implemented.");
		} else {
			File logFile = new File(parms.getOutputDirectory(), LOGFILE_NAME);
			System.out.println("Log file is '" + logFile.getPath() + "'");

			// Run all of the suites.
			// For each suite, clean the model and the upload area.
			selectAllSuites(parms);
			parms.setCleanModel(true);
			parms.setCleanUploads(true);

			System.out.println(parms);

			SeleniumRunner runner = new SeleniumRunner(parms);
			success = runner.runSelectedSuites();
		}

		System.exit(success ? 0 : -1);
	}

}
