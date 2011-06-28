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


package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.joda.time.DateTime;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexer;
import edu.cornell.mannlib.vitro.webapp.utils.FlagMathUtils;
import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;
import freemarker.template.TemplateModel;

/**
 * Produces the entity lists for tabs.
 *
 * @author bdc34
 *
 */
public class TabEntitiesController extends VitroHttpServlet {
    private static final long serialVersionUID = -5340982482787800013L;

    private static final Log log = LogFactory.getLog(TabEntitiesController.class.getName());
    public static int TAB_DEPTH_CUTOFF = 3;
    public static int MAX_PAGES = 40; //must be even
    public static int DEFAULT_NUMBER_INDIVIDUALS_ON_TAB = 8;
    private static int MAX_RESULTS=40000;
    private static int NON_PAGED_LIMIT=1000;
    private static Random random = new Random();
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /***********************************************
     Display a set of entities for a tab, these entities
     may be manually linked, auto-linked, a mix of these two,
     or a gallery.

     request.attributes
     a Tab object for the tabId must be in the attributes.
     It should have the key

     request.parameters
     "tabId" id of the tab to do entities for

     "tabDepth" String that is the depth of the tab in the display for
     which we are doing entities.
     leadingTab = 1, child of leadingTab = 2, etc.

     "alpha" if set to a letter entities will be filtered
     to have only that initial.

     bdc34 2006-01-12 created
     bdc34 2010-09-17 modified to use lucene for some tasks.
     */
public void doGet( HttpServletRequest req, HttpServletResponse response )
    throws IOException, ServletException {        
        super.doGet(req,response);
        
        try{
             VitroRequest request = new VitroRequest(req);
             TabDao tabDao = request.getWebappDaoFactory().getTabDao();                
                          
             int depth = getTabDepth(request);                      
             if( depth >= TAB_DEPTH_CUTOFF){
                 String tabId = request.getParameter("tabId");
                 log.debug("\ttab "+tabId+" is at, "+ depth+" below "+ TAB_DEPTH_CUTOFF);
                 return;
             }
    
             String tabId = request.getParameter("tabId");
             if( tabId == null ){
                 String e="TabEntitiesController expects that request parameter 'tabId' be set";
                 throw new ServletException(e);
             }
    
             Tab tab = TabWebUtil.findStashedTab(tabId,request);
             if( tab == null ){
                 String e="TabEntitiesController expects that tab"+tabId+" will be in the request attribute. "
                 +"It should have been placed there by a call to TabWebUtil.stashTabsInRequest in tabPrimary.jsp";
                 throw new ServletException(e);
             }                 
             req.setAttribute("tabId", tab.getTabId());
             request.setAttribute("controllerParam","primary=" + tab.getTabId());
             
             String alpha = request.getParameter("alpha");
             boolean doAlphaFilter = false;
             if(( alpha != null && alpha.length() == 1) ){                 
                 doAlphaFilter = true;
                 request.setAttribute("alpha", alpha.toUpperCase()); 
             }
             
             boolean doPagedFilter = request.getParameter("page") != null;                          
             boolean showPaged = true;
             if( tab.getGalleryRows() != 1 ){
                 /* bjl23 20061006: The tab.getGalleryRows()>1 is a hack to use this field as a switch to turn on 
                  * alpha filter display in non-gallery tabs. We need to add a db field for this. */
                 request.setAttribute("showAlpha","1");                 
                 showPaged = true;
             }            
             
             List<String> manuallyLinkedUris = Collections.emptyList();
             List<String> autoLinkedUris = Collections.emptyList();
             if( tab.isAutoLinked() || tab.isMixedLinked())
                 autoLinkedUris = request.getWebappDaoFactory().getTabDao().getTabAutoLinkedVClassURIs(tab.getTabId());
             if( tab.isManualLinked() || tab.isMixedLinked())
                 manuallyLinkedUris = request.getWebappDaoFactory().getTabDao().getTabManuallyLinkedEntityURIs(tab.getTabId());
             
             //If a tab is not linked to any types or individuals, don't show alpha or page filter links
             boolean tabNotLinked =  tabNotLinked( tab, autoLinkedUris, manuallyLinkedUris); 
             if( tabNotLinked ){
                 request.setAttribute("showAlpha", "0");
                 showPaged = false;
             }
             
             int page = getPage(request);
             int entsPerTab = getSizeForNonGalleryTab(tab, showPaged);
             
             int size = 0;
             if( ! tabNotLinked ){
                 try{
                     //a lot of the work happens in this next line
                     size = addLinkedIndividualsForTab (tab,request, autoLinkedUris, manuallyLinkedUris, 
                             alpha, page, entsPerTab, depth, doAlphaFilter, doPagedFilter);
                 }catch(Exception e){
                     log.warn(e,e);
                 }
             }
             
             //only show page list when the tab is depth 1.
             if( showPaged && depth == 1 && size > 0 && size > entsPerTab ){ 
                 request.setAttribute("showPages",Boolean.TRUE);
                 //make a data structure to hold information about paged tabs
                 request.setAttribute("pages", makePagesList(size, entsPerTab, page ));                 
             }else{
                 request.setAttribute("showPages",Boolean.FALSE);
             }
                        
             if( tab.isAutoLinked() || tab.isGallery() ){
                 if( doAlphaFilter ){
                     doAlphaFiltered(alpha, tab, request, response, tabDao, size);
                 }else{
                     doAutoLinked( tab, request, response, tabDao, size);
                 }
                              
             }else if( tab.isMixedLinked() || tab.isManualLinked() ){
                 doAutoLinked(tab,request,response,tabDao,size);
             }else{
                 log.debug("TabEntitiesController: doing none for tabtypeid: "
                         + tab.getTabtypeId() +" and link mode: " + tab.getEntityLinkMethod());
             }

             
        } catch (Throwable e) {            
            req.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.include(req, response);
        }
    }

