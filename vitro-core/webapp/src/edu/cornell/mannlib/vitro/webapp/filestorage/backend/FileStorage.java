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

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The interface for the File Storage system.
 */
public interface FileStorage {
	/**
	 * The name of the root directory, within the base directory.
	 */
	public static final String FILE_STORAGE_ROOT = "file_storage_root";

	/**
	 * The name of the file in the base directory that holds the namespace map.
	 */
	public static final String FILE_STORAGE_NAMESPACES_PROPERTIES = "file_storage_namespaces.properties";

	/**
	 * How often to we insert path separator characters?
	 */
	int SHORTY_LENGTH = 3;
	
	/**
	 * Store the bytes from this stream as a file with the specified ID and
	 * filename. If the file already exists, it is over-written.
	 * 
	 * @throws FileAlreadyExistsException
	 *             if a file already exists with this ID but with a different
	 *             filename.
	 * 
	 */
	void createFile(String id, String filename, InputStream bytes)
			throws FileAlreadyExistsException, IOException;

	/**
	 * If a file exists with this ID, get its name.
	 * 
	 * @return The name of the file (un-encoded) if it exists, or
	 *         <code>null</code> if it does not.
	 */
	String getFilename(String id) throws IOException;

	/**
	 * Get a stream that will provide the contents of the file that was stored
	 * with this ID and this filename. Close the stream when you're finished
	 * with it.
	 * 
	 * @throws FileNotFoundException
	 *             if there is no file that matches this ID and filename.
	 */
	InputStream getInputStream(String id, String filename)
			throws FileNotFoundException, IOException;

	/**
	 * If a file exists with this ID, it will be deleted, regardless of the file
	 * name. If no such file exists, no action is taken, no exception is thrown.
	 * 
	 * @return <code>true<code> if a file existed, <code>false</code> otherwise.
	 */
	boolean deleteFile(String id) throws IOException;
}
