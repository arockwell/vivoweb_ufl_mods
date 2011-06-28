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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

public class ProhibitedFromSearch {
	List<String> prohibitedClasses;
	String ProhibitedFromSearchURI;
	
	private static final String queryForProhibitedClasses = "SELECT ?prohibited WHERE{" +
			"?searchConfig <" + DisplayVocabulary.EXCLUDE_CLASS + "> ?prohibited . }";
		
	private static final Log log = LogFactory.getLog(ProhibitedFromSearch.class.getName()); 
		
	public ProhibitedFromSearch(String URI, OntModel model){
		this.ProhibitedFromSearchURI = URI;
		this.prohibitedClasses = new ArrayList<String>();
		addAllProhibitedClasses( buildProhibitedClassesList(URI,model) );
		model.register(new ProhibitedFromSearchChangeListener( this ));
	}
	 
	public synchronized boolean isClassProhibited(String classURI){
		if( classURI != null ){
			return prohibitedClasses.contains(classURI);
		}else{
			return false;
		}
	}
	
	private synchronized void removeProhibitedClass(String classURI){
		prohibitedClasses.remove(classURI);
	}
	
	private synchronized void addProhibitedClass(String classURI){
		prohibitedClasses.add(classURI);
	}
	
	private synchronized void addAllProhibitedClasses(List<String> classURIs){
		prohibitedClasses.addAll(classURIs);
	}
	
	private List<String> buildProhibitedClassesList( String URI, OntModel model){
		List<String> newProhibitedClasses = new ArrayList<String>();
		
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		Resource searchConfig = ResourceFactory.createResource(URI);
		initialBinding.add("searchConfig", searchConfig);

		Query query = QueryFactory.create(queryForProhibitedClasses);
		model.enterCriticalSection(Lock.READ);
		try{
			QueryExecution qExec = QueryExecutionFactory.create(query,model,initialBinding);
			try{
				ResultSet results = qExec.execSelect();
				for(;results.hasNext();){
					QuerySolution soln = results.nextSolution();				
					RDFNode n = soln.get("prohibited");
					if( n.isResource() && !n.isAnon()){
						newProhibitedClasses.add(((Resource) n).getURI());
					}else{
						log.warn("unexpected node in object position for prohibited classes: " + n.toString());
					}
				}
			}catch(Throwable t){
				log.error(t,t);			
			}finally{ qExec.close(); }
		}finally{ model.leaveCriticalSection(); }
		
		return newProhibitedClasses;
	}

	private static enum ChangeType { ADD, REMOVE } ;
	
	class ProhibitedFromSearchChangeListener extends StatementListener {
		ProhibitedFromSearch pfs;
		
		ProhibitedFromSearchChangeListener(ProhibitedFromSearch pfs){
			this.pfs = pfs;
		}
		
		@Override
		public void addedStatement(Statement s) { processChange(s,ChangeType.ADD);}

		@Override
		public void removedStatement(Statement s) {	processChange(s,ChangeType.REMOVE); }
	
		private void processChange( Statement s, ChangeType add){
			//is it a change to an exclude class property?
			if( s != null && s.getPredicate() != null 
				&& s.getPredicate().getURI() != null 
				&& s.getPredicate().getURI().equals(DisplayVocabulary.EXCLUDE_CLASS.getURI())){
				
				//is it about this ProhibitedFromSearch?
				if( s.getSubject() != null ){
					String subURI = ((Resource) s.getSubject()).getURI() ;
					if( subURI != null && subURI.equals( ProhibitedFromSearchURI )){
						if( s.getObject() != null && s.getObject().canAs(Resource.class)){
							String classURI = ((Resource)s.getObject().as(Resource.class)).getURI();
							if( add == ChangeType.ADD )
								pfs.addProhibitedClass(classURI);
							else
								pfs.removeProhibitedClass(classURI);
						}
					}
				}
			}							
		}
		
	}
}