    /**
     * This build a lucene query for the tab, runs the query, gets individuals for that query,
     * and adds the individuals to the tab.
     * 
     * @param tab - this parameter is mutated by this method
     * @param request
     * @param autoLinkedUris
     * @param manuallyLinkedUris
     * @param alpha - letter for alpha filtering
     * @param page - page for page filtering
     * @param entsPerTab 
     * @param depth
     * @param doAlphaFilter
     * @param doPagedFilter
     * @return total number of ents associated with tab regardless of filtering
     */
    private int addLinkedIndividualsForTab(Tab tab, VitroRequest request, 
            List<String> autoLinkedUris, List<String> manuallyLinkedUris, 
            String alpha, int page, int entsPerTab, int depth, 
            boolean doAlphaFilter , boolean doPagedFilter) {

        //try to get the URIs of the required individuals from the lucene index
        IndexSearcher index = LuceneIndexFactory.getIndexSearcher(getServletContext());
        boolean isSinglePortal = request.getWebappDaoFactory().getPortalDao().isSinglePortal();
        BooleanQuery query = null;
        if( tab.isAutoLinked() ){
            query = getQuery(tab, autoLinkedUris, null, alpha, isSinglePortal);
        }else if (tab.isManualLinked() ){
            query = getQuery(tab, null, manuallyLinkedUris, alpha, isSinglePortal);
        }else if ( tab.isMixedLinked() ){
            query = getQuery(tab, autoLinkedUris, manuallyLinkedUris, alpha, isSinglePortal);
        }else{
           log.error("Tab " + tab.getTabId() + " is neither manually, auto nor mixed. ");   
        }
          
        if( tab.getDayLimit() != 0  ){
            query = addDayLimit( query, tab );
        }
        boolean onlyWithThumbImg = false;
        if( depth > 1 && tab.getImageWidth() > 0 ){
            onlyWithThumbImg = true;
        }       
        
        IndividualDao indDao = request.getWebappDaoFactory().getIndividualDao();
        int size = 0;
        
        try {
            String sortField = tab.getEntitySortField();
            Sort sort = null;
            if( sortField != null && !doAlphaFilter && !doPagedFilter ){
                if( sortField.equalsIgnoreCase("timekey") || tab.getDayLimit() > 0){
                    sort =     new Sort(Entity2LuceneDoc.term.TIMEKEY);
                }else if( sortField.equalsIgnoreCase("sunrise") || tab.getDayLimit() < 0 ){
                    sort =     new Sort(Entity2LuceneDoc.term.SUNRISE, true);
                }else if( sortField.equalsIgnoreCase("sunset") ){
                    sort =     new Sort(Entity2LuceneDoc.term.SUNSET);
                }else{
                    sort =  new Sort(Entity2LuceneDoc.term.NAMELOWERCASE);
                }
            } else {
                sort =     new Sort(Entity2LuceneDoc.term.NAMELOWERCASE);
            }
            
            if( depth > 1 && "rand()".equalsIgnoreCase(sortField) ){
                    sort = null;                     
            }
                             
            TopDocs docs;
            if( sort != null )
                docs = index.search(query, null, MAX_RESULTS, sort);                     
            else
                docs = index.search(query, null, MAX_RESULTS);
            
           if( docs == null ){
               log.error("Search of lucene index returned null");
               return 0;
           }
           
           size = docs.totalHits;
           // don't get all the results, only get results for the requestedSize
           List<Individual> results = new ArrayList<Individual>(entsPerTab);
           int entsAddedToTab = 0;
           int ii = (page-1)*entsPerTab;
           boolean doingRandom = false;
           if(   !doAlphaFilter && !doPagedFilter && depth > 1 && size > 1 && "rand()".equalsIgnoreCase(tab.getEntitySortField())){
               doingRandom = true;
               ii = random.nextInt( size );
           }
           boolean looped = false;
           while( entsAddedToTab < entsPerTab && ii < docs.scoreDocs.length ){
               ScoreDoc hit = docs.scoreDocs[ii];
               if (hit != null) {
                   Document doc = index.doc(hit.doc);
                   if (doc != null) {
                       if( onlyWithThumbImg && "0".equals(doc.getField(Entity2LuceneDoc.term.THUMBNAIL).stringValue()) ){
                           //do Nothing                                                
                       }else{
                           String uri = doc.getField(Entity2LuceneDoc.term.URI).stringValue();
                           Individual ind = indDao.getIndividualByURI( uri );                                
                           results.add( ind );                         
                           entsAddedToTab++;
                       }
                   } else {
                       log.warn("no document found for lucene doc id " + hit.doc);
                   }
               } else {
                   log.debug("hit was null");
               }  
               if( doingRandom && ii >= docs.scoreDocs.length && ! looped){
                   ii=0;
                   looped = true;
               }else{                        
                   ii++;
               }
           }   
           
           //mutate state of tab
           tab.setRelatedEntityList(results);                
        }catch(IOException ex){
            log.warn(ex,ex);
            return size;
        }         
        return size;
    }

