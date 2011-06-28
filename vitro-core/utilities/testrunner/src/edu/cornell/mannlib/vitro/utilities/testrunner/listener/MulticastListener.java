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

package edu.cornell.mannlib.vitro.utilities.testrunner.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Listener} implementation that holds a list of {@link Listener}s and
 * sends each message to all of them.
 */
public class MulticastListener implements Listener {
	private final List<Listener> listeners = new ArrayList<Listener>();

	public void addListener(Listener l) {
		listeners.add(l);
	}

	// ----------------------------------------------------------------------
	// Listener methods
	// ----------------------------------------------------------------------

	@Override
	public void suiteIgnored(File suite) {
		for (Listener l : listeners) {
			l.suiteIgnored(suite);
		}
	}

	@Override
	public void suiteAdded(File suite) {
		for (Listener l : listeners) {
			l.suiteAdded(suite);
		}
	}

	@Override
	public void runStarted() {
		for (Listener l : listeners) {
			l.runStarted();
		}
	}

	@Override
	public void runFailed(Exception e) {
		for (Listener l : listeners) {
			l.runFailed(e);
		}
	}

	@Override
	public void runEndTime() {
		for (Listener l : listeners) {
			l.runEndTime();
		}
	}

	@Override
	public void runStopped() {
		for (Listener l : listeners) {
			l.runStopped();
		}
	}

	@Override
	public void cleanOutputStart(File outputDirectory) {
		for (Listener l : listeners) {
			l.cleanOutputStart(outputDirectory);
		}
	}

	@Override
	public void cleanOutputFailed(File outputDirectory, IOException e) {
		for (Listener l : listeners) {
			l.cleanOutputFailed(outputDirectory, e);
		}
	}

	@Override
	public void cleanOutputStop(File outputDirectory) {
		for (Listener l : listeners) {
			l.cleanOutputStop(outputDirectory);
		}
	}

	@Override
	public void webappStopping(String tomcatStopCommand) {
		for (Listener l : listeners) {
			l.webappStopping(tomcatStopCommand);
		}
	}

	@Override
	public void webappStopFailed(int returnCode) {
		for (Listener l : listeners) {
			l.webappStopFailed(returnCode);
		}
	}

	@Override
	public void webappStopped() {
		for (Listener l : listeners) {
			l.webappStopped();
		}
	}

	@Override
	public void dropDatabaseStarting(String statement) {
		for (Listener l : listeners) {
			l.dropDatabaseStarting(statement);
		}
	}

	@Override
	public void dropDatabaseFailed(int returnCode) {
		for (Listener l : listeners) {
			l.dropDatabaseFailed(returnCode);
		}
	}

	@Override
	public void dropDatabaseComplete() {
		for (Listener l : listeners) {
			l.dropDatabaseComplete();
		}
	}

	@Override
	public void loadDatabaseStarting(String statement) {
		for (Listener l : listeners) {
			l.loadDatabaseStarting(statement);
		}
	}

	@Override
	public void loadDatabaseFailed(int returnCode) {
		for (Listener l : listeners) {
			l.loadDatabaseFailed(returnCode);
		}
	}

	@Override
	public void loadDatabaseComplete() {
		for (Listener l : listeners) {
			l.loadDatabaseComplete();
		}
	}

	@Override
	public void webappCheckingReady(String command) {
		for (Listener l : listeners) {
			l.webappCheckingReady(command);
		}
	}

	@Override
	public void webappCheckReadyFailed(int returnCode) {
		for (Listener l : listeners) {
			l.webappCheckReadyFailed(returnCode);
		}
	}

	@Override
	public void webappCheckedReady() {
		for (Listener l : listeners) {
			l.webappCheckedReady();
		}
	}

	@Override
	public void webappStarting(String tomcatStartCommand) {
		for (Listener l : listeners) {
			l.webappStarting(tomcatStartCommand);
		}
	}

	@Override
	public void webappStartFailed(int returnCode) {
		for (Listener l : listeners) {
			l.webappStartFailed(returnCode);
		}
	}

	@Override
	public void webappStarted() {
		for (Listener l : listeners) {
			l.webappStarted();
		}
	}

	@Override
	public void subProcessStart(List<String> command) {
		for (Listener l : listeners) {
			l.subProcessStart(command);
		}
	}

	@Override
	public void subProcessStartInBackground(List<String> command) {
		for (Listener l : listeners) {
			l.subProcessStartInBackground(command);
		}
	}

	@Override
	public void subProcessStdout(String string) {
		for (Listener l : listeners) {
			l.subProcessStdout(string);
		}
	}

	@Override
	public void subProcessErrout(String string) {
		for (Listener l : listeners) {
			l.subProcessErrout(string);
		}
	}

	@Override
	public void subProcessStop(List<String> command) {
		for (Listener l : listeners) {
			l.subProcessStop(command);
		}
	}

	@Override
	public void suiteStarted(File suiteDir) {
		for (Listener l : listeners) {
			l.suiteStarted(suiteDir);
		}
	}

	@Override
	public void suiteTestingStarted(File suiteDir) {
		for (Listener l : listeners) {
			l.suiteTestingStarted(suiteDir);
		}
	}

	@Override
	public void suiteFailed(File suiteDir, int returnCode) {
		for (Listener l : listeners) {
			l.suiteFailed(suiteDir, returnCode);
		}
	}

	@Override
	public void suiteFailed(File suiteDir, Exception e) {
		for (Listener l : listeners) {
			l.suiteFailed(suiteDir, e);
		}
	}

	@Override
	public void suiteTestingStopped(File suiteDir) {
		for (Listener l : listeners) {
			l.suiteTestingStopped(suiteDir);
		}
	}

	@Override
	public void suiteStopped(File suiteDir) {
		for (Listener l : listeners) {
			l.suiteStopped(suiteDir);
		}
	}

	@Override
	public void cleanUploadStart(File uploadDirectory) {
		for (Listener l : listeners) {
			l.cleanUploadStart(uploadDirectory);
		}
	}

	@Override
	public void cleanUploadFailed(File uploadDirectory, IOException e) {
		for (Listener l : listeners) {
			l.cleanUploadFailed(uploadDirectory, e);
		}
	}

	@Override
	public void cleanUploadStop(File uploadDirectory) {
		for (Listener l : listeners) {
			l.cleanUploadStop(uploadDirectory);
		}
	}

	@Override
	public void logWarning(String message) {
		for (Listener l : listeners) {
			l.logWarning(message);
		}
	}

}
