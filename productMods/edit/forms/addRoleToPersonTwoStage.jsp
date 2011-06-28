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

<%-- 
  Custom two stage form for adding a Role to a Person.  
  
  Stage one is selecting the type of the non-person thing 
  associated with the Role with the intention of reducing the 
  number of Individuals that the user has to select from.
  Stage two is selecting the non-person Individual to associate
  with the Role. 

  This is intended to create a set of statements like:

  ?person  core:hasResearchActivityRole ?newRole.
  ?newRole rdf:type core:ResearchActivityRole ;         
           core:relatedRole ?someActivity .
  ?someActivity rdf:type core:ResearchActivity .
  ?someActivity rdfs:label "activity title" .
  
  Important: This form cannot be directly used as a custom form.  It has parameters that must be set.
  See addClinicalRoleToPerson.jsp for an example.
  
--%>           

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="com.hp.hpl.jena.rdf.model.Model" %>
<%@ page import="com.hp.hpl.jena.vocabulary.XSD" %>

<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONArray" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.JavaScript" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Css" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.TitleCase" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.StartYearBeforeEndYear"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode"%>

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.StartDateBeforeEndDate"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.DateTimeIntervalValidation"%><c:set var="vivoOnt" value="http://vivoweb.org/ontology" />

<%!
public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.addRoleToPersonTwoStage.jsp");
%>

<c:set var="vivoCore" value="${vivoOnt}/core#" />

<%--
  These are the parameters that MUST be set of this form:
   role type
   predicate inverse          
   role activity type label (should be singular)
   super type of role types for roleActivityType select list generation 
--%>

<c:set var="roleActivityTypeLabel">${param.roleActivityTypeLabel}</c:set>
<c:set var="buttonLabel" scope="request">${! empty param.buttonLabel ? param.buttonLabel : param.roleActivityTypeLabel}</c:set>
<c:set var="roleType">${param.roleType}</c:set>
<c:set var="roleActivityType_optionsType" >${param.roleActivityType_optionsType}</c:set>
<c:set var="roleActivityType_objectClassUri" >${param.roleActivityType_objectClassUri}</c:set> 
<c:set var="roleActivityType_literalOptions" >${param.roleActivityType_literalOptions}</c:set>
<c:set var="numDateFields">${! empty param.numDateFields ? param.numDateFields : 2 }</c:set>

<%
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();    
    //vreq.setAttribute("defaultNamespace", ""); //empty string triggers default new URI behavior
    
    String subjectUri = vreq.getParameter("subjectUri");
    String predicateUri = vreq.getParameter("predicateUri");    
    String subjectName = ((Individual) request.getAttribute("subject")).getName();
    vreq.setAttribute("subjectUriJson", MiscWebUtils.escape(subjectUri));
    
    vreq.setAttribute("stringDatatypeUriJson", MiscWebUtils.escape(XSD.xstring.toString()));
    
    String intDatatypeUri = XSD.xint.toString();    
    vreq.setAttribute("intDatatypeUri", intDatatypeUri);
    vreq.setAttribute("intDatatypeUriJson", MiscWebUtils.escape(intDatatypeUri));

    vreq.setAttribute("gYearDatatypeUriJson", MiscWebUtils.escape(XSD.gYear.toString()));
    
    vreq.setAttribute("roleActivityTitleCase", TitleCase.toTitleCase(vreq.getParameter("roleActivityTypeLabel")));
    String buttonLabel = (String) vreq.getAttribute("buttonLabel");
    vreq.setAttribute("buttonLabel", TitleCase.toTitleCase(buttonLabel));
    
    ObjectProperty op = wdf.getObjectPropertyDao().getObjectPropertyByURI( predicateUri ); 
    if( op != null &&  op.getURIInverse() != null ){
		%> <c:set var="inversePredicate"><%=op.getURIInverse()%></c:set> <%
    }else{
    	%> <c:set var="inversePredicate"></c:set> <%
    }
    
