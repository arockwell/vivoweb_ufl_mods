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
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.BiboDocument;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Entity;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SubEntity;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;


/**
 * This query runner is used to execute a sparql query that will fetch all the
 * publications defined by bibo:Document property for a particular
 * department/school/university.
 * 
 * Deepak Konidena.
 * @author bkoniden
 */
public class EntityPublicationCountQueryRunner implements QueryRunner<Entity> {

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String entityURI;
	private Model dataSource;
	private Log log = LogFactory.getLog(EntityPublicationCountQueryRunner.class.getName());
	private long before, after;

	private static final String SPARQL_QUERY_COMMON_SELECT_CLAUSE = ""
			+ "		(str(?Person) as ?personLit) "
			+ "		(str(?PersonLabel) as ?personLabelLit) "
			+ "		(str(?Document) as ?documentLit) "
			+ "		(str(?publicationDate) as ?" + QueryFieldLabels.DOCUMENT_PUBLICATION_DATE + ") ";
//			+ "		(str(?publicationYearUsing_1_1_property) as ?" + QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR_USING_1_1_PROPERTY + ") ";


	private static final String SPARQL_QUERY_COMMON_WHERE_CLAUSE = ""
			+ "?Document rdf:type bibo:Document . "
			+ "OPTIONAL {  ?Document core:dateTimeValue ?dateTimeValue . " 
			+ "				?dateTimeValue core:dateTime ?publicationDate } .";
//			+ "OPTIONAL {  ?Document core:year ?publicationYearUsing_1_1_property } ." ;

	private static String ENTITY_LABEL;
	private static String ENTITY_URL;
	private static String SUBENTITY_LABEL;
	private static String SUBENTITY_URL;

	public EntityPublicationCountQueryRunner(String entityURI,
			Model dataSource, Log log) {

		this.entityURI = entityURI;
		this.dataSource = dataSource;
//		this.log = log;

	}

	private Entity createJavaValueObjects(ResultSet resultSet) {

		Entity entity = null;
		Map<String, BiboDocument> biboDocumentURLToVO = new HashMap<String, BiboDocument>();
		Map<String, SubEntity> subentityURLToVO = new HashMap<String, SubEntity>();
		Map<String, SubEntity> personURLToVO = new HashMap<String, SubEntity>();

		before = System.currentTimeMillis();

		while (resultSet.hasNext()) {

			QuerySolution solution = resultSet.nextSolution();

			if (entity == null) {
				entity = new Entity(solution.get(ENTITY_URL).toString(),
						solution.get(ENTITY_LABEL).toString());
			}

			RDFNode documentNode = solution.get(QueryFieldLabels.DOCUMENT_URL);
			BiboDocument biboDocument;

			if (biboDocumentURLToVO.containsKey(documentNode.toString())) {
				biboDocument = biboDocumentURLToVO.get(documentNode.toString());

			} else {

				biboDocument = new BiboDocument(documentNode.toString());
				biboDocumentURLToVO.put(documentNode.toString(), biboDocument);

				RDFNode publicationDateNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_DATE);
				if (publicationDateNode != null) {
					biboDocument.setPublicationDate(publicationDateNode.toString());
				}

				/*
				 * This is being used so that date in the data from pre-1.2 ontology can be captured. 
				 * */
//				RDFNode publicationYearUsing_1_1_PropertyNode = solution.get(QueryFieldLabels.DOCUMENT_PUBLICATION_YEAR_USING_1_1_PROPERTY);
//				if (publicationYearUsing_1_1_PropertyNode != null) {
//					biboDocument.setPublicationYear(publicationYearUsing_1_1_PropertyNode.toString());
//				}

			}

			RDFNode subEntityURLNode = solution.get(SUBENTITY_URL);
			
			if (subEntityURLNode != null) {
				SubEntity subEntity;
				if (subentityURLToVO.containsKey(subEntityURLNode.toString())) {
					subEntity = subentityURLToVO.get(subEntityURLNode
							.toString());
				} else {
					subEntity = new SubEntity(subEntityURLNode.toString());
					subentityURLToVO
							.put(subEntityURLNode.toString(), subEntity);
				}

				RDFNode subEntityLabelNode = solution.get(SUBENTITY_LABEL);
				if (subEntityLabelNode != null) {
					subEntity.setIndividualLabel(subEntityLabelNode.toString());
				}
				
				entity.addSubEntity(subEntity);
				
				subEntity.addPublication(biboDocument);
			}
			
			RDFNode personURLNode = solution.get(QueryFieldLabels.PERSON_URL);
			
			if (personURLNode != null) {
				SubEntity person;
				
				if (personURLToVO.containsKey(personURLNode.toString())) {
					person = personURLToVO.get(personURLNode.toString());
				} else {
					person = new SubEntity(personURLNode.toString());
					personURLToVO.put(personURLNode.toString(), person);
				}
				
				RDFNode personLabelNode = solution.get(QueryFieldLabels.PERSON_LABEL);
				if (personLabelNode != null) {
					person.setIndividualLabel(personLabelNode.toString());
				}
				
				/*
				 * This makes sure that either,
				 * 		1. the parent organization is a department-like organization with no organizations 
				 * beneath it, or 
				 * 		2. the parent organizations has both sub-organizations and people directly 
				 * attached to that organizations e.g. president of a university.
				 * */
				if (subEntityURLNode == null) {

					entity.addSubEntity(person);
					
				}
				
				person.addPublication(biboDocument);				

			}			

			entity.addPublication(biboDocument);
		}
		
