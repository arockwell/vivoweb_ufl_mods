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
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

/**
 * Base class for the tools that scan the model. Holds some useful fields and
 * some utility methods.
 */
public abstract class FsuScanner {
	protected final Model model;
	protected final FSULog updateLog;

	protected final Property imageProperty;
	protected final Property thumbProperty;

	public FsuScanner(FSUController controller) {
		this.model = controller.getModel();
		this.updateLog = controller.getUpdateLog();

		this.imageProperty = model.createProperty(FileStorageUpdater.IMAGEFILE);
		this.thumbProperty = model
				.createProperty(FileStorageUpdater.IMAGETHUMB);
	}

	/**
	 * Read all of the specified properties on a resource, and return a
	 * {@link List} of the {@link String} values.
	 */
	protected List<String> getValues(Resource resource, Property property) {
		List<String> list = new ArrayList<String>();
		StmtIterator stmts = resource.listProperties(property);
		try {
			while (stmts.hasNext()) {
				Statement stmt = stmts.next();
				RDFNode object = stmt.getObject();
				if (object.isLiteral()) {
					list.add(((Literal) object).getString());
				} else {
					updateLog.error(resource,
							"property value was not a literal: "
									+ "property is '" + property.getURI()
									+ "', value is '" + object + "'");
				}
			}
		} finally {
			stmts.close();
		}
		return list;
	}

	/**
	 * Read all of the specified properties on a resource, and return a
	 * {@link List} of the {@link Statement}s.
	 */
	protected List<Statement> getStatements(Resource resource, Property property) {
		List<Statement> list = new ArrayList<Statement>();
		
		resource.getModel().enterCriticalSection(Lock.READ);
		StmtIterator stmts = resource.listProperties(property);
		try {
			while (stmts.hasNext()) {
				list.add(stmts.next());
			}
		} finally {
			stmts.close();
			resource.getModel().leaveCriticalSection();
		}
		return list;
	}

	/**
	 * Find the filename within a path so we can add this prefix to it, while
	 * retaining the path.
	 */
	protected String addFilenamePrefix(String prefix, String path) {
		int slashHere = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		if (slashHere == -1) {
			return prefix + path;
		} else {
			String dirs = path.substring(0, slashHere + 1);
			String filename = path.substring(slashHere + 1);
			return dirs + prefix + filename;
		}
	}

	/**
	 * We are about to create a file - if a file of this name already exists,
	 * increment the name until we have no collision.
	 * 
	 * @return the original file, or the file with the incremented name.
	 */
	protected File checkNameConflicts(final File file) {
		if (!file.exists()) {
			// No conflict.
			return file;
		}

		File parent = file.getParentFile();
		String filename = file.getName();
		for (int i = 0; i < 100; i++) {
			File newFile = new File(parent, i + filename);
			if (!newFile.exists()) {
				updateLog.log("File '" + file + "' already exists, using '"
						+ newFile + "' to avoid conflict.");
				return newFile;
			}
		}

		updateLog.error("File '" + file
				+ "' already exists. Unable to avoid conflict.");
		return file;
	}
}
