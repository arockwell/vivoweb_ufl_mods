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

package edu.cornell.mannlib.vitro.webapp.visualization.personlevel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.DataSource;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.coauthorship.CoAuthorshipGraphMLWriter;
import edu.cornell.mannlib.vitro.webapp.visualization.coauthorship.CoAuthorshipQueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.coauthorship.CoAuthorshipVisCodeGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.personpubcount.PersonPublicationCountQueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.personpubcount.PersonPublicationCountVisCodeGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.CoAuthorshipVOContainer;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Node;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.SparklineVOContainer;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.QueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.VisualizationRequestHandler;

public class PersonLevelRequestHandler implements VisualizationRequestHandler {

    private static final String EGO_PUB_SPARKLINE_VIS_CONTAINER_ID = "ego_pub_sparkline";
    private static final String UNIQUE_COAUTHORS_SPARKLINE_VIS_CONTAINER_ID = 
    									"unique_coauthors_sparkline";
    
	public void generateVisualization(VitroRequest vitroRequest,
			   HttpServletRequest request, 
			   HttpServletResponse response, 
			   Log log, 
			   DataSource dataSource) {

        String egoURIParam = vitroRequest.getParameter(
        							VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE);

        String renderMode = vitroRequest.getParameter(
        							VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE);
        
        String visMode = vitroRequest.getParameter(
        							VisualizationFrameworkConstants.VIS_MODE_URL_HANDLE);
        
		QueryHandler<CoAuthorshipVOContainer> 
			coAuthorshipQueryManager =
	        	new CoAuthorshipQueryHandler(egoURIParam,
							     dataSource,
							     log);
        
        QueryHandler<List<BiboDocument>> publicationQueryManager =
        	new PersonPublicationCountQueryHandler(egoURIParam,
											   	   dataSource,
											       log);
        
		try {
			
			CoAuthorshipVOContainer coAuthorshipVO = coAuthorshipQueryManager
															.getVisualizationJavaValueObjects();
			
	    	/*
	    	 * In order to avoid unneeded computations we have pushed this "if" condition up.
	    	 * This case arises when the render mode is data. In that case we dont want to generate 
	    	 * HTML code to render sparkline, tables etc. Ideally I would want to avoid this flow.
	    	 * It is ugly! 
	    	 * */
	    	if (VisualizationFrameworkConstants.DATA_RENDER_MODE_URL_VALUE
	    				.equalsIgnoreCase(renderMode)) { 
			
					/* 
			    	 * We will be using the same visualization package for providing data for both 
			    	 * list of unique coauthors & network of coauthors (used in the flash vis). 
			    	 * We will use "VIS_MODE_URL_HANDLE" as a modifier to differentiate between 
			    	 * these two. The defualt will be to provide data used to render the co-
			    	 * authorship network vis.
			    	 * */ 
					
					if (VisualizationFrameworkConstants.COAUTHORSLIST_VIS_MODE_URL_VALUE
								.equalsIgnoreCase(visMode)) { 
		    			/*
		    			 * When the csv file is required - containing the unique co-authors vs how 
		    			 * many times they have co-authored with the ego.
		    			 * */
							prepareVisualizationQueryListCoauthorsDataResponse(coAuthorshipVO, 
																			   response);
							return;
			    		
					} else {
			    			/*
			    			 * When the graphML file is required - based on which co-authorship 
			    			 * network visualization will be rendered.
			    			 * */
			    			prepareVisualizationQueryNetworkDataResponse(coAuthorshipVO, response);
							return;
					}
	    		
	    		
			}
					
			List<BiboDocument> authorDocuments = publicationQueryManager
														.getVisualizationJavaValueObjects();
	    	/*
	    	 * Create a map from the year to number of publications. Use the BiboDocument's
	    	 * parsedPublicationYear to populate the data.
	    	 * */
	    	Map<String, Integer> yearToPublicationCount = 
	    			((PersonPublicationCountQueryHandler) publicationQueryManager)
	    					.getYearToPublicationCount(authorDocuments);
	    														
	    	/*
	    	 * Computations required to generate HTML for the sparklines & related context.
	    	 * */
	    	
	    	SparklineVOContainer publicationSparklineVO = new SparklineVOContainer();
	    	SparklineVOContainer uniqueCoauthorsSparklineVO = new SparklineVOContainer();

	    	PersonPublicationCountVisCodeGenerator personPubCountVisCodeGenerator = 
	    		new PersonPublicationCountVisCodeGenerator(
	    			vitroRequest.getRequestURI(),
	    			egoURIParam,
	    			PersonPublicationCountVisCodeGenerator.FULL_SPARKLINE_MODE_URL_HANDLE,
	    			EGO_PUB_SPARKLINE_VIS_CONTAINER_ID,
	    			authorDocuments,
	    			yearToPublicationCount,
	    			publicationSparklineVO,
	    			log);	  
	    	
	    	CoAuthorshipVisCodeGenerator uniqueCoauthorsVisCodeGenerator = 
	    		new CoAuthorshipVisCodeGenerator(
	    			vitroRequest.getRequestURI(),
	    			egoURIParam,
	    			PersonPublicationCountVisCodeGenerator.FULL_SPARKLINE_MODE_URL_HANDLE,
	    			UNIQUE_COAUTHORS_SPARKLINE_VIS_CONTAINER_ID,
	    			getUniqueCoAuthorsPerYear(coAuthorshipVO),
	    			uniqueCoauthorsSparklineVO,
	    			log);
			
			
			RequestDispatcher requestDispatcher = null;

			prepareVisualizationQueryStandaloneResponse(
					egoURIParam, 
	    			publicationSparklineVO,
	    			uniqueCoauthorsSparklineVO,
	    			coAuthorshipVO,
	    			EGO_PUB_SPARKLINE_VIS_CONTAINER_ID,
	    			UNIQUE_COAUTHORS_SPARKLINE_VIS_CONTAINER_ID,
	    			vitroRequest,
	    			request);

			requestDispatcher = request.getRequestDispatcher(Controllers.BASIC_JSP);

	    	try {
	            requestDispatcher.forward(request, response);
	        } catch (Exception e) {
	            log.error("EntityEditController could not forward to view.");
	            log.error(e.getMessage());
	            log.error(e.getStackTrace());
	        }

		} catch (MalformedQueryParametersException e) {
			try {
				handleMalformedParameters(e.getMessage(), 
										  vitroRequest,
										  request, 
										  response, 
										  log);
			} catch (ServletException e1) {
				log.error(e1.getStackTrace());
			} catch (IOException e1) {
				log.error(e1.getStackTrace());
			}
			return;
		}
	}
	
