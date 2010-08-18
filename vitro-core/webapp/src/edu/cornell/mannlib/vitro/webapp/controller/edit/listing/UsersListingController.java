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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;

public class UsersListingController extends BaseEditController {

    private String[] roleNameStr = new String[51];

    public UsersListingController() {
        roleNameStr[1] = "self editor";
        roleNameStr[4] = "editor";
        roleNameStr[5] = "curator";
        roleNameStr[50] = "system administrator";
    }

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

        UserDao dao = vrequest.getFullWebappDaoFactory().getUserDao();

        List<User> users = dao.getAllUsers();
        Collections.sort(users);

        ArrayList results = new ArrayList();
        results.add("XX");
        results.add("User");
        results.add("first name");
        results.add("last name");
        results.add("role");
        results.add("first login");
        results.add("login count");
        //results.add("recent edits"); // 2010-01-21 not currently supporting
        
        Integer width = results.size();

        String EMPTY = "";

        if (users != null) {
            Iterator<User> userIt = users.iterator();
            while (userIt.hasNext()) {
                User user = userIt.next();
                results.add("XX");
                if (user.getUsername() != null) {
                    try {
                        results.add("<a href=\"./userEdit?uri="+URLEncoder.encode(user.getURI(),"UTF-8")+"&amp;home="+portal.getPortalId()+"\">"+user.getUsername()+"</a>");
                    } catch (Exception e) {
                        results.add(user.getUsername());
                    }
                } else {
                    results.add("");
                }
                String firstNameStr = (user.getFirstName() != null) ? user.getFirstName() : EMPTY;
                results.add(firstNameStr);
                String lastNameStr = (user.getLastName() != null) ? user.getLastName() : EMPTY;
                results.add(lastNameStr);
                String roleStr = "";
                try {
                    roleStr = roleNameStr[Integer.decode(user.getRoleURI())];
                } catch (Exception e) {}
                results.add(roleStr);
                String firstLoginStr = "";
                try {
                    firstLoginStr = (DISPLAY_DATE_FORMAT.format(user.getFirstTime()));
                } catch (Exception e) {}
                results.add(firstLoginStr);
                String loginCountStr = Integer.toString(user.getLoginCount());
                results.add(loginCountStr);
// 2010-01-21 not currently supporting "recent edits"
//                try {
//                	results.add("<a href=\"statementHistory?userURI="+URLEncoder.encode(user.getURI(),"UTF-8")+"\">recent edits</a>");
//                } catch (Exception e) {}

            }
            request.setAttribute("results",results);
        }

        request.setAttribute("columncount", width);
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","User Accounts");
        request.setAttribute("portalBean",portal);
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        request.setAttribute("horizontalJspAddButtonUrl", Controllers.RETRY_URL);
        request.setAttribute("horizontalJspAddButtonText", "Add new user account");
        request.setAttribute("horizontalJspAddButtonControllerParam", "User");
        request.setAttribute("home", portal.getPortalId());
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
