<%--
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
--%>

<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabMenu" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Portal" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.controller.VitroRequest" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.BreadCrumbsUtil" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.TabWebUtil" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.web.PortalWebUtil" %>
<%@ page import="edu.cornell.mannlib.vedit.beans.LoginStatusBean" %>
<%@ page import="java.util.List"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    /***********************************************
     Make the Tab menu list and search block

     mw542 2009-04-24 moved search from identity.jsp, updated with new code from bdc34
     bdc34 2006-01-03 created
     **********************************************/
     final Log log = LogFactory.getLog("edu.cornell.mannlib.vitro.web.menu.jsp");
     
     Portal portal = (Portal)request.getAttribute("portalBean");
     int portalId = -1;
     if (portal==null) {
     	portalId=1;
     } else {
     	portalId=portal.getPortalId();
     }

     VitroRequest vreq = new VitroRequest(request);
     
     List primaryTabs = vreq.getWebappDaoFactory().getTabDao().getPrimaryTabs(portalId);
     request.setAttribute("primaryTabs", primaryTabs);
     
     int tabId = TabWebUtil.getTabIdFromRequest(vreq); 
     int rootId = TabWebUtil.getRootTabId(vreq); 
     List tabLevels = vreq.getWebappDaoFactory().getTabDao().getTabHierarchy(tabId,rootId);
     request.setAttribute("tabLevels", tabLevels);

     String uri = (String)request.getAttribute("javax.servlet.forward.request_uri");
     if(uri != null){
    	 request.setAttribute("indexClass", uri.indexOf("browsecontroller") > 0 ? "class=\"activeTab\"" : "");

         if ( uri.indexOf("about") > 0) {
           request.setAttribute("aboutClass","class=\"activeTab\"");
         }
         if ( uri.indexOf("comments") > 0) {
           request.setAttribute("commentsClass","class=\"activeTab\"");
         }
     }
     
   // application variables not stored in application bean
     final String DEFAULT_SEARCH_METHOD = "fulltext";
     final int VIVO_SEARCHBOX_SIZE = 20;
     
     ApplicationBean appBean = vreq.getAppBean();
     PortalWebUtil.populateSearchOptions(portal, appBean, vreq.getWebappDaoFactory().getPortalDao());
     PortalWebUtil.populateNavigationChoices(portal, request, appBean, vreq.getWebappDaoFactory().getPortalDao());
     
     LoginStatusBean loginBean = LoginStatusBean.getBean(request);
     boolean isEditor = loginBean.isLoggedInAtLeast(LoginStatusBean.EDITOR);
     String loginName = loginBean.getUsername();
%>

<c:url var="themePath" value="/${themeDir}" />
<c:url var="searchURL" value="/search"/>
<c:set var="currentPortal" value="<%=portal.getPortalId()%>"/>
<c:set var="rootTab" value="<%=rootId%>"/>


<!-- ************** START menu.jsp ************** -->
<div id="navAndSearch" class="block">
  <div id="primaryAndOther">
    <ul id="primary">
      <c:forEach items="${primaryTabs}" var="tab">
        <li>
          <c:remove var="activeClass"/>
          <c:if test="${param.primary==tab.tabId}">
            <c:set var="activeClass"> class="activeTab" </c:set>
          </c:if>
          <c:forEach items="${tabLevels}" var="subTab">
            <c:if test="${subTab==tab.tabId && subTab != rootTab}">
              <c:set var="activeClass"> class="activeTab" </c:set>
            </c:if>
          </c:forEach>
          
          <c:url var="tabHref" value="/index.jsp"><c:param name="primary" value="${tab.tabId}"/></c:url>
          <a ${activeClass} href="${tabHref}">
             <c:out value="${tab.title}"/></a>
        </li>
      </c:forEach>
      <li>
         <a ${indexClass} href="<c:url value="/browsecontroller"/>"
            title="list all contents by type">
            Index</a>
      </li>
    </ul>
  
  </div><!--END 'primaryAndOther'-->
  
  <%-- TabMenu.getSecondaryTabMenu(vreq) --%> 


  <%------------- Search Form -------------%>
  <div id="searchBlock">
    <form id="searchForm" action="${searchURL}" >                	
      <label for="search">Search </label>
      <%  if (isEditor && appBean.isFlag1Active()) { %>
      <select id="search-form-modifier" name="flag1" class="form-item" >
        <option value="nofiltering" selected="selected">entire database (<%=loginName%>)</option>
      	<option value="${currentPortal}"><%=portal.getShortHand()%></option>
      </select>
      <%  } else {%>
      <input type="hidden" name="flag1" value="${currentPortal}" />
      <%  } %>
      <input type="text" name="querytext" id="search" class="search-form-item" value="<c:out value="${requestScope.querytext}"/>" size="<%=VIVO_SEARCHBOX_SIZE%>" />
    	<input class="search-form-submit" name="submit" type="submit"  value="Search" />
  	</form>
  </div>

<%-- this div is needed for clearing floats --%>
<%-- <div class="clear"></div> --%>

</div><!-- END 'navigation' -->
<div id="breadcrumbs" class="small"><%=BreadCrumbsUtil.getBreadCrumbsDiv(request)%></div>


<!-- ************************ END menu.jsp ************************ -->


