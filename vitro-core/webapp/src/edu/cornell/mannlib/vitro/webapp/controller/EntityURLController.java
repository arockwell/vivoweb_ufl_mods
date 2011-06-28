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
package edu.cornell.mannlib.vitro.webapp.controller;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.Classes2ClassesDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.search.lucene.Entity2LuceneDoc;
import edu.cornell.mannlib.vitro.webapp.search.lucene.LuceneIndexFactory;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public class EntityURLController extends VitroHttpServlet {
    
    private static final Log log = LogFactory.getLog(EntityURLController.class.getName());
    public static final int ENTITY_LIST_CONTROLLER_MAX_RESULTS = 30000;
     
    public void doGet (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
         
        String classUri = req.getParameter("vclass");
        BooleanQuery query = new BooleanQuery();
        query.add(
                 new TermQuery( new Term(Entity2LuceneDoc.term.RDFTYPE, classUri)),
                 BooleanClause.Occur.MUST );     
         
        IndexSearcher index = LuceneIndexFactory.getIndexSearcher(getServletContext());
        TopDocs docs = index.search(query, null, 
                 ENTITY_LIST_CONTROLLER_MAX_RESULTS, 
                 new Sort(Entity2LuceneDoc.term.NAMELOWERCASE));   
         
        if( docs == null ){
            log.error("Search of lucene index returned null");
            throw new ServletException("Search of lucene index returned null");
        }
         
        int ii = 0;
        int size = docs.totalHits;
        Resource resource = null;
        RDFNode node = null;
        Model model = ModelFactory.createDefaultModel();
        while( ii < size ){
            ScoreDoc hit = docs.scoreDocs[ii];
            if (hit != null) {
                Document doc = index.doc(hit.doc);
                if (doc != null) {                                                                                        
                    String uri = doc.getField(Entity2LuceneDoc.term.URI).stringValue();
                    resource = ResourceFactory.createResource(uri);
                    node = (RDFNode) ResourceFactory.createResource(classUri);
                    model.add(resource, RDF.type, node);
                } else {
                    log.warn("no document found for lucene doc id " + hit.doc);
                }
            } else {
                log.debug("hit was null");
            }                         
            ii++;            
        }   
      
        res.setContentType(RDFXML_MIMETYPE);
        model.write(res.getOutputStream(), "RDF/XML");
    }
    
    public void doPost (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
        doGet(req,res);
    }
    

}
