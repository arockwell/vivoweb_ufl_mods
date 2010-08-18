<%--
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
--%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/PropertyEditLink" prefix="edLnk" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Property" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.KeywordProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<jsp:useBean id="loginHandler" class="edu.cornell.mannlib.vedit.beans.LoginFormBean" scope="session" />
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.dashboardPropsList.jsp");
%>
<%
boolean showSelfEdits=false;
boolean showCuratorEdits=false;
if( VitroRequestPrep.isSelfEditing(request) ) {
    showSelfEdits=true;
    log.debug("self editing active");
} else {
    log.debug("self editing inactive");
}
if (loginHandler!=null && loginHandler.getLoginStatus()=="authenticated" && Integer.parseInt(loginHandler.getLoginRole())>=loginHandler.getEditor()) {
	showCuratorEdits=true;
	log.debug("curator editing active");
} else {
	log.debug("curator editing inactive");
}%>
<c:set var='entity' value='${requestScope.entity}'/><%-- just moving this into page scope for easy use --%>
<c:set var='portal' value='${requestScope.portalBean}'/><%-- likewise --%>
<%
	log.debug("Starting dashboardPropsList.jsp");

	// The goal here is to retrieve a list of object and data properties appropriate for the vclass
	// of the individual, by property group, and sorted the same way they would be in the public interface

	Individual subject = (Individual) request.getAttribute("entity");
	if (subject==null) {
    	throw new Error("Subject individual must be in request scope for dashboardPropsList.jsp");
	}
	
	String defaultGroupName=null;
	String unassignedGroupName = (String) request.getAttribute("unassignedPropsGroupName");
	if (unassignedGroupName != null && unassignedGroupName.length()>0) {
	    defaultGroupName = unassignedGroupName;
	    log.debug("found temp group attribute \""+unassignedGroupName+"\" for unassigned properties");
	}

    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
	PropertyGroupDao pgDao = wdf.getPropertyGroupDao();
	ArrayList<PropertyGroup> groupsList = (ArrayList) request.getAttribute("groupsList");
	if (groupsList != null) {
	    if (groupsList.size()>1) {%>
	        <ul id="propGroupNav">
<%        	for (PropertyGroup g : groupsList) { %>
				<li><h2><a href="#<%=g.getLocalName()%>" title="<%=g.getName()%>"><%=g.getName()%></a></h2></li>
<%		    }%>
			</ul>
<%	    }
	} else {
		ArrayList<Property> mergedList = (ArrayList) request.getAttribute("dashboardPropertyList");
		if (mergedList!=null) {
            String lastGroupName = null;
		    int groupCount=0;%>
		    <ul id="propGroupNav">
<%		    for (Property p : mergedList) {
    		    String groupName = defaultGroupName; // may be null
 			    String groupLocalName = defaultGroupName; // may be null
			    String groupPublicDescription=null;
			    String propertyLocalName = p.getLocalName() == null ? "unspecified" : p.getLocalName();
			    String openingGroupLocalName = (String) request.getParameter("curgroup");
    		    if (p.getGroupURI()!=null) {
    		        PropertyGroup pg = pgDao.getGroupByURI(p.getGroupURI());
    		        if (pg != null) {
		    		    groupName=pg.getName();
		    		    groupLocalName=pg.getLocalName();
		    		    groupPublicDescription=pg.getPublicDescription();
    		        }
    		    }
		        if (groupName != null && !groupName.equals(lastGroupName)) {
		    	    lastGroupName=groupName;
		            ++groupCount;
	    
			        if (openingGroupLocalName == null || openingGroupLocalName.equals("")) {
				        openingGroupLocalName = groupLocalName;
			        }
			        if (openingGroupLocalName.equals(groupLocalName)) {%>
      		            <li class="currentCat"><h2><a href="#<%=groupLocalName%>" title="<%=groupName%>"><%=groupName%></a></h2></li>
<%			        } else { %>
        		        <li><h2><a href="#<%=groupLocalName%>" title="<%=groupName%>"><%=groupName%></a></h2></li>
<%		            } 
                }
            }%>
            </ul>
<%      }
    }%>

