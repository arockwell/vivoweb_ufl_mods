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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;


import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

public class RefactorOperationController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(RefactorOperationController.class.getName());
	
	private String doFixDataTypes(HttpServletRequest request, HttpServletResponse response)
	{
		
		String userURI = null;
		LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
		if (loginBean != null) {
			userURI = loginBean.getUserURI();
		}
		
		try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" caught exception calling doGet()");
        }
		VitroRequest vreq = new VitroRequest(request);
		Portal portal = vreq.getPortal();

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp", Controllers.CHECK_DATATYPE_PROPERTIES);
        request.setAttribute("portalBean",portal);
        request.setAttribute("title","Check Datatype Properties");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
        
		OntModel ontModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		ontModel.enterCriticalSection(Lock.WRITE);
		ArrayList<String> results = new ArrayList<String>();
		
		/* Debugging code thats inserts invalid triples into model
		Property hasTitle = ontModel.getProperty("http://www.owl-ontologies.com/Ontology1209425965.owl#hasTitleee");
		Resource product = ontModel.createResource("http://www.owl-ontologies.com/Ontology1209425965.owl#someBook14");
		Resource product2 = ontModel.createResource("http://www.owl-ontologies.com/Ontology1209425965.owl#someBook15");
		Literal illegalLiteral = ontModel.createTypedLiteral(134);
		Literal illegalLiteral2 = ontModel.createTypedLiteral(234);
		ontModel.add(product, hasTitle, illegalLiteral);
		ontModel.add(product2, hasTitle, illegalLiteral2);
		*/
		
		try
		{
			ExtendedIterator dataProperties = ontModel.listDatatypeProperties();
			int hasRange = 0;
			int consistent = 0;
			int inconsistent = 0;
			int total = 0;
			int fixed = 0;
			while(dataProperties.hasNext()) // Iterate through all datatype properties
			{
				total++;
				DatatypeProperty p = (DatatypeProperty)dataProperties.next();
				OntResource range = p.getRange();
				if(range != null) hasRange++;
				NodeIterator n = ontModel.listObjectsOfProperty(p);
				//if(!n.hasNext()) results.add(p.getLocalName()+" is not in any statements");
				while(n.hasNext()) // Iterate through all objects of all datatype properties
				{
					RDFNode node = n.nextNode();
					if(node.isLiteral())
					{
						if(range != null) // If a literal has a predicate with a defined range, check and fix the literal's datatype
							{
								Literal l = (Literal)node;
								StmtIterator usingPandL = ontModel.listStatements(null, p, l);
								int size = 0;
								results.add("Statements using property "+p.getLocalName()+" and literal "+l.getLexicalForm()+":");
								while(usingPandL.hasNext())
								{
									Statement st = usingPandL.nextStatement();
									results.add("    "+st.getSubject().getLocalName()+" "+p.getLocalName()+" "+l.getLexicalForm());
									size++;
								}
								usingPandL.close();
								boolean valid = range.getURI().equals(l.getDatatypeURI());
								if(valid) consistent+= size;
								else 
									{
										results.add(p.getLocalName()+" has object "+l.getLexicalForm()+" of type "+l.getDatatypeURI()+" which is inconsistent");
										String typeName = "";
										if(range.getURI().contains(XSDDatatype.XSD)) typeName = range.getURI().substring(XSDDatatype.XSD.length()+1);
										else results.add("ERROR: "+p.getLocalName()+" has a range which does not contain the XSD namespace");
										Literal newLiteral = null;
										try {
											newLiteral = ontModel.createTypedLiteral(l.getLexicalForm(), new XSDDatatype(typeName));
										}
										catch(NullPointerException e){
											results.add("ERROR: Can't create XSDDatatype for literal "+l.getLexicalForm());
										}
										StmtIterator badStatements = ontModel.listStatements(null, p, l);
										StmtIterator toRemove = ontModel.listStatements(null, p, l);
										ArrayList<Statement> queue = new ArrayList<Statement>();
										while(badStatements.hasNext()) 
										{
											Statement badState = badStatements.nextStatement();
											Statement goodState = ontModel.createStatement(badState.getSubject(), p, newLiteral);
											queue.add(goodState);
											results.add("    Replacing: "+badState.toString());
											results.add("    With:      "+goodState.toString());
											fixed++;
										}
										for(int i = 0; i<queue.size(); i++)
										{
											ontModel.add(queue.get(i));
										}
										ontModel.remove(toRemove);
										badStatements.close();
										toRemove.close();
									}
								if(valid) results.add("Literal "+l.getLexicalForm()+" is in the range of property "+p.getLocalName());
								results.add("--------------");
							}
					}
					else results.add("ERROR: "+node.toString()+" is not a literal");
					
				}
				n.close();
			}
			dataProperties.close();
			
			results.add(hasRange+" of "+total+" datatype properties have defined ranges.");
			results.add("Of the statements that contain datatype properties with defined ranges, "+consistent+" are consistent and "+fixed+" are inconsistent.");
			results.add(fixed+" statements have been fixed.");
			//for(int i=0; i<results.size(); i++) System.out.println(results.get(i));
			
		}
		finally
		{
			ontModel.leaveCriticalSection();
		}
		request.setAttribute("results", results);
		try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

		return "";
	}

	private String doRenameResource(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		
		String userURI = null;
		LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
		if (loginBean != null) {
			userURI = loginBean.getUserURI();
		}
		
		OntModel ontModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		
		String oldURIStr = (String) epo.getAttribute("oldURI");
		String newURIStr = request.getParameter("newURI");
			
		// validateURI
		String errorMsg = null;
		try {
			request.getFullWebappDaoFactory().checkURI(newURIStr);
		} catch (InvalidPropertyURIException ipue) {
			// TODO We don't know if we're editing a property's URI or not here!
		}
		
		if (errorMsg != null) {
			epo.setAttribute("errorMsg",errorMsg);
            String referer = request.getHeader("Referer");
            int epoKeyIndex = referer.indexOf("_epoKey");
            if (epoKeyIndex<0)
            	try {
            		response.sendRedirect(referer+"&_epoKey="+request.getParameter("_epoKey"));
            	} catch (IOException ioe) {}
            else{
                String url = referer.substring(0,epoKeyIndex) + "_epoKey="+request.getParameter("_epoKey");
                try {
                	response.sendRedirect(url);
                } catch (IOException ioe) {}
            }
            return "STOP";
		}
			
		ontModel.enterCriticalSection(Lock.WRITE);
		ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
		try {
			Property prop = ontModel.getProperty(oldURIStr);
			if(prop != null)
			{
				try {
					Property newProp = ontModel.createProperty(newURIStr);
					StmtIterator statements = ontModel.listStatements(null, prop, (RDFNode)null);
					try {
						while(statements.hasNext()) {
							Statement statement = (Statement)statements.next();
							Resource subj = statement.getSubject();
							RDFNode obj = statement.getObject();
							Statement newStatement = ontModel.createStatement(subj, newProp, obj);
							ontModel.add(newStatement);
						}
					} finally {
						if (statements != null) {
							statements.close();
						}
					}
					ontModel.remove(ontModel.listStatements(null, prop, (RDFNode)null));
				} catch (InvalidPropertyURIException ipue) {
					/* if it can't be a property, don't bother with predicates */ 
				}			
				Resource res = ontModel.getResource(oldURIStr);
				ResourceUtils.renameResource(res,newURIStr);
			}
		} finally {
			ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,false));
			ontModel.leaveCriticalSection();
		}
		
		String redirectStr = null;
		
		/* we can't go back to the referer, because the URI is now different. */
		String refererStr;
		if ( (refererStr = epo.getReferer()) != null) {
			String controllerStr = null;
			String[] controllers = {"entityEdit", "propertyEdit", "datapropEdit", "ontologyEdit", "vclassEdit"};
			for (int i=0; i<controllers.length; i++) {
				if (refererStr.indexOf(controllers[i]) > -1) {
					controllerStr = controllers[i];
				}
			}
			if (controllerStr != null) {
				int portalId = -1;
				try {
					portalId = request.getPortalId();
				} catch (Throwable t) {}
				try {
					newURIStr = URLEncoder.encode(newURIStr, "UTF-8");
				} catch (UnsupportedEncodingException e) {}
				redirectStr = controllerStr+"?home="+((portalId>-1) ? portalId : "" )+"&uri="+newURIStr;
			}
		}
		
		return redirectStr;
		
	}
	
	private void doMovePropertyStatements(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		String userURI = null;
		LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
		if (loginBean != null) {
			userURI = loginBean.getUserURI();
		}
		
		OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
		
		Model tempRetractModel = ModelFactory.createDefaultModel();
		Model tempAddModel = ModelFactory.createDefaultModel();
		
		String oldURIStr = (String) epo.getAttribute("propertyURI");
		String newURIStr = request.getParameter("NewPropertyURI");
		String subjectClassURIStr = request.getParameter("SubjectClassURI");
		String objectClassURIStr = request.getParameter("ObjectClassURI");
		
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Resource res = ontModel.getResource(oldURIStr);
			Resource subjClass = (subjectClassURIStr.equals("") ? null : ResourceFactory.createResource(subjectClassURIStr));
			Property prop = ResourceFactory.createProperty(oldURIStr);
			Property newProp = (newURIStr.equals("")) ? null : ResourceFactory.createProperty(newURIStr);
			OntProperty propInv = null;
			OntProperty newPropInv = null;
			try {
				propInv = ontModel.getObjectProperty(prop.getURI()).getInverse();
			} catch (Exception e) { }
			try {
				newPropInv = ontModel.getObjectProperty(newProp.getURI()).getInverse();
			} catch (Exception e) { }
			RDFNode objClass = (objectClassURIStr == null || objectClassURIStr.equals("")) ? null : ResourceFactory.createResource(objectClassURIStr);
			
			ClosableIterator closeIt = (epo.getAttribute("propertyType").equals("ObjectProperty")) ?
				ontModel.listStatements(null,prop,(Resource)null) :
				ontModel.listStatements(null,prop,(Literal)null);
			try {
				for (Iterator stmtIt = closeIt; stmtIt.hasNext();) {
					Statement stmt = (Statement) stmtIt.next();
					Resource subj = stmt.getSubject();
					boolean moveIt = true;
					if (objClass != null) {
						Resource obj = (Resource) stmt.getObject();
						if (!ontModel.contains(obj,RDF.type,objClass)) {
							moveIt = false;
						}
					}
					if (moveIt && subjClass != null) {
						if (!ontModel.contains(subj,RDF.type,subjClass)) {
							moveIt = false;
						}
					}
					if (moveIt) {
						tempRetractModel.add(stmt);
						if (propInv != null) {
							tempRetractModel.add((Resource)stmt.getObject(),propInv,stmt.getSubject());
						}
						if (newProp != null) {
							tempAddModel.add(stmt.getSubject(),newProp,stmt.getObject());
							if (newPropInv != null) {
								tempAddModel.add((Resource)stmt.getObject(),newPropInv,stmt.getSubject());
							}
						}
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			ontModel.leaveCriticalSection();
		}
		ontModel.enterCriticalSection(Lock.WRITE);
		ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
		try {
			ontModel.remove(tempRetractModel);
			ontModel.add(tempAddModel);
		} finally {
			ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,false));
			ontModel.leaveCriticalSection();
		}

	}
	
	private void doMoveInstances(VitroRequest request, HttpServletResponse response, EditProcessObject epo) {
		String userURI = null;
		LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
		if (loginBean != null) {
			userURI = loginBean.getUserURI();
		}
		
		OntModel ontModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		if (ontModel==null) {
			ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
		}
		
		String oldClassURIStr = (String) epo.getAttribute("VClassURI");
		String newClassURIStr = (String) request.getParameter("NewVClassURI");
		
		Model tempRetractModel = ModelFactory.createDefaultModel();
		Model tempAddModel = ModelFactory.createDefaultModel();
		
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Resource oldClassRes = ontModel.getResource(oldClassURIStr);
			Resource newClassRes = (newClassURIStr.equals("")) ? null : ontModel.getResource(newClassURIStr);
			ClosableIterator closeIt = ontModel.listStatements(null, RDF.type, oldClassRes);
			try {
				for (Iterator stmtIt = closeIt; stmtIt.hasNext();) {
					Statement stmt = (Statement) stmtIt.next();
					tempRetractModel.add(stmt);
					if (newClassRes != null) {
						tempAddModel.add(stmt.getSubject(),stmt.getPredicate(),newClassRes);
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			ontModel.leaveCriticalSection();
		}
		
		ontModel.enterCriticalSection(Lock.WRITE);
		ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
		try {
			ontModel.remove(tempRetractModel);
			ontModel.add(tempAddModel);
		} finally {
			ontModel.getBaseModel().notifyEvent(new EditEvent(userURI,false));
			ontModel.leaveCriticalSection();
		}
		
	}
	
    public void doPost(HttpServletRequest req, HttpServletResponse response) {
    	VitroRequest request = new VitroRequest(req);
    	String defaultLandingPage = getDefaultLandingPage(request);
    	
        if(!checkLoginStatus(request,response))
        {
        	RequestDispatcher rd = request.getRequestDispatcher(Controllers.SITE_ADMIN);
           
            try {
                rd.forward(request, response);
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not forward to view.");
                log.error(e.getMessage());
                log.error(e.getStackTrace());
            }
            return;
        }
        
        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error(this.getClass().getName()+" encountered exception calling super.doGet()");
        }
        
        VitroRequest vreq = new VitroRequest(request);

        HashMap epoHash = null;
        EditProcessObject epo = null;
        try {
            epoHash = (HashMap) request.getSession().getAttribute("epoHash");
            epo = (EditProcessObject) epoHash.get(request.getParameter("_epoKey"));
        } catch (NullPointerException e) {
            //session or edit process expired
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException f) {
                e.printStackTrace();
            }
            return;
        }
        
        String modeStr;
        if (epo == null) 
        {
        	// Handles the case where we want to a type check on objects of datatype properties
        	handleConsistencyCheckRequest(request, response);
        	return;
        }
        else modeStr = (String)epo.getAttribute("modeStr");
        
        String redirectStr = null;
        
        if (request.getParameter("_cancel") == null) {
	        if (modeStr != null) {
	        	
	        	if (modeStr.equals("renameResource")) {
	        		redirectStr = doRenameResource(vreq, response, epo);
	        	} else if (modeStr.equals("movePropertyStatements")) {
	        		doMovePropertyStatements(vreq, response, epo);
	        	} else if (modeStr.equals("moveInstances")) {
	        		doMoveInstances(vreq, response, epo);
	        	} 
	        }
        }
        
        if (!"STOP".equals(redirectStr)) {
	        if (redirectStr == null) {
	        	redirectStr = (epo.getReferer()==null) ? defaultLandingPage : epo.getReferer();
	        }
	        try {
	            response.sendRedirect(redirectStr);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }

    }
    
    
    private void handleConsistencyCheckRequest(HttpServletRequest req, HttpServletResponse response)
    {
    	String modeStr = req.getParameter("modeStr");
    	if(modeStr != null)
    		if (modeStr.equals("fixDataTypes")) doFixDataTypes(req,response);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse response)
    {
    	doPost(req, response);
    }
        
}
        

