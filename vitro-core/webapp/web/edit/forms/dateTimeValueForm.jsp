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

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="com.hp.hpl.jena.vocabulary.XSD" %>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.StartYearBeforeEndYear"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.JavaScript" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Css" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.elements.DateTimeWithPrecision"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field"%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.DateTimeIntervalValidation"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%!
    public static Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.webapp.jsp.edit.forms.dateTimeValueForm");
%>
<%
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    vreq.setAttribute("defaultNamespace", ""); //empty string triggers default new URI behavior
%>

<c:set var="vivoCore" value="http://vivoweb.org/ontology/core#" />
<c:set var="type" value="<%= VitroVocabulary.RDF_TYPE %>" />
<c:set var="rdfs" value="<%= VitroVocabulary.RDFS %>" />
<c:set var="label" value="${rdfs}label" />

<c:set var="toDateTimeValue" value="${vivoCore}dateTimeValue"/>
<c:set var="valueType" value="${vivoCore}DateTimeValue"/>

<c:set var="dateTimeValue" value="${vivoCore}dateTime"/>
<c:set var="dateTimePrecision" value="${vivoCore}dateTimePrecision"/>

<%-- Assertions for adding a new date time value --%>
<v:jsonset var="n3ForValue">
    ?subject      <${toDateTimeValue}> ?valueNode .
    ?valueNode  <${type}> <${valueType}> .
    ?valueNode  <${dateTimeValue}> ?dateTimeField.value .
    ?valueNode  <${dateTimePrecision}> ?dateTimeField.precision .
</v:jsonset>

<%-- Queries for editing an existing role --%>
<v:jsonset var="existingNodeQuery" >
    SELECT ?existingNode WHERE {
          ?subject <${toDateTimeValue}> ?existingNode .
          ?existingNode <${type}> <${valueType}> . }
</v:jsonset>

<v:jsonset var="existingDateTimeValueQuery" >
    SELECT ?existingDateTimeValue WHERE {
     ?subject <${toDateTimeValue}> ?existingValueNode .
     ?existingValueNode <${type}> <${valueType}> .
     ?existingValueNode <${dateTimeValue}> ?existingDateTimeValue . }
</v:jsonset>

<v:jsonset var="existingPrecisionQuery" >
    SELECT ?existingPrecision WHERE {
      ?subject <${toDateTimeValue}> ?existingValueNode .
      ?existingValueNode <${type}> <${valueType}> .
      ?existingValueNode <${dateTimePrecision}> ?existingPrecision . }
</v:jsonset>


<%-- Configure add vs. edit --%>
<%

    String objectUri = (String) request.getAttribute("objectUri");
    if (objectUri != null) { // editing existing entry
%>
        <c:set var="editMode" value="edit" />
        <c:set var="titleVerb" value="Edit" />
        <c:set var="submitButtonText" value="Edit Date/Time Value" />
        <c:set var="disabledVal" value="disabled" />
<% 
    } else { // adding new entry
%>
        <c:set var="editMode" value="add" />
        <c:set var="titleVerb" value="Create" />
        <c:set var="submitButtonText" value="Create Date/Time Value" />
        <c:set var="disabledVal" value="" />
<%  } %> 

<c:set var="editjson" scope="request">
  {
    "formUrl" : "${formUrl}",
    "editKey" : "${editKey}",
    "urlPatternToReturnTo" : "/individual",
    
    "subject"   : ["subject",    "${subjectUriJson}" ],
    "predicate" : ["toDateTimeValue", "${predicateUriJson}" ],
    "object"    : ["valueNode", "${objectUriJson}", "URI" ],
    
    "n3required"    : [  ],
    
    "n3optional"    : [ "${n3ForValue}" ],
    
    "newResources"  : { "valueNode" : "${defaultNamespace}" },
    
    "urisInScope"    : { },
    "literalsInScope": { },
    "urisOnForm"     : [ ],
    "literalsOnForm" :  [ ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : {
        "dateTimeField.value"   : "${existingDateTimeValueQuery}",
    },
    "sparqlForExistingUris" : {
        "valueNode"      : "${existingNodeQuery}",
        "dateTimeField.precision": "${existingPrecisionQuery}"
    },
    "fields" : {     
      "dateTimeField" : {
         "newResource"      : "false",
         "validators"       : [  ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "",
         "rangeLang"        : "",
         "assertions"       : ["${n3ForValue}"]
      }
  }
}
</c:set>

<%
    log.debug(request.getAttribute("editjson"));

    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        editConfig = new EditConfiguration((String) request.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig,session);
        
        //setup date time edit elements
        Field dateTimeField = editConfig.getField("dateTimeField");
        // arguments for DateTimeWithPrecision are (fieldName, minimumPrecision, [requiredLevel])
        dateTimeField.setEditElement(new DateTimeWithPrecision(dateTimeField, VitroVocabulary.Precision.SECOND.uri(), VitroVocabulary.Precision.NONE.uri()));
    }     
            
    Model model = (Model) application.getAttribute("jenaOntModel");
    
    if (objectUri != null) { // editing existing
        editConfig.prepareForObjPropUpdate(model);
    } else { // adding new
        editConfig.prepareForNonUpdate(model);
    }
    
    List<String> customJs = new ArrayList<String>(Arrays.asList(JavaScript.JQUERY_UI.path(),
                                                                JavaScript.CUSTOM_FORM_UTILS.path()
                                                               ));            
    request.setAttribute("customJs", customJs);
    
    List<String> customCss = new ArrayList<String>(Arrays.asList(Css.JQUERY_UI.path(),
                                                                 Css.CUSTOM_FORM.path()
                                                                ));
    request.setAttribute("customCss", customCss);
    
    String subjectName = ((Individual) request.getAttribute("subject")).getName();
%>

<jsp:include page="${preForm}" />

<h2>${titleVerb} date time value for <%= subjectName %></h2>

<form class="customForm" action="<c:url value="/edit/processRdfForm2.jsp"/>" >
    
    <v:input id="dateTimeField" />
       
    <p class="submit"><v:input type="submit" id="submit" value="${submitButtonText}" cancel="true"/></p>
</form>
    
<jsp:include page="${postForm}"/>