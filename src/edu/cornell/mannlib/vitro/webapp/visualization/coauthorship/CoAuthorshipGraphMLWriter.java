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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.CoAuthorshipVOContainer;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Edge;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Node;

public class CoAuthorshipGraphMLWriter {
	
    private static final Log log = LogFactory.getLog(CoAuthorshipGraphMLWriter.class);
    
	private StringBuilder coAuthorshipGraphMLContent;

	private final String GRAPHML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" 
			+ "	<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
			+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
	        + "  xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
	        + "  http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n\n";

	private final String GRAPHML_FOOTER = "</graphml>";
	
	public CoAuthorshipGraphMLWriter(CoAuthorshipVOContainer visVOContainer) {
		
		coAuthorshipGraphMLContent = createCoAuthorshipGraphMLContent(visVOContainer);
		
	}

	public StringBuilder getCoAuthorshipGraphMLContent() {
		return coAuthorshipGraphMLContent;
	}

	private StringBuilder createCoAuthorshipGraphMLContent(
			CoAuthorshipVOContainer visVOContainer) {
		
		StringBuilder graphMLContent = new StringBuilder();
		
		graphMLContent.append(GRAPHML_HEADER);
		
		/*
		 * We are side-effecting "graphMLContent" object in this method since creating 
		 * another String object to hold key definition data will be redundant & will
		 * not serve the purpose.
		 * */
		generateKeyDefinitionContent(visVOContainer, graphMLContent);
		
		/*
		 * Used to generate graph content. It will contain both the nodes & edge information.
		 * We are side-effecting "graphMLContent".
		 * */
		generateGraphContent(visVOContainer, graphMLContent);
		
		graphMLContent.append(GRAPHML_FOOTER);
		
		return graphMLContent;
	}

	private void generateGraphContent(CoAuthorshipVOContainer visVOContainer,
			StringBuilder graphMLContent) {

		graphMLContent.append("\n<graph edgedefault=\"undirected\">\n");
		
		if (visVOContainer.getNodes() != null & visVOContainer.getNodes().size() > 0) {
			generateNodeSectionContent(visVOContainer, graphMLContent);
		}
		
		if (visVOContainer.getEdges() != null & visVOContainer.getEdges().size() > 0) {
			generateEdgeSectionContent(visVOContainer, graphMLContent);
		}
		
		graphMLContent.append("</graph>\n");
		  
		
		
		
	}

	private void generateEdgeSectionContent(CoAuthorshipVOContainer visVOContainer,
			StringBuilder graphMLContent) {
		
		graphMLContent.append("<!-- edges -->\n");
		
		Set<Edge> edges = visVOContainer.getEdges();
		
		List<Edge> orderedEdges = new ArrayList<Edge>(edges);
		
		Collections.sort(orderedEdges, new EdgeComparator());

		for (Edge currentEdge : orderedEdges) {
			
			/*
			 * This method actually creates the XML code for a single edge. "graphMLContent"
			 * is being side-effected. 
			 * */
			getEdgeContent(graphMLContent, currentEdge);
			
		}
		
	}

	private void getEdgeContent(StringBuilder graphMLContent, Edge currentEdge) {
		
		/*
		log.debug("EDGE_ID:" + currentEdge.getEdgeID() + "|");
		log.debug("EDGE_SOURCE:" + currentEdge.getSourceNode().getNodeURL() + "|");
		log.debug("EDGE_TARGET:" + currentEdge.getTargetNode().getNodeURL());
		*/
		graphMLContent.append("<edge " 
									+ "id=\"" + currentEdge.getEdgeID() + "\" " 
									+ "source=\"" + currentEdge.getSourceNode().getNodeID() + "\" "
									+ "target=\"" + currentEdge.getTargetNode().getNodeID() + "\" "
									+ ">\n");
		
		graphMLContent.append("\t<data key=\"collaborator1\">" 
								+ currentEdge.getSourceNode().getNodeName() 
								+ "</data>\n");
		
		graphMLContent.append("\t<data key=\"collaborator2\">" 
								+ currentEdge.getTargetNode().getNodeName() 
								+ "</data>\n");
		
		graphMLContent.append("\t<data key=\"number_of_coauthored_works\">" 
								+ currentEdge.getNumOfCoAuthoredWorks()
							+ "</data>\n");
		
		if (currentEdge.getEarliestCollaborationYearCount() != null) {
			
			/*
			 * There is no clean way of getting the map contents in java even though
			 * we are sure to have only one entry on the map. So using the for loop.
			 * I am feeling dirty just about now. 
			 * */
			for (Map.Entry<String, Integer> publicationInfo
						: currentEdge.getEarliestCollaborationYearCount().entrySet()) {
				
				graphMLContent.append("\t<data key=\"earliest_collaboration\">" 
											+ publicationInfo.getKey() 
										+ "</data>\n");

				graphMLContent.append("\t<data key=\"num_earliest_collaboration\">" 
											+ publicationInfo.getValue() 
										+ "</data>\n");
				
				
			}
			
		}
		
		if (currentEdge.getLatestCollaborationYearCount() != null) {
			
			for (Map.Entry<String, Integer> publicationInfo 
						: currentEdge.getLatestCollaborationYearCount().entrySet()) {
				
				graphMLContent.append("\t<data key=\"latest_collaboration\">" 
											+ publicationInfo.getKey() 
										+ "</data>\n");

				graphMLContent.append("\t<data key=\"num_latest_collaboration\">" 
											+ publicationInfo.getValue() 
										+ "</data>\n");
			}
			
		}
		
		if (currentEdge.getUnknownCollaborationYearCount() != null) {
			
				graphMLContent.append("\t<data key=\"num_unknown_collaboration\">" 
											+ currentEdge.getUnknownCollaborationYearCount() 
										+ "</data>\n");
				
		}
		
		graphMLContent.append("</edge>\n");
		
		
	}

