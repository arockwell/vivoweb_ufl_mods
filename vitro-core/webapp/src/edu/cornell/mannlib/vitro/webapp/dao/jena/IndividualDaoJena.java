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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.KeywordIndividualRelation;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.KeywordDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualCreationEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualDeletionEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;

public class IndividualDaoJena extends JenaBaseDao implements IndividualDao {

    public IndividualDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    private static final Log log = LogFactory.getLog(IndividualDaoJena.class.getName());

    public Collection<DataPropertyStatement> getExternalIds(String individualURI) {
        return this.getExternalIds(individualURI, null);
    }

    public Collection<DataPropertyStatement> getExternalIds(String individualURI, String dataPropertyURI) {
        Collection<DataPropertyStatement> externalIdStatements = new ArrayList<DataPropertyStatement>();
        Individual ind = getIndividualByURI(individualURI);
        HashSet<String> externalIdPropURISet = new HashSet<String>();
        HashSet<String> nonExternalIdPropURISet = new HashSet<String>();
        if (ind != null) {
            Collection<DataPropertyStatement> dpsColl = getWebappDaoFactory().getDataPropertyStatementDao().getDataPropertyStatementsForIndividualByDataPropertyURI(ind, dataPropertyURI);
            Iterator<DataPropertyStatement> dpsIt = dpsColl.iterator();
            while (dpsIt.hasNext()) {
                DataPropertyStatement dps = dpsIt.next();
                if (externalIdPropURISet.contains(dps.getDatapropURI())) {
                    externalIdStatements.add(dps);
                } else if (!nonExternalIdPropURISet.contains(dps.getDatapropURI())) {
                	OntModel tboxOntModel = getOntModelSelector().getTBoxModel();
                    tboxOntModel.enterCriticalSection(Lock.READ);
                    try {
                        Resource dataprop = tboxOntModel.getResource(dps.getDatapropURI());
                        if (dataprop != null && (tboxOntModel.contains(dataprop, DATAPROPERTY_ISEXTERNALID, ResourceFactory.createTypedLiteral(true)) || tboxOntModel.contains(dataprop, DATAPROPERTY_ISEXTERNALID, "TRUE") )) {
                            externalIdPropURISet.add(dps.getDatapropURI());
                            externalIdStatements.add(dps);
                        } else {
                            nonExternalIdPropURISet.add(dps.getDatapropURI());
                        }
                    } finally {
                        tboxOntModel.leaveCriticalSection();
                    }
                }
            }
        }
        return externalIdStatements;
    }

