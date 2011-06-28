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

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Attempts to pull a NetId and a SelfEditing identifier from the externally
 * authorized username.
 */
public class SelfEditingIdentifierFactory implements IdentifierBundleFactory {
	private static final Log log = LogFactory.getLog(SelfEditingIdentifierFactory.class);
	
	private static final int MAXIMUM_USERNAME_LENGTH = 100;
	
	public IdentifierBundle getIdentifierBundle(ServletRequest request,
			HttpSession session, ServletContext context) {
		if (!(request instanceof HttpServletRequest)) {
			log.debug("request is null or not an HttpServletRequest");
			return null;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		log.debug("request is for " + req.getRequestURI());
		
		NetId netId = figureNetId(req);
		SelfEditing selfId = figureSelfEditingId(req);
		
		return buildIdentifierBundle(netId, selfId);
	}

	/**
	 * If the user is externally authorized, create a NetId identifier.
	 */
	private NetId figureNetId(HttpServletRequest req) {
		LoginStatusBean bean = LoginStatusBean.getBean(req);
		String username = bean.getUsername();
		
		if (!bean.isLoggedIn()) {
			log.debug("No NetId: not logged in.");
			return null;
		}
		
		if (isEmpty(username)) {
			log.debug("No NetId: username is empty.");
			return null;
		}
		
		if (!bean.hasExternalAuthentication()) {
			log.debug("No NetId: user '" + bean.getUsername() +
			"' did not use external authentication.");
			return null;
		}
		
		if (username.length() > MAXIMUM_USERNAME_LENGTH) {
			log.info("The external username is longer than " + MAXIMUM_USERNAME_LENGTH
					+ " chars; this may be a malicious request");
			return null;
		}
		
		return new NetId(username);
	}

	/**
	 * If the authorized username is associated with an Individual in the model,
	 * create a SelfEditing identifier.
	 */
	private SelfEditing figureSelfEditingId(HttpServletRequest req) {
		LoginStatusBean bean = LoginStatusBean.getBean(req);
		String username = bean.getUsername();

		if (!bean.isLoggedIn()) {
			log.debug("No SelfEditing: not logged in.");
			return null;
		}

		if (isEmpty(username)) {
			log.debug("No SelfEditing: username is empty.");
			return null;
		}

		HttpSession session = req.getSession(false);
		if (session == null) {
			log.debug("No SelfEditing: session is null.");
			return null;
		}

		ServletContext context = session.getServletContext();
		WebappDaoFactory wdf = (WebappDaoFactory) context
				.getAttribute("webappDaoFactory");
		if (wdf == null) {
			log.error("Could not get a WebappDaoFactory from the ServletContext");
			return null;
		}

		IndividualDao indDao = wdf.getIndividualDao();

		SelfEditingConfiguration sec = SelfEditingConfiguration.getBean(req);
		String uri = sec.getIndividualUriFromUsername(indDao, username);
		if (uri == null) {
			log.debug("Could not find an Individual with a netId of "
					+ username);
			return null;
		}

		Individual ind = indDao.getIndividualByURI(uri);
		if (ind == null) {
			log.warn("Found a URI for the netId " + username
					+ " but could not build Individual");
			return null;
		}

		log.debug("Found an Individual for netId " + username + " URI: " + uri);
		String blacklisted = checkForBlacklisted(ind, context);
		return new SelfEditing(ind, blacklisted, false);
	}

	/**
	 * Create a bundle that holds the identifiers we created, or null if we
	 * didn't create any.
	 */
	private IdentifierBundle buildIdentifierBundle(NetId netId,
			SelfEditing selfId) {
		if (netId == null && selfId == null) {
			log.debug("no self-editing IDs in the session");
			return null;
		}

		IdentifierBundle idb = new ArrayIdentifierBundle();
		if (netId != null) {
			idb.add(netId);
			log.debug("added NetId from session: " + netId);
		}
		if (selfId != null) {
			idb.add(selfId);
			log.debug("added SelfEditing from Session: " + selfId);
		}
		return idb;
	}

	private boolean isEmpty(String string) {
		return (string == null || string.isEmpty());
	}

	// ----------------------------------------------------------------------
	// static utility methods
	// ----------------------------------------------------------------------
	
    public static final String NOT_BLACKLISTED = null;   
    private final static String BLACKLIST_SPARQL_DIR = "/admin/selfEditBlacklist";

    /**
     * Runs through .sparql files in the BLACKLIST_SPARQL_DIR, the first that returns one
     * or more rows will be cause the user to be blacklisted.  The first variable from
     * the first solution set will be returned.   
     */
    public static String checkForBlacklisted(Individual ind, ServletContext context) {
        if( ind == null || context == null ) {
            log.error("could not check for Blacklist, null individual or context");
            return NOT_BLACKLISTED;
        }        
        String realPath = context.getRealPath(BLACKLIST_SPARQL_DIR);        
        File blacklistDir = new File(realPath );        
        if( !blacklistDir.exists()){
            log.debug("could not find blacklist directory " + realPath);
            return NOT_BLACKLISTED;
        }
        if( ! blacklistDir.canRead() || ! blacklistDir.isDirectory() ){
            log.debug("cannot read blacklist directory " + realPath);
            return NOT_BLACKLISTED;            
        }

        log.debug("checking directlry " + realPath + " for blacklisting sparql query files");
        File[] files = blacklistDir.listFiles(new FileFilter(){
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".sparql");
            }}
        );

