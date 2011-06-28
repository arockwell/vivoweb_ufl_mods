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
import java.util.List;

/**
 * A listener for all events that occur during the run.
 */
public interface Listener {

	void suiteIgnored(File suite);

	void suiteAdded(File suite);

	void runStarted();

	void runFailed(Exception e);

	void runEndTime();

	void runStopped();

	void cleanOutputStart(File outputDirectory);

	void cleanOutputFailed(File outputDirectory, IOException e);

	void cleanOutputStop(File outputDirectory);

	void webappStopping(String tomcatStopCommand);

	void webappStopFailed(int returnCode);

	void webappStopped();

	void dropDatabaseStarting(String statement);

	void dropDatabaseFailed(int returnCode);

	void dropDatabaseComplete();

	void loadDatabaseStarting(String statement);

	void loadDatabaseFailed(int returnCode);

	void loadDatabaseComplete();

	void webappCheckingReady(String command);

	void webappCheckReadyFailed(int returnCode);

	void webappCheckedReady();

	void webappStarting(String command);

	void webappStartFailed(int returnCode);

	void webappStarted();

	void subProcessStart(List<String> command);

	void subProcessStartInBackground(List<String> command);

	void subProcessStdout(String string);

	void subProcessErrout(String string);

	void subProcessStop(List<String> command);

	void suiteStarted(File suiteDir);

	void suiteTestingStarted(File suiteDir);

	void suiteFailed(File suiteDir, int returnCode);

	void suiteFailed(File suiteDir, Exception e);

	void suiteTestingStopped(File suiteDir);

	void suiteStopped(File suiteDir);

	void cleanUploadStart(File uploadDirectory);

	void cleanUploadFailed(File uploadDirectory, IOException e);

	void cleanUploadStop(File uploadDirectory);

	void logWarning(String message);

}
