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

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.beans.ButtonForm;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class VClassWebappsListingController extends BaseEditController {
    
    private int NUM_COLS = 9;

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        VitroRequest vrequest = new VitroRequest(request);
        Portal portal = vrequest.getPortal();

        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request, response);
        } catch (Throwable t) {
            t.printStackTrace();
        }
       
        //need to figure out how to structure the results object to put the classes underneath
        
        List<VClass> classes = null;
        
        if (request.getParameter("showPropertyRestrictions") != null) {
        	PropertyDao pdao = vrequest.getFullWebappDaoFactory().getObjectPropertyDao();
        	classes = pdao.getClassesWithRestrictionOnProperty(request.getParameter("propertyURI"));
        } else {
        	VClassDao vcdao = vrequest.getFullWebappDaoFactory().getVClassDao();
        	
        	if (request.getParameter("iffRoot") != null) {
                classes = vcdao.getRootClasses();
        	} else {
        		classes = vcdao.getAllVclasses();
        	}
        	
        }

        String ontologyURI = vrequest.getParameter("ontologyUri");
            
        ArrayList<String> results = new ArrayList<String>();
        results.add("XX");
        results.add("Class");
        results.add("short definition");
        results.add("example");
        results.add("comments");
        results.add("group");
        results.add("ontology");
        results.add("display level");
        results.add("update level");        

        if (classes != null) {
            Collections.sort(classes);
            Iterator<VClass> classesIt = classes.iterator();
            while (classesIt.hasNext()) {
                VClass cls = (VClass) classesIt.next();
                if ( (ontologyURI==null) || ( (ontologyURI != null) && (cls.getNamespace()!=null) && (ontologyURI.equals(cls.getNamespace())) ) ) {
	                results.add("XX");
	                if (cls.getName() != null)
	                    try {
	                        //String className = (cls.getName()==null || cls.getName().length()==0) ? cls.getURI() : cls.getName();
	                        results.add("<a href=\"./vclassEdit?uri="+URLEncoder.encode(cls.getURI(),"UTF-8")+"&amp;home="+portal.getPortalId()+"\">"+cls.getLocalNameWithPrefix()+"</a>");
	                    } catch (Exception e) {
	                        results.add(cls.getLocalNameWithPrefix());
	                    }
	                else
	                    results.add("");
	                String shortDef = (cls.getShortDef()==null) ? "" : cls.getShortDef();
	                String example = (cls.getExample()==null) ? "" : cls.getExample();
	                StringBuffer commSb = new StringBuffer();
	                for (Iterator<String> commIt = vrequest.getFullWebappDaoFactory().getCommentsForResource(cls.getURI()).iterator(); commIt.hasNext();) { 
	                	commSb.append(commIt.next()).append(" ");
	                }
	                
	                // get group name
	                WebappDaoFactory wadf = vrequest.getFullWebappDaoFactory();
	                VClassGroupDao groupDao= wadf.getVClassGroupDao();
	                String groupURI = cls.getGroupURI();                
	                String groupName = "";
	                VClassGroup classGroup = null;
	                if(groupURI != null) { 
	                	classGroup = groupDao.getGroupByURI(groupURI);
	                	if (classGroup!=null) {
	                	    groupName = classGroup.getPublicName();
	                	}
	                }
	                
	                // TODO : lastModified
	                
	                // get ontology name
	                OntologyDao ontDao = wadf.getOntologyDao();
	                String ontName = null;
	                try {
	                	Ontology ont = ontDao.getOntologyByURI(cls.getNamespace());
	                	ontName = ont.getName();
	                } catch (Exception e) {}
	                ontName = (ontName == null) ? "" : ontName;
	                
	                results.add(shortDef);
	                results.add(example);
	                results.add(commSb.toString());
	                results.add(groupName);
	                results.add(ontName);
	                results.add(cls.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : cls.getHiddenFromDisplayBelowRoleLevel().getShorthand()); // column 8
	                results.add(cls.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : cls.getProhibitedFromUpdateBelowRoleLevel().getShorthand()); // column 9
               }
            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount",new Integer(NUM_COLS));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Classes");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        // new way of adding more than one button
        List <ButtonForm> buttons = new ArrayList<ButtonForm>();
        HashMap<String,String> newClassParams=new HashMap<String,String>();
        newClassParams.put("home", String.valueOf(portal.getPortalId()));
        String temp;
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	newClassParams.put("ontologyUri",temp);
        }
        ButtonForm newClassButton = new ButtonForm(Controllers.VCLASS_RETRY_URL,"buttonForm","Add new class",newClassParams);
        buttons.add(newClassButton);
        HashMap<String,String> hierClassParams=new HashMap<String,String>();
        hierClassParams.put("home", String.valueOf(portal.getPortalId()));
        if ( (temp=vrequest.getParameter("ontologyUri")) != null) {
        	hierClassParams.put("ontologyUri",temp);
        }
        ButtonForm hierClassButton = new ButtonForm("showClassHierarchy","buttonForm","Class hierarchy",hierClassParams);
        buttons.add(hierClassButton);
        request.setAttribute("topButtons", buttons);

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
   	
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request,response);
    }

}
