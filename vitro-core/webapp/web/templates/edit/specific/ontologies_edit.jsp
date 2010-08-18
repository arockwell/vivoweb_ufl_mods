<?xml version="1.0" encoding="UTF-8"?>

<!--
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
-->

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" 
          xmlns:c="http://java.sun.com/jstl/core" 
          xmlns:form="http://vitro.mannlib.cornell.edu/edit/tags"
          version="2.0">
<jsp:directive.page import="edu.cornell.mannlib.vitro.webapp.controller.Controllers"/>


<div class="editingForm">

<jsp:include page="/templates/edit/fetch/vertical.jsp"/> 

<div align="center">
<table class="form-background" border="0" cellpadding="2" cellspacing="2">
<tr valign="bottom" align="center">
	<td>
		<form action="listOntologies" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="Show All Ontologies"/>
		</form>
		<form action="showClassHierarchy" method="get">
                        <input type="hidden" name="home" value="${portalBean.portalId}" />
                        <input type="submit" class="form-button" value="Show This Ontology's Class Hierarchy" />
                        <input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
                </form>
		<form action="listPropertyWebapps" method="get">
                        <input type="hidden" name="home" value="${portalBean.portalId}" />
                        <input type="submit" class="form-button" value="Show This Ontology's Object Properties" />
                        <input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
                </form>
		<form action="listDatatypeProperties" method="get">
                        <input type="hidden" name="home" value="${portalBean.portalId}" />
                        <input type="submit" class="form-button" value="Show This Ontology's Datatype Properties" />
                        <input type="hidden" name="ontologyUri" value="${Ontology.URI}" />
                </form>
	</td>
	<td valign="bottom" align="center">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="Edit ${Ontology.name}"/>
			<input name="uri" type = "hidden" value="${Ontology.URI}" />
			<input type="hidden" name="controller" value="Ontology"/>
		</form>
	</td>
	<td valign="bottom">
		<form action="editForm" method="get">
			<input type="hidden" name="home" value="${portalBean.portalId}" />
			<input type="submit" class="form-button" value="Add New Ontology"/>
			<input type="hidden" name="controller" value="Ontology"/>
		</form>
        <form action="editForm" method="get">
                <input type="submit" class="form-button" value="Change URI"/>
                <input type="hidden" name="home" value="${portalBean.portalId}" />
                <input type="hidden" name="oldURI" value="${realURI}"/>
                <input type="hidden" name="mode" value="renameResource"/>
                <input type="hidden" name="controller" value="Refactor"/>
        </form>

        <div style="margin-left:-0.5em;margin-top:0.5em;padding:0.5em;border-style:solid;border-width:1px;">
            <form action="${exportURL}" method="get">
                <input type="hidden" name="subgraph" value="tbox"/>
                <input type="hidden" name="assertedOrInferred" value="asserted"/>
                <input type="hidden" name="ontologyURI" value="${Ontology.URI}"/>
                <input type="submit" class="form-button" name="submit" value="export ontology to RDF"/>
                <div style="padding:0;margin-top:0.3em;white-space:nowrap;">
	                <input type="radio" name="format" value="RDF/XML-ABBREV" checked="checked" selected="selected"/> RDF/XML abbreviated
	                <input type="radio" name="format" value="RDF/XML"/> RDF/XML
	                <input type="radio" name="format" value="N3"/> N3
	                <input type="radio" name="format" value="N-TRIPLES"/> N-Triples
	                <input type="radio" name="format" value="TURTLE"/> Turtle
                </div>
            </form>
        </div>


	</td>
</tr>
</table>
</div>
</div>

</jsp:root>
