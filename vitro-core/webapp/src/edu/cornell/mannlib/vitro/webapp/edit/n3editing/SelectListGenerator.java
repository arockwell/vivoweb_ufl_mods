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

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.search.beans.ProhibitedFromSearch;

public class SelectListGenerator {
    
    static Log log = LogFactory.getLog(SelectListGenerator.class);
    
    public static Map<String,String> getOptions(
            EditConfiguration editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact){
        if( editConfig == null ){
            log.error( "fieldToSelectItemList() must be called with a non-null EditConfiguration ");
            return Collections.EMPTY_MAP;
        }
        if( fieldName == null ){
            log.error( "fieldToSelectItemList() must be called with a non-null fieldName");
            return Collections.EMPTY_MAP;
        }                            
        
        Field field = editConfig.getField(fieldName);
        if (field==null) {
            log.error("no field \""+fieldName+"\" found from editConfig in SelectListGenerator.getOptions()");
            return Collections.EMPTY_MAP;
        }
        // now create an empty HashMap to populate and return
        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();
        // for debugging, keep a count of the number of options populated
        int optionsCount=0;

        Field.OptionsType optionsType = field.getOptionsType();
        String vclassUri = null;
        switch (optionsType){
            case HARDCODED_LITERALS:  // not auto-sorted, and empty values not removed or replaced
                List<List<String>> hardcodedLiteralOptions = field.getLiteralOptions();
                if (hardcodedLiteralOptions==null) {
                    log.error("no literalOptions List found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType UNSORTED_LITERALS specified");
                    return new HashMap <String,String>();
                }
                for(Object obj: ((Iterable)hardcodedLiteralOptions)){
                    List<String> literalPair = (List)obj;
                    String value=(String)literalPair.get(0);
                    if( value != null){  // allow empty string as a value
                        String label=(String)literalPair.get(1);
                        if (label!=null && label.trim().length() > 0) {
                            optionsMap.put(value,label);
                        } else {
                            optionsMap.put(value, value);
                        }
                        ++optionsCount;
                    }
                }
                break;                
            case LITERALS:
                List<List<String>> literalOptions = field.getLiteralOptions();
                if (literalOptions==null) {
                    log.error("no literalOptions List found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType LITERALS specified");
                    return new HashMap <String,String>();
                }
                for(Object obj: ((Iterable)literalOptions)){
                    List<String> literalPair = (List)obj;
                    String value=(String)literalPair.get(0);
                    if( value != null && value.trim().length() > 0){
                        String label=(String)literalPair.get(1);
                        if (label!=null && label.trim().length() > 0) {
                            optionsMap.put(value,label);
                        } else {
                            optionsMap.put(value, value);
                        }
                        ++optionsCount;
                    }
                }
                break;
            case STRINGS_VIA_DATATYPE_PROPERTY:
                log.debug("processing Field \""+fieldName+"\" optionType as a datatype property predicateUri in SelectListGenerator.getOptions()");
                String dataPropUri = field.getPredicateUri();
                if (dataPropUri==null || dataPropUri.equals("")){
                    log.error("no predicate dataPropUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType STRINGS_VIA_DATATYPE_PROPERTY specified");
                } else {
                    /* first test to see whether there's a default "leave blank" value specified with the literal options
                    String defaultOption=null;
                    if ((defaultOption=getDefaultOption(field))!=null) {
                        optionsMap.put(LEFT_BLANK, defaultOption);
                    } */
                    // now populate the options
                    log.debug("finding all choices for data property \""+dataPropUri+"\" in SelectListGenerator.getOptions()");
                    
                    if( wDaoFact == null ) log.error("incoming WebappDaoFactory from request is null in SelectListGenerator.getOptions().");
    
                    DataPropertyStatementDao dpsDao = wDaoFact.getDataPropertyStatementDao();
                    DataPropertyDao dpDao = wDaoFact.getDataPropertyDao();
                    DataProperty dp = dpDao.getDataPropertyByURI(dataPropUri);        
                    for (Iterator<DataPropertyStatement> i = dpsDao.getDataPropertyStatements(dp).iterator(); i.hasNext();) {
                        DataPropertyStatement dps = i.next();
                        if( dps != null ){                            
                            optionsMap.put(dps.getData().trim(),dps.getData().trim());                        
                            ++optionsCount;
                        }
                    }
                }
                break;
            case INDIVIDUALS_VIA_OBJECT_PROPERTY:
                log.debug("processing Field \""+fieldName+"\" optionType as an object property predicateUri in SelectListGenerator.getOptions()");
                String subjectUri = editConfig.getSubjectUri();
                if (subjectUri==null || subjectUri.equals("")){
                    log.error("no subjectUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType INDIVIDUALS_VIA_OBJECTPROPERTY specified");
                } else {
                    String predicateUri = field.getPredicateUri();
                    if (predicateUri==null || predicateUri.equals("")){
                        log.error("no predicateUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType INDIVIDUALS_VIA_OBJECTPROPERTY specified");
                    } else {
                        // first test to see whether there's a default "leave blank" value specified with the literal options
                        String defaultOption=null;
                        if ((defaultOption=getDefaultOption(field))!=null) {
                            optionsMap.put(LEFT_BLANK, defaultOption);
                        }
                        // now populate the options
                        log.debug("finding range individuals for subject \""+subjectUri+"\" and object property \""+predicateUri+"\" in SelectListGenerator.getOptions()");
                        
                        if( wDaoFact == null ) log.error("could not get WebappDaoFactory from request in SelectListGenerator.getOptions().");
    
                        Individual subject = wDaoFact.getIndividualDao().getIndividualByURI(subjectUri);
                        if( subject == null ) log.error("could not get individual for subject uri "+subjectUri+" in SelectListGenerator.getOptions()");
    
                        ObjectProperty objProp = wDaoFact.getObjectPropertyDao().getObjectPropertyByURI(predicateUri);
                        if( objProp == null ) 
                            log.error("could not get object property for predicate "+predicateUri+" in SelectListGenerator.getOptions()");
    
                        List <VClass> vclasses = new ArrayList<VClass>();
                        vclasses = wDaoFact.getVClassDao().getVClassesForProperty(subject.getVClassURI(),predicateUri);
                        if( vclasses == null ){
                            log.error("no owl:Class found for predicate " + predicateUri );
                            break;
                        }
                        if( vclasses.size() == 0 )
                            log.error("no owl:Class found for predicate " + predicateUri );
                        
                        HashMap<String,Individual> indMap = new HashMap<String,Individual>();
                        for ( VClass vclass :  vclasses){
                            for( Individual ind : wDaoFact.getIndividualDao().getIndividualsByVClassURI(vclass.getURI(),-1,-1)) {
                                if( !indMap.containsKey(ind.getURI())) {
                                    indMap.put(ind.getURI(),ind);
                                }
                            }
                        }
                        List<Individual> individuals = new ArrayList<Individual>(indMap.values());
    
                        List<ObjectPropertyStatement> stmts = subject.getObjectPropertyStatements();
                                                        if( stmts == null ) log.error("object properties for subject were null in SelectListGenerator.getOptions()");
    
                        individuals = removeIndividualsAlreadyInRange(individuals,stmts,predicateUri,editConfig.getObject());
                        //Collections.sort(individuals,new compareIndividualsByName());
    
                        ProhibitedFromSearch pfs = editConfig.getProhibitedFromSearch();
                        
                        for( Individual ind : individuals ){
                            String uri = ind.getURI();
                            if( uri != null ){   
                            	boolean prohibited = false;
                            	if (pfs != null) {
                            		for (VClass vc : ind.getVClasses()) {
                            			if (vc.getURI() != null) {
                            				if (pfs.isClassProhibited(ind.getVClassURI())) {
                            					prohibited = true;
                            					break;
                            				}
                            			}
                            		}
                            	}
                            	if (!prohibited) {
                            		optionsMap.put(uri,ind.getName().trim());                        
                            		++optionsCount;
                            	}
                            }
                        }
                    }
                }
                break;
            case INDIVIDUALS_VIA_VCLASS: //so we have a vclass URI
                vclassUri = field.getObjectClassUri();
                if (vclassUri==null || vclassUri.equals("")){
                    log.error("no vclassUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType INDIVIDUALS_VIA_VCLASS specified");
                } else {
                    // first test to see whether there's a default "leave blank" value specified with the literal options
                    String defaultOption=null;
                    if ((defaultOption=getDefaultOption(field))!=null) {
                        optionsMap.put(LEFT_BLANK, defaultOption);
                    }
                    // now populate the options                
                    if( wDaoFact == null ) log.error("could not get WebappDaoFactory from request in SelectListGenerator.getOptions().");
    
                    // if reasoning isn't available, we will also need to add 
                    // individuals asserted in subclasses
                    boolean inferenceAvailable = false;
                    if (wDaoFact instanceof WebappDaoFactoryJena) {
                    	PelletListener pl = ((WebappDaoFactoryJena) wDaoFact)
                    			.getPelletListener();
                    	if (pl != null && pl.isConsistent() 
                    	    && !pl.isInErrorState() 
                    	    && !pl.isReasoning()) {
                    		inferenceAvailable = true;
                    	}
                    }
                    
                    VClass vclass = wDaoFact.getVClassDao().getVClassByURI( vclassUri );
                    if( vclass == null ) { 
                        log.error("Cannot find owl:Class " + vclassUri + " in the model" );
                        optionsMap.put("", "Could not find class " + vclassUri);
                    }else{                
                        Map<String, Individual> individualMap = new HashMap<String, Individual>();
                		
                        for (Individual ind : wDaoFact.getIndividualDao().getIndividualsByVClassURI(vclass.getURI(),-1,-1)) {
                        	if (ind.getURI() != null) {                        		
                            	individualMap.put(ind.getURI(), ind);
                        	}
                        }
                        
                        if (!inferenceAvailable) {
                        	for (String subclassURI : wDaoFact.getVClassDao().getAllSubClassURIs(vclass.getURI())) {
                        		 for (Individual ind : wDaoFact.getIndividualDao().getIndividualsByVClassURI(subclassURI,-1,-1)) {
                                 	if (ind.getURI() != null) {
                                 		individualMap.put(ind.getURI(), ind);
                                 	}
                                 }
                        	}
                        }
                        
                        List<Individual> individuals = new ArrayList<Individual>();
                        individuals.addAll(individualMap.values());
                        Collections.sort(individuals);
                        
                        if (individuals.size()==0){ 
                            log.error("No individuals of type "+vclass.getName()+" to add to pick list in SelectListGenerator.getOptions(); check portal visibility");
                            optionsMap.put("", "No " + vclass.getName() + " found");
                        }else{
                        	ProhibitedFromSearch pfs = editConfig.getProhibitedFromSearch();
                            for( Individual ind : individuals ) {
                                String uri = ind.getURI();
                                if( uri != null ) {       
                                	boolean prohibited = false;
                                	if (pfs != null) {
                                		for (VClass vc : ind.getVClasses()) {
                                			if (vc.getURI() != null) {
                                				if (pfs.isClassProhibited(ind.getVClassURI())) {
                                					prohibited = true;
                                					break;
                                				}
                                				
                                			}
                                		}
                                	}
                                	if(!prohibited) {
                                		optionsMap.put(uri,ind.getName().trim());                        
                                		++optionsCount;
                                	}
                                }
                            }
                        }
                    }
                }
                break;
            case MONIKERS_VIA_VCLASS: //so we have a vclass URI
                vclassUri = field.getObjectClassUri();
                if (vclassUri==null || vclassUri.equals("")){
                    log.error("no vclassUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType MONIKERS_VIA_VCLASS specified");
                } else {
                    // first test to see whether there's a default "leave blank" value specified with the literal options
                    String defaultOption=null;
                    if ((defaultOption=getDefaultOption(field))!=null) {
                        optionsMap.put(LEFT_BLANK, defaultOption);
                    }
                    // now populate the options                
                    if( wDaoFact == null ) log.error("could not get WebappDaoFactory from request in SelectListGenerator.getOptions().");
    
                    VClass vclass = wDaoFact.getVClassDao().getVClassByURI( vclassUri );
                    if( vclass == null ) { 
                        log.error("Cannot find owl:Class " + vclassUri + " in the model" );
                        optionsMap.put("", "Could not find class " + vclassUri);
                    }else{                
                        List<Individual> individuals = wDaoFact.getIndividualDao().getIndividualsByVClassURI(vclass.getURI(),-1,-1);                                   
                        if (individuals.size()==0){ 
                            log.error("No individuals of type "+vclass.getName()+" to add to pick list in SelectListGenerator.getOptions(); check portal visibility");
                            optionsMap.put("", "No " + vclass.getName() + " found");
                        }else{                
                            for( Individual ind : individuals ) {
                                // note that don't want the individual URI -- the option should hold 2 copies of the moniker //
                                if (ind.getMoniker()!=null && ind.getMoniker().trim().length()>0) {
                                    optionsMap.put(ind.getMoniker(),ind.getMoniker().trim());                        
                                    ++optionsCount;
                                }
                            }
                        }
                    }
                }
                break;
                
            case CHILD_VCLASSES: //so we have a vclass URI
                vclassUri = field.getObjectClassUri();
                if (vclassUri==null || vclassUri.equals("")){
                    log.error("no vclassUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType CHILD_VCLASSES specified");
                } else {
                    // first test to see whether there's a default "leave blank" value specified with the literal options
                    String defaultOption=null;
                    if ((defaultOption=getDefaultOption(field))!=null) {
                        optionsMap.put(LEFT_BLANK, defaultOption);
                    }
                    // now populate the options                
                    if( wDaoFact == null ) log.error("could not get WebappDaoFactory from request in SelectListGenerator.getOptions().");
    
                    VClassDao vclassDao = wDaoFact.getVClassDao();
                    List<String> subClassList = vclassDao.getAllSubClassURIs(vclassUri);
                    if( subClassList == null || subClassList.size()==0 ) { 
                        log.debug("No subclasses of " + vclassUri + " found in the model so only default value from field's literalOptions will be used" );
                    } else {                
                        for( String subClassUri : subClassList ) {
                            VClass subClass = vclassDao.getVClassByURI(subClassUri);
                            if( subClass != null && !OWL.Nothing.getURI().equals(subClassUri)) {                        
                                optionsMap.put(subClassUri,subClass.getName().trim());                        
                                ++optionsCount;
                            }
                        }
                    }
                }
                break;

            case CHILD_VCLASSES_WITH_PARENT: //so we have a vclass URI
                vclassUri = field.getObjectClassUri();
                if (vclassUri==null || vclassUri.equals("")){
                    log.error("no vclassUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType CHILD_VCLASSES specified");
                } else {
                    // first test to see whether there's a default "leave blank" value specified with the literal options
                    String defaultOption=null;
                    if ((defaultOption=getDefaultOption(field))!=null) {
                        optionsMap.put(LEFT_BLANK, defaultOption);
                    }
                    // now populate the options                
                    if( wDaoFact == null ) log.error("could not get WebappDaoFactory from request in SelectListGenerator.getOptions().");
    
                    VClassDao vclassDao = wDaoFact.getVClassDao();
                    List<String> subClassList = vclassDao.getAllSubClassURIs(vclassUri);
                    if( subClassList == null || subClassList.size()==0 ) { 
                        log.debug("No subclasses of " + vclassUri + " found in the model so only default value from field's literalOptions will be used" );
                    } else {                
                        for( String subClassUri : subClassList ) {
                            VClass subClass = vclassDao.getVClassByURI(subClassUri);
                            if( subClass != null && !OWL.Nothing.getURI().equals(subClassUri)) {                        
                                optionsMap.put(subClassUri,subClass.getName().trim());                        
                                ++optionsCount;
                            }
                        }
                    optionsMap.put(vclassUri, "Other");
                    ++optionsCount;
                    }
                }
                break;
                
            case VCLASSGROUP: 

                String classGroupUri = field.getObjectClassUri(); // we're overloading this property to specify the classgroup
                if (classGroupUri==null || classGroupUri.equals("")){
                    log.error("no classGroupUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType VCLASSGROUP specified");
                } else {
                    // first test to see whether there's a default "leave blank" value specified with the literal options
                    String defaultOption=null;
                    if ((defaultOption=getDefaultOption(field))!=null) {
                        optionsMap.put(LEFT_BLANK, defaultOption);
                    }
                    // now populate the options                
                    if( wDaoFact == null ) log.error("could not get WebappDaoFactory from request in SelectListGenerator.getOptions().");
                    
                    VClassGroupDao vcgd = wDaoFact.getVClassGroupDao();
                    
                    // Need to call this method to populate the classgroups - otherwise the classgroup class list is empty
                    List vClassGroups = vcgd.getPublicGroupsWithVClasses(); 
                    
                    if (vClassGroups == null) {
                        log.error("No class groups found, so only default value from field's literalOptions will be used.");
                    } else {                          
                        VClassGroup vClassGroup = null;
                        for (Object o : vClassGroups) {
                            VClassGroup vcg = (VClassGroup) o;
                            if (vcg.getURI().equals(classGroupUri)) {
                                vClassGroup = vcg;
                                break;
                            }
                        }
                        if (vClassGroup == null) {
                            log.error("No class group with uri " + classGroupUri + "found, so only default value from field's literalOptions will be used.");
                        } else {                        
                            List<VClass> vClassList = vClassGroup.getVitroClassList();

                            if( vClassList == null || vClassList.size()==0 ) { 
                                log.debug("No classes in class group " + classGroupUri + " found in the model, so only default value from field's literalOptions will be used" );
                            } else {     
                                for( VClass vClass : vClassList ) {
                                    String vClassUri = vClass.getURI();
                                    if( vClass != null && !OWL.Nothing.getURI().equals(vClassUri)) {                        
                                        optionsMap.put(vClassUri,vClass.getName().trim());                        
                                        ++optionsCount;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
                
            case UNDEFINED :
                log.error("optionsType \"UNDEFINED\" for Field \""+fieldName+"\" in SelectListGenerator.getOptions()");
                break;
            default: log.error("unknown optionsType "+optionsType.toString()+" for Field \""+fieldName+"\" in SelectListGenerator.getOptions()");
        }
        log.debug("added "+optionsCount+" options for field \""+fieldName+"\" in SelectListGenerator.getOptions()");
        return optionsMap;               
    }
    
    /**
     * The default option is used when a option list is being auto
     * generated from a VClass or an ObjectProperty.  If there is an
     * item in the literals item list then the name of it will be used
     * as the text to display for an empty string value option. 
     * 
     * Having an option with an empty string for a Field that expects
     * a URI will cause the form processing to assume that the field 
     * was left blank.
     * 
     * @param field
     * @return
     */
    private static String getDefaultOption(Field field) {
        List <List<String>> defaultOptions = (List<List<String>>) field.getLiteralOptions();
        if (defaultOptions!=null) {
            for(Object obj: ((Iterable)defaultOptions)) {
                List<String> pair = (List<String>)obj;
                String value = pair.get(0);
                String label = pair.get(1);
                if( label != null && label.trim().length() > 0){
                    return label; // don't want to return a value
                }
            }
        }
        return null;
    }
    
    // copied from OptionsForPropertyTag.java in the thought that class may be deprecated
    private static List<Individual> removeIndividualsAlreadyInRange(List<Individual> individuals,
            List<ObjectPropertyStatement> stmts, String predicateUri, String objectUriBeingEdited){
        log.debug("starting to check for duplicate range individuals in SelectListGenerator.removeIndividualsAlreadyInRange() ...");
        HashSet<String>  range = new HashSet<String>();

        for(ObjectPropertyStatement ops : stmts){
            if( ops.getPropertyURI().equals(predicateUri))
                range.add( ops.getObjectURI() );
        }

        int removeCount=0;
        ListIterator<Individual> it = individuals.listIterator();
        while(it.hasNext()){
            Individual ind = it.next();
            if( range.contains( ind.getURI()) && !(ind.getURI().equals(objectUriBeingEdited)) ) {
                it.remove();
                ++removeCount;
            }
        }
        log.debug("removed "+removeCount+" duplicate range individuals");
        return individuals;
    }

    private static final String LEFT_BLANK = "";
}
