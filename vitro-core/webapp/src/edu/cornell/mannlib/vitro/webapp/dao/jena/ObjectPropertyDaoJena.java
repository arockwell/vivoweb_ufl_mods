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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.ProfileException;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;

public class ObjectPropertyDaoJena extends PropertyDaoJena implements ObjectPropertyDao {
    private static final Log log = LogFactory.getLog(ObjectPropertyDaoJena.class.getName());
    
    protected static final List<String> EXCLUDED_NAMESPACES = Arrays.asList(
            // Don't need to exclude these, because they are not owl:ObjectProperty
            //"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            //"http://www.w3.org/2000/01/rdf-schema#",
            "http://www.w3.org/2002/07/owl#"            
        ); 
    /*
     * This is a hack to throw out properties in the vitro, rdf, rdfs, and owl namespaces.
     * It will be implemented in a better way in v1.3 (Editing and Display Configuration).
     */
    protected static String propertyFilters = "";
    static {
        List<String> namespaceFilters = new ArrayList<String>();
        for (String s : EXCLUDED_NAMESPACES) {
            namespaceFilters.add("(afn:namespace(?property) != \"" + s + "\")");
        }
        // A hack to include the vitro:primaryLink and vitro:additionalLink properties in the list
        namespaceFilters.add("( ?property = vitro:primaryLink ||" +
                               "?property = vitro:additionalLink ||" +
                               "afn:namespace(?property) != \"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" )");
        propertyFilters = "FILTER (" + StringUtils.join(namespaceFilters, " && ") + ")\n";
    }
    
    protected static final String OBJECT_PROPERTY_QUERY_STRING = 
        prefixes + "\n" +
        "SELECT DISTINCT ?property WHERE { \n" +
        "   GRAPH ?g1 { ?subject ?property ?object } \n" + 
        "   GRAPH ?g2 { ?property rdf:type owl:ObjectProperty } \n" +
        propertyFilters +
        "}";

    protected static Query objectPropertyQuery;
    static {
        try {
            objectPropertyQuery = QueryFactory.create(OBJECT_PROPERTY_QUERY_STRING);
        } catch(Throwable th){
            log.error("could not create SPARQL query for OBJECT_PROPERTY_QUERY_STRING " + th.getMessage());
            log.error(OBJECT_PROPERTY_QUERY_STRING);
        }           
    }
    
    protected static final String LIST_VIEW_CONFIG_FILE_QUERY_STRING =
        "PREFIX display: <http://vitro.mannlib.cornell.edu/ontologies/display/1.1#>" +
        "SELECT ?property ?filename WHERE { \n" +
        "    ?property display:listViewConfigFile ?filename . \n" +
        "}";
    
    protected static Query listViewConfigFileQuery;
    static {
        try {
            listViewConfigFileQuery = QueryFactory.create(LIST_VIEW_CONFIG_FILE_QUERY_STRING);
        } catch(Throwable th){
            log.error("could not create SPARQL query for LIST_VIEW_CONFIG_FILE_QUERY_STRING " + th.getMessage());
            log.error(LIST_VIEW_CONFIG_FILE_QUERY_STRING);
        }           
    }
    
    Map<ObjectProperty, String> customListViewConfigFileMap = null;
    
