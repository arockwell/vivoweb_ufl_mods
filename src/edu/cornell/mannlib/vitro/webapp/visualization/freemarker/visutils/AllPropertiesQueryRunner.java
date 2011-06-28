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
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.GenericQueryMap;



/**
 * This query runner is used to execute a sparql query that will fetch all the 
 * properties  available for the provided individual URI.
 * 
 * @author cdtank
 */
public class AllPropertiesQueryRunner implements QueryRunner<GenericQueryMap> {

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String filterRule, individualURI;
	private Dataset Dataset;

	private Log log;

	public AllPropertiesQueryRunner(String individualURI,
							   String filterRule,
							   Dataset Dataset, 
							   Log log) {

		this.individualURI = individualURI;
		this.filterRule = filterRule;
		this.Dataset = Dataset;
		this.log = log;
		
	}

	private GenericQueryMap createJavaValueObjects(ResultSet resultSet) {
		
		GenericQueryMap queryResult = new GenericQueryMap();
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();
			
			
			RDFNode predicateNode = solution.get(QueryFieldLabels.PREDICATE);
			RDFNode objectNode = solution.get(QueryFieldLabels.OBJECT);
			
			if (predicateNode != null && objectNode != null) {
				queryResult.addEntry(predicateNode.toString(), 
									   objectNode.toString());
			} 
			
		}
		
		return queryResult;
	}

	private ResultSet executeQuery(String queryText,
								   Dataset Dataset) {

        QueryExecution queryExecution = null;
        Query query = QueryFactory.create(queryText, SYNTAX);

        queryExecution = QueryExecutionFactory.create(query, Dataset);
        return queryExecution.execSelect();
    }

	private String generateGenericSparqlQuery(String queryURI, String filterRule) {
//		Resource uri1 = ResourceFactory.createResource(queryURI);
		String filterClause;
		
		if (StringUtils.isNotBlank(filterRule)) {
			filterClause = "FILTER ( " + filterRule + " ) . ";
		} else {
			filterClause = "";			
		}

		String sparqlQuery = QueryConstants.getSparqlPrefixQuery()
							+ "SELECT "
							+ "		(str(?predicate) as ?" + QueryFieldLabels.PREDICATE + ") " 
							+ "		(str(?object) as ?" + QueryFieldLabels.OBJECT + ") "
							+ "WHERE { "
							+ "<" + queryURI + "> ?predicate ?object.  "
							+ filterClause
							+ "}";

		return sparqlQuery;
	}
	
	public GenericQueryMap getQueryResult()
			throws MalformedQueryParametersException {
		if (StringUtils.isNotBlank(this.individualURI)) {
        	/*
        	 * To test for the validity of the URI submitted.
        	 * */
        	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
    		IRI iri = iRIFactory.create(this.individualURI);
            if (iri.hasViolation(false)) {
                String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
                log.error("Generic Query " + errorMsg);
                throw new MalformedQueryParametersException(
                			"URI provided for an individual is malformed.");
            }
            
        } else {
            throw new MalformedQueryParametersException("URI parameter is either null or empty.");
        }

		ResultSet resultSet	= executeQuery(generateGenericSparqlQuery(
												this.individualURI, 
												this.filterRule),
										   this.Dataset);

		return createJavaValueObjects(resultSet);
	}
}
