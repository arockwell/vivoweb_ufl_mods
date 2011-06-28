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

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AbstractResourceAction;

/**
 * Policy to use for Vivo Self-Editing based on NetId for use at Cornell. All
 * methods in this class should be thread safe and side effect free.
 */
public class SelfEditingPolicy extends BaseSelfEditingPolicy implements
		PolicyIface {
	protected static Log log = LogFactory.getLog(SelfEditingPolicy.class);

	protected final OntModel model;
	private final AdministrativeUriRestrictor restrictor;

	public SelfEditingPolicy(Set<String> prohibitedProperties,
			Set<String> prohibitedResources, Set<String> prohibitedNamespaces,
			Set<String> editableVitroUris, OntModel model) {
		this.model = model;
		this.restrictor = new AdministrativeUriRestrictor(prohibitedProperties,
				prohibitedResources, prohibitedNamespaces, editableVitroUris);
	}

	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		if (whoToAuth == null) {
			return inconclusiveDecision("whoToAuth was null");
		}
		if (whatToAuth == null) {
			return inconclusiveDecision("whatToAuth was null");
		}

		List<String> userUris = getUrisOfSelfEditor(whoToAuth);

		if (userUris.isEmpty()) {
			return inconclusiveDecision("Not self-editing.");
		}

		if (whatToAuth instanceof AbstractObjectPropertyAction) {
			return isAuthorizedForObjectPropertyAction(userUris,
					(AbstractObjectPropertyAction) whatToAuth);
		}

		if (whatToAuth instanceof AbstractDataPropertyAction) {
			return isAuthorizedForDataPropertyAction(userUris,
					(AbstractDataPropertyAction) whatToAuth);
		}

		if (whatToAuth instanceof AbstractResourceAction) {
			return isAuthorizedForResourceAction((AbstractResourceAction) whatToAuth);
		}

		return inconclusiveDecision("Does not authorize "
				+ whatToAuth.getClass().getSimpleName() + " actions");
	}

	/**
	 * The user can edit a object property if it is not restricted and if it is
	 * about him.
	 */
	private PolicyDecision isAuthorizedForObjectPropertyAction(
			List<String> userUris, AbstractObjectPropertyAction action) {
		String subject = action.getUriOfSubject();
		String predicate = action.getUriOfPredicate();
		String object = action.getUriOfObject();

		if (!restrictor.canModifyResource(subject)) {
			return cantModifyResource(subject);
		}
		if (!restrictor.canModifyPredicate(predicate)) {
			return cantModifyPredicate(predicate);
		}
		if (!restrictor.canModifyResource(object)) {
			return cantModifyResource(object);
		}

		if (userCanEditAsSubjectOrObjectOfStmt(userUris, subject, object)) {
			return authorizedDecision("User is subject or object of statement.");
		} else {
			return userNotAuthorizedToStatement();
		}
	}

	/**
	 * The user can edit a data property if it is not restricted and if it is
	 * about him.
	 */
	private PolicyDecision isAuthorizedForDataPropertyAction(
			List<String> userUris, AbstractDataPropertyAction action) {
		String subject = action.getSubjectUri();
		String predicate = action.getPredicateUri();

		if (!restrictor.canModifyResource(subject)) {
			return cantModifyResource(subject);
		}
		if (!restrictor.canModifyPredicate(predicate)) {
			return cantModifyPredicate(predicate);
		}

		if (userCanEditAsSubjectOfStmt(userUris, subject)) {
			return authorizedDecision("User is subject of statement.");
		} else {
			return userNotAuthorizedToStatement();
		}
	}

	/**
	 * The user can add or remove resources if they are not restricted.
	 */
	private PolicyDecision isAuthorizedForResourceAction(
			AbstractResourceAction action) {
		String uri = action.getSubjectUri();
		if (!restrictor.canModifyResource(uri)) {
			return cantModifyResource(uri);
		} else {
			return authorizedDecision("May add/remove resource.");
		}
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private boolean userCanEditAsSubjectOfStmt(List<String> userUris,
			String subject) {
		for (String userUri : userUris) {
			if (userUri.equals(subject)) {
				return true;
			}
		}
		return false;
	}

	private boolean userCanEditAsSubjectOrObjectOfStmt(List<String> userUris,
			String subject, String object) {
		for (String userUri : userUris) {
			if (userUri.equals(subject)) {
				return true;
			}
			if (userUri.equals(object)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SelfEditingPolicy " + hashCode() + "[" + restrictor + "]";
	}

}