/* 
 There are 4 modes that this form can be in: 
  1.  Add, there is a subject and a predicate but no role and nothing else. 
        
  2. normal edit where everything should already be filled out.  There is a subject, a object and an individual on
     the other end of the object's core:roleIn stmt. 
  
  3. Repair a bad role node.  There is a subject, prediate and object but there is no individual on the 
     other end of the object's core:roleIn stmt.  This should be similar to an add but the form should be expanded.
     
  4. Really bad node. multiple core:roleIn statements.
*/

 EditMode mode = FrontEndEditingUtils.getEditMode(request, "http://vivoweb.org/ontology/core#roleIn");

 if( mode == EditMode.ADD ) {
    %> <c:set var="editMode" value="add"/><%
 } else if(mode == EditMode.EDIT){
     %> <c:set var="editMode" value="edit"/><%
 } else if(mode == EditMode.REPAIR){
     %> <c:set var="editMode" value="repair"/><%
 }
%>

<c:set var="vivoOnt" value="http://vivoweb.org/ontology" />
<c:set var="vivoCore" value="${vivoOnt}/core#" />
<c:set var="rdfs" value="<%= VitroVocabulary.RDFS %>" />
<c:set var="type" value="<%= VitroVocabulary.RDF_TYPE %>" />
<c:set var="label" value="${rdfs}label" />
<c:set var="defaultNamespace" value=""/> <%--blank triggers default URI generation behavior --%>

<c:set var="startYearPred" value="${vivoCore}startYear" />
<c:set var="endYearPred" value="${vivoCore}endYear" />
<c:set var="dateTimeValueType" value="${vivoCore}DateTimeValue"/>
<c:set var="dateTimePrecision" value="${vivoCore}dateTimePrecision"/>
<c:set var="dateTimeValue" value="${vivoCore}dateTime"/>

<c:set var="roleToInterval" value="${vivoCore}dateTimeInterval"/>
<c:set var="intervalType" value="${vivoCore}DateTimeInterval"/>
<c:set var="intervalToStart" value="${vivoCore}start"/>
<c:set var="intervalToEnd" value="${vivoCore}end"/>

<%-- label and type required if we are doing an add or a repair, but not an edit --%> 
<c:set var="labelRequired" ><%= (mode == EditMode.ADD || mode == EditMode.REPAIR) ?"\"nonempty\"," : "" %></c:set>
<c:set var="typeRequired" ><%= (mode == EditMode.ADD || mode == EditMode.REPAIR) ?"\"nonempty\"" : "" %></c:set>

<v:jsonset var="roleLabelAssertion" >
    ?role <${label}> ?roleLabel .
</v:jsonset>

<v:jsonset var="n3ForNewRole">
	@prefix core: <${vivoCore}> .
       
	?person ?rolePredicate ?role.	
	?role   a <${roleType}> .		  
    ?role   core:roleIn ?roleActivity .    
    ?roleActivity  core:relatedRole ?role .    
</v:jsonset>

<v:jsonset var="n3ForActivityType">     
    ?roleActivity a ?roleActivityType .
</v:jsonset>

<v:jsonset var="n3ForRoleToActivity"> 
	@prefix core: <${vivoCore}> .    
    ?role core:roleIn ?roleActivity .
    ?roleActivity  core:relatedRole ?role .   
</v:jsonset>

<v:jsonset var="n3ForActivityLabel">
    ?roleActivity <${label}> ?activityLabel .
</v:jsonset>

<v:jsonset var="n3ForInverse"> 
	?role  ?inverseRolePredicate ?person.
</v:jsonset>

<v:jsonset var="n3ForStart">
    ?role      <${roleToInterval}> ?intervalNode .    
    ?intervalNode  <${type}> <${intervalType}> .
    ?intervalNode <${intervalToStart}> ?startNode .    
    ?startNode  <${type}> <${dateTimeValueType}> .
    ?startNode  <${dateTimeValue}> ?startField.value .
    ?startNode  <${dateTimePrecision}> ?startField.precision .
</v:jsonset>

<v:jsonset var="n3ForEnd">
    ?role      <${roleToInterval}> ?intervalNode .    
    ?intervalNode  <${type}> <${intervalType}> .
    ?intervalNode <${intervalToEnd}> ?endNode .
    ?endNode  <${type}> <${dateTimeValueType}> .
    ?endNode  <${dateTimeValue}> ?endField.value .
    ?endNode  <${dateTimePrecision}> ?endField.precision .
