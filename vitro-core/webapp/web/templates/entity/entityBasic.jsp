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

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Link" %>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ page import="java.util.List" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/StringProcessorTag" prefix="p" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/PropertyEditLink" prefix="edLnk" %>

<%@ page errorPage="/error.jsp"%>
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.templates.entity.entityBasic.jsp");
%>
<%
    log.debug("Starting entityBasic.jsp");
    Individual entity = (Individual)request.getAttribute("entity");
%>

<c:set var="labelUri" value="http://www.w3.org/2000/01/rdf-schema#label" />
<c:set var="typeUri" value="http://www.w3.org/1999/02/22-rdf-syntax-ns#type" />
<c:set var="vitroUri" value="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#" />

<c:if test="${!empty entityURI}">
    <c:set var="myEntityURI" scope="request" value="${entityURI}"/>
    <%
        try {
            VitroRequest vreq = new VitroRequest(request);
            entity = vreq.getWebappDaoFactory().getIndividualDao().getIndividualByURI((String)request.getAttribute("myEntityURI"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    %>
</c:if>

<% 

if (entity == null){
    String e="entityBasic.jsp expects that request attribute 'entity' be set to the Entity object to display.";
    throw new JspException(e);
}

if (VitroRequestPrep.isSelfEditing(request) || LoginFormBean.loggedIn(request, LoginFormBean.NON_EDITOR) /* minimum level*/) {
    request.setAttribute("showSelfEdits",Boolean.TRUE);
}%>
<c:if test="${sessionScope.loginHandler != null &&
              sessionScope.loginHandler.loginStatus == 'authenticated' &&
              sessionScope.loginHandler.loginRole >= LoginFormBean.NON_EDITOR}">
    <c:set var="showCuratorEdits" value="${true}"/>
</c:if>

<c:set var="showEdits" value="${showSelfEdits || showCuratorEdits}" scope="request"/>
<c:set var="editingClass" value="${showEdits ? 'editing' : ''}" scope="request"/>

<c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>
<%
    //anytime we are at an entity page we shouldn't have an editing config or submission
    session.removeAttribute("editjson");
    EditConfiguration.clearAllConfigsInSession(session);
    EditSubmission.clearAllEditSubmissionsInSession(session);
%>

<c:set var='entity' value='${requestScope.entity}'/><%/* just moving this into page scope for easy use */ %>
<c:set var='entityMergedPropsListJsp' value='/entityMergedPropList'/>
<c:set var='portal' value='${currentPortalId}'/>
<c:set var='portalBean' value='${currentPortal}'/>


<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>

    <div id="content">
      <div id="personWrap">
        <jsp:include page="entityAdmin.jsp"/> 
        
        <div class="contents entity ${editingClass}">

            <div id="labelAndMoniker">
                <c:choose>
                    <c:when test="${!empty relatedSubject}">
                        <h2><p:process>${relatingPredicate.domainPublic} for ${relatedSubject.name}</p:process></h2>
                        <c:url var="backToSubjectLink" value="/entity">
                            <c:param name="home" value="${portalBean.portalId}"/>
                            <c:param name="uri" value="${relatedSubject.URI}"/>
                        </c:url>
                        <p><a href="${backToSubjectLink}">&larr; return to ${relatedSubject.name}</a></p>
                    </c:when>
                    <c:otherwise>
                    
                        <%-- Label --%>
                        <div class="datatypePropertyValue" id="label">
                            <div class="statementWrap">
                               <h2><p:process>${entity.name}</p:process></h2> 
                               <c:if test="${showEdits}">
                                   <c:set var="editLinks"><edLnk:editLinks item="<%= VitroVocabulary.LABEL %>" data="${entity.name}" icons="false"/></c:set>
                                   <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}</span></c:if>                                    
                               </c:if> 
                            </div>
                        </div>

                        <%-- Moniker--%>                       
                        <c:if test="${!empty entity.moniker}">
                            <div class="datatypeProperties">
                                <div class="datatypePropertyValue" id="moniker">
                                    <div class="statementWrap">
                                        <p:process><em class="moniker">${entity.moniker}</em></p:process>                       
                                    </div>
                                </div>
                            </div>
                        </c:if>                             
                                  
                    </c:otherwise>
                </c:choose>
            </div> <!-- end labelAndMoniker -->
            
            <!-- START Sparkline Visualization -->
            <jsp:include page="sparklineVisualization.jsp"/>
            <!-- END Sparkline Visualization -->
            
            <%-- Thumbnail  --%>
            <c:set var="isPerson" value='<%= entity.isVClass("http://xmlns.com/foaf/0.1/Person") %>' />
            <c:set var="hasImage" value="${!empty entity.thumbUrl}" />
            <c:set var="imageLinks"><edLnk:editLinks item="<%= VitroVocabulary.IND_MAIN_IMAGE %>" icons="false" /></c:set>
            <c:choose>
                <c:when test="${!isPerson && !hasImage}">
                    <c:if test="${showEdits && !empty imageLinks}">
                        <div id="dprop-vitro-image" class="propsItem ${editingClass}"> 
	                        <h3 class="propertyName">image</h3>
                            ${imageLinks}
                        </div> 
                    </c:if>
                </c:when>
                <c:when test="${isPerson && !hasImage}">
                    <div id="dprop-vitro-image" class="propsItem ${editingClass}"> 
	                    <div class="datatypeProperties">
	                        <div class="datatypePropertyValue">
	                            <div class="statementWrap thumbnail">
                                    <img src="<c:url value='/images/dummyImages/person.thumbnail.jpg'/>" 
                                                title="no image" alt="" width="115"/>
                                    <c:if test="${showEdits}">
                                        <span class="editLinks">${imageLinks}</span>
                                    </c:if>                                   
	                            </div>
	                        </div>
	                    </div> 
                    </div> 
                </c:when>
                <c:otherwise> <%-- hasImage --%>
                    <div id="dprop-vitro-image" class="propsItem ${editingClass}"> 
	                    <div class="datatypeProperties">
	                        <div class="datatypePropertyValue">
	                            <div class="statementWrap thumbnail">
	                                <a class="image" href="${pageContext.request.contextPath}${entity.imageUrl}">
                                        <img src="${pageContext.request.contextPath}${entity.thumbUrl}" 
                                                title="click to view larger image" 
                                                alt="" width="115"/>
	                                </a>
                                    <c:if test="${showEdits}">
                                        <span class="editLinks">${imageLinks}</span>
                                    </c:if>                                   
	                            </div>
	                        </div>
	                    </div> 
                    </div> 
                    <jsp:include page="entityCitation.jsp" />
                </c:otherwise>
            </c:choose>
            
            <%-- Links --%>                                                                                       
            <c:if test="${ showEdits || !empty entity.url || !empty entity.linksList }"> 
                <div id="dprop-vitro-links" class="propsItem ${editingClass}">
	                <c:set var="canEditPrimaryLinks"><edLnk:editLinks item="<%= VitroVocabulary.PRIMARY_LINK %>" icons="false"/></c:set>
    		        <c:set var="canEditAdditionalLinks"><edLnk:editLinks item="<%= VitroVocabulary.ADDITIONAL_LINK %>" icons="false"/></c:set>
                    <c:if test="${showEdits and !empty canEditPrimaryLinks and !empty canEditAdditionalLinks}">
                        <h3 class="propertyName">web pages</h3>
                        <c:choose>
                            <c:when test="${empty entity.url}">
                                <c:set var="addUrlPredicate" value="<%= VitroVocabulary.PRIMARY_LINK %>" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="addUrlPredicate" value="<%= VitroVocabulary.ADDITIONAL_LINK %>" />
                            </c:otherwise>
                        </c:choose>
                        <edLnk:editLinks item="${addUrlPredicate}" icons="false" />
                    </c:if>
                    <c:if test="${!empty entity.anchor}"> 
                    <ul class="externalLinks properties">
                        <%-- Primary link --%>                             
                            <c:choose>
                                <c:when test="${!empty entity.url}">
                                    <c:url var="entityUrl" value="${entity.url}" />
                                    <li class="primary">
                                        <span class="statementWrap">
                                            <a class="externalLink" href="<c:out value="${entityUrl}"/>"><p:process>${entity.anchor}</p:process></a>
                                            <c:if test="${showEdits}">
                                                <em>(primary link)</em>
                                                <c:set var="editLinks"><edLnk:editLinks item="<%= VitroVocabulary.PRIMARY_LINK %>" data="${entity.primaryLink.URI}" icons="false"/></c:set>
                                                <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}</span></c:if>                                                                           
                                            </c:if>
                                        </span>
                                    </li>
                                </c:when>
                                <c:otherwise>
                                    <%--  RY For now, not providing editing links for anchor text with no url. Should fix. --%>
                                    <li class="primary"><span class="externalLink"><p:process>${entity.anchor}</p:process></span></li>
                                </c:otherwise>
                            </c:choose>
                        
                        
                        <%-- Additional links --%>
                        <c:if test="${!empty entity.linksList }">
                            <c:forEach items="${entity.linksList}" var='link' varStatus="count"> 
                                <c:url var="linkUrl" value="${link.url}" />
                                <c:choose>
                                    <c:when test="${empty entity.url && count.first==true}"><li class="first"></c:when>
                                    <c:otherwise><li></c:otherwise>
                                </c:choose>
                                <span class="statementWrap">
                                    <a class="externalLink" href="<c:out value="${linkUrl}"/>"><p:process>${link.anchor}</p:process></a>
                                    <c:if test="${showEdits}">
                                        <em>(additional link)</em>
                                        <c:set var="editLinks"><edLnk:editLinks item="<%= VitroVocabulary.ADDITIONAL_LINK %>" data="${link.URI}" icons="false"/></c:set>
                                        <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}</span></c:if>                                                                           
                                    </c:if>  
                                </span>
                                </li>                                          
                            </c:forEach>
                        </c:if>
                    </ul>
                    </c:if>                   
                </div> <!-- end dprop-vitro-links  -->
            </c:if>

            <%-- Description --%> 
            <%--               
            <c:if test="${ showEdits || !empty entity.description}">
            	<c:if test="${not empty entity.description }">
					<c:set var="editLinksForExistingDesc"><edLnk:editLinks item="<%= VitroVocabulary.DESCRIPTION %>" data="${entity.description}" icons="false"/></c:set>
            	</c:if>
            	<c:set var="editLinksForNewDesc"><edLnk:editLinks item="<%= VitroVocabulary.DESCRIPTION %>" icons="false"/></c:set>            	
            	<c:set var="mayEditDesc" value="${showEdits && ((empty entity.description and not empty editLinksForNewDesc) or (not empty entity.description and not empty editLinksForExisitngDesc))}"/>
	
				<c:if test="${mayEditDesc || ! empty entity.description }">
					<div id="dprop-vitro-description" class="propsItem ${editingClass}">
					<h3 class="propertyName">description</h3> ${editLinksForNewDesc}
				</c:if>

                <c:if test="${!empty entity.description}">
                    <div class="datatypeProperties">
                        <div class="datatypePropertyValue">
                            <div class="statementWrap">
                                <div class="description"><p:process>${entity.description}</p:process></div>                                 
                                <c:if test="${showEdits && !empty editLinksForExistingDesc}">	                                    
                                	<span class="editLinks">${editLinksForExistingDesc}</span>                                                                     
                                </c:if> 
                            </div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${mayEditDesc || ! empty entity.description }">     
            		</div>
            	</c:if>
            </c:if>
            --%>                
            
            <%-- Ontology properties --%>
            <c:import url="${entityMergedPropsListJsp}">
                <c:param name="mode" value="${showEdits ? 'edit' : ''}"/>
                <c:param name="grouped" value="true"/>
                <%-- unless a value is provided, properties not assigned to a group will not have a tab or appear on the page --%>
                <c:param name="unassignedPropsGroupName" value="other"/>
            </c:import>

            <%-- Blurb --%>                              
            <c:if test="${!empty entity.blurb}">
                 <div class="datatypeProperties">
                     <div class="datatypePropertyValue">
                         <div class="statementWrap">
                             <p:process><div class="description">${entity.blurb}</div></p:process>                        
                         </div>
                     </div>
                 </div>
            </c:if>   
                      
            <%-- Citation, if no thumbnail --%>
            <c:if test="${empty entity.thumbUrl}"> 
                <jsp:include page="entityCitation.jsp" />
            </c:if>
            
            <%-- Keywords --%>
            <c:if test="${!empty entity.keywordString}">
                <p:process><p id="keywords">Keywords: ${entity.keywordString}</p></p:process>
            </c:if>
                               
            ${requestScope.servletButtons}
        
        <!-- 
        	<c:if test="${not empty entityLinkedDataURL}">
        	  <c:url var="rdfImg" value="/images/edit_icons/rdf_w3c_icon48.gif"/>
        	  <a href="${entityLinkedDataURL}" title="get this as RDF/XML"><img src="${rdfImg}"/></a>
        	</c:if>    
        	 -->
        </div> <!--  contents -->
      </div> <!-- personWrap -->
    </div> <!-- content -->
    
<script type="text/javascript" src="<c:url value="/js/imageUpload/imageUploadUtils.js"/>"></script>