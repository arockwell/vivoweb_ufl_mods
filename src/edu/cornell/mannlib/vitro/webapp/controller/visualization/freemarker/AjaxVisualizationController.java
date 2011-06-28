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

package edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VisConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.VisualizationRequestHandler;
import freemarker.template.Configuration;

/**
 * Services a visualization request. This will return a simple error message and a 501 if
 * there is no jena Model.
 *
 * @author cdtank
 */
@SuppressWarnings("serial")
public class AjaxVisualizationController extends FreemarkerHttpServlet {

	public static final String URL_ENCODING_SCHEME = "UTF-8";

	private static final Log log = LogFactory.getLog(AjaxVisualizationController.class.getName());
	
    protected static final Syntax SYNTAX = Syntax.syntaxARQ;
   
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    		throws IOException, ServletException {
    
		VitroRequest vreq = new VitroRequest(request);
		
		Object ajaxResponse = processAjaxRequest(vreq);
		
		if (ajaxResponse instanceof TemplateResponseValues) {
			
			Configuration config = getConfig(vreq);
			TemplateResponseValues trv = (TemplateResponseValues) ajaxResponse;
			writeTemplate(trv.getTemplateName(), trv.getMap(), config, request, response);
			
		} else {
			response.getWriter().write(ajaxResponse.toString());
		}
	}
    
    private Object processAjaxRequest(VitroRequest vreq) {
    	/*
    	 * Based on the query parameters passed via URI get the appropriate visualization 
    	 * request handler.
    	 * */
    	VisualizationRequestHandler visRequestHandler = 
    			getVisualizationRequestHandler(vreq);
    	
    	if (visRequestHandler != null) {
    	
    		/*
        	 * Pass the query to the selected visualization request handler & render the visualization.
        	 * Since the visualization content is directly added to the response object we are side-
        	 * effecting this method.
        	 * */
            return renderVisualization(vreq, visRequestHandler);
            
    	} else {
    		
    		return UtilityFunctions.handleMalformedParameters("Visualization Query Error", 
    														  "Inappropriate query parameters were submitted.", 
    														  vreq);
    		
    	}
    }


	private Object renderVisualization(VitroRequest vitroRequest,
									 VisualizationRequestHandler visRequestHandler) {
		
		Model model = vitroRequest.getJenaOntModel(); // getModel()
        if (model == null) {
            
            String errorMessage = "This service is not supporeted by the current " 
            			+ "webapp configuration. A jena model is required in the " 
            			+ "servlet context.";

            log.error(errorMessage);
            
            return UtilityFunctions.handleMalformedParameters("Visualization Query Error", 
            												  errorMessage, 
            												  vitroRequest);
			
        }
		
		DataSource dataSource = setupJENADataSource(model, vitroRequest);
        
		if (dataSource != null && visRequestHandler != null) {
        	
        	try {
				return visRequestHandler.generateAjaxVisualization(vitroRequest, 
														log, 
														dataSource);
			} catch (MalformedQueryParametersException e) {
				return UtilityFunctions.handleMalformedParameters(
						"Ajax Visualization Query Error - Individual Publication Count", 
						e.getMessage(), 
						vitroRequest);
				
			}
        	
        } else {
        	
    		String errorMessage = "Data Model Empty &/or Inappropriate " 
    									+ "query parameters were submitted. ";
    		
    		log.error(errorMessage);
    		
    		return UtilityFunctions.handleMalformedParameters("Visualization Query Error", 
    														  errorMessage, 
    														  vitroRequest);
    		
			
        }
	}

	private VisualizationRequestHandler getVisualizationRequestHandler(
				VitroRequest vitroRequest) {
		
		String visType = vitroRequest.getParameter(VisualizationFrameworkConstants
																	.VIS_TYPE_KEY);
    	VisualizationRequestHandler visRequestHandler = null;
    	
    	try {
    		visRequestHandler = VisualizationsDependencyInjector
										.getVisualizationIDsToClassMap(getServletContext()).get(visType);
    		
    	} catch (NullPointerException nullKeyException) {

    		return null;
		}
    	
		return visRequestHandler;
	}

	private DataSource setupJENADataSource(Model model, VitroRequest vreq) {

        log.debug("rdfResultFormat was: " + VisConstants.RDF_RESULT_FORMAT_PARAM);

        DataSource dataSource = DatasetFactory.create();
        ModelMaker maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");

    	dataSource.setDefaultModel(model);

        return dataSource;
	}

}

