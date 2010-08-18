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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.ProfileException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class OntologyDaoJena extends JenaBaseDao implements OntologyDao {

    public OntologyDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    // TODO: add model-per-ontology support
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }
    
    public static synchronized String adjustOntologyURI(String ontologyURI) { 
    	if ( (ontologyURI.length()>1) && (ontologyURI.charAt(ontologyURI.length()-1)=='#') ) { 
    		return ontologyURI.substring(0,ontologyURI.length()-1);
    	} else {
    		return ontologyURI;
    	}
    }
    
    private com.hp.hpl.jena.ontology.Ontology getOntology(String ontologyURI, OntModel ontModel) {
    	try {
    		ontModel.enterCriticalSection(Lock.READ);
    		return ontModel.getOntology(adjustOntologyURI(ontologyURI));
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }

    public void deleteOntology(Ontology ontology) {
    	deleteOntology(ontology,getOntModel());
    }

    public void deleteOntology(Ontology ontology, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            com.hp.hpl.jena.ontology.Ontology o = getOntology(ontology.getURI(),ontModel);
            if (o == null) {
                o = ontModel.getOntology(adjustOntologyURI(ontology.getURI()));
            }
            if (o != null) {
                o.remove();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public List<Ontology> getAllOntologies() {
        List<Ontology> ontologies = new ArrayList<Ontology>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator ontIt = getOntModel().listOntologies();
            try {
                while (ontIt.hasNext()) {
                    OntResource ontRes = (OntResource) ontIt.next();
                    if (!(NONUSER_NAMESPACES.contains(ontRes.getURI()) || NONUSER_NAMESPACES.contains(ontRes.getURI()+"#")))
                        ontologies.add(ontologyFromOntologyResource(ontRes));
                }
            } finally {
                ontIt.close();
            }
        } catch (ProfileException e) {
        	// The current profile does not support listing ontology objects, so we will return an empty list
        } finally {
            getOntModel().leaveCriticalSection();
        }
        Collections.sort(ontologies);
        return (ontologies.size()>0) ? ontologies : null;
    }
    
    public Ontology getOntologyByURI(String ontologyURI) {
    	Ontology o = null;
    	try {
    		o = ontologyFromOntologyResource(getOntology(ontologyURI,getOntModel()));	
    	} catch (Exception e) {}
    	if (o == null) {
	        try {
	        	o = ontologyFromOntologyResource(getOntology(adjustOntologyURI(ontologyURI),getOntModel()));
	        } catch (Exception e) {}
    	}
        return o;
    }

    public String insertNewOntology(Ontology ontology) {
    	return insertNewOntology(ontology,getOntModel());
    }

    public String insertNewOntology(Ontology ontology, OntModel ontModel) {
        if (ontology != null && ontology.getURI() != null && ontology.getURI().length()>0) {
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                com.hp.hpl.jena.ontology.Ontology o = ontModel.createOntology(adjustOntologyURI(ontology.getURI()));
                if (ontology.getName() != null && ontology.getName().length()>0) {
                    o.setLabel(ontology.getName(), PREFERRED_LANGUAGES[0]);
                }
                if (ontology.getPrefix() != null && ontology.getPrefix().length()>0) {
                    addPropertyStringValue(o,ONTOLOGY_PREFIX_ANNOT,ontology.getPrefix(),ontModel);
                }
                return o.getURI();
            } finally {
                ontModel.leaveCriticalSection();
            }
        } else {
            return null;
        }
    }

    public void updateOntology(Ontology ontology) {
    	updateOntology(ontology,getOntModel());
    }
    
    public void updateOntology(Ontology ontology, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        if (ontology != null && ontology.getURI() != null && ontology.getURI().length()>0) {
            try {
                com.hp.hpl.jena.ontology.Ontology o = getOntology(ontology.getURI(),ontModel);
                if (o == null) {
                    log.error("OntologyDaoJena.updateOntology() could not find ontology "+ontology.getURI()+" in Jena model");
                } else {
                    if (ontology.getName() != null && ontology.getName().length()>0) {
                        o.setLabel(ontology.getName(), PREFERRED_LANGUAGES[0]);
                    }
                    if (ontology.getPrefix() != null && ontology.getPrefix().length()>0) {
                        updatePropertyStringValue(o,ONTOLOGY_PREFIX_ANNOT,ontology.getPrefix(),ontModel);
                    }
                }
            } finally {
                ontModel.leaveCriticalSection();
            }
        }
    }

    private Ontology ontologyFromOntologyResource(OntResource ontRes) {
        if (ontRes == null)
            return null;
        Ontology ontology = new Ontology();
        ontology.setName(getLabelOrId(ontRes));
        ontology.setURI(ontRes.getURI());
        ontology.setPrefix(getPropertyStringValue(ontRes,ONTOLOGY_PREFIX_ANNOT));
        
        // we need this for the time being because other things are expecting getAllOntologies() to return objects with trailing fragment separators
        // TODO: improve this so '#' is only appended if the last character is not an XML name character
        if (!(ontology.getURI().substring(ontology.getURI().length()-1,ontology.getURI().length()).equals("#") ||
                ontology.getURI().substring(ontology.getURI().length()-1,ontology.getURI().length()).equals("/")
            )) {
            ontology.setURI(ontology.getURI()+"#");
        }
        
        return ontology;
    }


}