</v:jsonset>

<v:jsonset var="activityLabelQuery">
  PREFIX core: <${vivoCore}>
  PREFIX rdfs: <${rdfs}> 
  SELECT ?existingTitle WHERE {
        ?role  core:roleIn ?existingActivity .
        ?existingActivity rdfs:label ?existingTitle . }
</v:jsonset>

<v:jsonset var="activityQuery">
  PREFIX core: <${vivoCore}>  
  SELECT ?existingActivity WHERE { ?role  core:roleIn ?existingActivity . }
</v:jsonset>

<v:jsonset var="roleLabelQuery">
  SELECT ?existingRoleLabel WHERE { ?role  <${label}> ?existingRoleLabel . }
</v:jsonset>

<%-- 
<v:jsonset var="activityTypeQuery">
        PREFIX core: <${vivoCore}>
        SELECT ?existingActivityType WHERE {
            ?role core:roleIn ?existingActivity . 
            ?existingActivity a ?existingActivityType . 
        }    
</v:jsonset>
--%>
<% 
request.setAttribute("typeQuery", getActivityTypeQuery(vreq));
%>
<v:jsonset var="activityTypeQuery">${typeQuery}</v:jsonset>

 <v:jsonset var="existingIntervalNodeQuery" >  
    SELECT ?existingIntervalNode WHERE {
          ?role <${roleToInterval}> ?existingIntervalNode .
          ?existingIntervalNode <${type}> <${intervalType}> . }
</v:jsonset>
 
 <v:jsonset var="existingStartNodeQuery" >  
    SELECT ?existingStartNode WHERE {
      ?role <${roleToInterval}> ?intervalNode .
      ?intervalNode <${type}> <${intervalType}> .
      ?intervalNode <${intervalToStart}> ?existingStartNode . 
      ?existingStartNode <${type}> <${dateTimeValueType}> .}              
</v:jsonset>

<v:jsonset var="existingStartDateQuery" >  
    SELECT ?existingDateStart WHERE {
     ?role <${roleToInterval}> ?intervalNode .
     ?intervalNode <${type}> <${intervalType}> .
     ?intervalNode <${intervalToStart}> ?startNode .
     ?startNode <${type}> <${dateTimeValueType}> .
     ?startNode <${dateTimeValue}> ?existingDateStart . }
</v:jsonset>

<v:jsonset var="existingStartPrecisionQuery" >  
    SELECT ?existingStartPrecision WHERE {
      ?role <${roleToInterval}> ?intervalNode .
      ?intervalNode <${type}> <${intervalType}> .
      ?intervalNode <${intervalToStart}> ?startNode .
      ?startNode <${type}> <${dateTimeValueType}> .          
      ?startNode <${dateTimePrecision}> ?existingStartPrecision . }
</v:jsonset>

 <v:jsonset var="existingEndNodeQuery" >  
    SELECT ?existingEndNode WHERE {
      ?role <${roleToInterval}> ?intervalNode .
      ?intervalNode <${type}> <${intervalType}> .
      ?intervalNode <${intervalToEnd}> ?existingEndNode . 
      ?existingEndNode <${type}> <${dateTimeValueType}> .}              
</v:jsonset>

<v:jsonset var="existingEndDateQuery" >  
    SELECT ?existingEndDate WHERE {
     ?role <${roleToInterval}> ?intervalNode .
     ?intervalNode <${type}> <${intervalType}> .
     ?intervalNode <${intervalToEnd}> ?endNode .
     ?endNode <${type}> <${dateTimeValueType}> .
     ?endNode <${dateTimeValue}> ?existingEndDate . }
</v:jsonset>

<v:jsonset var="existingEndPrecisionQuery" >  
    SELECT ?existingEndPrecision WHERE {
      ?role <${roleToInterval}> ?intervalNode .
      ?intervalNode <${type}> <${intervalType}> .
      ?intervalNode <${intervalToEnd}> ?endNode .
      ?endNode <${type}> <${dateTimeValueType}> .          
      ?endNode <${dateTimePrecision}> ?existingEndPrecision . }
