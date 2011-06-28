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

package edu.cornell.mannlib.vitro.webapp.auth;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RequestPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class AuthorizationHelper {

    private static final Log log = LogFactory.getLog(AuthorizationHelper.class);
    
    private VitroRequest vreq;
    
    public AuthorizationHelper(VitroRequest vreq) {
        this.vreq = vreq;
    }

    public boolean isAuthorizedForRequestedAction(RequestedAction action) {
        PolicyIface policy = getPolicies();
        PolicyDecision dec = policy.isAuthorized(getIdentifiers(), action);
        if (dec != null && dec.getAuthorized() == Authorization.AUTHORIZED) {
            log.debug("Authorized because self-editing.");
            return true;
        } else {
            log.debug("Not Authorized even though self-editing: "
                    + ((dec == null) ? "null" : dec.getMessage() + ", "
                            + dec.getDebuggingInfo()));
            return false;
        }
    }

    /**
     * Get the policy from the request, or from the servlet context.
     */
    private PolicyIface getPolicies() {
        ServletContext servletContext = vreq.getSession().getServletContext();

        PolicyIface policy = RequestPolicyList.getPolicies(vreq);
        if (isEmptyPolicy(policy)) {
            policy = ServletPolicyList.getPolicies(servletContext);
            if (isEmptyPolicy(policy)) {
                log.error("No policy found in request at "
                        + RequestPolicyList.POLICY_LIST);
                policy = new PolicyList();
            }
        }

        return policy;
    }

    /**
     * Is there actually a policy here?
     */
    private boolean isEmptyPolicy(PolicyIface policy) {
        return policy == null
                || (policy instanceof PolicyList && ((PolicyList) policy)
                        .size() == 0);
    }

    private IdentifierBundle getIdentifiers() {
        HttpSession session = vreq.getSession();
        ServletContext context = session.getServletContext();
        IdentifierBundle ids = ServletIdentifierBundleFactory
                .getIdBundleForRequest(vreq, session, context);
        if (ids == null) {
            return new ArrayIdentifierBundle();
        } else {
            return ids;
        }
    }

}
