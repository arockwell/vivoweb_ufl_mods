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

package edu.cornell.mannlib.vitro.webapp.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.flags.AuthFlag;

public class BaseResourceBean implements ResourceBean {
	
	private static final Log log = LogFactory.getLog(BaseResourceBean.class.getName());
    protected String URI          = null;
    protected String namespace    = null;
    protected String localName    = null;
    protected String localNameWithPrefix = null;
    protected String pickListName = null;
    
    // these will be phased in and used in the filters Brian C. has been setting up,
    // with hiddenFromDisplay to control the level at which any class, individual, object property, or data property is displayed
    // and prohibitedFromEditing to control when a control for editing is made available
    protected RoleLevel hiddenFromDisplayBelowRoleLevel = null;
    //protected RoleLevel prohibitedFromCreateBelowRoleLevel = null;
    protected RoleLevel prohibitedFromUpdateBelowRoleLevel = null;
    //protected RoleLevel prohibitedFromDeleteBelowRoleLevel = null;
    
    public enum RoleLevel { PUBLIC("http://vitro.mannlib.cornell.edu/ns/vitro/role#public","public","public"),
                            SELF("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor","self-authenticated","self"),
                            EDITOR("http://vitro.mannlib.cornell.edu/ns/vitro/role#editor","editor,curator,dbAdmin","editor"),
                            CURATOR("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator","curator,dbAdmin","curator"),
                            DB_ADMIN("http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin","dbAdmin only","DBA"),
                            NOBODY("http://vitro.mannlib.cornell.edu/ns/vitro/role#nobody","nobody","nobody");
    
        private final String uri;
        private final String label;
        private final String shorthand;
        
        RoleLevel(String uriStr,String labelStr, String shortStr) {
            this.uri = uriStr;
            this.label = labelStr;
            this.shorthand = shortStr;
        }
        
        public String getURI() {
            return uri;
        }
        
        public String getLabel() {
            return label;
        }

        public String getShorthand() {
            return shorthand;
        }
        
        public static RoleLevel getRoleByUri(String uri2) {
            if( uri2 == null )
                return RoleLevel.values()[0];
           
            for( RoleLevel role : RoleLevel.values() ){
                if( role.uri.equals( uri2 ) )
                    return role;
            }
            return RoleLevel.values()[0];            
        }
        
        public static RoleLevel getRoleFromAuth(AuthFlag ar){
            int level = ar.getUserSecurityLevel();
            if( level == LoginFormBean.ANYBODY)    // 0
                return PUBLIC;
            if( level == LoginFormBean.NON_EDITOR) // 1
                return PUBLIC; // no correspondence with self-editing, which does not authorize through the LoginFormBean
            if( level == LoginFormBean.EDITOR )    // 4
                return EDITOR;
            if( level == LoginFormBean.CURATOR )   // 5
                return CURATOR;
            if( level == LoginFormBean.DBA )       // 50
                return DB_ADMIN;
            else
                return null;
        }
    }

    public boolean isAnonymous() {        
    	return (this.URI==null || VitroVocabulary.PSEUDO_BNODE_NS.equals(this.getNamespace()));
    }
    
    public String getURI() {
        return URI;
    }
    public void setURI(String URI) {  
        if( this.localName != null || this.namespace != null)
            buildLocalAndNS(URI);
        else
            this.URI = URI;
    }

    private void buildLocalAndNS(String URI) {
        if (URI == null) {
            this.URI = null;
            this.namespace = null;
            this.localName = null;
        } else {
            this.URI = URI;
            try {
                URIImpl uri = new URIImpl(URI);
                this.namespace = uri.getNamespace();
                this.localName = uri.getLocalName();
            } catch (Exception e) {
                log.error("Exception processing URI "+URI);
                e.printStackTrace();
            }
        }
    }
    
    public String getNamespace() {
        if( namespace == null && this.URI != null)
            buildLocalAndNS( this.URI);        
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null && localName != null ) {
            this.URI = namespace + localName;
        }
    }

    public String getLocalName() {
        if( localName == null && this.URI != null)
            buildLocalAndNS(this.URI);        
        return localName;
    }
    public void setLocalName(String localName) {
        this.localName = localName;
        if (namespace != null && localName != null) {
            this.URI = namespace + localName;
        }
    }

    public String getLocalNameWithPrefix() {
        return localNameWithPrefix==null ? getLocalName()==null ? 
                (URI==null ? "(no name)" : URI ): getLocalName() : localNameWithPrefix;
    }
    public void setLocalNameWithPrefix(String prefixedLocalName) {
        this.localNameWithPrefix = prefixedLocalName;
    }
    
    public String getPickListName() {
        return pickListName==null ? getLocalName()==null ? 
                (URI==null ? "(no name)" : URI ): getLocalName() : pickListName;
    }
    public void setPickListName(String pickListName) {
        this.pickListName = pickListName;
    }
    
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return hiddenFromDisplayBelowRoleLevel;
    }
    
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) {
        hiddenFromDisplayBelowRoleLevel = eR;
    }
    
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        hiddenFromDisplayBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }

    /*
    public RoleLevel getProhibitedFromCreateBelowRoleLevel() {
        return prohibitedFromCreateBelowRoleLevel;
    }
    
    public void setProhibitedFromCreateBelowRoleLevel(RoleLevel eR) {
        prohibitedFromCreateBelowRoleLevel = eR;
    }
    
    public void setProhibitedFromCreateBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromCreateBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    */
    
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return prohibitedFromUpdateBelowRoleLevel;
    }
    
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) {
        prohibitedFromUpdateBelowRoleLevel = eR;
    }
    
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromUpdateBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    /*
    public RoleLevel getProhibitedFromDeleteBelowRoleLevel() {
        return prohibitedFromDeleteBelowRoleLevel;
    }
    
    public void setProhibitedFromDeleteBelowRoleLevel(RoleLevel eR) {
        prohibitedFromDeleteBelowRoleLevel = eR;
    }
    
    public void setProhibitedFromDeleteBelowRoleLevelUsingRoleUri(String roleUri) {
        prohibitedFromDeleteBelowRoleLevel = BaseResourceBean.RoleLevel.getRoleByUri(roleUri);
    }
    */

	@Override
	public boolean equals(Object obj) {
		if(obj == null ) 
			return false;
		else if (obj instanceof BaseResourceBean ){
			String thisURI = this.getURI();
			String thatURI = ((BaseResourceBean)obj).getURI();
			if( thisURI != null && thatURI != null ){
				return thisURI.equals(thatURI);
			}
		}
		return obj.hashCode() == this.hashCode();			
	}
	
	@Override
	public int hashCode() {
		if( getURI() != null )
			return getURI().hashCode();
		else
			return super.hashCode();
	}

    
}
