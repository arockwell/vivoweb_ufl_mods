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

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class PageDaoJena extends JenaBaseDao implements PageDao {

    final static Log log = LogFactory.getLog(PageDaoJena.class);
    
    static protected Query pageQuery;
    static protected Query pageTypeQuery;
    static protected Query pageMappingsQuery;
    static protected Query homePageUriQuery;
    static protected Query classGroupPageQuery;
    
    static final String prefixes = 
        "PREFIX rdf:   <" + VitroVocabulary.RDF +"> \n" +
        "PREFIX rdfs:  <" + VitroVocabulary.RDFS +"> \n" + 
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n" +
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS +"> \n";
    
    
    static final protected String pageQueryString = 
        prefixes + "\n" +
        "SELECT ?pageUri ?bodyTemplate ?urlMapping ?title WHERE {\n" +
        "    ?pageUri rdf:type <" + DisplayVocabulary.PAGE_TYPE + ">.\n"+               
        "    OPTIONAL { ?pageUri <" + DisplayVocabulary.REQUIRES_BODY_TEMPLATE + "> ?bodyTemplate }.\n"+
        "    OPTIONAL { ?pageUri <" + DisplayVocabulary.TITLE + "> ?title }.\n"+
        "    OPTIONAL { ?pageUri <" + DisplayVocabulary.URL_MAPPING + "> ?urlMapping . }\n"+                
        "} \n" ;
    
    static final protected String pageTypeQueryString = 
        prefixes + "\n" +
        "SELECT ?type WHERE {\n" +
        "    ?pageUri rdf:type ?type .\n"+                              
        "} \n" ;
    
    static final protected String pageMappingsQueryString = 
        prefixes + "\n" +
        "SELECT ?pageUri ?urlMapping WHERE {\n" +
        "    ?pageUri rdf:type <" + DisplayVocabulary.PAGE_TYPE + "> .\n"+                       
        "    ?pageUri <" + DisplayVocabulary.URL_MAPPING + "> ?urlMapping . \n"+                
        "} \n" ;
                          
    static final protected String homePageUriQueryString = 
        prefixes + "\n" +
        "SELECT ?pageUri WHERE {\n" +
        "    ?pageUri rdf:type <" + DisplayVocabulary.HOME_PAGE_TYPE + "> .\n"+                
        "} \n" ;

    static final protected String classGroupPageQueryString = 
        prefixes + "\n" +
        "SELECT ?classGroup WHERE { ?pageUri <" + DisplayVocabulary.FOR_CLASSGROUP + "> ?classGroup . }";
    
    static{
        try{    
            pageQuery=QueryFactory.create(pageQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for pageQuery " + th.getMessage());
            log.error(pageQueryString);
        }
        try{
            pageTypeQuery = QueryFactory.create(pageTypeQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for pageTypeQuery " + th.getMessage());
            log.error(pageTypeQueryString);
        }
        try{    
            pageMappingsQuery=QueryFactory.create(pageMappingsQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for pageMappingsQuery " + th.getMessage());
            log.error(pageMappingsQueryString);
        }   
        try{    
            homePageUriQuery=QueryFactory.create(homePageUriQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for homePageUriQuery " + th.getMessage());
            log.error(homePageUriQueryString);
        }
        try{    
            classGroupPageQuery=QueryFactory.create(classGroupPageQueryString);
        }catch(Throwable th){
            log.error("could not create SPARQL query for classGroupPageQuery " + th.getMessage());
            log.error(classGroupPageQueryString);
        }  
    }        
    
    public PageDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
   
    @Override
    public Map<String, String> getPageMappings() {
        Model displayModel = getOntModelSelector().getDisplayModel();
        QueryExecution qexec = QueryExecutionFactory.create( pageQuery, displayModel );
        
        Map<String,String> rv = new HashMap<String,String>();
        ResultSet resultSet = qexec.execSelect();
        while(resultSet.hasNext()){
            QuerySolution soln = resultSet.next();
            rv.put(nodeToString(soln.get("urlMapping")) , nodeToString( soln.get("pageUri") ));
        }
        return rv; 
    }
    
    /**
     * Gets information about a page identified by a URI.   
     */
    @Override
    public Map<String, Object> getPage(String pageUri) {     
      //setup query parameters
      QuerySolutionMap initialBindings = new QuerySolutionMap();
      initialBindings.add("pageUri", ResourceFactory.createResource(pageUri));
      
      List<Map<String, Object>> list; 
      Model displayModel = getOntModelSelector().getDisplayModel();
      displayModel.enterCriticalSection(false);
      try{
          QueryExecution qexec = QueryExecutionFactory.create(pageQuery,displayModel,initialBindings );
          list = executeQueryToCollection( qexec );
          qexec.close();
      }finally{
          displayModel.leaveCriticalSection();
      }
      
      if( list == null ){
          log.error("executeQueryToCollection returned null.");
          return Collections.emptyMap();
      }
      if( list.size() == 0 ){
          log.debug("no page found for " + pageUri);
          return Collections.emptyMap();          
      }
      
      if( list.size() > 1 )
          log.debug("multiple results found for " + pageUri + " using only the first.");
      Map<String,Object> pageData = list.get(0);
      
      //now get the rdf:types for the page
      List<String> types = new ArrayList<String>();
      displayModel.enterCriticalSection(false);
      try{
          QueryExecution qexec = QueryExecutionFactory.create(pageTypeQuery, displayModel, initialBindings);
          ResultSet rs = qexec.execSelect();
          while(rs.hasNext()){
              QuerySolution soln = rs.next();
              types.add( nodeToString( soln.get("type" ) ));
          }
          qexec.close();
      }finally{
          displayModel.leaveCriticalSection();
      }
      
      if( list == null )
          log.error("could not get types for page " + pageUri);
      else
          pageData.put("types", types);
      
      return pageData;
    }
    
    @Override
    public String getHomePageUri(){
        Model displayModel = getOntModelSelector().getDisplayModel();
        QueryExecution qexec = QueryExecutionFactory.create( homePageUriQuery, displayModel );
        
        List<String> rv = new ArrayList<String>();
        ResultSet resultSet = qexec.execSelect();        
        while(resultSet.hasNext()){
            QuerySolution soln = resultSet.next();
            rv.add( nodeToString(soln.get("pageUri")) );        
        }
        if( rv.size() == 0 ){
            log.error("No display:HomePage defined in display model.");
            return null;
        }
        if( rv.size() > 1 ){
            log.error("More than one display:HomePage defined in display model.");
            for( String hp : rv ){
                log.error("home page: " + hp);
            }
        }
        return rv.get(0);
    }
    
    /**
     * Gets a URI for display:forClassGroup for the specified page.
     * Only one value is expected in the model.
     * This may return null if there is no ClassGroup associated with the page. 
     * @param pageUri
     * @return
     */
    @Override
    public String getClassGroupPage(String pageUri) {        
        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("pageUri", ResourceFactory.createResource(pageUri));
        
        Model displayModel = getOntModelSelector().getDisplayModel();
        QueryExecution qexec = QueryExecutionFactory.create( classGroupPageQuery, displayModel , initialBindings);
        
        List<String> classGroupsForPage = new ArrayList<String>();
        ResultSet resultSet = qexec.execSelect();        
        while(resultSet.hasNext()){
            QuerySolution soln = resultSet.next();
            classGroupsForPage.add( nodeToString(soln.get("classGroup")) );        
        }
        if( classGroupsForPage.size() == 0 ){
            log.debug("No classgroup info defined in display model for "+ pageUri);
            return null;
        }
        if( classGroupsForPage.size() > 1 ){
            log.error("More than one display:forClassGroup defined in display model for page " + pageUri);            
        }        
        return classGroupsForPage.get(0);
    }
    
    
    /* ****************************************************************************** */
    
    /**
     * Converts a sparql query that returns a multiple rows to a list of maps.
     * The maps will have column names as keys to the values.
     */
    protected List<Map<String, Object>> executeQueryToCollection(
            QueryExecution qexec) {
        List<Map<String, Object>> rv = new ArrayList<Map<String, Object>>();
        ResultSet results = qexec.execSelect();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            rv.add(querySolutionToMap(soln));
        }
        return rv;
    }
    
    protected Map<String,Object> querySolutionToMap( QuerySolution soln ){
        Map<String,Object> map = new HashMap<String,Object>();
        Iterator<String> varNames = soln.varNames();
        while(varNames.hasNext()){
            String varName = varNames.next();
            map.put(varName, nodeToObject( soln.get(varName)));
        }
        return map;
    }
    
    static protected Object nodeToObject( RDFNode node ){
        if( node == null ){
            return "";
        }else if( node.isLiteral() ){
            Literal literal = node.asLiteral();
            return literal.getValue();
        }else if( node.isURIResource() ){
            Resource resource = node.asResource();
            return resource.getURI();
        }else if( node.isAnon() ){  
            Resource resource = node.asResource();
            return resource.getId().getLabelString(); //get b-node id
        }else{
            return "";
        }
    }

    static protected String nodeToString( RDFNode node ){
        if( node == null ){
            return "";
        }else if( node.isLiteral() ){
            Literal literal = node.asLiteral();
            return literal.getLexicalForm();
        }else if( node.isURIResource() ){
            Resource resource = node.asResource();
            return resource.getURI();
        }else if( node.isAnon() ){  
            Resource resource = node.asResource();
            return resource.getId().getLabelString(); //get b-node id
        }else{
            return "";
        }
    }
    protected Map<String,Object> resultsToMap(){
        return null;
    }


    

}
