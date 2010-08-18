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

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing;

import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class VClassWebappWithInstancesListingController extends BaseEditController {

    private int NUM_COLS = 6;
    
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


        String uriStr = request.getParameter("uri");

        ArrayList results = new ArrayList();

        if(uriStr != null) {

            VClassDao dao = vrequest.getFullWebappDaoFactory().getVClassDao();

            results.add("XX");
            results.add("name");
            results.add("group");
            results.add("example");
            results.add("last modified");
            results.add("XX");

            VClass vcw = (VClass) dao.getVClassByURI(uriStr);

            if (vcw != null) {

                results.add("XX");
                String nameStr = (vcw.getName()==null) ? "" : vcw.getName();
                results.add(nameStr);
                String groupStr = ""; // TODO
                results.add(groupStr);
                String exampleStr = (vcw.getExample()==null) ? "" : vcw.getExample();
                results.add(exampleStr);
                String lastModifiedStr = ""; //TODO
                results.add(lastModifiedStr);
                results.add("XX");

                IndividualDao ewDao = vrequest.getFullWebappDaoFactory().getIndividualDao();

                List ents = ewDao.getIndividualsByVClassURI(vcw.getURI(), -2, -2);
                if (ents != null && ents.size()>0) {
                    results.add("+");
                    results.add("XX");
                    results.add("Class");
                    results.add("example");
                    results.add("description");
                    results.add("@@entities");
                    Iterator entIt = ents.iterator();
                    int maxEnts = 25;
                    while (entIt.hasNext() && maxEnts>0) {
                        --maxEnts;
                        Individual ew = (Individual) entIt.next();
                        results.add("XX");
                        if (ew.getName() != null && ew.getURI() != null) {
                            try {
                                results.add("<a href=\"entityEdit?uri="+URLEncoder.encode(ew.getURI(),"UTF-8")+"\">"+ew.getName()+"</a>");
                            } catch (Exception e) {
                                results.add(ew.getName());
                            }
                        } else {
                            results.add("");
                        }
                        //String exampleStr = (vcw.getExample() == null) ? "" : vcw.getName();
                        //results.add(exampleStr);
                        String descriptionStr = (vcw.getDescription() == null) ? "" : vcw.getDescription();
                        results.add(descriptionStr);
                        if (entIt.hasNext())
                            results.add("@@entities");

                    }
                }
                request.setAttribute("results",results);
            }
        }

        request.setAttribute("columncount",new Integer(NUM_COLS));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Class Groups");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    private class EntityWebappAlphaComparator implements Comparator {
        public int compare (Object o1, Object o2) {
            Collator collator = Collator.getInstance();
            return collator.compare(((ObjectProperty)o1).getLocalName(),((ObjectProperty)o2).getLocalName());
        }
    }

}
