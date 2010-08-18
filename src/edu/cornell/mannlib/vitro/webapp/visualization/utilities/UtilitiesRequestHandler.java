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

package edu.cornell.mannlib.vitro.webapp.visualization.utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileServingHelper;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryFieldLabels;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.GenericQueryMap;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.AllPropertiesQueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.GenericQueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.QueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.VisualizationRequestHandler;

public class UtilitiesRequestHandler extends VisualizationRequestHandler {
	
	public UtilitiesRequestHandler(VitroRequest vitroRequest,
			HttpServletRequest request, HttpServletResponse response, Log log) {

		super(vitroRequest, request, response, log);

	}

	public void generateVisualization(DataSource dataSource) {

		VitroRequest vitroRequest = super.getVitroRequest();
        String individualURIParam = vitroRequest.getParameter(
        									VisualizationFrameworkConstants
        											.INDIVIDUAL_URI_URL_HANDLE);

        String visMode = vitroRequest.getParameter(VisualizationFrameworkConstants
        											.VIS_MODE_URL_HANDLE);
        
        String preparedURL = "";

        Log log = super.getLog();
        HttpServletRequest request = super.getRequest();
        try {
        
            /*
    		 * If the info being requested is about a profile which includes the name, moniker
    		 * & image url.
    		 * */
    		if (VisualizationFrameworkConstants.PROFILE_INFO_UTILS_VIS_MODE
    					.equalsIgnoreCase(visMode)) {
    			
    			
    			String filterRule = "?predicate = j.2:mainImage " 
    									+ "|| ?predicate = vitro:moniker  " 
    									+ "|| ?predicate = rdfs:label";
    			
    			QueryHandler<GenericQueryMap> profileQueryHandler = 
    					new AllPropertiesQueryHandler(individualURIParam, 
    												  filterRule,
    												  dataSource,
    												  log);
    			
    			try {
    				
    				GenericQueryMap profilePropertiesToValues = 
    							profileQueryHandler.getVisualizationJavaValueObjects();
    				
    				profilePropertiesToValues.addEntry("imageContextPath", 
    												   request.getContextPath());
    				
    				Gson profileInformation = new Gson();
    				
    				prepareVisualizationQueryResponse(profileInformation
    														.toJson(profilePropertiesToValues));
    				
    				return;
    				
    				
    			} catch (MalformedQueryParametersException e) {
    				try {
    					handleMalformedParameters(e.getMessage());
    				} catch (ServletException e1) {
    					log.error(e1.getStackTrace());
    				} catch (IOException e1) {
    					log.error(e1.getStackTrace());
    				}
    				return;
    			}
    			
    			
    		} else if (VisualizationFrameworkConstants.IMAGE_UTILS_VIS_MODE
    						.equalsIgnoreCase(visMode)) {
    			/*
        		 * If the url being requested is about a standalone image, which is used when we 
        		 * want to render an image & other info for a co-author OR ego for that matter.
        		 * */
    			
    			Map<String, String> fieldLabelToOutputFieldLabel = new HashMap<String, String>();
    			fieldLabelToOutputFieldLabel.put("downloadLocation", 
    											  QueryFieldLabels.THUMBNAIL_LOCATION_URL);
    			fieldLabelToOutputFieldLabel.put("fileName", QueryFieldLabels.THUMBNAIL_FILENAME);
    			
    			String whereClause = "<" + individualURIParam 
    									+ "> j.2:thumbnailImage ?thumbnailImage .  " 
    									+ "?thumbnailImage j.2:downloadLocation " 
    									+ "?downloadLocation ; j.2:filename ?fileName .";
    			
    			
    			
    			QueryHandler<ResultSet> imageQueryHandler = 
    					new GenericQueryHandler(individualURIParam,
    											fieldLabelToOutputFieldLabel,
    											whereClause,
    											dataSource,
    											log);
    			
    			try {
    				
    				String thumbnailAccessURL = 
    						getThumbnailInformation(
    								imageQueryHandler.getVisualizationJavaValueObjects(),
    								fieldLabelToOutputFieldLabel);
    				
    				prepareVisualizationQueryResponse(thumbnailAccessURL);
    				return;
    				
    				
    			} catch (MalformedQueryParametersException e) {
    				try {
    					handleMalformedParameters(e.getMessage());
    				} catch (ServletException e1) {
    					log.error(e1.getStackTrace());
    				} catch (IOException e1) {
    					log.error(e1.getStackTrace());
    				}
    				return;
    			}
    			
    			
    		} else if (VisualizationFrameworkConstants.COAUTHOR_UTILS_VIS_MODE
    						.equalsIgnoreCase(visMode)) {
    	    	/*
    	    	 * By default we will be generating profile url else some specific url like 
    	    	 * coAuthorShip vis url for that individual.
    	    	 * */
				
				preparedURL += request.getContextPath()
								+ "/admin/visQuery"
								+ "?" 
								+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
								+ "=" + URLEncoder.encode(individualURIParam, 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString()
						 	    + "&"
			 				    + VisualizationFrameworkConstants.VIS_TYPE_URL_HANDLE 
								+ "=" + URLEncoder.encode("coauthorship", 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString()
			 				    + "&"
			 				    + VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE
								+ "=" + URLEncoder.encode(VisualizationFrameworkConstants
																.STANDALONE_RENDER_MODE_URL_VALUE, 
														VisualizationController.URL_ENCODING_SCHEME)
												  .toString();
				

				prepareVisualizationQueryResponse(preparedURL);
				return;

			} else if (VisualizationFrameworkConstants.PERSON_LEVEL_UTILS_VIS_MODE
							.equalsIgnoreCase(visMode)) {
    	    	/*
    	    	 * By default we will be generating profile url else some specific url like 
    	    	 * coAuthorShip vis url for that individual.
    	    	 * */
				
				preparedURL += request.getContextPath()
								+ "/admin/visQuery"
								+ "?" 
								+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
								+ "=" + URLEncoder.encode(individualURIParam, 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString()
						 	    + "&"
			 				    + VisualizationFrameworkConstants.VIS_TYPE_URL_HANDLE 
								+ "=" + URLEncoder.encode("person_level", 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString()
			 				    + "&"
			 				    + VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE
								+ "=" + URLEncoder.encode(VisualizationFrameworkConstants
																.STANDALONE_RENDER_MODE_URL_VALUE, 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString();
				
				prepareVisualizationQueryResponse(preparedURL);
				return;

			} else {
				
				preparedURL += request.getContextPath()
								+ "/individual"
								+ "?" 
								+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
								+ "=" + URLEncoder.encode(individualURIParam, 
										 VisualizationController.URL_ENCODING_SCHEME).toString();
				
				prepareVisualizationQueryResponse(preparedURL);
				return;
	
			}
			
        } catch (UnsupportedEncodingException e) {
			log.error(e.getLocalizedMessage());
		}

	}

	private String getThumbnailInformation(ResultSet resultSet,
										   Map<String, String> fieldLabelToOutputFieldLabel) {
		
		String finalThumbNailLocation = "";
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();
			
			
			RDFNode downloadLocationNode = solution.get(
													fieldLabelToOutputFieldLabel
															.get("downloadLocation"));
			RDFNode fileNameNode = solution.get(fieldLabelToOutputFieldLabel.get("fileName"));
			
			if (downloadLocationNode != null && fileNameNode != null) {
				finalThumbNailLocation = 
						FileServingHelper
								.getBytestreamAliasUrl(downloadLocationNode.toString(),
										fileNameNode.toString());
			}
			
		}
		
		return finalThumbNailLocation;
	}
	
	private void prepareVisualizationQueryResponse(String preparedURL) {

		super.getResponse().setContentType("text/plain");
		
		try {
		
		PrintWriter responseWriter = super.getResponse().getWriter();
		
		responseWriter.append(preparedURL);
		
		responseWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleMalformedParameters(String errorMessage)
		throws ServletException, IOException {
	
		Portal portal = super.getVitroRequest().getPortal();
		
		HttpServletRequest request = super.getRequest();
		request.setAttribute("error", errorMessage);
		
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(Controllers.BASIC_JSP);
		request.setAttribute("bodyJsp", "/templates/visualization/visualization_error.jsp");
		request.setAttribute("portalBean", portal);
		request.setAttribute("title", "Visualization Query Error - Individual Publication Count");
		
		try {
			requestDispatcher.forward(request, super.getResponse());
		} catch (Exception e) {
			Log log = super.getLog();
			log.error("EntityEditController could not forward to view.");
			log.error(e.getMessage());
			log.error(e.getStackTrace());
		}
	}
	
}
