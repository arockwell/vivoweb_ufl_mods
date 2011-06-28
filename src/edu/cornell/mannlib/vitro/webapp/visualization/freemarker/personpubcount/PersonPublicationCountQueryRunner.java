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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.personpubcount;

import java.util.HashSet;
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
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Individual;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;



/**
 * This query runner is used to execute a sparql query that will fetch all the publications
 * defined by bibo:Document property for a particular individual.
 * 
 * @author cdtank
 */
public class PersonPublicationCountQueryRunner implements QueryRunner<Set<BiboDocument>> {

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String personURI;
	private Dataset Dataset;

	private Individual author; 

	public Individual getAuthor() {
		return author;
	}

	private Log log;

	private static final String SPARQL_QUERY_COMMON_SELECT_CLAUSE = "" 
			+ "SELECT (str(?authorLabel) as ?" + QueryFieldLabels.AUTHOR_LABEL + ") \n" 
			+ "		(str(?document) as ?" + QueryFieldLabels.DOCUMENT_URL + ") \n" 			 			 			 
			+ "		(str(?publicationDate) as ?" + QueryFieldLabels.DOCUMENT_PUBLICATION_DATE + ") \n";
		//	+ "		(str(?publicationYearUsing_1_1_property) as ?" + QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR_USING_1_1_PROPERTY + ") \n";			

	private static final String SPARQL_QUERY_COMMON_WHERE_CLAUSE = "" 
			+ "?document rdfs:label ?documentLabel .\n" 
			+ "OPTIONAL {  ?document core:dateTimeValue ?dateTimeValue . \n" 
			+ "				?dateTimeValue core:dateTime ?publicationDate } .\n" ;
			//+ "OPTIONAL {  ?document core:year ?publicationYearUsing_1_1_property } ." ;
	
	public PersonPublicationCountQueryRunner(String personURI,
			Dataset Dataset, Log log) {

		this.personURI = personURI;
		this.Dataset = Dataset;
		this.log = log;

	}

	private Set<BiboDocument> createJavaValueObjects(ResultSet resultSet) {
		Set<BiboDocument> authorDocuments = new HashSet<BiboDocument>();
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();

			BiboDocument biboDocument = new BiboDocument(
											solution.get(QueryFieldLabels.DOCUMENT_URL)
												.toString());

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
            Dataset Dataset) {

        QueryExecution queryExecution = null;
        Query query = QueryFactory.create(getSparqlQuery(queryURI), SYNTAX);
        queryExecution = QueryExecutionFactory.create(query, Dataset);
        return queryExecution.execSelect();
    }

	private String getSparqlQuery(String queryURI) {

		String sparqlQuery = QueryConstants.getSparqlPrefixQuery()
							+ SPARQL_QUERY_COMMON_SELECT_CLAUSE
							+ "(str(<" + queryURI + ">) as ?authPersonLit)\n "
							+ "WHERE { \n"
							+ "<" + queryURI + "> rdf:type foaf:Person ;\n" 
							+ 					" rdfs:label ?authorLabel \n;" 
							+ 					" core:authorInAuthorship ?authorshipNode .  \n" 
							+ "	?authorshipNode rdf:type core:Authorship ;" 
							+ 					" core:linkedInformationResource ?document . \n"
							+  SPARQL_QUERY_COMMON_WHERE_CLAUSE
							+ "}\n";

		log.debug(sparqlQuery);
		
		return sparqlQuery;
	}

	public Set<BiboDocument> getQueryResult()
		throws MalformedQueryParametersException {

        if (StringUtils.isNotBlank(this.personURI)) {

        	/*
        	 * To test for the validity of the URI submitted.
        	 * */
        	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
    		IRI iri = iRIFactory.create(this.personURI);
            if (iri.hasViolation(false)) {
                String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
                log.error("Pub Count vis Query " + errorMsg);
                throw new MalformedQueryParametersException(
                		"URI provided for an individual is malformed.");
            }
        	
        } else {
        	throw new MalformedQueryParametersException("URL parameter is either null or empty.");
        }

		ResultSet resultSet	= executeQuery(this.personURI,
										   this.Dataset);

		return createJavaValueObjects(resultSet);
	}

}
