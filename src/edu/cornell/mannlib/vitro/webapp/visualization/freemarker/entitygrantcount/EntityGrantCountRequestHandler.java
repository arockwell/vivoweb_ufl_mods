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

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.entitygrantcount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.DataVisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.VOConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.entitycomparison.EntityComparisonUtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Entity;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.JsonObject;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SubEntity;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.VisualizationRequestHandler;


public class EntityGrantCountRequestHandler implements
		VisualizationRequestHandler {
	
	private Log log = LogFactory.getLog(EntityGrantCountRequestHandler.class.getName());
	
	@Override
	public ResponseValues generateStandardVisualization(
			VitroRequest vitroRequest, Log log, Dataset Dataset)
			throws MalformedQueryParametersException {
		
		String entityURI = vitroRequest
				.getParameter(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY);
		
		if (StringUtils.isBlank(entityURI)){
			
			entityURI = EntityComparisonUtilityFunctions
								.getStaffProvidedOrComputedHighestLevelOrganization(log, Dataset);
			
		}
		return prepareStandaloneMarkupResponse(vitroRequest, entityURI);
	}

	@Override
	public Map<String, String> generateDataVisualization(
			VitroRequest vitroRequest, Log log, Dataset Dataset)
			throws MalformedQueryParametersException {

		String entityURI = vitroRequest
				.getParameter(VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY);
		
		/*
		 * This will provide the data in json format mainly used for standalone tmeporal vis. 
		 * */
		if (VisualizationFrameworkConstants.TEMPORAL_GRAPH_JSON_DATA_VIS_MODE
					.equalsIgnoreCase(vitroRequest.getParameter(VisualizationFrameworkConstants.VIS_MODE_KEY))) {
			
			if (StringUtils.isNotBlank(entityURI)){
				
				return getSubjectEntityAndGenerateDataResponse(
								vitroRequest, 
								log,
								Dataset, 
								entityURI);
			} else {
				
				return getSubjectEntityAndGenerateDataResponse(
								vitroRequest, 
								log,
								Dataset,
								EntityComparisonUtilityFunctions
										.getStaffProvidedOrComputedHighestLevelOrganization(
												log,
												Dataset));
			}
			
		} else {
			/*
			 * This provides csv download files for the content in the tables.
			 * */		

		EntityGrantCountConstructQueryRunner constructQueryRunner = new EntityGrantCountConstructQueryRunner(entityURI, Dataset, log);
		Model constructedModel = constructQueryRunner.getConstructedModel();
		
		QueryRunner<Entity> queryManager = new EntityGrantCountQueryRunner(
				entityURI, constructedModel, log);	
		
		Entity entity = queryManager.getQueryResult();
		
		
		Map<String, Set<String>> subOrganizationTypesResult = EntityComparisonUtilityFunctions.getSubEntityTypes(
				log, Dataset, entityURI);

		return prepareDataResponse(entity, entity.getSubEntities(),subOrganizationTypesResult);
		
		}

	}
	
	@Override
	public Object generateAjaxVisualization(VitroRequest vitroRequest, Log log,
			Dataset Dataset) throws MalformedQueryParametersException {
		throw new UnsupportedOperationException("Entity Grant Count does not provide Ajax Response.");
	}
	
	private Map<String, String> getSubjectEntityAndGenerateDataResponse(
			VitroRequest vitroRequest, Log log, Dataset Dataset,
			String subjectEntityURI)
			throws MalformedQueryParametersException {
		
		EntityGrantCountConstructQueryRunner constructQueryRunner = new EntityGrantCountConstructQueryRunner(subjectEntityURI, Dataset, log);
		Model constructedModel = constructQueryRunner.getConstructedModel();
		
		QueryRunner<Entity> queryManager = new EntityGrantCountQueryRunner(
				subjectEntityURI, constructedModel, log);	

		Entity entity = queryManager.getQueryResult();
		
		if (entity.getEntityLabel().equals("no-label")) {
			return prepareStandaloneDataErrorResponse(vitroRequest, subjectEntityURI);
		} else {	
		
			return getSubEntityTypesAndComputeDataResponse(
					vitroRequest, log, Dataset,
					subjectEntityURI, entity);
		}
	}

	private Map<String, String> getSubEntityTypesAndComputeDataResponse(
			VitroRequest vitroRequest, Log log, Dataset Dataset,
			String subjectOrganization, Entity entity)
			throws MalformedQueryParametersException {
		
		Map<String, Set<String>> subOrganizationTypesResult = EntityComparisonUtilityFunctions.getSubEntityTypes(
				log, Dataset, subjectOrganization);
		
		return prepareStandaloneDataResponse(vitroRequest, entity, subOrganizationTypesResult);
	}
	
	
	private Map<String, String> prepareStandaloneDataErrorResponse(
			VitroRequest vitroRequest, String subjectEntityURI) {

		Map<String, String> fileData = new HashMap<String, String>();
		
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, "{\"error\" : \"No Grants for this Organization found in VIVO.\"}");
		return fileData;
	}
	
	
	private Map<String, String> prepareStandaloneDataResponse(
										VitroRequest vitroRequest, 
										Entity entity,
										Map<String, Set<String>> subOrganizationTypesResult) {

		Map<String, String> fileData = new HashMap<String, String>();
		
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
				writeGrantsOverTimeJSON(vitroRequest, entity.getSubEntities(), subOrganizationTypesResult));
		return fileData;
	}

	/**
	 * Provides response when json file containing the grant count over the
	 * years is requested.
	 * 
	 * @param entity
	 * @param subentities
	 * @param subOrganizationTypesResult
	 */
	private Map<String, String> prepareDataResponse(Entity entity, Set<SubEntity> subentities,
			Map<String, Set<String>> subOrganizationTypesResult) {

		String entityLabel = entity.getEntityLabel();

		/*
		* To make sure that null/empty records for entity names do not cause any mischief.
		* */
		if (StringUtils.isBlank(entityLabel)) {
			entityLabel = "no-organization";
		}
		
		String outputFileName = UtilityFunctions.slugify(entityLabel)
				+ "_grants-per-year" + ".csv";
		
		
		Map<String, String> fileData = new HashMap<String, String>();
		
		fileData.put(DataVisualizationController.FILE_NAME_KEY, 
					 outputFileName);
		fileData.put(DataVisualizationController.FILE_CONTENT_TYPE_KEY, 
					 "application/octet-stream");
		fileData.put(DataVisualizationController.FILE_CONTENT_KEY, 
				getEntityGrantsPerYearCSVContent(subentities, subOrganizationTypesResult));
		return fileData;
	}
	

	private TemplateResponseValues prepareStandaloneMarkupResponse(VitroRequest vreq,
			   String entityURI) {

		Portal portal = vreq.getPortal();
		String standaloneTemplate = "entityComparisonOnGrantsStandalone.ftl";
		
		String organizationLabel = EntityComparisonUtilityFunctions.getEntityLabelFromDAO(vreq,
											  entityURI);
		
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("portalBean", portal);
		body.put("title", organizationLabel + " - Temporal Graph Visualization");
		body.put("organizationURI", entityURI);
		body.put("organizationLabel", organizationLabel);
		
		return new TemplateResponseValues(standaloneTemplate, body);
	}
	
	/**
	 * @deprecated This method should not be called anymore although the templates being 
	 * called by this method are still in use, so we should not get rid of it.
	 * @return
	 */
	private ResponseValues prepareStandaloneErrorResponse(
			VitroRequest vitroRequest, String entityURI) {
		
        Portal portal = vitroRequest.getPortal();
        String standaloneTemplate = "entityGrantComparisonError.ftl";
        Map<String, Object> body = new HashMap<String, Object>();
        
        String organizationLabel = EntityComparisonUtilityFunctions.getEntityLabelFromDAO(vitroRequest,
				entityURI);
        
        body.put("organizationLabel", organizationLabel);
        
        
        body.put("portalBean", portal);
        body.put("title", organizationLabel + " - Temporal Graph Visualization");
        body.put("organizationURI", entityURI);
        
        return new TemplateResponseValues(standaloneTemplate, body);
	}	
	
	
	/**
	 * function to generate a json file for year <-> grant count mapping
	 * @param vreq 
	 * @param subentities
	 * @param subOrganizationTypesResult  
	 */
	private String writeGrantsOverTimeJSON(VitroRequest vreq, Set<SubEntity> subentities, Map<String, Set<String>> subOrganizationTypesResult) {

		Gson json = new Gson();
		Set<JsonObject> subEntitiesJson = new HashSet<JsonObject>();

		for (SubEntity subentity : subentities) {
			JsonObject entityJson = new JsonObject(
					subentity.getIndividualLabel());

			List<List<Integer>> yearGrantCount = new ArrayList<List<Integer>>();

			for (Map.Entry<String, Integer> grantEntry : UtilityFunctions
					.getYearToGrantCount(subentity.getGrants())
					.entrySet()) {

				List<Integer> currentGrantYear = new ArrayList<Integer>();
				if (grantEntry.getKey().equals(
						VOConstants.DEFAULT_GRANT_YEAR)) {
					currentGrantYear.add(-1);
				} else {
					currentGrantYear.add(Integer.parseInt(grantEntry.getKey()));
				}
					
				currentGrantYear.add(grantEntry.getValue());
				yearGrantCount.add(currentGrantYear);
			}

		//	log.info("entityJson.getLabel() : " + entityJson.getLabel() + " subOrganizationTypesResult " + subOrganizationTypesResult.toString());
			
			entityJson.setYearToActivityCount(yearGrantCount);
			entityJson.getOrganizationType().addAll(subOrganizationTypesResult.get(entityJson.getLabel()));

			entityJson.setEntityURI(subentity.getIndividualURI());
			
			boolean isPerson = UtilityFunctions.isEntityAPerson(vreq, subentity);
			
			if(isPerson){
				entityJson.setVisMode("PERSON");
			} else{
				entityJson.setVisMode("ORGANIZATION");
			}			
			
//			setEntityVisMode(entityJson);
			subEntitiesJson.add(entityJson);
		}
		
	//	System.out.println("\nStopWords are "+ EntitySubOrganizationTypesQueryRunner.stopWords.toString() + "\n");
		return json.toJson(subEntitiesJson);

	}
	
	
	private String getEntityGrantsPerYearCSVContent(Set<SubEntity> subentities, Map<String, Set<String>> subOrganizationTypesResult) {

		StringBuilder csvFileContent = new StringBuilder();
		
		csvFileContent.append("Entity Name, Grant Count, Entity Type\n");
		
		for(SubEntity subEntity : subentities){
			
			csvFileContent.append(StringEscapeUtils.escapeCsv(subEntity.getIndividualLabel()));
			csvFileContent.append(", ");
			csvFileContent.append(subEntity.getGrants().size());
			csvFileContent.append(", ");
			
			StringBuilder joinedTypes = new StringBuilder();
			
			for(String subOrganizationType : subOrganizationTypesResult.get(subEntity.getIndividualLabel())){
				joinedTypes.append(subOrganizationType + "; ");
			}
			
			csvFileContent.append(StringEscapeUtils.escapeCsv(joinedTypes.toString()));
			csvFileContent.append("\n");
		}	

		return csvFileContent.toString();
	}	
}