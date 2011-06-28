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

package edu.cornell.mannlib.vitro.webapp.search.lucene;

import java.io.File;
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
	String baseIndexDirName = null;
	
	private static final Log log = LogFactory.getLog(LuceneIndexFactory.class.getName());

	public static final String LUCENE_INDEX_FACTORY= "LuceneIndexFactory";
	
	public LuceneIndexFactory(String baseIndexDirName){
	    this.baseIndexDirName = baseIndexDirName;
	}
	
	/**
	 * Get a lucene IndexSearch. This may return null.
	 */
    public static IndexSearcher getIndexSearcher( ServletContext context){    	
    	return getLuceneIndexFactoryFromContext(context).innerGetIndexSearcher(context);    	
    }
    
    protected static LuceneIndexFactory getLuceneIndexFactoryFromContext(ServletContext context){
        Object obj = context.getAttribute(LUCENE_INDEX_FACTORY);        
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
    
    
    public static LuceneIndexFactory setup(ServletContext context, String baseIndexDirName){
        LuceneIndexFactory lif = (LuceneIndexFactory)context.getAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY);
        if( lif == null ){
            lif = new LuceneIndexFactory(baseIndexDirName);
            context.setAttribute(LuceneIndexFactory.LUCENE_INDEX_FACTORY, lif);
        }   
        return lif;        
    }   
        
    /**
     * This method can be used to force the LuceneIndexFactory to return a new IndexSearcher.
     * This will force a re-opening of the search index.
     * 
     * This could be useful if the index was rebult in a different directory on the file system.
     */
    public synchronized void forceNewIndexSearcher(){
        log.debug("forcing the re-opening of the search index");
        IndexSearcher oldSearcher = searcher;
        
        
        searcher = null;
    }
    
    protected synchronized void forceClose(){
        log.debug("forcing the closing of the search index");
        try {
            if( searcher != null )
                searcher.close();
        } catch (IOException e) {
            log.error("could not close lucene searcher: " + e.getMessage());
        }
        searcher = null;
    }
    
	private synchronized IndexSearcher innerGetIndexSearcher(ServletContext context) {
		if (searcher == null ) {	    
			String liveDir = getLiveIndexDir( context );
			if( liveDir != null ){
				try {
					Directory fsDir = FSDirectory.getDirectory(liveDir);
					searcher = new IndexSearcher(fsDir);
				} catch (IOException e) {
				    String base = getBaseIndexDir();
					log.error("could not make IndexSearcher " + e);
					log.error("It is likely that you have not made a directory for the lucene index.  "
								+ "Create the directory " + base + " and set permissions/ownership so"
								+ " that the tomcat process can read and write to it.");
				}		
			}else{
			    log.error("Could not create IndexSearcher because index directory was null. It may be that the LucenSetup.indexDir is " +
			    		" not set in your deploy.properties file.");
			}
	    }		
		return searcher;
	}
		
	protected String getBaseIndexDir(){
	    if( this.baseIndexDirName == null )
	        log.error("LucenIndexFactory was not setup correctly, it must have a value for baseIndexDir");
		return this.baseIndexDirName;
	}
	
	protected String getLiveIndexDir(ServletContext servletContext){
	    String base = getBaseIndexDir();
	    if( base == null )
	        return null;
	    else
	        return base + File.separator + "live";	    
	}
	
	
		
}
