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

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * This class is thread safe.  Notice that doAsyncIndexBuild() is frequently 
 * called because the inference system does not seem to send notifyEvents. 
 */
public class SearchReindexingListener implements ModelChangedListener {						
	private IndexBuilder indexBuilder;
	
	public SearchReindexingListener(IndexBuilder indexBuilder) {
		if(indexBuilder == null )
			throw new IllegalArgumentException("Constructor parameter indexBuilder must not be null");		
		this.indexBuilder = indexBuilder;		
	}	

	private synchronized void addChange(Statement stmt){	    
		if( stmt == null ) return;
		if( log.isDebugEnabled() ){
		    String sub="unknown";
		    String pred = "unknown";
		    String obj ="unknown";
		    
		    if( stmt.getSubject().isURIResource() ){           
	            sub =  stmt.getSubject().getURI();
	        }	                
		    if( stmt.getPredicate() != null ){
		        pred = stmt.getPredicate().getURI();
		    }
	        if( stmt.getObject().isURIResource() ){          
	            obj =  ((Resource) (stmt.getPredicate().as(Resource.class))).getURI();
	        }else{
	            obj = stmt.getObject().toString();
	        }
	        log.debug("changed statement: sub='" + sub + "' pred='" + pred +"' obj='" + obj + "'");
        }
		
		if( stmt.getSubject().isURIResource() ){			
			indexBuilder.addToChangedUris(stmt.getSubject().getURI());
			log.debug("subject: " + stmt.getSubject().getURI());
		}
				
		if( stmt.getObject().isURIResource() ){
			indexBuilder.addToChangedUris(((Resource) stmt.getObject()).getURI());			
		}	
	}

	private void requestAsyncIndexUpdate(){
		indexBuilder.doUpdateIndex();
	}	
	
	@Override
	public void notifyEvent(Model arg0, Object arg1) {
		if ( (arg1 instanceof EditEvent) ){
			EditEvent editEvent = (EditEvent)arg1;
			if( !editEvent.getBegin() ){// editEvent is the end of an edit				
				log.debug("Doing search index build at end of EditEvent");				
				requestAsyncIndexUpdate();
			}		
		} else{
			log.debug("ignoring event " + arg1.getClass().getName() + " "+ arg1 );
		}
	}
	
	@Override
	public void addedStatement(Statement stmt) {
		addChange(stmt);
		requestAsyncIndexUpdate();
	}

	@Override
	public void removedStatement(Statement stmt){
		addChange(stmt);
		requestAsyncIndexUpdate();
	}
	
	private static final Log log = LogFactory.getLog(SearchReindexingListener.class.getName());

	@Override
	public void addedStatements(Statement[] arg0) {
		for( Statement s: arg0){
			addChange(s);
		}
		requestAsyncIndexUpdate();
	}

	@Override
	public void addedStatements(List<Statement> arg0) {
		for( Statement s: arg0){
			addChange(s);
		}
		requestAsyncIndexUpdate();
	}

	@Override
	public void addedStatements(StmtIterator arg0) {
		try{
			while(arg0.hasNext()){
				Statement s = arg0.nextStatement();
				addChange(s);
			}
		}finally{
			arg0.close();
		}
		requestAsyncIndexUpdate();		
	}

	@Override
	public void addedStatements(Model m) {
		m.enterCriticalSection(Lock.READ);
		StmtIterator it = null;
		try{
			it = m.listStatements();
			while(it.hasNext()){
				addChange(it.nextStatement());
			}			
		}finally{
			if( it != null ) it.close();
			m.leaveCriticalSection();
		}
		requestAsyncIndexUpdate();
	}

	@Override
	public void removedStatements(Statement[] arg0) {
		//same as add stmts
		this.addedStatements(arg0);		
	}

	@Override
	public void removedStatements(List<Statement> arg0) {
		//same as add
		this.addedStatements(arg0);		
	}

	@Override
	public void removedStatements(StmtIterator arg0) {
		//same as add
		this.addedStatements(arg0);
	}

	@Override
	public void removedStatements(Model arg0) {
		//same as add
		this.addedStatements(arg0);
	}
}
