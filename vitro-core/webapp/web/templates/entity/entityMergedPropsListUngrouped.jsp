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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/StringProcessorTag" prefix="p" %>
<%@ taglib uri="http://vitro.mannlib.cornell.edu/vitro/tags/PropertyEditLink" prefix="edLnk" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Property" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.KeywordProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataProperty" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VClassDao" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginFormBean" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>

<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.HashSet" %>
<%@page import="java.util.LinkedList"%>
<%@page import="java.util.Set"%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>


<jsp:useBean id="loginHandler" class="edu.cornell.mannlib.vedit.beans.LoginFormBean" scope="session" />
<%! 
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.templates.entity.entityMergedPropsList.jsp");
%>
	<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>
<%  if( VitroRequestPrep.isSelfEditing(request) ) {
        log.debug("setting showSelfEdits true");%>
        <c:set var="showSelfEdits" value="${true}"/>
<%  }
    if (loginHandler!=null && loginHandler.getLoginStatus()=="authenticated" && Integer.parseInt(loginHandler.getLoginRole())>=loginHandler.getNonEditor()) {
	    log.debug("setting showCuratorEdits true");%>
	    <c:set var="showCuratorEdits" value="${true}"/>
<%  }%>
    <c:set var='entity' value='${requestScope.entity}'/><%-- just moving this into page scope for easy use --%>
    <c:set var='portal' value='${requestScope.portalBean}'/><%-- likewise --%>
    <c:set var="hiddenDivCount" value="0"/>
