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

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing.jena;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class NamespacesListingController extends BaseEditController {

    public void doGet(HttpServletRequest request, HttpServletResponse response) {    	
        VitroRequest vrequest = new VitroRequest(request);
        Portal portal = vrequest.getPortal();

        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request, response);
        } catch (Throwable t) {
            t.printStackTrace();
        }
               
        LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");

        OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
               
        ArrayList results = new ArrayList();
        request.setAttribute("results",results);
        results.add("XX");
        results.add("namespace");
        results.add("prefix");
        
        Property namespaceURIProp = ontModel.getProperty(VitroVocabulary.NAMESPACE_NAMESPACEURI);
        
        ontModel.enterCriticalSection(Lock.READ);
        try {
        	ClosableIterator closeIt = ontModel.listIndividuals(ontModel.getResource(VitroVocabulary.NAMESPACE));
        	try {
        		for (Iterator namespaceIt=closeIt; namespaceIt.hasNext();) {
        			Individual namespaceInd = (Individual) namespaceIt.next();
        			
        			String namespaceURI = "";
        			try {
        				namespaceURI = ((Literal)namespaceInd.getPropertyValue(namespaceURIProp)).getLexicalForm(); 
        			} catch (Exception e) { /* ignore it for now */ }
        			results.add("XX");
        			results.add(namespaceURI);
        			RDFNode prefixMapping = namespaceInd.getPropertyValue(ontModel.getProperty(VitroVocabulary.NAMESPACE_HASPREFIXMAPPING));
        			boolean prefixFound = false;
        			if ( (prefixMapping != null) && (prefixMapping.canAs(Individual.class)) ) {
        				Individual prefixMappingInd = (Individual) prefixMapping.as(Individual.class);
        				RDFNode prefixNode = prefixMappingInd.getPropertyValue(ontModel.getProperty(VitroVocabulary.NAMESPACE_PREFIX));
        				if ( (prefixNode != null) && prefixNode.isLiteral() ) {
        					prefixFound = true;
        					try {
        						results.add("<a href=\"editForm?controller=NamespacePrefix&amp;prefix="+((Literal)prefixNode).getLexicalForm()+"&amp;namespace="+URLEncoder.encode(namespaceURI,"UTF-8")+"\">"+((Literal)prefixNode).getLexicalForm()+"</a>");
        					} catch (Exception e) {
        						//
        					}
        				}
        			}
        			if (!prefixFound) {
        				try {
        					results.add("<a href=\"editForm?controller=NamespacePrefix&amp;namespace="+URLEncoder.encode(namespaceURI,"UTF-8")+"\">add prefix</a>");
        				} catch (Exception e) {
        					//
        				}
        			}
        		}
        	} finally {
        		closeIt.close();
        	}
        } finally {
           	ontModel.leaveCriticalSection();
        }

        request.setAttribute("columncount",new Integer(3));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Recognized Namespaces");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("home", portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
}
