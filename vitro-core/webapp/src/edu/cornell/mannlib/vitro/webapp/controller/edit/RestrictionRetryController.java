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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.vocabulary.RDFS;
import com.ibm.icu.text.Collator;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class RestrictionRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RestrictionRetryController.class.getName());
	private static final boolean DATA = true;
	private static final boolean OBJECT = false;
	
	public void doGet(HttpServletRequest req, HttpServletResponse response) {
		
		VitroRequest request = new VitroRequest(req);
		if (!checkLoginStatus(request,response))
		    return;
		
		try {
		    super.doGet(request,response);
		} catch (Exception e) {
		    log.error("PropertyRetryController encountered exception calling super.doGet()");
		}
		
		try {
			
			EditProcessObject epo = createEpo(request);
			
			request.setAttribute("editAction","addRestriction");
			epo.setAttribute("VClassURI", request.getParameter("VClassURI"));
			
			String restrictionTypeStr = request.getParameter("restrictionType");
			epo.setAttribute("restrictionType",restrictionTypeStr);
			request.setAttribute("restrictionType",restrictionTypeStr);
				
			// default to object property restriction
			boolean propertyType = ("data".equals(request.getParameter("propertyType"))) ? DATA : OBJECT ;
			
			List<Property> pList = (propertyType == OBJECT) 
				? request.getFullWebappDaoFactory().getObjectPropertyDao().getAllObjectProperties()
			    : request.getFullWebappDaoFactory().getDataPropertyDao().getAllDataProperties();
			List<Option> onPropertyList = new LinkedList<Option>(); 
			Collections.sort(pList, new PropSorter());
			for (Iterator<Property> i = pList.iterator(); i.hasNext(); ) {
				Property p = i.next(); 
				onPropertyList.add( new Option(p.getURI(),p.getLocalNameWithPrefix()) );
			}
					
			epo.setFormObject(new FormObject());
			epo.getFormObject().getOptionLists().put("onProperty", onPropertyList);
			
			if (restrictionTypeStr.equals("someValuesFrom")) {
				request.setAttribute("specificRestrictionForm","someValuesFromRestriction_retry.jsp");
				List<Option> optionList = (propertyType == OBJECT) 
					? getValueClassOptionList(request)
					: getValueDatatypeOptionList(request) ;
				epo.getFormObject().getOptionLists().put("ValueClass",optionList);
			} else if (restrictionTypeStr.equals("allValuesFrom")) {
				request.setAttribute("specificRestrictionForm","allValuesFromRestriction_retry.jsp");
				List<Option> optionList = (propertyType == OBJECT) 
					? getValueClassOptionList(request)
				    : getValueDatatypeOptionList(request) ;
				epo.getFormObject().getOptionLists().put("ValueClass",optionList);
			} else if (restrictionTypeStr.equals("hasValue")) {
				request.setAttribute("specificRestrictionForm", "hasValueRestriction_retry.jsp");
				if (propertyType == OBJECT) {
					request.setAttribute("propertyType", "object");	
				} else {
					request.setAttribute("propertyType", "data");
				}	
			} else if (restrictionTypeStr.equals("minCardinality") || restrictionTypeStr.equals("maxCardinality") || restrictionTypeStr.equals("cardinality")) {
				request.setAttribute("specificRestrictionForm", "cardinalityRestriction_retry.jsp");
			} 
			
	        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
	        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
	        request.setAttribute("formJsp","/templates/edit/specific/restriction_retry.jsp");
	        request.setAttribute("scripts","/templates/edit/formBasic.js");
	        request.setAttribute("title","Add Restriction");
	        request.setAttribute("_action","insert");
	        setRequestAttributes(request,epo);
	
	        try {
	            rd.forward(request, response);
	        } catch (Exception e) {
	            log.error(this.getClass().getName()+"PropertyRetryController could not forward to view.");
	            log.error(e.getMessage());
	            log.error(e.getStackTrace());
	        }
        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private List<Option> getValueClassOptionList(VitroRequest request) {
		List<Option> valueClassOptionList = new LinkedList<Option>();
		VClassDao vcDao = request.getFullWebappDaoFactory().getVClassDao();
		for (Iterator i = vcDao.getAllVclasses().iterator(); i.hasNext(); ) {
			VClass vc = (VClass) i.next();
			valueClassOptionList.add(new Option(vc.getURI(), vc.getLocalNameWithPrefix()));
		}
		return valueClassOptionList;
	}
	
	private List<Option> getValueDatatypeOptionList(VitroRequest request) {
		List<Option> valueDatatypeOptionList = new LinkedList<Option>();
		DatatypeDao dtDao = request.getFullWebappDaoFactory().getDatatypeDao();
		for (Iterator i = dtDao.getAllDatatypes().iterator(); i.hasNext(); ) {
			Datatype dt = (Datatype) i.next();
			valueDatatypeOptionList.add(new Option(dt.getUri(), dt.getName()));
		}
		valueDatatypeOptionList.add(new Option(RDFS.Literal.getURI(), "rdfs:Literal"));
		return valueDatatypeOptionList;
	}
	
	private class PropSorter implements Comparator<Property> {
		
		public int compare(Property p1, Property p2) {
			if (p1.getLocalNameWithPrefix() == null) return 1;
			if (p2.getLocalNameWithPrefix() == null) return -1;
			return Collator.getInstance().compare(p1.getLocalNameWithPrefix(), p2.getLocalNameWithPrefix());
		}
		
	}
	
}