    public void addVClass(String individualURI, String vclassURI) {
    	OntModel ontModel = getOntModelSelector().getABoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,individualURI));
        try {
            Resource indRes = ontModel.getResource(individualURI);
            ontModel.add(indRes, RDF.type, ontModel.getResource(vclassURI));
            updatePropertyDateTimeValue(indRes, MODTIME, Calendar.getInstance().getTime(),ontModel);
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            ontModel.leaveCriticalSection();
        }
    }

    public void removeVClass(String individualURI, String vclassURI) {
    	OntModel ontModel = getOntModelSelector().getABoxModel();
        ontModel.enterCriticalSection(Lock.WRITE);
        ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,individualURI));
        try {
            Resource indRes = ontModel.getResource(individualURI);
            getOntModel().remove(indRes, RDF.type, ontModel.getResource(vclassURI));
            updatePropertyDateTimeValue(indRes, MODTIME, Calendar.getInstance().getTime(), ontModel);
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            ontModel.leaveCriticalSection();
        }
    }

    public List<Individual> getIndividualsByVClass(VClass vclass ) {
        return getIndividualsByVClassURI(vclass.getURI(),-1,-1);
    }
    
    public List<Individual> getIndividualsByVClassURI(String vclassURI) {
        return getIndividualsByVClassURI(vclassURI,-1,-1);
    }

    public List<Individual> getIndividualsByVClassURI(String vclassURI, int offset, int quantity ) {

    	if (vclassURI==null) {
            return null;
        }
        
        List<Individual> ents = new ArrayList<Individual>();
        
        Resource theClass = (vclassURI.indexOf(PSEUDO_BNODE_NS) == 0) 
            ? getOntModel().createResource(new AnonId(vclassURI.split("#")[1]))
            : ResourceFactory.createResource(vclassURI);
    
        
        
        if (theClass.isAnon() && theClass.canAs(UnionClass.class)) {
        	UnionClass u = (UnionClass) theClass.as(UnionClass.class);
        	for (OntClass operand : u.listOperands().toList()) {
        		VClass vc = new VClassJena(operand, getWebappDaoFactory());
        		ents.addAll(getIndividualsByVClass(vc));
        	}
        } else {
        	OntModel ontModel = getOntModelSelector().getABoxModel();
        	try {
        		ontModel.enterCriticalSection(Lock.READ);
	            StmtIterator stmtIt = ontModel.listStatements((Resource) null, RDF.type, theClass);
	            try {
	                while (stmtIt.hasNext()) {
	                    Statement stmt = stmtIt.nextStatement();
	                    OntResource ind = (OntResource) stmt.getSubject().as(OntResource.class);
	                    ents.add(new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
	                }
	            } finally {
	                stmtIt.close();
	            }
        	} finally {
        		ontModel.leaveCriticalSection();
        	}
        }
        

        java.util.Collections.sort(ents);

        return ents;

    }

    public int getCountOfIndividualsInVClass(String vclassURI ) {
        int count = 0;
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            OntClass cls = getOntModel().getOntClass(vclassURI);
            Iterator<? extends OntResource> indIt = cls.listInstances();
            while (indIt.hasNext()) {
                count++;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return count;
    }

    public String insertNewIndividual(Individual ent) throws InsertException {
        return insertNewIndividualWebapp(ent);
    }

    public String insertNewIndividualWebapp(Individual ent) throws InsertException {
        initInd(ent);
        return insertNewIndividual(ent, getOntModelSelector().getABoxModel());
    }

    /**
     * Inserts a new Individual into the knowledge base.
     * Note that a number of magic things happen in here.
     */
    public String insertNewIndividual(Individual ent, OntModel ontModel) throws InsertException {
    	
        String preferredURI = getUnusedURI(ent);          
           
        String entURI = null;
        
    	Resource cls = null;
        OntModel tboxModel = getOntModelSelector().getTBoxModel();
        tboxModel.enterCriticalSection(Lock.READ);
        try {
            try {
                cls = tboxModel.getOntClass(ent.getVClassURI());
            } catch (Exception e) {}
            if (cls==null) {
                cls = OWL.Thing; // This assumes we want OWL-DL compatibility. Individuals cannot be untyped.
            }
        } finally {
        	tboxModel.leaveCriticalSection();
        }
        
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
         
            entURI = new String(preferredURI);          
            com.hp.hpl.jena.ontology.Individual test = ontModel.getIndividual(entURI);
            int count = 0;
            while (test != null) {
                ++count;
                entURI = new String(preferredURI) + count;
                test = ontModel.getIndividual(entURI);
            }
            
            try {
                ontModel.getBaseModel().notifyEvent(new IndividualCreationEvent(getWebappDaoFactory().getUserURI(),true,entURI));
                com.hp.hpl.jena.ontology.Individual ind = ontModel.createIndividual(entURI,cls);
                if (ent.getName() != null) {
                    ind.setLabel(ent.getName(), (String) getDefaultLanguage());
                }
                List<VClass> vclasses = ent.getVClasses(false);
                if (vclasses != null) {
                    for (Iterator<VClass> typeIt = vclasses.iterator(); typeIt.hasNext(); ) {
                        VClass vc = typeIt.next();
                        ind.addRDFType(ResourceFactory.createResource(vc.getURI()));
                    }
                }
                String flag1Set = ent.getFlag1Set();
                if (flag1Set != null) {
                    String[] flag1Value = flag1Set.split(",");
                    for (int i=0; i<flag1Value.length; i++) {
                        Resource flag1Type = getOntModel().getResource(VitroVocabulary.vitroURI+"Flag1Value"+flag1Value[i]+"Thing");
                        if (flag1Type != null) {
                            ind.addRDFType(flag1Type);
                        }
                    }
                }
                String flag2Set = ent.getFlag2Set();
                if (flag2Set != null) {
                    String[] flag2Value = flag2Set.split(",");
                    for (int i=0; i<flag2Value.length; i++) {
                        Resource flag2Type = getFlag2ValueMap().get(flag2Value[i]);
                        if (flag2Type != null) {
                            ind.addRDFType(flag2Type);
                        }
                    }
                }
                addPropertyStringValue(ind,MONIKER,ent.getMoniker(),ontModel);
                addPropertyStringValue(ind,BLURB,ent.getBlurb(),ontModel);
                addPropertyStringValue(ind,DESCRIPTION,ent.getDescription(),ontModel);
                addPropertyDateTimeValue(ind,SUNRISE,ent.getSunrise(), ontModel);
                addPropertyDateTimeValue(ind,SUNSET,ent.getSunset(), ontModel);
                addPropertyDateTimeValue(ind,TIMEKEY,ent.getTimekey(), ontModel);
                addPropertyDateTimeValue(ind,MODTIME,Calendar.getInstance().getTime(),ontModel);
                if (ent.getMainImageUri() != null) {
                	addPropertyResourceURIValue(ind, IND_MAIN_IMAGE, ent.getMainImageUri());
                }
                
                if (ent.getAnchor()!= null && ent.getAnchor().length()>0 && LINK != null) {
                    com.hp.hpl.jena.ontology.Individual primaryLink = ontModel.createIndividual(entURI+"_primaryLink", LINK);
                    primaryLink.addProperty(RDF.type, LINK);
                    if (log.isTraceEnabled()) {
                    	log.trace("added RDF type Link to primary link in insertNewIndividual() for new individual "+ent.getName());
                    }
                    addPropertyStringValue(primaryLink, LINK_ANCHOR, ent.getAnchor(),ontModel);
                    addPropertyStringValue(primaryLink, LINK_URL,ent.getUrl(),ontModel);
                    ind.addProperty(PRIMARY_LINK,primaryLink);
                }
                if( ent.getSearchBoost() != null ) {
                    addPropertyFloatValue(ind,SEARCH_BOOST_ANNOT, ent.getSearchBoost(), ontModel);
                }
                /* 2009-01-27 hold off on individual-level filtering for now
                ind.removeAll(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
                if (HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT != null && ent.getHiddenFromDisplayBelowRoleLevel() != null) { // only need to add if present
                    try {
                        ind.addProperty(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(ent.getHiddenFromDisplayBelowRoleLevel().getURI()));
                    } catch (Exception e) {
                        log.error("error adding HiddenFromDisplayBelowRoleLevel annotation to individual "+ent.getURI());
                    }
                }

                ind.removeAll(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
                if (PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT != null && ent.getProhibitedFromUpdateBelowRoleLevel() != null) { // only need to add if present
                    try {
                        ind.addProperty(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(ent.getProhibitedFromUpdateBelowRoleLevel().getURI()));
                    } catch (Exception e) {
                        log.error("error adding ProhibitedFromUpdateBelowRoleLevel annotation to individual "+ent.getURI());
                    }
                }
                */
            } catch (Exception e) {
                log.error("Exception inserting individual: ",e);
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualCreationEvent(getWebappDaoFactory().getUserURI(),false,entURI));
            ontModel.leaveCriticalSection();
        }
        return entURI;
    }

    public int updateIndividual(Individual ent) {
        return updateIndividualWebapp(ent);
    }

    public int updateIndividualWebapp(Individual ent) {
        initInd(ent);
        return updateIndividual(ent, getOntModelSelector().getABoxModel());
    }

    private void initInd(Individual ent) {
        ent.getAnchor();
        ent.getBlurb();
        ent.getClass();
        ent.getVClasses(false);
        ent.getDataPropertyList();
        ent.getDataPropertyStatements();
        ent.getDescription();
        ent.getExternalIds();
        ent.getFlag1Numeric();
        ent.getFlag1Set();
        ent.getFlag2Set();
        ent.getMainImageUri();
        ent.getImageUrl();
        ent.getThumbUrl();
        ent.getKeywords();
        ent.getKeywordString();
        ent.getLinksList();
        ent.getPrimaryLink();
        ent.getModTime();
        ent.getMoniker();
        ent.getName();
        ent.getNamespace();
        ent.getObjectPropertyList();
        ent.getStatus();
        ent.getStatusId();
        ent.getSunrise();
        ent.getSunset();
        ent.getTimekey();
        ent.getUrl();
        ent.getVClassURI();
        ent.getVClass();
        ent.getVClassURI();
        //ent.getHiddenFromDisplayBelowRoleLevel();
        //ent.getProhibitedFromUpdateBelowRoleLevel();
    }

    public int updateIndividual(Individual ent, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,ent.getURI()));
            com.hp.hpl.jena.ontology.Individual ind = ontModel.getIndividual(ent.getURI());
            if (ind != null) {
                if (ent.getName() != null && ( (ind.getLabel(getDefaultLanguage())==null) || (ind.getLabel(getDefaultLanguage())!=null && ent.getName()!=null && !ent.getName().equals(ind.getLabel(getDefaultLanguage())) ) ) ) {
                    ind.setLabel(ent.getName(), getDefaultLanguage());
                }
                Set<String> oldTypeURIsSet = new HashSet<String>();
                for (Iterator<Resource> typeIt = ind.listRDFTypes(true); typeIt.hasNext();) {
                    Resource t = typeIt.next();
                    if (t.getURI() != null) {
                        oldTypeURIsSet.add(t.getURI());
                    }
                }
                Set<String> newTypeURIsSet = new HashSet<String>();
                newTypeURIsSet.add(ent.getVClassURI());
                boolean conservativeTypeDeletion = false;
                try {
                    List<VClass> vcl = ent.getVClasses(false);
                    if (vcl == null) {
                        conservativeTypeDeletion = true; // if the bean has null here instead of an empty list, we don't want to trust it and just start deleting any existing types.  So we'll just update the Vitro flag-related types and leave the rest alone.
                    } else {
                        for (Iterator<VClass> typeIt = vcl.iterator(); typeIt.hasNext(); ) {
                            VClass vc = typeIt.next();
                            newTypeURIsSet.add(vc.getURI());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String flag1Set = ent.getFlag1Set();
                if (flag1Set != null) {
                    String[] flag1Value = flag1Set.split(",");
                    for (int i=0; i<flag1Value.length; i++) {
                        newTypeURIsSet.add(VitroVocabulary.vitroURI+"Flag1Value"+flag1Value[i]+"Thing");
                    }
                }
                String flag2Set = ent.getFlag2Set();
                if (flag2Set != null) {
                    String[] flag2Value = flag2Set.split(",");
                    for (int i=0; i<flag2Value.length; i++) {
                        try {
                            newTypeURIsSet.add(getFlag2ValueMap().get(flag2Value[i]).getURI());
                        } catch (NullPointerException e) {}
                    }
                }
                for (Iterator<String> oldIt = oldTypeURIsSet.iterator(); oldIt.hasNext();) {
                    String uri = oldIt.next();
                    if (!newTypeURIsSet.contains(uri)) {
                        if ( (!conservativeTypeDeletion) || (uri.indexOf(VitroVocabulary.vitroURI) == 0) ) {
                            ind.removeRDFType(ResourceFactory.createResource(uri));
                        }
                    }
                }
                for (Iterator<String> newIt = newTypeURIsSet.iterator(); newIt.hasNext();) {
                    String uri = newIt.next();
                    if (!oldTypeURIsSet.contains(uri)) {
                        ind.addRDFType(ResourceFactory.createResource(uri));
                    }
                }
                updatePropertyStringValue(ind,MONIKER,ent.getMoniker(),ontModel);
                updatePropertyStringValue(ind,BLURB,ent.getBlurb(),ontModel);
                updatePropertyStringValue(ind,DESCRIPTION,ent.getDescription(),ontModel);                
                updatePropertyDateTimeValue(ind,SUNRISE,ent.getSunrise(), ontModel);
                updatePropertyDateTimeValue(ind,SUNSET,ent.getSunset(), ontModel);
                updatePropertyDateTimeValue(ind,TIMEKEY,ent.getTimekey(), ontModel);
                updatePropertyDateTimeValue(ind,MODTIME,Calendar.getInstance().getTime(),ontModel);
                updatePropertyResourceURIValue(ind, IND_MAIN_IMAGE, ent.getMainImageUri(), ontModel);
                if (ent.getAnchor()!= null && ent.getAnchor().length()>0) {
                    if (LINK != null && PRIMARY_LINK != null) {
                        boolean updatedExisting = false;
                        try {
                            Resource primaryLink = (Resource) ind.getPropertyValue(PRIMARY_LINK);
                            if (primaryLink != null) {
                            	if (log.isTraceEnabled()) {
                            		log.trace("Modifying the primary link to entity "+ent.getName());
                            	}
                                updatePropertyStringValue(primaryLink, LINK_ANCHOR, ent.getAnchor(), ontModel);
                                updatePropertyStringValue(primaryLink, LINK_URL, ent.getUrl(), ontModel);
                                updatedExisting = true;
                            }
                        } catch (Exception e) {}
                        if (!updatedExisting) {
                        	if (log.isTraceEnabled()) {
                        		log.trace("Adding a primary link to entity "+ent.getName());
                        	}
                            ind.removeAll(PRIMARY_LINK);
                            com.hp.hpl.jena.ontology.Individual primaryLink = LINK.createIndividual(ent.getURI()+"_primaryLink");
                            primaryLink.addProperty(RDF.type, LINK);
                            addPropertyStringValue(primaryLink, LINK_ANCHOR, ent.getAnchor(), ontModel);
                            addPropertyStringValue(primaryLink, LINK_URL,ent.getUrl(), ontModel);
                            ind.addProperty(PRIMARY_LINK,primaryLink);
                        }
                    }
                } else {
                    ind.removeAll(PRIMARY_LINK);
                }
                if( ent.getSearchBoost() != null ) {
                    updatePropertyFloatValue(ind, SEARCH_BOOST_ANNOT, ent.getSearchBoost(), ontModel);
                }
                /*
                if (HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT != null) {
                    try {
                        ind.removeAll(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
                        if (ent.getHiddenFromDisplayBelowRoleLevel()!=null) {
                            String badURIErrorStr = checkURI(ent.getHiddenFromDisplayBelowRoleLevel().getURI());
                            if (badURIErrorStr == null) {
                                ind.addProperty(HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(ent.getHiddenFromDisplayBelowRoleLevel().getURI()));
                            } else {
                                log.error(badURIErrorStr);
                            }
                        }
                    } catch (Exception e) {
                        log.error("error linking individual "+ent.getURI()+" to HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL edit role");
                    }
                } else {
                    log.error("vitro:hiddenFromDisplayBelowRoleLevelAnnot property not found in RBox");
                }

                if (PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT != null) {
                    try {
                        ind.removeAll(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
                        if (ent.getProhibitedFromUpdateBelowRoleLevel()!=null) {
                            String badURIErrorStr = checkURI(ent.getProhibitedFromUpdateBelowRoleLevel().getURI());
                            if (badURIErrorStr == null) {
                                ind.addProperty(PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT, ResourceFactory.createResource(ent.getProhibitedFromUpdateBelowRoleLevel().getURI()));
                            } else {
                                log.error(badURIErrorStr);
                            }
                        }
                    } catch (Exception e) {
                        log.error("error linking individual "+ent.getURI()+" to PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL edit role");
                    }
                } else {
                    log.error("vitro:prohibitedFromUpdateBelowRoleLevelAnnot property not found in RBox");
                }
                */
            return 0;
            } else {
                return 1;
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,ent.getURI()));
            ontModel.leaveCriticalSection();
        }
    }

    public void markModified(Individual ind) {
        markModified(ind,getOntModel());
    }

    public void markModified(Individual ind, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            com.hp.hpl.jena.ontology.Individual jind = ontModel.getIndividual(ind.getURI());
            if (jind != null) {
                updatePropertyDateTimeValue(jind,MODTIME,Calendar.getInstance().getTime(),ontModel);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public int deleteIndividual(String URI) {
        return deleteIndividual(URI, getOntModelSelector().getABoxModel());
    }

    public int deleteIndividual(String URI, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            if (URI == null) {
                log.debug("Cannot remove individual with null URI");
                return 1;
            }
            ontModel.getBaseModel().notifyEvent(new IndividualDeletionEvent(getWebappDaoFactory().getUserURI(),true,URI));
            Resource res = ontModel.getResource(URI);
            if (res == null) {
                // This should never happen, but just in case
                log.error("Null resource returned from model");
                return 1;
            }
            if (res.canAs(OntResource.class)) {
                OntResource ontRes = (OntResource) res.as(OntResource.class);
                smartRemove(ontRes, ontModel);
            } else {
                ontModel.removeAll(res,null,null);
                ontModel.removeAll(null,null,res);
            }
        } finally {
            ontModel.getBaseModel().notifyEvent(new IndividualDeletionEvent(getWebappDaoFactory().getUserURI(),false,URI));
            ontModel.leaveCriticalSection();
        }
        return 0;
    }

    public int deleteIndividual(Individual ent) {
        return deleteIndividual(ent.getURI());
    }

    public int deleteIndividualWebapp(Individual ent) {
        return deleteIndividual(ent.getURI());
    }

    public Individual getIndividualByURI(String entityURI) {
        if( entityURI == null || entityURI.length() == 0 )
            return null;

        OntModel ontModel = getOntModelSelector().getABoxModel();
        
        ontModel.enterCriticalSection(Lock.READ);
        try {
        	OntResource ontRes = (entityURI.startsWith(VitroVocabulary.PSEUDO_BNODE_NS)) 
        		? (OntResource) ontModel.createResource(new AnonId(entityURI.substring(VitroVocabulary.PSEUDO_BNODE_NS.length()))).as(OntResource.class)
        		: ontModel.getOntResource(entityURI);
            Individual ent = new IndividualJena(ontRes, (WebappDaoFactoryJena) getWebappDaoFactory());
            return ent;
        } catch (Exception ex) {
            return null;
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public void fillVClassForIndividual(Individual entity) {
        entity.setVClass(getWebappDaoFactory().getVClassDao().getVClassByURI(entity.getVClassURI()));
    }

    public List<String> monikers( String typeURI ) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            HashSet<String> monikers = new HashSet<String>();
            OntClass cls = getOntModel().getOntClass(typeURI);
            ResIterator inds = getOntModel().listSubjectsWithProperty(
            		RDF.type, ResourceFactory.createResource(typeURI));
            while (inds.hasNext()) {
            	Resource ind = inds.nextResource();
                if (MONIKER != null) {
                Iterator<Statement> monikerIt = ind.listProperties(MONIKER);
                    while (monikerIt.hasNext()) {
                        Statement monikerStmt = monikerIt.next();
                        monikers.add(((Literal)monikerStmt.getObject()).getString());
                    }
                }
            }
            List<String> monikerList = new ArrayList<String>();
            if (monikers.size()>0) {
                Iterator<String> monikerIt = monikers.iterator();
                while (monikerIt.hasNext()) {
                    monikerList.add(monikerIt.next());
                }
                Collections.sort(monikerList,new Comparator<String>() {
                    public int compare( String first, String second ) {
                        if (first==null) {
                            return 1;
                        }
                        if (second==null) {
                            return -1;
                        }
                        Collator collator = Collator.getInstance();
                        return collator.compare(first,second);
                    }
                });
                return monikerList;
            }
            else
                return null;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public List<String> getKeywordsForIndividual(String entityURI) {
        KeywordDao kDao = getWebappDaoFactory().getKeywordDao();
        List<String> keywords = new LinkedList<String>();
        List<KeywordIndividualRelation> kirs = getWebappDaoFactory().getKeys2EntsDao().getKeywordIndividualRelationsByIndividualURI(entityURI);
        Iterator<KeywordIndividualRelation> kirsIt = kirs.iterator();
        while (kirsIt.hasNext()) {
            Keyword keyword = kDao.getKeywordById(kirsIt.next().getKeyId());
            keywords.add(keyword.getTerm());
        }
        return keywords;
    }

    public List<String> getKeywordsForIndividualByMode(String entityURI, String modeStr) {
        KeywordDao kDao = getWebappDaoFactory().getKeywordDao();
        List<String> keywords = new LinkedList<String>();
        List<KeywordIndividualRelation> kirs = getWebappDaoFactory().getKeys2EntsDao().getKeywordIndividualRelationsByIndividualURI(entityURI);
        Iterator<KeywordIndividualRelation> kirsIt = kirs.iterator();
        while (kirsIt.hasNext()) {
            KeywordIndividualRelation kir = kirsIt.next();
            if (kir.getMode().equalsIgnoreCase(modeStr)) {
                Keyword keyword = kDao.getKeywordById(kir.getKeyId());
                keywords.add(keyword.getTerm());
            }
        }
        return keywords;
    }

    public List<Keyword> getKeywordObjectsForIndividual(String entityURI) {
        KeywordDao kDao = getWebappDaoFactory().getKeywordDao();
        List<Keyword> keywords = new LinkedList<Keyword>();
        List<KeywordIndividualRelation> kirs = getWebappDaoFactory().getKeys2EntsDao().getKeywordIndividualRelationsByIndividualURI(entityURI);
        Iterator<KeywordIndividualRelation> kirsIt = kirs.iterator();
        while (kirsIt.hasNext()) {
            Keyword keyword = kDao.getKeywordById(kirsIt.next().getKeyId());
            keywords.add(keyword);
        }
        return keywords;
    }

	public String getIndividualURIFromNetId(String netIdStr, String netidMatchingPropertyUri) {
		if (netidMatchingPropertyUri == null) {
			return null;
		}

        Property prop = getOntModel().getProperty(netidMatchingPropertyUri);
        Literal netid = getOntModel().createLiteral(netIdStr);
        
        ResIterator stmts = null;
        try{
            stmts = getOntModel().listResourcesWithProperty(prop, netid);
            if (stmts.hasNext()) {
                return stmts.nextResource().getURI();
            } else {
            	return null;
            }
        }   finally{
            if( stmts != null ) stmts.close();
        }
    }

    /**
     * In Jena it can be difficult to get an object with a given dataproperty if
     * you do not care about the datatype or lang of the literal.  Use this
     * method if you would like to ignore the lang and datatype.  
     */
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value){        
        Property prop = null;
        if( RDFS.label.getURI().equals( dataPropertyUri )){
            prop = RDFS.label;
        }else{
            prop = getOntModel().getDatatypeProperty(dataPropertyUri);
        }

        if( prop == null ) {            
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because " + dataPropertyUri + "was not found in model.");
            return Collections.emptyList();
        }

        if( value == null ){
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because value was null");
            return Collections.emptyList();
        }
        
        Literal litv1 = getOntModel().createLiteral(value);        
        Literal litv2 = getOntModel().createTypedLiteral(value);   
        
        //warning: this assumes that any language tags will be EN
        Literal litv3 = getOntModel().createLiteral(value,"EN");        
        
        HashMap<String,Individual> individualsMap = new HashMap<String, Individual>();
                
        getOntModel().enterCriticalSection(Lock.READ);
        int count = 0;
        try{
            StmtIterator stmts
                = getOntModel().listStatements((Resource)null, prop, litv1);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(subUri);
                    individualsMap.put(subUri,new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
                }
            }
            
            stmts = getOntModel().listStatements((Resource)null, prop, litv2);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(subUri);
                    individualsMap.put(subUri,new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
                }                
            }
            
            stmts = getOntModel().listStatements((Resource)null, prop, litv3);                                           
            while(stmts.hasNext()){
                count++;
                Statement stmt = stmts.nextStatement();
                
                RDFNode sub = stmt.getSubject();
                if( sub == null || sub.isAnon() || sub.isLiteral() )                    
                    continue;                
                
                RDFNode obj = stmt.getObject();
                if( obj == null || !obj.isLiteral() )
                    continue;
                
                Literal literal = (Literal)obj;
                Object v = literal.getValue();
                if( v == null )                     
                    continue;                
                
                String subUri = ((Resource)sub).getURI();
                if( ! individualsMap.containsKey(subUri)){
                    com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(subUri);
                    individualsMap.put(subUri,new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
                }                
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        
        List<Individual> rv = new ArrayList<Individual>(individualsMap.size());
        rv.addAll(individualsMap.values());
        return rv;
    }

    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value, String datatypeUri, String lang){        
        if( datatypeUri == null && lang == null )
            return getIndividualsByDataProperty(dataPropertyUri,value);
        
        Property prop = null;
        
        if( RDFS.label.getURI().equals( dataPropertyUri )){
            prop = RDFS.label;
        }else{
            prop = getOntModel().getDatatypeProperty(dataPropertyUri);
        }
        
        if( prop == null || value == null){
            log.debug("Could not getIndividualsByDataProperty() " +
                    "because " + dataPropertyUri + "was not found in model.");
            return Collections.emptyList();
        }

        ResIterator stmts = null;
        List<Individual> inds = new ArrayList<Individual>();

        Literal literal = null;
        if( datatypeUri != null && datatypeUri.length() > 0 )
            literal = getOntModel().createTypedLiteral(value, datatypeUri);
        else if( lang != null && lang.length() > 0 )
            literal =  getOntModel().createLiteral(value,lang);
        else
            literal = getOntModel().createLiteral(value);

        getOntModel().enterCriticalSection(Lock.READ);
        try{
            stmts = getOntModel().listSubjectsWithProperty(prop, literal);
            while(stmts.hasNext()){
                Resource st = stmts.nextResource();
                if ( st.getURI() == null ) { // check to make sure this node isn't null
                    continue;
                }
                com.hp.hpl.jena.ontology.Individual ind = getOntModel().getIndividual(st.getURI());
                inds.add(new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
            }
        } finally {
            if( stmts != null ) stmts.close();
            getOntModel().leaveCriticalSection();
        }
        return inds;
    }    

    @Deprecated
    public Individual getIndividualByExternalId(int externalIdType, String externalIdValue ) {
        return null;
    }

    @Deprecated
    public Individual getIndividualByExternalId(int externalIdType, String externalIdValue, String vClassURI) {
        return null;
    }

    public String getNetId(String entityURI) {
        return null;
    }

    public String getStatus(String entityURI) {
        return null;
    }

    public Iterator<Individual> getAllOfThisTypeIterator() {
        final List<com.hp.hpl.jena.ontology.Individual> list = 
            new LinkedList<com.hp.hpl.jena.ontology.Individual>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator<com.hp.hpl.jena.ontology.Individual> allIndIt = getOntModel().listIndividuals();
            try {
                while (allIndIt.hasNext()) {
                    com.hp.hpl.jena.ontology.Individual ind = allIndIt.next();
                    
                    boolean userVisible = true;
                    //Check for non-user visible types, maybe this should be an annotation?
                    ClosableIterator<Resource> typeIt = ind.listRDFTypes(false);
                    try {
                        while (typeIt.hasNext()) {
                            Resource typeRes = typeIt.next();
                            String type = typeRes.getURI();
                            // brute forcing this until we implement a better strategy
                            if (VitroVocabulary.PORTAL.equals(type) || 
                            	VitroVocabulary.TAB.equals(type) ||
                            	VitroVocabulary.TAB_INDIVIDUALRELATION.equals(type) ||
                            	VitroVocabulary.LINK.equals(type) ||
                            	VitroVocabulary.KEYWORD.equals(type) ||
                            	VitroVocabulary.KEYWORD_INDIVIDUALRELATION.equals(type) ||
                            	VitroVocabulary.CLASSGROUP.equals(type) ||
                            	VitroVocabulary.PROPERTYGROUP.equals(type) ||
                            	VitroVocabulary.APPLICATION.equals(type)) {    	
                                userVisible = false;
                                break;
                            }
                            if( OWL.ObjectProperty.getURI().equals(type) ||
                            	OWL.DatatypeProperty.getURI().equals(type) ||
                            	OWL.AnnotationProperty.getURI().equals(type) ||
                            	RDF.type.getURI().equals(type) ){
                            	userVisible = false;
                            	break;
                        	} 
                        }
                    } finally {
                        typeIt.close();
                    }
                    if (userVisible) {
                        list.add(ind);
                    }
                    
                }
            } finally {
                allIndIt.close();
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        if (list.size() >0){
            return new Iterator<Individual>(){
                Iterator<com.hp.hpl.jena.ontology.Individual> innerIt = list.iterator();
                public boolean hasNext() { 
                    return innerIt.hasNext();                    
                }
                public Individual next() {
                    return new IndividualJena(innerIt.next(), (WebappDaoFactoryJena) getWebappDaoFactory());
                }
                public void remove() {
                    //not used
                }            
            };
        }
        else
            return null;
    }  

    public Iterator<Individual> getAllOfThisVClassIterator(String vClassURI) {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List<Individual> ents = new LinkedList<Individual>();
            OntClass cls = getOntModel().getOntClass(vClassURI);
            Iterator<? extends OntResource> indIt = cls.listInstances();
            while (indIt.hasNext()) {
            	OntResource ind = indIt.next();
                ents.add(new IndividualJena(ind, (WebappDaoFactoryJena) getWebappDaoFactory()));
            }
            return ents.iterator();
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public Iterator<Individual> getUpdatedSinceIterator(long updatedSince){
        List<Individual> ents = new ArrayList<Individual>();
        Date since = new DateTime(updatedSince).toDate();
        String sinceStr = xsdDateTimeFormat.format(since);
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Property modTimeProp = MODTIME;
            if (modTimeProp == null)
                modTimeProp = getOntModel().getProperty(VitroVocabulary.MODTIME);
            if (modTimeProp == null)
                return null; // throw an exception?
            String queryStr = "PREFIX vitro: <"+ VitroVocabulary.vitroURI+"> " +
                              "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                              "SELECT ?ent " +
                              "WHERE { " +
                              "     ?ent vitro:modTime ?modTime ." +
                              "     FILTER (xsd:dateTime(?modTime) >= \""+sinceStr+"\"^^xsd:dateTime) " +
                              "}";
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe = QueryExecutionFactory.create(query,getOntModel());
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = (QuerySolution) results.next();
                Resource res = (Resource) qs.get("?ent");
                com.hp.hpl.jena.ontology.Individual ent = getOntModel().getIndividual(res.getURI());
                if (ent != null) {
                    boolean userVisible = false;
                    ClosableIterator<Resource> typeIt = ent.listRDFTypes(true);
                    try {
                        while (typeIt.hasNext()) {
                            Resource typeRes = typeIt.next();
                            if (typeRes.getNameSpace() == null || (!NONUSER_NAMESPACES.contains(typeRes.getNameSpace()))) {
                                userVisible = true;
                                break;
                            }
                        }
                    } finally {
                        typeIt.close();
                    }
                    if (userVisible) {
                        ents.add(new IndividualJena(ent, (WebappDaoFactoryJena) getWebappDaoFactory()));
                    }
                }
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }
        return ents.iterator();
    }

    public boolean isIndividualOfClass(String vclassURI, String indURI) {
        if( vclassURI == null || indURI == null 
            || "".equals(vclassURI) || "".equals(indURI)) 
            return false;
        return getOntModel().contains(getOntModel().getResource(indURI), 
                    RDF.type ,
                    getOntModel().getResource(vclassURI));        
    }
    
	public String getUnusedURI(Individual individual) throws InsertException {
		String errMsg = null;		
		String namespace = null;
		String uri = null;
		boolean uriIsGood = false;
		
		if ( individual == null || 
			(individual.getURI() != null && individual.getURI().startsWith( DEFAULT_NAMESPACE ) ) 
			|| individual.getNamespace() == null 
			|| individual.getNamespace().length() == 0 
		    || DEFAULT_NAMESPACE.equals(individual.getNamespace()) ){
			//we always want local names like n23423 for the default namespace
			namespace = DEFAULT_NAMESPACE;
			uri = null;			
		}else if( individual.getURI() != null ){
			errMsg = getWebappDaoFactory().checkURI(individual.getURI());
			if( errMsg == null){
				uriIsGood = true;
				uri = individual.getURI();
			}else{
				throw new InsertException(errMsg);
			}
		}else{
			namespace = individual.getNamespace();
			if( namespace == null || namespace.length() == 0 )
				namespace = DEFAULT_NAMESPACE;
			String localName = individual.getName();
			
			/* try to use the specified namespace and local name */
			if (localName != null) {							
				localName = localName.replaceAll("\\W", "");
				localName = localName.replaceAll(":", "");
				if (localName.length() > 2) {
					if (Character.isDigit(localName.charAt(0))) {
						localName = "n" + localName;
					}
					uri = namespace + localName;
					errMsg = getWebappDaoFactory().checkURI(uri);
					if( errMsg == null)
						uriIsGood = true;
					else
						throw new InsertException(errMsg);					
				}
			}
			/* else try namespace + n2343 */ 			
		}
		
		Random random = new Random(System.currentTimeMillis());
		int attempts = 0;
		
		while( uriIsGood == false && attempts < 30 ){			
			String localName = "n" + random.nextInt( Math.min(Integer.MAX_VALUE,(int)Math.pow(2,attempts + 13)) );
			uri = namespace + localName;			
			errMsg = getWebappDaoFactory().checkURI(uri);
			if(  errMsg != null)
				uri = null;
			else
				uriIsGood = true;				
			attempts++;
		}
		
		if( uri == null )
			throw new InsertException("Could not create URI for individual: " + errMsg);
								
		return uri;
	}
	
}
