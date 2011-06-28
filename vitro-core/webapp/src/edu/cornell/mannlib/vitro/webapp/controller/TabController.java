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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.web.TabWebUtil;

public class TabController extends VitroHttpServlet {
    private static final long serialVersionUID = -5340982482787800013L;
    private static final Log log = LogFactory.getLog(TabController.class.getName());

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
        try {
            //this will set up the portal
            super.doGet(request,response);
            VitroRequest vreq = new VitroRequest(request);

            if ( checkForNoPortal(request, response) )
                return;
            
            String viewParam = request.getParameter("view");
            String toJsp = Controllers.BASIC_JSP;
            if(viewParam != null && viewParam.equals("atom")) toJsp = "/templates/tabs/tabAtom.jsp";
            if(viewParam != null && viewParam.equals("exhibit")) toJsp = "/templates/tabs/tabExhibit.jsp";
            // here we figure out what level tab this is requesting,
            // what tab, and how that tab is laid out.
            // armed with this info we then foist the handling of the
            // request to some other code

            //get tab and subtabs.
            Tab leadingTab = populateLeadingTab(request);
            request.setAttribute("leadingTab",leadingTab);            

            Portal portal = vreq.getPortal();

            if (leadingTab.getTabId()==portal.getRootTabId()) {
            	request.setAttribute("homePageRequested", "true");
                request.setAttribute("title", portal.getAppName());
                RequestDispatcher rd = request.getRequestDispatcher("/home");
                rd.forward(request, response);
                return;
            }
            else {
                request.setAttribute("title",leadingTab.getTitle());
            }

            String body = leadingTab.getBody();
            if( body != null && body.startsWith("JSPBody:") )                 
                request.setAttribute("bodyJsp", body.substring(body.indexOf(":")+1));
            else
                request.setAttribute("bodyJsp", Controllers.TAB_PRIMARY_JSP);

            /* *** send it to a jsp to get rendered ***** */
            RequestDispatcher rd = request.getRequestDispatcher(toJsp);
            rd.forward(request, response);

        } catch (Throwable e) {
            request.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = request.getRequestDispatcher("/error.jsp");
            rd.forward(request, response);
        }
    }

    /**
     * bump users who come to research.cals.cornell.edu to research home page on CALS site
     * @param request
     * @param response
     * @throws IOException
     */
    public boolean checkForNoPortal(HttpServletRequest request,
                                    HttpServletResponse response )
    throws ServletException, IOException{
        String requestURL=request.getRequestURL().toString();
        String queryString=request.getQueryString();

        boolean bad = false;

//         HttpSession session = request.getSession(false);
//         Object obj;
//         if( session != null && (obj=session.getAttribute("currentPortalId"))!=null){
//             if(obj instanceof Integer){
//                 Integer n = (Integer)obj;
//                 if( ( 2 <= n.intValue() <= 5 ) || n.intValue() == 60)
//                     return false;
//                 else
//                     bad = true;
//             }
//         }
        String str = "";
        if( (str = (String)request.getAttribute("home")) != null){
            if("2".equals( str) || "3".equals( str) || "4".equals( str) ||
               "5".equals( str) || "60".equals(str) ){
                return false;
            }
        }

        if (requestURL.equalsIgnoreCase("http://research.cals.cornell.edu/")
               && ( queryString==null || queryString.equals("")))
                bad = true;
        if( requestURL.equalsIgnoreCase("http://research.cals.cornell.edu/index.jsp")
               && ( queryString==null || queryString.equals("")))
            bad = true;

        if(bad){
            // sending a redirect HTTP header to avoid problems with browsers set to ignore meta-refresh
            response.sendRedirect("http://research.cals.cornell.edu/?home=60");
            /*
            ServletOutputStream out = response.getOutputStream();
            out.print(
                      "<html><head>"+
                      "<meta http-equiv='refresh' "+
                      "content='0;URL=http://research.cals.cornell.edu/index.jsp?home=60' />"+
                      "<title>CALS Research</title>"+
                      "</head>"+
                      "<body bgcolor='black'>"+
                      "</body>"+
                      "</html>");
            */
            return true;
        }
        return false;
    }

       /**
     * Return a tab bean the way that Index.jsp and beans.TabBean use to.
     * This will get a tab and its subtabs.
     * This pulls all the info it needs about what tabs and what portals
     * from the HttpServletRequest.
     *
     * There is some leftover stuff related to filteringAlphabetically, maybe this
     * should be stuck in request as an attribute?
     * @param request
     * @return
     * @throws javax.servlet.jsp.JspException
        */
    public Tab populateLeadingTab(HttpServletRequest request) throws JspException {
        Tab tab = null;
        VitroRequest vreq = new VitroRequest(request);

        ApplicationBean appBean= vreq.getAppBean();
        int leadingTabId = TabWebUtil.getTabIdFromRequest(vreq);

        // bdc34: not reading authorization out of user id presently
        // where do we get that from?
        int auth_level=0;

        // Now populate the tab data structure for this page, this will get subtabs
        if (leadingTabId>0 && appBean != null)
            tab = vreq.getWebappDaoFactory().getTabDao().getTab(leadingTabId,auth_level,appBean);

        if( tab == null ) {
        	tab = new Tab();
        	tab.setTitle("Warning: tab not found ("+leadingTabId+")");
            //throw new JspException("TabWebUtil.populateLeadingTab() -" +
            //        " unable to find tab id: " + leadingTabId);
        }
        
        return tab;
    }
   
}
