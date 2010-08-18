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

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.updater.FileStorageUpdater;

/**
 * Check that the conditions are met for updating uploaded files. If everything
 * is in place, call the updater.
 */
public class UpdateUploadedFiles implements ServletContextListener {
	private static final Log log = LogFactory.getLog(UpdateUploadedFiles.class);

	/**
	 * Nothing to do on teardown.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		return;
	}

	/**
	 * Check that the ontology model, the old upload directory, and the file
	 * storage system are all valid. Then do the update.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			ServletContext ctx = sce.getServletContext();

			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("assertionsWebappDaoFactory");
			if (wadf == null) {
				throw new IllegalStateException("Webapp DAO Factory is null. "
						+ "The ServletContext does not contain an attribute "
						+ "for '" + "assertionsWebappDaoFactory" + "'. "
						+ "Does the log contain a previous exception from "
						+ "JenaDataSourceSetup? Is it possible that web.xml "
						+ "is not set up to run JenaDataSourceSetup before "
						+ "UpdateUploadedFiles?");
			}

			OntModel jenaOntModel = (OntModel) ctx
					.getAttribute(JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME);
			if (jenaOntModel == null) {
				throw new IllegalStateException("Ontology model is null. "
						+ "The ServletContext does not contain an attribute "
						+ "for '"
						+ JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME
						+ "'. "
						+ "Does the log contain a previous exception from "
						+ "JenaDataSourceSetup? Is it possible that web.xml "
						+ "is not set up to run JenaDataSourceSetup before "
						+ "UpdateUploadedFiles?");
			}

			FileStorage fileStorage = (FileStorage) ctx
					.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
			if (fileStorage == null) {
				throw new IllegalStateException("File storage system is null. "
						+ "The ServletContext does not contain an attribute "
						+ "for '" + FileStorageSetup.ATTRIBUTE_NAME + "'. "
						+ "Does the log contain a previous exception from "
						+ "FileStorageSetup? Is it possible that web.xml is "
						+ "not set up to run FileStorageSetup before "
						+ "UpdateUploadedFiles?");
			}

			String uploadDirectoryName = ConfigurationProperties
					.getProperty(FileStorageSetup.PROPERTY_FILE_STORAGE_BASE_DIR);
			if (uploadDirectoryName == null) {
				throw new IllegalStateException("Upload directory name is null");
			}
			File uploadDirectory = new File(uploadDirectoryName);
			if (!uploadDirectory.exists()) {
				throw new IllegalStateException("Upload directory '"
						+ uploadDirectory.getAbsolutePath()
						+ "' does not exist.");
			}

			String webappImagePath = ctx.getRealPath("images");
			File webappImageDirectory = (webappImagePath == null) ? null
					: new File(webappImagePath);

			FileStorageUpdater fsu = new FileStorageUpdater(wadf, jenaOntModel,
					fileStorage, uploadDirectory, webappImageDirectory);
			fsu.update();
		} catch (Exception e) {
			log.fatal("Unknown problem", e);
		}
	}
}
