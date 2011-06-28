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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryFieldLabels;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.CoAuthorshipData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Edge;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Node;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UniqueIDGenerator;

/**
 * This query runner is used to execute a sparql query to get all the publications
 * for a particular individual. It will also fetch all the authors that worked
 * on that particular publication. 
 * 
 * @author cdtank
 */
public class CoAuthorshipQueryRunner implements QueryRunner<CoAuthorshipData> {

	private static final int MAX_AUTHORS_PER_PAPER_ALLOWED = 100;

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String egoURI;
	
	private Dataset Dataset;

	private Log log;

	private UniqueIDGenerator nodeIDGenerator;

	private UniqueIDGenerator edgeIDGenerator;

	public CoAuthorshipQueryRunner(String egoURI,
			Dataset Dataset, Log log) {

		this.egoURI = egoURI;
		this.Dataset = Dataset;
		this.log = log;
		
		this.nodeIDGenerator = new UniqueIDGenerator();
		this.edgeIDGenerator = new UniqueIDGenerator();

	}

	private CoAuthorshipData createQueryResult(ResultSet resultSet) {
		
		Set<Node> nodes = new HashSet<Node>();
		
		Map<String, BiboDocument> biboDocumentURLToVO = new HashMap<String, BiboDocument>();
		Map<String, Set<Node>> biboDocumentURLToCoAuthors = new HashMap<String, Set<Node>>();
		Map<String, Node> nodeURLToVO = new HashMap<String, Node>();
		Map<String, Edge> edgeUniqueIdentifierToVO = new HashMap<String, Edge>();
		
		Node egoNode = null;

		Set<Edge> edges = new HashSet<Edge>();
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();
			
			/*
			 * We only want to create only ONE ego node.
			 * */
			RDFNode egoAuthorURLNode = solution.get(QueryFieldLabels.AUTHOR_URL);
			if (nodeURLToVO.containsKey(egoAuthorURLNode.toString())) {

				egoNode = nodeURLToVO.get(egoAuthorURLNode.toString());
				
			} else {
				
				egoNode = new Node(egoAuthorURLNode.toString(), nodeIDGenerator);
				nodes.add(egoNode);
				nodeURLToVO.put(egoAuthorURLNode.toString(), egoNode);
				
				RDFNode authorLabelNode = solution.get(QueryFieldLabels.AUTHOR_LABEL);
				if (authorLabelNode != null) {
					egoNode.setNodeName(authorLabelNode.toString());
				}
			}
			
			RDFNode documentNode = solution.get(QueryFieldLabels.DOCUMENT_URL);
			BiboDocument biboDocument;
			
			if (biboDocumentURLToVO.containsKey(documentNode.toString())) {
				biboDocument = biboDocumentURLToVO.get(documentNode.toString());
			} else {
				biboDocument = createDocumentVO(solution, documentNode.toString());
				biboDocumentURLToVO.put(documentNode.toString(), biboDocument);	
			}
			
			egoNode.addAuthorDocument(biboDocument);
			
			/*
			 * After some discussion we concluded that for the purpose of this visualization
			 * we do not want a co-author node or edge if the publication has only one
			 * author and that happens to be the ego.
			 * */
			if (solution.get(QueryFieldLabels.AUTHOR_URL).toString().equalsIgnoreCase(
					solution.get(QueryFieldLabels.CO_AUTHOR_URL).toString())) {
				continue;
			}
			
			Node coAuthorNode;
			
			RDFNode coAuthorURLNode = solution.get(QueryFieldLabels.CO_AUTHOR_URL);
			if (nodeURLToVO.containsKey(coAuthorURLNode.toString())) {

				coAuthorNode = nodeURLToVO.get(coAuthorURLNode.toString());
				
			} else {
				
				coAuthorNode = new Node(coAuthorURLNode.toString(), nodeIDGenerator);
				nodes.add(coAuthorNode);
				nodeURLToVO.put(coAuthorURLNode.toString(), coAuthorNode);
				
				RDFNode coAuthorLabelNode = solution.get(QueryFieldLabels.CO_AUTHOR_LABEL);
				if (coAuthorLabelNode != null) {
					coAuthorNode.setNodeName(coAuthorLabelNode.toString());
				}
			}
			
			coAuthorNode.addAuthorDocument(biboDocument);
			
			Set<Node> coAuthorsForCurrentBiboDocument;
			
			if (biboDocumentURLToCoAuthors.containsKey(biboDocument.getDocumentURL())) {
				coAuthorsForCurrentBiboDocument = biboDocumentURLToCoAuthors
														.get(biboDocument.getDocumentURL());
			} else {
				coAuthorsForCurrentBiboDocument = new HashSet<Node>();
				biboDocumentURLToCoAuthors.put(biboDocument.getDocumentURL(), 
											   coAuthorsForCurrentBiboDocument);
			}
			
			coAuthorsForCurrentBiboDocument.add(coAuthorNode);
			
			Edge egoCoAuthorEdge = getExistingEdge(egoNode, coAuthorNode, edgeUniqueIdentifierToVO);
			
			/*
			 * If "egoCoAuthorEdge" is null it means that no edge exists in between the egoNode 
			 * & current coAuthorNode. Else create a new edge, add it to the edges set & add 
			 * the collaborator document to it.
			 * */
			if (egoCoAuthorEdge != null) {
				egoCoAuthorEdge.addCollaboratorDocument(biboDocument);
			} else {
				egoCoAuthorEdge = new Edge(egoNode, coAuthorNode, biboDocument, edgeIDGenerator);
				edges.add(egoCoAuthorEdge);
				edgeUniqueIdentifierToVO.put(
						getEdgeUniqueIdentifier(egoNode.getNodeID(),
												coAuthorNode.getNodeID()), 
						egoCoAuthorEdge);
			}
			
			
		}
		
		
		