    private boolean tabNotLinked(Tab tab, List<String> autoLinkedUris,
            List<String> manuallyLinkedUris) {
        if( tab.isManualLinked() )
            return manuallyLinkedUris.isEmpty();
        else if( tab.isAutoLinked() )
            return autoLinkedUris.isEmpty();
        else
            return autoLinkedUris.isEmpty() && manuallyLinkedUris.isEmpty();        
    }

    private BooleanQuery addDayLimit(BooleanQuery query, Tab tab) {
        DateTime now = new DateTime();
        if( tab.getDayLimit() > 0 ){
            String start = now.toString(LuceneIndexer.DATE_FORMAT);
            String future = now.plusDays( tab.getDayLimit() ).toString(LuceneIndexer.DATE_FORMAT);
            query.add( new RangeQuery(new Term(Entity2LuceneDoc.term.TIMEKEY,start),new Term(Entity2LuceneDoc.term.TIMEKEY,future), true) , BooleanClause.Occur.MUST);
        }else{
            String end = now.toString(LuceneIndexer.DATE_FORMAT);
            String past = now.minusDays( tab.getDayLimit() ).toString(LuceneIndexer.DATE_FORMAT);
            query.add( new RangeQuery(new Term(Entity2LuceneDoc.term.SUNRISE,past),new Term(Entity2LuceneDoc.term.SUNRISE,end), true) , BooleanClause.Occur.MUST);
        }
        return query;
    }

    private void doAlphaFiltered(String alpha, Tab tab,
            VitroRequest request, HttpServletResponse response, TabDao tabDao, int size)
    throws ServletException, IOException {
        log.debug("in doAlphaFitlered");        
        
        request.setAttribute("entities", tab.getRelatedEntities());
        request.setAttribute("alpha", alpha);
        request.setAttribute("count", Integer.toString(size) );
        request.setAttribute("tabParam",tab.getTabDepthName()+"="+tab.getTabId());
        request.setAttribute("letters",Controllers.getLetters());
        //request.setAttribute("letters",tab.grabEntityFactory().getLettersOfEnts());
        request.setAttribute("servlet",Controllers.TAB);
        String jsp = Controllers.ENTITY_LIST_FOR_TABS_JSP;
        RequestDispatcher rd =
            request.getRequestDispatcher(jsp);
        rd.include(request, response);        
    }
    
