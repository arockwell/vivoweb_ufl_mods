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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.channels.FileChannel;

import javax.servlet.ServletContext;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;

import edu.cornell.mannlib.vitro.webapp.dao.DisplayModelDao;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

public class DisplayModelDaoJena implements DisplayModelDao {
    WebappDaoFactoryJena wdf;
    
    protected static final String MENU_N3_FILE = "/WEB-INF/ontologies/app/menu.n3";
    protected static final String MENU_N3_FILE_BACKUP = "/WEB-INF/ontologies/app/menu.backup";
    
    protected static Resource MENU_TEXT_RES = 
        ResourceFactory.createResource(DisplayVocabulary.MENU_TEXT_RES);    
    protected static Property HAS_TEXT_REPRESENTATION =
        ResourceFactory.createProperty(DisplayVocabulary.HAS_TEXT_REPRESENTATION);
    
    public DisplayModelDaoJena(WebappDaoFactoryJena wdfj){
        this.wdf = wdfj;
    }    
    
    public void replaceDisplayModel(String n3, ServletContext context) throws Exception{
        OntModel displayModel = wdf.getOntModelSelector().getDisplayModel();
                
        //get old menu file and turn into model
        Model oldMenuStmts = ModelFactory.createDefaultModel();
//        InputStream oldIn = FileManager.get().open( context.getRealPath(MENU_N3_FILE ) );
//        if( oldIn == null ){
//            throw new Exception("Cannot open existing menu file.");
//        }
        try{
            oldMenuStmts.read(new StringReader(getDisplayModel(context)), null, "N3");
        }catch(Throwable th){
            throw new Exception("Cannot read in existing menu. " + th.getMessage());
        }
        
        //turn the N3 text for the new menu into a model        
        Model newMenuStmts = ModelFactory.createDefaultModel();
        StringReader newIn = new StringReader( n3 );
        try{
            newMenuStmts.read(newIn, null,"N3");
        }catch(Throwable th){
            throw new Exception("There was an error in the menu N3: "+ th.getMessage());
        }
        
        displayModel.enterCriticalSection(true);
        try{
            //copy old menu file to backup
//            File oldMenuFile = new File(context.getRealPath(MENU_N3_FILE));
//            File oldMenuFileBackup = new File(context.getRealPath(MENU_N3_FILE_BACKUP));
//            copyFile(oldMenuFile , oldMenuFileBackup);
            
            //save new menu file to old menu file
            displayModel.removeAll(MENU_TEXT_RES, HAS_TEXT_REPRESENTATION, null);
            displayModel.add(MENU_TEXT_RES, HAS_TEXT_REPRESENTATION, n3);
            
//            File newMenuFile = new File(context.getRealPath(MENU_N3_FILE));
//            FileWriter mfWriter = new FileWriter(newMenuFile);
//            mfWriter.write(n3);
//            mfWriter.close();
            
            //remove old menu statements from display model
            displayModel.remove(oldMenuStmts);
            
            //add new menu statements to display model
            displayModel.add(newMenuStmts);
        }finally{
            displayModel.leaveCriticalSection();
        }        
    }
    
    
    public String getDisplayModel(ServletContext context) throws IOException{
        OntModel displayModel = wdf.getOntModelSelector().getDisplayModel();
        String text = null;
        displayModel.enterCriticalSection(false);
        try{
            Statement stmt = displayModel.getProperty(MENU_TEXT_RES,HAS_TEXT_REPRESENTATION);
            if( stmt != null && stmt.getLiteral() != null)
                text = stmt.getLiteral().getLexicalForm();
        }finally{
            displayModel.leaveCriticalSection();
        }
        if( text == null ){       
            //text of file is not yet in model
            File oldMenuFile = new File(context.getRealPath(MENU_N3_FILE));
            StringBuffer str = new StringBuffer(2000);
            BufferedReader reader = new BufferedReader(new FileReader(oldMenuFile));
            char[] buf = new char[1024];
            int numRead=0;
            while((numRead=reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                str.append(readData);
            }
            reader.close();
            
            //Now write the file contents into the display model so that on
            //future edits, the user can be presented with their last input.        
            String menuN3Content = str.toString();
            displayModel.enterCriticalSection(true);
            try{
                displayModel.add(MENU_TEXT_RES, HAS_TEXT_REPRESENTATION, menuN3Content);
            }finally{
                displayModel.leaveCriticalSection();
            }
            return menuN3Content;
        }else{            
            return text;
        }        
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
         destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
         source = new FileInputStream(sourceFile).getChannel();
         destination = new FileOutputStream(destFile).getChannel();
         destination.transferFrom(source, 0, source.size());
        }
        finally {
         if(source != null) {
          source.close();
         }
         if(destination != null) {
          destination.close();
         }
       }
    }


}
