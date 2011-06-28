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

import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * If a resource has more than one image or more than one thumbnail, this
 * discards the extras.
 */
public class MultiplePropertyRemover extends FsuScanner {

	public MultiplePropertyRemover(FSUController controller) {
		super(controller);
	}

	/**
	 * By now, we have removed any non-literals or dead ends, so keep the first
	 * one and discard any extras.
	 */
	public void remove() {
		updateLog.section("Checking for resources with more "
				+ "than one main image, or more than one thumbnail.");

		removeExtraProperties(imageProperty, "main image");
		removeExtraProperties(thumbProperty, "thumbnail");
	}

	/**
	 * Check each resource that has this property.
	 */
	public void removeExtraProperties(Property prop, String label) {
		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				prop)) {
			removeExtraProperties(resource, prop, label);
		}
	}

	/**
	 * If this resource has more than one of this property, delete the extras.
	 */
	private void removeExtraProperties(Resource resource, Property prop,
			String label) {
		List<Statement> stmts = getStatements(resource, prop);
		for (int i = 1; i < stmts.size(); i++) {
			Statement stmt = stmts.get(i);
			RDFNode node = stmt.getObject();
			if (node.isLiteral()) {
				String value = ((Literal) node).getString();
				updateLog.warn(resource, "removing extra " + label
						+ " property: '" + value + "'");
			} else {
				updateLog.warn(resource, "removing extra " + label
						+ " property: '" + node + "'");
			}
			ModelWrapper.removeStatement(model, stmt);
		}
	}
}
