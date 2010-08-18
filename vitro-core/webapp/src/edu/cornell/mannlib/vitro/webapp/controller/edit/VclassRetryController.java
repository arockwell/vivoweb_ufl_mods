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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.beans.FormObject;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vedit.forwarder.PageForwarder;
import edu.cornell.mannlib.vedit.forwarder.impl.UrlForwarder;
import edu.cornell.mannlib.vedit.listener.ChangeListener;
import edu.cornell.mannlib.vedit.util.FormUtils;
import edu.cornell.mannlib.vedit.validator.impl.XMLNameValidator;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Classes2Classes;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.utils.RoleLevelOptionsSetup;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class VclassRetryController extends BaseEditController {
	
	private static final Log log = LogFactory.getLog(VclassRetryController.class.getName());

    public void doPost (HttpServletRequest req, HttpServletResponse response) {

    	VitroRequest request = new VitroRequest(req);
    	
        if (!checkLoginStatus(request,response))
            return;

        try {
            super.doGet(request,response);
        } catch (Exception e) {
            log.error("VclassRetryController encountered exception calling super.doGet()");
        }

        //create an EditProcessObject for this and put it in the session
        EditProcessObject epo = super.createEpo(request);
        epo.setDataAccessObject(request.getFullWebappDaoFactory().getVClassDao());

        /*for testing*/
        VClass testMask = new VClass();
        epo.setBeanClass(VClass.class);
        epo.setBeanMask(testMask);

        String action = null;
        if (epo.getAction() == null) {
            action = "insert";
            epo.setAction("insert");
        } else {
            action = epo.getAction();
        }

        VClassDao vcwDao = request.getFullWebappDaoFactory().getVClassDao();
        epo.setDataAccessObject(vcwDao);
        VClassGroupDao cgDao = request.getFullWebappDaoFactory().getVClassGroupDao();
        OntologyDao oDao = request.getFullWebappDaoFactory().getOntologyDao();

        VClass vclassForEditing = null;
        if (!epo.getUseRecycledBean()){
            if (request.getParameter("uri") != null) {
                try {
                    vclassForEditing = (VClass)vcwDao.getVClassByURI(request.getParameter("uri"));
                    action = "update";
                    epo.setAction("update");
                } catch (NullPointerException e) {
                    log.error("Need to implement 'record not found' error message.");
                }
            } else {
                vclassForEditing = new VClass();
                if (request.getParameter("GroupId") != null) {
                    try {
                        vclassForEditing.setGroupURI(request.getParameter("GroupURI"));
                    } catch (NumberFormatException e) {
                        // too bad
                    }
                }
            }
            epo.setOriginalBean(vclassForEditing);
        } else {
            vclassForEditing = (VClass) epo.getNewBean();
            // action = "update";
            // log.error("using newBean");
        }

        //make a simple mask for the class's id
        Object[] simpleMaskPair = new Object[2];
        simpleMaskPair[0]="URI";
        simpleMaskPair[1]=vclassForEditing.getURI();
        epo.getSimpleMask().add(simpleMaskPair);

        //validators
        List localNameValidatorList = new ArrayList();
        localNameValidatorList.add(new XMLNameValidator());
        epo.getValidatorMap().put("LocalName",localNameValidatorList);

        //set up any listeners
        List changeListenerList = new LinkedList();
        if (request.getParameter("superclassUri") != null) {
            changeListenerList.add(new SubclassListener(request.getParameter("superclassUri"), request.getFullWebappDaoFactory()));
        }
        epo.setChangeListenerList(changeListenerList);

        //set portal flag to current portal
        Portal currPortal = (Portal) request.getAttribute("portalBean");
        int currPortalId = 1;
        if (currPortal != null) {
            currPortalId = currPortal.getPortalId();
        }
        //make a postinsert pageforwarder that will send us to a new class's fetch screen
        epo.setPostInsertPageForwarder(new VclassInsertPageForwarder(currPortalId));
        //make a postdelete pageforwarder that will send us to the list of classes
        epo.setPostDeletePageForwarder(new UrlForwarder("showClassHierarchy?home="+currPortalId));

        //set the getMethod so we can retrieve a new bean after we've inserted it
        try {
            Class[] args = new Class[1];
            args[0] = String.class;
            epo.setGetMethod(vcwDao.getClass().getDeclaredMethod("getVClassByURI",args));
        } catch (NoSuchMethodException e) {
            log.error(this.getClass().getName()+" could not find the getVClassByURI method");
        }

        HashMap<String,List> optionMap = new HashMap<String,List>();
        try {
            VClassGroupDao vcgDao = request.getFullWebappDaoFactory().getVClassGroupDao();
            List classGroupOptionList = FormUtils.makeOptionListFromBeans(vcgDao.getPublicGroupsWithVClasses(),"URI","PublicName",vclassForEditing.getGroupURI(),null,(vclassForEditing.getGroupURI()!=null && !(vclassForEditing.getGroupURI().equals(""))));
            classGroupOptionList.add(new Option("", "none", ("update".equals(action) && (vclassForEditing.getGroupURI()==null || vclassForEditing.getGroupURI().equals("")))));
            optionMap.put("GroupURI", classGroupOptionList);

        } catch (Exception e) {
            log.error("unable to create GroupId option list");
        }
        try {
            List namespaceIdList = (action.equals("insert"))
                    ? FormUtils.makeOptionListFromBeans(oDao.getAllOntologies(),"URI","Name", ((vclassForEditing.getNamespace()==null) ? "" : vclassForEditing.getNamespace()), null, false)
                    : FormUtils.makeOptionListFromBeans(oDao.getAllOntologies(),"URI","Name", ((vclassForEditing.getNamespace()==null) ? "" : vclassForEditing.getNamespace()), null, true);
	        namespaceIdList.add(new Option(request.getFullWebappDaoFactory().getDefaultNamespace(),"default"));
            optionMap.put("Namespace", namespaceIdList);
        } catch (Exception e) {
            log.error(this.getClass().getName() + "unable to create Namespace option list");
        }

        optionMap.put("HiddenFromDisplayBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getDisplayOptionsList(vclassForEditing));    
        optionMap.put("ProhibitedFromUpdateBelowRoleLevelUsingRoleUri",RoleLevelOptionsSetup.getUpdateOptionsList(vclassForEditing));

        FormObject foo = new FormObject();
        foo.setErrorMap(epo.getErrMsgMap());
        foo.setOptionLists(optionMap);

        epo.setFormObject(foo);

        request.setAttribute("formValue",foo.getValues());

        String html = FormUtils.htmlFormFromBean(vclassForEditing,action,foo,epo.getBadValueMap());

        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("formHtml",html);
        request.setAttribute("bodyJsp","/templates/edit/formBasic.jsp");
        request.setAttribute("formJsp","/templates/edit/specific/vclass_retry.jsp");
        request.setAttribute("colspan","4");
        request.setAttribute("scripts","/templates/edit/formBasic.js");
        request.setAttribute("title","Class Editing Form");
        request.setAttribute("_action",action);
        request.setAttribute("unqualifiedClassName","VClass");
        setRequestAttributes(request,epo);

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("VclassRetryController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }

    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    /** This listener allows us to link a new class to a parent upon creation */
    class SubclassListener implements ChangeListener {
        String superclassURI = null;
        WebappDaoFactory daoFactory = null;
        public SubclassListener(String superclassURI, WebappDaoFactory cdf) {
            this.superclassURI = superclassURI;
            this.daoFactory = cdf;
        }
        public void doInserted(Object newObj, EditProcessObject epo) {
            Classes2Classes c2c = new Classes2Classes();
            c2c.setSubclassURI(((VClass)newObj).getURI());
            c2c.setSuperclassURI(superclassURI);
            daoFactory.getClasses2ClassesDao().insertNewClasses2Classes(c2c);            
        }
        public void doUpdated(Object oldObj, Object newObj, EditProcessObject epo) {
            // nothing to do
        }
        public void doDeleted(Object oldObj, EditProcessObject epo) {
            // nothing to do
        }
    }

    class VclassInsertPageForwarder implements PageForwarder {
        private int portalId = 1;

        public VclassInsertPageForwarder(int currPortalId) {
            portalId = currPortalId;
        }

        public void doForward(HttpServletRequest request, HttpServletResponse response, EditProcessObject epo){
            String newVclassUrl = "vclassEdit?home="+portalId+"&uri=";
            VClass vcl = (VClass) epo.getNewBean();
            try {
                newVclassUrl += URLEncoder.encode(vcl.getURI(),"UTF-8");
            } catch (Exception e) {
                log.error(this.getClass().getName()+" could not use UTF-8 encoding to encode new URL");
            }
            try {
                response.sendRedirect(newVclassUrl);
            } catch (IOException ioe) {
                log.error("VclassInsertPageForwarder could not send redirect.");
            }
        }
    }

}