    public ObjectPropertyDaoJena(DatasetWrapperFactory dwf, 
                                 WebappDaoFactoryJena wadf) {
        super(dwf, wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }
    
    public void deleteObjectProperty(String propertyURI) {
        OntProperty p = getOntModel().getOntProperty(propertyURI);
        ObjectProperty op = new ObjectProperty();
        op.setURI(propertyURI);
        deleteObjectProperty(op);
    }

    public void fillObjectPropertiesForIndividual(Individual entity) {
        if( entity == null ) return;
        List<ObjectProperty> props = new ArrayList<ObjectProperty>();
        props.addAll( getObjectPropertiesForObjectPropertyStatements(entity.getObjectPropertyStatements()) );
        entity.setPropertyList(props);
    }

    protected ObjectProperty propertyFromOntProperty(OntProperty op) {
        if (op==null) {
            return null;
        }
        ObjectProperty p = new ObjectProperty();
        try {
            p.setURI(op.getURI());
            p.setNamespace(op.getNameSpace());
            p.setLocalName(op.getLocalName());
            OntologyDao oDao=getWebappDaoFactory().getOntologyDao();
            Ontology o = (Ontology)oDao.getOntologyByURI(p.getNamespace());
            if (o==null) {
                if (!VitroVocabulary.vitroURI.equals(p.getNamespace())) {
                    log.debug("propertyFromOntProperty(): no ontology object found for the namespace "+p.getNamespace());
                }
            } else {
                String prefix = o.getPrefix()==null?(o.getName()==null?"unspec":o.getName()):o.getPrefix();
                p.setLocalNameWithPrefix(prefix+":"+p.getLocalName());
                //log.warn("setting pickListName to: "+p.getLocalName()+" ("+prefix+")");
                p.setPickListName(p.getLocalName()+" ("+prefix+")");
            }
            String propertyName = getPropertyStringValue(op,PROPERTY_FULLPROPERTYNAMEANNOT);
            if (propertyName != null) {
            	p.setDomainSidePhasedOut(propertyName);
            } else {
            	p.setDomainSidePhasedOut(op.getLocalName());
            }
            if (op.getLabel(null) != null)
                p.setDomainPublic(getLabelOrId(op));
            else
                p.setDomainPublic(op.getLocalName());
            if (p.getDomainPublic() == null)
                p.setDomainPublic("[related to]");
            if (op.getDomain() != null)
                p.setDomainVClassURI( (op.getDomain().isAnon()) ? PSEUDO_BNODE_NS+op.getDomain().getId().toString() : op.getDomain().getURI());
            if (op.getRange() != null)
                p.setRangeVClassURI( (op.getRange().isAnon()) ? PSEUDO_BNODE_NS+op.getRange().getId().toString() : op.getRange().getURI() );
            OntProperty invOp = null;
            try {
            	invOp = op.getInverse();
            } catch (ProfileException pe) {}
            try {
            if (op.getSuperProperty() != null)
                p.setParentURI(op.getSuperProperty().getURI());
            } catch (ConversionException ce) { 
            	StmtIterator parentStmtIt = op.listProperties(RDFS.subPropertyOf);
            	if (parentStmtIt.hasNext()) {
            		Statement parentStmt = parentStmtIt.nextStatement();
            		if (parentStmt.getObject().isResource()) {
            			p.setParentURI(((Resource)parentStmt.getObject()).getURI());
            		}
            	}
            	parentStmtIt.close();
            }
            if (invOp != null) {
                p.setURIInverse(invOp.getURI());
                p.setNamespaceInverse(invOp.getNameSpace());
                p.setLocalNameInverse(invOp.getLocalName());
                String invPropertyName = getPropertyStringValue(invOp,PROPERTY_FULLPROPERTYNAMEANNOT);
                if (invPropertyName != null) {
                	p.setRangeSidePhasedOut(invPropertyName);
                } else {
                	p.setRangeSidePhasedOut(invOp.getLocalName());
                }
                p.setRangePublic(getLabelOrId(invOp));
            }
            try {
	            if (op.isTransitiveProperty() || (invOp != null && invOp.isTransitiveProperty()) )  {
	            	p.setTransitive(true);
	            }
            } catch (ProfileException pe) {}
            try {
	            if (op.isSymmetricProperty() || (invOp != null && invOp.isSymmetricProperty()) )  {
	            	p.setSymmetric(true);
	            }
            } catch (ProfileException pe) {}
            try {
	            if (op.isFunctionalProperty())  {
	            	p.setFunctional(true);
	            }
            } catch (ProfileException pe) {}
            try {
	            if (op.isInverseFunctionalProperty())  {
	            	p.setInverseFunctional(true);
	            }
        	} catch (ProfileException pe) {}
            p.setExample(getPropertyStringValue(op,EXAMPLE_ANNOT));            
            p.setDescription(getPropertyStringValue(op,DESCRIPTION_ANNOT));
            p.setPublicDescription(getPropertyStringValue(op,PUBLIC_DESCRIPTION_ANNOT));
           
            try {
            	p.setDomainDisplayTier(Integer.toString(getPropertyNonNegativeIntValue(op,DISPLAY_RANK_ANNOT)));
            } catch (Exception e) {
            	log.error("Error converting displayRank integer to string for "+op.getURI());
            }
            try {
            	p.setRangeDisplayTier(Integer.toString(getPropertyNonNegativeIntValue(invOp,DISPLAY_RANK_ANNOT)));
            } catch (Exception e) {
            	log.error("Error converting displayRank integer to string for "+invOp.getURI());
            }
            p.setDomainDisplayLimit(getPropertyNonNegativeIntValue(op,DISPLAY_LIMIT));
            p.setRangeDisplayLimit(getPropertyNonNegativeIntValue(invOp,DISPLAY_LIMIT));
            p.setDomainEntitySortField(getPropertyStringValue(op,PROPERTY_ENTITYSORTFIELD));
            p.setRangeEntitySortField(getPropertyStringValue(invOp,PROPERTY_ENTITYSORTFIELD));
            RDFNode objectIndividualSortPropertyNode = op.getPropertyValue(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
            if (objectIndividualSortPropertyNode instanceof Resource) {  
            	p.setObjectIndividualSortPropertyURI( ((Resource)objectIndividualSortPropertyNode).getURI() ); 
            }
            p.setDomainEntitySortDirection(getPropertyStringValue(op,PROPERTY_ENTITYSORTDIRECTION));
            p.setRangeEntitySortDirection(getPropertyStringValue(invOp,PROPERTY_ENTITYSORTDIRECTION));
            
            //There might be multiple HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            StmtIterator it = op.listProperties(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel hiddenRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = (Resource)obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (hiddenRoleLevel == null || roleFromModel.compareTo(hiddenRoleLevel) > 0 )){
                            hiddenRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            p.setHiddenFromDisplayBelowRoleLevel(hiddenRoleLevel); //this might get set to null

            //There might be multiple PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT properties, only use the highest
            it = op.listProperties(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel prohibitedRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = (Resource)obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel =  BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (prohibitedRoleLevel == null || roleFromModel.compareTo(prohibitedRoleLevel) > 0 )){
                            prohibitedRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            p.setProhibitedFromUpdateBelowRoleLevel(prohibitedRoleLevel); //this might get set to null

            p.setCustomEntryForm(getPropertyStringValue(op,PROPERTY_CUSTOMENTRYFORMANNOT));
            Boolean selectFromObj = getPropertyBooleanValue(op,PROPERTY_SELECTFROMEXISTINGANNOT);
            p.setSelectFromExisting(selectFromObj==null ? true : selectFromObj);
            Boolean offerCreateObj = getPropertyBooleanValue(op,PROPERTY_OFFERCREATENEWOPTIONANNOT);
            p.setOfferCreateNewOption(offerCreateObj==null ? false : offerCreateObj);
            Boolean dependencyDeletionObj = getPropertyBooleanValue(op,PROPERTY_STUBOBJECTPROPERTYANNOT);
            p.setStubObjectRelation(dependencyDeletionObj==null ? false : dependencyDeletionObj);

            Boolean collateBySubclass = getPropertyBooleanValue(op,PROPERTY_COLLATEBYSUBCLASSANNOT);
            p.setCollateBySubclass(collateBySubclass==null ? false : collateBySubclass);
            
            // the <i> thing from the old system causes sorting problems and ugliness; here is an inelegant way of dealing with it for now (Note <i>s will disappear on update)
            if (p.getDomainSidePhasedOut() != null) {
            	p.setDomainSidePhasedOut(stripItalics(p.getDomainSidePhasedOut()));
            }
            if (p.getRangeSidePhasedOut() != null) {
            	p.setRangeSidePhasedOut(stripItalics(p.getRangeSidePhasedOut()));
            }
            Resource groupRes = (Resource) op.getPropertyValue(PROPERTY_INPROPERTYGROUPANNOT);
            if (groupRes != null) {
                p.setGroupURI(groupRes.getURI());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return p;
    }
    
    private String stripItalics(String in) {
    	String out = in.replaceAll("\\<i\\>","");
    	out = out.replaceAll("\\<\\/i\\>","");
    	return out;
    }

    public List getAllObjectProperties() {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List props = new ArrayList();
            try {
	            ClosableIterator opIt = getOntModel().listObjectProperties();
	            try {
	                while (opIt.hasNext()) {
	                    com.hp.hpl.jena.ontology.ObjectProperty op = (com.hp.hpl.jena.ontology.ObjectProperty) opIt.next();
	                    if (!NONUSER_NAMESPACES.contains(op.getNameSpace()))
	                    props.add(propertyFromOntProperty(op));
	                }
	            } finally {
	                opIt.close();
	            }
            } catch (ProfileException pe) {
            	ClosableIterator opIt = getOntModel().listSubjectsWithProperty(RDF.type,RDF.Property);
	            try {
	                while (opIt.hasNext()) {
	                	Resource res = (Resource) opIt.next();
	                	if ( (res.canAs(OntProperty.class)) && (!NONUSER_NAMESPACES.contains(res.getNameSpace())) ) {
	                		props.add(propertyFromOntProperty((OntProperty)res.as(OntProperty.class)));
	                	}
	                }
	            } finally {
	                opIt.close();
	            }
            }
            return props;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List getPropIdsForClass(String classURI, boolean direction) {
        return null;
    }

    public ObjectProperty getObjectPropertyByURI(String propertyURI) {
        if( propertyURI == null ) return null;
        
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            OntProperty op = getOntModel().getOntProperty(propertyURI);
            return propertyFromOntProperty(op);
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List<ObjectProperty> getObjectPropertiesForObjectPropertyStatements(List objPropertyStmts) {
        if( objPropertyStmts == null || objPropertyStmts.size() < 1) return new ArrayList();
        HashMap<String,ObjectProperty> hash = new HashMap<String,ObjectProperty>();
        String uris ="";
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Iterator it = objPropertyStmts.iterator();
            while(it.hasNext()){
                ObjectPropertyStatement objPropertyStmt = (ObjectPropertyStatement)it.next();
                if (hash.containsKey(objPropertyStmt.getPropertyURI())) {
                    ObjectProperty p = (ObjectProperty) hash.get(objPropertyStmt.getPropertyURI());
                    p.addObjectPropertyStatement(objPropertyStmt);
                } else {
                    OntProperty op = getOntModel().getOntProperty(objPropertyStmt.getPropertyURI());
                    if (op != null) {
                        ObjectProperty p = propertyFromOntProperty(op);
                        hash.put(p.getURI(),p);
                        p.addObjectPropertyStatement(objPropertyStmt);
                    }
                }
            }

            List<ObjectProperty> props = new ArrayList<ObjectProperty>();
            Iterator<String> keyIt = hash.keySet().iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                props.add(hash.get(key));
            }
            return props;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

	// This is a funny method to address the fact that the editing code will
	// often supply us the property beans containing an inverse namespace but no
	// inverse local name, which should be interpreted as a lack of an inverse
	// property entirely.  Yes, this is a hack.
	private boolean hasInverse(ObjectProperty prop) {
		boolean hasInverse = false;
		if ( (prop.getURIInverse() != null) && (!("".equals(prop.getURIInverse()))) ) {
			hasInverse = true;
		} 
		if ( (prop.getNamespaceInverse() != null) && (!(prop.getNamespaceInverse().equals(""))) &&
			 ( (prop.getLocalNameInverse()==null) || ("".equals(prop.getLocalNameInverse()) ) ) ) {
			hasInverse = false;
		}
		return hasInverse;
	}

    public int insertObjectProperty(ObjectProperty prop) throws InsertException {
    	return insertProperty(prop,getOntModel());
    }

    public int insertProperty(ObjectProperty prop, OntModel ontModel) throws InsertException {
        if (prop.getURI()==null || prop.getURI().length()<3) {
            return 1;
        }
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
	        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
	    	String errMsgStr = getWebappDaoFactory().checkURI(prop.getURI());
	    	if (errMsgStr != null) {
	    		throw new InsertException(errMsgStr);
	    	}
	        com.hp.hpl.jena.ontology.ObjectProperty p = ontModel.createObjectProperty(prop.getURI());
	        com.hp.hpl.jena.ontology.ObjectProperty inv = null;
	        if (hasInverse(prop)) {
	        	log.debug("non-null inverse URI: " +prop.getURIInverse());	        	
	        	errMsgStr = getWebappDaoFactory().checkURI(prop.getURIInverse());
	        	if (errMsgStr != null) {
	        		throw new InsertException("Unusable URI for inverse property: "+errMsgStr);
	        	}
	            inv = ontModel.createObjectProperty(prop.getURIInverse());
	            inv.setInverseOf(p);
	            p.setInverseOf(inv);
	        }
	        doUpdate(prop,p,inv,ontModel);
	        return 0;
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        	ontModel.leaveCriticalSection();
        }
    }

    public void updateObjectProperty(ObjectProperty prop) {
    	updateProperty(prop,getOntModel());
    }

    
    public void updateProperty(ObjectProperty prop, OntModel ontModel) {
        if (prop.getURI()==null || prop.getURI().length()<3) {
            return;
        }
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
	        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));     
	        com.hp.hpl.jena.ontology.ObjectProperty p = ontModel.getObjectProperty(prop.getURI());
	        if (p == null) {
	            return;
	        }
	        com.hp.hpl.jena.ontology.ObjectProperty inv = null;
	        if (hasInverse(prop)) {
			    try {
		            	inv = ontModel.getObjectProperty(prop.getURIInverse());
		            	
		            	if  (!inv.isInverseOf(p)) {
		            	  inv.setInverseOf(p);
		            	  p.setInverseOf(inv);
		            	}
	            } catch (Exception e) {
	                   log.debug("Couldn't set "+prop.getURIInverse()+" as inverse");
				       // BJL: What we really want to do here is create a new property as inverse
			    }      
	        }
	        
            try {
	             doUpdate(prop,p,inv,ontModel);
            } catch (Exception e) {
                 e.printStackTrace();
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
        	ontModel.leaveCriticalSection();
        }
    }
    
    /* This method assumes that the caller is locking and unlocking the model */
    private void doUpdate(ObjectProperty prop, OntProperty p, OntProperty inv, OntModel ontModel) {
        
        updateRDFSLabel(p,prop.getDomainPublic());

        if (inv != null) {
    		updateRDFSLabel(inv,prop.getRangePublic());
    	}

    	try {
	        String parentURI = prop.getParentURI();
	        if (parentURI != null) {
		        com.hp.hpl.jena.ontology.ObjectProperty parent = ontModel.getObjectProperty(prop.getParentURI());
		        if (parent != null) {
		        	
		        	if (!p.hasSuperProperty(parent, true)) {
		               p.addSuperProperty(parent);
		        	}
		        	
		            if (inv != null) {
		                OntProperty parentInv = parent.getInverse();
		                
		                if (parentInv != null) {
		                	if (!inv.hasSuperProperty(parentInv,true)) {
		                	   inv.setSuperProperty(parentInv);
		                	}
		                }		                
		            }
		        }
	        }
    	} catch (Exception e) {
    		// something odd happens here with layered models
    		// plus, this whole section needs reworking
    		log.error("Unable to update parent property for "+prop.getURI(), e);
    	}
    	
        if (prop.getTransitive()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.TransitiveProperty)) {
        		getOntModel().add(p,RDF.type,OWL.TransitiveProperty);
        	}
        	
        	if ( (inv != null) && (!getOntModel().contains(inv,RDF.type,OWL.TransitiveProperty))) {
        		getOntModel().add(inv,RDF.type,OWL.TransitiveProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.TransitiveProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.TransitiveProperty);
        	}
        	if ( (inv != null) && (getOntModel().contains(inv,RDF.type,OWL.TransitiveProperty))) {
        		getOntModel().remove(inv,RDF.type,OWL.TransitiveProperty);
        	}
        }
        
        if (prop.getSymmetric()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.SymmetricProperty)) {
        		getOntModel().add(p,RDF.type,OWL.SymmetricProperty);
        	}
        	if ( (inv != null) && (!getOntModel().contains(inv,RDF.type,OWL.SymmetricProperty))) {
        		getOntModel().add(inv,RDF.type,OWL.SymmetricProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.SymmetricProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.SymmetricProperty);
        	}
        	if ( (inv != null) && (getOntModel().contains(inv,RDF.type,OWL.SymmetricProperty))) {
        		getOntModel().remove(inv,RDF.type,OWL.SymmetricProperty);
        	}
        }
        
        if (prop.getFunctional()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.FunctionalProperty)) {
        		getOntModel().add(p,RDF.type,OWL.FunctionalProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.FunctionalProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.FunctionalProperty);
        	}
        }
        
        if (prop.getInverseFunctional()) {
        	if (!getOntModel().contains(p,RDF.type,OWL.InverseFunctionalProperty)) {
        		getOntModel().add(p,RDF.type,OWL.InverseFunctionalProperty);
        	}
        } else {
        	if (getOntModel().contains(p,RDF.type,OWL.InverseFunctionalProperty)) {
        		getOntModel().remove(p,RDF.type,OWL.InverseFunctionalProperty);
        	}
        }
 
