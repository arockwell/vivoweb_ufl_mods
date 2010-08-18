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

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;

public class SitemapIndexController extends VitroHttpServlet {
	private static final Log log = LogFactory.getLog(SitemapIndexController.class.getName());

	private static ArrayList<ArrayList<String>> urlIndex = null;
    
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        try {
            super.doGet(req, res);
            VitroRequest vreq = new VitroRequest(req);
            String entityListString = vreq.getParameter("entityListNum");
            Integer entityListNum = null;
            if(entityListString != null) entityListNum = Integer.parseInt(entityListString);
            if(entityListNum != null) 
            	{
            		generateEntitySitemap(req, res, entityListNum);
            		return;
            	}
            String tabsPortalString = vreq.getParameter("tabsPortalNum");
            Integer tabsPortalNum = null;
            if(tabsPortalString != null) tabsPortalNum = Integer.parseInt(tabsPortalString);
            if(tabsPortalNum != null) 
            	{
            		generateTabsSitemap(req, res, tabsPortalNum);
            		return;
            	}
            
            setUrlIndex(vreq);
            
            vreq.setAttribute("individuals", urlIndex.size());
            vreq.setAttribute("context", req.getServerName()+":"+req.getServerPort()+req.getContextPath());
            Portal portal = vreq.getPortal();
            RequestDispatcher rd = vreq.getRequestDispatcher("/siteMapIndex.jsp");
            rd.forward(req,res);
            
        } catch (Throwable e) {
            log.error(e);
            req.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.forward(req, res);
        }
    }
    
    private void generateEntitySitemap(HttpServletRequest req, HttpServletResponse res, int listNum)
    {
    	VitroRequest vreq = new VitroRequest(req);
    	if(urlIndex == null) setUrlIndex(vreq);
    	RequestDispatcher rd = vreq.getRequestDispatcher("/siteMap.jsp");
    	vreq.setAttribute("list", urlIndex.get(listNum));
    	try {
    		rd.forward(req,res);
    	}
    	catch(Exception e)
    	{
    		log.error(e);
    	}
    }
    private void generateTabsSitemap(HttpServletRequest req, HttpServletResponse res, int portalNum)
    {
    	VitroRequest vreq = new VitroRequest(req);
    	List<Tab> tabs = (List<Tab>)vreq.getWebappDaoFactory().getTabDao().getTabsForPortal(portalNum);
    	ArrayList<String> tabsList = new ArrayList<String>();
    	for(int i=0; i<tabs.size(); i++)
    	{
    		addDecendentTabs(tabsList, tabs.get(i), portalNum, "http://"+vreq.getServerName()+":"+vreq.getServerPort()+vreq.getContextPath());    		
    	}
    	RequestDispatcher rd = vreq.getRequestDispatcher("/siteMap.jsp");
    	vreq.setAttribute("list", tabsList);
    	try {
    		rd.forward(req,res);
    	}
    	catch(Exception e)
    	{
    		log.error(e);
    	}    	
    }
    
    private void addDecendentTabs(ArrayList<String> tabsList, Tab tab, int portalNum, String contextPath)
    {
    	if(tab == null) return;
    	String tabURI = contextPath+escapeEntity("/index.jsp?home="+portalNum+"&"+tab.getTabDepthName()+"="+tab.getTabId());
    	tabsList.add(tabURI);
    	//tabsList.add(tabURI);
    	
		Collection<Tab> childTabs = tab.getChildTabs();
		if(childTabs != null)
			for(Tab t:childTabs) addDecendentTabs(tabsList, t, portalNum, contextPath);
    }
    
    private void setUrlIndex(VitroRequest vreq)
    {
    	Iterator<Individual> individualIter = vreq.getWebappDaoFactory().getIndividualDao().getAllOfThisTypeIterator();
        ArrayList<ArrayList<String>> arrays = new ArrayList<ArrayList<String>>();
        for(int i=0; individualIter.hasNext(); i++)
        {
        	ArrayList<String> individuals = new ArrayList<String>();
        	for(int j=0; j<50000 && individualIter.hasNext(); j++)
        	{
        		String individualUri = SitemapIndexController.forURL(individualIter.next().getURI());
        		
        		individualUri = "http://"+vreq.getServerName()+":"+vreq.getServerPort()+vreq.getContextPath()+"/entity?home=1&uri="+individualUri;
        		//individualUri = "http://vivo.cornell.edu/entity?home=1&uri="+individualUri;
        		
        		individuals.add(escapeEntity(individualUri));
        		
        	}
        	arrays.add(individuals);
        }
        urlIndex = arrays;
    }
    
    private static String forURL(String frag)
    {
            String result = null;
            try 
            {
                    result = URLEncoder.encode(frag, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
            return result;
    }
    
    private String escapeEntity(String frag)
    {
            if(frag.contains("&")) frag = replaceAll(frag, "&", "&amp;");
            if(frag.contains("'")) frag = replaceAll(frag, "'", "&apos;");
            if(frag.contains("\"")) frag = replaceAll(frag, "\"", "&quot;");
            if(frag.contains(">")) frag = replaceAll(frag, ">", "&gt;");
            if(frag.contains("<")) frag = replaceAll(frag, "<", "&lt;");
            return frag;
    }
    
    private String replaceAll(String original, String match, String replace)
    {
    	int index1 = original.indexOf(match);
    	int index2 = original.indexOf(replace);
    	if(index1 == index2 && index1 != -1) 
    		{
    			original = original.substring(0, index1+replace.length()+1)+replaceAll(original.substring(index1+replace.length()+1), match, replace);
    			return original;
    		}
    	if(index1 == -1) return original;
    	String before = original.substring(0, index1) + replace;
    	return before + replaceAll(original.substring(index1+1), match, replace);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

}
