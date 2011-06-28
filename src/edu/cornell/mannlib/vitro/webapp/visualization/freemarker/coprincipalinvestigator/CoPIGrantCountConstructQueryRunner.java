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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;


public class CoPIGrantCountConstructQueryRunner {
	
	protected static final Syntax SYNTAX = Syntax.syntaxARQ;
	
	private String egoURI;
	
	private Dataset Dataset;
	
	private long before, after;

	private Log log = LogFactory.getLog(CoPIGrantCountConstructQueryRunner.class.getName());

	
	private static final String SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING = 
        		  "?Role core:roleIn ?Grant . "
                + "?Grant rdfs:label ?GrantLabel . "
                + "?Grant core:relatedRole ?RelatedRole . ";
	
	public CoPIGrantCountConstructQueryRunner(String egoURI, Dataset Dataset, Log log){
		this.egoURI = egoURI;
		this.Dataset = Dataset;
		//this.log = log;
	}

	private String generateConstructQueryForInvestigatorLabel(String queryURI) {
		
		String sparqlQuery = 
			 "CONSTRUCT { " 
			+	"<"+queryURI+ ">  rdfs:label ?investigatorLabel ."
			+ "}"	
			+ "WHERE {"
			+	"<"+queryURI+ ">  rdfs:label ?investigatorLabel "
			+ "}";
				
		return sparqlQuery;
	}
	
	private String generateConstructQueryForInvestigatorRoleOfProperty(String queryURI, String preboundProperty){
		
		String sparqlQuery = 
			 "CONSTRUCT { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING
			+   "?RelatedRole core:investigatorRoleOf ?coInvestigator ."
			+	"?coInvestigator rdfs:label ?coInvestigatorLabel . "
			+ "}"
			+ "WHERE { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING
			+   "?RelatedRole core:investigatorRoleOf ?coInvestigator ."
			+	"?coInvestigator rdfs:label ?coInvestigatorLabel . "
			+ "}";
		
		return sparqlQuery;
	}
	
	private String generateConstructQueryForPrincipalInvestigatorRoleOfProperty(String queryURI, String preboundProperty){
		
		String sparqlQuery = 
			 "CONSTRUCT { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING
			+   "?RelatedRole core:principalInvestigatorRoleOf ?coInvestigator ."
			+	"?coInvestigator rdfs:label ?coInvestigatorLabel . "
			+ "}"
			+ "WHERE { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING
			+   "?RelatedRole core:principalInvestigatorRoleOf ?coInvestigator ."
			+	"?coInvestigator rdfs:label ?coInvestigatorLabel . "
			+ "}";
		
		return sparqlQuery;
	}	
	
	private String generateConstructQueryForCoPrincipalInvestigatorRoleOfProperty(String queryURI, String preboundProperty){
		
		String sparqlQuery = 
			 "CONSTRUCT { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING
			+   "?RelatedRole core:co-PrincipalInvestigatorRoleOf ?coInvestigator ."
			+	"?coInvestigator rdfs:label ?coInvestigatorLabel . "
			+ "}"
			+ "WHERE { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	SPARQL_QUERY_COMMON_CONSTRUCT_AND_WHERE_STRING
			+   "?RelatedRole core:co-PrincipalInvestigatorRoleOf ?coInvestigator ."
			+	"?coInvestigator rdfs:label ?coInvestigatorLabel . "
			+ "}";
		
		return sparqlQuery;
	}	
	
	private String generateConstructQueryForDateTimeValueofRole(String queryURI, String preboundProperty){
		
		String sparqlQuery = 
			 "CONSTRUCT { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+ 	"?Role core:dateTimeInterval ?dateTimeIntervalValue . "
			+   "?dateTimeIntervalValue core:start ?startDate . "
			+	"?startDate core:dateTime ?startDateTimeValue . "
			+	"?dateTimeIntervalValue core:end ?endDate . "
			+	"?endDate core:dateTime ?endDateTimeValue . "
			+ "}"
			+ "WHERE { "
			+	"{"
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
				+ 	"?Role core:dateTimeInterval ?dateTimeIntervalValue . "
				+   "?dateTimeIntervalValue core:start ?startDate . "
				+	"?startDate core:dateTime ?startDateTimeValue . "
			+	"} UNION "
			+	"{"
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
				+ 	"?Role core:dateTimeInterval ?dateTimeIntervalValue . "
				+	"?dateTimeIntervalValue core:end ?endDate . "
				+	"?endDate core:dateTime ?endDateTimeValue . "			
			+ 	"}"
			+ "}";
		
		return sparqlQuery;
	}		
	
