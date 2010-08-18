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

package edu.cornell.mannlib.vitro.webapp.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.search.beans.ObjectSourceIface;

public interface IndividualDao extends ObjectSourceIface {

	/**
	 * Returns a collection of DataPropertyStatements involving all the external ID literals for a given Individual.
	 */
    public abstract Collection<DataPropertyStatement> getExternalIds(String individualURI);

    public abstract Collection<DataPropertyStatement> getExternalIds(String individualURI, String dataPropertyURI);

    /**
     * Adds the specified Individual to the specified VClass (i.e. adds rdf:type).
     * @param individualURI
     * @param vclassURI
     */
    public abstract void addVClass(String individualURI, String vclassURI);
    
    /**
     * Removes the specified Individual from the specificed VClass (i.e. retracts rdf:type)
     * @param individualURI
     * @param vclassURI
     */
    public abstract void removeVClass(String individualURI, String vclassURI);
    
    /**
     * Returns a list of all the Individuals in the specified VClass.
     * @param vclass
     * @return
     */
    public abstract List <Individual> getIndividualsByVClass(VClass vclass);

    /**
     * Returns a list of Individuals in a given VClass.
     */
    public abstract List <Individual> getIndividualsByVClassURI(String vclassURI);
    
    /**
     * Returns a list of Individuals in a given VClass.
     */
    public abstract List <Individual> getIndividualsByVClassURI(String vclassURI, int offset,
            int quantity);

    /**
     * @returns new individual URI  if success.
     */
    public abstract String insertNewIndividual(Individual individual) throws InsertException;

    /**
     * updates a single individual in the knowledge base.
     * @return 0 on failed
     */
    public abstract int updateIndividual(Individual individual);

    /**
     * deletes a single individual from the knowledge base.
     * @param id
     * @return 0 on failed
     */
    public abstract int deleteIndividual(String individualURI);

    public abstract int deleteIndividual(Individual individual);

    public abstract void markModified(Individual individual);

    /**
     * Get a row from the entities table and make an Entity.
     * PropertiesList will not be filled out.
     * VClass will be filled out.
     * @param entityId
     * @return an Entity object or null if not found.
     */
    public abstract Individual getIndividualByURI(String individualURI);

    /**
     * Returns an Iterator over all Individuals in the model that are user-viewable.
     */
    public abstract Iterator<Individual> getAllOfThisTypeIterator();

    /**
     * Returns an Iterator over all Individuals in the model that are user-viewable and of the specified VClass URI.
     * @param vClassURI
     * @return
     */
    public abstract Iterator<Individual> getAllOfThisVClassIterator(String vClassURI);

    /**
     * Returns an Iterator over all Individuals in the model that are user-viewable and have been updated since the specified time.
     */
    public abstract Iterator<Individual> getUpdatedSinceIterator(long updatedSince);

        int getCountOfIndividualsInVClass(String vclassURI );

    public boolean isIndividualOfClass(String vclassURI, String indURI);
    
    /**
     * Returns a list of individuals with the given value for the given dataProperty.  If
     * there are no Indiviuals that fit the criteria then an empty list is returned.
     * 
     * @param dataPropertyUri
     * @param value
     * @return
     */
    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value);

    public List<Individual> getIndividualsByDataProperty(String dataPropertyUri, String value, String datatypeUri, String lang);
    
	void fillVClassForIndividual(Individual individual);

	List<String> monikers(String vclassURI);

	@SuppressWarnings("unchecked")
	List<String> getKeywordsForIndividual(String individualURI);

	@SuppressWarnings("unchecked")
	List<String> getKeywordsForIndividualByMode(String individualURI,
			String modeStr);

	@SuppressWarnings("unchecked")
	List<Keyword> getKeywordObjectsForIndividual(String individualURI);

	String getIndividualURIFromNetId(String netIdStr);

	String getNetId(String entityURI);

	String getStatus(String entityURI);

	/**
	 * Standard way to get a new URI that is not yet used.
	 * @param individual, may be null
	 * @return new URI that is not found in the subject, predicate or object position of any statement.
	 * @throws InsertException Could not create a URI
	 */
	String getUnusedURI(Individual individual) throws InsertException;
	
    @Deprecated
    public abstract Individual getIndividualByExternalId(int externalIdType,
                                                         String externalIdValue);

    @Deprecated
    Individual getIndividualByExternalId(int externalIdType,
                                         String externalIdValue,
                                         String vClassURI);
}