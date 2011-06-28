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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VisConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoAuthorshipData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoPIData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoPINode;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Grant;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Node;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SubEntity;

public class UtilityFunctions {
	
	public static Map<String, Integer> getYearToPublicationCount(
			Set<BiboDocument> authorDocuments) {

    	/*
    	 * Create a map from the year to number of publications. Use the BiboDocument's
    	 * parsedPublicationYear to populate the data.
    	 * */
    	Map<String, Integer> yearToPublicationCount = new TreeMap<String, Integer>();

    	for (BiboDocument curr : authorDocuments) {

    		/*
    		 * Increment the count because there is an entry already available for
    		 * that particular year.
    		 * 
    		 * I am pushing the logic to check for validity of year in "getPublicationYear" itself
    		 * because,
    		 * 	1. We will be using getPub... multiple times & this will save us duplication of code
    		 * 	2. If we change the logic of validity of a pub year we would not have to make 
    		 * changes all throughout the codebase.
    		 * 	3. We are asking for a publicationDate which is captured using the vivo 1.2 ontology 
    		 * & if not saved then we are being nice & checking if date is saved using core:year if so 
    		 * we use that else we return UNKOWN_YEAR.
    		 * */
    		String publicationYear = curr.getParsedPublicationYear();
    		
			if (yearToPublicationCount.containsKey(publicationYear)) {
    			yearToPublicationCount.put(publicationYear,
    									   yearToPublicationCount
    									   		.get(publicationYear) + 1);

    		} else {
    			yearToPublicationCount.put(publicationYear, 1);
    		}

    	}

		return yearToPublicationCount;
	}
	
