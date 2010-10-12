/*
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
*/

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cornell.mannlib.vitro.webapp.search.SearchException;

public class LuceneIndexFactory {
    
	IndexSearcher searcher = null;	
	private static final Log log = LogFactory.getLog(LuceneIndexFactory.class.getName());

	public static final String LUCENE_INDEX_FACTORY= "LuceneIndexFactory";
	
	/**
	 * Get a lucene IndexSearch. This may return null.
	 */
    public static IndexSearcher getIndexSearcher( ServletContext context){    	
    	return getLuceneIndexFactoryFromContext(context).innerGetIndexSearcher(context);    	
    }
    
    public static LuceneIndexFactory getLuceneIndexFactoryFromContext(ServletContext context){
        Object obj = context.getAttribute(LUCENE_INDEX_FACTORY);
        if( obj == null ){
            setup(context);
            obj = context.getAttribute(LUCENE_INDEX_FACTORY);
        }
        if( obj == null ){
            log.error("cannot get LuceneIndexFactory from context.  Search is not setup correctly");
            return null;
        }
        if( ! (obj instanceof LuceneIndexFactory)){
            log.error("LuceneIndexFactory in context was not of correct type. Expected " + LuceneIndexFactory.class.getName() 
                    + " found " + obj.getClass().getName() + " Search is not setup correctly");
            return null;
        }        
        return (LuceneIndexFactory)obj;
    }
    
    
    public static void setup(ServletContext context){
        LuceneIndexFactory lif = (LuceneIndexFactory)context.getAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY);
        if( lif == null ){
            context.setAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY, new LuceneIndexFactory());
        }   
    }   
        
    /**
     * This method can be used to force the LuceneIndexFactory to return a new IndexSearcher.
     * This will force a re-opening of the search index.
     * 
     * This could be useful if the index was rebult in a different directory on the file system.
     */
    public synchronized void forceNewIndexSearcher(){
        log.debug("forcing the re-opening of the search index");
        searcher = null;
    }
    
	private synchronized IndexSearcher innerGetIndexSearcher(ServletContext context) {
		if (searcher == null ) {	    
			String indexDir = getIndexDir( context );
			if( indexDir != null ){
				try {
					Directory fsDir = FSDirectory.getDirectory(indexDir);
					searcher = new IndexSearcher(fsDir);
				} catch (IOException e) {
					log.error("could not make indexSearcher " + e);
					log.error("It is likely that you have not made a directory for the lucene index.  "
								+ "Create the directory indicated in the error and set permissions/ownership so"
								+ " that the tomcat server can read and write to it.");
				}		
			}else{
			    log.error("Could not create IndexSearcher because index directory was null. It may be that the LucenSetup.indexDir is " +
			    		" not set in your deploy.properties file.");
			}
	    }		
		return searcher;
	}
		
	private String getIndexDir(ServletContext servletContext){
		Object obj = servletContext.getAttribute(LuceneSetup.INDEX_DIR);
		if (obj == null ){
			log.error("could not find " + LuceneSetup.INDEX_DIR + " in context. Search is not configured correctly.");
			return null;
		}else if ( !(obj instanceof String) ){
			log.error( LuceneSetup.INDEX_DIR + " from context was not a String. Search is not configured correctly.");
			return null;
		}else
			return (String) obj;
	}
		
}