		/*
		if (subentityURLToVO.size() != 0) {
			
			entity.addSubEntitities(subentityURLToVO.values());
			
		} else if (subentityURLToVO.size() == 0 && personURLToVO.size() != 0) {
			
			entity.addSubEntitities(personURLToVO.values());
			
		} else*/ if (subentityURLToVO.size() == 0 && personURLToVO.size() == 0) {
			
			entity = new Entity(this.entityURI, "no-label");
			
		}
		
		//TODO: return non-null value
		log.debug("Returning entity that contains the following set of subentities: "+entity.getSubEntities().toString());
		after = System.currentTimeMillis();
		log.debug("Time taken to iterate through the ResultSet of SELECT queries is in milliseconds: " + (after - before) );

		return entity;
	}
		
	private ResultSet executeQuery(String queryURI, Model dataSource) {

		QueryExecution queryExecution = null;
		Query query = QueryFactory.create(
				getSparqlQuery(queryURI), SYNTAX);
		queryExecution = QueryExecutionFactory.create(query, dataSource);
		return queryExecution.execSelect();
	}

	private String getSparqlQuery(String queryURI) {
		
		String result = "";
			
		ENTITY_URL = QueryFieldLabels.ORGANIZATION_URL;
		ENTITY_LABEL = QueryFieldLabels.ORGANIZATION_LABEL;
		SUBENTITY_URL = QueryFieldLabels.SUBORGANIZATION_URL;
		SUBENTITY_LABEL = QueryFieldLabels.SUBORGANIZATION_LABEL;

		result = getSparqlQueryForOrganization(queryURI);

		return result;
	}

	
	private String getSparqlQueryForOrganization(String queryURI){
		
		String sparqlQuery = QueryConstants.getSparqlPrefixQuery()
		+ "SELECT 	(str(?organizationLabel) as ?organizationLabelLit) "
		+ "	 		(str(?subOrganization) as ?subOrganizationLit) "
		+ "			(str(?subOrganizationLabel) as ?subOrganizationLabelLit) "
		+ SPARQL_QUERY_COMMON_SELECT_CLAUSE + "		(str(<" + queryURI
		+ ">) as ?" + ENTITY_URL + ") "
		+ "WHERE { " + "<" + queryURI + "> rdfs:label ?organizationLabel ."
		+ "{ "
		+ "<" + queryURI + "> core:hasSubOrganization ?subOrganization ."
		+ "?subOrganization rdfs:label ?subOrganizationLabel ; core:organizationForPosition ?Position . "
		+ " ?Position core:positionForPerson ?Person ."
		+ " ?Person  core:authorInAuthorship ?Resource ;   rdfs:label ?PersonLabel . "
		+ " ?Resource core:linkedInformationResource ?Document .  "
		+ SPARQL_QUERY_COMMON_WHERE_CLAUSE + "}"
		+ "UNION "
		+ "{ "
		+ "<" + queryURI + "> core:organizationForPosition ?Position ."
		+ " ?Position core:positionForPerson ?Person ."
		+ "	?Person  core:authorInAuthorship ?Resource ;   rdfs:label ?PersonLabel . "
		+ " ?Resource core:linkedInformationResource ?Document ."
		+ SPARQL_QUERY_COMMON_WHERE_CLAUSE + "}"
		+ "}";
		
		//System.out.println("\n\nEntity Pub Count query is: "+ sparqlQuery);
		log.debug("\nThe sparql query is :\n" + sparqlQuery);
		
		return sparqlQuery;

	}
	
	public Entity getQueryResult() throws MalformedQueryParametersException {

		if (StringUtils.isNotBlank(this.entityURI)) {

			/*
			 * To test for the validity of the URI submitted.
			 */
			IRIFactory iRIFactory = IRIFactory.jenaImplementation();
			IRI iri = iRIFactory.create(this.entityURI);
			if (iri.hasViolation(false)) {
				String errorMsg = ((Violation) iri.violations(false).next())
						.getShortMessage();
				log.error("Entity Pub Count Query " + errorMsg);
				throw new MalformedQueryParametersException(
						"URI provided for an entity is malformed.");
			}

		} else {
			throw new MalformedQueryParametersException(
					"URL parameter is either null or empty.");
		}

		before = System.currentTimeMillis();
		
		ResultSet resultSet = executeQuery(this.entityURI, this.dataSource);
		
		after = System.currentTimeMillis();
		
		log.debug("Time taken to execute the SELECT queries is in milliseconds: " + (after - before) );
		
		return createJavaValueObjects(resultSet);
	}

}