        String reasonForBlacklist = NOT_BLACKLISTED;
        for( File file : files ){
            try{
                reasonForBlacklist = runSparqlFileForBlacklist( file, ind, context);
                if( reasonForBlacklist != NOT_BLACKLISTED ) 
                    break;
            }catch(RuntimeException ex){
                log.error("Could not run blacklist check query for file " +
                        file.getAbsolutePath() + File.separatorChar + file.getName(),
                        ex);                
            }
        }
        return reasonForBlacklist;           
    }

    /**
     * Runs the SPARQL query in the file with the uri of the individual 
     * substituted in.  If there are any solution sets, then the URI of
     * the variable named "cause" will be returned, make sure that it is a 
     * resource with a URI. Otherwise null will be returned.
     * The URI of ind will be substituted into the query where ever the
     * token "?individualURI" is found.
     */
    private static String runSparqlFileForBlacklist
        (File file, Individual ind, ServletContext context) 
    {
        if( !file.canRead() ){
            log.debug("cannot read blacklisting SPARQL file " + file.getName());
            return NOT_BLACKLISTED;
        }
        String queryString = null;
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);            
            byte b[]= new byte[fis.available()];
            fis.read(b);
            queryString = new String(b);            
        }catch( FileNotFoundException fnfe){
            log.debug(fnfe);
            return NOT_BLACKLISTED;
        }catch( IOException ioe){
            log.debug(ioe);
            return NOT_BLACKLISTED;
        }finally{
            try {
                fis.close();
            } catch (IOException e) {
              log.warn("could not close file", e);
            }
        }
        
        if( queryString == null || queryString.length() == 0 ){
            log.debug(file.getName() + " is empty");
            return NOT_BLACKLISTED;            
        }
       Model model = (Model)context.getAttribute("jenaOntModel");
       // VitroRequest request = new VitroRequest(req);	
       // Dataset dataset = request.getDataset();
        
        queryString = queryString.replaceAll("\\?individualURI", "<" + ind.getURI() + ">");
        log.debug(queryString);
        Query query = QueryFactory.create(queryString);        
        QueryExecution qexec = QueryExecutionFactory.create(query,model);
        try{
            ResultSet results = qexec.execSelect();
            while(results.hasNext()){
                QuerySolution solution = results.nextSolution();
                if( solution.contains("cause") ){      
                    RDFNode node = solution.get("cause");
                    if( node.canAs( Resource.class ) ){
                        Resource x = solution.getResource("cause");                     
                        return x.getURI();
                    }else if( node.canAs(Literal.class)){
                        Literal x = (Literal)node.as(Literal.class);
                        return x.getString();
                    }
                }else{
                    log.error("Query solution must contain a variable \"cause\" of type Resource or Literal.");
                    return null;
                }
            }
        }finally{ qexec.close(); }    
        return null;
    }

    public static SelfEditing getSelfEditingIdentifier( IdentifierBundle whoToAuth ){
        if( whoToAuth == null ) return null;        
        for(Identifier id : whoToAuth){
            if (id instanceof SelfEditing) 
                return (SelfEditing)id;                           
        }
        return null;
    }
    
    public static String getSelfEditingUri( IdentifierBundle whoToAuth){
        SelfEditing sid = getSelfEditingIdentifier(whoToAuth);
        if( sid != null )
            return sid.getValue();
        else
            return null;
    }

    // ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------
    
    public static class NetId implements Identifier{
        public final String value;
        public NetId(String value){
            this.value = value;
        }
        public String getValue(){return value;}
        public String toString(){ return "NetID: " + value;}
    }
    
    
    /**
     * An identifier with the Individual that represents the human self-editor. 
     */
    public static class SelfEditing implements Identifier{        
        final Individual individual;
        final String blacklisted;        
        final boolean faked; //if this is true it was setup by FakeSeflEditingIdentifierFactory 
                        
        public SelfEditing ( Individual individual, String blacklisted ){
          this(individual,blacklisted,false);   
        }
        
        public SelfEditing ( Individual individual, String blacklisted, boolean faked){
            if( individual == null )
                throw new IllegalArgumentException("Individual must not be null");            
            this.individual = individual;
            this.blacklisted = blacklisted;            
            this.faked = faked;
        }
        public String getValue(){
            return individual.getURI();
        }
        public Individual getIndividual(){
            return individual;
        }
        public String getBlacklisted(){
            return blacklisted;
        }
        public String toString(){
            return "SelfEditing as " + getValue() +
            (getBlacklisted()!=null?  " blacklisted via " + getBlacklisted():"");
        }
        public boolean isFake() {
            return faked;
        }
    }
    
}
