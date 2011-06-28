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

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

/**
 * This will attempt to run System.gc() once a night. 
 * 
 * @author bdc34
 *
 */
public class NightlyDefragement implements ServletContextListener, Runnable {
    
    private static NightlyDefragement nightlyDefragement = null;
    private static boolean stop = false;
    private static final Log log = LogFactory.getLog(NightlyDefragement.class);
            
    protected DateTime lastRun;
    
    @Override
    public void run() {
        while( ! stop ){
            DateTime now = new DateTime();
            
            if( now.hourOfDay().get() > 0 
                 && now.hourOfDay().get() < 2 
                 && lastRun.isBefore( now.minusHours(22) ) ){ 
                
                log.info("running defragement");
                long start = System.currentTimeMillis();
                System.gc();
                log.info("Finished defragement, " + (start - System.currentTimeMillis()) + "msec");
                lastRun = now;
            }
            
            try{       
                synchronized( nightlyDefragement ){
                    this.wait(30*60*1000); //30 min;
                }
            }catch( InterruptedException ex){
                log.debug("woken up");                
            }
        }  
        log.info(" Stopping NightlyDefragement thread.");
    }

    
    public void stopNicely(){
        stop = true;
        synchronized( nightlyDefragement ){
            nightlyDefragement.notifyAll();
        }
    }
    
    
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        
        if (AbortStartup.isStartupAborted(arg0.getServletContext())) {
            return;
        }
        
        lastRun = new DateTime().minusHours( 400 );
        if( nightlyDefragement != null ){
            log.warn("NightlyDefragement listener has already been setup. Check your web.xml for duplicate listeners.");            
        }else{        
            nightlyDefragement = this;
            Thread thread = new Thread(this , "nightlyDefragementThread");                
            thread.start();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        nightlyDefragement.stopNicely();
    }

    
}
