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

import junit.framework.Assert;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Link;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.Tab;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PortalDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.TabDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Test that the Jena DAOs write different types of data to the appropriate models.
 * @author bjl23
 *
 */
public class OntModelSegementationTest {

	private WebappDaoFactoryJena wadf;
	
	@org.junit.Before
	public void setUpWebappDaoFactoryJena() {
		wadf = new WebappDaoFactoryJena(new SimpleOntModelSelector());
	}
	
	@org.junit.Test
	public void testUserAccountModel() {
		
		UserDao udao = wadf.getUserDao();
		OntModelSelector oms = wadf.getOntModelSelector();
		
		User user = new User();
		user.setFirstName("Chuck");
		user.setLastName("Roast");
		user.setUsername("chuckroast");
		
		String userURI = udao.insertUser(user);
		user.setURI(userURI);
		Assert.assertTrue(oms.getUserAccountsModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		
		user.setUsername("todd");
		udao.updateUser(user);
		Assert.assertTrue(oms.getUserAccountsModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		
		udao.deleteUser(user);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		
	}
	
	@org.junit.Test
	public void testApplicationMetadataModel() throws InsertException {
		
		PortalDao pdao = wadf.getPortalDao();
		TabDao tdao = wadf.getTabDao();
		VClassGroupDao vcgdao = wadf.getVClassGroupDao();
		PropertyGroupDao pgdao = wadf.getPropertyGroupDao();
		OntModelSelector oms = wadf.getOntModelSelector();
	
		this.assertAllModelsEmpty(oms);
		
		//insert a portal
		Portal portal = new Portal();
		portal.setPortalId(1);
		portal.setAppName("test portal");
		pdao.insertPortal(portal);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		//insert a tab
		Tab tab = new Tab();
		tab.setTitle("test tab");
		int tabId = tdao.insertTab(tab);
		tab.setTabId(tabId);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		//insert a classgroup
		VClassGroup group = new VClassGroup();
		group.setURI("http://example.org/classgroup");
		group.setPublicName("test group");
		vcgdao.insertNewVClassGroup(group);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		//insert a property group
		PropertyGroup pgroup = new PropertyGroup();
		pgroup.setURI("http://example.org/propertygroup");
		pgroup.setName("test property group");
		pgdao.insertNewPropertyGroup(pgroup);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		portal.setAppName("updated portal");
		tab.setTitle("updated tab");
		group.setPublicName("updated group");
		pgroup.setName("updated property group");
		
		pdao.updatePortal(portal);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		tdao.updateTab(tab);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		vcgdao.updateVClassGroup(group);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		pgdao.updatePropertyGroup(pgroup);
		this.assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		pdao.deletePortal(portal);
		tdao.deleteTab(tab);
		vcgdao.deleteVClassGroup(group);
		pgdao.deletePropertyGroup(pgroup);
		
		this.assertAllModelsEmpty(oms);
		
	}
	
	@org.junit.Test
	public void testTBoxModel() throws InsertException {
		
		OntModelSelector oms = wadf.getOntModelSelector();
		VClassDao vcDao = wadf.getVClassDao();
		ObjectPropertyDao opDao = wadf.getObjectPropertyDao();
		DataPropertyDao dpDao = wadf.getDataPropertyDao();
		OntologyDao oDao = wadf.getOntologyDao();
		
		VClass vclass = new VClass();
		vclass.setURI("http://example.org/vclass");
		vcDao.insertNewVClass(vclass);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		ObjectProperty op = new ObjectProperty();
		op.setURI("http://example.org/objectProperty");
		opDao.insertObjectProperty(op);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		DataProperty dp = new DataProperty();
		dp.setURI("http://example.org/dataProperty");
		dpDao.insertDataProperty(dp);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		Ontology o = new Ontology();
		o.setURI("http://example.org/");
		oDao.insertNewOntology(o);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		vclass.setName("vclass");
		op.setDomainPublic("objectProperty");
		dp.setPublicName("dataProperty");
		o.setName("ontology");
		
		vcDao.updateVClass(vclass);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		opDao.updateObjectProperty(op);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		dpDao.updateDataProperty(dp);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		oDao.updateOntology(o);
		this.assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		vcDao.deleteVClass(vclass);
		opDao.deleteObjectProperty(op);
		dpDao.deleteDataProperty(dp);
		oDao.deleteOntology(o);
		
		this.assertAllModelsEmpty(oms);
			
	}
	
	@org.junit.Test
	public void testAboxModel() throws InsertException {
		
		OntModelSelector oms = wadf.getOntModelSelector();
		IndividualDao iDao = wadf.getIndividualDao();
		
		Individual ind = new IndividualImpl("http://example.org/individual");
		iDao.insertNewIndividual(ind);
		this.assertABoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		ind.setName("ind");
		iDao.updateIndividual(ind);
		this.assertABoxModelNonemptyAndAllOtherModelsAreEmpty(oms);
		
		iDao.deleteIndividual(ind);
		this.assertAllModelsEmpty(oms);
		
	}

	private void assertAllModelsEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		Assert.assertTrue(oms.getFullModel().size() == 0);
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	private void assertMetadataModelNonemptyAndAllOtherModelsAreEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getApplicationMetadataModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == oms.getApplicationMetadataModel().size());
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	private void assertTBoxModelNonemptyAndAllOtherModelsAreEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getTBoxModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == oms.getTBoxModel().size());
		Assert.assertTrue(oms.getABoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	private void assertABoxModelNonemptyAndAllOtherModelsAreEmpty(OntModelSelector oms) {
		Assert.assertTrue(oms.getABoxModel().size() > 0);
		Assert.assertTrue(oms.getFullModel().size() == oms.getABoxModel().size());
		Assert.assertTrue(oms.getTBoxModel().size() == 0);
		Assert.assertTrue(oms.getApplicationMetadataModel().size() == 0);
		Assert.assertTrue(oms.getUserAccountsModel().size() == 0);
	}
	
	@org.junit.Test
	public void testConcurrency() throws InsertException {
//		(new Thread(new ClassLister(wadf))).start();
//		(new Thread(new ClassLister(wadf))).start();
//		VClass v = null;
//		for (int i = 0; i < 50; i++) {
//			v = new VClass();
//			v.setURI("http://example.org/vclass" + i);
//			wadf.getVClassDao().insertNewVClass(v);
//		}
//		for (int i = 0; i < 500; i++) {
//			v.setName("blah " + i);
//			wadf.getVClassDao().updateVClass(v);
//		}
		
	}
	
	private class ClassLister implements Runnable {
		
		private WebappDaoFactory wadf;
		
		public ClassLister(WebappDaoFactory wadf) {
			this.wadf = wadf;
		}
		
		public void run() {
			
			//int vclassTotal = wadf.getVClassDao().getAllVclasses().size();
			
			for (int i = 0; i < 1500; i++) {
				
				wadf.getVClassDao().getAllVclasses().size();
				
			//	if (vclassTotal != wadf.getVClassDao().getAllVclasses().size()) {
			//		throw new RuntimeException("Inconsistent VClass list size");
			//	}
			}
			
		}
		
	}
	
}
