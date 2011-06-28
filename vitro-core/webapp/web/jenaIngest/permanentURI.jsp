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

<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Model"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
	maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>

<h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Assign Permanent URIs To Resources</h2>

<p>This tool will rename the resources in the selected model to with new
randomly-generated URIs following the pattern used in the main application.  
The tool will generate URIs that are not already in use in the main web 
application model. Statements using the newly-renamed resources will be 
written to the "model to save."</p> 

<p>The permanent URIs may be generated in an arbitrary "new namespace for 
resources."  Otherwise, the "use default namespace" option will generate
URIs exactly of the form created through the GUI interface.</p>

<form action="ingest" method="get" >
<%String modelName = (String)request.getAttribute("modelName"); %>
<input type="hidden" name="oldModel" value="<%=modelName%>"/>
<input type="hidden" name="action" value="permanentURI" />
<p>Current namespace of resources  <select name=oldNamespace><%List namespaces = (List)request.getAttribute("namespaceList");
if(namespaces != null){
	Iterator namespaceItr = namespaces.iterator();
	Integer count = 0;
	while (namespaceItr.hasNext()){
		String namespaceText = (String) namespaceItr.next();
		%>
<option value="<%=namespaceText%>"><%=namespaceText%></option><%}}%>
</select></p>

<p>Model to save  <select name=newModel>
<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelNames = (String) it.next();
        %>
<option value="<%=modelNames%>"><%=modelNames%></option>
<%    
    }
%>
</select></p>
<p>New namespace for resources  <input type="text" name="newNamespace" /></p>
Or <%String defaultNamespace = (String)request.getAttribute("defaultNamespace");%>
<p>Use default namespace <%=defaultNamespace%>  <input type="checkbox" name="defaultNamespace" value ="<%=defaultNamespace%>"/>
</p>

<p><input class="submit" type="submit" name="submit" value="Generate URIs" /></p>
</form>