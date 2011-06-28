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

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;

/**
 * Initializes the file storage system, and stores a reference in the servlet
 * context.
 */
public class FileStorageSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(FileStorageSetup.class);

	/**
	 * The implementation of the {@link FileStorage} system will be stored in
	 * the {@link ServletContext} as an attribute with this name.
	 */
	public static final String ATTRIBUTE_NAME = FileStorage.class.getName();

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the file storage base directory.
	 */
	public static final String PROPERTY_FILE_STORAGE_BASE_DIR = "upload.directory";

	/**
	 * The default implementation will use this key to ask
	 * {@link ConfigurationProperties} for the default URI namespace.
	 */
	public static final String PROPERTY_DEFAULT_NAMESPACE = "Vitro.defaultNamespace";

	/**
	 * Create an implementation of {@link FileStorage} and store it in the
	 * {@link ServletContext}, as an attribute named according to
	 * {@link #ATTRIBUTE_NAME}.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			File baseDirectory = figureBaseDir();
			Collection<String> fileNamespace = confirmDefaultNamespace();
			FileStorage fs = new FileStorageImpl(baseDirectory, fileNamespace);

			ServletContext sc = sce.getServletContext();
			sc.setAttribute(ATTRIBUTE_NAME, fs);
		} catch (Exception e) {
			log.fatal("Failed to initialize the file system.", e);
		}
	}

	/**
	 * Get the configuration property for the file storage base directory, and
	 * check that it points to an existing, writeable directory.
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 */
	private File figureBaseDir() {
		String baseDirPath = ConfigurationProperties
				.getProperty(PROPERTY_FILE_STORAGE_BASE_DIR);
		if (baseDirPath == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_FILE_STORAGE_BASE_DIR + "'");
		}
		return new File(baseDirPath);
	}

	/**
	 * Get the configuration property for the default namespace, and confirm
	 * that it is in the proper form. The default namespace is assumed to be in
	 * this form: <code>http://vivo.mydomain.edu/individual/</code>
	 * 
	 * For use by the constructor in implementations of {@link FileStorage}.
	 * 
	 * @returns a collection containing the default namespace.
	 */
	private Collection<String> confirmDefaultNamespace() {
		String defaultNamespace = ConfigurationProperties
				.getProperty(PROPERTY_DEFAULT_NAMESPACE);
		if (defaultNamespace == null) {
			throw new IllegalArgumentException(
					"Configuration properties must contain a value for '"
							+ PROPERTY_DEFAULT_NAMESPACE + "'");
		}

		String defaultSuffix = "/individual/";

		if (!defaultNamespace.endsWith(defaultSuffix)) {
			log.warn("Default namespace does not match the expected form "
					+ "(does not end with '" + defaultSuffix + "'): '"
					+ defaultNamespace + "'");
		}

		return Collections.singleton(defaultNamespace);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do here.
	}

}
