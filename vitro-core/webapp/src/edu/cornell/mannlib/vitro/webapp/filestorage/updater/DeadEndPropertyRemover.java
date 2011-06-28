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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Removes any image properties (main or thumbnail) that point to files that
 * don't actually exist.
 */
public class DeadEndPropertyRemover extends FsuScanner {
	private ImageDirectoryWithBackup imageDirectoryWithBackup;

	public DeadEndPropertyRemover(FSUController controller) {
		super(controller);
		this.imageDirectoryWithBackup = controller
				.getImageDirectoryWithBackup();
	}

	/**
	 * Remove dead end properties for both main images and thumbnails.
	 */
	public void remove() {
		updateLog.section("Removing image properties whose "
				+ "referenced files do not exist.");

		removeDeadEndProperties(imageProperty, "main image");
		removeDeadEndProperties(thumbProperty, "thumbnail");
	}

	/**
	 * Check all of the individuals that possess this property.
	 */
	private void removeDeadEndProperties(Property prop, String label) {
		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				prop)) {
			removeDeadEndPropertiesFromResource(resource, prop, label);
		}
	}

	/**
	 * Check these statments on this resource. If any of them does not point to
	 * an existing file, remove the statement.
	 */
	private void removeDeadEndPropertiesFromResource(Resource resource,
			Property prop, String label) {
		for (Statement stmt : getStatements(resource, prop)) {
			RDFNode node = stmt.getObject();
			if (node.isLiteral()) {
				String filename = ((Literal) node).getString();
				File file = imageDirectoryWithBackup.getExistingFile(filename);
				if (!file.exists()) {
					updateLog.warn(
							resource,
							"removing link to " + label + " '" + filename
									+ "': file does not exist at '"
									+ file.getAbsolutePath() + "'.");

					ModelWrapper.removeStatement(model, stmt);
				}
			}
		}
	}

}
