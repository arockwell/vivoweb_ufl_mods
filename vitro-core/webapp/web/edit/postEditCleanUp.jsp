<%--
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
--%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.StringUtils" %>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@page import="com.hp.hpl.jena.rdf.model.ResourceFactory"%>
<%@page import="com.hp.hpl.jena.rdf.model.Property"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jstl/functions" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.1" prefix="str" %>


<%
    /* Clear any cruft from session. */
    String resourceToRedirectTo = null;	
    String urlPattern = null;
    String predicateLocalName = null;
    String predicateAnchor = "";
    if( session != null ) {
        EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
        //In order to support back button resubmissions, don't remove the editConfig from session.
        //EditConfiguration.clearEditConfigurationInSession(session, editConfig);
        
        EditSubmission editSub = EditSubmission.getEditSubmissionFromSession(session,editConfig);        
        EditSubmission.clearEditSubmissionInSession(session, editSub);
        
        if( editConfig != null ){
            String predicateUri = editConfig.getPredicateUri();            
            if( predicateUri != null ){
            	try{
            		Property prop = ResourceFactory.createProperty(predicateUri);
            		predicateLocalName = prop.getLocalName();
            	}catch (com.hp.hpl.jena.shared.InvalidPropertyURIException e){            		
            		log.debug("could not convert predicateUri into a valid URI",e);
            	}            	            	
            }                        
                        
            if( editConfig.getEntityToReturnTo() != null && editConfig.getEntityToReturnTo().startsWith("?") ){            	
            	resourceToRedirectTo = (String)request.getAttribute("entityToReturnTo");            
            }else{            
            	resourceToRedirectTo = editConfig.getEntityToReturnTo();
            }
            
        	//if there is no entity to return to it is likely a cancel
        	if( resourceToRedirectTo == null || resourceToRedirectTo.length() == 0 )
        		resourceToRedirectTo = editConfig.getSubjectUri();
            
        }
        
        //set up base URL
        String cancel = request.getParameter("cancel");
        String urlPatternToReturnTo = null;
        String urlPatternToCancelTo = null;
        if (editConfig != null) {
            urlPatternToReturnTo = editConfig.getUrlPatternToReturnTo();
            urlPatternToCancelTo = request.getParameter("url");
        }
        // If a different cancel return path has been designated, use it. Otherwise, use the regular return path.
        if (cancel != null && cancel.equals("true") && !StringUtils.isEmpty(urlPatternToCancelTo)) {
            urlPattern = urlPatternToCancelTo;
        }
        else if (!StringUtils.isEmpty(urlPatternToReturnTo)) {
        	urlPattern = urlPatternToReturnTo;       
        } else {
        	urlPattern = "/individual";       	
        }
        
        //looks like a redirect to a profile page, try to add anchor for property that was just edited.
        if( urlPattern.endsWith("individual") || urlPattern.endsWith("entity") ){        	
       		if( predicateLocalName != null && predicateLocalName.length() > 0){
       			predicateAnchor = "#" + predicateLocalName;
       			request.setAttribute("predicateAnchor", predicateAnchor);
       		}
        }
    }
    
    if( resourceToRedirectTo != null ){ %>   	
	    <c:url context="/" var="encodedUrl" value="<%=urlPattern%>">              
	       <c:param name="uri" value="<%=resourceToRedirectTo%>" />
	       <c:param name="extra" value="true"/>  <%--  For ie6 --%>
	    </c:url>
	    <c:redirect url="${encodedUrl}${predicateAnchor}" />                    
    <% } else { %>
        <c:redirect url="<%= Controllers.LOGIN %>" />
    <% } %>

<%!
Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.edit.postEditCleanUp.jsp");
%>



