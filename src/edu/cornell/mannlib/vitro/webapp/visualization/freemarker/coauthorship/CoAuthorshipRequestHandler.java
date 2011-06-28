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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coauthorship;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.DataVisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoAuthorshipData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Node;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.VisualizationRequestHandler;

/**
 * This request handler is used when information related to co-authorship network
 * for an individual is requested. It currently provides 2 outputs,
 * 		1. Graphml content representing the individual's co-authorship network 
 * 		1. CSV file containing the list(& count) of unique co-authors with which 
 * the individual has worked over the years. This data powers the related sparkline.
 *  
 * @author cdtank
 */
public class CoAuthorshipRequestHandler implements VisualizationRequestHandler {

	@Override
	public Object generateAjaxVisualization(VitroRequest vitroRequest, Log log,
			Dataset Dataset) throws MalformedQueryParametersException {
		throw new UnsupportedOperationException("CoAuthorship does not provide Ajax Response.");
	}

	@Override
	public Map<String, String> generateDataVisualization(
			VitroRequest vitroRequest, Log log, Dataset Dataset)
			throws MalformedQueryParametersException {

	
		String egoURI = vitroRequest.getParameter(
				VisualizationFrameworkConstants
						.INDIVIDUAL_URI_KEY);

		String visMode = vitroRequest.getParameter(
						VisualizationFrameworkConstants
								.VIS_MODE_KEY);
		
		QueryRunner<CoAuthorshipData> queryManager =
		new CoAuthorshipQueryRunner(egoURI, Dataset, log);
		
		CoAuthorshipData authorNodesAndEdges = 
		queryManager.getQueryResult();
	
    	/* 
    	 * We will be using the same visualization package for both sparkline & coauthorship
    	 * flash vis. We will use "VIS_MODE_KEY" as a modifier to differentiate 
    	 * between these two. The default will be to render the coauthorship network vis.
    	 * */ 
		if (VisualizationFrameworkConstants.COAUTHORS_COUNT_PER_YEAR_VIS_MODE
				.equalsIgnoreCase(visMode)) { 
			/*
			 * When the csv file is required - based on which sparkline visualization will 
			 * be rendered.
			 * */
				return prepareCoauthorsCountPerYearDataResponse(authorNodesAndEdges);
				
		} else if (VisualizationFrameworkConstants.COAUTHORS_LIST_VIS_MODE
				.equalsIgnoreCase(visMode)) { 
			/*
			 * When the csv file is required - based on which sparkline visualization will 
			 * be rendered.
			 * */
				return prepareCoauthorsListDataResponse(authorNodesAndEdges);
				
		} else if (VisualizationFrameworkConstants.COAUTHOR_NETWORK_DOWNLOAD_VIS_MODE
				.equalsIgnoreCase(visMode)) { 
			/*
			 * When the csv file is required - based on which sparkline visualization will 
			 * be rendered.
			 * */
				return prepareNetworkDownloadDataResponse(authorNodesAndEdges);
				
		} else {
    			/*
    			 * When the graphML file is required - based on which coauthorship network 
    			 * visualization will be rendered.
    			 * */
    			return prepareNetworkStreamDataResponse(authorNodesAndEdges);
		}
	
	}

	public ResponseValues generateStandardVisualization(VitroRequest vitroRequest,
											  	Log log, 
											    Dataset Dataset) 
		throws MalformedQueryParametersException {

		/*
		 * Support for this has ceased to exist. Standalone mode was created only for demo 
		 * purposes for VIVO Conf.
		 * */
/*		String egoURI = vitroRequest.getParameter(
        										VisualizationFrameworkConstants
        												.INDIVIDUAL_URI_KEY);

		QueryRunner<CoAuthorshipData> queryManager =
        	new CoAuthorshipQueryRunner(egoURI, Dataset, log);
	
			CoAuthorshipData authorNodesAndEdges = 
					queryManager.getQueryResult();
			
	    	
		return prepareStandaloneResponse(egoURI,
										 authorNodesAndEdges,
										 vitroRequest);*/
		
		throw new UnsupportedOperationException("CoAuthorship does not provide Standalone Response.");
		
	}
	
	

	private String getCoauthorsListCSVContent(CoAuthorshipData coAuthorshipData) {
		
		StringBuilder csvFileContent = new StringBuilder();
		
		csvFileContent.append("Co-author, Count\n");
		
		//for (Entry<String, Integer> currentEntry : coAuthorsToCount.entrySet()) {
		for (Node currNode : coAuthorshipData.getNodes()) {			
			
			/*
			 * We have already printed the Ego Node info.
			 * */
			if (currNode != coAuthorshipData.getEgoNode()) {
				
			
			csvFileContent.append(StringEscapeUtils.escapeCsv(currNode.getNodeName()));
			csvFileContent.append(",");
			csvFileContent.append(currNode.getNumOfAuthoredWorks());
			csvFileContent.append("\n");
			
			}
			
			
		}
		
		return csvFileContent.toString();
		
	}

	private String getCoauthorsPerYearCSVContent(Map<String, Set<Node>> yearToCoauthors) {
		
		StringBuilder csvFileContent = new StringBuilder();
		
		csvFileContent.append("Year, Count, Co-author(s)\n");
		
		for (Entry<String, Set<Node>> currentEntry : yearToCoauthors.entrySet()) {
			csvFileContent.append(StringEscapeUtils.escapeCsv(currentEntry.getKey()));
			csvFileContent.append(",");
			csvFileContent.append(currentEntry.getValue().size());
			csvFileContent.append(",");
			csvFileContent.append(StringEscapeUtils.escapeCsv(getCoauthorNamesAsString(currentEntry.getValue())));
			csvFileContent.append("\n");
		}
		
		return csvFileContent.toString();
			
	}
	