    private void doAutoLinked(Tab tab, VitroRequest request, HttpServletResponse response, TabDao tabDao, int size)
    throws ServletException, IOException {
        log.debug("in doAutoLinked");
                                                                            
        request.setAttribute("entities", tab.getRelatedEntities());           
        request.setAttribute("count", Integer.toString(size));            
        request.setAttribute("tabParam",tab.getTabDepthName()+"="+tab.getTabId());
        request.setAttribute("letters",Controllers.getLetters());
        request.setAttribute("servlet",Controllers.TAB);
        String jsp = Controllers.ENTITY_LIST_FOR_TABS_JSP;
        RequestDispatcher rd =
            request.getRequestDispatcher(jsp);
            rd.include(request, response);        
    }

    private int getPage(VitroRequest request) {
        String p = request.getParameter("page") ;
        if( p == null )
            return 1;
        try{
            return Integer.parseInt(p);
        }catch(Exception e){
            return 1;
        }
    }

    /**
     * Mixed and auto linked tabs get their individuals via
     * a query to the lucene index.  This is to allow sorting and alpha
     * filtering  of the combined list.  Manually linked tabs do not
     * get their individuals using lucene, see doManual(). 
     * 
     */
    private BooleanQuery getQuery(Tab tab, List<String> vclassUris, List<String> manualUris, String alpha , boolean isSinglePortal){
        BooleanQuery query = new BooleanQuery();
        try{
            
            //make the type query if needed            
            BooleanQuery typeQuery = null;
            if( tab.isAutoLinked() || tab.isMixedLinked() ){
                   typeQuery = new BooleanQuery();
                   BooleanQuery queryForTypes = new BooleanQuery();
                   //setup up type queries in their own sub query
                   for( String vclassUri : vclassUris ){
                       if(  vclassUri != null && !"".equals(vclassUri)){                                   
                           queryForTypes.add(  
                              new TermQuery( new Term(Entity2LuceneDoc.term.RDFTYPE, vclassUri)),
                              BooleanClause.Occur.SHOULD );
                       }
                   }
                   typeQuery.add(queryForTypes, BooleanClause.Occur.MUST);                    
                   
                   //check for portal filtering only on auto linked queries
                   if( ! isSinglePortal ){
                       int tabPortal = tab.getPortalId();
                       if( tabPortal < 16 ){ //could be a normal portal
                           typeQuery.add(
                                   new TermQuery( new Term(Entity2LuceneDoc.term.PORTAL, Integer.toString(1 << tab.getPortalId() ))),
                                   BooleanClause.Occur.MUST);
                       }else{ //could be a combined portal
                           BooleanQuery tabQueries = new BooleanQuery();
                           Long[] ids= FlagMathUtils.numeric2numerics(tabPortal);
                           for( Long id : ids){                               
                               tabQueries.add(
                                       new TermQuery( new Term(Entity2LuceneDoc.term.PORTAL,id.toString()) ),
                                       BooleanClause.Occur.SHOULD);
                           }
                           typeQuery.add(tabQueries,BooleanClause.Occur.MUST);
                       }
                   }                                      
            }
           
            //make query for manually linked individuals
            BooleanQuery manualQuery = null;
            boolean foundManual = false;
            if( tab.isManualLinked() || tab.isMixedLinked()){
                manualQuery = new BooleanQuery();
                BooleanQuery queryForManual = new BooleanQuery();                
                for( String indURI : manualUris){
                    if( indURI != null ){
                        queryForManual.add( 
                                new TermQuery( new Term(Entity2LuceneDoc.term.URI, indURI)),
                                BooleanClause.Occur.SHOULD );
                        foundManual = true;
                    }
                    
                }
                if( foundManual )
                    manualQuery.add( queryForManual, BooleanClause.Occur.MUST);
            }
            
       
            
           if( tab.isAutoLinked() || !foundManual ){
               query = typeQuery;
           }else if ( tab.isManualLinked() ){
               query =  manualQuery;
           }else{
               BooleanQuery orQuery = new BooleanQuery();
               orQuery.add( typeQuery, BooleanClause.Occur.SHOULD);
               orQuery.add( manualQuery, BooleanClause.Occur.SHOULD);
               query.add( orQuery, BooleanClause.Occur.MUST);
           }
           
           //Add alpha filter if it is needed
           Query alphaQuery = null;
           if( alpha != null && !"".equals(alpha) && alpha.length() == 1){      
               alphaQuery =    
                   new PrefixQuery(new Term(Entity2LuceneDoc.term.NAMELOWERCASE, alpha.toLowerCase()));
               query.add(alphaQuery,BooleanClause.Occur.MUST);
           }           
           
                           
           log.debug("Query for tab " + tab.getTabId() + ": " + query);
           return query;
       }catch (Exception ex){
           log.error(ex,ex);
           return new BooleanQuery();        
       }        
    }    

