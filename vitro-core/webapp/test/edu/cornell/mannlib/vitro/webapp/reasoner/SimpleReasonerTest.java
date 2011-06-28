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

package edu.cornell.mannlib.vitro.webapp.reasoner;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.reasoner.support.SimpleReasonerTBoxListener;


public class SimpleReasonerTest extends AbstractTestClass {
	
	@Before
	public void suppressErrorOutput() {
		suppressSyserr();
	}

	@Test
	public void addType(){
	
		// Test that when a new instance is asserted, its asserted type is not added to the
		// inference graph
		
		// Create a Tbox with a simple class hierarchy. B is a subclass of A.
		// Pellet will compute TBox inferences
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addSubClass(classB);
	            
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // Individual x 
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		
        // add a statement to the ABox that individual x is of type (i.e. is an instance of) B.		
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		aBox.add(xisb);		

		// Verify that "x is of type B" was not inferred	
		Assert.assertFalse(inf.contains(xisb));	
	}
	
	@Test
	public void addTypes(){
	
		// Create a Tbox with a simple class hierarchy. D and E are subclasses of C. B and C are subclasses of A.
		// Pellet will compute TBox inferences
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classE.setLabel("class E", "en-US");
	    
	    classC.addSubClass(classD);
	    classC.addSubClass(classE);
	    
        classA.addSubClass(classB);
        classA.addSubClass(classC);
        
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // add a statement to the ABox that individual x is of type E.
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classE);		

