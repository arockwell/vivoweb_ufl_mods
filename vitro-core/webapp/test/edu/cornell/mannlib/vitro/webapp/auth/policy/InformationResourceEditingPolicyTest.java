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

import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization.AUTHORIZED;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization.INCONCLUSIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AddResource;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * TODO
 */
public class InformationResourceEditingPolicyTest extends AbstractTestClass {
	private static final Log log = LogFactory
			.getLog(InformationResourceEditingPolicyTest.class);

	/** Can edit properties or resources in this namespace. */
	private static final String NS_PERMITTED = "http://vivo.mydomain.edu/individual/";

	/** Can't edit properties or resources in this namespace. */
	private static final String NS_RESTRICTED = VitroVocabulary.vitroURI;

	/** The resource type is not checked by the admin restrictor. */
	private static final String RESOURCE_TYPE = NS_RESTRICTED + "funkyType";

	private static final String URI_PERMITTED_RESOURCE = NS_PERMITTED
			+ "permittedResource";
	private static final String URI_RESTRICTED_RESOURCE = NS_RESTRICTED
			+ "restrictedResource";

	private static final String URI_PERMITTED_PREDICATE = NS_PERMITTED
			+ "permittedPredicate";
	private static final String URI_RESTRICTED_PREDICATE = NS_RESTRICTED
			+ "restrictedPredicate";

	/**
	 * Where the model statements are stored for this test.
	 */
	private static final String N3_DATA_FILENAME = "resources/InformationResourceEditingPolicyTest.n3";

	/**
	 * These URIs must match the data in the N3 file.
	 */
	private static final String URI_BOZO = NS_PERMITTED + "bozo";
	private static final String URI_JOE = NS_PERMITTED + "joe";
	private static final String URI_NOBODY_WROTE_IT = NS_PERMITTED
			+ "nobodyWroteIt";
	private static final String URI_BOZO_WROTE_IT = NS_PERMITTED
			+ "bozoWroteIt";
	private static final String URI_BOZO_EDITED_IT = NS_PERMITTED
			+ "bozoEditedIt";
	private static final String URI_JOE_WROTE_IT = NS_PERMITTED + "joeWroteIt";
	private static final String URI_JOE_EDITED_IT = NS_PERMITTED
			+ "joeEditedIt";

	private static OntModel ontModel;

