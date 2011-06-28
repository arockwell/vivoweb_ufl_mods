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

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;

public class VivoPolicy extends DefaultInconclusivePolicy{

	private static String AUTHORSHIP_FROM_PUB =  "http://vivoweb.org/ontology/core#informationResourceInAuthorship"; 
	private static String AUTHORSHIP_FROM_PERSON =  "http://vivoweb.org/ontology/core#authorInAuthorship";
	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {

		if( whatToAuth instanceof DropObjectPropStmt ){
			DropObjectPropStmt dops = (DropObjectPropStmt)whatToAuth;
			
			/* Do not offer the user the option to delete so they will use the custom form instead */ 
			/* see issue NIHVIVO-739 */
			if( AUTHORSHIP_FROM_PUB.equals( dops.getUriOfPredicate() )) {
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
						"Use the custom edit form for core:informationResourceInAuthorship");
			}
			
			if( AUTHORSHIP_FROM_PERSON.equals( dops.getUriOfPredicate() )) {
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
						"Use the custom edit form for core:authorInAuthorship");
			}
			 
			if( "http://vivoweb.org/ontology/core#linkedAuthor".equals( dops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}
			
			if( "http://vivoweb.org/ontology/core#linkedInformationResource".equals( dops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}			
		}
		if( whatToAuth instanceof AddObjectPropStmt ){
			AddObjectPropStmt aops = (AddObjectPropStmt)whatToAuth;
			if( "http://vivoweb.org/ontology/core#linkedAuthor".equals( aops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}
			
			if( "http://vivoweb.org/ontology/core#linkedInformationResource".equals( aops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}	
		}
		
		return super.isAuthorized(whoToAuth, whatToAuth);						
	}

	
}
