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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

/**
 * <p>
 * A harness that runs a system-level command.
 * </p>
 * <p>
 * No provision is made for standard input.
 * </p>
 * <p>
 * The standard output and standard error streams are asynchronously read, so
 * the sub-process will not block on full buffers. Warning: if either of these
 * streams contain more data than can fit into a String, then we will have a
 * problem.
 * </p>
 * 
 * @author jblake
 */
public class CommandRunner {

	private Integer returnCode;
	private String stdOut = "";
	private String stdErr = "";
	private File workingDirectory;

	/* Gets informed of output as it arrives. Never null. */
	private final Listener listener;

	private final Map<String, String> environmentAdditions = new HashMap<String, String>();

	public CommandRunner(SeleniumRunnerParameters parms) {
		this.listener = parms.getListener();
	}

	/** Set the directory that the command will run in. */
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/** Add (or replace) any environment variable. */
	public void setEnvironmentVariable(String key, String value) {
		this.environmentAdditions.put(key, value);
	}

	/**
	 * Run the command.
	 * 
	 * @param command
	 *            a list containing the operating system program and its
	 *            arguments. See
	 *            {@link java.lang.ProcessBuilder#ProcessBuilder(List)}.
	 */
	public void run(List<String> command) throws CommandRunnerException {
		listener.subProcessStart(command);
		try {
			ProcessBuilder builder = new ProcessBuilder(command);

			if (workingDirectory != null) {
				builder.directory(workingDirectory);
			}

			if (!environmentAdditions.isEmpty()) {
				builder.environment().putAll(this.environmentAdditions);
			}

			Process process = builder.start();
			StreamEater outputEater = new StreamEater(process.getInputStream(),
					false);
			StreamEater errorEater = new StreamEater(process.getErrorStream(),
					true);

			this.returnCode = process.waitFor();

			outputEater.join(1000);
			outputEater.stopRunning();
			this.stdOut = outputEater.getContents();

			errorEater.join(1000);
			errorEater.stopRunning();
			this.stdErr = errorEater.getContents();
		} catch (IOException e) {
			throw new CommandRunnerException(
					"Exception when handling sub-process:", e);
		} catch (InterruptedException e) {
			throw new CommandRunnerException(
					"Exception when handling sub-process:", e);
		}
		listener.subProcessStop(command);
	}

	public int getReturnCode() {
		if (returnCode == null) {
			throw new IllegalStateException("Return code is not available.");
		}
		return returnCode;
	}

	public String getStdErr() {
		return stdErr;
	}

	public String getStdOut() {
		return stdOut;
	}

	/**
	 * A thread that reads an InputStream until it reaches end of file, then
	 * closes the stream. Designated as error stream or not, so it can tell the
	 * logger.
	 */
	private class StreamEater extends Thread {
		private final InputStream stream;
		private final boolean isError;
		private volatile boolean running;

		private final StringWriter contents = new StringWriter();

		private final byte[] buffer = new byte[4096];

		public StreamEater(InputStream stream, boolean isError) {
			this.stream = stream;
			this.isError = isError;
			this.running = true;
			this.start();
		}
		
		public void stopRunning() {
			this.running = false;
		}

		@Override
		public void run() {
			try {
				int howMany = 0;
				while (running) {
					howMany = stream.read(buffer);
					if (howMany > 0) {
						String string = new String(buffer, 0, howMany);
						contents.write(string);

						if (isError) {
							listener.subProcessErrout(string);
						} else {
							listener.subProcessStdout(string);
						}
					} else if (howMany == 0) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public String getContents() {
			return contents.toString();
		}
	}

}
