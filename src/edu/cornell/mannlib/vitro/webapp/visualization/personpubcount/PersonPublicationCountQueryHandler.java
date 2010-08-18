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

package edu.cornell.mannlib.vitro.webapp.visualization.personpubcount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.query.DataSource;
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
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Individual;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.QueryHandler;



/**
 * @author cdtank
 *
 */
public class PersonPublicationCountQueryHandler implements QueryHandler<List<BiboDocument>> {

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String queryParam;
	private DataSource dataSource;

	private Individual author; 

	public Individual getAuthor() {
		return author;
	}

	private Log log;

	private static final String SPARQL_QUERY_COMMON_SELECT_CLAUSE = "" 
			+ "SELECT (str(?authorLabel) as ?authorLabelLit) " 
			+ "		(str(?document) as ?documentLit) " 
			+ "		(str(?documentMoniker) as ?documentMonikerLit) " 
			+ "		(str(?documentLabel) as ?documentLabelLit) " 
			+ "		(str(?documentBlurb) as ?documentBlurbLit) " 
			+ "		(str(?publicationYear) as ?publicationYearLit) " 
			+ "		(str(?publicationYearMonth) as ?publicationYearMonthLit) " 
			+ "		(str(?publicationDate) as ?publicationDateLit) " 
			+ "		(str(?documentDescription) as ?documentDescriptionLit) ";

	private static final String SPARQL_QUERY_COMMON_WHERE_CLAUSE = "" 
			+ "?document rdf:type bibo:Document ." 
			+ "?document rdfs:label ?documentLabel ." 
			+ "OPTIONAL {  ?document core:year ?publicationYear } ." 
			+ "OPTIONAL {  ?document core:yearMonth ?publicationYearMonth } ." 
			+ "OPTIONAL {  ?document core:date ?publicationDate } ." 
			+ "OPTIONAL {  ?document vitro:moniker ?documentMoniker } ." 
			+ "OPTIONAL {  ?document vitro:blurb ?documentBlurb } ." 
			+ "OPTIONAL {  ?document vitro:description ?documentDescription }";
	
	public PersonPublicationCountQueryHandler(String queryParam,
			DataSource dataSource, Log log) {

		this.queryParam = queryParam;
		this.dataSource = dataSource;
		this.log = log;

	}

	private List<BiboDocument> createJavaValueObjects(ResultSet resultSet) {
		List<BiboDocument> authorDocuments = new ArrayList<BiboDocument>();
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();

			BiboDocument biboDocument = new BiboDocument(
											solution.get(QueryFieldLabels.DOCUMENT_URL)
												.toString());

			RDFNode documentLabelNode = solution.get(QueryFieldLabels.DOCUMENT_LABEL);
			if (documentLabelNode != null) {
				biboDocument.setDocumentLabel(documentLabelNode.toString());
			}


			RDFNode documentBlurbNode = solution.get(QueryFieldLabels.DOCUMENT_BLURB);
			if (documentBlurbNode != null) {
				biboDocument.setDocumentBlurb(documentBlurbNode.toString());
			}

			RDFNode documentmonikerNode = solution.get(QueryFieldLabels.DOCUMENT_MONIKER);
			if (documentmonikerNode != null) {
				biboDocument.setDocumentMoniker(documentmonikerNode.toString());
			}

			RDFNode documentDescriptionNode = solution.get(QueryFieldLabels.DOCUMENT_DESCRIPTION);
			if (documentDescriptionNode != null) {
				biboDocument.setDocumentDescription(documentDescriptionNode.toString());
			}

			RDFNode publicationYearNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR);
			if (publicationYearNode != null) {
				biboDocument.setPublicationYear(publicationYearNode.toString());
			}
			
			RDFNode publicationYearMonthNode = solution.get(
													QueryFieldLabels
															.DOCUMENT_PUBLICATION_YEAR_MONTH);
			if (publicationYearMonthNode != null) {
				biboDocument.setPublicationYearMonth(publicationYearMonthNode.toString());
			}
			
