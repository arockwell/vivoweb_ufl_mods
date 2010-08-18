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

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Arrays"%>

<%@ page import="com.hp.hpl.jena.rdf.model.Literal"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.StringUtils" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="v" uri="http://vitro.mannlib.cornell.edu/vitro/tags" %>

<%! 
    private String getInputType(String propertyName) {
        String inputType = StringUtils.equalsOneOf(propertyName, "blurb", "description") ? "textarea" : "text";
        return  inputType;
    }
    String thisPage = "defaultVitroNsPropForm.jsp";
    String inThisPage = " in " + thisPage;

%>
<%
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("edu.cornell.mannlib.vitro.jsp.edit.forms." + thisPage);
    log.debug("Starting " + thisPage);
    
    VitroRequest vreq = new VitroRequest(request);
    WebappDaoFactory wdf = vreq.getWebappDaoFactory();
    vreq.setAttribute("defaultNamespace", wdf.getDefaultNamespace());
    
    String subjectUri   = vreq.getParameter("subjectUri");
    String predicateUri = vreq.getParameter("predicateUri");
    String propertyName = predicateUri.substring(predicateUri.lastIndexOf("#")+1);
    vreq.setAttribute("propertyName", propertyName);
 
    DataPropertyStatement dps = (DataPropertyStatement)vreq.getAttribute("dataprop");
     
    String datapropKeyStr = vreq.getParameter("datapropKey");
    
    Individual subject = (Individual)vreq.getAttribute("subject");
    if( subject == null ) {
        throw new Error("In " + thisPage + ", could not find subject " + subjectUri);
    }
    
    Model model =  (Model)application.getAttribute("jenaOntModel");
    
    // Get datatype and language for the data property statement
    String rangeDatatypeUri = null,
           rangeLang = null;
    
    if (dps != null) {
        
        rangeDatatypeUri = dps.getDatatypeURI();        
        if (rangeDatatypeUri == null) {
            log.debug("no range datatype uri set on vitro namespace property statement for property " + predicateUri + inThisPage);
        } else {
            log.debug("range datatype uri of [" + rangeDatatypeUri + "] on vitro namespace property statement for property " + predicateUri + inThisPage);
        }        
        
        rangeLang = dps.getLanguage();
        if( rangeLang == null ) {            
            log.debug("no language attribute on vitro namespace property statement for property " + predicateUri + inThisPage);
            rangeLang = "";
        } else {
            log.debug("language attribute of ["+rangeLang+"] on vitro namespace property statement for property " + predicateUri + inThisPage);
        }
        
    } else {
        log.debug("No incoming vitro namespace property statement for property "+predicateUri+"; adding a new statement");  
        rangeDatatypeUri = FrontEndEditingUtils.getVitroNsPropDatatypeUri(predicateUri);   
    }
    
    String rangeDatatypeUriJson = rangeDatatypeUri == null ? "" : MiscWebUtils.escape(rangeDatatypeUri);
  
    vreq.setAttribute("rangeDatatypeUriJson", rangeDatatypeUriJson);   
    vreq.setAttribute("rangeLangJson", rangeLang);    

    // Create list of validators
    ArrayList<String> validatorList = new ArrayList<String>();
    if (predicateUri.equals(VitroVocabulary.LABEL) || predicateUri.equals(VitroVocabulary.RDF_TYPE)) {
        validatorList.add("nonempty");       
    }
    if (!StringUtils.isEmpty(rangeDatatypeUriJson)) {
        validatorList.add("datatype:" + rangeDatatypeUriJson);
    }
    vreq.setAttribute("validators", StringUtils.quotedList(validatorList, ","));

%>

<c:set var="predicate" value="<%=predicateUri%>" />

<%--  Then enter a SPARQL query for the field, by convention concatenating the field id with "Existing"
      to convey that the expression is used to retrieve any existing value for the field in an existing individual.
      This must then be referenced in the sparqlForExistingLiterals section of the JSON block below
      and in the literalsOnForm --%>
<v:jsonset var="dataExisting">
    SELECT ?dataExisting WHERE {
        ?subject <${predicate}> ?dataExisting }
</v:jsonset>

<%--  Pair the "existing" query with the skeleton of what will be asserted for a new statement involving this field.
      The actual assertion inserted in the model will be created via string substitution into the ? variables.
      NOTE the pattern of punctuation (a period after the prefix URI and after the ?field) --%>