	private String generateConstructQueryForDateTimeValueofGrant(String queryURI, String preboundProperty){
		
		String sparqlQuery = 
			 "CONSTRUCT { "
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
			+	"?Role core:roleIn ?Grant ."
			+ 	"?Grant core:dateTimeInterval ?dateTimeIntervalValueForGrant . "
			+   "?dateTimeIntervalValueForGrant core:start ?startDateForGrant . "
			+	"?startDateForGrant core:dateTime ?startDateTimeValueForGrant . "
			+	"?dateTimeIntervalValueForGrant core:end ?endDateForGrant . "
			+	"?endDateForGrant core:dateTime ?endDateTimeValueForGrant . "
			+ "}"
			+ "WHERE { "
			+	"{"
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
				+	"?Role core:roleIn ?Grant ."
				+ 	"?Grant core:dateTimeInterval ?dateTimeIntervalValueForGrant . "
				+   "?dateTimeIntervalValueForGrant core:start ?startDateForGrant . "
				+	"?startDateForGrant core:dateTime ?startDateTimeValueForGrant . "
			+	"} UNION "
			+	"{"
			+	"<"+queryURI+ ">" + preboundProperty + " ?Role . "
				+	"?Role core:roleIn ?Grant ."
				+ 	"?Grant core:dateTimeInterval ?dateTimeIntervalValueForGrant . "
				+	"?dateTimeIntervalValueForGrant core:end ?endDateForGrant . "
				+	"?endDateForGrant core:dateTime ?endDateTimeValueForGrant . "			
			+ 	"}"
			+ "}";
		
		return sparqlQuery;
	}		
	
	private Model executeQuery(Set<String> constructQueries, Dataset Dataset) {
		
        Model constructedModel = ModelFactory.createDefaultModel();


        for (String queryString : constructQueries) {
            
            before = System.currentTimeMillis();

        	log.debug("CONSTRUCT query string : " + queryString);
            
        	Query query = null;
        	
        	try{
        		query = QueryFactory.create(QueryConstants.getSparqlPrefixQuery() + queryString, SYNTAX);
        	}catch(Throwable th){
                log.error("Could not create CONSTRUCT SPARQL query for query " +
                        "string. " + th.getMessage());
                log.error(queryString);
        	}
        	
            QueryExecution qe = QueryExecutionFactory.create(
                    query, Dataset);
            try {
                qe.execConstruct(constructedModel);
            } finally {
                qe.close();
            }
        	
            after = System.currentTimeMillis();
            log.debug("Time taken to execute the CONSTRUCT query is in milliseconds: " + (after - before) );

        }	

		return constructedModel;
	}	
	
	public Model getConstructedModel()
	throws MalformedQueryParametersException {

	if (StringUtils.isNotBlank(this.egoURI)) {
		/*
    	 * To test for the validity of the URI submitted.
    	 * */
    	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
		IRI iri = iRIFactory.create(this.egoURI);
        if (iri.hasViolation(false)) {
            String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
            log.error("Ego Co-PI Vis Query " + errorMsg);
            throw new MalformedQueryParametersException(
            		"URI provided for an individual is malformed.");
        }
    } else {
        throw new MalformedQueryParametersException("URI parameter is either null or empty.");
    }
	
	Set<String> constructQueries = new HashSet<String>();
	
	populateConstructQueries(constructQueries);
	
	Model model	= executeQuery(constructQueries,
									   this.Dataset);
	
	return model;
		
	}

	private void populateConstructQueries(Set<String> constructQueries) {
		
		constructQueries.add(generateConstructQueryForInvestigatorLabel(this.egoURI));
		constructQueries.add(generateConstructQueryForInvestigatorRoleOfProperty(this.egoURI, "core:hasInvestigatorRole"));
		constructQueries.add(generateConstructQueryForCoPrincipalInvestigatorRoleOfProperty(this.egoURI, "core:hasInvestigatorRole"));
		constructQueries.add(generateConstructQueryForPrincipalInvestigatorRoleOfProperty(this.egoURI, "core:hasInvestigatorRole"));
		constructQueries.add(generateConstructQueryForDateTimeValueofRole(this.egoURI, "core:hasInvestigatorRole"));
		constructQueries.add(generateConstructQueryForDateTimeValueofGrant(this.egoURI, "core:hasInvestigatorRole"));
		
		constructQueries.add(generateConstructQueryForInvestigatorRoleOfProperty(this.egoURI, "core:hasPrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForCoPrincipalInvestigatorRoleOfProperty(this.egoURI, "core:hasPrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForPrincipalInvestigatorRoleOfProperty(this.egoURI, "core:hasPrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForDateTimeValueofRole(this.egoURI, "core:hasPrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForDateTimeValueofGrant(this.egoURI, "core:hasPrincipalInvestigatorRole"));

		constructQueries.add(generateConstructQueryForInvestigatorRoleOfProperty(this.egoURI, "core:hasCo-PrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForCoPrincipalInvestigatorRoleOfProperty(this.egoURI, "core:hasCo-PrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForPrincipalInvestigatorRoleOfProperty(this.egoURI, "core:hasCo-PrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForDateTimeValueofRole(this.egoURI, "core:hasCo-PrincipalInvestigatorRole"));
		constructQueries.add(generateConstructQueryForDateTimeValueofGrant(this.egoURI, "core:hasCo-PrincipalInvestigatorRole"));
		
	}	
}