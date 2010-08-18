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

package edu.cornell.mannlib.vitro.webapp.visualization.coauthorship;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.DataSource;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.CoAuthorshipVOContainer;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Node;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.QueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.VisualizationRequestHandler;

public class CoAuthorshipRequestHandler extends VisualizationRequestHandler {

	
	public CoAuthorshipRequestHandler(VitroRequest vitroRequest,
			HttpServletRequest request, HttpServletResponse response, Log log) {
		
		super(vitroRequest, request, response, log);
		
	}

	public void generateVisualization(DataSource dataSource) {

        VitroRequest vitroRequest = super.getVitroRequest();
		String egoURIParam = vitroRequest.getParameter(
        										VisualizationFrameworkConstants
        												.INDIVIDUAL_URI_URL_HANDLE);

        String renderMode = vitroRequest.getParameter(
        										VisualizationFrameworkConstants
        												.RENDER_MODE_URL_HANDLE);
        
        String visMode = vitroRequest.getParameter(
        										VisualizationFrameworkConstants
        												.VIS_MODE_URL_HANDLE);

        Log log = super.getLog();
		QueryHandler<CoAuthorshipVOContainer> queryManager =
        	new CoAuthorshipQueryHandler(egoURIParam,
						     dataSource,
						     log);

		try {
			
			CoAuthorshipVOContainer authorNodesAndEdges = 
					queryManager.getVisualizationJavaValueObjects();
			
	    	/*
	    	 * In order to avoid unneeded computations we have pushed this "if" condition up.
	    	 * This case arises when the render mode is data. In that case we dont want to generate 
	    	 * HTML code to render sparkline, tables etc. Ideally I would want to avoid this flow.
	    	 * It is ugly!
	    	 * */
	    	
			if (VisualizationFrameworkConstants.DATA_RENDER_MODE_URL_VALUE
					.equalsIgnoreCase(renderMode)) {
				
		    	/* 
		    	 * We will be using the same visualization package for both sparkline & coauthorship
		    	 * flash vis. We will use "VIS_MODE_URL_HANDLE" as a modifier to differentiate 
		    	 * between these two. The defualt will be to render the coauthorship network vis.
		    	 * */ 
				
				if (VisualizationFrameworkConstants.SPARKLINE_VIS_MODE_URL_VALUE
						.equalsIgnoreCase(visMode)) { 
	    			/*
	    			 * When the csv file is required - based on which sparkline visualization will 
	    			 * be rendered.
	    			 * */
						prepareVisualizationQuerySparklineDataResponse(authorNodesAndEdges);
						return;
		    		
				} else {
		    			/*
		    			 * When the graphML file is required - based on which coauthorship network 
		    			 * visualization will be rendered.
		    			 * */
		    			prepareVisualizationQueryNetworkDataResponse(authorNodesAndEdges);
						return;
				}
			}
			
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

	}

	private void prepareVisualizationQueryNetworkDataResponse(
			CoAuthorshipVOContainer authorNodesAndEdges) {

		super.getResponse().setContentType("text/xml");
		
		try {
		
		PrintWriter responseWriter = super.getResponse().getWriter();
		
		/*
		 * We are side-effecting responseWriter since we are directly manipulating the response 
		 * object of the servlet.
		 * */
		CoAuthorshipGraphMLWriter coAuthorshipGraphMLWriter = 
				new CoAuthorshipGraphMLWriter(authorNodesAndEdges);
		
		responseWriter.append(coAuthorshipGraphMLWriter.getCoAuthorshipGraphMLContent());
		
		responseWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareVisualizationQuerySparklineDataResponse(
			CoAuthorshipVOContainer authorNodesAndEdges) {
		
		String outputFileName;
		Map<String, Set<Node>> yearToCoauthors = new TreeMap<String, Set<Node>>();
		
		if (authorNodesAndEdges.getNodes() != null && authorNodesAndEdges.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(authorNodesAndEdges
									.getEgoNode().getNodeName())
			+ "_coauthors-per-year" + ".csv";
			
			yearToCoauthors = getCoAuthorsStats(authorNodesAndEdges);
			
		} else {
			
			outputFileName = "no_coauthors-per-year" + ".csv";			
		}
		
		HttpServletResponse response = super.getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", 
									  "attachment;filename=" + outputFileName);
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		/*
		 * We are side-effecting responseWriter since we are directly manipulating the response 
		 * object of the servlet.
		 * */
		generateCsvFileBuffer(yearToCoauthors, 
							  responseWriter);

		responseWriter.close();
		
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	
	private void generateCsvFileBuffer(Map<String, Set<Node>> yearToCoauthors, 
									   PrintWriter printWriter) {
		
        	printWriter.append("\"Year\", \"Number of Co-Authors\", \"Co-Author(s)\"\n");
			
			for (Entry<String, Set<Node>> currentEntry : yearToCoauthors.entrySet()) {
				
				printWriter.append("\"" + currentEntry.getKey() + "\"," 
								   + "\"" + currentEntry.getValue().size() + "\","
								   + "\"" + getCoauthorsString(currentEntry.getValue()) + "\"\n"
											  );
			}
			
		printWriter.flush();
	}
	
	private String getCoauthorsString(Set<Node> coAuthors) {
		
		StringBuilder coAuthorsMerged = new StringBuilder();
		
		String coAuthorSeparator = "; ";
		for (Node currCoAuthor : coAuthors) {
			coAuthorsMerged.append(currCoAuthor.getNodeName() + coAuthorSeparator);
		}
		
		return StringUtils.removeEnd(coAuthorsMerged.toString(), coAuthorSeparator);
	}
	
	private Map<String, Set<Node>> getCoAuthorsStats(CoAuthorshipVOContainer authorNodesAndEdges) {

		Map<String, Set<Node>> yearToCoAuthors = new TreeMap<String, Set<Node>>();
		
		Node egoNode = authorNodesAndEdges.getEgoNode();
		
		for (Node currNode : authorNodesAndEdges.getNodes()) {
					
				/*
				 * We have already printed the Ego Node info.
				 * */
				if (currNode != egoNode) {
					
					for (String year : currNode.getYearToPublicationCount().keySet()) {
						
						Set<Node> coAuthorNodes;
						
						if (yearToCoAuthors.containsKey(year)) {
							
							coAuthorNodes = yearToCoAuthors.get(year);
							coAuthorNodes.add(currNode);
							
						} else {
							
							coAuthorNodes = new HashSet<Node>();
							coAuthorNodes.add(currNode);
							yearToCoAuthors.put(year, coAuthorNodes);
						}
						
					}
					
				}
		}
		
		return yearToCoAuthors;
	}

	private void handleMalformedParameters(String errorMessage)
			throws ServletException, IOException {

		Portal portal = super.getVitroRequest().getPortal();

		HttpServletRequest request = super.getRequest();
		request.setAttribute("error", errorMessage);

		RequestDispatcher requestDispatcher = 
				request.getRequestDispatcher(Controllers.BASIC_JSP);
		request.setAttribute("bodyJsp", 
										"/templates/visualization/visualization_error.jsp");
		request.setAttribute("portalBean", portal);
		request.setAttribute("title", 
										"Visualization Query Error - Individual Publication Count");

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
