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

import java.util.HashSet;
import java.util.LinkedHashSet;
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

public class EntitySubOrganizationTypesConstructQueryRunner {
	
	protected static final Syntax SYNTAX = Syntax.syntaxARQ;
	
	private String egoURI;
	
	private Dataset Dataset;

	private Log log = LogFactory.getLog(EntitySubOrganizationTypesConstructQueryRunner.class.getName());
	
	public EntitySubOrganizationTypesConstructQueryRunner(String egoURI, Dataset Dataset, Log log){
		this.egoURI = egoURI;
		this.Dataset = Dataset;
		//this.log = log;
	}	
	
	private String generateConstructQueryForOrganizationLabel(String queryURI) {
		
		String sparqlQuery = 
			 "CONSTRUCT { " 
			+	"<"+queryURI+ ">  rdfs:label ?organizationLabel ."
			+ "}"	
			+ "WHERE {"
			+	"<"+queryURI+ ">  rdfs:label ?organizationLabel "
			+ "}";
		return sparqlQuery;
	}
	
	private String generateConstructQueryForSubOrganizationTypes(String queryURI){
	    
		String sparqlQuery = 			 
		
			"CONSTRUCT { "
			+	"<"+queryURI+ "> core:hasSubOrganization ?subOrganization . "
			+	"?subOrganization rdfs:label ?subOrganizationLabel . "
			+	"?subOrganization rdf:type ?subOrganizationType . "
			+ 	"?subOrganization core:organizationForPosition ?Position . "
			+   "?subOrganizationType rdfs:label ?subOrganizationTypeLabel . "
			+   "?Position core:positionForPerson ?Person ."
			+ 	"?Person rdfs:label ?PersonLabel ."
			+	"?Person rdf:type ?PersonType . "
			+ 	"?PersonType rdfs:label ?PersonTypeLabel  "
			+"}"
			+ "WHERE { "
			+	"<"+queryURI+ "> core:hasSubOrganization ?subOrganization . "
			+	"?subOrganization rdfs:label ?subOrganizationLabel . "
			+	"?subOrganization rdf:type ?subOrganizationType . "
		    +   "?subOrganizationType rdfs:label ?subOrganizationTypeLabel . "
			+ 	"?subOrganization core:organizationForPosition ?Position . "	
			+	"?Position core:positionForPerson ?Person .	"
			+   "?Person rdfs:label ?PersonLabel ."
			+   "?Person rdf:type ?PersonType . "
			+   "?PersonType rdfs:label ?PersonTypeLabel  "         

			+ "}" ;
					
		
		return sparqlQuery;
	
	}
	
	private String generateConstructQueryForPersonTypes(String queryURI){
		
		String sparqlQuery = 			 
		
			"CONSTRUCT { "
			+	"<"+queryURI+ "> core:organizationForPosition ?Position . "
			+   "?Position core:positionForPerson ?Person ."
			+ 	"?Person rdfs:label ?PersonLabel ."
			+	"?Person rdf:type ?PersonType . "
			+ 	"?PersonType rdfs:label ?PersonTypeLabel  "
			+"}"
			+ "WHERE { "
			+	"<"+queryURI+ "> core:organizationForPosition ?Position . "
			+	"?Position core:positionForPerson ?Person ."
			+ 	"?Person rdfs:label ?PersonLabel ."
			+	"?Person rdf:type ?PersonType . "
			+ 	"?PersonType rdfs:label ?PersonTypeLabel  "			
			+ "}" ;
					
		
		return sparqlQuery;
	
	}	
	
	private Model executeQuery(Set<String> constructQueries, Dataset Dataset) {
		
        Model constructedModel = ModelFactory.createDefaultModel();
        long before = 0;
        
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
        	
            log.debug("Time to run " + (before - System.currentTimeMillis()) );
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
	
	Set<String> constructQueries = new LinkedHashSet<String>();
	
	populateConstructQueries(constructQueries);
	
	Model model	= executeQuery(constructQueries,
									   this.Dataset);
	//model.write(System.out);
	return model;
		
	}

	private void populateConstructQueries(Set<String> constructQueries) {
		
		constructQueries.add(generateConstructQueryForOrganizationLabel(this.egoURI));
		constructQueries.add(generateConstructQueryForSubOrganizationTypes(this.egoURI));
		constructQueries.add(generateConstructQueryForPersonTypes(this.egoURI));
		
		
	}	

}
