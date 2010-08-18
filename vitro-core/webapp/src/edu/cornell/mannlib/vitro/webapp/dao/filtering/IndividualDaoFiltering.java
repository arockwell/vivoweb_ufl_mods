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

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.jga.algorithms.Filter;
import net.sf.jga.algorithms.Summarize;
import net.sf.jga.algorithms.Transform;
import net.sf.jga.fn.UnaryFunctor;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;


class IndividualDaoFiltering extends BaseFiltering implements IndividualDao{
    IndividualDao innerIndividualDao;
    VitroFilters filters;

    public IndividualDaoFiltering(IndividualDao individualDao,
            VitroFilters filters2) {
        this.innerIndividualDao = individualDao;
        this.filters = filters2;
    }


    protected List<Individual> filterAndWrap(Collection<Individual> cin, VitroFilters filters){
        if( cin == null || cin.size() == 0) return Collections.EMPTY_LIST;

        ArrayList<Individual>  filteredList = new ArrayList<Individual>();
        Filter.filter(cin,filters.getIndividualFilter(),filteredList);
        
        ArrayList<Individual> cout  = new ArrayList<Individual>();
        for(Individual ind : filteredList){
            cout.add( new IndividualFiltering(ind,filters));
        }        
        return cout;
    }

    protected Iterator<Individual> filterAndWrap(Iterator<Individual> it, VitroFilters filters){
        if( it == null ) return null;
        if( filters == null ) return it;
        return Transform.transform(    
                Filter.filter(it,filters.getIndividualFilter()),
                new ToFilteredIndividual(filters));        
    }
    
    /* **************** methods that filter ****************** */
    public Individual getIndividualByURI(String individualURI) {
        Individual ind = innerIndividualDao.getIndividualByURI(individualURI);
        if( ind != null && filters.getIndividualFilter().fn(ind))           
            return new IndividualFiltering(ind, filters);
        else
            return null;        
    }
    
    @Deprecated
    public Individual getIndividualByExternalId(int externalIdType,
            String externalIdValue, String classURI) {
        Individual ind = innerIndividualDao.getIndividualByExternalId(externalIdType,externalIdValue,classURI);
        if( ind != null &&  filters.getIndividualFilter().fn(ind) )
            return new IndividualFiltering(ind, filters );
        else
            return null;
    }

    @Deprecated
    public Individual getIndividualByExternalId(int externalIdType,
            String externalIdValue) {
        Individual ind = innerIndividualDao.getIndividualByExternalId(externalIdType,externalIdValue);
        if( ind != null && filters.getIndividualFilter().fn(ind))
            return new IndividualFiltering(ind,filters);
        else
            return null;
    }
    public void fillVClassForIndividual(Individual individual) {
        innerIndividualDao.fillVClassForIndividual(individual);
    }


