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

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.io.InputStream;
import java.lang.reflect.Field;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MainMenu;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.menu.MenuItem;


public class MenuDaoJenaTest extends AbstractTestClass {
    
    OntModel displayModel;
    
    @Before
    public void setUp() throws Exception {
        // Suppress error logging.
        setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);

        Model model = ModelFactory.createDefaultModel();        
        InputStream in = MenuDaoJenaTest.class.getResourceAsStream("resources/menuForTest.n3");
        model.read(in,"","N3");        
        displayModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM,model);
    }

    @Test
    public void getMenuItemTest(){
        SimpleOntModelSelector sos = new SimpleOntModelSelector( ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM));
        sos.setDisplayModel(displayModel);
        MenuDaoJena menuDaoJena = new MenuDaoJena(new WebappDaoFactoryJena(sos));
        
        MainMenu menu = menuDaoJena.getMainMenu( "notImportant" );       
        
        try{
            Class clz = UrlBuilder.class;
            Field f = clz.getDeclaredField( "contextPath" );
            f.setAccessible(true);
            f.set(null, "bogusUrlContextPath"); 
        }catch(Exception e){
            Assert.fail(e.toString());
        }
        
        Assert.assertNotNull(menu);
        Assert.assertNotNull( menu.getItems() );
        Assert.assertEquals(5, menu.getItems().size());
        
        //The nulls in getUrl() are from the UrlBuilder not being setup correctly.
        //it should be fine.
        
        MenuItem item = menu.getItems().get(0);
        Assert.assertNotNull(item);
        Assert.assertEquals("Home",item.getLinkText());
        Assert.assertEquals("bogusUrlContextPath/home",item.getUrl());
        
        item = menu.getItems().get(1);
        Assert.assertNotNull(item);
        Assert.assertEquals("People",item.getLinkText());
        Assert.assertEquals("bogusUrlContextPath/people",item.getUrl());
        
        item = menu.getItems().get(2);
        Assert.assertNotNull(item);
        Assert.assertEquals("Publications",item.getLinkText());
        Assert.assertEquals("bogusUrlContextPath/publications",item.getUrl());
        
        item = menu.getItems().get(3);
        Assert.assertNotNull(item);
        Assert.assertEquals("Events",item.getLinkText());
        Assert.assertEquals("bogusUrlContextPath/events",item.getUrl());
        
        item = menu.getItems().get(4);
        Assert.assertNotNull(item);
        Assert.assertEquals("Organizations",item.getLinkText());
        Assert.assertEquals("bogusUrlContextPath/organizations",item.getUrl());
    }
    
    
    @Test
    public void isActiveTest(){
        SimpleOntModelSelector sos = new SimpleOntModelSelector( ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM));
        sos.setDisplayModel(displayModel);
        MenuDaoJena menuDaoJena = new MenuDaoJena(new WebappDaoFactoryJena(sos));                 
        
        //First arg is the page the user is on.  Second arg is the urlmapping of the menu item.
        Assert.assertTrue( menuDaoJena.isActive("/", "/") );
        Assert.assertTrue( menuDaoJena.isActive("/people", "/people") );
        
        Assert.assertFalse( menuDaoJena.isActive("/people", "/") );
        Assert.assertFalse( menuDaoJena.isActive("/", "/people") );
        
    }
}