			RDFNode publicationDateNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_DATE);
			if (publicationDateNode != null) {
				biboDocument.setPublicationDate(publicationDateNode.toString());
			}
			
			/*
			 * Since we are getting publication count for just one author at a time we need
			 * to create only one "Individual" instance. We test against the null for "author" to
			 * make sure that it has not already been instantiated. 
			 * */
			RDFNode authorURLNode = solution.get(QueryFieldLabels.AUTHOR_URL);
			if (authorURLNode != null && author == null) {
				author = new Individual(authorURLNode.toString());
				RDFNode authorLabelNode = solution.get(QueryFieldLabels.AUTHOR_LABEL);
				if (authorLabelNode != null) {
					author.setIndividualLabel(authorLabelNode.toString());
				}
				
			}

			authorDocuments.add(biboDocument);
		}
		return authorDocuments;
	}

	private ResultSet executeQuery(String queryURI,
            DataSource dataSource) {

        QueryExecution queryExecution = null;
        try {
            Query query = QueryFactory.create(generateSparqlQuery(queryURI), SYNTAX);

//            QuerySolutionMap qs = new QuerySolutionMap();
//            qs.add("authPerson", queryParam); // bind resource to s
            
            queryExecution = QueryExecutionFactory.create(query, dataSource);
            

            if (query.isSelectType()) {
                return queryExecution.execSelect();
            }
        } finally {
            if (queryExecution != null) {
            	queryExecution.close();
            }
        }
		return null;
    }

	private String generateSparqlQuery(String queryURI) {
//		Resource uri1 = ResourceFactory.createResource(queryURI);

		String sparqlQuery = QueryConstants.getSparqlPrefixQuery()
							+ SPARQL_QUERY_COMMON_SELECT_CLAUSE
							+ "(str(<" + queryURI + ">) as ?authPersonLit) "
							+ "WHERE { "
							+ "<" + queryURI + "> rdf:type foaf:Person ;" 
							+ 					" rdfs:label ?authorLabel ;" 
							+ 					" core:authorInAuthorship ?authorshipNode .  " 
							+ "	?authorshipNode rdf:type core:Authorship ;" 
							+ 					" core:linkedInformationResource ?document . "
							+  SPARQL_QUERY_COMMON_WHERE_CLAUSE
							+ "}";

//		System.out.println("SPARQL query for person pub count -> \n" + sparqlQuery);
		return sparqlQuery;
	}

	public List<BiboDocument> getVisualizationJavaValueObjects()
		throws MalformedQueryParametersException {

        if (StringUtils.isNotBlank(this.queryParam)) {

        	/*
        	 * To test for the validity of the URI submitted.
        	 * */
        	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
    		IRI iri = iRIFactory.create(this.queryParam);
            if (iri.hasViolation(false)) {
                String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
                log.error("Pub Count vis Query " + errorMsg);
                throw new MalformedQueryParametersException(
                		"URI provided for an individual is malformed.");
            }
        	
        } else {
        	throw new MalformedQueryParametersException("URL parameter is either null or empty.");
        }

		ResultSet resultSet	= executeQuery(this.queryParam,
										   this.dataSource);

		return createJavaValueObjects(resultSet);
	}

	public Map<String, Integer> getYearToPublicationCount(
			List<BiboDocument> authorDocuments) {

		//List<Integer> publishedYears = new ArrayList<Integer>();

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
    		 * 	3. We are asking for a publication year & we should get a proper one or NOT at all.
    		 * */
    		String publicationYear;
    		if (curr.getPublicationYear() != null) {
    			publicationYear = curr.getPublicationYear();
    		} else {
    			publicationYear = curr.getParsedPublicationYear();
    		}
    		
			if (yearToPublicationCount.containsKey(publicationYear)) {
    			yearToPublicationCount.put(publicationYear,
    									   yearToPublicationCount
    									   		.get(publicationYear) + 1);

    		} else {
    			yearToPublicationCount.put(publicationYear, 1);
    		}

//    		if (!parsedPublicationYear.equalsIgnoreCase(BiboDocument.DEFAULT_PUBLICATION_YEAR)) {
//    			publishedYears.add(Integer.parseInt(parsedPublicationYear));
//    		}

    	}

		return yearToPublicationCount;
	}





}
