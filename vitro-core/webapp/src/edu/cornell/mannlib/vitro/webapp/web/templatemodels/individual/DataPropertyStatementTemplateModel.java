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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash;

public class DataPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(DataPropertyStatementTemplateModel.class); 
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    private String value = null;
    
    // Used for editing
    private String dataPropHash = null;

    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            Literal literal, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper);
        
        this.value = literal.getLexicalForm();
        setEditAccess(literal, policyHelper);

    }
    
    /*
     * This method handles the special case where we are creating a DataPropertyStatementTemplateModel outside the GroupedPropertyList.
     * Specifically, it allows rdfs:label to be treated like a data property statement and thus have editing links. It is not possible
     * to handle rdfs:label like vitro links and vitroPublic image, because it is not possible to construct a DataProperty from
     * rdfs:label. 
     */
    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper);
        
        DataPropertyStatementDao dpsDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<Literal> literals = dpsDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);
        
        // Make sure the subject has a value for this property 
        if (literals.size() > 0) {
            Literal literal = literals.get(0);
            value = literal.getLexicalForm();
            setEditAccess(literal, policyHelper);
        } 
    }
    
    protected void setValue(String value) {
        this.value = value;
    }
    
    private void setEditAccess(Literal value, EditingPolicyHelper policyHelper) {
        
        if (policyHelper != null) { // we're editing         
            DataPropertyStatement dps = new DataPropertyStatementImpl(subjectUri, propertyUri, value.getLexicalForm());
            // Language and datatype are needed to get the correct hash value
            dps.setLanguage(value.getLanguage());
            dps.setDatatypeURI(value.getDatatypeURI());
            this.dataPropHash = String.valueOf(RdfLiteralHash.makeRdfLiteralHash(dps));
            
            // Determine whether the statement can be edited
            RequestedAction action = new EditDataPropStmt(dps);
            if (policyHelper.isAuthorizedAction(action)) {
                markEditable();
            }      
            
            // Determine whether the statement can be deleted
            // Hack for rdfs:label - the policy doesn't prevent deletion
            if ( ! propertyUri.equals(VitroVocabulary.LABEL) ) {
                action = new DropDataPropStmt(dps);
                if (policyHelper.isAuthorizedAction(action)) {
                    markDeletable();
                } 
            }
        }        
    }
    
    
    /* Access methods for templates */
    
    public String getValue() {
        return value;
    }
    
    public String getEditUrl() {
        String editUrl = "";
        if (isEditable()) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "datapropKey", dataPropHash);
            if (! isDeletable()) {
                params.put("deleteProhibited", "prohibited");
            }
            editUrl = UrlBuilder.getUrl(EDIT_PATH, params);    
        }
        return editUrl;
    }
    
    public String getDeleteUrl() {
        String deleteUrl = "";
        if (isDeletable()) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "datapropKey", dataPropHash,
                    "cmd", "delete");
            deleteUrl = UrlBuilder.getUrl(EDIT_PATH, params);
        }
        return deleteUrl;
    }

}
