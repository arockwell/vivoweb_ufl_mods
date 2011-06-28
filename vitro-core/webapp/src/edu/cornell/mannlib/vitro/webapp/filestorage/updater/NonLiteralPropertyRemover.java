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

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

/**
 * All image properties should have literal values. Burn any that don't.
 */
public class NonLiteralPropertyRemover extends FsuScanner {

	public NonLiteralPropertyRemover(FSUController controller) {
		super(controller);
	}

	/**
	 * Remove any image properties whose objects are not {@link Literal}s.
	 */
	public void remove() {
		updateLog.section("Checking for image properties whose objects "
				+ "are not literals.");

		removeNonLiterals(imageProperty, "image file");
		removeNonLiterals(thumbProperty, "thumbnail");
	}

	/**
	 * Check all resources for bogus values on this property.
	 */
	private void removeNonLiterals(Property prop, String label) {
		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				prop)) {
			removeNonLiterals(resource, prop, label);
		}
	}

	/**
	 * Check this resource for bogus values onthis property.
	 */
	private void removeNonLiterals(Resource resource, Property prop,
			String label) {
		List<RDFNode> bogusValues = new ArrayList<RDFNode>();
		for (Statement stmt : ResourceWrapper.listProperties(resource, prop)) {
			RDFNode object = stmt.getObject();
			if (!object.isLiteral()) {
				bogusValues.add(object);
			}
		}

		for (RDFNode bogusValue : bogusValues) {
			updateLog.warn(resource, "discarding " + label
					+ " property with non-literal as object: '" + bogusValue
					+ "'");
			model.enterCriticalSection(Lock.WRITE);
			try {
				model.createStatement(resource, prop, bogusValue).remove();
			} finally {
				model.leaveCriticalSection();
			}
		}
	}

}
