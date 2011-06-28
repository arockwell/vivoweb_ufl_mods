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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
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

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.flags.PortalFlag;
import edu.cornell.mannlib.vitro.webapp.search.SearchException;
import edu.cornell.mannlib.vitro.webapp.search.beans.Searcher;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroHighlighter;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneSetup;
import edu.cornell.mannlib.vitro.webapp.search.lucene.SimpleLuceneHighlighter;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import edu.cornell.mannlib.vitro.webapp.utils.Html2Text;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ListedIndividualTemplateModel;
import freemarker.template.Configuration;

/**
 * PagedSearchController is the new search controller that interacts 
 * directly with the lucene API and returns paged, relevance ranked results.
 *  
 * @author bdc34
 * 
 * Rewritten to use Freemarker: rjy7
 *
 */
public class FreemarkerPagedSearchController extends FreemarkerHttpServlet implements Searcher {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreemarkerPagedSearchController.class.getName());
    private static final String XML_REQUEST_PARAM = "xml";
    
    private IndexSearcher searcher = null;
    private int defaultHitsPerPage = 25;
    private int defaultMaxSearchSize= 1000;   
    
    protected static final Map<Format,Map<Result,String>> templateTable;
    
    protected enum Format{ 
        HTML, XML; 
    }
    
    protected enum Result{
        PAGED, FORM, ERROR, BAD_QUERY         
    }
    
    static{
        templateTable = setupTemplateTable();
    }
    
