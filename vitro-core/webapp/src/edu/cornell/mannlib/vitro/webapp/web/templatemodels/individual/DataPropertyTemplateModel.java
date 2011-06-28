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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;

public class DataPropertyTemplateModel extends PropertyTemplateModel {

    private static final Log log = LogFactory.getLog(DataPropertyTemplateModel.class);  
    
    private static final String TYPE = "data";
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    private List<DataPropertyStatementTemplateModel> statements;

    DataPropertyTemplateModel(DataProperty dp, Individual subject, VitroRequest vreq, 
            EditingPolicyHelper policyHelper, List<DataProperty> populatedDataPropertyList) {
        
        super(dp, subject, policyHelper);
        setName(dp.getPublicName());
        statements = new ArrayList<DataPropertyStatementTemplateModel>();
        
        // If the property is populated, get the data property statements via a sparql query
        if (populatedDataPropertyList.contains(dp)) {
            log.debug("Getting data for populated data property " + getUri());
            DataPropertyStatementDao dpDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
            List<Literal> values = dpDao.getDataPropertyValuesForIndividualByProperty(subject, dp);            
            for (Literal value : values) {
                statements.add(new DataPropertyStatementTemplateModel(subjectUri, propertyUri, value, policyHelper));
            }
        } else {
            log.debug("Data property " + getUri() + " is unpopulated.");
        }
        
        // Determine whether a new statement can be added
        if (policyHelper != null) {
            // If the display limit has already been reached, we can't add a new statement
            int displayLimit = dp.getDisplayLimit();
            // Display limit of -1 (default value for new property) means no display limit
            if ( (displayLimit < 0) || (displayLimit > statements.size()) ) {
                RequestedAction action = new AddDataPropStmt(subjectUri, propertyUri,RequestActionConstants.SOME_LITERAL, null, null);
                if (policyHelper.isAuthorizedAction(action)) {
                    addAccess = true;
                }
            }
        }
    }
    
    @Override
    protected boolean isEmpty() {
        return statements.isEmpty();
    }
    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }

    @Override
    public String getAddUrl() {
        String addUrl = "";
        if (addAccess) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri);
            addUrl = UrlBuilder.getUrl(EDIT_PATH, params);       
        }
        return addUrl;
    }
    
    public List<DataPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
    public DataPropertyStatementTemplateModel getFirst() {
        return ( (statements == null || statements.isEmpty()) ) ? null : statements.get(0);
    }
    
    public String getFirstValue() {
        DataPropertyStatementTemplateModel first = getFirst();
        return first == null ? null : first.getValue();
    }
    
}