/*     3/29/2010 sjm. Commenting out per bjl. As far as we can tell from looking at the code, these fields
 *                    are no longer used by the application. Leaving this here commented out for now though,
 *                    just in case.   
 
        
        if (prop.getDomainSidePhasedOut() != null) {
        	updatePropertyStringValue(p,PROPERTY_FULLPROPERTYNAMEANNOT,prop.getDomainSidePhasedOut(),ontModel);
        }
        if (prop.getRangeSidePhasedOut() != null && inv != null) {
        	updatePropertyStringValue(inv,PROPERTY_FULLPROPERTYNAMEANNOT,prop.getRangeSidePhasedOut(),ontModel);
        }
*/        
    	
        if ( (prop.getDomainVClassURI() != null) && (prop.getDomainVClassURI().length()>0) ) {
            if (!p.hasDomain(ontModel.getResource(prop.getDomainVClassURI()))) {
        	    p.setDomain(ontModel.getResource(prop.getDomainVClassURI()));
                if (inv != null) {
                   inv.setRange(p.getDomain());
                }
            }
        } else {
            p.removeAll(RDFS.domain);
            if (inv != null) {
            	inv.removeAll(RDFS.range);
            }        	
        }
                
        if ( (prop.getRangeVClassURI() != null) && (prop.getRangeVClassURI().length()>0) ) {
            if (!p.hasRange(ontModel.getResource(prop.getRangeVClassURI()))) {
               p.setRange(ontModel.getResource(prop.getRangeVClassURI()));
               if (inv != null) {
                   inv.setDomain(p.getRange());
               }
            }
        } else {
            p.removeAll(RDFS.range);
            if (inv != null) {
            	inv.removeAll(RDFS.domain);
            }        	
        }
        
        updatePropertyStringValue(p,EXAMPLE_ANNOT,prop.getExample(),getOntModel());
        updatePropertyStringValue(p,DESCRIPTION_ANNOT,prop.getDescription(),getOntModel());
        updatePropertyStringValue(p,PUBLIC_DESCRIPTION_ANNOT,prop.getPublicDescription(),getOntModel());
        updatePropertyNonNegativeIntValue(p,DISPLAY_LIMIT,prop.getDomainDisplayLimit(),getOntModel());
        updatePropertyStringValue(p,PROPERTY_ENTITYSORTFIELD,prop.getDomainEntitySortField(),getOntModel());
        updatePropertyStringValue(p,PROPERTY_ENTITYSORTDIRECTION,prop.getDomainEntitySortDirection(),getOntModel());
        if (inv != null) {
            updatePropertyStringValue(inv,EXAMPLE_ANNOT,prop.getExample(),getOntModel());
            updatePropertyStringValue(inv,DESCRIPTION_ANNOT,prop.getDescription(),getOntModel());
            updatePropertyNonNegativeIntValue(inv,DISPLAY_LIMIT,prop.getRangeDisplayLimit(),getOntModel());
            updatePropertyStringValue(inv,PROPERTY_ENTITYSORTFIELD,prop.getRangeEntitySortField(),getOntModel());
            updatePropertyStringValue(inv,PROPERTY_ENTITYSORTDIRECTION,prop.getRangeEntitySortDirection(),getOntModel());
        }
                
    	if (prop.getDomainDisplayTier() != null) {
    		updatePropertyNonNegativeIntValue(p,DISPLAY_RANK_ANNOT,Integer.decode(prop.getDomainDisplayTier()),getOntModel());

    		if (inv != null) {
            	if (prop.getRangeDisplayTier() != null) {
            		updatePropertyNonNegativeIntValue(inv,DISPLAY_RANK_ANNOT,Integer.decode(prop.getRangeDisplayTier()),getOntModel());
            	}
    		}
    	}
    	
        String oldObjectIndividualSortPropertyURI = null;
    	RDFNode sortPropertyNode = p.getPropertyValue(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
    	if (sortPropertyNode != null && sortPropertyNode instanceof Resource) {
    		oldObjectIndividualSortPropertyURI = ((Resource)sortPropertyNode).getURI();
    	}
        if ( ( oldObjectIndividualSortPropertyURI != null && prop.getObjectIndividualSortPropertyURI() == null ) ||
        	 ( oldObjectIndividualSortPropertyURI != null && prop.getObjectIndividualSortPropertyURI() != null && !(oldObjectIndividualSortPropertyURI.equals(prop.getObjectIndividualSortPropertyURI()))))	{
        	p.removeAll(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
        } 
        if ( ( oldObjectIndividualSortPropertyURI == null && prop.getObjectIndividualSortPropertyURI() != null ) ||
           	 ( oldObjectIndividualSortPropertyURI != null && prop.getObjectIndividualSortPropertyURI() != null && !(oldObjectIndividualSortPropertyURI.equals(prop.getObjectIndividualSortPropertyURI()))))	{
        	Resource newObjectIndividualSortProperty = null;
        	try {
        		if (prop.getObjectIndividualSortPropertyURI().length()>0) {
        			newObjectIndividualSortProperty = ResourceFactory.createResource(prop.getObjectIndividualSortPropertyURI());
        		}
        	} catch (Exception e) {}
        	if (newObjectIndividualSortProperty != null) {
        		p.addProperty(PROPERTY_OBJECTINDIVIDUALSORTPROPERTY,newObjectIndividualSortProperty);
        	}
        }

        if (prop.getHiddenFromDisplayBelowRoleLevel() != null) {
        	updatePropertyResourceURIValue(p, HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, prop.getHiddenFromDisplayBelowRoleLevel().getURI());
        }

        if (prop.getProhibitedFromUpdateBelowRoleLevel() != null) {
        	updatePropertyResourceURIValue(p, PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, prop.getProhibitedFromUpdateBelowRoleLevel().getURI());
        }

        updatePropertyStringValue(p,PROPERTY_CUSTOMENTRYFORMANNOT,prop.getCustomEntryForm(),ontModel);
        updatePropertyBooleanValue(p,PROPERTY_SELECTFROMEXISTINGANNOT,prop.getSelectFromExisting(),ontModel,JenaBaseDao.KEEP_ONLY_IF_FALSE);
        updatePropertyBooleanValue(p,PROPERTY_OFFERCREATENEWOPTIONANNOT,prop.getOfferCreateNewOption(),ontModel,JenaBaseDao.KEEP_ONLY_IF_TRUE);
        updatePropertyBooleanValue(p,PROPERTY_STUBOBJECTPROPERTYANNOT,prop.getStubObjectRelation(),ontModel,JenaBaseDao.KEEP_ONLY_IF_TRUE);
        updatePropertyBooleanValue(p,PROPERTY_COLLATEBYSUBCLASSANNOT,prop.getCollateBySubclass(),ontModel,JenaBaseDao.KEEP_ONLY_IF_TRUE);
        
        updatePropertyResourceURIValue(p, PROPERTY_INPROPERTYGROUPANNOT, prop.getGroupURI());
    }

    public void deleteObjectProperty(ObjectProperty prop) {
    	deleteProperty(prop,getOntModel());
    }

    public void deleteProperty(ObjectProperty prop, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),true));
        try {
            OntProperty op = ontModel.getOntProperty(prop.getURI());
            if (op != null) {            	
            	//Remove restriction class.
            	Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.onProperty, op);
            	while(restIt.hasNext()) {
            		Resource restRes = restIt.next();
            		if (restRes.canAs(OntResource.class)) {
            			OntResource restOntRes = (OntResource) restRes.as(OntResource.class);
            			smartRemove(restOntRes, ontModel);
            		}
            	}            	
            	removeRulesMentioningResource(op, ontModel);
            	
            	//Remove vitro:DependentResource
            	List<Statement> depResStmts = new LinkedList<Statement>();
            	StmtIterator depResIt = ontModel.listStatements(null, op, (RDFNode)null);
            	while(depResIt.hasNext()) {
            		depResStmts.addAll(DependentResourceDeleteJena
            				.getDependentResourceDeleteList(depResIt.nextStatement(), ontModel) );
            	}
            	ontModel.remove(depResStmts);
                op.remove();
            }                    	
        	
            if ( (prop.getURIInverse() != null) && (prop.getURIInverse().length()>0) ) {
            	OntProperty invOp = ontModel.getOntProperty(prop.getURIInverse());
	            if (invOp != null) {
	            	Iterator<Resource> restIt = ontModel.listSubjectsWithProperty(OWL.onProperty, op);
	            	while(restIt.hasNext()) {
	            		Resource restRes = restIt.next();
	            		if (restRes.canAs(OntResource.class)) {
	            			OntResource restOntRes = (OntResource) restRes.as(OntResource.class);
	            			smartRemove(restOntRes, ontModel);
	            		}
	            	}
	            	removeRulesMentioningResource(invOp, ontModel);
	            	
	            	//bdc34: I'm not sure if we want to remove DependentResources on a ObjectProperty delete.
	                invOp.remove();
	            }
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new EditEvent(getWebappDaoFactory().getUserURI(),false));
            ontModel.leaveCriticalSection();
        }
        // remove ABox statements after removing property
        // so dependentResource deletion test will pass
        removeABoxStatementsWithPredicate(prop);
    }

    public List<ObjectProperty> getRootObjectProperties() {
        List rootProperties = new ArrayList();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
        	ClosableIterator propIt;
        	try {
        		propIt = getOntModel().listObjectProperties();
        	} catch (ProfileException pe) {
        		propIt = getOntModel().listSubjectsWithProperty(RDF.type, RDF.Property);
        	}
            try {
                while (propIt.hasNext()) {
                	Resource res = (Resource) propIt.next();
                	if (res.canAs(OntProperty.class)) {
	                    com.hp.hpl.jena.ontology.OntProperty op = (com.hp.hpl.jena.ontology.OntProperty) res.as(OntProperty.class);
	                    boolean isRoot = false;
	                    Iterator parentIt = op.listSuperProperties();
	                    if (parentIt != null) {
	                    	List<Property> parentList = new ArrayList<Property>();
	                    	try {
	                    		parentList.addAll(op.listSuperProperties().toList());
	                    	} catch (ConversionException ce) {
	                    		StmtIterator parentStmtIt = op.listProperties(RDFS.subPropertyOf);
	                    		while (parentStmtIt.hasNext()) {
	                    			Statement parentStmt = parentStmtIt.nextStatement();
	                    			if (parentStmt.getObject().isResource()) {
	                    				Resource parentRes = (Resource) parentStmt.getObject();
	                    				if (parentRes.getURI() != null) {
	                    					Property parentProperty = ResourceFactory.createProperty(parentRes.getURI());
	                    					parentList.add(parentProperty);
	                    				}
	                    			}
	                    		}
	                    		parentStmtIt.close();
	                    	}
	                    	if (parentList.size()==0) {
	                    		isRoot = true;
	                    	} else {
	                    		isRoot = true;
	                    	    Iterator<? extends Property> pit = parentList.iterator();
	                    	    while (pit.hasNext()) {
	                    	    	Property pt = pit.next();
	                    	    	if ( (!pt.equals(op)) && (!(getOntModel().contains(op,OWL.equivalentProperty,pt)) || (getOntModel().contains(pt,OWL.equivalentProperty,op))) ) {
	                    	    		isRoot = false;
	                    	    	}
	                    	    }
	                    	} 
	                    } else {
	                    	isRoot = true;
	                    }
	                    
	                    if (isRoot) {
	                        if (!NONUSER_NAMESPACES.contains(op.getNameSpace())) {
	                            rootProperties.add(propertyFromOntProperty(op));
	                        }
	                    }
                	}
                }
            } finally {
                propIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        if (rootProperties.size()==0)
            return null;
        else
            return rootProperties;
    }

    public List<String> getSuperPropertyURIs(String objectPropertyURI) {
        List supURIs = new ArrayList();
        try {
            Iterator supIt = getOntModel().getOntProperty(objectPropertyURI).listSuperProperties(true);
            while (supIt.hasNext()) {
                try {
                    OntProperty op = (OntProperty) supIt.next();
                    supURIs.add(op.getURI());
                } catch (Exception cce) {}
            }
        } catch (Exception e) {}
        return supURIs;
    }

    public List<String> getSubPropertyURIs(String objectPropertyURI) {
        List subURIs = new ArrayList();
        try {
            Iterator subIt = getOntModel().getOntProperty(objectPropertyURI).listSubProperties(true);
            while (subIt.hasNext()) {
                try {
                    OntProperty op = (OntProperty) subIt.next();
                    subURIs.add(op.getURI());
                } catch (Exception cce) {}
            }
        } catch (Exception e) {}
        return subURIs;
    }

    public List<ObjectPropertyStatement> getStatementsUsingObjectProperty(ObjectProperty op) {
        return null;
    }
    
    // checks for annotation property vitro:skipEditForm.
    // Used by N3 editing system to govern behavior of edit link:
    // if annotation is present, editing system redirects to display
    // the object individual instead of a normal editing form.
    public boolean skipEditForm(String predicateURI) {
    	OntModel ontModel = getOntModel();
    	try {
    		ontModel.enterCriticalSection(Lock.READ);
    		if (getOntModel().contains(
    				ontModel.getResource(predicateURI), 
    				ontModel.getProperty(VitroVocabulary.SKIP_EDIT_FORM),
    				ontModel.createTypedLiteral("true", XSDDatatype.XSDboolean) ) ) {
    			return true;
    		}
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    	return false;
    }

    @Override
    public List<ObjectProperty> getObjectPropertyList(Individual subject) {
        return getObjectPropertyList(subject.getURI());
    }
    
    @Override
    /*
     * SPARQL-based method for getting the individual's object properties.
     * Ideally this implementation should replace the existing way of getting
     * the object property list, but the consequences of this may be far-reaching,
     * so we are implementing a new method now and will merge the old approach
     * into the new one in a future release.
     */
    public List<ObjectProperty> getObjectPropertyList(String subjectUri) {
        log.debug("Object property query string:\n" + OBJECT_PROPERTY_QUERY_STRING);
        log.debug("Object property query:\n" + objectPropertyQuery);
        Iterator<QuerySolution> results = getPropertyQueryResults(subjectUri, objectPropertyQuery);
        List<ObjectProperty> properties = new ArrayList<ObjectProperty>();
        while (results.hasNext()) {
            QuerySolution soln = results.next();
            Resource resource = soln.getResource("property");
            String uri = resource.getURI();
            log.debug("Found populated object property " + uri);
            ObjectProperty property = getObjectPropertyByURI(uri);
            if (property != null) {
                properties.add(property);
            }
        }
        return properties; 
    }
    
    @Override
    public String getCustomListViewConfigFileName(ObjectProperty op) {
        if (customListViewConfigFileMap == null) {
            customListViewConfigFileMap = new HashMap<ObjectProperty, String>();
            OntModel displayModel = getOntModelSelector().getDisplayModel();
            QueryExecution qexec = QueryExecutionFactory.create(listViewConfigFileQuery, displayModel); 
            ResultSet results = qexec.execSelect();           
            while (results.hasNext()) {
                QuerySolution soln = results.next();
                ObjectProperty prop = getObjectPropertyByURI(soln.getResource("property").getURI());
                String filename = soln.getLiteral("filename").getLexicalForm();
                customListViewConfigFileMap.put(prop, filename);                
            }           
        }        
        return customListViewConfigFileMap.get(op);
    }

}
