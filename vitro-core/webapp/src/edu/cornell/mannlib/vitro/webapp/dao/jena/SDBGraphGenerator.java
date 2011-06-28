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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import java.sql.Connection;
import java.sql.SQLException;

public class SDBGraphGenerator implements GraphGenerator {

	private static final Log log = LogFactory.getLog(SDBGraphGenerator.class.getName());
	
    private BasicDataSource ds;
    private Connection connection;
    private StoreDesc storeDesc;
    private String graphID;
	
    public SDBGraphGenerator(BasicDataSource dataSource, StoreDesc storeDesc,
    							String graphID) {
    	this.ds = dataSource;
    	this.storeDesc = storeDesc;
    	this.graphID = graphID;
    }

    public Graph generateGraph() {
        try {
            if ( this.connection == null ) {
                this.connection = ds.getConnection();
            } else if ( this.connection.isClosed() ) {
                try {
                    this.connection.close();
                } catch (SQLException e) {                  
                    // The connection will throw an "Already closed"
                    // SQLException that we need to catch.  We need to 
                    // make this extra call to .close() in order to make
                    // sure that the connection is returned to the pool.
                    // This depends on the particular behavior of version
                    // 1.4 of the Apache Commons connection pool library.
                    // Earlier versions threw the exception right away,
                    // making this impossible. Future versions may do the
                    // same.
                }
                this.connection = ds.getConnection();
            }
            Store store = SDBFactory.connectStore(connection, storeDesc);
            return SDBFactory.connectNamedGraph(store, graphID); 
        } catch (SQLException e) {
            String errMsg = "Unable to generate SDB graph"; 
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }
    	
	public Connection getConnection() {
		return connection;
	}

}
