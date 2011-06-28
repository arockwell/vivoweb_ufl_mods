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

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.util.Collections;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class PrimitiveRdfEditTest {
    
    OntModel testModel;
    WebappDaoFactory wdf;

	private String testN3a = 
		"<http://example.com/motorcycles/honda/vtl1000> <http://example.com/engines/displacement> \"1000cm3\" ." +
		"<http://example.com/motorcycles/honda/919> <http://example.com/engines/displacement> \"919cm3\" ." ;
	
	private String testN3b = 
		"<http://example.com/motorcycles/honda/919> <http://example.com/motorcycles/relatedTo> <http://exmaple.com/motorcycle/honda/599> ." ;
	
	
	@Before
	public void setUp() throws Exception { }

	@Test
	public void testProcessChanges() throws Exception {
		OntModel writeModel = ModelFactory.createOntologyModel();
        
		int totalStmts = 3;
		
		PrimitiveRdfEdit pre = new PrimitiveRdfEdit();
		String params[] = { testN3a, testN3b };
		Set<Model> models = pre.parseRdfParam(params, "N3");
		Assert.assertNotNull(models);
		Assert.assertTrue( models.size() == 2);
				
		Assert.assertNotNull( writeModel );
		long size = writeModel.size();
		pre.processChanges( models, Collections.EMPTY_SET, writeModel, writeModel, "uri:fakeEditorUri");		
		Assert.assertEquals(size+totalStmts, writeModel.size());
				
		String params3[] = { testN3b };
		Set<Model> retracts = pre.parseRdfParam( params3, "N3");		
		pre.processChanges(Collections.EMPTY_SET, retracts, writeModel, writeModel, "uri:fakeEditorUri");
		Assert.assertEquals(size+totalStmts-1, writeModel.size());		
	}


	@Test
	public void testParseRdfParam() throws Exception {
		PrimitiveRdfEdit pre = new PrimitiveRdfEdit();
		String params[] = { testN3a, testN3b };
		Set<Model> models = pre.parseRdfParam(params, "N3");
		Assert.assertNotNull(models);
		Assert.assertTrue( models.size() == 2);
				
	}

}
