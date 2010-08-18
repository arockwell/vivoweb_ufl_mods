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

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.flags.PortalFlagChoices" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page errorPage="/error.jsp"%>
<%  /***********************************************
          Displays the little group of things at the bottom of the page
          for administrators and editors.
         
         request.attributes:
         an Entity object with the name "entity" 
         
         
         request.parameters:
         None, should only work with requestScope attributes for security reasons.
         
          Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
          for debugging info.
                 
         bdc34 2006-01-22 created        
        **********************************************/ 
        Individual entity=(Individual)request.getAttribute("entity");                         
        if (entity == null){
            String e="entityAdmin.jsp expects that request attribute 'entity' be set to the Entity object to display.";
            throw new JspException(e);
        }
%>
<c:if test="${sessionScope.loginHandler != null &&
              sessionScope.loginHandler.loginStatus == 'authenticated' &&
              sessionScope.loginHandler.loginRole >= sessionScope.loginHandler.editor }">
    <c:set var='entity' value='${requestScope.entity}'/><%/* just moving this into page scope for easy use */ %>
    <c:set var='portal' value='${requestScope.portal}'/> 
    <div class='admin top'>
        <h3 class="toggle">Admin Panel</h3>
        <div class="panelContents">
            <c:url var="editHref" value="/entityEdit">
      		    <c:param name="home" value="${currentPortalId}"/>
              <c:param name="uri" value="${entity.URI}"/>
            </c:url>
          <a href='<c:out value="${editHref}"/>'> edit this individual</a> 
          <p>Resource URI: <c:out value="${entity.URI}"/></p>
          </div>
    </div>

</c:if>
