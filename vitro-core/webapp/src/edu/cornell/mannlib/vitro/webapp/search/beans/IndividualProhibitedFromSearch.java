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

package edu.cornell.mannlib.vitro.webapp.search.beans;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;

public class IndividualProhibitedFromSearch {
    
    protected OntModel fullModel;
    
    protected static Log log = LogFactory.getLog(IndividualProhibitedFromSearch.class);
    
    
    public IndividualProhibitedFromSearch( ServletContext context ){
        this.fullModel = ModelContext.getUnionOntModelSelector(context).getFullModel(); 
    }    
    
    public boolean isIndividualProhibited(String uri){
        if( uri == null || uri.isEmpty() )
            return true;
        
        boolean prohibited = false;
        try {
            fullModel.getLock().enterCriticalSection(Lock.READ);                               
            Query query = makeAskQueryForUri( uri );
            prohibited = QueryExecutionFactory.create( query, fullModel).execAsk();            
        } finally {
            fullModel.getLock().leaveCriticalSection();
        }
        if( prohibited )
            log.debug("prohibited " + uri);
        
        return prohibited;
    }
    
    private Query makeAskQueryForUri( String uri ){
        String queryString =
            "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n" +            
            "ASK { \n" +
            "    <"+uri+"> <" + RDF.type.getURI() + "> ?type . \n" +             
            "  FILTER ( \n" + 
            "     (  fn:starts-with( str(?type), \"" + VitroVocabulary.vitroURI + "\" ) \n" +
            "        && \n"+
            "        ! fn:starts-with( str(?type), \"" + VitroVocabulary.vitroURI + "Flag\" ) ) || \n" +            
            "     fn:starts-with( str(?type), \"" + VitroVocabulary.PUBLIC + "\" ) || \n" +
            "     str(?type) = \"" + OWL.ObjectProperty.getURI() + "\"  || \n" +
            "     str(?type) = \"" + OWL.DatatypeProperty.getURI() + "\"  || \n" +
            "     str(?type) = \"" + OWL.AnnotationProperty.getURI() + "\"  \n" +
            "   )\n" +
            "}" ;                
        return QueryFactory.create( queryString );
    }
}