<%	Individual subject = (Individual) request.getAttribute("entity");
	if (subject==null) {
    	throw new Error("Subject individual must be in request scope for dashboardPropsList.jsp");
	}
	// Nick wants not to use explicit parameters to trigger visibility of a div, but for now we don't just want to always show the 1st one
	String openingGroupLocalName = (String) request.getParameter("curgroup");
    VitroRequest vreq = new VitroRequest(request);
    // added to permit distinguishing links outside the current portal
    int currentPortalId = -1;
    Portal currentPortal = vreq.getPortal();
    if (currentPortal!=null) {
    	currentPortalId = currentPortal.getPortalId();
    }

    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
	PropertyGroupDao pgDao = wdf.getPropertyGroupDao();
    VClassDao vcDao = wdf.getVClassDao();
    
    ArrayList<Property> propsList = (ArrayList) request.getAttribute("mergedList");
	for (Property p : propsList) {%>
 		<c:set var="stmtCounter" value="0"/>
<%		if (p instanceof ObjectProperty) {
 			ObjectProperty op = (ObjectProperty)p;%>
 			<c:set var="objProp" value="<%=op%>"/> 		
 			<c:set var="editableInSomeWay" value="${false}"/>
 			<c:if test="${showSelfEdits || showCuratorEdits}">
	    		<edLnk:editLinks item="${objProp}" var="links" />
	    		<c:if test="${!empty links}">
	    			<c:set var="editableInSomeWay" value="${true}"/>
	    		</c:if>                                                       
      		</c:if>
      		
	    	<c:set var="objStyle" value="display: block;"/>
	    	<c:set var="objRows" value="${fn:length(objProp.objectPropertyStatements)}"/>
	    	<c:if test="${objRows==0}"><c:set var="objStyle" value="display: block;"/></c:if>
	    	<c:if test="${editableInSomeWay || objRows>0}">
	    	    <c:set var="classForEditControls" value=""/>
    		    <c:if test="${showSelfEdits || showCuratorEdits}"><c:set var="classForEditControls" value=" editing"/></c:if>
                <c:set var="uniqueOpropDivName" value="${fn:replace(objProp.localNameWithPrefix,':','-')}"/>
				<div class="propsItem ${classForEditControls}" id="${'oprop-'}${uniqueOpropDivName}">
					<h3 class="propertyName">${objProp.editLabel}</h3>
		    		<c:if test="${showSelfEdits || showCuratorEdits}"><edLnk:editLinks item="${objProp}" icons="false" /></c:if>

  					<%-- Verbose property display additions for object properties, using context variable verbosePropertyListing --%>
                    <c:if test="${showCuratorEdits && verbosePropertyListing}">
                        <c:url var="propertyEditLink" value="/propertyEdit">
                            <c:param name="home" value="${portal.portalId}"/>
                            <c:param name="uri" value="${objProp.URI}"/>
                        </c:url>
                        <c:choose>
                            <c:when test="${!empty objProp.hiddenFromDisplayBelowRoleLevel.label}"><c:set var="displayCue" value="${objProp.hiddenFromDisplayBelowRoleLevel.label}"/></c:when>
                            <c:otherwise><c:set var="displayCue" value="unspecified"/></c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${!empty objProp.prohibitedFromUpdateBelowRoleLevel.label}"><c:set var="updateCue" value="${objProp.prohibitedFromUpdateBelowRoleLevel.label}"/></c:when>
                            <c:otherwise><c:set var="updateCue" value="unspecified"/></c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${!empty objProp.localNameWithPrefix}"><c:set var="localName" value="${objProp.localNameWithPrefix}"/></c:when>
                            <c:otherwise><c:set var="localName" value="no local name"/></c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${!empty objProp.domainDisplayTier}"><c:set var="displayTier" value="${objProp.domainDisplayTier}"/></c:when>
                            <c:otherwise><c:set var="displayTier" value="blank"/></c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${!empty objProp.groupURI}">
<%                              PropertyGroup pg = pgDao.getGroupByURI(op.getGroupURI());
                                if (pg!=null && pg.getName()!=null) {
                                    request.setAttribute("groupName",pg.getName());%>
                                    <span class="verbosePropertyListing"><a class="propertyLink" href="${propertyEditLink}"/>${localName}</a> (object property); display tier ${displayTier} within group ${groupName}; display level: ${displayCue}; update level: ${updateCue}</span>
<%                              } else {%>
                                    <span class="verbosePropertyListing"><a class="propertyLink" href="${propertyEditLink}"/>${localName}</a> (object property); display tier ${displayTier}; display level: ${displayCue}; update level: ${updateCue}</span>
<%                              } %>
                            </c:when>
                            <c:otherwise>
                                <span class="verbosePropertyListing"><a class="propertyLink" href="${propertyEditLink}"/>${localName}</a> (object property); display tier ${displayTier}; display level: ${displayCue}; update level: ${updateCue}</span>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
  					<%-- end Verbose property display additions for object properties --%>
  					
  					<c:set var="displayLimit" value="${objProp.domainDisplayLimit}"/>
  					<c:if test="${displayLimit<0}">
  					    <c:set var="displayLimit" value="32"/> <% /* arbitrary limit if value is unset, i.e. -1 */ %>
  					</c:if>
					<c:if test="${fn:length(objProp.objectPropertyStatements)-displayLimit==1}"><c:set var="displayLimit" value="${displayLimit+1}"/></c:if>
					<c:if test="${objRows>0}">
      					<ul class='properties'>
  					</c:if>
					<c:set var="collateByClass" value="<%=op.getCollateBySubclass()%>"/>
					<c:if test="${collateByClass }" >
						<c:set var="collateClassesShownCount" value="0"/>
						<c:set var="collateCurrentClass" value="_none"/>				
					</c:if>
					<c:forEach items="${objProp.objectPropertyStatements}" var="objPropertyStmt">															
						<c:if test="${ collateByClass && collateCurrentClass!=objPropertyStmt.object.VClassURI}">						   
		            		<c:if test="${ collateClassesShownCount > 0 }">
		            			</ul></li><!-- collateClasses -->
		            		</c:if>
		            		<c:set var="collateCurrentClass" value="${objPropertyStmt.object.VClassURI}" />
		            		<c:set var="collateCurrentClassName" value="${objPropertyStmt.object.VClass.name}" />
		            		<c:set var="collateClassesShownCount" value="${collateClassesShown + 1}"/>		            		
		            		<li>
		            		<h5 class="collate">${collateCurrentClassName}</h5>
		            		<ul class='properties'><!-- collateClasses -->
		            	</c:if>

						<c:if test="${stmtCounter == displayLimit}"><!-- set up toggle div and expandable continuation div -->
							<c:if test="${ collateByClass }"> </ul></li></c:if>
							<c:if test="${ ! collateByClass }"> </ul></c:if>  							
  		                	<c:set var="hiddenDivCount" value="${hiddenDivCount+1}"/>
							<c:url var="themePath" value="/${themeDir}site_icons" />
									
			               <div class="navlinkblock ">
			                 <span class="entityMoreSpan">
			                   <c:out value='${objRows - stmtCounter}' />
			                   <c:choose>
			                       <c:when test='${displayLimit==0}'> entries</c:when>
			                       <c:otherwise> more</c:otherwise>
			                   </c:choose>
			                 </span>
			               
			                 <div class="extraEntities">
			                 <c:if test="${ collateByClass }"> <li></c:if>
							 <ul class="properties">
			              		 
						</c:if>
     					<li>
	     					<span class="statementWrap">
	     					<c:set var="opStmt" value="${objPropertyStmt}" scope="request"/>
	           				<c:url var="propertyLink" value="/entity">
	               				<c:param name="home" value="${portal.portalId}"/>
	               				<c:param name="uri" value="${objPropertyStmt.object.URI}"/>               			
	           				</c:url>
							<%
							   String customShortView = MiscWebUtils.getCustomShortView(request); 
							%>
	         				<c:set var="altRenderJsp" value="<%= customShortView %>" />
	         				<c:remove var="opStmt" scope="request"/>
	            			<c:choose>
					            <c:when test="${!empty altRenderJsp}">
									<c:set scope="request" var="individual" value="${objPropertyStmt.object}"/>
									<c:set scope="request" var="predicateUri" value="${objProp.URI}"/>
									<jsp:include page="${altRenderJsp}" flush="true"/>
					            	<c:remove var="altRenderJsp"/>
					    		</c:when>
					            <c:otherwise>
					            	<a class="propertyLink" href='<c:out value="${propertyLink}"/>'><p:process><c:out value="${objPropertyStmt.object.name}"/></p:process></a>
					            	<c:if test="${!empty objPropertyStmt.object.moniker}">
					                    <p:process><c:out value="| ${objPropertyStmt.object.moniker}"/></p:process>
					                </c:if>
								</c:otherwise>
		            		</c:choose>
		            		<c:if test="${showSelfEdits || showCuratorEdits}">
	         					  <c:set var="editLinks"><edLnk:editLinks item="${objPropertyStmt}" icons="false"/></c:set>
	         					  <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}&nbsp;</span></c:if>
	         					  <c:if test="${empty editLinks}"><em class="nonEditable">(non-editable) </em></c:if>
	         					</c:if>
	      					</span>
      					</li>
						<c:set var="stmtCounter" value="${stmtCounter+1}"/>
					</c:forEach>
					<c:if test="${objRows > 0}"></ul></c:if>
					<c:if test="${ collateClassesShownCount > 0 }"></li><!-- collateClasses 2 --></c:if>
					<c:if test="${ collateByClass && collateClassesShownCount > 0 }"></ul><!-- collate end --></c:if>										
   					<c:if test="${ stmtCounter > displayLimit}">
   					</div><%-- navlinkblock --%>
   					</div><%-- extraEntities --%></c:if>
 				</div><!-- ${objProp.localNameWithPrefix} -->
 			</c:if>
<%		} else if (p instanceof DataProperty) {
  			DataProperty dp = (DataProperty)p;%>  			
  			<c:set var="dataProp" value="<%=dp%>"/>
 			<c:set var="dataRows" value="${fn:length(dataProp.dataPropertyStatements)}"/>
 						 		
 			<c:set var="hasRowsToShow" value="${ dataRows > 0 }"/> 			 			 			
 			<c:if test="${showSelfEdits || showCuratorEdits}"><c:remove var="mayAddDataprop"/><edLnk:editLinks var="mayAddDataprop" item="${dataProp}" icons="false"/></c:if>
 			<c:set var="mayEdit" value="${ !empty mayAddDataprop }"/> 			 			
 			<c:if test="${ hasRowsToShow or mayEdit }">
			    <c:set var="dataStyle" value="display: block;"/>
			    <c:if test="${dataRows==0}"><c:set var="dataStyle" value="display: block;"/></c:if>		    
				<c:set var="classForEditControls" value=""/>
			    <c:if test="${showSelfEdits || showCuratorEdits}"><c:set var="classForEditControls" value=" editing"/></c:if>
	            <c:set var="uniqueDpropDivName" value="${fn:replace(dataProp.localNameWithPrefix,':','-')}"/>            
	 			<div id="${'dprop-'}${uniqueDpropDivName}" class="propsItem ${classForEditControls}" style="${dataStyle}">
					<h3 class="propertyName">${dataProp.editLabel}</h3>
					<c:if test="${showSelfEdits || showCuratorEdits}"><edLnk:editLinks item="${dataProp}" icons="false"/></c:if> 					
			    	<%-- Verbose property display additions for data properties, using context variable verbosePropertyListing --%>
	                <c:if test="${showCuratorEdits && verbosePropertyListing}">
	                    <c:url var="propertyEditLink" value="/datapropEdit">
	                        <c:param name="home" value="${portal.portalId}"/>
	                        <c:param name="uri" value="${dataProp.URI}"/>
	                    </c:url>
	                    <c:choose>
	                        <c:when test="${!empty dataProp.hiddenFromDisplayBelowRoleLevel.label}"><c:set var="displayCue" value="${dataProp.hiddenFromDisplayBelowRoleLevel.label}"/></c:when>
	                        <c:otherwise><c:set var="displayCue" value="unspecified"/></c:otherwise>
	                    </c:choose>
	                    <c:choose>
	                        <c:when test="${!empty dataProp.prohibitedFromUpdateBelowRoleLevel.label}"><c:set var="updateCue" value="${dataProp.prohibitedFromUpdateBelowRoleLevel.label}"/></c:when>
	                        <c:otherwise><c:set var="updateCue" value="unspecified"/></c:otherwise>
	                    </c:choose>
	                    <c:choose>
	                        <c:when test="${!empty dataProp.localNameWithPrefix}"><c:set var="localName" value="${dataProp.localNameWithPrefix}"/></c:when>
	                        <c:otherwise><c:set var="localName" value="no local name"/></c:otherwise>
	                    </c:choose>
	                    <c:choose>
	                        <c:when test="${!empty dataProp.displayTier}"><c:set var="displayTier" value="${dataProp.displayTier}"/></c:when>
	                        <c:otherwise><c:set var="displayTier" value="blank"/></c:otherwise>
	                    </c:choose>
	                    <c:choose>
	                        <c:when test="${!empty dataProp.groupURI}">
	<%                          PropertyGroup pg = pgDao.getGroupByURI(dp.getGroupURI());
	                            if (pg!=null && pg.getName()!=null) {
	                                request.setAttribute("groupName",pg.getName());%>
	                                <span class="verbosePropertyListing"><a class="propertyLink" href="${propertyEditLink}"/>${localName}</a> (data property); display tier ${displayTier} within group ${groupName}; display level: ${displayCue}; update level: ${updateCue}</span>
	<%                          } else {%>
	                                <span class="verbosePropertyListing"><a class="propertyLink" href="${propertyEditLink}"/>${localName}</a> (data property); display tier ${displayTier}; display level: ${displayCue}; update level: ${updateCue}</span>
	<%                          } %>
	                        </c:when>
	                        <c:otherwise>
	                            <span class="verbosePropertyListing"><a class="propertyLink" href="${propertyEditLink}"/>${localName}</a> (data property); display tier ${displayTier}; display level: ${displayCue}; update level: ${updateCue}</span>
	                        </c:otherwise>
	                    </c:choose>
	                </c:if>
	                <%-- end Verbose property display additions for data properties --%>
			    	
					<c:set var="displayLimit" value="${dataProp.displayLimit}"/>
					<c:if test="${displayLimit<0}">
					    <c:set var="displayLimit" value="32"/> <% /* arbitrary limit if value is unset, i.e. -1 */ %>
					</c:if>
					<c:if test="${fn:length(dataProp.dataPropertyStatements)-displayLimit==1}"><c:set var="displayLimit" value="${displayLimit+1}"/></c:if>
					<c:if test="${displayLimit < 0}"><c:set var="displayLimit" value="20"/></c:if>
					<c:if test="${!empty dataProp.dataPropertyStatements}"><div class="datatypeProperties"></c:if>
	 			    	<c:if test="${dataRows > 1}">
							<ul class="datatypePropertyValue">
						</c:if>
						<c:if test="${dataRows == 1}">
							<div class="datatypePropertyValue"><span class="statementWrap">
						</c:if>
						<c:forEach items="${dataProp.dataPropertyStatements}" var="dataPropertyStmt">
							<c:if test="${stmtCounter == displayLimit}">
	  							<c:if test="${dataRows > 1 && displayLimit < 0}"></ul></c:if>
	     
	                       <div class="navlinkblock ">
	                         <span class="entityMoreSpan">
	                           <c:out value='${dataRows - stmtCounter}' />
	                           <c:choose>
	                               <c:when test='${displayLimit==0}'> entries</c:when>
	                               <c:otherwise> more</c:otherwise>
	                           </c:choose>
	                         </span>
	
	                      <div class="extraEntities">
							</c:if>
			            	<c:set var="stmtCounter" value="${stmtCounter+1}"/>
			            	<c:choose>
			                	<c:when test='${dataRows==1}'>
	  		                	<p:process>${dataPropertyStmt.data}</p:process>
			                	    <c:if test="${showSelfEdits || showCuratorEdits}">
	               					    <c:set var="editLinks"><edLnk:editLinks item="${dataPropertyStmt}" icons="false"/></c:set>
	  		                	    <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}&nbsp;</span></c:if>
	  		                	    <c:if test="${empty editLinks}"><em class="nonEditable">(non-editable) </em></c:if>
			                	    </c:if>
			                	</c:when>
			                	<c:otherwise>
			                	    <li><span class="statementWrap">
			                	    <p:process>${dataPropertyStmt.data}</p:process>
			                	    <c:if test="${showSelfEdits || showCuratorEdits}">
			                	      <c:set var="editLinks"><edLnk:editLinks item="${dataPropertyStmt}" icons="false"/></c:set>
	  		                	    <c:if test="${!empty editLinks}"><span class="editLinks">${editLinks}&nbsp;</span></c:if>
	  		                	    <c:if test="${empty editLinks}"><em class="nonEditable">(non-editable) </em></c:if>
			                	    </c:if>
			                	    </span></li>
			                	</c:otherwise>
			            	</c:choose>
			            	
	       					<c:if test="${dataRows==1}"></span></div></c:if>
						</c:forEach>
						<c:if test="${stmtCounter > displayLimit}"></div></div></c:if>
	                <c:if test="${!empty dataProp.dataPropertyStatements}"></div><!-- datatypeProperties --></c:if>
				</div><!-- ${dataProp.localNameWithPrefix} -->	
			</c:if>	
<%		} else { // keyword property -- ignore
		    if (p instanceof KeywordProperty) {%>
				<p>Not expecting keyword properties here.</p>
<%			} else {
  				log.warn("unexpected unknown property type found");%>
   				<p>Unknown property type found</p>
<%			}
		} 
   } // end for (Property p : g.getPropertyList()
%>
