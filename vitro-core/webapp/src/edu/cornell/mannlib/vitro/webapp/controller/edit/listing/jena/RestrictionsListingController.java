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

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing.jena;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class RestrictionsListingController extends BaseEditController {

	private static String LAMBDA = "";
	
	private EditProcessObject epo = null;
	
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
        
        epo = super.createEpo(request);
        
        LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");

        OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
        
        ObjectPropertyDao opDao = vrequest.getFullWebappDaoFactory().getObjectPropertyDao();
        VClassDao vcDao = vrequest.getFullWebappDaoFactory().getVClassDao();
        IndividualDao iDao = vrequest.getFullWebappDaoFactory().getIndividualDao();
        
        ArrayList results = new ArrayList();
        request.setAttribute("results",results);
        results.add("XX");
        results.add("property");
        results.add("restriction");
        results.add("filler(s)");
        results.add(LAMBDA);
        
        String vClassURI = request.getParameter("VClassURI");

        if (vClassURI != null) {
        	ontModel.enterCriticalSection(Lock.READ);
        	try {
	        	OntClass ontClass = ontModel.getOntClass(vClassURI);
	        	if (ontClass != null) {
	        		ClosableIterator superClassIt = ontClass.listSuperClasses();
	        		try {
		        		for (Iterator i = superClassIt; i.hasNext(); ) {
		        			OntClass superClass = (OntClass) i.next();
		        			tryRestriction(superClass, vcDao, opDao, iDao, results, vClassURI);
		        		}
	        		} finally {
	        			superClassIt.close();
	        		}
	        		ClosableIterator equivClassIt = ontClass.listEquivalentClasses();
	        		try {
		        		for (Iterator i = equivClassIt; i.hasNext(); ) {
		        			OntClass superClass = (OntClass) i.next();
		        			tryRestriction(superClass, vcDao, opDao, iDao, results, vClassURI);
		        		}
	        		} finally {
	        			equivClassIt.close();
	        		}
	        	} else {
	        		doClassNotFound(results);
	        	}
            } finally {
            	ontModel.leaveCriticalSection();
            }
        } else {
        	doClassNotFound(results);
        }


        

        request.setAttribute("columncount",new Integer(5));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Restrictions");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("home", portal.getPortalId());
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
    private void tryRestriction(OntClass theClass, VClassDao vcDao, ObjectPropertyDao opDao, IndividualDao iDao, ArrayList results, String vClassURI) {
		if (theClass.isRestriction()) {
			Restriction rest = (Restriction) theClass.as(Restriction.class);
			try {
				results.add("XX");
				Property onProperty = rest.getOnProperty();
				ObjectProperty op = opDao.getObjectPropertyByURI(onProperty.getURI());
				results.add(op.getLocalNameWithPrefix());
				if (rest.isAllValuesFromRestriction()) {
					results.add("all values from");
					AllValuesFromRestriction avfrest = (AllValuesFromRestriction) rest.as(AllValuesFromRestriction.class);
					Resource allValuesFrom = avfrest.getAllValuesFrom();
					results.add(printAsClass(vcDao, allValuesFrom));	        					
				} else if (rest.isSomeValuesFromRestriction()) {
					results.add("some values from");
					SomeValuesFromRestriction svfrest = (SomeValuesFromRestriction) rest.as(SomeValuesFromRestriction.class);
					Resource someValuesFrom = svfrest.getSomeValuesFrom();
					results.add(printAsClass(vcDao, someValuesFrom));
				} else if (rest.isHasValueRestriction()) {
					results.add("has value");
					HasValueRestriction hvrest = (HasValueRestriction) rest.as(HasValueRestriction.class);
					RDFNode hasValue = hvrest.getHasValue();
					if (hasValue.isResource()) {
						Resource hasValueRes = (Resource) hasValue.as(Resource.class);
						try {
							if (hasValueRes.getURI() != null) {
								Individual ind = iDao.getIndividualByURI(hasValueRes.getURI());
								if (ind.getName() != null) {
									results.add(ind.getName());
								}
							}
						} catch (Exception e) {
							results.add("???");
						}
					}
					
				} else if (rest.isMinCardinalityRestriction()) {
					MinCardinalityRestriction crest = (MinCardinalityRestriction) rest.as(MinCardinalityRestriction.class);
					results.add("at least "+crest.getMinCardinality());
					results.add(LAMBDA);
				} else if (rest.isMaxCardinalityRestriction()) {
					MaxCardinalityRestriction crest = (MaxCardinalityRestriction) rest.as(MaxCardinalityRestriction.class);
					results.add("at most "+crest.getMaxCardinality());
					results.add(LAMBDA);
				} else if (rest.isCardinalityRestriction()) {
					CardinalityRestriction crest = (CardinalityRestriction) rest.as(CardinalityRestriction.class);
					results.add("exactly "+crest.getCardinality());
					results.add(LAMBDA);
				}
				
				results.add("<form action=\"addRestriction\" method=\"post\">" +
						        "<input type=\"hidden\" name=\"_action\" value=\"delete\"/>" +
						        "<input type=\"submit\" value=\"Delete\"/>" + 
						        "<input type=\"hidden\" name=\"_epoKey\" value=\""+epo.getKey()+"\"/>" +
						        "<input type=\"hidden\" name=\"classUri\" value=\""+vClassURI+"\"/>" +
						        "<input type=\"hidden\" name=\"restrictionId\" value=\""+( (rest.getId() != null) ? rest.getId() : rest.getURI() )+"\"/>" +
						    "</form>");
				
			} catch (Exception e) {
				e.printStackTrace(); // results.add("unknown property");
			}
					
		}	
    }
    
    private String printAsClass(VClassDao vcDao, Resource res) {
    	String UNKNOWN = "???";
    	try {
    		VClass vClass = vcDao.getVClassByURI(res.getURI());
    		return (vClass.getName() != null) ? vClass.getName() : UNKNOWN ;
    	} catch (Exception e) {
    		return UNKNOWN;
    	}
    }

    private void doClassNotFound(ArrayList results) {
    	results.add("XX");
    	results.add("Class not found");
    	results.add(LAMBDA);
    	results.add(LAMBDA);
    	results.add(LAMBDA);
    }
    
	
}
