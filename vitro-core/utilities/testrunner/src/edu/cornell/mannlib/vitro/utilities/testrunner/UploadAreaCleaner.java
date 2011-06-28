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

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

/**
 * Clean out the file upload area, so the next suite will start with no uploads.
 */
public class UploadAreaCleaner {
	private final SeleniumRunnerParameters parms;
	private final Listener listener;

	public UploadAreaCleaner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.listener = parms.getListener();
	}

	/**
	 * Delete all of the directories and files in the upload directory. Don't
	 * delete the upload directory itself.
	 */
	public void clean() throws IOException {
		File uploadDirectory = parms.getUploadDirectory();
		if (!uploadDirectory.isDirectory()) {
			throw new IllegalArgumentException("'" + uploadDirectory.getPath()
					+ "' is not a directory.");
		}

		listener.cleanUploadStart(uploadDirectory);

		try {
			for (File file : uploadDirectory.listFiles()) {
				if (file.isFile()) {
					FileHelper.deleteFile(file);
				} else {
					FileHelper.purgeDirectoryRecursively(file);
				}
			}
		} catch (IOException e) {
			listener.cleanUploadFailed(uploadDirectory, e);
			throw e;
		} finally {
			listener.cleanUploadStop(uploadDirectory);
		}
	}

}
