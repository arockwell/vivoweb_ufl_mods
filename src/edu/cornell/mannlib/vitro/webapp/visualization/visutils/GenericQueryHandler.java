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

package edu.cornell.mannlib.vitro.webapp.visualization.visutils;

import java.util.Map;

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
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;



/**
 * @author cdtank
 *
 */
public class GenericQueryHandler implements QueryHandler<ResultSet> {

	protected static final Syntax SYNTAX = Syntax.syntaxARQ;

	private String whereClause, individualURLParam;
	private DataSource dataSource;

	private Log log;

	private Map<String, String> fieldLabelToOutputFieldLabel;

	public GenericQueryHandler(String individualURLParam,
							   Map<String, String> fieldLabelToOutputFieldLabel, 
							   String whereClause,
							   DataSource dataSource, 
							   Log log) {

		this.individualURLParam = individualURLParam;
		this.fieldLabelToOutputFieldLabel = fieldLabelToOutputFieldLabel;
		this.whereClause = whereClause;
		this.dataSource = dataSource;
		this.log = log;
		
	}

	private ResultSet executeQuery(String queryText,
								   DataSource dataSource) {

        QueryExecution queryExecution = null;
        try {
            Query query = QueryFactory.create(queryText, SYNTAX);

//            QuerySolutionMap qs = new QuerySolutionMap();
//            qs.add("authPerson", queryParam); // bind resource to s
            
            queryExecution = QueryExecutionFactory.create(query, dataSource);
            

            //remocve this if loop after knowing what is describe & construct sparql stuff.
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

	private String generateGenericSparqlQuery() {
//		Resource uri1 = ResourceFactory.createResource(queryURI);

		StringBuilder sparqlQuery = new StringBuilder();
		sparqlQuery.append(QueryConstants.getSparqlPrefixQuery());
		
		sparqlQuery.append("SELECT\n");
		
		for (Map.Entry<String, String> currentfieldLabelToOutputFieldLabel 
				: this.fieldLabelToOutputFieldLabel.entrySet()) {
			
			sparqlQuery.append("\t(str(?" + currentfieldLabelToOutputFieldLabel.getKey() + ") as ?" 
									+ currentfieldLabelToOutputFieldLabel.getValue() + ")\n");
			
		}
		
		sparqlQuery.append("WHERE {\n");
		
		sparqlQuery.append(this.whereClause);
		
		sparqlQuery.append("}\n");
		
		return sparqlQuery.toString();
	}

	
	public ResultSet getVisualizationJavaValueObjects()
			throws MalformedQueryParametersException {
		if (StringUtils.isNotBlank(this.individualURLParam)) {
        	/*
        	 * To test for the validity of the URI submitted.
        	 * */
        	IRIFactory iRIFactory = IRIFactory.jenaImplementation();
    		IRI iri = iRIFactory.create(this.individualURLParam);
            if (iri.hasViolation(false)) {
                String errorMsg = ((Violation) iri.violations(false).next()).getShortMessage();
                log.error("Generic Query " + errorMsg);
                throw new MalformedQueryParametersException(
                		"URI provided for an individual is malformed.");
            }        	
        } else {
            throw new MalformedQueryParametersException("URI parameter is either null or empty.");
        }

		ResultSet resultSet	= executeQuery(generateGenericSparqlQuery(),
										   this.dataSource);

		return resultSet;
	}


}