    private int getSizeForGalleryTab(Tab tab){
        int rows = tab.getGalleryRows() != 0 ? tab.getGalleryRows() : 8;
        int col = tab.getGalleryCols() != 0 ? tab.getGalleryCols() : 8;
        return rows  * col;        
    }
    
    private int getSizeForNonGalleryTab(Tab tab, boolean showPaged ){  
        if( showPaged )
            if( tab.getGalleryCols() == 0 || tab.getGalleryRows() == 0 )
                return 8;
            else 
                return getSizeForGalleryTab(tab);           
        else
            return NON_PAGED_LIMIT;     
    }

    private int getTabDepth(VitroRequest request){
        String obj = null;
        try {
           obj = request.getParameter("tabDepth");
           if( obj == null ){
            String e="TabEntitesController expects that request parameter 'tabDepth' be set"
            +", use 1 as the leading tab's depth.";
            throw new ServletException(e);
          }
           return Integer.parseInt((String)obj);
        }catch(Exception ex){
            return 1;
        }
    }
    
    public static List<PageRecord> makePagesList( int count, int pageSize,  int selectedPage){        
        
        List<PageRecord> records = new ArrayList<PageRecord>( MAX_PAGES + 1 );
        int requiredPages = count/pageSize ;
        int remainder = count % pageSize ; 
        if( remainder > 0 )
            requiredPages++;
        
        if( selectedPage < MAX_PAGES && requiredPages > MAX_PAGES ){
            //the selected pages is within the first maxPages, just show the normal pages up to maxPages.
            for(int page = 1; page < requiredPages && page <= MAX_PAGES ; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }
            records.add( new PageRecord( "page="+ (MAX_PAGES+1), Integer.toString(MAX_PAGES+1), "more...", false));
        }else if( requiredPages > MAX_PAGES && selectedPage+1 > MAX_PAGES && selectedPage < requiredPages - MAX_PAGES){
            //the selected pages is in the middle of the list of page
            int startPage = selectedPage - MAX_PAGES / 2;
            int endPage = selectedPage + MAX_PAGES / 2;            
            for(int page = startPage; page <= endPage ; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }
            records.add( new PageRecord( "page="+ endPage+1, Integer.toString(endPage+1), "more...", false));
        }else if ( requiredPages > MAX_PAGES && selectedPage > requiredPages - MAX_PAGES ){
            //the selected page is in the end of the list 
            int startPage = requiredPages - MAX_PAGES;      
            double max = Math.ceil(count/pageSize);
            for(int page = startPage; page <= max; page++ ){
                records.add( new PageRecord( "page=" + page, Integer.toString(page), Integer.toString(page), selectedPage == page ) );            
            }          
        }else{
            //there are fewer than maxPages pages.            
            for(int i = 1; i <= requiredPages; i++ ){
                records.add( new PageRecord( "page=" + i, Integer.toString(i), Integer.toString(i), selectedPage == i ) );            
            }    
        }                
        return records;
    }
    
    public static class PageRecord  {
        public PageRecord(String param, String index, String text, boolean selected) {            
            this.param = param;
            this.index = index;
            this.text = text;
            this.selected = selected;
        }
        public String param;
        public String index;
        public String text;        
        public boolean selected=false;
        
        public String getParam() {
            return param;
        }
        public String getIndex() {
            return index;
        }
        public String getText() {
            return text;
        }
        public boolean getSelected(){
            return selected;
        }
    }
}
