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

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: bdc34
 * Date: Jan 22, 2008
 * Time: 5:55:57 PM
 */
public class SparqlEvaluate {
    private static Log log = LogFactory.getLog( SparqlEvaluate.class );

    Model model;
    public SparqlEvaluate(Model model){
        if( model == null ) throw new Error("SparqlEvaluate must be passed a Model");
         this.model = model;
    }

    public void evaluateForAdditionalUris( EditConfiguration editConfig ){
        Map<String,String> varsToUris = sparqlEvaluateForUris(editConfig, editConfig.getSparqlForAdditionalUrisInScope());
        editConfig.getUrisInScope().putAll(varsToUris);
    }

    public void evalulateForAdditionalLiterals( EditConfiguration editConfig ){
        Map<String,Literal> varsToLiterals = sparqlEvaluateForLiterals(editConfig, editConfig.getSparqlForAdditionalLiteralsInScope());
        editConfig.getLiteralsInScope().putAll(varsToLiterals);
    }

    public void evaluateForExistingUris( EditConfiguration editConfig){
        Map<String,String> varsToUris = sparqlEvaluateForUris(editConfig, editConfig.getSparqlForExistingUris());
        editConfig.getUrisInScope().putAll(varsToUris);
    }

    public void evaluateForExistingLiterals( EditConfiguration editConfig){
        Map<String,Literal> varsToLiterals = sparqlEvaluateForLiterals(editConfig, editConfig.getSparqlForExistingLiterals());
        editConfig.getLiteralsInScope().putAll(varsToLiterals);
    }

//    public  Map<String,String> sparqlEvaluateForExistingToUris(Map<String,String> varToSpqrql){
//        Map<String,String> varToUris = new HashMap<String,String>();
//        for(String var : varToSpqrql.keySet()){
//            varToUris.put(var, queryToUri( varToSpqrql.get(var) ));
//        }
//        return varToUris;
//    }
//
//    public  Map<String,String> sparqlEvaluateForAdditionalLiterals(Map<String,String> varToSpqrql){
//        Map<String,String> varToLiterals = new HashMap<String,String>();
//        for(String var : varToSpqrql.keySet()){
//            varToLiterals.put(var, queryToLiteral( varToSpqrql.get(var) ));
//        }
//        return varToLiterals;
//    }

//    private  Map<String,String> sparqlEvaluateForExistingToUris( EditConfiguration editConfig ) {
//        Map<String,String> varToSpqrql = editConfig.getSparqlForExistingUris();
//        Map<String,String> uriScope = editConfig.getUrisInScope();
//        Map<String,Literal> literalScope = editConfig.getLiteralsInScope();
//
//        Map<String,String> varToUris = new HashMap<String,String>();
//
//        for(String var : varToSpqrql.keySet()){
//            String query = varToSpqrql.get(var);
//            List<String> queryStrings = new ArrayList <String>();
//            queryStrings.add(query);
//            queryStrings= editConfig.getN3Generator().subInUris(uriScope, queryStrings);
//            queryStrings = editConfig.getN3Generator().subInLiterals(literalScope,queryStrings);
//            varToUris.put(var, queryToUri(  queryStrings.get(0) ));           //might result in (key -> null)
//        }
//
//        return varToUris;
//    }

    protected  Map<String,Literal> sparqlEvaluateForLiterals( EditConfiguration editConfig, Map<String,String> varToSparql)  {
        Map<String,String> uriScope = editConfig.getUrisInScope();
        Map<String,Literal> literalScope = editConfig.getLiteralsInScope();

        Map<String,Literal> varToLiterals = new HashMap<String,Literal>();
        for(String var : varToSparql.keySet()){
            String query = varToSparql.get(var);
                        
            /* skip if var set to use a system generated value */
            if( query == null || EditConfiguration.USE_SYSTEM_VALUE.equals( query ))
                continue;
            
            List<String> queryStrings = new ArrayList <String>();
            queryStrings.add( query );
            queryStrings= editConfig.getN3Generator().subInUris(uriScope, queryStrings);
            queryStrings = editConfig.getN3Generator().subInLiterals(literalScope,queryStrings);
            varToLiterals.put(var, queryToLiteral(  queryStrings.get(0) ));   //might result in (key -> null)
        }

        return varToLiterals;
    }