	/**
	 * This method is used to return a mapping between publication year & all the co-authors
	 * that published with ego in that year. 
	 * @param authorNodesAndEdges
	 * @return
	 */
	public static Map<String, Set<Node>> getPublicationYearToCoAuthors(
										CoAuthorshipData authorNodesAndEdges) {

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
	
	/**
	 * Currently the approach for slugifying filenames is naive. In future if there is need, 
	 * we can write more sophisticated method.
	 * @param textToBeSlugified
	 * @return
	 */
	public static String slugify(String textToBeSlugified) {
		String textBlockSeparator = "-";
		return StringUtils.removeEnd(StringUtils.substring(textToBeSlugified.toLowerCase().trim()
											.replaceAll("[^a-zA-Z0-9-]+", textBlockSeparator), 
											0, 
											VisConstants.MAX_NAME_TEXT_LENGTH),
									 textBlockSeparator);
	}
	
	
    public static ResponseValues handleMalformedParameters(String errorPageTitle, 
    													   String errorMessage, 
    													   VitroRequest vitroRequest) {

        Portal portal = vitroRequest.getPortal();

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("portalBean", portal);
        body.put("error", errorMessage);
        body.put("title", errorPageTitle);
        
        return new TemplateResponseValues(VisualizationFrameworkConstants.ERROR_TEMPLATE, body);
    }
    
    public static void handleMalformedParameters(String errorPageTitle,
    											 String errorMessage,
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
		request.setAttribute("title", errorPageTitle);
		
		try {
			requestDispatcher.forward(request, response);
		} catch (Exception e) {
			log.error("EntityEditController could not forward to view.");
			log.error(e.getMessage());
			log.error(e.getStackTrace());
		}
	}
    
	public static Map<String, Set<CoPINode>> getGrantYearToCoPI(
			CoPIData pINodesAndEdges) {
		

		Map<String, Set<CoPINode>> yearToCoPIs = new TreeMap<String, Set<CoPINode>>();
		
		CoPINode egoNode = pINodesAndEdges.getEgoNode();
		
		for (CoPINode currNode : pINodesAndEdges.getNodes()) {
					
				/*
				 * We have already printed the Ego Node info.
				 * */
				if (currNode != egoNode) {
					
					for (String year : currNode.getYearToGrantCount().keySet()) {
						
						Set<CoPINode> coPINodes;
						
						if (yearToCoPIs.containsKey(year)) {
							
							coPINodes = yearToCoPIs.get(year);
							coPINodes.add(currNode);
							
						} else {
							
							coPINodes = new HashSet<CoPINode>();
							coPINodes.add(currNode);
							yearToCoPIs.put(year, coPINodes);
						}
						
					}
					
				}
		}
		return yearToCoPIs;

	}

	public static Map<String, Integer> getYearToGrantCount(Set<Grant> pIGrants) {
		
    	/*
    	 * Create a map from the year to number of grants. Use the Grant's
    	 * parsedGrantStartYear to populate the data.
    	 * */
    	Map<String, Integer> yearToGrantCount = new TreeMap<String, Integer>();

    	for (Grant curr : pIGrants) {

    		/*
    		 * Increment the count because there is an entry already available for
    		 * that particular year.
    		 * 
    		 * I am pushing the logic to check for validity of year in "getGrantYear" itself
    		 * because,
    		 * 	1. We will be using getGra... multiple times & this will save us duplication of code
    		 * 	2. If we change the logic of validity of a grant year we would not have to make 
    		 * changes all throughout the codebase.
    		 * 	3. We are asking for a grant year & we should get a proper one or NOT at all.
    		 * */
    		String grantYear = curr.getParsedGrantStartYear();
    		
			if (yearToGrantCount.containsKey(grantYear)) {
    			yearToGrantCount.put(grantYear,
    									   yearToGrantCount
    									   		.get(grantYear) + 1);

    		} else {
    			yearToGrantCount.put(grantYear, 1);
    		}

    	}

		return yearToGrantCount;
		
	}
	
	public static DateTime getValidParsedDateTimeObject(String unparsedDateTime) {
		
		for (DateTimeFormatter currentFormatter : VOConstants.POSSIBLE_DATE_TIME_FORMATTERS) {
			
			try {
				
				DateTime dateTime = currentFormatter.parseDateTime(unparsedDateTime);
				return dateTime;
				
			} catch (Exception e2) {
				/*
				 * The current date-time formatter did not pass the muster. 
				 * */
			}
		}
		
		/*
		 * This means that none of the date time formatters worked. 
		 * */
		return null;
	}
	
	public static String getCSVDownloadURL(String individualURI, String visType, String visMode) {
		
		ParamMap CSVDownloadURLParams = null;
		
		if (StringUtils.isBlank(visMode)) {
			
			CSVDownloadURLParams = new ParamMap(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY,
					 individualURI,
					 VisualizationFrameworkConstants.VIS_TYPE_KEY,
					 visType);
			
		} else {
			
			CSVDownloadURLParams = new ParamMap(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY,
					 individualURI,
					 VisualizationFrameworkConstants.VIS_TYPE_KEY,
					 visType,
					 VisualizationFrameworkConstants.VIS_MODE_KEY,
					 visMode);

		}
		
		String csvDownloadLink = UrlBuilder.getUrl(VisualizationFrameworkConstants.DATA_VISUALIZATION_SERVICE_URL_PREFIX,
								 CSVDownloadURLParams);
		
		return csvDownloadLink != null ? csvDownloadLink : "" ;

	}
	
	public static String getCollaboratorshipNetworkLink(String individualURI, String visType, String visMode) {
		
		ParamMap collaboratorshipNetworkURLParams = new ParamMap(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY,
				 individualURI,
				 VisualizationFrameworkConstants.VIS_TYPE_KEY,
				 visType,
				 VisualizationFrameworkConstants.VIS_MODE_KEY,
				 visMode);

		String collaboratorshipNetworkURL = UrlBuilder.getUrl(
										VisualizationFrameworkConstants.FREEMARKERIZED_VISUALIZATION_URL_PREFIX,
										collaboratorshipNetworkURLParams);
		
		return collaboratorshipNetworkURL != null ? collaboratorshipNetworkURL : "" ;
	}
	
	public static boolean isEntityAPerson(VitroRequest vreq, SubEntity subentity) {
		return vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI(subentity.getIndividualURI()).isVClass("http://xmlns.com/foaf/0.1/Person");
	}

}
