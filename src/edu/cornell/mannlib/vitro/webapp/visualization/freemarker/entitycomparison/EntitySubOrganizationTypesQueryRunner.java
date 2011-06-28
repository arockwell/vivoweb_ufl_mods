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
package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.entitycomparison;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.hp.hpl.jena.rdf.model.Model;


import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryFieldLabels;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;

import java.util.Set;
import java.util.HashSet;


/**
 * @author bkoniden
 * Deepak Konidena
 */
public class EntitySubOrganizationTypesQueryRunner implements QueryRunner<Map<String, Set<String>>> {
	
	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String entityURI;
	private Model dataSource;
	private Log log = LogFactory.getLog(EntitySubOrganizationTypesQueryRunner.class.getName());
	
	private static final String SPARQL_QUERY_SELECT_CLAUSE = ""
		+ "		(str(?organizationLabel) as ?"+QueryFieldLabels.ORGANIZATION_LABEL+") "
		+ "		(str(?subOrganizationLabel) as ?"+QueryFieldLabels.SUBORGANIZATION_LABEL+") "
		+ "		(str(?subOrganizationType) as ?"+QueryFieldLabels.SUBORGANIZATION_TYPE +")"
		+ "		(str(?subOrganizationTypeLabel) as ?"+QueryFieldLabels.SUBORGANIZATION_TYPE_LABEL+") "
		+ " 	(str(?Person) as ?personLit) "            
		+ "		(str(?PersonLabel) as ?personLabelLit) "                      
		+ "		(str(?PersonTypeLabel) as ?personTypeLabelLit) ";

	
	public EntitySubOrganizationTypesQueryRunner(String entityURI,
			Model dataSource, Log log){
		
		this.entityURI = entityURI;
		this.dataSource = dataSource;
	//	this.log = log;
	}
	
	private ResultSet executeQuery(String queryURI, Model dataSource) {

		QueryExecution queryExecution = null;
		Query query = QueryFactory.create(
				getSparqlQuery(queryURI), SYNTAX);
		queryExecution = QueryExecutionFactory.create(query, dataSource);
			return queryExecution.execSelect();
	}
	
	private String getSparqlQuery(String queryURI) {

		String sparqlQuery = "";

		sparqlQuery = QueryConstants.getSparqlPrefixQuery()
				+ "SELECT "
				+ SPARQL_QUERY_SELECT_CLAUSE
				+ " WHERE { "
				+ "<"
				+ queryURI
				+ "> rdfs:label ?organizationLabel . "
				+ "{ "
				+ "<"+ queryURI + "> core:hasSubOrganization ?subOrganization .  "
				+ "?subOrganization rdfs:label ?subOrganizationLabel ; rdf:type ?subOrganizationType ;" 
				+ " core:organizationForPosition ?Position . "
				+ "?subOrganizationType rdfs:label ?subOrganizationTypeLabel . "
				+ "?Position core:positionForPerson ?Person ."
				+ "}"
				+ "UNION "
				+ "{ "
				+ "<"+ queryURI + "> core:organizationForPosition ?Position . "
				+ "?Position core:positionForPerson ?Person . "
				+ "?Person  rdfs:label ?PersonLabel ; rdf:type ?PersonType . "
				+ "?PersonType rdfs:label ?PersonTypeLabel . "
				+ "}"
				+ "}";

		
	//	log.debug("\n SubOrganizationTypesQuery :" + sparqlQuery);

		return sparqlQuery;

	}
	
	private Map<String, Set<String>> createJavaValueObjects(ResultSet resultSet) {

	//	Map<String, Set<String>> subOrganizationLabelToTypes = new HashMap<String, Set<String>>();
	//	Map<String, Set<String>> personLabelToTypes = new HashMap<String, Set<String>>();
		Map<String, Set<String>> subEntityLabelToTypes = new HashMap<String, Set<String>>();
		
		while(resultSet.hasNext()){
			
			QuerySolution solution = resultSet.nextSolution();
			
			RDFNode subOrganizationLabel = solution.get(QueryFieldLabels.SUBORGANIZATION_LABEL);
			
			if (subOrganizationLabel != null) {
				
				if (subEntityLabelToTypes.containsKey(subOrganizationLabel.toString())) {
					RDFNode subOrganizationType = solution
							.get(QueryFieldLabels.SUBORGANIZATION_TYPE_LABEL);
					if (subOrganizationType != null) {
						subEntityLabelToTypes.get(
								subOrganizationLabel.toString()).add(
								subOrganizationType.toString());
					}
				} else {
					RDFNode subOrganizationType = solution
							.get(QueryFieldLabels.SUBORGANIZATION_TYPE_LABEL);
					if (subOrganizationType != null) {
						subEntityLabelToTypes.put(
								subOrganizationLabel.toString(),
								new HashSet<String>());
						subEntityLabelToTypes.get(
								subOrganizationLabel.toString()).add(
								subOrganizationType.toString());
					}
				}
			}

			RDFNode personLabel = solution.get(QueryFieldLabels.PERSON_LABEL);

			if (personLabel != null) {
				if (subEntityLabelToTypes.containsKey(personLabel.toString())) {
					RDFNode personType = solution
							.get(QueryFieldLabels.PERSON_TYPE_LABEL);
					if (personType != null
							&& !personType.toString().startsWith("http")) {
						subEntityLabelToTypes.get(personLabel.toString()).add(
								personType.toString());
					}
				} else {
					RDFNode personType = solution
							.get(QueryFieldLabels.PERSON_TYPE_LABEL);
					if (personType != null
							&& !personType.toString().startsWith("http")) {
						subEntityLabelToTypes.put(personLabel.toString(),
								new HashSet<String>());
						subEntityLabelToTypes.get(personLabel.toString()).add(
								personType.toString());
					}
				}
			}			
		}		
		
//		System.out.println("\n\nSub Organization Label Types Size --> " + subOrganizationLabelToTypes.size());
//		System.out.println("\n\nPeople Label Types Size --> " + personLabelToTypes.size());
		
//		log.debug("Sub Organization Label Types Size : " + subEntityLabelToTypes.size());
		
		return subEntityLabelToTypes;
		//return (subOrganizationLabelToTypes.size() != 0 )? subOrganizationLabelToTypes : personLabelToTypes ;
	}

	public Map<String, Set<String>> getQueryResult() throws MalformedQueryParametersException {

		if (StringUtils.isNotBlank(this.entityURI)) {

			/*
			 * To test for the validity of the URI submitted.
			 */
			IRIFactory iRIFactory = IRIFactory.jenaImplementation();
			IRI iri = iRIFactory.create(this.entityURI);
			if (iri.hasViolation(false)) {
				String errorMsg = ((Violation) iri.violations(false).next())
						.getShortMessage();
				log.error("Entity Comparison sub organization types query " + errorMsg);
				throw new MalformedQueryParametersException(
						"URI provided for an entity is malformed.");
			}

		} else {
			throw new MalformedQueryParametersException(
					"URL parameter is either null or empty.");
		}

		ResultSet resultSet = executeQuery(this.entityURI, this.dataSource);

		return createJavaValueObjects(resultSet);
	}

}

