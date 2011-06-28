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
package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coprincipalinvestigator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.DataVisualizationController;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoPIData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoPINode;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.VisualizationRequestHandler;
/**
 * @author bkoniden
 * Deepak Konidena
 */
public class CoPIGrantCountRequestHandler implements VisualizationRequestHandler{
	

	@Override
	public Object generateAjaxVisualization(VitroRequest vitroRequest, Log log,
			Dataset Dataset) throws MalformedQueryParametersException {
		throw new UnsupportedOperationException("Co-PI Grant Count does not provide Ajax Response.");
	}

	@Override
	public Map<String, String> generateDataVisualization(
			VitroRequest vitroRequest, Log log, Dataset Dataset)
			throws MalformedQueryParametersException {
		
		String egoURI = vitroRequest.getParameter(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY);
		String visMode = vitroRequest.getParameter(VisualizationFrameworkConstants.VIS_MODE_KEY);
		
		CoPIGrantCountConstructQueryRunner constructQueryRunner = new CoPIGrantCountConstructQueryRunner(egoURI, Dataset, log);
		Model constructedModel = constructQueryRunner.getConstructedModel();
		
		QueryRunner<CoPIData> queryManager = new CoPIGrantCountQueryRunner(egoURI, constructedModel, log);
		
		CoPIData PINodesAndEdges = queryManager.getQueryResult();
				
    	/* 
    	 * We will be using the same visualization package for both sparkline & co-pi
    	 * flash vis. We will use "VIS_MODE_KEY" as a modifier to differentiate 
    	 * between these two. The default will be to render the co-pi network vis.
    	 * */ 
		if (VisualizationFrameworkConstants.COPIS_COUNT_PER_YEAR_VIS_MODE
				.equalsIgnoreCase(visMode)) { 
			/*
			 * When the csv file is required - based on which sparkline visualization will 
			 * be rendered.
			 * */
				return prepareCoPIsCountPerYearDataResponse(PINodesAndEdges);
				
		} else if (VisualizationFrameworkConstants.COPIS_LIST_VIS_MODE
				.equalsIgnoreCase(visMode)) { 
			/*
			 * When the csv file is required - based on which sparkline visualization will 
			 * be rendered.
			 * */
				return prepareCoPIsListDataResponse(PINodesAndEdges);
				
		} else if (VisualizationFrameworkConstants.COPI_NETWORK_DOWNLOAD_VIS_MODE
				.equalsIgnoreCase(visMode)) { 
			/*
			 * When the csv file is required - based on which sparkline visualization will 
			 * be rendered.
			 * */
				return prepareNetworkDownloadDataResponse(PINodesAndEdges);
				
		} else {
    			/*
    			 * When the graphML file is required - based on which co-pi network 
    			 * visualization will be rendered.
    			 * */
    			return prepareNetworkStreamDataResponse(PINodesAndEdges);
		}
			
	}
	
	@Override
	public ResponseValues generateStandardVisualization(
			VitroRequest vitroRequest, Log log, Dataset Dataset)
			throws MalformedQueryParametersException {
		/*
		 * Support for this has ceased to exist. Standalone mode was created only for demo 
		 * purposes for VIVO Conf.
		 * */		
/*		String egoURI = vitroRequest.getParameter(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY);
		
		QueryRunner<CoPIData> queryManager = new CoPIGrantCountQueryRunner(egoURI, Dataset, log);
		
		CoPIData PINodesAndEdges = queryManager.getQueryResult();
		
		return prepareStandaloneResponse(egoURI,
				 						  PINodesAndEdges,
				 						  vitroRequest); */
		throw new UnsupportedOperationException("CoPI does not provide Standalone Response.");				 						  
				 						  
	}
	
	/**
	 * When the page for person level visualization is requested.
	 * @param egoURI
	 * @param vitroRequest
	 * @param coPIVO
	 */
	private TemplateResponseValues prepareStandaloneResponse(String egoURI,
			CoPIData coPIVO, VitroRequest vitroRequest) {
		
        Portal portal = vitroRequest.getPortal();
        
        String title = "";
        Map<String, Object> body = new HashMap<String, Object>();


        if (coPIVO.getNodes() != null
				&& coPIVO.getNodes().size() > 0) {
        	title = coPIVO.getEgoNode().getNodeName() + " - ";
        	body.put("numOfInvestigators", coPIVO.getNodes().size());
			
			title = coPIVO.getEgoNode().getNodeName() + " - ";
		}

		if (coPIVO.getEdges() != null
				&& coPIVO.getEdges().size() > 0) {
			body.put("numOfCoInvestigations", coPIVO.getEdges().size());
		}

		String standaloneTemplate = "coInvestigation.ftl";
		
        body.put("portalBean", portal);
        body.put("egoURIParam", egoURI);
        body.put("title", title + "Co-PI Visualization");
        
        return new TemplateResponseValues(standaloneTemplate, body);
	}


	private String getCoPIsListCSVContent(CoPIData coPIData) {
		
		StringBuilder csvFileContent = new StringBuilder();
		
		csvFileContent.append("Co-investigator, Count\n");
		
//		for (Entry<String, Integer> currentEntry : coPIData.entrySet()) {
		for (CoPINode currNode : coPIData.getNodes()) {
			
			/*
			 * We have already printed the Ego Node info.
			 * */
			if (currNode != coPIData.getEgoNode()) {
			
			csvFileContent.append(StringEscapeUtils.escapeCsv(currNode.getNodeName()));
			csvFileContent.append(",");
			csvFileContent.append(currNode.getNumberOfInvestigatedGrants());
			csvFileContent.append("\n");
			
			}
			
		}
		
		return csvFileContent.toString();
	}	
	

