/*
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
*/

package edu.cornell.mannlib.vitro.webapp.visualization.constants;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class QueryConstants {
	
	public static final Map<String, String> PREFIX_TO_NAMESPACE = new HashMap<String, String>() { {
		
			put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			put("xsd", "http://www.w3.org/2001/XMLSchema#");
			put("owl", "http://www.w3.org/2002/07/owl#");
			put("swrl", "http://www.w3.org/2003/11/swrl#");
			put("swrlb", "http://www.w3.org/2003/11/swrlb#");
			put("vitro", "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#");
			put("far", "http://vitro.mannlib.cornell.edu/ns/reporting#");
			put("ai", "http://vitro.mannlib.cornell.edu/ns/hotel#");
			put("aktp", "http://www.aktors.org/ontology/portal#");
			put("akts", "http://www.aktors.org/ontology/support#");
			put("bibo", "http://purl.org/ontology/bibo/");
			put("hr", "http://vivo.cornell.edu/ns/hr/0.9/hr.owl#");
			put("dcterms", "http://purl.org/dc/terms/");
			put("dcelem", "http://purl.org/dc/elements/1.1/");
			put("event", "http://purl.org/NET/c4dm/event.owl#");
			put("foaf", "http://xmlns.com/foaf/0.1/");
			put("geo", "http://aims.fao.org/aos/geopolitical.owl#");
			put("mann", "http://vivo.cornell.edu/ns/mannadditions/0.1#");
			put("pubmed", "http://vitro.mannlib.cornell.edu/ns/pubmed#");
			put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			put("rdfsyn", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			put("skos", "http://www.w3.org/2004/02/skos/core#");
			put("socsci", "http://vivo.library.cornell.edu/ns/vivo/socsci/0.1#");
			put("stars", "http://vitro.mannlib.cornell.edu/ns/cornell/stars/classes#");
			put("temp", "http://vitro.mannlib.cornell.edu/ns/temp#");
			put("wos", "http://vivo.mannlib.cornell.edu/ns/ThomsonWOS/0.1#");
			put("core", "http://vivoweb.org/ontology/core#");
			put("vivo", "http://vivo.library.cornell.edu/ns/0.1#");
			put("j.1", "http://aims.fao.org/aos/geopolitical.owl#");
			put("j.2", "http://vitro.mannlib.cornell.edu/ns/vitro/public#");
		
	} };
	
	public static String getSparqlPrefixQuery() {
		
		StringBuilder prefixSection = new StringBuilder(); 
		
		for (Map.Entry<String, String> prefixEntry : PREFIX_TO_NAMESPACE.entrySet()) {
			prefixSection.append("PREFIX " + prefixEntry.getKey() 
									+ ": <" + prefixEntry.getValue() + ">\n");
		}
		return prefixSection.toString();
	}
}


