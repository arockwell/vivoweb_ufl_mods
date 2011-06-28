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

package edu.cornell.mannlib.vitro.webapp.filestorage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;

/**
 * Static methods to help when serving uploaded files.
 */
public class FileServingHelper {
	private static final Log log = LogFactory.getLog(FileServingHelper.class);

	private static final String DEFAULT_PATH = "/individual/";
	private static final String FILE_PATH = "/file/";
	private static final String DEFAULT_NAMESPACE = initializeDefaultNamespace();

	/**
	 * At startup, get the default namespace from the configuration properties,
	 * and trim off the suffix.
	 */
	private static String initializeDefaultNamespace() {
		String defaultNamespace = ConfigurationProperties
				.getProperty(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		if (!defaultNamespace.endsWith(DEFAULT_PATH)) {
			log.warn("Default namespace does not match the expected form: '"
					+ defaultNamespace + "'");
		}

		return defaultNamespace;
	}

	/**
	 * <p>
	 * Combine the URI and the filename to produce a relative URL for the file
	 * (relative to the context of the webapp). The filename will be URLEncoded
	 * as needed.
	 * </p>
	 * <p>
	 * This should involve stripping the default namespace from the front of the
	 * URL, replacing it with the file prefix, and adding the filename to the
	 * end.
	 * </p>
	 * 
	 * @return <ul>
	 *         <li>the translated URL, if the URI was in the default namespace,</li>
	 *         <li>the original URI, if it wasn't in the default namespace,</li>
	 *         <li>null, if the original URI or the filename was null.</li>
	 *         </ul>
	 */
	public static String getBytestreamAliasUrl(String uri, String filename) {
		if ((uri == null) || (filename == null)) {
			return null;
		}
		if (!uri.startsWith(DEFAULT_NAMESPACE)) {
			log.warn("uri does not start with the default namespace: '" + uri
					+ "'");
			return uri;
		}
		String remainder = uri.substring(DEFAULT_NAMESPACE.length());

		try {
			filename = URLEncoder.encode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("No UTF-8 encoding?", e); // Can't happen.
		}

		String separator = remainder.endsWith("/") ? "" : "/";
		return FILE_PATH + remainder + separator + filename;
	}

	/** No need for instances because all of the methods are static. */
	private FileServingHelper() {
		// nothing to instantiate.
	}

	/**
	 * <p>
	 * Take a relative URL (relative to the context of the webapp) and produce
	 * the URI for the file bytestream.
	 * </p>
	 * <p>
	 * This should involve removing the filename from the end of the URL, and
	 * replacing the file prefix with the default namespace.
	 * </p>
	 * 
	 * @return the URI, or <code>null</code> if the URL couldn't be translated.
	 */
	public static String getBytestreamUri(String path) {
		if (path == null) {
			return null;
		}
		if (!path.startsWith(FILE_PATH)) {
			log.warn("path does not start with a file prefix: '" + path + "'");
			return null;
		}
		String remainder = path.substring(FILE_PATH.length());

		int slashHere = remainder.lastIndexOf('/');
		if (slashHere == -1) {
			log.debug("path does not include a filename: '" + path + "'");
			return null;
		}
		remainder = remainder.substring(0, slashHere);

		return DEFAULT_NAMESPACE + remainder;
	}
}
