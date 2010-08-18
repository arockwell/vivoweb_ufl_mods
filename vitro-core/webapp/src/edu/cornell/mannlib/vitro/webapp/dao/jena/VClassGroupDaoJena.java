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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class VClassGroupDaoJena extends JenaBaseDao implements VClassGroupDao {

    private static final Log log = LogFactory.getLog(TabDaoJena.class.getName());

    public VClassGroupDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getApplicationMetadataModel();
    }
    
    public void deleteVClassGroup(VClassGroup vcg) {
    	deleteVClassGroup(vcg,getOntModel());
    }

    public void deleteVClassGroup(VClassGroup vcg, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Individual groupInd = ontModel.getIndividual(vcg.getURI());
            if (groupInd != null) {
                groupInd.remove();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public LinkedHashMap getClassGroupMap() {
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            LinkedHashMap map = new LinkedHashMap();
            List groups = new ArrayList();
            ClosableIterator groupIt = getOntModel().listIndividuals(CLASSGROUP);
            try {
                while (groupIt.hasNext()) {
                    Individual groupInd = (Individual) groupIt.next();
                    VClassGroup group = groupFromGroupIndividual(groupInd);
                    if (group!=null) {
                        groups.add(group);
                    }
                }
            } finally {
                groupIt.close();
            }
            Collections.sort(groups);
            Iterator groupsIt = groups.iterator();
            while (groupsIt.hasNext()) {
                VClassGroup group = (VClassGroup) groupsIt.next();
                map.put(group.getPublicName(), group);
            }
            return map;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }

    public VClassGroup getGroupByURI(String uri) {
        if (uri == null) {
            return null;
        }
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            Individual group = getOntModel().getIndividual(uri);
            return groupFromGroupIndividual(group);
        } catch (IllegalArgumentException ex) {
            return null;
        } finally {
            getOntModel().leaveCriticalSection();
        }
    }


    public List getPublicGroupsWithVClasses() {
        return getPublicGroupsWithVClasses(false);
    }

    public List getPublicGroupsWithVClasses(boolean displayOrder) {
        return getPublicGroupsWithVClasses(displayOrder, true);
    }

    public List getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses) {
        return getPublicGroupsWithVClasses(displayOrder, includeUninstantiatedClasses, true);
    }

    public List getPublicGroupsWithVClasses(boolean displayOrder, boolean includeUninstantiatedClasses,
            boolean getIndividualCount) {
        VClassDao classDao = getWebappDaoFactory().getVClassDao();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            List groups = new ArrayList();
            ClosableIterator groupIt = getOntModel().listIndividuals(CLASSGROUP);
            try {
                while (groupIt.hasNext()) {
                    Individual grp = (Individual) groupIt.next();
                    VClassGroup vgrp = groupFromGroupIndividual(grp);
                    if (vgrp!=null) {
                        classDao.addVClassesToGroup(vgrp, includeUninstantiatedClasses, getIndividualCount);
                        groups.add(vgrp);
                        java.util.Collections.sort(groups);
                    }
                }    
            } finally {
                groupIt.close();
            }
            // BJL23 2008-12-18
            // It's often problematic that classes don't show up in editing picklists until they're in a classgroup.
            // I'm going to try adding all other classes to a classgroup called "ungrouped"
            // We really need to rework these methods and move the filtering behavior into the nice filtering framework
            /* commenting this out until I rework the filtering DAO to use this method */
            /*
            List<VClass> ungroupedClasses = new ArrayList<VClass>();
            List<VClass> allClassList = getWebappDaoFactory().getVClassDao().getAllVclasses();
            Iterator<VClass> allClassIt = allClassList.iterator();
            while (allClassIt.hasNext()) {
            	VClass cls = allClassIt.next();
            	if (cls.getGroupURI()==null) {
            		ungroupedClasses.add(cls);
            	}
            }
            if (ungroupedClasses.size()>0) {
            	VClassGroup ungrouped = new VClassGroup();
            	ungrouped.setPublicName("ungrouped");
            	groups.add(ungrouped);
            }
            */
            if (groups.size()>0) {
                return groups;
            } else {
                classDao.addVClassesToGroups(groups);
                return groups;
            }
        } finally {
            getOntModel().leaveCriticalSection();
        }

    }

    public VClassGroup groupFromGroupIndividual(Individual groupInd) {
        if (groupInd==null) {
            return null;
        }
        VClassGroup group = new VClassGroup();
        group.setPublicName(groupInd.getLabel(null));
        group.setURI(groupInd.getURI());
        group.setNamespace(groupInd.getNameSpace());
        group.setLocalName(groupInd.getLocalName());
        try {
            group.setDisplayRank(Integer.decode(((Literal)(groupInd.getProperty(getOntModel().getDatatypeProperty(VitroVocabulary.DISPLAY_RANK)).getObject())).getString()).intValue());
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return group;
    }

    public int insertNewVClassGroup(VClassGroup vcg) {
    	insertNewVClassGroup(vcg,getOntModelSelector().getApplicationMetadataModel());
        return 0;
    }

    public int insertNewVClassGroup(VClassGroup vcg, OntModel ontModel) {
    	
    	// VitroClassGroups should really inherit from Individual objects now,
    	// but they don't (yet).
    	// I'm going to make an Individual so I can avoid duplicating URI code.
    	
    	edu.cornell.mannlib.vitro.webapp.beans.Individual groupInd = 
    		new IndividualImpl(); // We should make a factory for these
    	groupInd.setURI(vcg.getURI());
    	groupInd.setNamespace(DEFAULT_NAMESPACE+"vitroClassGroup");
    	groupInd.setName(vcg.getPublicName());
    	groupInd.setVClassURI(CLASSGROUP.getURI());
    	
    	String groupURI = null;
    	try {
    		groupURI = (new WebappDaoFactoryJena(ontModel)).getIndividualDao().insertNewIndividual(groupInd);
    	} catch (InsertException ie) {
    		throw new RuntimeException(InsertException.class.getName() + "Unable to insert class group "+groupURI, ie);
    	}
    	
    	if (groupURI != null) {
	        ontModel.enterCriticalSection(Lock.WRITE);
	        try {
	        	Individual groupJenaInd = ontModel.getIndividual(groupURI);
	            try {
	                groupJenaInd.addProperty(DISPLAY_RANK, Integer.toString(vcg.getDisplayRank()), XSDDatatype.XSDint);
	            } catch (Exception e) {
	                log.error("error setting displayRank for "+groupInd.getURI());
	            }
	        } finally {
	            ontModel.leaveCriticalSection();
	        }
	        return 0;
    	} else {
    		log.error("Unable to insert class group " + vcg.getPublicName());
    		return 1;
    	}
        
    }

    public int removeUnpopulatedGroups(List groups) {
        if (groups==null || groups.size()==0)
            return 0;
        int removedGroupsCount = 0;
        ListIterator it = groups.listIterator();
        while(it.hasNext()){
            VClassGroup group = (VClassGroup) it.next();
            List classes = group.getVitroClassList();
            if( classes == null || classes.size() < 1 ){
                removedGroupsCount++;
                it.remove();
            }
        }
        return removedGroupsCount;
    }

    public void sortGroupList(List groupList) {
        Collections.sort(groupList, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                VClassGroup first  = (VClassGroup) obj1;
                VClassGroup second = (VClassGroup) obj2;
                if (first!=null) {
                    if (second!=null) {
                        return (first.getDisplayRank()-second.getDisplayRank());
                    } else {
                        log.error("error--2nd VClassGroup is null in VClassGroupDao.getGroupList().compare()");
                    }
                } else {
                    log.error("error--1st VClassGroup is null in VClassGroupDao.getGroupList().compare()");
                }
                return 0;
            }
        });
    }

    public void updateVClassGroup(VClassGroup vcg) {
    	updateVClassGroup(vcg,getOntModelSelector().getApplicationMetadataModel());
    }

    public void updateVClassGroup(VClassGroup vcg, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            Individual groupInd = ontModel.getIndividual(vcg.getURI());
            try {
                groupInd.setLabel(vcg.getPublicName(), (String) getDefaultLanguage());
            } catch (Exception e) {
                log.error("error updating name for "+groupInd.getURI());
            }
            try {
                groupInd.removeAll(DISPLAY_RANK);
                groupInd.addProperty(DISPLAY_RANK, Integer.toString(vcg.getDisplayRank()), XSDDatatype.XSDint);
            } catch (Exception e) {
                log.error("error updating display rank for "+groupInd.getURI());
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

}
