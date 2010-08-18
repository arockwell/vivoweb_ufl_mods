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

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class UpdateEntityFlagServlet extends VitroHttpServlet {
    private static final int DEFAULT_PORTAL_ID=1;
    private static final Log log = LogFactory.getLog(UpdateEntityFlagServlet.class.getName());

    public void doPost(HttpServletRequest req, HttpServletResponse response) {
    	
    	VitroRequest request = new VitroRequest(req);
    	
        Connection con=null;
        try {
            HttpSession session = request.getSession();
            LoginFormBean f = (LoginFormBean) session.getAttribute( "loginHandler" );

            //don't need to touch the users database for now

            // JCR 20040905 passing on portal home parameter
            String portalIdStr=(portalIdStr=request.getParameter("home"))==null?String.valueOf(DEFAULT_PORTAL_ID):portalIdStr;
            //request.setAttribute("home",portalIdStr);


            int entityId=0;
            // YES, you do want to look at the QuerySpecId parameter, not an entityId parameter in the next line
            String entityIdStr=(entityIdStr=request.getParameter("querySpecId"))==null?String.valueOf(entityId):entityIdStr;
            try {
                entityId=Integer.parseInt(entityIdStr);
            } catch (Exception ex) {
                log.error("error -- could not parse " + entityIdStr + " as an integer value");
                request.setAttribute("processError","Exception when parsing entity id: "+entityIdStr+" as integer value: " + ex.getMessage());
                getServletConfig().getServletContext().getRequestDispatcher("/fetch?queryspec=entityv&linkwhere=entities.id="+entityId).forward( request, response );
                return;
            }

            //try {
                // TODO: this way of getting a connection is soon going away.
                //con=getVitroConnection().getConnection();
                con = null;
                if (con==null) {
                    request.setAttribute("processError","SQLException on establishing database pool connection: connection is null");
                    getServletConfig().getServletContext().getRequestDispatcher("/fetch?queryspec=entityv&linkwhere=entities.id="+entityId).forward( request, response );
                    return;
                }
//            } catch (SQLException ex) {
//                request.setAttribute("processError","SQLException on establishing database connection: "+ex.getMessage());
//                getServletConfig().getServletContext().getRequestDispatcher("/fetch?queryspec=entityv&linkwhere=entities.id="+entityId).forward( request, response );
//                return;
//            }

            Statement stmt=con.createStatement();

            String[] flag1ParameterValues = request.getParameterValues("flag1s");
            String updateEntityStr="UPDATE entities SET flag1Set='";
            String flag1SetMembers="";
            if ( flag1ParameterValues != null ) {
                for ( int i=0; i<flag1ParameterValues.length; i++ ) {
                    if (i==0) {
                        flag1SetMembers=flag1ParameterValues[i];
                    } else {
                        flag1SetMembers+="," + flag1ParameterValues[i];
                    }
                }
            }
            updateEntityStr+=flag1SetMembers + "',flag2Set='";

            String[] flag2ParameterValues = request.getParameterValues("flag2s");
            String flag2SetMembers="";
            if ( flag2ParameterValues != null ) {
                for ( int i=0; i<flag2ParameterValues.length; i++ ) {
                    if (i==0) {
                        flag2SetMembers=flag2ParameterValues[i];
                    } else {
                        flag2SetMembers+="," + flag2ParameterValues[i];
                    }
                }
            }
            updateEntityStr+=flag2SetMembers + "',flag3Set='";

            String[] flag3ParameterValues = request.getParameterValues("flag3s");
            String flag3SetMembers="";
            if ( flag3ParameterValues != null ) {
                for ( int i=0; i<flag3ParameterValues.length; i++ ) {
                    if (i==0) {
                        flag3SetMembers=flag3ParameterValues[i];
                    } else {
                        flag3SetMembers+="," + flag3ParameterValues[i];
                    }
                }
            }
            updateEntityStr+=flag3SetMembers + "' WHERE id='"+entityId + "'";

            try {
                int updateCount=stmt.executeUpdate(updateEntityStr);
            } catch (SQLException ex) {
                log.error("Error from updating entity filtering flags via " + updateEntityStr + ": " + ex.getMessage());
                stmt.close();
                con.close();
                return;
            }
            stmt.close();
            con.close();
            response.sendRedirect("entity?home="+portalIdStr+"&id="+entityId);
        } catch (Exception ex) {
            log.error( ex.getMessage() );
            ex.printStackTrace();
        }finally {
            updateSearchIndex(request);
            //VitroConnection.close(con);
        }
    }

    /** Updates the search index. */
    private void updateSearchIndex(HttpServletRequest request){
        IndexBuilder builder = (IndexBuilder)getServletContext().getAttribute(IndexBuilder.class.getName());
        if( builder != null )
        	builder.doUpdateIndex();
    }
}

