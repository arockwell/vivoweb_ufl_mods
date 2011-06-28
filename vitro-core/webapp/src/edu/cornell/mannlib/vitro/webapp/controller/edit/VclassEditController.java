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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroModelProperties;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class VclassEditController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(VclassEditController.class.getName());
	private static final int NUM_COLS = 12;

    public void doPost (HttpServletRequest req, HttpServletResponse response) {
    	
    	VitroRequest request = new VitroRequest(req);

        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("VclassEditController caught exception calling doGet()");
        }

        EditProcessObject epo = super.createEpo(request, FORCE_NEW);
        request.setAttribute("epoKey", epo.getKey());

        VClassDao vcwDao = request.getFullWebappDaoFactory().getVClassDao();
        VClass vcl = (VClass)vcwDao.getVClassByURI(request.getParameter("uri"));
        
        if (vcl == null) {
        	if (VitroModelProperties.isRDFS(request.getFullWebappDaoFactory().getLanguageProfile()) 
        			&& ( (RDF.getURI()+"Resource").equals(request.getParameter("uri")))) {
        		vcl = new VClass(RDF.getURI()+"Resource");
        	}
        }

        request.setAttribute("VClass",vcl);
        
        ArrayList results = new ArrayList();
        results.add("Class");                // 1
        results.add("short definition");     // 2
        results.add("example");              // 3
        results.add("description");          // 4
        results.add("editor comments");      // 5
        results.add("group");                // 6
        results.add("display level");        // 7
        results.add("update level");         // 8
        results.add("custom entry form");    // 9
        results.add("custom display view");  // 10
        results.add("custom search view");   // 11
        results.add("URI");                  // 12
        
        String name = vcl.getLocalNameWithPrefix();
        String shortDef = (vcl.getShortDef()==null) ? "" : vcl.getShortDef();
        String example = (vcl.getExample()==null) ? "" : vcl.getExample();
        String description = (vcl.getDescription()==null) ? "" : vcl.getDescription();
        
        WebappDaoFactory wadf = request.getFullWebappDaoFactory();

        String groupURI = vcl.getGroupURI();
        String groupName = "none";
        if(groupURI != null) { 
            VClassGroupDao groupDao= wadf.getVClassGroupDao();
            VClassGroup classGroup = groupDao.getGroupByURI(groupURI);
            if (classGroup != null) {
                groupName = classGroup.getPublicName();
            }
        }

        boolean foundComment = false;
        StringBuffer commSb = null;
        for (Iterator<String> commIt = request.getFullWebappDaoFactory().getCommentsForResource(vcl.getURI()).iterator(); commIt.hasNext();) { 
            if (commSb==null) {
                commSb = new StringBuffer();
                foundComment=true;
            }
            commSb.append(commIt.next()).append(" ");
        }
        if (!foundComment) {
            commSb = new StringBuffer("no comments yet");
        }
                
        String hiddenFromDisplay  = (vcl.getHiddenFromDisplayBelowRoleLevel()  == null ? "unspecified" : vcl.getHiddenFromDisplayBelowRoleLevel().getLabel());
        String ProhibitedFromUpdate = (vcl.getProhibitedFromUpdateBelowRoleLevel() == null ? "unspecified" : vcl.getProhibitedFromUpdateBelowRoleLevel().getLabel());

        String customEntryForm = (vcl.getCustomEntryForm() == null ? "" : vcl.getCustomEntryForm());
        String customDisplayView = (vcl.getCustomDisplayView() == null ? "" : vcl.getCustomDisplayView());
        String customShortView = (vcl.getCustomShortView() == null ? "" : vcl.getCustomShortView());
        String customSearchView = (vcl.getCustomSearchView() == null ? "" : vcl.getCustomSearchView());
       //String lastModified = "<i>not implemented yet</i>"; // TODO
        String uri = (vcl.getURI() == null) ? "" : vcl.getURI();
        
        results.add(name);                   // 1
        results.add(shortDef);               // 2
        results.add(example);                // 3
        results.add(description);            // 4
        results.add(commSb.toString());      // 5
        results.add(groupName);              // 6
        results.add(hiddenFromDisplay);      // 7
        results.add(ProhibitedFromUpdate);   // 8
        results.add(customEntryForm);        // 9
        results.add(customDisplayView);      // 10
        results.add(customSearchView);       // 11
        results.add(uri);                    // 12
        request.setAttribute("results", results);
        request.setAttribute("columncount", NUM_COLS);
        request.setAttribute("suppressquery", "true");

        epo.setDataAccessObject(vcl);
        FormObject foo = new FormObject();
        HashMap OptionMap = new HashMap();

        HashMap formSelect = new HashMap(); // tells the JSP what select lists are populated, and thus should be displayed
        request.setAttribute("formSelect",formSelect);

        // if supported, we want to show only the asserted superclasses and subclasses.  Don't want to see anonymous classes, restrictions, etc.
        VClassDao vcDao;
        if (request.getAssertionsWebappDaoFactory() != null) {
        	vcDao = request.getAssertionsWebappDaoFactory().getVClassDao();
        } else {
        	vcDao = request.getFullWebappDaoFactory().getVClassDao();
        }
        List superURIs = vcDao.getSuperClassURIs(vcl.getURI(),false);
        List superVClasses = new ArrayList();
        Iterator superURIit = superURIs.iterator();
        while (superURIit.hasNext()) {
            String superURI = (String) superURIit.next();
            if (superURI != null) {
                VClass superVClass = vcDao.getVClassByURI(superURI);
                if (superVClass != null) {
                    superVClasses.add(superVClass);
                }
            }
        }
        request.setAttribute("superclasses",superVClasses);

        List subURIs = vcDao.getSubClassURIs(vcl.getURI());
        List subVClasses = new ArrayList();
        Iterator subURIit = subURIs.iterator();
        while (subURIit.hasNext()) {
            String subURI = (String) subURIit.next();
            VClass subVClass = vcDao.getVClassByURI(subURI);
            if (subVClass != null) {
                subVClasses.add(subVClass);
            }
        }
        request.setAttribute("subclasses",subVClasses);
        
        try {
	        List djURIs = vcDao.getDisjointWithClassURIs(vcl.getURI());
	        List djVClasses = new ArrayList();
	        Iterator djURIit = djURIs.iterator();
	        while (djURIit.hasNext()) {
	            String djURI = (String) djURIit.next();
	            try {
		            VClass djVClass = vcDao.getVClassByURI(djURI);
		            if (djVClass != null) {
		                djVClasses.add(djVClass);
		            }
	            } catch (Exception e) { /* probably owl:Nothing or some other such nonsense */ }
	        }
	        request.setAttribute("disjointClasses",djVClasses);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        try {
	        List eqURIs = vcDao.getEquivalentClassURIs(vcl.getURI());
	        List eqVClasses = new ArrayList();
	        Iterator eqURIit = eqURIs.iterator();
	        while (eqURIit.hasNext()) {
	            String eqURI = (String) eqURIit.next();
	            try {
		            VClass eqVClass = vcDao.getVClassByURI(eqURI);
		            if (eqVClass != null) {
		                eqVClasses.add(eqVClass);
		            }
	            } catch (Exception e) { }
	        }
	        request.setAttribute("equivalentClasses",eqVClasses);
        } catch (Exception e) {
        	log.error("Couldn't get the equivalent classes: ");
        	e.printStackTrace();
        }

        // add the options
        foo.setOptionLists(OptionMap);
        epo.setFormObject(foo);

        boolean instantiable = (vcl.getURI().equals(OWL.Nothing.getURI())) ? false : true;
        
        Portal portal = (new VitroRequest(request)).getPortal();
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("epoKey",epo.getKey());
        request.setAttribute("vclassWebapp", vcl);
        request.setAttribute("instantiable", instantiable);
        request.setAttribute("bodyJsp","/templates/edit/specific/classes_edit.jsp");
        request.setAttribute("portalBean",portal);
        request.setAttribute("title","Class Control Panel");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("VclassEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request,response);
    }

}