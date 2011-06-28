<?xml version="1.0" encoding="UTF-8"?>

<!--
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
-->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:c  ="http://java.sun.com/jstl/core"
          xmlns:fn ="http://java.sun.com/jsp/jstl/functions">

<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/>

<!--    
/**
 *
 * @version 0.8
 * @author bjl23
 *
 */
-->

<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
        <form action="showDataPropertyHierarchy" method="get">
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="submit" class="form-button" value="Data Property Hierarchy"/>
        </form>
		<form action="listDatatypeProperties" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="All Data Properties"/>
		</form>
	    <form action="listVClassWebapps" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="showPropertyRestrictions" value="true"/>
			<input type="hidden" name="propertyURI" value="${datatypeProperty.URI}"/>
			<input type="submit" class="form-button" value="Show Classes With a Restriction on This Property"/>
		</form>		
        <form action="listDataPropertyStatements" method="get">
        	<input type="hidden" name="home" value="${portalBean.portalId}" />
        	<input type="hidden" name="propertyURI" value="${datatypeProperty.URI}"/>
        	from <input type="text" name="startAt" value="1" size="2"/>
        	to <input type="text" name="endAt" value="50" size="3"/><br/>
        	<input type="submit" class="form-button" value="Show Examples of Statements Using This Property"/>
        </form>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="controller" value="Dataprop"/>
			<input type="hidden" name="uri" value="${datatypeProperty.URI}"/>
			<input type="submit" class="form-button" value="Edit this Data Property"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="hidden" name="controller" value="Dataprop"/>
			<input type="submit" class="form-button" value="Add New Data Property"/>
		</form>
		<form action="editForm" method="get">
            <input type="submit" class="form-button" value="Change URI"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="oldURI" value="${datatypeProperty.URI}"/>
            <input type="hidden" name="mode" value="renameResource"/>
            <input type="hidden" name="controller" value="Refactor"/>
        </form>
        <form action="editForm" method="get">
            <input type="submit" class="form-button" value="Move Statements to Different Property"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="propertyURI" value="${datatypeProperty.URI}"/>
            <input type="hidden" name="mode" value="movePropertyStatements"/>
            <input type="hidden" name="propertyType" value="DataProperty"/>
            <input type="hidden" name="controller" value="Refactor"/>
        </form>
	</td>
</tr>

<tr><td colspan="3"><hr/></td></tr>
<!-- _____________________________________________ superproperties __________________________________________ -->
<tr valign="bottom" align="center">
    <td colspan="2" valign="bottom" align="left">
       <c:if test="${!empty superproperties}">
        <form action="props2PropsOp" method="post">
            <ul style="list-style-type:none;">
            <c:forEach var="superproperty" items="${superproperties}">
            <c:url var="superpropertyURL" value="datapropEdit">
                <c:param name="home" value="${portalBean.portalId}"/>
                <c:param name="uri" value="${superproperty.URI}"/>
            </c:url>
                <li><input type="checkbox" name="SuperpropertyURI" value="${superproperty.URI}" class="form-item"/>
                    <a href="${superpropertyURL}">${superproperty.localNameWithPrefix}</a>
                </li>
            </c:forEach>
            </ul>
            <input type="hidden" name="SubpropertyURI" value="${datatypeProperty.URI}"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="operation" value="remove"/>
            <input type="hidden" name="_epoKey" value="${epoKey}"/> 
            <input type="submit" class="form-button" value="Remove Checked Superproperty Links"/>
        </form>
        </c:if>
    </td>
    <td>
        <form action="editForm" method="get">
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="SubpropertyURI" value="${datatypeProperty.URI}"/>
            <input type="hidden" name="opMode" value="superproperty"/>
            <input type="hidden" name="controller" value="Properties2Properties"/>
            <input type="hidden" name="propertyType" value="data"/>
            <input type="submit" class="form-button" value="New Link to Superproperty"/>
        </form>
    </td>
</tr>
<tr><td colspan="3"><hr/></td></tr>         
<!-- _______________________________________________ subproperties _____________________________________________ -->
<tr valign="bottom" align="center">
    <td colspan="2" valign="bottom" align="left">
        <c:if test="${!empty subproperties}">
        <form action="props2PropsOp" method="post">
            <ul style="list-style-type:none;">
            <c:forEach var="subproperty" items="${subproperties}">
                <c:url var="subpropertyURL" value="datapropEdit">
                    <c:param name="home" value="${portalBean.portalId}"/>
                    <c:param name="uri" value="${subproperty.URI}"/>
                </c:url>
                <li><input type="checkbox" name="SubpropertyURI" value="${subproperty.URI}" class="form-item"/>
                     <a href="${subpropertyURL}"> ${subproperty.localNameWithPrefix} </a>
                </li>                       
            </c:forEach>    
            </ul>
            <input type="hidden" name="SuperpropertyURI" value="${datatypeProperty.URI}"/>
            <input type="submit" class="form-button" value="Remove Checked Subproperty Links"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="_epoKey" value="${epoKey}"/>
            <input type="hidden" name="operation" value="remove"/>
        </form>
             </c:if>
    </td>
    <td valign="bottom">
        <form action="editForm" method="get">
            <input type="hidden" name="controller" value="Properties2Properties"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="SuperpropertyURI" value="${datatypeProperty.URI}"/>
            <input type="hidden" name="opMode" value="subproperty"/>
            <input type="hidden" name="propertyType" value="data"/>
            <input type="submit" class="form-button" value="New Link to Subproperty"/>
        </form>
    </td>
</tr>

<!-- _______________________________________________ equivalent properties _____________________________________________ -->
<tr valign="bottom" align="center">
    <td colspan="2" valign="bottom" align="left">
        <c:if test="${!empty equivalentProperties}">
        <form action="props2PropsOp" method="post">
            <ul style="list-style-type:none;">
            <c:forEach var="eqproperty" items="${equivalentProperties}">
                <c:url var="eqpropertyURL" value="datapropEdit">
                    <c:param name="home" value="${portalBean.portalId}"/>
                    <c:param name="uri" value="${eqproperty.URI}"/>
                </c:url>
                <li><input type="checkbox" name="SubpropertyURI" value="${eqproperty.URI}" class="form-item"/>
                     <a href="${eqpropertyURL}"> ${eqproperty.localNameWithPrefix} </a>
                </li>                       
            </c:forEach>    
            </ul>
            <input type="hidden" name="SuperpropertyURI" value="${datatypeProperty.URI}"/>
            <input type="submit" class="form-button" value="Remove Checked Equivalent Property Links"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="_epoKey" value="${epoKey}"/>
            <input type="hidden" name="operation" value="remove"/>
            <input type="hidden" name="opMode" value="equivalentProperty"/>
        </form>
             </c:if>
    </td>
    <td valign="bottom">
        <form action="editForm" method="get">
            <input type="hidden" name="controller" value="Properties2Properties"/>
            <input type="hidden" name="home" value="${portalBean.portalId}" />
            <input type="hidden" name="SuperpropertyURI" value="${datatypeProperty.URI}"/>
            <input type="hidden" name="opMode" value="equivalentProperty"/>
            <input type="hidden" name="propertyType" value="data"/>
            <input type="submit" class="form-button" value="New Link to Equivalent Property"/>
        </form>
    </td>
</tr>

</table>
</div>
</div>

</jsp:root>