	private void generateNodeSectionContent(CoAuthorshipVOContainer visVOContainer,
			StringBuilder graphMLContent) {
		
		graphMLContent.append("<!-- nodes -->\n");
		
		Node egoNode = visVOContainer.getEgoNode();
		Set<Node> authorNodes = visVOContainer.getNodes();
		
		/*
		 * This method actually creates the XML code for a single node. "graphMLContent"
		 * is being side-effected. The egoNode is added first because this is the "requirement"
		 * of the co-author vis. Ego should always come first.
		 * 
		 * */
		getNodeContent(graphMLContent, egoNode);
		
		List<Node> orderedAuthorNodes = new ArrayList<Node>(authorNodes);
		orderedAuthorNodes.remove(egoNode);
		
		Collections.sort(orderedAuthorNodes, new NodeComparator());
		
		
		for (Node currNode : orderedAuthorNodes) {
			
			/*
			 * We have already printed the Ego Node info.
			 * */
			if (currNode != egoNode) {
				
				getNodeContent(graphMLContent, currNode);
				
			}
			
		}
		
	}

	private void getNodeContent(StringBuilder graphMLContent, Node node) {
		
		String profileURL = null;
		try {
			profileURL = "/individual?" 
								+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
								+ "=" + URLEncoder.encode(node.getNodeURL(),
														  VisualizationController
														  		.URL_ENCODING_SCHEME).toString();
		} catch (UnsupportedEncodingException e) {
			log.warn(e,e);
		}
		
		
//		log.debug("NODE_ID:" + node.getNodeID() + "|");
		graphMLContent.append("<node id=\"" + node.getNodeID() + "\">\n");
		graphMLContent.append("\t<data key=\"url\">" + node.getNodeURL() + "</data>\n");
//		graphMLContent.append("\t<data key=\"name\">" + node.getNodeName() + "</data>\n");
//		log.debug("NODE_NAME:" + node.getNodeURL());
		graphMLContent.append("\t<data key=\"label\">" + node.getNodeName() + "</data>\n");
		
		if (profileURL != null) {
			graphMLContent.append("\t<data key=\"profile_url\">" + profileURL + "</data>\n");
		}
		
		
		graphMLContent.append("\t<data key=\"number_of_authored_works\">" 
								+ node.getNumOfAuthoredWorks() 
							+ "</data>\n");
		
		if (node.getEarliestPublicationYearCount() != null) {
			
			/*
			 * There is no clean way of getting the map contents in java even though
			 * we are sure to have only one entry on the map. So using the for loop.
			 * I am feeling dirty just about now. 
			 * */
			for (Map.Entry<String, Integer> publicationInfo 
						: node.getEarliestPublicationYearCount().entrySet()) {
				
				graphMLContent.append("\t<data key=\"earliest_publication\">" 
											+ publicationInfo.getKey() 
										+ "</data>\n");

				graphMLContent.append("\t<data key=\"num_earliest_publication\">" 
											+ publicationInfo.getValue() 
										+ "</data>\n");
				
				
			}
			
		}
		
		if (node.getLatestPublicationYearCount() != null) {
			
			for (Map.Entry<String, Integer> publicationInfo 
						: node.getLatestPublicationYearCount().entrySet()) {
				
				graphMLContent.append("\t<data key=\"latest_publication\">" 
											+ publicationInfo.getKey() 
										+ "</data>\n");

				graphMLContent.append("\t<data key=\"num_latest_publication\">" 
											+ publicationInfo.getValue() 
										+ "</data>\n");
				
				
			}
			
		}
		
		if (node.getUnknownPublicationYearCount() != null) {
			
				graphMLContent.append("\t<data key=\"num_unknown_publication\">" 
											+ node.getUnknownPublicationYearCount() 
										+ "</data>\n");
				
		}
		
		graphMLContent.append("</node>\n");
	}

	private void generateKeyDefinitionContent(CoAuthorshipVOContainer visVOContainer, 
											  StringBuilder graphMLContent) {
		
		/*
		 * Generate the key definition content for node. 
		 * */
		getKeyDefinitionFromSchema(visVOContainer.getNodeSchema(), graphMLContent);
		
		/*
		 * Generate the key definition content for edge. 
		 * */
		getKeyDefinitionFromSchema(visVOContainer.getEdgeSchema(), graphMLContent);
		
		
	}

	private void getKeyDefinitionFromSchema(Set<Map<String, String>> schema,
			StringBuilder graphMLContent) {
		
		for (Map<String, String> currentNodeSchemaAttribute : schema) {
			
			graphMLContent.append("\n<key ");
			
			for (Map.Entry<String, String> currentAttributeKey 
						: currentNodeSchemaAttribute.entrySet()) {
				
				graphMLContent.append(currentAttributeKey.getKey() 
										+ "=\"" + currentAttributeKey.getValue() 
										+ "\" ");

			}
			
			if (currentNodeSchemaAttribute.containsKey("default")) {
				
				graphMLContent.append(">\n");
				graphMLContent.append("<default>");
				graphMLContent.append(currentNodeSchemaAttribute.get("default"));
				graphMLContent.append("</default>\n");
				graphMLContent.append("</key>\n");
				
			} else {
				graphMLContent.append("/>\n");
			}
			
		}
	}

}
