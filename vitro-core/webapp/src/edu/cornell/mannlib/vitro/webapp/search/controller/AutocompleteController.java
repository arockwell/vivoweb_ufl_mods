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

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc.VitroLuceneTermNames;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import freemarker.template.Configuration;

/**
 * AutocompleteController is used to generate autocomplete and select element content
 * through a Lucene search. 
 */

public class AutocompleteController extends FreemarkerHttpServlet{

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AutocompleteController.class);
    
    private static final String TEMPLATE_DEFAULT = "autocompleteResults.ftl";
    
    private static String QUERY_PARAMETER_NAME = "term";
    
    String NORESULT_MSG = "";    
    private int defaultMaxSearchSize= 1000;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        
        Map<String, Object> map = new HashMap<String, Object>();

        VitroRequest vreq = new VitroRequest(request);
        Configuration config = getConfig(vreq);
        PortalFlag portalFlag = vreq.getPortalFlag();
        
        try {
 
            // make sure an IndividualDao is available
            if( vreq.getWebappDaoFactory() == null 
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("makeUsableBeans() could not get IndividualDao ");
                doSearchError(map, config, request, response);
                return;
            }                    
            
            int maxHitSize = defaultMaxSearchSize;
            
            String qtxt = vreq.getParameter(QUERY_PARAMETER_NAME);
            Analyzer analyzer = getAnalyzer(getServletContext());
            
            Query query = getQuery(vreq, portalFlag, analyzer, qtxt);             
            if (query == null ) {
                log.debug("query for '" + qtxt +"' is null.");
                doNoQuery(map, config, request, response);
                return;
            }
            log.debug("query for '" + qtxt +"' is " + query.toString());
                        
            IndexSearcher searcherForRequest = LuceneIndexFactory.getIndexSearcher(getServletContext());
            
            TopDocs topDocs = null;
            try{
                topDocs = searcherForRequest.search(query,null,maxHitSize);
            }catch(Throwable t){
                log.error("in first pass at search: " + t);
                // this is a hack to deal with odd cases where search and index threads interact
                try{
                    wait(150);
                    topDocs = searcherForRequest.search(query,null,maxHitSize);
                }catch (Exception ex){
                    log.error(ex);
                    doFailedSearch(map, config, request, response);
                    return;
                }
            }

            if( topDocs == null || topDocs.scoreDocs == null){
                log.error("topDocs for a search was null");                
                doFailedSearch(map, config, request, response);
                return;
            }
            
            int hitsLength = topDocs.scoreDocs.length;
            if ( hitsLength < 1 ){                
                doFailedSearch(map, config, request, response);
                return;
            }            
            log.debug("found "+hitsLength+" hits"); 

            List<SearchResult> results = new ArrayList<SearchResult>();
            for(int i=0; i<topDocs.scoreDocs.length ;i++){
                try{                     
                    Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                    String uri = doc.get(VitroLuceneTermNames.URI);
                    String name = doc.get(VitroLuceneTermNames.NAMERAW);
                    SearchResult result = new SearchResult(name, uri);
                    results.add(result);
                } catch(Exception e){
                    log.error("problem getting usable Individuals from search " +
                            "hits" + e.getMessage());
                }
            }   

            Collections.sort(results);
            map.put("results", results);
            writeTemplate(TEMPLATE_DEFAULT, map, config, request, response);
   
        } catch (Throwable e) {
            log.error("AutocompleteController(): " + e);            
            doSearchError(map, config, request, response);
            return;
        }
    }

