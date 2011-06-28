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

package edu.cornell.mannlib.vitro.webapp.search.beans;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;


public class ProhibitedFromSearchTest {
	String SEARCH_CONFIG_URI = "http://example.com/TestSearchConfig";
	String TEST_CLASS = "http://vivoweb.org/ontology/test/bogus#Class5";
	
	String n3 = 
		"@prefix : <http://vitro.mannlib.cornell.edu/ns/vitroDisplay#> . \n" +
		"@prefix vivo: <http://vivoweb.org/ontology/test/bogus#> . \n" +
		"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"+
		"<"+SEARCH_CONFIG_URI+"> rdf:type :ProhibitedFromSearch ;\n" +		
		"           <" + DisplayVocabulary.EXCLUDE_CLASS.getURI() + "> vivo:Class2, vivo:Class3, vivo:Class4, <"+TEST_CLASS+"> .\n" ;
	
	
	
	@Test
	public void testBuildingProhibited(){
		Model r = ModelFactory.createDefaultModel().read(new StringReader(n3), null, "N3");
		OntModel m = (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		m.add( r.listStatements() );
		
		Assert.assertTrue(m.size() > 4);	
		ProhibitedFromSearch pfs = new ProhibitedFromSearch( SEARCH_CONFIG_URI , m);
		Assert.assertNotNull(pfs.prohibitedClasses);
		Assert.assertTrue(pfs.prohibitedClasses.size() == 4);
		Assert.assertTrue(pfs.isClassProhibited(TEST_CLASS));
		Assert.assertTrue(!pfs.isClassProhibited("http://someOtherClass.com/test"));
	}
	
	@Test
	public void testNotFound(){
		Model r = ModelFactory.createDefaultModel().read(new StringReader(n3), null, "N3");
		OntModel m = (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		m.add( r.listStatements() );
		
		Assert.assertTrue(m.size() > 4);	
		ProhibitedFromSearch pfs = new ProhibitedFromSearch( "http://NotFound.com/inModel", m);
		Assert.assertNotNull(pfs.prohibitedClasses);
		Assert.assertTrue(pfs.prohibitedClasses.size() == 0);
		Assert.assertTrue(!pfs.isClassProhibited(TEST_CLASS));
		Assert.assertTrue(!pfs.isClassProhibited("http://someOtherClass.com/test"));
	}
	
	
	@Test
	public void testListener(){
		Model r = ModelFactory.createDefaultModel().read(new StringReader(n3), null, "N3");
		OntModel m = (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		m.add( r.listStatements() );		
		Assert.assertTrue(m.size() > 4);
		
		ProhibitedFromSearch pfs = new ProhibitedFromSearch( SEARCH_CONFIG_URI , m);
		Assert.assertTrue(pfs.prohibitedClasses.size() == 4);
		
		Resource bougsClass3 = ResourceFactory.createResource("http://example.com/bougsClass3");
		Resource searchConfig = ResourceFactory.createResource(SEARCH_CONFIG_URI);		
		m.add(searchConfig, DisplayVocabulary.EXCLUDE_CLASS, bougsClass3);
		Assert.assertEquals(5, pfs.prohibitedClasses.size());
		
		m.remove(searchConfig, DisplayVocabulary.EXCLUDE_CLASS, bougsClass3);
		Assert.assertEquals(4, pfs.prohibitedClasses.size());
		
		Resource bougsClass4 = ResourceFactory.createResource("http://vivoweb.org/ontology/test/bogus#Class4");
		m.remove(searchConfig, DisplayVocabulary.EXCLUDE_CLASS, bougsClass4);
		Assert.assertEquals(3, pfs.prohibitedClasses.size());			
	}
	
	
	@Test
	public void testListenerAbnormal(){
		Model r = ModelFactory.createDefaultModel().read(new StringReader(n3), null, "N3");
		OntModel m = (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		m.add( r.listStatements() );		
		Assert.assertTrue(m.size() > 4);
		
		ProhibitedFromSearch pfs = new ProhibitedFromSearch( SEARCH_CONFIG_URI , m);
		Assert.assertTrue(pfs.prohibitedClasses.size() == 4);
		int originalSize = pfs.prohibitedClasses.size();
		
		Literal bogusLiteral = ResourceFactory.createPlainLiteral("some bogus literal");
		Resource searchConfig = ResourceFactory.createResource(SEARCH_CONFIG_URI);		
		m.add(searchConfig, DisplayVocabulary.EXCLUDE_CLASS, bogusLiteral);
		Assert.assertEquals(originalSize, pfs.prohibitedClasses.size());		
		m.remove(searchConfig, DisplayVocabulary.EXCLUDE_CLASS, bogusLiteral);
		Assert.assertEquals(originalSize, pfs.prohibitedClasses.size());
		
		Resource anonRes = ResourceFactory.createResource();
		m.remove(searchConfig, DisplayVocabulary.EXCLUDE_CLASS, anonRes);
		Assert.assertEquals(originalSize, pfs.prohibitedClasses.size());			
	}
	
	@Test
	public void testPrimaryIndex(){		
		String primaryIndexN3 = 
			"<http://vitro.mannlib.cornell.edu/ontologies/display/1.1#PrimaryLuceneIndex>" +
			"<http://vitro.mannlib.cornell.edu/ontologies/display/1.1#excludeClass>" +
			"<http://vivoweb.org/ontology/core#NonAcademic> . ";		
		
		Model r = ModelFactory.createDefaultModel().read(new StringReader(primaryIndexN3), null, "N3");
		OntModel m = (OntModel) ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		m.add( r.listStatements() );
		
		Assert.assertTrue(m.size() == 1);	
		ProhibitedFromSearch pfs = new ProhibitedFromSearch( DisplayVocabulary.PRIMARY_LUCENE_INDEX_URI, m);
		Assert.assertNotNull(pfs.prohibitedClasses);
		Assert.assertEquals(1, pfs.prohibitedClasses.size() );
		Assert.assertTrue(pfs.isClassProhibited("http://vivoweb.org/ontology/core#NonAcademic"));
		Assert.assertTrue(!pfs.isClassProhibited("http://someOtherClass.com/test"));
	}
}
