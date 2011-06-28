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

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import junit.framework.Assert;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;


public class EditN3UtilsTest {

    @Test
    public void testStripInvalidXMLChars() {
        Model m = ModelFactory.createDefaultModel();
        String containsInvalidXMLChars = "Blah \u0001blah \u0002blah\uDDDD";
        String clean = "Blah blah blah"; 
        
        // add a statement with the literal incompatible with XML to model m
        m.add(m.createResource(), RDFS.label, containsInvalidXMLChars);
        
        Assert.assertFalse(isSerializableAsXML(m));
        
        String stripped = EditN3Utils.stripInvalidXMLChars(
                                containsInvalidXMLChars);
        Assert.assertEquals(clean, stripped);
        
        // clear the model of any statements
        m.removeAll();
        // add a statement with a literal that has been stripped of bad chars
        m.add(m.createResource(), RDFS.label, stripped);
        
        Assert.assertTrue(isSerializableAsXML(m));      
    }
    
    private boolean isSerializableAsXML(Model m) {
        try {
            NullOutputStream nullStream = new NullOutputStream();
            m.write(nullStream, "RDF/XML");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
}
