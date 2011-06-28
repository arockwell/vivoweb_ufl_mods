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

package edu.cornell.mannlib.vitro.webapp.controller;

import org.junit.Assert;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;


public class EntityControllerTest {

	@Test
	public void testAcceptHeader(){
		EntityController entityController = new EntityController();
		
		/* Check to see if vitro would send RDF/XML to tabulator */
		String tabulatorsAcceptHeader =
			"text/xml,application/xml,application/xhtml+xml,text/html;q=0.5,text/plain;q=0.5," +
			"image/png,*/*;q=0.1," +
			"application/rdf+xml;q=1.0,text/n3;q=0.4";
		ContentType result = entityController.checkForLinkedDataRequest("http://notUsedInThisTestCase.com/bogus",tabulatorsAcceptHeader); 
		Assert.assertTrue( result != null );	
		Assert.assertTrue( "application/rdf+xml".equals( result.toString()) );
	}
}
