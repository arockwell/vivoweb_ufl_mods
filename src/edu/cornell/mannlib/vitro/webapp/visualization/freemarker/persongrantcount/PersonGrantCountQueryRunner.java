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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.persongrantcount;

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
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Grant;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Individual;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;


/**
 * This query runner is used to execute Sparql query that will fetch all the grants for an individual
 * @author bkoniden
 * Deepak Konidena
 *
 */
public class PersonGrantCountQueryRunner implements QueryRunner<Set<Grant>>{
	
	protected static final Syntax SYNTAX = Syntax.syntaxARQ;
	
	private String personURI;
	private Dataset Dataset;
	private Individual principalInvestigator;
	
	public Individual getPrincipalInvestigator(){
		return principalInvestigator;
	}
	
	private Log log;
	
	private static final String SPARQL_QUERY_COMMON_SELECT_CLAUSE = ""
						+ " SELECT (str(?PILabel) as ?PILabelLit) "
						+ " (str(?Grant) as ?grantLit)"
						+ " (str(?GrantLabel) as ?grantLabelLit)"
						+ " (str(?startDateTimeValue) as ?grantStartDateLit) "
						+ "	(str(?endDateTimeValue) as ?grantEndDateLit)  "
						+ " (str(?startDateTimeValueForGrant) as ?grantStartDateForGrantLit) "
						+ "	(str(?endDateTimeValueForGrant) as ?grantEndDateForGrantLit)  ";
	


	private static final String SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_ROLE_DATE_TIME = ""
		+ 		"OPTIONAL {"		
		+ "			?Role core:dateTimeInterval ?dateTimeIntervalValue . "
		+			"?dateTimeIntervalValue core:start ?startDate . "		
		+			"?startDate core:dateTime ?startDateTimeValue . " 	
		+			"OPTIONAL {"	
		+				"?dateTimeIntervalValue core:end ?endDate . "	
		+				"?endDate core:dateTime ?endDateTimeValue . " 			
		+			"}"
		+ 		"} . "	;	
	
	private static final String SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_GRANT_DATE_TIME = ""
		+ 		"OPTIONAL {"	
		+ "			?Grant core:dateTimeInterval ?dateTimeIntervalValueForGrant . "
		+			"?dateTimeIntervalValueForGrant core:start ?startDateForGrant . "		
		+			"?startDateForGrant core:dateTime ?startDateTimeValueForGrant . " 	
		+			"OPTIONAL {"	
		+				"?dateTimeIntervalValueForGrant core:end ?endDateForGrant . "	
		+				"?endDateForGrant core:dateTime ?endDateTimeValueForGrant . " 			
		+			"}"
		+ 		"}"	;	
	
	
	public PersonGrantCountQueryRunner(String personURI, Dataset Dataset, Log log){
		
		this.personURI = personURI;
		this.Dataset = Dataset;
		this.log = log;
	}
	
	private Set<Grant> createJavaValueObjects(ResultSet resultSet){
		Set<Grant> PIGrant = new HashSet<Grant>();
		
		while(resultSet.hasNext()){
			QuerySolution solution = resultSet.nextSolution();
			
			Grant grant = new Grant(solution.get(QueryFieldLabels.GRANT_URL).toString());
			
			RDFNode grantLabelNode = solution.get(QueryFieldLabels.GRANT_LABEL);
			if(grantLabelNode != null){
				grant.setIndividualLabel(grantLabelNode.toString());
			}
			
			RDFNode grantStartDateNode = solution.get(QueryFieldLabels.ROLE_START_DATE);
			if(grantStartDateNode != null){
				grant.setGrantStartDate(grantStartDateNode.toString());
			}else {
				grantStartDateNode = solution.get(QueryFieldLabels.GRANT_START_DATE);
				if(grantStartDateNode != null){
					grant.setGrantStartDate(grantStartDateNode.toString());
				}
			}
			
			RDFNode grantEndDateNode = solution.get(QueryFieldLabels.ROLE_END_DATE);
			if(grantEndDateNode != null){
				grant.setGrantEndDate(grantEndDateNode.toString());
			}else {
				grantEndDateNode = solution.get(QueryFieldLabels.GRANT_END_DATE);
				if(grantEndDateNode != null){
					grant.setGrantEndDate(grantEndDateNode.toString());
				}				
			}
			
			/*
			 * Since we are getting grant count for just one PI at a time we need
			 * to create only one "Individual" instance. We test against the null for "PI" to
			 * make sure that it has not already been instantiated. 
			 * */
			RDFNode PIURLNode = solution.get(QueryFieldLabels.PI_URL);
			if (PIURLNode != null && principalInvestigator == null) {
				principalInvestigator = new Individual(PIURLNode.toString());
				RDFNode PILabelNode = solution.get(QueryFieldLabels.PI_LABEL);
				if (PILabelNode != null) {
					principalInvestigator.setIndividualLabel(PILabelNode.toString());
				}
			}
			
			PIGrant.add(grant);
		}
		return PIGrant;
	}
	
