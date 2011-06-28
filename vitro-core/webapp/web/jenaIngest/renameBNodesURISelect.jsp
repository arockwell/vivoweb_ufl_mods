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

<%@ page import="com.hp.hpl.jena.ontology.Individual" %>
<%@ page import="com.hp.hpl.jena.ontology.OntModel" %>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker" %>
<%@ page import="com.hp.hpl.jena.shared.Lock" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map.Entry" %>

<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
        maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jenaIngest/renameNode.js"></script>
<script type="text/javascript">
function selectProperties(){
	document.getElementById("properties").disabled = false;
	document.getElementById("pattern").disabled = false;
}
function disableProperties(){
	document.getElementById("properties").disabled = true;
	document.getElementById("pattern").disabled = true;
}
</script>

    <h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Convert Blank Nodes to Named Resources</h2>

    <form id="takeuri" action="ingest" method="get">
        <input type="hidden" name="action" value="renameBNodesURISelect"/>

    <h3>Select URI prefix</h3>
   
	<p>URIs will be constructed from the following string:</p>
	<input id="namespace" type="text" style="width:65%;" name="namespaceEtcStr"/> 

    <p/>
    
    <p>You can concatenate above string with random integer OR your own pattern based on <b>values</b> of one of the properties (Properties will be enabled in the dropdown) </p>
    
    <input type="radio" value="integer" name="concatenate" checked="checked" onclick="disableProperties()"><b>No</b>, concatenate with random integer</input>
    <br></br>
    <input type="radio" value="pattern" name="concatenate" onclick="selectProperties()"><b>Yes</b>, concatenate with my pattern</input> 
  
    
    <% Map<String,LinkedList<String>> propertyMap = (Map) request.getAttribute("propertyMap");
       Set<Entry<String,LinkedList<String>>> set = propertyMap.entrySet();
       Iterator<Entry<String,LinkedList<String>>> itr = set.iterator();
       Entry<String, LinkedList<String>> entry = null;
    %>
   <%if(itr.hasNext()){%>
    <select name="property" id="properties" disabled="disabled">
    <% while(itr.hasNext()){%>
    
    	<%entry = itr.next();
    	Iterator<String> listItr = entry.getValue().iterator();
    	%>
    	<option value ="<%=entry.getKey() %>"><%=entry.getKey()%></option>
    <%}}
    %>
    </select>
    <br></br>
    <p>Enter your pattern that will prefix property value with an underscore eg. depID_$$$ where depID is your pattern and $$$ is the property value.</p>
    <input id="pattern" disabled="disabled" type="text" style="width:35%;" name="pattern"/> 
    
    
  
    <h3>Select Destination Model</h3>

    <select name="destinationModelName">
        <option value="vitro:baseOntModel">webapp assertions</option>
<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %> <option value="<%=modelName%>"/><%=modelName%></option>
        <%    
    }
%>   
    </select>

    <input class="submit" type="submit" value="Rename Blank Nodes"/>
    
    </form>
    
