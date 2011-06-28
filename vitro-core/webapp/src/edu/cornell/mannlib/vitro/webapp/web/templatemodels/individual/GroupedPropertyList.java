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

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class GroupedPropertyList extends BaseTemplateModel {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(GroupedPropertyList.class);
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    @SuppressWarnings("serial")
    private static final List<String> VITRO_PROPS_TO_ADD_TO_LIST = new ArrayList<String>() {{
        add(VitroVocabulary.PRIMARY_LINK);
        add(VitroVocabulary.ADDITIONAL_LINK);
        add(VitroVocabulary.IND_MAIN_IMAGE);
    }}; 
    
    private Individual subject;
    private VitroRequest vreq;
    private WebappDaoFactory wdf;

    private List<PropertyGroupTemplateModel> groups;
    
    GroupedPropertyList(Individual subject, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        this.subject = subject;
        this.vreq = vreq;
        this.wdf = vreq.getWebappDaoFactory();
        
        boolean editing = policyHelper != null;
        
        // Create the property list for the subject. The properties will be put into groups later.
        List<Property> propertyList = new ArrayList<Property>();
        
        // First get all the object properties that occur in statements in the db with this subject as subject.
        // This may include properties that are not defined as "possible properties" for a subject of this class,
        // so we cannot just rely on getting that list.
        List<ObjectProperty> populatedObjectPropertyList = subject.getPopulatedObjectPropertyList();
        propertyList.addAll(populatedObjectPropertyList);
           
        // If editing this page, merge in object properties applicable to the individual that are currently
        // unpopulated, so the properties are displayed to allow statements to be added to these properties.
        // RY In future, we should limit this to properties that the user CAN add properties to.
        if (editing) {
            mergeAllPossibleObjectProperties(populatedObjectPropertyList, propertyList);
        }
        
        // Now do much the same with data properties: get the list of populated data properties, then add in placeholders for missing ones 
        // rjy7 Currently we are getting the list of properties in one sparql query, then doing a separate query
        // to get values for each property. This could be optimized by doing a single query to get a map of properties to 
        // DataPropertyStatements. Note that this does not apply to object properties, because the queries
        // can be customized and thus differ from property to property. So it's easier for now to keep the
        // two working in parallel.
        List<DataProperty> populatedDataPropertyList = subject.getPopulatedDataPropertyList();
        propertyList.addAll(populatedDataPropertyList);
    
        if (editing) {
            mergeAllPossibleDataProperties(propertyList);           
        }
    
        sort(propertyList); 
    
        // Put the list into groups        
        List<PropertyGroup> propertyGroupList = addPropertiesToGroups(propertyList);
    
        // Build the template data model from the groupList
        groups = new ArrayList<PropertyGroupTemplateModel>(propertyGroupList.size());
        for (PropertyGroup propertyGroup: propertyGroupList) {
            groups.add(new PropertyGroupTemplateModel(vreq, propertyGroup, subject, 
                    policyHelper, populatedDataPropertyList, populatedObjectPropertyList));
        }   
        
        if (!editing) {
            pruneEmptyProperties();
        }        
    
    }

    private void pruneEmptyProperties() {
        Iterator<PropertyGroupTemplateModel> iGroups = groups.iterator();
        while (iGroups.hasNext()) {
            PropertyGroupTemplateModel pgtm = iGroups.next();
            Iterator<PropertyTemplateModel> iProperties = pgtm.getProperties().iterator();
            while (iProperties.hasNext()) {
                PropertyTemplateModel property = iProperties.next();
                if (property instanceof ObjectPropertyTemplateModel) {
                    if ( ( (ObjectPropertyTemplateModel) property).isEmpty() ) {                        
                        iProperties.remove();
                    }
                } else if ( ( (DataPropertyTemplateModel) property).isEmpty() ) {                                            
                    iProperties.remove();
                }
            } 
            if (pgtm.isEmpty()) {
                iGroups.remove();
            }
        }        
    }
    
    @SuppressWarnings("unchecked")
    protected void sort(List<Property> propertyList) {            
        try {
            Collections.sort(propertyList, new PropertyRanker(vreq));
        } catch (Exception ex) {
            log.error("Exception sorting merged property list: " + ex.getMessage());
        }
    }
    
    private void mergeAllPossibleObjectProperties(List<ObjectProperty> populatedObjectPropertyList, List<Property> propertyList) {
        
        // There is no ObjectPropertyDao.getAllPossibleObjectPropertiesForIndividual() parallel to 
        // DataPropertyDao.getAllPossibleDatapropsForIndividual(). The comparable method for object properties
        // is defined using PropertyInstance rather than ObjectProperty.
        PropertyInstanceDao piDao = wdf.getPropertyInstanceDao();
        Collection<PropertyInstance> allPropInstColl =  piDao.getAllPossiblePropInstForIndividual(subject.getURI());        
        if (allPropInstColl != null) {
            for (PropertyInstance pi : allPropInstColl) {
                if (pi != null) {
                    if (! alreadyOnObjectPropertyList(populatedObjectPropertyList, pi)) {
                        addObjectPropertyToPropertyList(pi.getPropertyURI(), propertyList);
                    }
                } else {
                    log.error("a property instance in the Collection created by PropertyInstanceDao.getAllPossiblePropInstForIndividual() is unexpectedly null");
                }
            }
        } else {
            log.error("a null Collection is returned from PropertyInstanceDao.getAllPossiblePropInstForIndividual()");
        } 
        
        // These properties are outside the ontologies (in vitro and vitro public) but need to be added to the list.
        // In future, vitro ns props will be phased out. Vitro public properties should be changed so they do not
        // constitute a special case (i.e., included in piDao.getAllPossiblePropInstForIndividual()).
        for (String propertyUri : VITRO_PROPS_TO_ADD_TO_LIST) {
            if ( ! alreadyOnPropertyList(propertyList, propertyUri) ) {
                addObjectPropertyToPropertyList(propertyUri, propertyList);
            }                
        }
    }    

    private boolean alreadyOnObjectPropertyList(List<ObjectProperty> opList, PropertyInstance pi) {
        if (pi.getPropertyURI() == null) {
            return false;
        }
        for (ObjectProperty op : opList) {
            if (op.getURI() != null && op.getURI().equals(pi.getPropertyURI())) {
                return op.isSubjectSide() == pi.getSubjectSide();
            }
        }
        return false;
    }
    
    private void addObjectPropertyToPropertyList(String propertyUri, List<Property> propertyList) {
        ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
        ObjectProperty op = opDao.getObjectPropertyByURI(propertyUri);
        if (op == null) {
            log.error("ObjectProperty op returned null from opDao.getObjectPropertyByURI()");
        } else if (op.getURI() == null) {
            log.error("ObjectProperty op returned with null propertyURI from opDao.getObjectPropertyByURI()");
        } else {          
            propertyList.add(op);
        }         
    }
    
    protected void mergeAllPossibleDataProperties(List<Property> propertyList) {
        DataPropertyDao dpDao = wdf.getDataPropertyDao();
        Collection <DataProperty> allDatapropColl = dpDao.getAllPossibleDatapropsForIndividual(subject.getURI());
        if (allDatapropColl != null) {
            for (DataProperty dp : allDatapropColl ) {
                if (dp!=null) {
                    if (dp.getURI() == null) {
                        log.error("DataProperty dp returned with null propertyURI from dpDao.getAllPossibleDatapropsForIndividual()");
                    } else if (! alreadyOnPropertyList(propertyList, dp)) {
                        propertyList.add(dp);
                    }
                } else {
                    log.error("a data property in the Collection created in DataPropertyDao.getAllPossibleDatapropsForIndividual() is unexpectedly null)");
                }
            }
        } else {
            log.error("a null Collection is returned from DataPropertyDao.getAllPossibleDatapropsForIndividual())");
        }        
    }

    private boolean alreadyOnPropertyList(List<Property> propertyList, Property p) {
        if (p.getURI() == null) {
            log.error("Property p has no propertyURI in alreadyOnPropertyList()");
            return true; // don't add to list
        }
        return (alreadyOnPropertyList(propertyList, p.getURI()));
    }
    
    private boolean alreadyOnPropertyList(List<Property> propertyList, String propertyUri) {
        for (Property p : propertyList) {
            String uri = p.getURI();
            if (uri != null && uri.equals(propertyUri)) {
                return true;
            }
        }
        return false;        
    }
    
    private List<PropertyGroup> addPropertiesToGroups(List<Property> propertyList) {
    
        // Get the property groups
        PropertyGroupDao pgDao = wdf.getPropertyGroupDao(); 
        List<PropertyGroup> groupList = pgDao.getPublicGroups(false); // may be returned empty but not null
        // To test no property groups defined, use: 
        // List<PropertyGroup> groupList = new ArrayList<PropertyGroup>(); 
                                                
        int groupCount = groupList.size();
    
        /* 
         * If no property groups are defined, create a dummy group with a null name to signal to the template that it's
         * not a real group. This allows the looping structure in the template to be the same whether there are groups or not.
         */
        if (groupCount == 0) {
            log.warn("groupList has no groups on entering addPropertiesToGroups(); will create a new group");
            PropertyGroup dummyGroup = pgDao.createDummyPropertyGroup(null, 1);
            dummyGroup.getPropertyList().addAll(propertyList);
            sortGroupPropertyList(dummyGroup.getName(), propertyList);
            groupList.add(dummyGroup);
            return groupList;            
        } 
    
        /* 
         * This group will hold properties that are not assigned to any groups. In case no real property groups are
         * populated, we end up with the dummy group case above, and we will change the name to null to signal to the
         * template that it shouldn't be treated like a group.
         */
        PropertyGroup groupForUnassignedProperties = pgDao.createDummyPropertyGroup("", MAX_GROUP_DISPLAY_RANK);
        
        if (groupCount > 1) {
            try {
                Collections.sort(groupList);
            } catch (Exception ex) {
                log.error("Exception on sorting groupList in addPropertiesToGroups()");
            }                    
        }
    
        populateGroupListWithProperties(groupList, groupForUnassignedProperties, propertyList);
    
        // Remove unpopulated groups
        try {
            int removedCount = pgDao.removeUnpopulatedGroups(groupList);
            if (removedCount == 0) {
                log.warn("Of "+groupCount+" groups, none removed by removeUnpopulatedGroups");
            }
            groupCount -= removedCount;      
        } catch (Exception ex) {
            log.error("Exception on trying to prune groups list with properties: "+ex.getMessage());
        }
        
        // If the group for unassigned properties is populated, add it to the group list.
        if (groupForUnassignedProperties.getPropertyList().size() > 0) {
            groupList.add(groupForUnassignedProperties);  
            // If no real property groups are populated, the groupForUnassignedProperties moves from case 2 to case 1 above, so change
            // the name to null to signal to the templates that there are no real groups.
            if (groupCount == 0) {
                groupForUnassignedProperties.setName(null);
            }
        }
        
        return groupList;
    }
    
    private void populateGroupListWithProperties(List<PropertyGroup> groupList, 
            PropertyGroup groupForUnassignedProperties, List<Property> propertyList) {
        
        // Assign the properties to the groups
        for (PropertyGroup pg : groupList) {
             if (pg.getPropertyList().size()>0) {
                 pg.getPropertyList().clear();
             }
             for (Property p : propertyList) {
                 if (p.getURI() == null) {
                     log.error("Property p has null URI in populateGroupsListWithProperties()");
                 // If the property is not assigned to any group, add it to the group for unassigned properties
                 } else if (p.getGroupURI()==null) {
                     if (groupForUnassignedProperties!=null) { 
                         // RY How could it happen that it's already in the group? Maybe we can remove this test.
                         if (!alreadyOnPropertyList(groupForUnassignedProperties.getPropertyList(),p)) {                          
                             groupForUnassignedProperties.getPropertyList().add(p);
                             if (log.isDebugEnabled()) {
                                 log.debug("adding property " + getLabel(p) + " to group for unassigned properties");
                             }                             
                         }
                     } 
                 // Otherwise, if the property is assigned to this group, add it to the group if it's not already there
                 } else if (p.getGroupURI().equals(pg.getURI())) {
                     // RY How could it happen that it's already in the group? Maybe we can remove this case.
                     if (!alreadyOnPropertyList(pg.getPropertyList(),p)) {
                         pg.getPropertyList().add(p);
                     }
                 }
             }
             sortGroupPropertyList(pg.getName(), pg.getPropertyList());
        }
    }
    
    private void sortGroupPropertyList(String groupName, List<Property> propertyList) {
        if (propertyList.size()>1) {
            try {
                Collections.sort(propertyList,new Property.DisplayComparatorIgnoringPropertyGroup());
            } catch (Exception ex) {
                log.error("Exception sorting property group "+ groupName + " property list: "+ex.getMessage());
            }
        }       
    }

    private class PropertyRanker implements Comparator {
    
        WebappDaoFactory wdf;
        PropertyGroupDao pgDao;
    
        private PropertyRanker(VitroRequest vreq) {
            this.wdf = vreq.getWebappDaoFactory();
            this.pgDao = wdf.getPropertyGroupDao();
        }
        
        public int compare (Object o1, Object o2) {
            Property p1 = (Property) o1;
            Property p2 = (Property) o2;
            
            // sort first by property group rank; if the same, then sort by property rank
            final int MAX_GROUP_RANK=99;
            
            int p1GroupRank=MAX_GROUP_RANK;
            try {
                if (p1.getGroupURI()!=null) {
                    PropertyGroup pg1 = pgDao.getGroupByURI(p1.getGroupURI());
                    if (pg1!=null) {
                        p1GroupRank=pg1.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p1GroupRank for group "+p1.getLabel());
            }
            
            int p2GroupRank=MAX_GROUP_RANK;
            try {
                if (p2.getGroupURI()!=null) {
                    PropertyGroup pg2 = pgDao.getGroupByURI(p2.getGroupURI());
                    if (pg2!=null) {
                        p2GroupRank=pg2.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p2GroupRank for group "+p2.getLabel());
            }
            
            // int diff = pgDao.getGroupByURI(p1.getGroupURI()).getDisplayRank() - pgDao.getGroupByURI(p2.getGroupURI()).getDisplayRank();
            int diff=p1GroupRank - p2GroupRank;
            if (diff==0) {
                diff = determineDisplayRank(p1) - determineDisplayRank(p2);
                if (diff==0) {
                    return getLabel(p1).compareTo(getLabel(p2));
                } else {
                    return diff;
                }
            }
            return diff;
        }
        
        private int determineDisplayRank(Property p) {
            if (p instanceof DataProperty) {
                DataProperty dp = (DataProperty)p;
                return dp.getDisplayTier();
            } else if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty)p;
                String tierStr = op.getDomainDisplayTier(); // no longer used: p.isSubjectSide() ? op.getDomainDisplayTier() : op.getRangeDisplayTier();
                try {
                    return Integer.parseInt(tierStr);
                } catch (NumberFormatException ex) {
                    log.error("Cannot decode object property display tier value "+tierStr+" as an integer");
                }
            } else {
                log.error("Property is of unknown class in PropertyRanker()");  
            }
            return 0;
        }
    }
    
    // Since we're now including some vitro properties in the property list, which don't have labels,
    // use their local name instead.
    private String getLabel(Property property) {
        String label = property.getLabel();
        if (label == null) {
            label = property.getLocalName();
        }
        return label;
    }
    
    /* Access methods for templates */
    
    public List<PropertyGroupTemplateModel> getAll() {
        return groups;
    }
    
    public PropertyTemplateModel getProperty(String propertyUri) {

        for (PropertyGroupTemplateModel pgtm : groups) {
            List<PropertyTemplateModel> properties = pgtm.getProperties();
            for (PropertyTemplateModel ptm : properties) {
                if (propertyUri.equals(ptm.getUri())) {
                    return ptm;
                }
            }
        }        
        return null;
    }
    
    public PropertyTemplateModel getPropertyAndRemoveFromList(String propertyUri) {

        for (PropertyGroupTemplateModel pgtm : groups) {
            List<PropertyTemplateModel> properties = pgtm.getProperties();
            for (PropertyTemplateModel ptm : properties) {
                if (propertyUri.equals(ptm.getUri())) { 
                    // Remove the property from the group
                    properties.remove(ptm);
                    // If this is the only property in the group, remove the group as well
                    if (properties.size() == 0) {
                        groups.remove(pgtm);   
                    }
                    return ptm;
                }
            }
        }        
        return null;
    }

}

