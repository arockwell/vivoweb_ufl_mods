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

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.text.Collator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class IndividualTypeRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(IndividualTypeRetryController.class.getName());

	public void doGet (HttpServletRequest request, HttpServletResponse response) {
		
        if(!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("Classes2ClassesRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
		
        VitroRequest vreq = new VitroRequest(request);
        
        WebappDaoFactory t;
        WebappDaoFactory wadf = ((t = vreq.getAssertionsWebappDaoFactory()) != null) ? t : vreq.getFullWebappDaoFactory();
        IndividualDao iDao = wadf.getIndividualDao();
        VClassDao vcDao = wadf.getVClassDao();
        
        String individualURI = request.getParameter("IndividualURI");
        
        Individual ind = iDao.getIndividualByURI(individualURI);
        if (ind == null) {
        	ind = new IndividualImpl(individualURI);
        }
        request.setAttribute("individual", ind);
        
		List<VClass> allVClasses = vcDao.getAllVclasses();
		Set<String> allClassURISet = new HashSet<String>();
		Map<String,String> classNameMap = new HashMap<String,String>();
		for (Iterator allClassIt = allVClasses.iterator(); allClassIt.hasNext(); ) {
			VClass vc = (VClass) allClassIt.next();
			classNameMap.put(vc.getURI(),vc.getLocalNameWithPrefix());
			allClassURISet.add(vc.getURI());
		}
		allVClasses = null;
			
		for (Iterator<VClass> indClassIt = ind.getVClasses(false).iterator(); indClassIt.hasNext(); ) {
			VClass vc = indClassIt.next();
			allClassURISet.remove(vc.getURI());
			for (Iterator<String> djURIIt = vcDao.getDisjointWithClassURIs(vc.getURI()).iterator(); djURIIt.hasNext(); ) {
				String djURI = djURIIt.next();
				allClassURISet.remove(djURI);
			}
		}
		
		List<String> classURIList = new LinkedList<String>();
		classURIList.addAll(allClassURISet);
		
		FormObject foo = new FormObject();
		epo.setFormObject(foo);
		HashMap optionMap = new HashMap();
		foo.setOptionLists(optionMap);

		List<Option> typeOptionList = new ArrayList<Option>(); 
		
		for (Iterator<String> classURIIt = classURIList.iterator(); classURIIt.hasNext();) {
			String classURI = classURIIt.next();
			Option opt = new Option(classURI,classNameMap.get(classURI));
			typeOptionList.add(opt);
		}
		
		Collections.sort(typeOptionList,new OptionCollator());
		optionMap.put("types",typeOptionList);
		
	       RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
	       request.setAttribute("editAction","individualTypeOp");
	        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
	        request.setAttribute("scripts","/templates/edit/formBasic.js");
        	request.setAttribute("formJsp","/templates/edit/specific/individualType_retry.jsp");
        	request.setAttribute("title","Individual Type Editing Form");
	        request.setAttribute("_action","insert");
	        setRequestAttributes(request,epo);

	        try {
	            rd.forward(request, response);
	        } catch (Exception e) {
	            log.error(this.getClass().getName()+" could not forward to view.");
	            log.error(e.getMessage());
	            log.error(e.getStackTrace());
	        }
	        
	}
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) {
		// shouldn't be posting to this controller
	}
	
	private class OptionCollator implements Comparator {
	    public int compare (Object o1, Object o2) {
	        Collator collator = Collator.getInstance();
	        return collator.compare( ((Option)o1).getBody() , ((Option)o2).getBody() );
	    }
	}
	
}