//    protected enum SearchTemplate {
//        PAGED_RESULTS("search-pagedResults.ftl"),
//        FORM("search-form.ftl"),
//        ERROR("search-error.ftl"),
//        BAD_QUERY("search-badQuery.ftl"),
//        XML_RESULT("search-xmlResults.ftl");
//        
//        private final String filename;
//        
//        SearchTemplate(String filename) {
//            this.filename = filename;
//        }
//
//        public String toString() {
//            return filename;
//        }
//    }
         
    /**
     * Overriding doGet from FreemarkerHttpController to do a page template (as
     * opposed to body template) style output for XML requests.
     * 
     * This follows the pattern in AutocompleteController.java.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean wasXmlRequested = isRequesedFormatXml(request);
        if( ! wasXmlRequested ){
            super.doGet(request,response);
        }else{
            VitroRequest vreq = new VitroRequest(request);
            Configuration config = getConfig(vreq);            
            ResponseValues rvalues = processRequest(vreq);
            
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/xml;charset=UTF-8");
            writeTemplate(rvalues.getTemplateName(), rvalues.getMap(), config, request, response);
        }
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        //There may be other non-html formats in the future
        Format format = getFormat(vreq);            
        boolean wasXmlRequested = Format.XML == format;
        log.debug("xml was the requested format");                         
        boolean wasHtmlRequested = ! wasXmlRequested; 
        
        try {
            Portal portal = vreq.getPortal();
            PortalFlag portalFlag = vreq.getPortalFlag();
            
            //make sure an IndividualDao is available 
            if( vreq.getWebappDaoFactory() == null 
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("Could not get webappDaoFactory or IndividualDao");
                throw new Exception("Could not access model.");
            }
            IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
            VClassGroupDao grpDao = vreq.getWebappDaoFactory().getVClassGroupDao();
            VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();
            String alphaFilter = vreq.getParameter("alpha");
            
            int startIndex = 0;
            try{ 
                startIndex = Integer.parseInt(vreq.getParameter("startIndex")); 
            }catch (Throwable e) { 
                startIndex = 0; 
            }            
            log.debug("startIndex is " + startIndex);                       
            
            int hitsPerPage = defaultHitsPerPage;
            try{ 
                hitsPerPage = Integer.parseInt(vreq.getParameter("hitsPerPage")); 
            } catch (Throwable e) { 
                hitsPerPage = defaultHitsPerPage; 
            }                        
            log.debug("hitsPerPage is " + hitsPerPage);
            
            int maxHitSize = defaultMaxSearchSize;
            if( startIndex >= defaultMaxSearchSize - hitsPerPage )
                maxHitSize = startIndex + defaultMaxSearchSize;
            if( alphaFilter != null ){
                maxHitSize = maxHitSize * 2;
                hitsPerPage = maxHitSize;
            }
            log.debug("maxHitSize is " + maxHitSize);

            String qtxt = vreq.getParameter(VitroQuery.QUERY_PARAMETER_NAME);
            Analyzer analyzer = getAnalyzer(getServletContext());
            
            Query query = null;
            try {
                query = getQuery(vreq, portalFlag, analyzer, qtxt);
                log.debug("query for '" + qtxt +"' is " + query.toString());
            } catch (ParseException e) {
                return doBadQuery(portal, qtxt,format);
            } 

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
                    String msg = makeBadSearchMessage(qtxt,ex.getMessage());
                    if (msg == null) {
                        msg = "The search request contained errors.";
                    }
                    return doFailedSearch(msg, qtxt,format);
                }
            }

            if( topDocs == null || topDocs.scoreDocs == null){
                log.error("topDocs for a search was null");                
                String msg = "The search request contained errors.";
                return doFailedSearch(msg, qtxt,format);
            }
            
            int hitsLength = topDocs.scoreDocs.length;
            if ( hitsLength < 1 ){                
                return doNoHits(qtxt,format);
            }            
            log.debug("found "+hitsLength+" hits");

            int lastHitToShow = 0;
            if((startIndex + hitsPerPage) > hitsLength ) {
                lastHitToShow = hitsLength;
            } else {
                lastHitToShow = startIndex + hitsPerPage - 1;
            }
            
            List<Individual> beans = new LinkedList<Individual>();                        
            for(int i=startIndex; i<topDocs.scoreDocs.length ;i++){
                try{
                    if( (i >= startIndex) && (i <= lastHitToShow) ){                        
                        Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                        String uri = doc.get(Entity2LuceneDoc.term.URI);
                        Individual ent = new IndividualImpl();
                        ent.setURI(uri);
                        ent = iDao.getIndividualByURI(uri);
                        if(ent!=null)
                            beans.add(ent);
                    }
                }catch(Exception e){
                    log.error("problem getting usable Individuals from search " +
                            "hits" + e.getMessage());
                }
            }            
  
            ParamMap pagingLinkParams = new ParamMap();
            pagingLinkParams.put("querytext", qtxt);
            pagingLinkParams.put("hitsPerPage", String.valueOf(hitsPerPage));
            
            if( wasXmlRequested ){
                pagingLinkParams.put(XML_REQUEST_PARAM,"1");                
            }
            
            /* Start putting together the data for the templates */
            
            Map<String, Object> body = new HashMap<String, Object>();
            
            String classGroupParam = vreq.getParameter("classgroup");    
            boolean classGroupFilterRequested = false;
            if (!StringUtils.isEmpty(classGroupParam)) {
                VClassGroup grp = grpDao.getGroupByURI(classGroupParam);
                classGroupFilterRequested = true;
                if (grp != null && grp.getPublicName() != null)
                    body.put("classGroupName", grp.getPublicName());
            }
            
            String typeParam = vreq.getParameter("type");
            boolean typeFiltereRequested = false;
            if (!StringUtils.isEmpty(typeParam)) {
                VClass type = vclassDao.getVClassByURI(typeParam);
                typeFiltereRequested = true;
                if (type != null && type.getName() != null)
                    body.put("typeName", type.getName());
            }
            
            /* Add classgroup and type refinement links to body */
            if( wasHtmlRequested ){                                
                // Search request includes no classgroup and no type, so add classgroup search refinement links.
                if ( !classGroupFilterRequested && !typeFiltereRequested ) { 
                    List<VClassGroup> classgroups = getClassGroups(grpDao, topDocs, searcherForRequest);
                    List<VClassGroupSearchLink> classGroupLinks = new ArrayList<VClassGroupSearchLink>(classgroups.size());
                    for (VClassGroup vcg : classgroups) {
                        if (vcg.getPublicName() != null) {
                            classGroupLinks.add(new VClassGroupSearchLink(qtxt, vcg));
                        }
                    }
                    body.put("classGroupLinks", classGroupLinks);                       
     
                // Search request is for a classgroup, so add rdf:type search refinement links
                // but try to filter out classes that are subclasses
                } else if ( classGroupFilterRequested && !typeFiltereRequested ) {  
                    List<VClass> vClasses = getVClasses(vclassDao,topDocs,searcherForRequest);
                    List<VClassSearchLink> vClassLinks = new ArrayList<VClassSearchLink>(vClasses.size());
                    for (VClass vc : vClasses) {
                        vClassLinks.add(new VClassSearchLink(qtxt, vc));
                    }
                    body.put("classLinks", vClassLinks);                       
                    pagingLinkParams.put("classgroup", classGroupParam);
    
                // This case is never displayed
                } else if (!StringUtils.isEmpty(alphaFilter)) {
                    body.put("alphas", getAlphas(topDocs, searcherForRequest));
                    alphaSortIndividuals(beans);
                } else {
                    pagingLinkParams.put("type", typeParam);
                }
            }           

            // Convert search result individuals to template model objects
            body.put("individuals", ListedIndividualTemplateModel
                    .getIndividualTemplateModelList(beans, vreq));

            body.put("querytext", qtxt);
            body.put("title", qtxt + " - " + portal.getAppName()
                    + " Search Results");
            
            body.put("hitsLength",hitsLength);
            body.put("startIndex", startIndex);
            
            body.put("pagingLinks", getPagingLinks(startIndex, hitsPerPage,
                    hitsLength, maxHitSize, vreq.getServletPath(),
                    pagingLinkParams));

            if (startIndex != 0) {
                body.put("prevPage", getPreviousPageLink(startIndex,
                        hitsPerPage, vreq.getServletPath(), pagingLinkParams));
            }
            if (startIndex < (hitsLength - hitsPerPage)) {
                body.put("nextPage", getNextPageLink(startIndex, hitsPerPage,
                        vreq.getServletPath(), pagingLinkParams));
            }

            String template = templateTable.get(format).get(Result.PAGED);
            
            return new TemplateResponseValues(template, body);
        } catch (Throwable e) {
            return doSearchError(e,format);
        }        
    }
    
    private void alphaSortIndividuals(List<Individual> beans) {
        Collections.sort(beans, new Comparator< Individual >(){
            public int compare(Individual o1, Individual o2) {
                if( o1 == null || o1.getName() == null )
                    return 1;
                else
                    return o1.getName().compareTo(o2.getName());
            }});        
    }

    private List<String> getAlphas(TopDocs topDocs, IndexSearcher searcher) {
        Set<String> alphas = new HashSet<String>();
        for(int i=0;i<topDocs.scoreDocs.length; i++){
            Document doc;
            try {
                doc = searcher.doc(topDocs.scoreDocs[i].doc);
                String name =doc.get(Entity2LuceneDoc.term.NAME);
                if( name != null && name.length() > 0)
                    alphas.add( name.substring(0, 1));                
            } catch (CorruptIndexException e) {
                log.debug("Could not get alphas for document",e);
            } catch (IOException e) {
                log.debug("Could not get alphas for document",e);
            }
        
        }
        return new ArrayList<String>(alphas);
    }

    /**
     * Get the class groups represented for the individuals in the topDocs.
     */
    private List<VClassGroup> getClassGroups(VClassGroupDao grpDao, TopDocs topDocs,
            IndexSearcher searcherForRequest) {        
        LinkedHashMap<String,VClassGroup> grpMap = grpDao.getClassGroupMap();
        int n = grpMap.size();
        
        HashSet<String> classGroupsInHits = new HashSet<String>(n);
        int grpsFound = 0;
        
        for(int i=0; i<topDocs.scoreDocs.length && n > grpsFound ;i++){
            try{
                Document doc = searcherForRequest.doc(topDocs.scoreDocs[i].doc);                    
                Field[] grps = doc.getFields(Entity2LuceneDoc.term.CLASSGROUP_URI);                
                if(grps != null || grps.length > 0){
                    for(int j=0;j<grps.length;j++){
                        String groupUri = grps[j].stringValue();
                        if( groupUri != null && ! classGroupsInHits.contains(groupUri)){
                            classGroupsInHits.add(groupUri);
                            grpsFound++;
                            if( grpsFound >= n )
                                break;
                        }                        
                    }                    
                }
            }catch(Exception e){
                log.error("problem getting VClassGroups from search hits " 
                        + e.getMessage());
            }
        }            
        
        List<String> classgroupURIs= Collections.list(Collections.enumeration(classGroupsInHits));        
        List<VClassGroup> classgroups = new ArrayList<VClassGroup>( classgroupURIs.size() );
        for(String cgUri: classgroupURIs){
            if( cgUri != null && ! "".equals(cgUri) ){
                VClassGroup vcg = grpDao.getGroupByURI( cgUri );
                if( vcg == null ){
                    log.debug("could not get classgroup for URI " + cgUri);
                }else{
                    classgroups.add(vcg);
                }
            }          
        }        
        grpDao.sortGroupList(classgroups);     
                
        return classgroups;
    }

    private class VClassGroupSearchLink extends LinkTemplateModel {
 
        VClassGroupSearchLink(String querytext, VClassGroup classgroup) {
            super(classgroup.getPublicName(), "/search", "querytext", querytext, "classgroup", classgroup.getURI());
        }
    }
    
    private class VClassSearchLink extends LinkTemplateModel {
        
        VClassSearchLink(String querytext, VClass type) {
            super(type.getName(), "/search", "querytext", querytext, "type", type.getURI());
        }
    }
    
    private List<PagingLink> getPagingLinks(int startIndex, int hitsPerPage, int hitsLength, int maxHitSize, String baseUrl, ParamMap params) {

        List<PagingLink> pagingLinks = new ArrayList<PagingLink>();
        
        // No paging links if only one page of results
        if (hitsLength <= hitsPerPage) {
            return pagingLinks;
        }
        
        for (int i = 0; i < hitsLength; i += hitsPerPage) {
            params.put("startIndex", String.valueOf(i));
            if ( i < maxHitSize - hitsPerPage) {
                int pageNumber = i/hitsPerPage + 1;
                if (i >= startIndex && i < (startIndex + hitsPerPage)) {
                    pagingLinks.add(new PagingLink(pageNumber));
                } else {
                    pagingLinks.add(new PagingLink(pageNumber, baseUrl, params));
                }
            } else {
                pagingLinks.add(new PagingLink("more...", baseUrl, params));
            }
        }   
        
        return pagingLinks;
    }
    
    private String getPreviousPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put("startIndex", String.valueOf(startIndex-hitsPerPage));
        //return new PagingLink("Previous", baseUrl, params);
        return UrlBuilder.getUrl(baseUrl, params);
    }
    
    private String getNextPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put("startIndex", String.valueOf(startIndex+hitsPerPage));
        //return new PagingLink("Next", baseUrl, params);
        return UrlBuilder.getUrl(baseUrl, params);
    }
    
    private class PagingLink extends LinkTemplateModel {
        
        PagingLink(int pageNumber, String baseUrl, ParamMap params) {
            super(String.valueOf(pageNumber), baseUrl, params);
        }
        
        // Constructor for current page item: not a link, so no url value.
        PagingLink(int pageNumber) {
            setText(String.valueOf(pageNumber));
        }
        
        // Constructor for "more..." item
        PagingLink(String text, String baseUrl, ParamMap params) {
            super(text, baseUrl, params);
        }
    }
   
    private List<VClass> getVClasses(VClassDao vclassDao, TopDocs topDocs,
            IndexSearcher searherForRequest){        
        HashSet<String> typesInHits = getVClassUrisForHits(topDocs,searherForRequest);                                
        List<VClass> classes = new ArrayList<VClass>(typesInHits.size());
        
        Iterator<String> it = typesInHits.iterator();
        while(it.hasNext()){
            String typeUri = it.next();
            try{
                if( VitroVocabulary.OWL_THING.equals(typeUri))
                    continue;
                VClass type = vclassDao.getVClassByURI(typeUri);
                if( ! type.isAnonymous() &&
                      type.getName() != null && !"".equals(type.getName()) &&
                      type.getGroupURI() != null ) //don't display classes that aren't in classgroups                      
                    classes.add(type);
            }catch(Exception ex){
                if( log.isDebugEnabled() )
                    log.debug("could not add type " + typeUri, ex);
            }                        
        }
        Collections.sort(classes, new Comparator<VClass>(){
            public int compare(VClass o1, VClass o2) {                
                return o1.compareTo(o2);
            }});
        return classes;
    }       
        
    private HashSet<String> getVClassUrisForHits(TopDocs topDocs, 
            IndexSearcher searcherForRequest){
        HashSet<String> typesInHits = new HashSet<String>();        
        for(int i=0; i<topDocs.scoreDocs.length; i++){
            try{
                Document doc=searcherForRequest.doc(topDocs.scoreDocs[i].doc);
                Field[] types = doc.getFields(Entity2LuceneDoc.term.RDFTYPE);
                if(types != null ){
                    for(int j=0;j<types.length;j++){
                        String typeUri = types[j].stringValue();
                        typesInHits.add(typeUri);
                    }
                }
            }catch(Exception e){
                log.error("problems getting rdf:type for search hits",e);
            }
        }
        return typesInHits;
    }       

    private Analyzer getAnalyzer(ServletContext servletContext) throws SearchException {
        Object obj = servletContext.getAttribute(LuceneSetup.ANALYZER);
        if( obj == null || !(obj instanceof Analyzer) )
            throw new SearchException("Could not get analyzer");
        else
            return (Analyzer)obj;        
    }

    private Query getQuery(VitroRequest request, PortalFlag portalState,
                       Analyzer analyzer, String querystr ) throws SearchException, ParseException {
        Query query = null;
        try{
            //String querystr = request.getParameter(VitroQuery.QUERY_PARAMETER_NAME);
            if( querystr == null){
                log.error("There was no Parameter '"+VitroQuery.QUERY_PARAMETER_NAME            
                    +"' in the request.");                
                return null;
            }else if( querystr.length() > MAX_QUERY_LENGTH ){
                log.debug("The search was too long. The maximum " +
                        "query length is " + MAX_QUERY_LENGTH );
                return null;
            }               
            QueryParser parser = getQueryParser(analyzer);
            query = parser.parse(querystr);

            String alpha = request.getParameter("alpha");
            if( alpha != null && !"".equals(alpha) && alpha.length() == 1){
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST );
                boolQuery.add( 
                    new WildcardQuery(new Term(Entity2LuceneDoc.term.NAME, alpha+'*')),
                    BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
            //check if this is classgroup filtered
            Object param = request.getParameter("classgroup");
            if( param != null && !"".equals(param)){                         
                  BooleanQuery boolQuery = new BooleanQuery();
                  boolQuery.add( query, BooleanClause.Occur.MUST);
                  boolQuery.add(  new TermQuery(
                                      new Term(Entity2LuceneDoc.term.CLASSGROUP_URI, 
                                              (String)param)),
                                  BooleanClause.Occur.MUST);
                  query = boolQuery;
            }

            //check if this is rdf:type filtered
            param = request.getParameter("type");
            if(  param != null && !"".equals(param)){                
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST);
                boolQuery.add(  new TermQuery(
                                    new Term(Entity2LuceneDoc.term.RDFTYPE, 
                                            (String)param)),
                                BooleanClause.Occur.MUST);
                query = boolQuery;
            }

            //if we have a flag/portal query then we add
            //it by making a BooelanQuery.
            Query flagQuery = makeFlagQuery( portalState );
            if( flagQuery != null ){
                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add( query, BooleanClause.Occur.MUST);
                boolQuery.add( flagQuery, BooleanClause.Occur.MUST);
                query = boolQuery;
            }
            
            log.debug("Query: " + query);
            
        } catch (ParseException e) {
            throw new ParseException(e.getMessage());
        } catch (Exception ex){
            throw new SearchException(ex.getMessage());
        }

        return query;
    }
    
    @SuppressWarnings("static-access")
    private QueryParser getQueryParser(Analyzer analyzer){
        //defaultSearchField indicates which field search against when there is no term
        //indicated in the query string.
        //The analyzer is needed so that we use the same analyzer on the search queries as
        //was used on the text that was indexed.
        QueryParser qp = new QueryParser(defaultSearchField,analyzer);
        //this sets the query parser to AND all of the query terms it finds.
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        //set up the map of stemmed field names -> unstemmed field names
//        HashMap<String,String> map = new HashMap<String, String>();
//        map.put(Entity2LuceneDoc.term.ALLTEXT,Entity2LuceneDoc.term.ALLTEXTUNSTEMMED);
//        qp.setStemmedToUnstemmed(map);
        return qp;
    }
 
    /**
     * Makes a flag based query clause.  This is where searches can filtered
     * by portal.
     *
     * If you think that search is not working correctly with protals and
     * all that kruft then this is a method you want to look at.
     *
     * It only takes into account "the portal flag" and flag1Exclusive must
     * be set.  Where does that stuff get set?  Look in vitro.flags.PortalFlag
     * 
     * One thing to keep in mind with portal filtering and search is that if
     * you want to search a portal that is different then the portal the user
     * is 'in' then the home parameter should be set to force the user into
     * the new portal.  
     * 
     * Ex.  Bob requests the search page for vivo in portal 3.  You want to
     * have a drop down menu so bob can search the all CALS protal, id 60.
     * You need to have a home=60 on your search form. If you don't set 
     * home=60 with your search query, then the search will not be in the
     * all portal AND the WebappDaoFactory will be filtered to only show 
     * things in portal 3.    
     * 
     * Notice: flag1 as a parameter is ignored. bdc34 2009-05-22.
     */
    @SuppressWarnings("static-access")
    private Query makeFlagQuery( PortalFlag flag){        
        if( flag == null || !flag.isFilteringActive() 
                || flag.getFlag1DisplayStatus() == flag.SHOW_ALL_PORTALS )
            return null;

        // make one term for each bit in the numeric flag that is set
        Collection<TermQuery> terms = new LinkedList<TermQuery>();
        int portalNumericId = flag.getFlag1Numeric();        
        Long[] bits = FlagMathUtils.numeric2numerics(portalNumericId);
        for (Long bit : bits) {
            terms.add(new TermQuery(new Term(Entity2LuceneDoc.term.PORTAL, Long
                    .toString(bit))));
        }

        // make a boolean OR query for all of those terms
        BooleanQuery boolQuery = new BooleanQuery();
        if (terms.size() > 0) {
            for (TermQuery term : terms) {
                    boolQuery.add(term, BooleanClause.Occur.SHOULD);
            }
            return boolQuery;
        } else {
            //we have no flags set, so no flag filtering
            return null;
        }
    } 
    

    private ExceptionResponseValues doSearchError(Throwable e, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Search failed: " + e.getMessage());  
        return new ExceptionResponseValues(getTemplate(f,Result.ERROR), body, e);
    }
    
    private TemplateResponseValues doBadQuery(Portal portal, String query, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", "Search " + portal.getAppName());
        body.put("query", query);
        return new TemplateResponseValues(getTemplate(f,Result.BAD_QUERY), body);
    }
    
    private TemplateResponseValues doFailedSearch(String message, String querytext, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        if ( StringUtils.isEmpty(message) ) {
            message = "Search failed.";
        }        
        body.put("message", message);
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);
    }

    private TemplateResponseValues doNoHits(String querytext, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();       
        body.put("title", "Search for '" + querytext + "'");        
        body.put("message", "No matching results.");     
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);        
    }

    /**
     * Makes a message to display to user for a bad search term.
     * @param query
     * @param exceptionMsg
     */
    private String makeBadSearchMessage(String querytext, String exceptionMsg){
        String rv = "";
        try{
            //try to get the column in the search term that is causing the problems
            int coli = exceptionMsg.indexOf("column");
            if( coli == -1) return "";
            int numi = exceptionMsg.indexOf(".", coli+7);
            if( numi == -1 ) return "";
            String part = exceptionMsg.substring(coli+7,numi );
            int i = Integer.parseInt(part) - 1;

            // figure out where to cut preview and post-view
            int errorWindow = 5;
            int pre = i - errorWindow;
            if (pre < 0)
                pre = 0;
            int post = i + errorWindow;
            if (post > querytext.length())
                post = querytext.length();
            // log.warn("pre: " + pre + " post: " + post + " term len:
            // " + term.length());

            // get part of the search term before the error and after
            String before = querytext.substring(pre, i);
            String after = "";
            if (post > i)
                after = querytext.substring(i + 1, post);

            rv = "The search term had an error near <span class='searchQuote'>"
                + before + "<span class='searchError'>" + querytext.charAt(i)
                + "</span>" + after + "</span>";
        } catch (Throwable ex) {
            return "";
        }
        return rv;
    }
    
    @SuppressWarnings("unchecked")
    private HashSet<String> getDataPropertyBlacklist(){
        HashSet<String>dpBlacklist = (HashSet<String>)
        getServletContext().getAttribute(LuceneSetup.SEARCH_DATAPROPERTY_BLACKLIST);
        return dpBlacklist;        
    }
    
    @SuppressWarnings("unchecked")
    private HashSet<String> getObjectPropertyBlacklist(){
        HashSet<String>opBlacklist = (HashSet<String>)
        getServletContext().getAttribute(LuceneSetup.SEARCH_OBJECTPROPERTY_BLACKLIST);
        return opBlacklist;        
    }
    
    
    private final String defaultSearchField = "ALLTEXT";
    public static final int MAX_QUERY_LENGTH = 500;

    
    /**
     * Need to accept notification from indexer that the index has been changed.
     */
    public void close() {
        searcher = null;        
    }

    public VitroHighlighter getHighlighter(VitroQuery q) {
        throw new Error("PagedSearchController.getHighlighter() is unimplemented");
    }

    public VitroQueryFactory getQueryFactory() {
        throw new Error("PagedSearchController.getQueryFactory() is unimplemented");
    }

    public List search(VitroQuery query) throws SearchException {
        throw new Error("PagedSearchController.search() is unimplemented");
    }

    protected boolean isRequesedFormatXml(HttpServletRequest req){
        if( req != null ){
            String param = req.getParameter(XML_REQUEST_PARAM);
            if( param != null && "1".equals(param)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    protected Format getFormat(HttpServletRequest req){
        if( req != null && req.getParameter("xml") != null && "1".equals(req.getParameter("xml")))
            return Format.XML;
        else 
            return Format.HTML;
    }
    
    protected static String getTemplate(Format format, Result result){
        if( format != null && result != null)
            return templateTable.get(format).get(result);
        else{
            log.error("getTemplate() must not have a null format or result.");
            return templateTable.get(Format.HTML).get(Result.ERROR);
        }
    }
    
    protected static Map<Format,Map<Result,String>> setupTemplateTable(){
        Map<Format,Map<Result,String>> templateTable = 
            new HashMap<Format,Map<Result,String>>();
        
        HashMap<Result,String> resultsToTemplates = new HashMap<Result,String>();
        
        //setup HTML format
        resultsToTemplates.put(Result.PAGED, "search-pagedResults.ftl");
        resultsToTemplates.put(Result.FORM, "search-form.ftl");
        resultsToTemplates.put(Result.ERROR, "search-error.ftl");
        resultsToTemplates.put(Result.BAD_QUERY, "search-badQuery.ftl");        
        templateTable.put(Format.HTML, Collections.unmodifiableMap(resultsToTemplates));
        
        //setup XML format
        resultsToTemplates = new HashMap<Result,String>();
        resultsToTemplates.put(Result.PAGED, "search-xmlResults.ftl");
        resultsToTemplates.put(Result.FORM, "search-xmlForm.ftl");
        resultsToTemplates.put(Result.ERROR, "search-xmlError.ftl");
        resultsToTemplates.put(Result.BAD_QUERY, "search-xmlBadQuery.ftl");        
        templateTable.put(Format.XML, Collections.unmodifiableMap(resultsToTemplates));
        
        return Collections.unmodifiableMap(templateTable);
    }
}