</v:jsonset>

<c:set var="editjson" scope="request">
{
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/individual",

    "subject"   : ["person", "${subjectUriJson}" ],
    "predicate" : ["rolePredicate", "${predicateUriJson}" ],
    "object"    : ["role", "${objectUriJson}", "URI" ],
    
    "n3required"    : [ "${n3ForNewRole}", "${roleLabelAssertion}" ],        
    "n3optional"    : [ "${n3ForActivityLabel}", "${n3ForActivityType}", "${n3ForInverse}", "${n3ForStart}", "${n3ForEnd}" ],        
                                                                                        
    "newResources"  : { "role" : "${defaultNamespace}",
                        "roleActivity" : "${defaultNamespace}",
                        "intervalNode" : "${defaultNamespace}",
                        "startNode" : "${defaultNamespace}",
                        "endNode" : "${defaultNamespace}" },

    "urisInScope"    : { "inverseRolePredicate" : "${inversePredicate}" },
    "literalsInScope": { },
    "urisOnForm"     : [ "roleActivity", "roleActivityType" ],
    "literalsOnForm" : [ "activityLabel", "roleLabel" ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : { 
        "activityLabel":"${activityLabelQuery}",
        "roleLabel":"${roleLabelQuery}",
        "startField.value"   : "${existingStartDateQuery}",
        "endField.value"     : "${existingEndDateQuery}" 
    },               
    "sparqlForExistingUris" : { 
        "roleActivity":"${activityQuery}" , 
        "roleActivityType":"${activityTypeQuery}" ,
        "intervalNode"      : "${existingIntervalNodeQuery}", 
        "startNode"         : "${existingStartNodeQuery}",
        "endNode"           : "${existingEndNodeQuery}",
        "startField.precision": "${existingStartPrecisionQuery}",
        "endField.precision"  : "${existingEndPrecisionQuery}"
    },
    "fields" : {
      "activityLabel" : {
         "newResource"      : "false",
         "validators"       : [ ${labelRequired} "datatype:${stringDatatypeUriJson}" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${stringDatatypeUriJson}",
         "rangeLang"        : "",
         "assertions"       : ["${n3ForActivityLabel}" ]
      },   
      "roleActivityType" : {
         "newResource"      : "true",
         "validators"       : [ ${typeRequired} ],
         "optionsType"      : "${roleActivityType_optionsType}",
         "literalOptions"   : [ ${roleActivityType_literalOptions } ],
         "predicateUri"     : "",
         "objectClassUri"   : "${roleActivityType_objectClassUri}",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",
         "assertions"       : ["${n3ForActivityType}" ]
      },               
      "roleActivity" : {
         "newResource"      : "true",
         "validators"       : [ ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",         
         "assertions"       : [ "${n3ForRoleToActivity}" ]
      },
      "roleLabel" : {
         "newResource"      : "false",
         "validators"       : [ "nonempty","datatype:${stringDatatypeUriJson}" ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${stringDatatypeUriJson}",
         "rangeLang"        : "",
         "assertions"       : ["${roleLabelAssertion}" ]
      },
      "startField" : {
         "newResource"      : "false",
         "validators"       : [ ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",         
         "assertions"       : [ "${n3ForStart}" ]
      },
      "endField" : {
         "newResource"      : "false",
         "validators"       : [ ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",         
         "assertions"       : ["${n3ForEnd}" ]
      }
  }
}
</c:set>
   
<%   
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        editConfig = new EditConfiguration((String) request.getAttribute("editjson"));     
        EditConfiguration.putConfigInSession(editConfig,session);
        
      //setup date time edit elements
        Field startField = editConfig.getField("startField");
        startField.setEditElement(
                new DateTimeWithPrecision(startField, 
                        VitroVocabulary.Precision.YEAR.uri(),
                        VitroVocabulary.Precision.NONE.uri()));        
        Field endField = editConfig.getField("endField");
        endField.setEditElement(
                new DateTimeWithPrecision(endField, 
                        VitroVocabulary.Precision.YEAR.uri(),
                        VitroVocabulary.Precision.NONE.uri()));
    }
    
    editConfig.addValidator(new DateTimeIntervalValidation("startField","endField") ); 

    Model model = (Model) application.getAttribute("jenaOntModel");
    String objectUri = (String) request.getAttribute("objectUri");
    if (objectUri != null) { 
        editConfig.prepareForObjPropUpdate(model);
        // Return browser to person individual after editing an existing role.
    } else { 
        editConfig.prepareForNonUpdate(model);
        // NIHVIVO-1014 Return browser to person individual after editing an existing role.
        // Return the browser to the new activity entity after adding a new role.
        // editConfig.setEntityToReturnTo("?roleActivity");
    }        

    List<String> customJs = new ArrayList<String>(Arrays.asList(JavaScript.JQUERY_UI.path(),
                                                                JavaScript.CUSTOM_FORM_UTILS.path(),
                                                                "/edit/forms/js/customFormWithAutocomplete.js"                                                    
                                                               ));            
    request.setAttribute("customJs", customJs);
    
    List<String> customCss = new ArrayList<String>(Arrays.asList(Css.JQUERY_UI.path(),
                                                                 Css.CUSTOM_FORM.path(),
                                                                 "/edit/forms/css/customFormWithAutocomplete.css"
                                                                ));                                                                                                                                   
    request.setAttribute("customCss", customCss); 
%>

<c:set var="requiredHint" value="<span class='requiredHint'> *</span>" />
<c:set var="yearHint" value="<span class='hint'>(YYYY)</span>" />

<c:choose>
    <%-- Includes edit AND repair mode --%>
    <c:when test="<%= request.getAttribute(\"objectUri\")!=null %>">     	
        <c:set var="titleVerb" value="Edit" />        
        <c:set var="submitButtonText" value="Edit ${buttonLabel}" />
        <c:set var="disabledVal">${editMode == "repair" ? "" : "disabled" }</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="titleVerb" value="Create" />
        <c:set var="editMode" value="add" />
        <c:set var="submitButtonText" value="${buttonLabel}" />
        <c:set var="disabledVal" value="" />
    </c:otherwise>
</c:choose>

<jsp:include page="${preForm}" />

<% if( mode == EditMode.ERROR ){ %>
 <div>This form is unable to handle the editing of this role because it is associated with 
      multiple ${param.roleActivityTypeLabel} individuals.</div>      
<% }else{ %>
	
	<h2>${titleVerb}&nbsp;${roleActivityTypeLabel} entry for <%= subjectName %></h2>
	<%-- DO NOT CHANGE IDS, CLASSES, OR HTML STRUCTURE IN THIS FORM WITHOUT UNDERSTANDING THE IMPACT ON THE JAVASCRIPT! --%>
	
	<form id="addRoleForm" class="customForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" >
	
	    <p class="inline"><v:input type="select" label="${roleActivityTitleCase} Type ${requiredHint}" name="roleActivityType" disabled="${disabledVal}" id="typeSelector" /></p>
	    
	    <div class="fullViewOnly">
	        
		    <p><v:input type="text" id="relatedIndLabel" name="activityLabel" label="### Name ${requiredHint}" cssClass="acSelector" disabled="${disabledVal}" size="50"  /></p>
	
	        <%-- Store these values in hidden fields, because the displayed fields are disabled and don't submit. This ensures that when
	        returning from a validation error, we retain the values. --%>
	        <c:if test="${editMode == 'edit'}">
	           <v:input type="hidden" id="roleActivityType" />
	           <v:input type="hidden" id="activityLabel" />
	        </c:if>
	        
		    <div class="acSelection">
		        <%-- RY maybe make this a label and input field. See what looks best. --%>
		        <p class="inline"><label></label><span class="acSelectionInfo"></span> <a href="<c:url value="/individual?uri=" />" class="verifyMatch">(Verify this match)</a></p>
		        <v:input type="hidden" id="roleActivityUri" name="roleActivity" cssClass="acUriReceiver" /> <!-- Field value populated by JavaScript -->
		    </div>
	
	        <p><v:input type="text" id="roleLabel" label="Role in ### ${requiredHint}" size="50" /></p>
	        
	        <c:choose>
	            <c:when test="${numDateFields == 1}">
	                <v:input id="startField" label="Year ${yearHint}" size="7"/>            
	            </c:when>
	            <c:otherwise>
	                <h4 class="label">Years of Participation in ###</h4>    
	                <v:input id="startField" label="Start Year ${yearHint}" size="7"/>   
	                <v:input id="endField" label="End Year ${yearHint}" size="7"/>             
	            </c:otherwise>
	        </c:choose>
	 
	    </div>   
	     
	    <p class="submit"><v:input type="submit" id="submit" value="${submitButtonText}" cancel="true" /></p>
	    
	    <p id="requiredLegend" class="requiredHint">* required fields</p>
	</form>
	
	<c:url var="acUrl" value="/autocomplete?tokenize=true&stem=true" />
	
	<script type="text/javascript">
	var customFormData  = {
	    acUrl: '${acUrl}',
	    editMode: '${editMode}',
	    submitButtonTextType: 'compound',
	    defaultTypeName: 'activity' // used in repair mode, to generate button text and org name field label
	};
	</script>
<% } %>

<jsp:include page="${postForm}"/>

<%!

private static final String VIVO_CORE = "http://vivoweb.org/ontology/core#";
private static final String  DEFAULT_ACTIVITY_TYPE_QUERY = 
    "PREFIX core: <" + VIVO_CORE + ">\n" +
    "SELECT ?existingActivityType WHERE { \n" +
        "?role core:roleIn ?existingActivity . \n" +
        "?existingActivity a ?existingActivityType . \n" +
    "}"; 
// The activity type query results must be limited to the values in the activity type select element. 
// Sometimes the query returns a superclass such as owl:Thing instead. 
private String getActivityTypeQuery(VitroRequest vreq) {

    String activityTypeQuery = null;

	// Select options are subclasses of a specified class
	String objectClassUri = vreq.getParameter("roleActivityType_objectClassUri");
	if (StringUtils.isNotBlank(objectClassUri)) { 
	    log.debug("objectClassUri = " + objectClassUri);
	    activityTypeQuery = 
	    "PREFIX core: <" + VIVO_CORE + ">\n" +
	    "PREFIX rdfs: <" + VitroVocabulary.RDFS + ">\n" +
	    "SELECT ?existingActivityType WHERE {\n" +
	        "?role core:roleIn ?existingActivity . \n" +
	        "?existingActivity a ?existingActivityType . \n" +
	        "?existingActivityType rdfs:subClassOf <" + objectClassUri + "> . \n" +
	    "}";
	} else {  
	    String optionsType = vreq.getParameter("roleActivityType_optionsType");
	    // Select options are hardcoded
	    if ("HARDCODED_LITERALS".equals(optionsType)) {
	        String typeLiteralOptions = vreq.getParameter("roleActivityType_literalOptions");
	        if (StringUtils.isNotBlank(typeLiteralOptions)) {           
	            try {
	                JSONObject json = new JSONObject("{values: [" + typeLiteralOptions + "]}");
	                Set<String> typeUris = new HashSet<String>();
	                JSONArray values = json.getJSONArray("values");
	                int valueCount = values.length();
	                for (int i = 0; i < valueCount; i++) {
	                    JSONArray option = values.getJSONArray(i);
	                    String uri = option.getString(0);
	                    if (StringUtils.isNotBlank(uri)) {
	                        typeUris.add("(?existingActivityType = <" + uri + ">)");
	                    }	                    
	                }
	                String typeFilters = "FILTER (" + StringUtils.join(typeUris, "||") + ")";
	                activityTypeQuery = DEFAULT_ACTIVITY_TYPE_QUERY.replace("}", "") + typeFilters + "}";
	            } catch (JSONException e) {
	                activityTypeQuery = DEFAULT_ACTIVITY_TYPE_QUERY;
	            }
	        }
	    } else { 
	        activityTypeQuery = DEFAULT_ACTIVITY_TYPE_QUERY;	    
	    } 
	}
	log.debug("Activity type query: " + activityTypeQuery);
    return activityTypeQuery;
}
%>