    protected Map<String,String> sparqlEvaluateForUris( EditConfiguration editConfig, Map<String,String>varToSparql) {
        Map<String,String> uriScope = editConfig.getUrisInScope();
        Map<String,Literal> literalScope = editConfig.getLiteralsInScope();

        Map<String,String> varToUris = new HashMap<String,String>();

        for(String var : varToSparql.keySet()){
            String query = varToSparql.get(var);
            /* skip if var set to use a system generated value */
            if( query == null || EditConfiguration.USE_SYSTEM_VALUE.equals( query ))
                continue;
            List<String> queryStrings = new ArrayList <String>();
            queryStrings.add(query);
            queryStrings= editConfig.getN3Generator().subInUris(uriScope, queryStrings);
            queryStrings = editConfig.getN3Generator().subInLiterals(literalScope,queryStrings);
            String uriFromQuery = queryToUri(  queryStrings.get(0) );
            if( uriFromQuery != null )
            {    
            	//Added parens and output
            	varToUris.put(var, uriFromQuery);
            }
            else 
                log.debug("sparqlEvaluateForUris(): for var " + var 
                        + " the following query evaluated to null:\n"+queryStrings.get(0)+"\n(end of query)\n");                            
        }

        return varToUris;
    }

//    protected Map<String,Literal> sparqlEvaluateForAdditionalLiterals( EditConfiguration editConfig)  {
//        Map<String,String> varToSpqrql = editConfig.getSparqlForAdditionalLiteralsInScope();
//        Map<String,String> uriScope = editConfig.getUrisInScope();
//        Map<String,Literal> literalScope = editConfig.getLiteralsInScope();
//
//        Map<String,Literal> varToLiterals = new HashMap<String,Literal>();
//        for(String var : varToSpqrql.keySet()){
//            String query = varToSpqrql.get(var);
//            List<String> queryStrings = new ArrayList <String>();
//            queryStrings.add( query );
//            queryStrings= editConfig.getN3Generator().subInUris(uriScope, queryStrings);
//            queryStrings = editConfig.getN3Generator().subInLiterals(literalScope,queryStrings);
//            Literal literalFromQuery = queryToLiteral(  queryStrings.get(0) );
//            if( literalFromQuery != null )
//                varToLiterals.put(var, literalFromQuery );
//            else
//                log.debug("sparqlEvaluateForAdditionalLiterals(): for var " + var 
//                        + "query evaluated to null. query: '" + queryStrings.get(0) +"'");
//        }
//
//        return varToLiterals;
//    }
    
    protected  String queryToUri(String querystr){
        String value = null;
        QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, model);
            if( query.isSelectType() ){
                ResultSet results = null;
                results = qe.execSelect();
                if( results.hasNext()){
                    List vars = results.getResultVars();
                    if( vars == null )
                      throw new Error("sparql had no result variables");
                    if( vars.size() > 1 )
                        throw new Error("sparql queries for use on forms may only return one result");
                    String var =(String) vars.get(0);
                    QuerySolution qs = results.nextSolution();
                    Resource resource = qs.getResource(var);
                    value =  resource.getURI();
                }else{
                    return null;
                }
            } else {
                throw new Error("only SELECT type SPARQL queries are supported");
            }
        }catch(Exception ex){
            throw new Error("could not parse SPARQL in queryToUri: \n" + querystr + '\n' + ex.getMessage());
        }finally{
            if( qe != null)
                qe.close();
        }
        if( log.isDebugEnabled() ) log.debug("queryToUri() query: '"+ querystr +"'\nvalue: '" + value +"'");
        return value;
    }


    protected  Literal queryToLiteral(String querystr){
        Literal value = null;
        QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(querystr);
            qe = QueryExecutionFactory.create(query, model);
            if( query.isSelectType() ){
                ResultSet results = null;
                results = qe.execSelect();
                if( results.hasNext()){
                    List vars = results.getResultVars();
                    if( vars == null )
                        throw new Error("sparql had no result variables");
                    if( vars.size() > 1 )
                        throw new Error("sparql queries for use on forms may only return one result");
                    String var =(String) vars.get(0);
                    QuerySolution qs = results.nextSolution();
                    value = qs.getLiteral(var);
                }else{
                    return null;
                }
            } else {
                throw new Error("only SELECT type SPARQL queries are supported");
            }
        }catch(Exception ex){
            throw new Error("could not parse SPARQL in queryToLiteral: \n" + querystr + '\n' + ex.getMessage());
        }finally{
            if( qe != null)
                qe.close();
        }

        if( log.isDebugEnabled() ) log.debug("queryToLiteral() query: '"+ querystr +"'\nvalue: '" + value +"'");
        return value;
    }


}