		/*
		 * This method takes out all the authors & edges between authors that belong to documents 
		 * that have more than 100 authors. We conjecture that these papers do not provide much 
		 * insight. However, we have left the documents be.
		 * 
		 * This method side-effects "nodes" & "edges".  
		 * */
		removeLowQualityNodesAndEdges(nodes, 
									  biboDocumentURLToVO, 
									  biboDocumentURLToCoAuthors, 
									  edges);
		
		/*
		 * We need to create edges between 2 co-authors. E.g. On a paper there were 3 authors
		 * ego, A & B then we have already created edges like,
		 * 		ego - A
		 * 		ego - B
		 * The below sub-routine will take care of,
		 * 		A - B 
		 * 
		 * We are side-effecting "edges" here. The only reason to do this is because we are adding 
		 * edges en masse for all the co-authors on all the publications considered so far. The 
		 * other reason being we dont want to compare against 2 sets of edges (edges created before 
		 * & co-author edges created during the course of this method) when we are creating a new 
		 * edge.
		 * */
		createCoAuthorEdges(biboDocumentURLToVO, 
							biboDocumentURLToCoAuthors,
							edges,
							edgeUniqueIdentifierToVO);
		
		
		return new CoAuthorshipData(egoNode, nodes, edges);
	}

	private void removeLowQualityNodesAndEdges(Set<Node> nodes,
											   Map<String, BiboDocument> biboDocumentURLToVO,
											   Map<String, Set<Node>> biboDocumentURLToCoAuthors, 
											   Set<Edge> edges) {
		
		Set<Node> nodesToBeRemoved = new HashSet<Node>();
		for (Map.Entry<String, Set<Node>> currentBiboDocumentEntry 
					: biboDocumentURLToCoAuthors.entrySet()) {
				
				if (currentBiboDocumentEntry.getValue().size() > MAX_AUTHORS_PER_PAPER_ALLOWED) {
					
					BiboDocument currentBiboDocument = biboDocumentURLToVO
															.get(currentBiboDocumentEntry.getKey());
					
					Set<Edge> edgesToBeRemoved = new HashSet<Edge>();
					
					for (Edge currentEdge : edges) {
						Set<BiboDocument> currentCollaboratorDocuments = 
									currentEdge.getCollaboratorDocuments();
						
						if (currentCollaboratorDocuments.contains(currentBiboDocument)) {
							currentCollaboratorDocuments.remove(currentBiboDocument);
							if (currentCollaboratorDocuments.isEmpty()) {
								edgesToBeRemoved.add(currentEdge);
							}
						}
					}
						
					edges.removeAll(edgesToBeRemoved);

					for (Node currentCoAuthor : currentBiboDocumentEntry.getValue()) {
						currentCoAuthor.getAuthorDocuments().remove(currentBiboDocument);
						if (currentCoAuthor.getAuthorDocuments().isEmpty()) {
							nodesToBeRemoved.add(currentCoAuthor);
						}
					}
				}
		}
		nodes.removeAll(nodesToBeRemoved);
	}

	private void createCoAuthorEdges(
			Map<String, BiboDocument> biboDocumentURLToVO,
			Map<String, Set<Node>> biboDocumentURLToCoAuthors, Set<Edge> edges, 
			Map<String, Edge> edgeUniqueIdentifierToVO) {
		
		for (Map.Entry<String, Set<Node>> currentBiboDocumentEntry 
					: biboDocumentURLToCoAuthors.entrySet()) {
			
			/*
			 * If there was only one co-author (other than ego) then we dont have to create any 
			 * edges. so the below condition will take care of that.
			 * 
			 * We are restricting edges between co-author if a particular document has more than
			 * 100 co-authors. Our conjecture is that such edges do not provide any good insight
			 * & causes unnecessary computations causing the server to time-out.
			 * */
			if (currentBiboDocumentEntry.getValue().size() > 1 
					&& currentBiboDocumentEntry.getValue().size() 
							<= MAX_AUTHORS_PER_PAPER_ALLOWED) {
				
				
				Set<Edge> newlyAddedEdges = new HashSet<Edge>();
			
				/*
				 * In order to leverage the nested "for loop" for making edges between all the 
				 * co-authors we need to create a list out of the set first. 
				 * */
				List<Node> coAuthorNodes = new ArrayList<Node>(currentBiboDocumentEntry.getValue());
				Collections.sort(coAuthorNodes, new NodeComparator());
				
				int numOfCoAuthors = coAuthorNodes.size();
				
				for (int ii = 0; ii < numOfCoAuthors - 1; ii++) {
					for (int jj = ii + 1; jj < numOfCoAuthors; jj++) {
						
						Node coAuthor1 = coAuthorNodes.get(ii);
						Node coAuthor2 = coAuthorNodes.get(jj);
						
						Edge coAuthor1_2Edge = getExistingEdge(coAuthor1, 
															   coAuthor2, 
															   edgeUniqueIdentifierToVO);
						
						BiboDocument currentBiboDocument = biboDocumentURLToVO
																.get(currentBiboDocumentEntry
																			.getKey());
			
						if (coAuthor1_2Edge != null) {
							coAuthor1_2Edge.addCollaboratorDocument(currentBiboDocument);
						} else {
							coAuthor1_2Edge = new Edge(coAuthor1, 
													   coAuthor2, 
													   currentBiboDocument, 
													   edgeIDGenerator);
							newlyAddedEdges.add(coAuthor1_2Edge);
							edgeUniqueIdentifierToVO.put(
									getEdgeUniqueIdentifier(coAuthor1.getNodeID(),
															coAuthor2.getNodeID()), 
									coAuthor1_2Edge);
						}
					}
				}
				edges.addAll(newlyAddedEdges);
			}
			
		}
	}

	private Edge getExistingEdge(
					Node collaboratingNode1, 
					Node collaboratingNode2, 
					Map<String, Edge> edgeUniqueIdentifierToVO) {
		
		String edgeUniqueIdentifier = getEdgeUniqueIdentifier(collaboratingNode1.getNodeID(), 
															  collaboratingNode2.getNodeID());
		
		return edgeUniqueIdentifierToVO.get(edgeUniqueIdentifier);
		
	}

	private String getEdgeUniqueIdentifier(int nodeID1, int nodeID2) {

		String separator = "*"; 
		
		if (nodeID1 < nodeID2) {
			return nodeID1 + separator + nodeID2;
		} else {
			return nodeID2 + separator + nodeID1;
		}
			
	}