		// Verify that "x is of type C" was inferred
		Statement xisc = ResourceFactory.createStatement(ind_x, RDF.type, classC);	
		Assert.assertTrue(inf.contains(xisc));	
		
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
	}

	
	@Test
	public void removeTypes(){
	
		// Create a Tbox with a simple class hierarchy. C is a subclass of B and B is a subclass of A.
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");
	    
	    classB.addSubClass(classC);
	    classA.addSubClass(classB);
        
        // this is the model to receive inferences
        Model inf = ModelFactory.createDefaultModel();
        
		// create an Abox and register the SimpleReasoner listener with it
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		aBox.register(new SimpleReasoner(tBox, aBox, inf));
		
        // add a statement to the ABox that individual x is of type C.
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		

        // add a statement to the ABox that individual x is of type B.
		aBox.add(ind_x, RDF.type, classB);		

		// remove the statement that individual x is of type C
		aBox.remove(ind_x, RDF.type, classC);
		
		// Verify that the inference graph contains the statement that x is of type A.
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
	}
	

	// This tests added TBox subClassOf and equivalentClass statements.
	// The ABox data that will be the basis for the inference will
	// be in the ABox graph.
	@Test
	public void addSubClass1(){
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		tBox.register(new SimpleReasonerTBoxListener(simpleReasoner));

		// Add classes classes A, B, C and D to the TBox
	
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    	   
        // Add a statement that individual x is of type C to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		
	    
        // Add a statement that C is a subclass of A to the TBox	
	    
	    classA.addSubClass(classC);
		
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));

		// Verify that "x is of type B" was not inferred
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertFalse(inf.contains(xisb));	

		// Verify that "x is of type D" was not inferred
		Statement xisd = ResourceFactory.createStatement(ind_x, RDF.type, classD);	
		Assert.assertFalse(inf.contains(xisd));	
		
	}

	
	// this tests added TBox subClassOf and equivalentClass statements.
	// The ABox data that is the basis for the inference will be
	// in the inferred graph
	@Test
	public void addSubClass2(){
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		tBox.register(new SimpleReasonerTBoxListener(simpleReasoner));

		// Add classes classes A, B, C and D to the TBox
	    // D is a subclass of C
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    	   
	    classC.addSubClass(classD);
	    
        // Add a statement that individual x is of type D to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classD);		
	    
        // Add a statement that C is a subclass of A to the TBox	
	    classA.addSubClass(classC);
		
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));		
	}
	
	@Test
	// this tests incremental reasoning as a result of the removal of a subClassOf 
	// or equivalentClass statement from the TBox.
	public void removeSubClass(){
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		tBox.register(new SimpleReasonerTBoxListener(simpleReasoner));

		// Add classes A, B, C, D, E, F, G and H to the TBox.
		// B, C and D are subclasses of A.
		// E is a subclass of B.
		// F and G are subclasses of C.
		// H is a subclass of D.
	
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    OntClass classD = tBox.createClass("http://test.vivo/D");
	    classD.setLabel("class D", "en-US");
	    
	    OntClass classE = tBox.createClass("http://test.vivo/E");
	    classE.setLabel("class E", "en-US");
	    
	    OntClass classF = tBox.createClass("http://test.vivo/F");
	    classF.setLabel("class F", "en-US");

	    OntClass classG = tBox.createClass("http://test.vivo/G");
	    classG.setLabel("class G", "en-US");

	    OntClass classH = tBox.createClass("http://test.vivo/H");
	    classH.setLabel("class H", "en-US");

	    classA.addSubClass(classB);
	    classA.addSubClass(classC);
	    classA.addSubClass(classD);
	    classB.addSubClass(classE);
	    classC.addSubClass(classF);
	    classC.addSubClass(classG);
	    classD.addSubClass(classH);
	    
        // Add a statement that individual x is of type E to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classE);		
	    
		// Remove the statement that B is a subclass of A from the TBox
		classA.removeSubClass(classB);
		
		// Verify that "x is of type A" is not in the inference graph
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertFalse(inf.contains(xisa));

		
		// Verify that "x is of type B" is in the inference graph
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertTrue(inf.contains(xisb));	

        // Add statements that individual y is of types F and H to the ABox
		Resource ind_y = aBox.createResource("http://test.vivo/y");
		aBox.add(ind_y, RDF.type, classF);	
		aBox.add(ind_y, RDF.type, classH);
		
		// Remove the statement that C is a subclass of A from the TBox
		classA.removeSubClass(classC);

		// Verify that "y is of type A" is in the inference graph
		Statement yisa = ResourceFactory.createStatement(ind_y, RDF.type, classA);	
		Assert.assertTrue(inf.contains(yisa));
				
	}
	
	// Test inference based on class equivalence
	// 
	@Test
	public void equivClass1(){
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		tBox.register(new SimpleReasonerTBoxListener(simpleReasoner));

		// Add classes classes A, B and C to the TBox
	    // A is equivalent to B
		// C is a subclass of A
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

		OntClass classC = tBox.createClass("http://test.vivo/C");
	    classC.setLabel("class C", "en-US");

	    classA.addEquivalentClass(classB);
	    classA.addSubClass(classC);
	    
        // Add a statement that individual x is of type C to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classC);		
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));		
		
		// Verify that "x is of type B" was inferred
		Statement xisb = ResourceFactory.createStatement(ind_x, RDF.type, classB);	
		Assert.assertTrue(inf.contains(xisb));		

	}
	
	// Test inference based on class equivalence
	// 
	@Test
	public void equivClass2(){
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		tBox.register(new SimpleReasonerTBoxListener(simpleReasoner));

		// Add classes classes A and B to the TBox
	    // A is equivalent to B
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addEquivalentClass(classB);
	    
        // Add a statement that individual x is of type B to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classB);		
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));		
	}
	
	
	// Test inference based on class equivalence
	// 
	@Test
	public void equivClass3(){
				
		// Create TBox, ABox and Inference models and register
		// the ABox reasoner listeners with the ABox and TBox
		// Pellet will compute TBox inferences
		
		OntModel tBox = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC); 
		OntModel aBox = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
        Model inf = ModelFactory.createDefaultModel();
		
        SimpleReasoner simpleReasoner = new SimpleReasoner(tBox, aBox, inf);
		aBox.register(simpleReasoner);
		tBox.register(new SimpleReasonerTBoxListener(simpleReasoner));

		// Add classes classes A and B to the TBox
	    // A is equivalent to B
		
		OntClass classA = tBox.createClass("http://test.vivo/A");
	    classA.setLabel("class A", "en-US");

		OntClass classB = tBox.createClass("http://test.vivo/B");
	    classB.setLabel("class B", "en-US");

	    classA.addEquivalentClass(classB);
	    
        // Add a statement that individual x is of type B to the ABox
		Resource ind_x = aBox.createResource("http://test.vivo/x");
		aBox.add(ind_x, RDF.type, classB);		
	    
		// Verify that "x is of type A" was inferred
		Statement xisa = ResourceFactory.createStatement(ind_x, RDF.type, classA);	
		Assert.assertTrue(inf.contains(xisa));	
		
		// Remove the statement that x is of type B from the ABox
		aBox.remove(ind_x, RDF.type, classB);
		
		// Verify that "x is of type A" was removed from the inference graph
		Assert.assertFalse(inf.contains(xisa));	
				
	}
	
	
	// To help in debugging the unit test
	void printModels(OntModel ontModel) {
	    
		System.out.println("\nThe model has " + ontModel.size() + " statements:");
		System.out.println("---------------------------------------------------");
		ontModel.writeAll(System.out,"N3",null);
		
	}	
}