    public String getIndividualURIFromNetId(String netIdStr) {
        String uri = innerIndividualDao.getIndividualURIFromNetId(netIdStr);
        if( uri == null ) return null;
        Individual ent = getIndividualByURI(uri);
        if( ent != null && filters.getIndividualFilter().fn(ent) )
            return uri;
        else
            return null;
    }
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value) {
        return filterAndWrap(innerIndividualDao.getIndividualsByDataProperty(dataPropertyUri,value),
                     filters);
    }

    public List<Individual> getIndividualsByDataProperty(
            String dataPropertyUri, String value, String datatypeUri,
            String lang) {
        return filterAndWrap(innerIndividualDao.getIndividualsByDataProperty(dataPropertyUri,value,datatypeUri,lang),
                filters);
    }
        
    public List getIndividualsByVClass(VClass vclass) {
        List<Individual> list = innerIndividualDao.getIndividualsByVClass(vclass);
        if( list == null )
            return Collections.EMPTY_LIST;
        else
            return filterAndWrap(list, filters);
    }
    
    public List getIndividualsByVClassURI(String vclassURI) {    
        List<Individual> list = innerIndividualDao.getIndividualsByVClassURI(vclassURI);
        if( list == null ) 
            return Collections.EMPTY_LIST;
        else
            return filterAndWrap(list,filters);
    }
    
    public List getIndividualsByVClassURI(String vclassURI, int offset, int quantity) {    
        List<Individual> list = innerIndividualDao.getIndividualsByVClassURI(vclassURI,offset,quantity);
        if( list == null ) 
            return Collections.EMPTY_LIST;
        else
            return filterAndWrap(list,filters);
    }  
    
    /* All of the methods that return iterator don't wrap the Individual in
     * a IndividualFiltering so they might cause problems */
    
    public Iterator getAllOfThisTypeIterator() {        
        return filterAndWrap(innerIndividualDao.getAllOfThisTypeIterator(), 
                filters);        
    }

    public Iterator getAllOfThisVClassIterator(String classURI) {        
        return filterAndWrap(
                innerIndividualDao.getAllOfThisVClassIterator(classURI), 
                filters);        
    }
    
    public Iterator getUpdatedSinceIterator(long updatedSince) {
        return filterAndWrap(
                innerIndividualDao.getUpdatedSinceIterator(updatedSince),
                filters);        
    }
    
    private class ToFilteredIndividual extends UnaryFunctor<Individual, Individual>{
        private final VitroFilters filters;
        public ToFilteredIndividual(VitroFilters vf){
            this.filters = vf;
        }
        @Override
        public Individual fn(Individual arg) {
            return new IndividualFiltering(arg,filters);
        }        
    }
    
    
    public int getCountOfIndividualsInVClass(String vclassURI) {
        Iterator<Individual> it = innerIndividualDao.getAllOfThisVClassIterator(vclassURI);
        if( it == null ) return 0;
        
        Iterator<Individual> itFiltered = Filter.filter(it,filters.getIndividualFilter());
        return (int)(Summarize.count(itFiltered,filters.getIndividualFilter()));
    }
        

    /* ******************* unfiltered methods ****************** */
    public Collection<DataPropertyStatement> getExternalIds(String individualURI) {
        return innerIndividualDao.getExternalIds(individualURI);
    }

    public Collection<DataPropertyStatement> getExternalIds(String individualURI, String dataPropertyURI) {
        return innerIndividualDao.getExternalIds(individualURI, dataPropertyURI);
    }

    public int deleteIndividual(String individualURI) {
        return innerIndividualDao.deleteIndividual(individualURI);
    }


    public int deleteIndividual(Individual individual) {
        return innerIndividualDao.deleteIndividual(individual);
    }

    public int getCountOfIndividualsInVClass(int vclassId) {
        throw new Error("IndividualDaoFiltering.getCountOfIndividualsInVClass is not supported");
    }
    
    public void addVClass(String individualURI, String vclassURI) {
        innerIndividualDao.addVClass(individualURI, vclassURI);
    }
    
    public void removeVClass(String individualURI, String vclassURI) {
        innerIndividualDao.removeVClass(individualURI, vclassURI);
    }

    public String insertNewIndividual(Individual individual) throws InsertException {
        return innerIndividualDao.insertNewIndividual(individual);
    }

    public int updateIndividual(Individual individual) {
        return innerIndividualDao.updateIndividual(individual);
    }

    public void markModified(Individual individual) {
        innerIndividualDao.markModified(individual);
    }

    public List<String> getKeywordsForIndividual(String individualURI) {
        return innerIndividualDao.getKeywordsForIndividual(individualURI);
    }

    public List<String> getKeywordsForIndividualByMode(String individualURI,
            String modeStr) {
        return innerIndividualDao.getKeywordsForIndividualByMode(individualURI,modeStr);
    }

    public List<Keyword> getKeywordObjectsForIndividual(String individualURI) {
        return innerIndividualDao.getKeywordObjectsForIndividual(individualURI);
    }

    public String getNetId(String entityURI) {
        return innerIndividualDao.getNetId(entityURI);
    }

    public String getStatus(String entityURI) {
        return innerIndividualDao.getStatus(entityURI);
    }

    public List monikers(String vclassURI) {
        return innerIndividualDao.monikers(vclassURI);
    }

    public String toString(){
        return "IndividualDaoFiltering filter: " + filters.getIndividualFilter().toString() + "\n" +
                "InnerDao: " + innerIndividualDao.toString();
    }

    public boolean isIndividualOfClass(String vclassURI, String indURI) {
        return innerIndividualDao.isIndividualOfClass(vclassURI, indURI);
    }

	public String getUnusedURI(Individual individual) throws InsertException {
		return innerIndividualDao.getUnusedURI(individual);
	}

}