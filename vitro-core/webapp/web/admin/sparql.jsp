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
<%@page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<script type="text/javascript" src="../js/sparql/prototype.js">
</script>
<script type="text/javascript" src="../js/sparql/sparql.js">
</script>
<style type="text/css">
td {
	vertical-align: top;
	border: 1px solid #b2b2b2;
}
</style>
<BODY onload="init()">
	<div id="content" class="sparqlform"
		style="width: 900px; margin: 0 auto;">
		<h2>
			SPARQL Query Builder
		</h2>
		<table id="builder" width="100%">
			<tr>
				<td width="33%">
					Subject
				</td>
				<td width="33%">
					Predicate
				</td>
				<td width="34%">
					Object
				</td>
			</tr>
			<tr id="clazz(0)">
				<td id="subject(0)" width="33%">
					<select id="subject(0,0)">
						<option value="">
							Thing
						</option>
					</select>
				</td>
				<td id="predicate(0)">

				</td>
				<td id="object(0)" width="34%">

				</td>
			</tr>
		</table>
		<div>
			<input type="button" class="submit" value="Generate Query" onclick="genQuery();" />
		</div>
		<div id="sparqlquery" style="visibility: hidden;">
			<form action="sparqlquery">
				<div>
					<textarea id="query" name="query" rows="20" cols="111" class="maxWidth">
							
						</textarea>
				</div>
				<p>
					<h3>
						Format for SELECT query results:
					</h3>
					<input id='RS_XML_BUTTON' type='radio' name='resultFormat'
						value='RS_XML'>
					<label for='RS_XML_BUTTON'>
						RS_XML
					</label>
					<input id='RS_TEXT_BUTTON' type='radio' name='resultFormat'
						value='RS_TEXT' checked='checked'>
					<label for='RS_TEXT_BUTTON'>
						RS_TEXT
					</label>
					<input id='RS_CSV_BUTTON' type='radio' name='resultFormat'
						value='vitro:csv'>
					<label for='RS_CSV_BUTTON'>
						CSV
					</label>
					<input id='RS_RDF_N3_BUTTON' type='radio' name='resultFormat'
						value='RS_RDF/N3'>
					<label for='RS_RDF_N3_BUTTON'>
						RS_RDF/N3
					</label>
					<input id='RS_JSON_BUTTON' type='radio' name='resultFormat'
						value='RS_JSON'>
					<label for='RS_JSON_BUTTON'>
						RS_JSON
					</label>
					<input id='RS_RDF_BUTTON' type='radio' name='resultFormat'
						value='RS_RDF'>
					<label for='RS_RDF_BUTTON'>
						RS_RDF
					</label>
				</p>

				<p>
					<h3>
						Format for CONSTRUCT and DESCRIBE query results:
					</h3>
					<input id='RR_RDFXML_BUTTON' type='radio' name='rdfResultFormat'
						value='RDF/XML'>
					<label for='RR_RDFXML_BUTTON'>
						RDF/XML
					</label>
					<input id='RR_RDFXMLABBREV_BUTTON' type='radio'
						name='rdfResultFormat' value='RDF/XML-ABBREV' checked='checked'>
					<label for='RR_RDFXMLABBREV_BUTTON'>
						RDF/XML-ABBREV
					</label>
					<input id='RR_N3_BUTTON' type='radio' name='rdfResultFormat'
						value='N3'>
					<label for='RR_N3_BUTTON'>
						N3
					</label>
					<input id='RR_NTRIPLE_BUTTON' type='radio' name='rdfResultFormat'
						value='N-TRIPLE'>
					<label for='RR_NTRIPLE_BUTTON'>
						N-Triples
					</label>
					<input id='RR_TURTLE_BUTTON' type='radio' name='rdfResultFormat'
						value='TTL'>
					<label for='RR_TURTLE_BUTTON'>
						Turtle
					</label>
				</p>

				<div>

					<ul class="clean">
						<%
							try {
								if (request.getSession() != null
										&& application.getAttribute("vitroJenaModelMaker") != null) {
									ModelMaker maker = (ModelMaker) application
											.getAttribute("vitroJenaModelMaker");
									for (Iterator it = maker.listModels(); it.hasNext();) {
										String modelName = (String) it.next();
						%>
						<li>
							<input type="checkbox" name="sourceModelName"
								value="<%=modelName%>" /><%=modelName%></li>
						<%
							}
								} else {
						%><li>
							could not find named models in session
						</li>
						<%
							}
							} catch (Exception ex) {
						%><li>
							could not find named models in ModelMaker
						</li>
						<%
							}
						%>
					</ul>

				</div>
				<input type="submit" value="Run Query" class="submit">
			</form>
		</div>
	</div>
</BODY>
</HTML>