//	public Map<String, VivoCollegeOrSchool> getCollegeURLToVO() {
//		return collegeURLToVO;
//	}

	private BiboDocument createDocumentVO(QuerySolution solution, String documentURL) {

			BiboDocument biboDocument = new BiboDocument(documentURL);

			RDFNode publicationDateNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_DATE);
			if (publicationDateNode != null) {
				biboDocument.setPublicationDate(publicationDateNode.toString());
			}

			/*
			 * This is being used so that date in the data from pre-1.2 ontology can be captured. 
			 * */
//			RDFNode publicationYearUsing_1_1_PropertyNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR_USING_1_1_PROPERTY);
//			if (publicationYearUsing_1_1_PropertyNode != null) {
//				biboDocument.setPublicationYear(publicationYearUsing_1_1_PropertyNode.toString());
//			}
			
			return biboDocument;
	}
	
	private ResultSet executeQuery(String queryText,
								   Dataset Dataset) {

        QueryExecution queryExecution = null;
        Query query = QueryFactory.create(queryText, SYNTAX);

        queryExecution = QueryExecutionFactory.create(query, Dataset);
        return queryExecution.execSelect();
    }

	private String generateEgoCoAuthorshipSparqlQuery(String queryURI) {
//		Resource uri1 = ResourceFactory.createResource(queryURI);

		String sparqlQuery = QueryConstants.getSparqlPrefixQuery()
			+ "SELECT \n"
			+ "		(str(<" + queryURI + ">) as ?" + QueryFieldLabels.AUTHOR_URL + ") \n" 
			+ "		(str(?authorLabel) as ?" + QueryFieldLabels.AUTHOR_LABEL + ") \n" 
			+ "		(str(?coAuthorPerson) as ?" + QueryFieldLabels.CO_AUTHOR_URL + ") \n" 
			+ "		(str(?coAuthorPersonLabel) as ?" + QueryFieldLabels.CO_AUTHOR_LABEL + ") \n"
			+ "		(str(?document) as ?" + QueryFieldLabels.DOCUMENT_URL + ") \n"
			+ "		(str(?publicationDate) as ?" + QueryFieldLabels.DOCUMENT_PUBLICATION_DATE + ") \n"
		//	+ "		(str(?publicationYearUsing_1_1_property) as ?" + QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR_USING_1_1_PROPERTY + ") \n"
			+ "WHERE { \n"
			+ "<" + queryURI + "> rdf:type foaf:Person ;" 
								+ " rdfs:label ?authorLabel ;" 
								+ " core:authorInAuthorship ?authorshipNode . \n"
			+ "?authorshipNode rdf:type core:Authorship ;" 
								+ " core:linkedInformationResource ?document . \n"
			+ "?document core:informationResourceInAuthorship ?coAuthorshipNode . \n" 
			+ "?coAuthorshipNode core:linkedAuthor ?coAuthorPerson . \n" 
			+ "?coAuthorPerson rdfs:label ?coAuthorPersonLabel . \n"
			+ "OPTIONAL {  ?document core:dateTimeValue ?dateTimeValue . \n" 
			+ "				?dateTimeValue core:dateTime ?publicationDate } .\n" 
		//	+ "OPTIONAL {  ?document core:year ?publicationYearUsing_1_1_property } .\n" 
			+ "} \n" 
			+ "ORDER BY ?document ?coAuthorPerson\n";

		log.debug("COAUTHORSHIP QUERY - " + sparqlQuery);
		
		return sparqlQuery;
	}

	
	public CoAuthorshipData getQueryResult()
		throws MalformedQueryParametersException {

		if (StringUtils.isNotBlank(this.egoURI)) {
			/*
        	 * To test for the validity of the URI submitted.
        	 * */
        	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
    		IRI iri = iRIFactory.create(this.egoURI);
            if (iri.hasViolation(false)) {
                String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
                log.error("Ego Co-Authorship Vis Query " + errorMsg);
                throw new MalformedQueryParametersException(
                		"URI provided for an individual is malformed.");
            }
        } else {
            throw new MalformedQueryParametersException("URI parameter is either null or empty.");
        }

		ResultSet resultSet	= executeQuery(generateEgoCoAuthorshipSparqlQuery(this.egoURI),
										   this.Dataset);
		return createQueryResult(resultSet);
	}

}
