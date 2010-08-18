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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

public class PropertyDaoJena extends JenaBaseDao implements PropertyDao {
	
	protected static final Log log = LogFactory.getLog(PropertyDaoJena.class.getName());
	
    public PropertyDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }

	public void addSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	addSuperproperty(property.getURI(),superproperty.getURI());
    }
    
    public void addSuperproperty(String propertyURI, String superpropertyURI) {
		getOntModel().enterCriticalSection(Lock.WRITE);
		try {
			getOntModel().add(getOntModel().getResource(propertyURI),RDFS.subPropertyOf,getOntModel().getResource(superpropertyURI));
		} finally {
			getOntModel().leaveCriticalSection();
		}
	}
    
    public void removeSuperproperty(ObjectProperty property, ObjectProperty superproperty) {
    	removeSuperproperty(property.getURI(),superproperty.getURI());
    }
    
    public void removeSuperproperty(String propertyURI, String superpropertyURI) {
    	getOntModel().enterCriticalSection(Lock.WRITE);
    	try {
    		if (getOntModel().contains(getOntModel().getResource(propertyURI),RDFS.subPropertyOf,getOntModel().getResource(superpropertyURI))) {
    			getOntModel().remove(getOntModel().getResource(propertyURI),RDFS.subPropertyOf,getOntModel().getResource(superpropertyURI));
    		}
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    }
    
    public void addSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	addSuperproperty(subproperty, property);
    }
    
    public void addSubproperty(String propertyURI, String subpropertyURI) {
    	addSuperproperty(subpropertyURI, propertyURI);
    }
    
    public void removeSubproperty(ObjectProperty property, ObjectProperty subproperty) {
    	removeSuperproperty(subproperty, property);
    }
    
    public void removeSubproperty(String propertyURI, String subpropertyURI) {
    	removeSuperproperty(subpropertyURI,propertyURI);
    }
    
    public List <String> getSubPropertyURIs(String propertyURI) {
    	List<String> subURIs = new LinkedList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
            Iterator subIt = getOntModel().getOntProperty(propertyURI).listSubProperties(true);
            while (subIt.hasNext()) {
                try {
                    OntProperty prop = (OntProperty) subIt.next();
                    subURIs.add(prop.getURI());
                } catch (Exception cce) {}
            }
	    } catch (Exception e) {
	    	log.error(e); 
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return subURIs;
    }

    private void getAllSubPropertyURIs(String propertyURI, HashSet<String> subtree){
        List<String> directSubproperties = getSubPropertyURIs(propertyURI);     
        Iterator<String> it=directSubproperties.iterator();
        while(it.hasNext()){
            String uri = (String)it.next();
            if (!subtree.contains(uri)) {
            	subtree.add(uri);
            	getAllSubPropertyURIs(uri,subtree);
            }
        }
    }

    public List<String> getAllSubPropertyURIs(String propertyURI) {
    	HashSet<String> nodeSet = new HashSet<String>();
    	nodeSet.add(propertyURI);
    	getAllSubPropertyURIs(propertyURI, nodeSet);
    	nodeSet.remove(propertyURI);
    	List<String> outputList = new LinkedList<String>();
    	outputList.addAll(nodeSet);
    	return outputList;
    }
    
    public List <String> getSuperPropertyURIs(String propertyURI, boolean direct) {
       	List<String> supURIs = new LinkedList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
            Iterator supIt = getOntModel().getOntProperty(propertyURI).listSuperProperties(direct);
            while (supIt.hasNext()) {
                try {
                    OntProperty prop = (OntProperty) supIt.next();
                    supURIs.add(prop.getURI());
                } catch (Exception cce) {}
            }
	    } catch (Exception e) {
	    	log.error(e); 
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return supURIs;
    }

    private void getAllSuperPropertyURIs(String propertyURI, HashSet<String> subtree){
        List<String> directSuperproperties = getSuperPropertyURIs(propertyURI,true);     
        Iterator<String> it=directSuperproperties.iterator();
        while(it.hasNext()){
            String uri = (String)it.next();
            if (!subtree.contains(uri)) {
            	subtree.add(uri);
            	getAllSuperPropertyURIs(uri,subtree);
            }
        }
    }

    public List<String> getAllSuperPropertyURIs(String propertyURI) {
    	HashSet<String> nodeSet = new HashSet<String>();
    	nodeSet.add(propertyURI);
    	getAllSuperPropertyURIs(propertyURI, nodeSet);
    	nodeSet.remove(propertyURI);
    	List<String> outputList = new LinkedList<String>();
    	outputList.addAll(nodeSet);
    	return outputList;
    }

	public void addSubproperty(Property property, Property subproperty) {
		addSubproperty(property.getURI(), subproperty.getURI());
	}

	public void addSuperproperty(Property property, Property superproperty) {
		addSuperproperty(property.getURI(), superproperty.getURI());
	}

	public void removeSubproperty(Property property, Property subproperty) {
		removeSubproperty(property.getURI(), subproperty.getURI());	
	}

	public void removeSuperproperty(Property property, Property superproperty) {
		removeSuperproperty(property.getURI(), superproperty.getURI());
	}

	public void addEquivalentProperty(String propertyURI,
			String equivalentPropertyURI) {
		if (propertyURI == null || equivalentPropertyURI == null) {
			throw new RuntimeException("cannot assert equivalence of anonymous properties");
		}
		OntModel ontModel = getOntModel();	
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			Resource property = ontModel.getResource(propertyURI);
			Resource equivalentProperty = ontModel.getResource(equivalentPropertyURI);
			ontModel.add(property, OWL.equivalentProperty, equivalentProperty);
			ontModel.add(equivalentProperty, OWL.equivalentProperty, property);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	public void addEquivalentProperty(Property property,
			Property equivalentProperty) {
		addEquivalentProperty(property.getURI(), equivalentProperty.getURI());
	}

	public List<String> getEquivalentPropertyURIs(String propertyURI) {
       	List<String> equivURIs = new LinkedList<String>();
    	getOntModel().enterCriticalSection(Lock.READ);
    	try {
            StmtIterator eqStmtIt = getOntModel().listStatements(getOntModel().getResource(propertyURI), OWL.equivalentProperty, (RDFNode) null);
            while (eqStmtIt.hasNext()) {
                Statement eqStmt = eqStmtIt.nextStatement();
                RDFNode prop = eqStmt.getObject();
                if (prop.isResource() && ((Resource) prop).getURI() != null) {
                	equivURIs.add(((Resource) prop).getURI());
                }
            }
	    } catch (Exception e) {
	    	log.error(e); 
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return equivURIs;
	}

	public void removeEquivalentProperty(String propertyURI, String equivalentPropertyURI) {
		if (propertyURI == null || equivalentPropertyURI == null) {
			throw new RuntimeException("cannot remove equivalence axiom about anonymous properties");
		}
		OntModel ontModel = getOntModel();	
		ontModel.enterCriticalSection(Lock.WRITE);
		try {
			Resource property = ontModel.getResource(propertyURI);
			Resource equivalentProperty = ontModel.getResource(equivalentPropertyURI);
			ontModel.remove(property, OWL.equivalentProperty, equivalentProperty);
			ontModel.remove(equivalentProperty, OWL.equivalentProperty, property);
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	public void removeEquivalentProperty(Property property,
			Property equivalentProperty) {
		removeEquivalentProperty(property, equivalentProperty);
	}
	
	protected void removeABoxStatementsWithPredicate(Property predicate) {
		// DO NOT issue a removeAll() with a null (wildcard) in predicate position!
		if (predicate == null) {
			log.debug("Cannot remove ABox statements with a null predicate.");
			return;
		} else {
			removeABoxStatementsWithPredicate(predicate.getURI());
		}
	}
	
	protected void removeABoxStatementsWithPredicate(String predicateURI) {
		if (predicateURI == null) {
			log.debug("Cannot remove ABox statements with null predicate URI.");
			return;
		}
        OntModel aboxModel = getOntModelSelector().getABoxModel();
        aboxModel.enterCriticalSection(Lock.WRITE);
        try {
        	aboxModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        	aboxModel.removeAll((Resource) null, aboxModel.getProperty(predicateURI), (RDFNode) null);
        } finally {
        	aboxModel.getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        	aboxModel.leaveCriticalSection();
        }
    		
	}
	
	/**
	 * Finds the classes that have a definition involving a restriction
	 * on the given property. 
	 *
	 * @param   propertyURI  identifier of a property
	 * @return  a list of VClass objects representing the classes that have
	 *          definitions involving a restriction on the given property.
	 */

    public List <VClass> getClassesWithRestrictionOnProperty(String propertyURI) {
    	
    	if (propertyURI == null) {
    		log.info("getClassesWithRestrictionOnProperty: called with null propertyURI");
    		return null;
    	}
    	    	
		OntModel ontModel = getOntModel();	
		ontModel.enterCriticalSection(Lock.READ);
		
		HashSet<String> classURISet = new HashSet<String>();
		
		try {
			Resource targetProp = ontModel.getResource(propertyURI);
			   
			if (targetProp != null) {
			
			    StmtIterator stmtIter = ontModel.listStatements((Resource) null, OWL.onProperty, (RDFNode) targetProp);
	
			    while (stmtIter.hasNext()) {
				   Statement statement = stmtIter.next();
				   
				   if ( statement.getSubject().canAs(OntClass.class) ) {
					   classURISet.addAll(getRelatedClasses((OntClass) statement.getSubject().as(OntClass.class)));
				   } else {
					   log.warn("getClassesWithRestrictionOnProperty: Unexpected use of onProperty: it is not applied to a class");
				   }
			    }
			} else {
	    		log.error("getClassesWithRestrictionOnProperty: Error: didn't find a Property in the ontology model for the URI: " +  propertyURI);				
			}
		} finally {
			ontModel.leaveCriticalSection();
		}

		List<VClass> classes = new ArrayList<VClass>();
		Iterator<String> iter = classURISet.iterator();
		
		VClassDao vcd = getWebappDaoFactory().getVClassDao();
		
		while (iter.hasNext()) {
		
		   String curi = iter.next();
		   VClass vc = vcd.getVClassByURI(curi);
		  
		   if (vc != null) {
		       classes.add(vc);	  
		   } else {
			   log.error("getClassesWithRestrictionOnProperty: Error: no VClass found for URI: " + curi);
		   }	
		}
       
        return (classes.size()>0) ? classes : null;
    }

	/**
	 * Finds all named superclasses, subclasses and equivalent classes of
	 * the given class.
	 *
	 * @param   resourceURI  identifier of a class
	 * @return  set of class URIs
	 * 
	 * Note: this method assumes that the caller holds a read lock on
	 * the ontology model.
	 */

    public HashSet<String> getRelatedClasses(OntClass ontClass) {
    	
        HashSet<String> classSet = new HashSet<String>();
  
        List<OntClass> classList = ontClass.listEquivalentClasses().toList();
        classList.addAll(ontClass.listSubClasses().toList());
        classList.addAll(ontClass.listSuperClasses().toList());
        
        Iterator<OntClass> it = classList.iterator();
		         
        while (it.hasNext()) {
        	OntClass oc = it.next();
        	
        	if (!oc.isAnon()) {
        		classSet.add(oc.getURI());
        	}
        }
        		
        return classSet;
    }
}
