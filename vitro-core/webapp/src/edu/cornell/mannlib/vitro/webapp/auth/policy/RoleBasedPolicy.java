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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.DropResource;

/**
 * Policy that mimics the authorization roles of the old system.  So each
 * principle that is to be authorized needs to be associated with an individual
 * in the model and the individual needs to have a ObjectPropertyStatement between it and 
 * a authorization role.
 * 
 * ex.
 * 
 * vivo:indvidual23323 vivo:cornellNetId "bdc34".
 * vivo:indvidual22323 vitro:authRole <role://50>.
 * 
 * Notice that this policy doesn't need setup because it will look for
 * an authorization role identifier in the model.
 *
 * @author bdc34
 *
 */
public class RoleBasedPolicy extends DefaultInconclusivePolicy  implements PolicyIface {
    private static final Log log = LogFactory.getLog(RoleBasedPolicy.class.getName());

    /**
     * What is the minimum AuthRole needed to perform a given action?
     */
    private static Map<Class<?>,AuthRole> actionToMinRole = new HashMap<Class<?>,AuthRole>();
    static{
        //anybody actions
        //view resources?

        //user actions
        //allow netid authenticated people to do things?

        //edit actions
        actionToMinRole.put(AddDataPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(AddObjectPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(AddResource.class, AuthRole.EDITOR);
        actionToMinRole.put(DropDataPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(DropObjectPropStmt.class, AuthRole.EDITOR);
        actionToMinRole.put(DropResource.class, AuthRole.EDITOR);
        actionToMinRole.put(UploadFile.class, AuthRole.EDITOR);
        actionToMinRole.put(ServerStatus.class, AuthRole.EDITOR);
        actionToMinRole.put(UpdateTextIndex.class, AuthRole.EDITOR);
        //curator actions
        actionToMinRole.put(DefineDataProperty.class, AuthRole.CURATOR);
        actionToMinRole.put(DefineObjectProperty.class, AuthRole.CURATOR);
        actionToMinRole.put(CreateOwlClass.class, AuthRole.CURATOR);
        actionToMinRole.put(RemoveOwlClass.class, AuthRole.CURATOR);
        //dba actions (dba role is allowed to do anything)
        actionToMinRole.put(AddNewUser.class, AuthRole.DBA);
        actionToMinRole.put(LoadOntology.class, AuthRole.DBA);
        actionToMinRole.put(RemoveUser.class, AuthRole.DBA);
        actionToMinRole.put(RebuildTextIndex.class, AuthRole.DBA);
    };

    public PolicyDecision isAuthorized(IdentifierBundle whomToAuth, RequestedAction whatToAuth) {
        if( whomToAuth == null ){
            log.error( "null was passed as whoToAuth" );
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"null was passed as whoToAuth");
        }
        if( whatToAuth == null ){
            log.error("null was passed as whatToAuth");
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,"null was passed as whatToAuth");
        }

        //dba can do anything
        if( AuthRole.DBA.thisRoleOrGreater(whomToAuth))
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"in DBA role");

        //We need to find the class of the RequestedAction since that
        //encodes what type of action is being requested.
        Class<?> requesetClass = whatToAuth.getClass();
        AuthRole minmumRoleForAction = actionToMinRole.get(requesetClass);

        if( minmumRoleForAction == null ){
            String msg = "no minimum role found for action " + whatToAuth.getClass().getName();
            log.error(msg);
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,msg);
        }

        if( minmumRoleForAction.thisRoleOrGreater(whomToAuth) )
            return new BasicPolicyDecision(Authorization.AUTHORIZED,"authorized for role");
        else
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,"not authorized for role");
    }

    @Override
	public String toString() {
    	return "RoleBasedPolicy";
	}


	/********************** Roles *****************************************/
    public static enum AuthRole implements Identifier {
        ANYBODY( "role:/0" ,LoginStatusBean.ANYBODY),
        USER(    "role:/1" ,LoginStatusBean.NON_EDITOR),
        EDITOR(  "role:/4" ,LoginStatusBean.EDITOR),
        CURATOR( "role:/5" ,LoginStatusBean.CURATOR),
        DBA(     "role:/50",LoginStatusBean.DBA);

        private final String roleUri;
        private final int level;

        AuthRole(String uri, int level) {
            this.roleUri = uri;
            this.level = level;
        }

        public String roleUri()   { return roleUri; }
        public int level() {return level;}

        /** returns null if not found */
        public static AuthRole convertUriToAuthRole(String uri){
            for( AuthRole role : AuthRole.values()){
                if( role.roleUri().equals( uri ))
                    return role;
            }
            return null;
        }

        public boolean thisRoleOrGreater(IdentifierBundle ibundle){
            if( ibundle == null )
                return false;
            for(Object obj : ibundle){
                if( obj instanceof AuthRole &&
                    ((AuthRole)obj).level() >= this.level())
                    return true;
            }
            return false;
        }

		@Override
		public String toString() {
			return "AuthRole: " + name();
		}
    }/* end of enum AuthRole */
}/* end of class RoleBasedPolicy */
