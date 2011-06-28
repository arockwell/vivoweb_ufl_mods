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

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.PageController;
import edu.cornell.mannlib.vitro.webapp.dao.PageDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
/**
 * This filter is intended to route requests to pages defined in the display model.
 * 
 * It should be last in the chain of filters since it will not call filters further
 * down the chain.
 * 
 * It should only be applied to requests, not forwards, includes or errors.
 */
public class PageRoutingFilter implements Filter{
    protected FilterConfig filterConfig;
    
    private final static Log log = LogFactory.getLog( PageRoutingFilter.class);
        
    protected final static String URL_PART_PATTERN = "(/[^/]*).*";
    protected final static String PAGE_CONTROLLER_NAME = "PageController";
    protected final static String HOME_CONTROLLER_NAME = "HomePageController";
    protected final static String TAB_CONTROLLER_NAME = "TabController";
    
    protected final Pattern urlPartPattern = Pattern.compile(URL_PART_PATTERN);    
    
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.filterConfig = arg0;    
        log.debug("pageRoutingFilter setup");
    }
    
    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain) 
        throws IOException, ServletException {        
        PageDao pageDao = getPageDao();        
        Map<String,String> urlMappings = pageDao.getPageMappings();
        
        // get URL without hostname or servlet context
        HttpServletResponse response = (HttpServletResponse) arg1;
        HttpServletRequest req = (HttpServletRequest) arg0;        
        String path = req.getRequestURI().substring(req.getContextPath().length());
        
        // check for first part of path
        // ex. /hats/superHat -> /hats
        Matcher m = urlPartPattern.matcher(path);
        if( m.matches() && m.groupCount() >= 1){
            String path1stPart = m.group(1);            
            String pageUri = urlMappings.get(path1stPart);
            
            //try it with a leading slash?
            if( pageUri == null )
                pageUri = urlMappings.get("/"+path1stPart);            
            
            if( pageUri != null && ! pageUri.isEmpty() ){
                log.debug(path + "is a request to a page defined in the display model as " + pageUri );
                		
                //add the pageUri to the request scope for use by the PageController
                PageController.putPageUri(req, pageUri);
                
                //This will send requests to HomePageController or PageController
                String controllerName = getControllerToForwardTo(req, pageUri, pageDao);            
                log.debug(path + " is being forwarded to controller " + controllerName);
                
                RequestDispatcher rd = filterConfig.getServletContext().getNamedDispatcher( controllerName );            
                rd.forward(req, response);
            }else if( "/".equals( path ) || path.isEmpty() ){
                log.debug("url '" +path + "' is being forward to home controller" );
                RequestDispatcher rd = filterConfig.getServletContext().getNamedDispatcher( HOME_CONTROLLER_NAME );            
                rd.forward(req, response);
            }else{
                doNonDisplayPage(path,arg0,arg1,chain);
            }
        }else{
            doNonDisplayPage(path,arg0,arg1,chain);    
        }        
    }
    
    protected void doNonDisplayPage(String path, ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException, ServletException{
        log.debug(path + "this isn't a request to a page defined in the display model, handle it normally.");
        chain.doFilter(arg0, arg1);    
    }
    
    protected String getControllerToForwardTo(HttpServletRequest req,
            String pageUri, PageDao pageDao) {
        
        if( isTabController(req) )
            return TAB_CONTROLLER_NAME;        
        String homePageUri = pageDao.getHomePageUri();
        if( pageUri != null && pageUri.equals(homePageUri) )
            return HOME_CONTROLLER_NAME;
        else
            return PAGE_CONTROLLER_NAME;
    }

    /**
     * Checks to see if this is a request to the old tab controller
     */
    protected boolean isTabController( HttpServletRequest req  ){
        if( req.getParameter("primary") != null ||
            req.getParameter("secondary") != null ||
            req.getParameter("collection") != null ||
            req.getParameter("subcollection") != null ){
            String path = req.getRequestURI().substring(req.getContextPath().length());
            return "/".equals(path) ;
        }
        return false;                
    }
    
    protected PageDao getPageDao(){
        WebappDaoFactory wdf = (WebappDaoFactory) 
            filterConfig.getServletContext().getAttribute("webappDaoFactory");
        return wdf.getPageDao();
    }
        
    @Override
    public void destroy() {
       //nothing to do here        
    }
}
