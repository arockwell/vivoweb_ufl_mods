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

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A way to look for files in TOMCAT_WEBAPP/vivo/images, if they are not found
 * in upload.directory/images.
 */
public class ImageDirectoryWithBackup {
	private static final Log log = LogFactory
			.getLog(ImageDirectoryWithBackup.class);

	/** The primary image directory, where we do most of the manipulation. */
	private final File uploadImageDirectory;

	/**
	 * If we are looking for a file and don't find it in the primary directory,
	 * look for it here.
	 */
	private final File webappImageDirectory;

	/**
	 * Be careful! webappImageDirectory may be null.
	 */
	public ImageDirectoryWithBackup(File uploadImageDirectory,
			File webappImageDirectory) {
		this.uploadImageDirectory = uploadImageDirectory;
		this.webappImageDirectory = webappImageDirectory;
	}

	/**
	 * When looking to read a file, start by looking in the
	 * {@link #uploadImageDirectory}.
	 * 
	 * If the file isn't found there, look in the {@link #webappImageDirectory}
	 * as a fallback.
	 * 
	 * If not there either, return the pointer to the nonexistent file in the
	 * {@link #uploadImageDirectory}.
	 */
	File getExistingFile(String relativePath) {
		File file1 = new File(uploadImageDirectory, relativePath);
		if (file1.exists()) {
			log.trace("Found file: " + file1.getAbsolutePath());
			return file1;
		}
		if (webappImageDirectory != null) {
			File file2 = new File(webappImageDirectory, relativePath);
			if (file2.exists()) {
				log.trace("Found file: " + file2.getAbsolutePath());
				return file2;
			}
		}
		log.trace("Didn't find file: " + file1.getAbsolutePath());
		return file1;
	}

	/**
	 * New files will always be created in the primary directory.
	 */
	File getNewfile(String relativePath) {
		return new File(uploadImageDirectory, relativePath);
	}

	/**
	 * You can get a direct reference to the primary image directory, but it
	 * should only be used for directory-base operations, like final cleanup.
	 */
	public File getPrimaryImageDirectory() {
		return uploadImageDirectory;
	}
}
