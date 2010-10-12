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

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class ApplicationDaoJena extends JenaBaseDao implements ApplicationDao {

	Integer portalCount = null;
	List<String> externallyLinkedNamespaces = null;
	
    public ApplicationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
	   
	
	public boolean isFlag1Active() {
		boolean somePortalIsFiltering=false;		
		if (portalCount == null) {
			boolean active = false;
			for (Portal p : getWebappDaoFactory().getPortalDao().getAllPortals()) {
				if (p.isFlag1Filtering()) {
					somePortalIsFiltering = true;
				}
			}
		}		
		return somePortalIsFiltering && getWebappDaoFactory().getPortalDao().getAllPortals().size() > 1;		
	}

	
	public boolean isFlag2Active() {
		return (getFlag2ValueMap().isEmpty()) ? false : true;
	}


    public List<String> getExternallyLinkedNamespaces() {
        if (externallyLinkedNamespaces == null) {            
            externallyLinkedNamespaces = new ArrayList<String>();
            OntModel ontModel = getOntModelSelector().getDisplayModel();
            Property linkedNamespaceProp = ontModel.getProperty(VitroVocabulary.DISPLAY + "linkedNamespace");
            NodeIterator nodes = ontModel.listObjectsOfProperty(linkedNamespaceProp);
            while (nodes.hasNext()) {
                RDFNode node = nodes.next();
                if (node.isLiteral()) {
                    String namespace = ((Literal)node).getLexicalForm();
                    // org.openrdf.model.impl.URIImpl.URIImpl.getNamespace() returns a 
                    // namespace with a final slash, so this makes matching easier.
                    // It also accords with the way the default namespace is defined.
                    if (!namespace.endsWith("/")) {
                        namespace = namespace + "/";
                    }
                    externallyLinkedNamespaces.add(namespace);
                }
            }
        }
        return externallyLinkedNamespaces;
    }
    
}