	private String getCoPIsPerYearCSVContent(Map<String, Set<CoPINode>> yearToCoPI) {

		StringBuilder csvFileContent = new StringBuilder();
		
		csvFileContent.append("Year, Count, Co-investigator(s)\n");

		for (Map.Entry<String, Set<CoPINode>> currentEntry : yearToCoPI.entrySet()) {
			
			csvFileContent.append(StringEscapeUtils.escapeCsv(currentEntry.getKey()));
			csvFileContent.append(",");
			csvFileContent.append(currentEntry.getValue().size());
			csvFileContent.append(",");
			csvFileContent.append(StringEscapeUtils.escapeCsv(getCoPINamesAsString(currentEntry.getValue())));
			csvFileContent.append("\n");
		}
		
		return csvFileContent.toString();
	}

	private String getCoPINamesAsString(Set<CoPINode> CoPIs) {
		
		StringBuilder coPIsMerged = new StringBuilder();
		
		String coPISeparator = ";";
		for(CoPINode currentCoPI : CoPIs){
			coPIsMerged.append(currentCoPI.getNodeName() + coPISeparator);
		}
		
		return StringUtils.removeEnd(coPIsMerged.toString(), coPISeparator);
	}
	
	
	/**
	 * Provides response when a csv file containing number & names of unique co-pis per 
	 * year is requested. 
	 * @param piNodesAndEdges
	 * @param response
	 */
	private Map<String, String> prepareCoPIsCountPerYearDataResponse(CoPIData piNodesAndEdges) {
		
		String outputFileName;
		Map<String, Set<CoPINode>> yearToCoPIs = new TreeMap<String, Set<CoPINode>>();
		
		if (piNodesAndEdges.getNodes() != null && piNodesAndEdges.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(piNodesAndEdges
									.getEgoNode().getNodeName())
			+ "_co-investigators-per-year" + ".csv";
			
			yearToCoPIs = UtilityFunctions.getGrantYearToCoPI(piNodesAndEdges);
			
		} else {
			
			outputFileName = "no_co-investigators-per-year" + ".csv";			
		}
		
        Map<String, String> fileData = new HashMap<String, String>();
		fileData.put(DataVisualizationController.FILE_NAME_KEY, 
					 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 getCoPIsPerYearCSVContent(yearToCoPIs));

		return fileData;
	}

	/**
	 * Provides response when a csv file containing number & names of unique co-pis per 
	 * year is requested. 
	 * @param coPIData
	 * @param response
	 */
	private Map<String, String> prepareCoPIsListDataResponse(CoPIData coPIData) {
		
		String outputFileName = "";
		
		if (coPIData.getNodes() != null && coPIData.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(coPIData.getEgoNode().getNodeName()) 
									+ "_co-investigators" + ".csv";
	
		} else {
			outputFileName = "no_co-investigators" + ".csv";
		}
		
        Map<String, String> fileData = new HashMap<String, String>();
		fileData.put(DataVisualizationController.FILE_NAME_KEY, 
					 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 getCoPIsListCSVContent(coPIData));

		return fileData;
	}

	private Map<String, Integer> getCoPIsList(CoPIData coPIVO) {
		
		Map<String, Integer> coPIsToCount = new TreeMap<String, Integer>();
		
		for (CoPINode currNode : coPIVO.getNodes()) {
			
			/*
			 * We have already printed the Ego Node info.
			 * */
			if (currNode != coPIVO.getEgoNode()) {
				
				coPIsToCount.put(currNode.getNodeName(), currNode.getNumberOfInvestigatedGrants());
				
			}
		}
		return coPIsToCount;
	}
	
	/**
	 * Provides a response when graphml formatted co-pi network is requested, typically by 
	 * the flash vis.
	 * @param coPIData
	 * @param response
	 */
	private Map<String, String> prepareNetworkStreamDataResponse(CoPIData coPIData) {
	
		CoPIGraphMLWriter coPIGraphMLWriter = 
				new CoPIGraphMLWriter(coPIData);
		
        Map<String, String> fileData = new HashMap<String, String>();
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "text/xml");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 coPIGraphMLWriter.getCoPIGraphMLContent().toString());

		return fileData;
	
	}
	
	private Map<String, String> prepareNetworkDownloadDataResponse(CoPIData coPIData) {
		
		String outputFileName = "";
		
		if (coPIData.getNodes() != null && coPIData.getNodes().size() > 0) {
			
			outputFileName = UtilityFunctions.slugify(coPIData.getEgoNode().getNodeName()) 
									+ "_co-investigator-network.graphml" + ".xml";
			
		} else {
			outputFileName = "no_co-investigator-network.graphml" + ".xml";			
		}
		
		CoPIGraphMLWriter coPIGraphMLWriter = 
				new CoPIGraphMLWriter(coPIData);
		
        Map<String, String> fileData = new HashMap<String, String>();
        fileData.put(DataVisualizationController.FILE_NAME_KEY, 
				 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "text/xml");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
					 coPIGraphMLWriter.getCoPIGraphMLContent().toString());

		return fileData;
	
	}
	
}