	private ResultSet executeQuery(String queryURI, Dataset Dataset){
		
		QueryExecution queryExecution = null;
		
		Query query = QueryFactory.create(getSparqlQuery(queryURI), SYNTAX);
		queryExecution = QueryExecutionFactory.create(query,Dataset);
		
		return queryExecution.execSelect();
	}
	


	private String getSparqlQuery(String queryURI){
		
		String sparqlQuery = QueryConstants.getSparqlPrefixQuery()
							
							+ SPARQL_QUERY_COMMON_SELECT_CLAUSE
							
							+ "(str(<" + queryURI + ">) as ?PILit) "
							
							+ "WHERE "
							+ "{ "  	
							+ 		"<" + queryURI + "> rdfs:label ?PILabel . "  	
							+  		"{ "
							        	
							+			"<" + queryURI + "> core:hasCo-PrincipalInvestigatorRole ?Role . "

							+			"?Role core:roleIn ?Grant . "

							+			"?Grant rdfs:label ?GrantLabel . "
					
							+ 			SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_ROLE_DATE_TIME
							
							+			SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_GRANT_DATE_TIME
							
							+ 		"} "
								
							+		"UNION "
									
							+		"{ "
							        	
							+			"<" + queryURI + "> core:hasPrincipalInvestigatorRole ?Role . "

							+			"?Role core:roleIn ?Grant . "

							+			"?Grant rdfs:label ?GrantLabel . "	
	
							+ 			SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_ROLE_DATE_TIME
							
							+			SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_GRANT_DATE_TIME
							
							
							+ 		"} "
							
							+		"UNION "
							
							+		"{ "
							        	
							+			"<" + queryURI + "> core:hasInvestigatorRole ?Role . "

							+			"?Role core:roleIn ?Grant . "

							+			"?Grant rdfs:label ?GrantLabel . "	
							
							+ 			SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_ROLE_DATE_TIME
							
							+			SPARQL_QUERY_COMMON_OPTIONAL_BLOCK_FOR_GRANT_DATE_TIME
							
							
							+ 		"} "

							+ "} ";
		
		log.debug("SPARQL query for person grant count -> \n"+ sparqlQuery);
		//System.out.println("SPARQL query for person grant count -> \n"+ sparqlQuery);
		
		return sparqlQuery;
	}
	
	public Set<Grant> getQueryResult() throws MalformedQueryParametersException{
		
		if(StringUtils.isNotBlank(this.personURI)){
			
			/*
			 * To test the validity of the URI submitted
			 */
			IRIFactory iriFactory = IRIFactory.jenaImplementation();
			IRI iri = iriFactory.create(this.personURI);
			
			if(iri.hasViolation(false)){
				String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
                log.error("Grant Count vis Query " + errorMsg);
                throw new MalformedQueryParametersException(
                		"URI provided for an individual is malformed.");				
			}
		} else {
			throw new MalformedQueryParametersException("URL parameter is either null or empty.");
		}
		
		ResultSet resultSet = executeQuery(this.personURI, this.Dataset);
		
		return createJavaValueObjects(resultSet);
	}
	
}