	@BeforeClass
	public static void setupModel() throws IOException {
		InputStream stream = InformationResourceEditingPolicyTest.class
				.getResourceAsStream(N3_DATA_FILENAME);
		Model model = ModelFactory.createDefaultModel();
		model.read(stream, null, "N3");
		stream.close();

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,
				model);
		ontModel.prepare();
		dumpModel();
	}

	private InformationResourceEditingPolicy policy;
	private RequestedAction action;

	@Before
	public void setupPolicy() {
		AdministrativeUriRestrictor restrictor = new AdministrativeUriRestrictor(
				null, null, null, null);
		policy = new InformationResourceEditingPolicy(ontModel, restrictor);
	}

	private IdentifierBundle idNobody;
	private IdentifierBundle idBozo;
	private IdentifierBundle idJoe;
	private IdentifierBundle idBozoAndJoe;

	@Before
	public void setupIdBundles() {
		idNobody = new ArrayIdentifierBundle();

		idBozo = new ArrayIdentifierBundle();
		idBozo.add(makeSelfEditingId(URI_BOZO));

		idJoe = new ArrayIdentifierBundle();
		idJoe.add(makeSelfEditingId(URI_JOE));

		idBozoAndJoe = new ArrayIdentifierBundle();
		idBozoAndJoe.add(makeSelfEditingId(URI_BOZO));
		idBozoAndJoe.add(makeSelfEditingId(URI_JOE));
	}

	@Before
	public void setLogging() {
		// setLoggerLevel(this.getClass(), Level.DEBUG);
		// setLoggerLevel(InformationResourceEditingPolicy.class, Level.DEBUG);
	}

	// ----------------------------------------------------------------------
	// the tests
	// ----------------------------------------------------------------------

	@Test
	public void whoIsNull() {
		action = new AddResource(RESOURCE_TYPE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(null, action));
	}

	@Test
	public void whatIsNull() {
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, null));
	}

	@Test
	public void notSelfEditing() {
		action = new AddResource(RESOURCE_TYPE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idNobody, action));
	}

	@Test
	public void requestedActionOutOfScope() {
		action = new ServerStatus();
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void dataPropSubjectIsRestricted() {
		action = new AddDataPropStmt(URI_RESTRICTED_RESOURCE,
				URI_PERMITTED_PREDICATE, "junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void dataPropPredicateIsRestricted() {
		action = new AddDataPropStmt(URI_JOE_EDITED_IT,
				URI_RESTRICTED_PREDICATE, "junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void dataPropSubjectIsNotInfoResource() {
		action = new AddDataPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, "junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void dataPropSubjectIsInfoResourceButNobodyIsSelfEditing() {
		action = new AddDataPropStmt(URI_JOE_WROTE_IT, URI_PERMITTED_PREDICATE,
				"junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idNobody, action));
	}

	@Test
	public void dataPropSubjectIsInfoResourceButNoAuthorsOrEditors() {
		action = new AddDataPropStmt(URI_NOBODY_WROTE_IT,
				URI_PERMITTED_PREDICATE, "junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void dataPropSubjectIsInfoResourceButWrongAuthor() {
		action = new AddDataPropStmt(URI_BOZO_WROTE_IT,
				URI_PERMITTED_PREDICATE, "junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void dataPropSubjectIsInfoResourceButWrongEditor() {
		action = new AddDataPropStmt(URI_BOZO_EDITED_IT,
				URI_PERMITTED_PREDICATE, "junk", null, null);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void dataPropSubjectIsInfoResourceWithSelfEditingAuthor() {
		action = new AddDataPropStmt(URI_JOE_WROTE_IT, URI_PERMITTED_PREDICATE,
				"junk", null, null);
		assertDecision(AUTHORIZED, policy.isAuthorized(idJoe, action));
		assertDecision(AUTHORIZED, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void dataPropSubjectIsInfoResourceWithSelfEditingEditor() {
		action = new AddDataPropStmt(URI_JOE_EDITED_IT,
				URI_PERMITTED_PREDICATE, "junk", null, null);
		assertDecision(AUTHORIZED, policy.isAuthorized(idJoe, action));
		assertDecision(AUTHORIZED, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void objectPropSubjectIsRestricted() {
		action = new AddObjectPropStmt(URI_RESTRICTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_JOE_EDITED_IT);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropPredicateIsRestricted() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_RESTRICTED_PREDICATE, URI_JOE_EDITED_IT);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropObjectIsRestricted() {
		action = new AddObjectPropStmt(URI_JOE_EDITED_IT,
				URI_PERMITTED_PREDICATE, URI_RESTRICTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropNeitherSubjectOrObjectIsInfoResource() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropSubjectIsInfoResourceButNobodyIsSelfEditing() {
		action = new AddObjectPropStmt(URI_JOE_EDITED_IT,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idNobody, action));
	}

	@Test
	public void objectPropSubjectIsInfoResourceButNoAuthorsOrEditors() {
		action = new AddObjectPropStmt(URI_NOBODY_WROTE_IT,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void objectPropSubjectIsInfoResourceButWrongAuthor() {
		action = new AddObjectPropStmt(URI_BOZO_WROTE_IT,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropSubjectIsInfoResourceButWrongEditor() {
		action = new AddObjectPropStmt(URI_BOZO_EDITED_IT,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropSubjectIsInfoResourceWithSelfEditingAuthor() {
		action = new AddObjectPropStmt(URI_JOE_WROTE_IT,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(idJoe, action));
		assertDecision(AUTHORIZED, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void objectPropSubjectIsInfoResourceWithSelfEditingEditor() {
		action = new AddObjectPropStmt(URI_JOE_EDITED_IT,
				URI_PERMITTED_PREDICATE, URI_PERMITTED_RESOURCE);
		assertDecision(AUTHORIZED, policy.isAuthorized(idJoe, action));
		assertDecision(AUTHORIZED, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void objectPropObjectIsInfoResourcebutNobodyIsSelfEditing() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_JOE_EDITED_IT);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idNobody, action));
	}

	@Test
	public void objectPropObjectIsInfoResourceButNoAuthorsOrEditors() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_NOBODY_WROTE_IT);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void objectPropObjectIsInfoResourceButWrongAuthor() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_BOZO_WROTE_IT);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropObjectIsInfoResourceButWrongEditor() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_BOZO_EDITED_IT);
		assertDecision(INCONCLUSIVE, policy.isAuthorized(idJoe, action));
	}

	@Test
	public void objectPropObjectIsInfoResourceWithSelfEditingAuthor() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_JOE_WROTE_IT);
		assertDecision(AUTHORIZED, policy.isAuthorized(idJoe, action));
		assertDecision(AUTHORIZED, policy.isAuthorized(idBozoAndJoe, action));
	}

	@Test
	public void objectPropObjectIsInfoResourceWithSelfEditingEditor() {
		action = new AddObjectPropStmt(URI_PERMITTED_RESOURCE,
				URI_PERMITTED_PREDICATE, URI_JOE_EDITED_IT);
		assertDecision(AUTHORIZED, policy.isAuthorized(idJoe, action));
		assertDecision(AUTHORIZED, policy.isAuthorized(idBozoAndJoe, action));
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private SelfEditing makeSelfEditingId(String uri) {
		IndividualImpl ind = new IndividualImpl();
		ind.setURI(uri);
		SelfEditing selfEditing = new SelfEditing(ind,
				SelfEditingIdentifierFactory.NOT_BLACKLISTED);
		return selfEditing;
	}

	private void assertDecision(Authorization expected, PolicyDecision decision) {
		log.debug("Decision is: " + decision);
		assertNotNull("decision exists", decision);
		assertEquals("authorization", expected, decision.getAuthorized());
	}

	private static void dumpModel() {
		if (log.isDebugEnabled()) {
			StmtIterator stmtIt = ontModel.listStatements();
			while (stmtIt.hasNext()) {
				Statement stmt = stmtIt.next();
				log.debug("stmt: " + stmt);
			}
		}
	}
}