<v:jsonset var="dataAssertion"  >
    ?subject <${predicate}> ?${propertyName} .
</v:jsonset>

<c:set var="editjson" scope="request">
  {
    "formUrl"              : "${formUrl}",
    "editKey"              : "${editKey}",
    "datapropKey"          : "<%= datapropKeyStr == null ? "" : datapropKeyStr %>",    
    "urlPatternToReturnTo" : "/entity",
    
    "subject"   : ["subject",   "${subjectUriJson}" ],
    "predicate" : ["predicate", "${predicateUriJson}" ],
    "object"    : ["${propertyName}", "", "DATAPROPHASH" ],
    
    "n3required"    : [ "${dataAssertion}" ],
    "n3optional"    : [ ],
    "newResources"  : { },
    "urisInScope"    : { },
    "literalsInScope": { },
    "urisOnForm"     : [ ],
    "literalsOnForm" :  [ "${propertyName}" ],
    "filesOnForm"    : [ ],
    "sparqlForLiterals" : { },
    "sparqlForUris" : {  },
    "sparqlForExistingLiterals" : { "${propertyName}" : "${dataExisting}" },
    "sparqlForExistingUris" : { },
    "fields" : {
      "${propertyName}" : {
         "newResource"      : "false",
         "validators"       : [ ${validators} ],
         "optionsType"      : "UNDEFINED",
         "literalOptions"   : [ ],
         "predicateUri"     : "",
         "objectClassUri"   : "",
         "rangeDatatypeUri" : "${rangeDatatypeUriJson}",
         "rangeLang"        : "${rangeLangJson}",
         "assertions"       : [ "${dataAssertion}" ]
      }
  }
}
</c:set>

<%
    if( log.isDebugEnabled()) log.debug(request.getAttribute("editjson"));
    
    EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session,request);
    if (editConfig == null) {
        log.debug("No editConfig in session. Making new editConfig.");
        log.debug(vreq.getAttribute("editjson"));
        editConfig = new EditConfiguration((String)vreq.getAttribute("editjson"));
        EditConfiguration.putConfigInSession(editConfig, session);

    }
    
    if ( datapropKeyStr != null && datapropKeyStr.trim().length() > 0  ) {
        editConfig.prepareForDataPropUpdate(model,dps);
    }    

    // Configure form
    String propertyLabel = propertyName.equals("label") ? "name" : propertyName;
    String actionText = dps == null ? "Add new " : "Edit ";
    String submitLabel = actionText + propertyLabel;
    String title = actionText + "<em>" + propertyLabel + "</em> for " + subject.getName();
    
    String inputType = getInputType(propertyName);
    log.debug(propertyName + " needs input type " + inputType + inThisPage);
    boolean useTinyMCE = inputType.equals("textarea");
    log.debug( (useTinyMCE ? "" : "not ") + "using tinyMCE to edit " + propertyName + inThisPage);
  
%>

<jsp:include page="${preForm}">
    <jsp:param name="useTinyMCE" value="<%= useTinyMCE %>"/>
</jsp:include>

<h2><%= title %></h2>
<form action="<c:url value="/edit/processDatapropRdfForm.jsp"/>" >
    <v:input type="<%= inputType %>" id="${propertyName}" size="30" />
    <input type="hidden" name="vitroNsProp" value="true" />
    <p class="submit"><v:input type="submit" id="submit" value="<%= submitLabel %>" cancel="true"/></p>
</form>

<c:if test="${ (!empty param.datapropKey) && (empty param.deleteProhibited) }">
     <form class="deleteForm" action="editDatapropStmtRequestDispatch.jsp" method="post">                               
            <label for="delete"><h3>Delete this entry?</h3></label>
            <input type="hidden" name="subjectUri"   value="${param.subjectUri}"/>
            <input type="hidden" name="predicateUri" value="${param.predicateUri}"/>
            <input type="hidden" name="datapropKey"  value="${param.datapropKey}"/>                
            <input type="hidden" name="cmd"          value="delete"/>
            <input type="hidden" name="vitroNsProp" value="true" />
            <v:input type="submit" id="delete" value="Delete" cancel="" />
     </form>
</c:if>

<jsp:include page="${postForm}"/>