//    private String getIndexDir(ServletContext servletContext) throws SearchException {
//        Object obj = servletContext.getAttribute(LuceneSetup.INDEX_DIR);
//        if( obj == null || !(obj instanceof String) )
//            throw new SearchException("Could not get IndexDir for lucene index");
//        else
//            return (String)obj;
//    }

    private Analyzer getAnalyzer(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.ANALYZER);
        if( obj == null || !(obj instanceof Analyzer) )
            throw new SearchException("Could not get anlyzer");
        else
            return (Analyzer)obj;        
    }

    private Query getQuery(VitroRequest request, PortalFlag portalState,
                       Analyzer analyzer, String querystr) throws SearchException{
        
        Query query = null;
        try {
            if( querystr == null){
                log.error("There was no Parameter '"+ QUERY_PARAMETER_NAME            
                    +"' in the request.");                
                return null;
            }else if( querystr.length() > MAX_QUERY_LENGTH ){
                log.debug("The search was too long. The maximum " +
                        "query length is " + MAX_QUERY_LENGTH );
                return null;
            } 

            query = makeNameQuery(querystr, analyzer, request);
            
            // Filter by type
            {
                BooleanQuery boolQuery = new BooleanQuery(); 
                String typeParam = (String) request.getParameter("type");
                boolQuery.add(  new TermQuery(
                        new Term(VitroLuceneTermNames.RDFTYPE, 
                                typeParam)),
                    BooleanClause.Occur.MUST);
                boolQuery.add(query, BooleanClause.Occur.MUST);
                query = boolQuery;
            }

            //if we have a flag/portal query then we add
            //it by making a BooelanQuery.
            // RY 7/24/10 Temporarily commenting out for now because we're suddenly getting portal:2
            // thrown onto the query. Will need to investigate post-launch of NIHVIVO 1.1.
//            Query flagQuery = makeFlagQuery( portalState );
//            if( flagQuery != null ){
//                BooleanQuery boolQuery = new BooleanQuery();
//                boolQuery.add( query, BooleanClause.Occur.MUST);
//                boolQuery.add( flagQuery, BooleanClause.Occur.MUST);
//                query = boolQuery;
//            }
            
        } catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return query;
    }
    
    private Query makeNameQuery(String querystr, Analyzer analyzer, HttpServletRequest request) {

        String tokenizeParam = (String) request.getParameter("tokenize"); 
        boolean tokenize = "true".equals(tokenizeParam);
        
        // Note: Stemming is only relevant if we are tokenizing: an untokenized name
        // query will not be stemmed. So we don't look at the stem parameter until we get to
        // makeTokenizedNameQuery().
        if (tokenize) {
            return makeTokenizedNameQuery(querystr, analyzer, request);
        } else {
            return makeUntokenizedNameQuery(querystr);
        }
    }
    
    private Query makeTokenizedNameQuery(String querystr, Analyzer analyzer, HttpServletRequest request) {
 
        String stemParam = (String) request.getParameter("stem"); 
        boolean stem = "true".equals(stemParam);
        String termName = stem ? VitroLuceneTermNames.NAME : VitroLuceneTermNames.NAMEUNSTEMMED;

        BooleanQuery boolQuery = new BooleanQuery();
        
        // Use the query parser to analyze the search term the same way the indexed text was analyzed.
        // For example, text is lowercased, and function words are stripped out.
        QueryParser parser = getQueryParser(termName, analyzer);
        
        // The wildcard query doesn't play well with stemming. Query term name:tales* doesn't match
        // "tales", which is indexed as "tale", while query term name:tales does. Obviously we need 
        // the wildcard for name:tal*, so the only way to get them all to match is use a disjunction 
        // of wildcard and non-wildcard queries. The query will look have only an implicit disjunction
        // operator: e.g., +(name:tales name:tales*)
        try {
            log.debug("Adding non-wildcard query for " + querystr);
            Query query = parser.parse(querystr);  
            boolQuery.add(query, BooleanClause.Occur.SHOULD);

            // Prevent ParseException here when adding * after a space.
            // If there's a space at the end, we don't need the wildcard query.
            if (! querystr.endsWith(" ")) {
                log.debug("Adding wildcard query for " + querystr);
                Query wildcardQuery = parser.parse(querystr + "*");            
                boolQuery.add(wildcardQuery, BooleanClause.Occur.SHOULD);
            }
            
            log.debug("Name query is: " + boolQuery.toString());
        } catch (ParseException e) {
            log.warn(e, e);
        }
        
        
        return boolQuery;
        
/*       
        Query query = null;
        
        // The search index is lowercased
        querystr = querystr.toLowerCase();
        
        List<String> terms = Arrays.asList(querystr.split("[, ]+"));
        for (Iterator<String> i = terms.iterator(); i.hasNext(); ) {
            String term = (String) i.next();
            BooleanQuery boolQuery = new BooleanQuery(); 
            // All items but last get a regular term query
            if (i.hasNext()) {                
                boolQuery.add( 
                        new TermQuery(new Term(termName, term)),
                        BooleanClause.Occur.MUST);  
                if (query != null) {
                    boolQuery.add(query, BooleanClause.Occur.MUST);
                }
                query = boolQuery;                          
            }
            // Last term
            else {
                // If the last token of the query string ends in a word-delimiting character
                // it should not get a wildcard query term.
                // E.g., "Dickens," should match "Dickens" but not "Dickenson"
                Pattern p = Pattern.compile("\\W$");
                Matcher m = p.matcher(querystr);
                boolean lastTermIsWildcard = !m.find();
                
                if (lastTermIsWildcard) {
                    log.debug("Adding wildcard query on last term");
                    boolQuery.add( 
                            new WildcardQuery(new Term(termName, term + "*")),
                            BooleanClause.Occur.MUST);                 
                } else {
                    log.debug("Adding term query on last term");
                    boolQuery.add( 
                            new TermQuery(new Term(termName, term)),
                            BooleanClause.Occur.MUST);                    
                }
                if (query != null) {
                    boolQuery.add(query, BooleanClause.Occur.MUST); 
                }
                query = boolQuery;
            }
        }
        return query;
*/        
    }

    private Query makeUntokenizedNameQuery(String querystr) {
        
        querystr = querystr.toLowerCase();
        String termName = VitroLuceneTermNames.NAMELOWERCASE;
        BooleanQuery query = new BooleanQuery();
        log.debug("Adding wildcard query on unanalyzed name");
        query.add( 
                new WildcardQuery(new Term(termName, querystr + "*")),
                BooleanClause.Occur.MUST);   
        
        return query;
    }
            
    private QueryParser getQueryParser(String searchField, Analyzer analyzer){
        // searchField indicates which field to search against when there is no term
        // indicated in the query string.
        // The analyzer is needed so that we use the same analyzer on the search queries as
        // was used on the text that was indexed.
        QueryParser qp = new QueryParser(searchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        return qp;
    }

    private void doNoQuery(Map<String, Object> map, Configuration config, HttpServletRequest request, HttpServletResponse response) {
        writeTemplate(TEMPLATE_DEFAULT, map, config, request, response);
    }

    private void doFailedSearch(Map<String, Object> map, Configuration config, HttpServletRequest request, HttpServletResponse response) {
        writeTemplate(TEMPLATE_DEFAULT, map, config, request, response);
    }
 
    private void doSearchError(Map<String, Object> map, Configuration config, HttpServletRequest request, HttpServletResponse response) {
        // For now, we are not sending an error message back to the client because with the default autocomplete configuration it
        // chokes.
        writeTemplate(TEMPLATE_DEFAULT, map, config, request, response);
    }

    public static final int MAX_QUERY_LENGTH = 500;

    public class SearchResult implements Comparable<Object> {
        private String label;
        private String uri;
        
        SearchResult(String label, String value) {
            this.label = label;
            this.uri = value;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getUri() {
            return uri;
        }
        
        public String getJson() {
            return "{ \"label\": \"" + label + "\", " + "\"uri\": \"" + uri + "\" }";
        }

        public int compareTo(Object o) throws ClassCastException {
            if ( !(o instanceof SearchResult) ) {
                throw new ClassCastException("Error in SearchResult.compareTo(): expected SearchResult object.");
            }
            SearchResult sr = (SearchResult) o;
            return label.compareTo(sr.getLabel());
        }
    }
    


}
