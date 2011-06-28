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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A collection of static routines for moving, copying and deleting files.
 */
public class FileUtil {
	/**
	 * Copy a file from one location to another, and remove it from the original
	 * location.
	 */
	public static void moveFile(File from, File to) throws IOException {
		copyFile(from, to);
		deleteFile(from);
	}

	/**
	 * Copy a file from one location to another.
	 */
	public static void copyFile(File from, File to) throws IOException {
		if (!from.exists()) {
			throw new FileNotFoundException("File '" + from.getAbsolutePath()
					+ "' does not exist.");
		}

		InputStream in = null;
		try {
			in = new FileInputStream(from);
			writeFile(in, to);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create a file with the contents of this data stream.
	 * 
	 * @param stream
	 *            the data stream. You must close it afterward.
	 */
	public static void writeFile(InputStream stream, File to)
			throws IOException {
		if (to.exists()) {
			throw new IOException("File '" + to.getAbsolutePath()
					+ "' already exists.");
		}

		File parent = to.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
			if (!parent.exists()) {
				throw new IOException("Can't create parent directory for '"
						+ to.getAbsolutePath() + "'");
			}
		}

		OutputStream out = null;
		try {
			out = new FileOutputStream(to);
			byte[] buffer = new byte[8192];
			int howMany;
			while (-1 != (howMany = stream.read(buffer))) {
				out.write(buffer, 0, howMany);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Delete this file, and make sure that it's gone.
	 */
	public static void deleteFile(File file) throws IOException {
		file.delete();
		if (file.exists()) {
			throw new IOException("Failed to delete file '"
					+ file.getAbsolutePath() + "'");
		}
	}

	/** No need to instantiate it -- all methods are static. */
	private FileUtil() {
		// Nothing to instantiate.
	}

}
