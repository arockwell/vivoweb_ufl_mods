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

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * This controller receives Ajax requests for reordering a list of individuals. 
 * Parameters:
 * predicate: the data property used for ranking
 * individuals: an ordered list of individuals to be ranked
 * @author rjy7
 *
 */
public class ReorderController extends PrimitiveRdfEdit {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ReorderController.class);

    private static String RANK_PREDICATE_PARAMETER_NAME = "predicate";
    private static String INDIVIDUAL_PREDICATE_PARAMETER_NAME = "individuals";
    
    @Override
    protected void processRequest(VitroRequest vreq, HttpServletResponse response) {

        String errorMsg = null;
        String rankPredicate = vreq.getParameter(RANK_PREDICATE_PARAMETER_NAME);
        if (rankPredicate == null) {
            errorMsg = "No rank parameter specified";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_BAD_REQUEST );
            return;
        }

        String[] individualUris = vreq.getParameterValues(INDIVIDUAL_PREDICATE_PARAMETER_NAME);
        if (individualUris == null || individualUris.length == 0) {
            errorMsg = "No individuals specified";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_BAD_REQUEST);  
            return;
        }

        WebappDaoFactory wadf = vreq.getWebappDaoFactory();        
        if( vreq.getWebappDaoFactory() == null) {
            errorMsg = "No WebappDaoFactory available";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }  

        DataPropertyStatementDao dpsDao = wadf.getDataPropertyStatementDao();  
        if( dpsDao == null) {
            errorMsg = "No DataPropertyStatementDao available";
            log.error(errorMsg);
            doError(response, errorMsg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }  

        //check permissions     
        //TODO: (bdc34)This is not yet implemented, must check the IDs against the policies for permissons before doing an edit!
        // rjy7 This should be inherited from the superclass
        boolean hasPermission = true;        
        if( !hasPermission ){
            //if not okay, send error message
            doError(response,"Insufficent permissions", HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        // This may not be the most efficient way. Should we instead build up a Model of retractions and additions, so
        // we only hit the database once?
        int counter = 1;
        for (String individualUri : individualUris) {           
            // Retract all existing rank statements for this individual
            dpsDao.deleteDataPropertyStatementsForIndividualByDataProperty(individualUri, rankPredicate);
        
            // Then add the new rank statement for this individual
            // insertNewDataPropertyStatement will insert the rangeDatatype of the property, so we don't need to set that here.
            dpsDao.insertNewDataPropertyStatement(new DataPropertyStatementImpl(individualUri, rankPredicate, String.valueOf(counter)));
            
            counter++;
        }

//        // Alternate implementation: build up a model of additions and retractions, use only one db write
//        DataPropertyDao dpDao = wadf.getDataPropertyDao();  
//        if( dpDao == null) {
//            errorMsg = "No DataPropertyDao available";
//            log.error(errorMsg);
//            doError(response, errorMsg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            return;
//        }      
//        DataProperty dp = dpDao.getDataPropertyByURI(rankPredicate);
//        String rangeDatatypeUri = dp.getRangeDatatypeURI();
//        
//        Model additions = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();        
//        Model retractions = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();        
//        OntModel model = getQueryModel(vreq);
//        
//        int counter = 1;
//        for (String individualUri : individualUris) {
//            Resource resource = model.createResource(individualUri);
//            Property property = model.getProperty(rankPredicate);
//            // Remove all existing rank statements for this individual and predicate
//            StmtIterator stmts = model.listStatements(resource, property, (RDFNode) null);  
//            retractions.add(stmts);
//            
//            Literal lit = null;
//            if (rangeDatatypeUri != null) {
//                lit = model.createTypedLiteral(counter, rangeDatatypeUri);
//            } else {
//                lit = model.createLiteral(String.valueOf(counter));
//                // or: lit = model.createTypedLiteral(count) ?
//            }
//            Statement stmt = model.createStatement(resource, property, lit);
//            additions.add(stmt);
//            
//            counter++;
//        }
//        ServletContext sc = getServletContext();
//        String editorUri = EditN3Utils.getEditorUri(vreq, vreq.getSession(false), sc);           
//        try {
//            processChanges( additions, retractions, getWriteModel(vreq), getQueryModel(vreq), editorUri);
//        } catch (Exception e) {
//            doError(response,e.getMessage(),HttpStatus.SC_INTERNAL_SERVER_ERROR);
//            return;
//        }          
//        // end alternate implementation
        
        response.setStatus(HttpServletResponse.SC_OK);
        
    }
    
    protected void doError(HttpServletResponse response, String errorMsg, int httpstatus) {
        super.doError(response, "Error: " + errorMsg, httpstatus);
    }
    
}
