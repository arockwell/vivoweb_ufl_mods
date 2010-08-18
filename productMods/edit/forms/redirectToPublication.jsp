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

<%-- 
This form will not allow an edit but will redirect to the publication.
This can be used to skip over an authorship context node.

What does this do on an add?
It shouldn't encounter an add, it will redirect to the subject.  Hide the add with in a policy.

What about the delete link?
The delete link will not go to this form.  You should hide the delete link with the policy.
--%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%! 
	public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.redirectToObject.jsp");
	public static String nodeToPubProp = "http://vivoweb.org/ontology/core#linkedInformationResource";
%>
<%	
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();    
               
    Individual subject = (Individual) request.getAttribute("subject");
	Individual obj = (Individual) request.getAttribute("object");
	
	if( obj == null ){
    	log.warn("this custom form is intended to redirect to the object but none was found.");
    	%>  
    	<jsp:forward page="/individual">
    		<jsp:param value="${subjectUri}" name="uri"/>
    	</jsp:forward>  
    	<%
    }else{
    	List<ObjectPropertyStatement> stmts =  obj.getObjectPropertyStatements( nodeToPubProp );
    	if( stmts == null || stmts.size() == 0 ){
    		%>  
        	<jsp:forward page="/individual">
        		<jsp:param value="${subjectUri}" name="uri"/>
        	</jsp:forward>  
        	<%	
    	} else {
    		ObjectPropertyStatement ops = stmts.get(0);
    		String pubUri = ops.getObjectURI();
    		if( pubUri != null ){
    			%>  
            	<jsp:forward page="/individual">
            		<jsp:param value="<%= pubUri %>" name="uri"/>
            	</jsp:forward>  
            	<%	
    		} else{
    			%>  
            	<jsp:forward page="/individual">
            		<jsp:param value="${subjectUri}" name="uri"/>
            	</jsp:forward>  
            	<%	}	
    		}
    	}
%>
  