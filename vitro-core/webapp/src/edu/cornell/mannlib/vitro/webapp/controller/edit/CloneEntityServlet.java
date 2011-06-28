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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * NOTE:does not work yet under semweb-align - gets tricky here
 * Takes an entity and inserts a (nearly) identical entity with a matching properties list.
 * NOTE (BJL23): This currently modifies the objects returned from the daos and calls inserts.
   If we go to a system of persistent in-memory objects, this method will need to be updated.
 * @author bjl23,jc55
 * */
public class CloneEntityServlet extends BaseEditController {
    private static final String NO_PROPERTY_RESTRICTION = null;
    private static final int DEFAULT_PORTAL_ID=1;
    private static final int MIN_EDIT_ROLE=4;
    private static final Log log = LogFactory.getLog(CloneEntityServlet.class.getName());

    public void doPost(HttpServletRequest req, HttpServletResponse response) {
    	VitroRequest request = new VitroRequest(req);
        HttpSession session = req.getSession();
        ServletContext context = getServletContext();
        try {
            String portalIdStr = (portalIdStr = request.getParameter("home")) == null ? String
                    .valueOf(DEFAULT_PORTAL_ID)
                    : portalIdStr;

            if (!checkLoginStatus(request,response)) {
//                getServletConfig().getServletContext().getRequestDispatcher(
//                "/index.jsp?home=" + portalIdStr).forward(request,
//                 response);
                 return;
            }

            //attempt to clone a tab but if we don't find the parameter 'tabId' the clone a entity
            try{
                int id = doCloneTab(request, response);
                if( id >= 0){
                    response.sendRedirect("tabEdit?home=" + portalIdStr
                        + "&id=" + id);
                    return;
                }
            }catch(Exception ex){
                log.error("Could not clone tab: " + ex);
                getServletConfig().getServletContext().getRequestDispatcher(
                    "/index.jsp?home=" + portalIdStr).forward(request,
                    response);
                return;
            }

        String individualURI=request.getParameter("uri");
        if (individualURI == null || individualURI.equals("")){
            getServletConfig().getServletContext().getRequestDispatcher("/index.jsp?home="+portalIdStr).forward( request, response );
            return;
        }
        
        LoginStatusBean loginBean = LoginStatusBean.getBean(request);
		WebappDaoFactory myWebappDaoFactory = request.getFullWebappDaoFactory().getUserAwareDaoFactory(loginBean.getUserURI());
        IndividualDao individualDao = myWebappDaoFactory.getIndividualDao();
        PropertyInstanceDao propertyInstanceDao = myWebappDaoFactory.getPropertyInstanceDao();

        Individual ind = individualDao.getIndividualByURI(individualURI);
        if (ind == null){ log.error("Error retrieving individual "+individualURI); return; }
        String newName = request.getParameter("name");
        if (newName != null && newName.length()>0) {
            ind.setName(newName);
            if (Character.isDigit(newName.charAt(0))) {
                newName = "n"+newName;
            }
            if (ind.getNamespace() != null) {
                ind.setURI(ind.getNamespace()+newName.replaceAll("\\W",""));
            } else {
                ind.setURI(VitroVocabulary.vitroURI + newName.replaceAll("\\W",""));
            }
        } else {
            ind.setName("CLONE OF "+ind.getName());
        }
        
        String timeKeyStr = request.getParameter("timekey");
        if (timeKeyStr!=null && !timeKeyStr.equals("")) {
            Date timekey = parseStringToDate(timeKeyStr);
            if (timekey!=null) {
                ind.setTimekey(timekey);
            }
        }
        // specify other values that do NOT want to be carried over from the existing Individual
        ind.setSunrise(new DateTime().toDate());
        // cannot set these values to null because they will still be copies
        ind.setBlurb("");
        ind.setDescription("");        
 
        String cloneURI=individualDao.insertNewIndividual(ind);
        if (cloneURI == null){ log.error("Error inserting cloned individual"); return; }

        Collection<PropertyInstance> PIColl=propertyInstanceDao.getExistingProperties(individualURI, NO_PROPERTY_RESTRICTION);
        Iterator<PropertyInstance> it=PIColl.iterator();
        while (it.hasNext()){
            PropertyInstance currPI=(PropertyInstance)it.next();
            currPI.setSubjectEntURI(cloneURI);
            propertyInstanceDao.insertProp(currPI);
        }

        // addIndividualToLuceneIndex( context, cloneURI );

        String encodedCloneURI = URLEncoder.encode(cloneURI, "UTF-8");
        response.sendRedirect("entityEdit?home="+portalIdStr+"&uri="+encodedCloneURI);
        //response.sendRedirect("entity?home="+portalIdStr+"&id="+newEntityId);

    } catch (Exception ex) {
        log.error( ex.getMessage() );
        ex.printStackTrace();
    }
}
    
public Date parseStringToDate(String str) {
    if (str.trim() == "") return null;
    int year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour;
    try {
        year = Integer.parseInt(str.substring(0,4));
        monthOfYear = Integer.parseInt(str.substring(5,7));
        dayOfMonth = Integer.parseInt(str.substring(8,10));
        hourOfDay = Integer.parseInt(str.substring(11,13));
        minuteOfHour = str.length()>13 ? Integer.parseInt(str.substring(14,16)) : 0;
        if (str.length()>16) {
            String suppStr = str.substring(17);
            if (suppStr.equalsIgnoreCase("pm") || suppStr.equalsIgnoreCase("p.m.")) {
                hourOfDay = hourOfDay < 12 ? hourOfDay + 12 : hourOfDay;
            }
        }
        log.warn("parsed clone timekey ["+str+"] to year: "+year+", month: "+monthOfYear+", day: "+dayOfMonth+", hour: "+hourOfDay+", minute: "+minuteOfHour);
    } catch (NumberFormatException ex) {
        log.error("Could not parse string "+str+" based on expected format 'yyyy-mm-dd hh-mm'");
        return null;
    }
    return new DateTime(year,monthOfYear,dayOfMonth,hourOfDay,minuteOfHour,0,0).toDate();
}


public void doGet(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {
    doPost(request,response);
}

/**
 * Attempts to clone a tab.
 * This will check for the parameter 'tabId' and if it exists
 * this method will call cloneTabDb to do the actual database inserts.
 * If no parameter is found then this method will return -1.
 *
 * @param request
 * @param response
 * @return tab id of newly cloned tab or -1 if no tabId parameter was found
 * in the http request.
 */
private int doCloneTab(HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    String tabIdStr = request.getParameter("tabId");
    if (tabIdStr == null)
        return -1;

    int tabId = -1;
    try {
        tabId = Integer.parseInt(tabIdStr);
    } catch (NumberFormatException ex) {
        throw new Exception("error doCloneTab()-- could not parse "
                + tabIdStr + " as an integer value");
    }
    return 1; // getFacade().cloneTab(tabId);
}



}