	private Map<String, Set<Node>> getUniqueCoAuthorsPerYear(
			CoAuthorshipVOContainer authorNodesAndEdges) {

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

	private void prepareVisualizationQueryNetworkDataResponse(
			CoAuthorshipVOContainer coAuthorsipVO, HttpServletResponse response) {

		String outputFileName = "";
		
		if (coAuthorsipVO.getNodes() != null && coAuthorsipVO.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(coAuthorsipVO.getEgoNode().getNodeName()) 
									+ "_coauthor-network.graphml" + ".xml";
			
		} else {
			
			outputFileName = "no_coauthor-network.graphml" + ".xml";			
			
		}
		
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=" + outputFileName);
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		/*
		 * We are side-effecting responseWriter since we are directly manipulating the response 
		 * object of the servlet.
		 * */
		CoAuthorshipGraphMLWriter coAuthorShipGraphMLWriter = 
				new CoAuthorshipGraphMLWriter(coAuthorsipVO);
		
		responseWriter.append(coAuthorShipGraphMLWriter.getCoAuthorshipGraphMLContent());
		
		responseWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareVisualizationQueryListCoauthorsDataResponse(
			CoAuthorshipVOContainer coAuthorshipVO, HttpServletResponse response) {

		String outputFileName = "";
		Map<String, Integer> coAuthorsToCount = new TreeMap<String, Integer>();
		
		if (coAuthorshipVO.getNodes() != null && coAuthorshipVO.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(coAuthorshipVO.getEgoNode().getNodeName()) 
									+ "_coauthors" + ".csv";

			coAuthorsToCount = getCoAuthorsList(coAuthorshipVO);
			
		} else {
			
			outputFileName = "no_coauthors" + ".csv";
			
		}
			
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=" + outputFileName);
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		/*
		 * We are side-effecting responseWriter since we are directly manipulating the response 
		 * object of the servlet.
		 * */
		generateCsvFileBuffer(coAuthorsToCount, 
							  responseWriter);

		responseWriter.close();
		
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	
	
	private Map<String, Integer> getCoAuthorsList(CoAuthorshipVOContainer coAuthorsipVO) {
		
		Map<String, Integer> coAuthorsToCount = new TreeMap<String, Integer>();
		
		for (Node currNode : coAuthorsipVO.getNodes()) {
			
			/*
			 * We have already printed the Ego Node info.
			 * */
			if (currNode != coAuthorsipVO.getEgoNode()) {
				
				coAuthorsToCount.put(currNode.getNodeName(), currNode.getNumOfAuthoredWorks());
				
			}
		}
		return coAuthorsToCount;
	}

	private void generateCsvFileBuffer(Map<String, Integer> coAuthorsToCount, 
									   PrintWriter printWriter) {
		
	    	printWriter.append("\"Co-Author\", \"Count\"\n");
			
			for (Entry<String, Integer> currentEntry : coAuthorsToCount.entrySet()) {
				
				printWriter.append("\"" + currentEntry.getKey() + "\"," 
								   + "\"" + currentEntry.getValue() + "\"\n"
											  );
			}
			
		printWriter.flush();
	}
	
	private void prepareVisualizationQueryStandaloneResponse(
					String egoURIParam, 
					SparklineVOContainer egoPubSparklineVO, 
					SparklineVOContainer uniqueCoauthorsSparklineVO, 
					CoAuthorshipVOContainer coAuthorshipVO, 
					String egoPubSparklineVisContainer, 
					String uniqueCoauthorsSparklineVisContainer, 
					VitroRequest vitroRequest, 
					HttpServletRequest request) {

        Portal portal = vitroRequest.getPortal();
        
        request.setAttribute("egoURIParam", egoURIParam);
        
        String title = "";
        if (coAuthorshipVO.getNodes() != null && coAuthorshipVO.getNodes().size() > 0) {
        	request.setAttribute("numOfAuthors", coAuthorshipVO.getNodes().size());
        	title = "for " + coAuthorshipVO.getEgoNode().getNodeName();
		}
		
		if (coAuthorshipVO.getEdges() != null && coAuthorshipVO.getEdges().size() > 0) {
			request.setAttribute("numOfCoAuthorShips", coAuthorshipVO.getEdges().size());
		}
		
        
        request.setAttribute("egoPubSparklineVO", egoPubSparklineVO);
        request.setAttribute("uniqueCoauthorsSparklineVO", uniqueCoauthorsSparklineVO);
        
        request.setAttribute("egoPubSparklineContainerID", egoPubSparklineVisContainer);
        request.setAttribute("uniqueCoauthorsSparklineVisContainerID", 
        					 uniqueCoauthorsSparklineVisContainer);
        
        request.setAttribute("title", "Person Level Visualization " + title);
        request.setAttribute("portalBean", portal);
        request.setAttribute("scripts", "/templates/visualization/person_level_inject_head.jsp");
        
        request.setAttribute("bodyJsp", "/templates/visualization/person_level.jsp");
	}

	private void handleMalformedParameters(String errorMessage, 
			VitroRequest vitroRequest, 
			HttpServletRequest request, 
			HttpServletResponse response, 
			Log log)
			throws ServletException, IOException {

		Portal portal = vitroRequest.getPortal();

		request.setAttribute("error", errorMessage);

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(Controllers.BASIC_JSP);
		request.setAttribute("bodyJsp", "/templates/visualization/visualization_error.jsp");
		request.setAttribute("portalBean", portal);
		request.setAttribute("title", "Visualization Query Error - Individual Publication Count");

		try {
			requestDispatcher.forward(request, response);
		} catch (Exception e) {
			log.error("EntityEditController could not forward to view.");
			log.error(e.getMessage());
			log.error(e.getStackTrace());
		}
	}

}