	private String getCoauthorNamesAsString(Set<Node> coAuthors) {
		
		StringBuilder coAuthorsMerged = new StringBuilder();
		
		String coAuthorSeparator = "; ";
		for (Node currCoAuthor : coAuthors) {
			coAuthorsMerged.append(currCoAuthor.getNodeName() + coAuthorSeparator);
		}
		
		return StringUtils.removeEnd(coAuthorsMerged.toString(), coAuthorSeparator);
	}

	/**
	 * Provides response when a csv file containing number & names of unique co-authors per 
	 * year is requested. 
	 * @param authorNodesAndEdges
	 * @param response
	 */
	private Map<String, String> prepareCoauthorsCountPerYearDataResponse(CoAuthorshipData authorNodesAndEdges) {
		
		String outputFileName;
		Map<String, Set<Node>> yearToCoauthors = new TreeMap<String, Set<Node>>();
		
		if (authorNodesAndEdges.getNodes() != null && authorNodesAndEdges.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(authorNodesAndEdges
									.getEgoNode().getNodeName())
			+ "_co-authors-per-year" + ".csv";
			
			yearToCoauthors = UtilityFunctions.getPublicationYearToCoAuthors(authorNodesAndEdges);
			
		} else {
			
			outputFileName = "no_co-authors-per-year" + ".csv";			
		}
		
        Map<String, String> fileData = new HashMap<String, String>();
		fileData.put(DataVisualizationController.FILE_NAME_KEY, 
					 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 getCoauthorsPerYearCSVContent(yearToCoauthors));

		return fileData;
	}

	/**
	 * Provides response when a csv file containing number & names of unique co-authors per 
	 * year is requested. 
	 * @param authorNodesAndEdges
	 * @param response
	 */
	private Map<String, String> prepareCoauthorsListDataResponse(CoAuthorshipData coAuthorshipData) {
		
		String outputFileName = "";

		if (coAuthorshipData.getNodes() != null && coAuthorshipData.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(coAuthorshipData.getEgoNode().getNodeName()) 
									+ "_co-authors" + ".csv";
		} else {
			outputFileName = "no_co-authors" + ".csv";
		}
		
        Map<String, String> fileData = new HashMap<String, String>();
		fileData.put(DataVisualizationController.FILE_NAME_KEY, 
					 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 getCoauthorsListCSVContent(coAuthorshipData));

		return fileData;
	}
	
	/**
	 * Provides a response when graphml formatted co-authorship network is requested, typically by 
	 * the flash vis.
	 * @param authorNodesAndEdges
	 * @param response
	 */
	private Map<String, String> prepareNetworkStreamDataResponse(CoAuthorshipData authorNodesAndEdges) {
	
		CoAuthorshipGraphMLWriter coAuthorshipGraphMLWriter = 
				new CoAuthorshipGraphMLWriter(authorNodesAndEdges);
		
        Map<String, String> fileData = new HashMap<String, String>();
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "text/xml");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 coAuthorshipGraphMLWriter.getCoAuthorshipGraphMLContent().toString());

		return fileData;
	
	}
	
	private Map<String, String> prepareNetworkDownloadDataResponse(CoAuthorshipData authorNodesAndEdges) {
		
		String outputFileName = "";
		
		if (authorNodesAndEdges.getNodes() != null && authorNodesAndEdges.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(authorNodesAndEdges.getEgoNode().getNodeName()) 
									+ "_co-author-network.graphml" + ".xml";
			
		} else {
			outputFileName = "no_co-author-network.graphml" + ".xml";			
		}
		
		CoAuthorshipGraphMLWriter coAuthorshipGraphMLWriter = 
				new CoAuthorshipGraphMLWriter(authorNodesAndEdges);
		
        Map<String, String> fileData = new HashMap<String, String>();
        fileData.put(DataVisualizationController.FILE_NAME_KEY, 
				 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "text/xml");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 coAuthorshipGraphMLWriter.getCoAuthorshipGraphMLContent().toString());

		return fileData;
	
	}
	
	/**
	 * When the page for person level visualization is requested.
	 * @param egoURI
	 * @param coAuthorshipVO
	 * @param vitroRequest
	 * @param request
	 * @return 
	 */
	private TemplateResponseValues prepareStandaloneResponse(
					String egoURI, 
					CoAuthorshipData coAuthorshipVO, 
					VitroRequest vitroRequest) {

        Portal portal = vitroRequest.getPortal();
        
        String title = "";
        Map<String, Object> body = new HashMap<String, Object>();
        
        if (coAuthorshipVO.getNodes() != null && coAuthorshipVO.getNodes().size() > 0) {
        	title = coAuthorshipVO.getEgoNode().getNodeName() + " - ";
        	body.put("numOfAuthors", coAuthorshipVO.getNodes().size());
		}
		
		if (coAuthorshipVO.getEdges() != null && coAuthorshipVO.getEdges().size() > 0) {
			body.put("numOfCoAuthorShips", coAuthorshipVO.getEdges().size());
		}
		
        
        //request.setAttribute("scripts", "/templates/visualization/person_level_inject_head.jsp");
        
        String standaloneTemplate = "coAuthorship.ftl";

        
        body.put("portalBean", portal);
        body.put("egoURIParam", egoURI);
        body.put("title", title + "Co-Authorship Visualization");
        
        return new TemplateResponseValues(standaloneTemplate, body);
	}
	
}