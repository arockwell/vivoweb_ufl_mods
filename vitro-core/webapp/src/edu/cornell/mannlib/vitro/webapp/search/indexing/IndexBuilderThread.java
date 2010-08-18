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

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread that executes the methods in IndexBuilder.  
 * 
 * @author bdc34
 *
 */
public class IndexBuilderThread extends Thread{
	private IndexBuilder indexBuilder;
	protected boolean stopRequested = false;
	protected long reindexInterval = 1000 * 60 /* msec */ ;
	
	private static final Log log = LogFactory.getLog(IndexBuilderThread.class.getName());
	
	public IndexBuilderThread(IndexBuilder ib){
		super("IndexBuilderThread");
		this.indexBuilder = ib;
	}
	
	@Override
	public void run() {
		while(true){
			if( stopRequested ){
				log.info("Stopping IndexBuilderThread ");		
				return;
			}
			
			try{
				if( indexBuilder.isReindexRequested() ){
					log.debug("full re-index requested");
					indexBuilder.indexRebuild();
				}else{
					if( indexBuilder != null && indexBuilder.isThereWorkToDo() ){						
						Thread.sleep(250); //wait a bit to let a bit more work to come into the queue
						log.debug("work found for IndexBuilder, starting update");
						indexBuilder.updatedIndex();
					}
				}
			}catch (Throwable e) {
				log.error(e,e);
			}
			
			if( indexBuilder != null && ! indexBuilder.isThereWorkToDo() ){
				log.debug("there is no indexing working to do, going to sleep");
				try {
					synchronized (this) {
						this.wait(reindexInterval);	
					}			
				} catch (InterruptedException e) {
					log.debug(" woken up",e);
				}
			}
		}
	}

	public synchronized void kill(){
		log.debug("Attempting to kill IndexBuilderThread ");
		stopRequested = true;
		this.notifyAll();
	}
}
