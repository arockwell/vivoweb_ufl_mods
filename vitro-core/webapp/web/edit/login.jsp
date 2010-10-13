<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep" %>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory"%>
<%@page import="com.hp.hpl.jena.ontology.OntModel"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.dao.jena.LoginEvent"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page errorPage="/error.jsp"%>

<!--
<div align="center">The experimental in-line editing is disabled for this system</div>

<c:url value="/" var="siteRoot" />
<div align="center">
  <button type="button"
    onclick="javascript:document.location.href='${siteRoot}'">
    Return to main site</button>
-->


<%
    String errorMsg = "";
    IdentifierBundle ids =
        ServletIdentifierBundleFactory.getIdBundleForRequest(request,session,pageContext.getServletContext());
    
    //get the selfEditingId
    SelfEditingIdentifierFactory.SelfEditing selfEditingId =
        SelfEditingIdentifierFactory.getSelfEditingIdentifier(ids);
    
    if( selfEditingId != null ){        
        if( selfEditingId.getBlacklisted() == SelfEditingIdentifierFactory.NOT_BLACKLISTED &&
            selfEditingId.getValue() != null ){               
    		VitroRequestPrep.forceToSelfEditing(request);
            
            //write who logged in to the audit log.
            Authenticate.sendLoginNotifyEvent(
                    new LoginEvent(selfEditingId.getValue()), 
                    application,
                    session);
            
            //This puts the user into the all portal when the log into 
            //self editing.  The reason for this is to avoid confusion
            //created by individuals that are hidden by portal filtering
            //but visible when logged in to self editing.
            %>
            <c:set var="personUri"><%= selfEditingId.getValue() %></c:set>
            <c:redirect url="/entity">       
                <c:param name="uri" value="${personUri}"/>
            </c:redirect>
            <%
            return;        
       } else {
          // Conditions that deny a user self-editing:
          // no netid from CUWebAuth, not really logged in
          // blacklisted
          // A good netid but no individual in the system       
          %> <jsp:forward page="/edit/mayNotLogin.jsp"/> <%
          return;                
      }
    }    
    
    //get the netId
    String netid = null;
    if( ids != null ){
        for(Identifier id : ids){
            if( id instanceof SelfEditingIdentifierFactory.NetId){
                netid = ((SelfEditingIdentifierFactory.NetId)id).getValue();
            }
        }
    }

    
    String personUri = null;
    WebappDaoFactory wdf = (WebappDaoFactory)application.getAttribute("WebappDaoFactory");    
    String uri = null;
    Individual ind = null;
    
    try{
        if( netid != null )
            uri =wdf.getIndividualDao().getIndividualURIFromNetId(netid);    
        if( uri != null )
            ind = wdf.getIndividualDao().getIndividualByURI(uri);
    
        if( netid == null || netid.length() <= 0){
            errorMsg = NO_NETID_ERROR_MSG;
        }else if( netid.length() > 100 ){
            errorMsg = "The system could not accept the login '" + netid +
            "', it is too long.";
        }else if( uri == null || ind == null ){
            errorMsg = NO_MATCHING_NETID_ERROR_MSG;
        }            
    }catch(RuntimeException rx){
        errorMsg = DEFAULT_ERROR_MSG;
    }    

    VitroRequestPrep.forceOutOfSelfEditing(request);
    //continue on to JSP error page bellow
    request.setAttribute("title","there was a problem accessing your profile");
    request.setAttribute("errorMsg",errorMsg);
%>


<jsp:include page="formPrefix.jsp"/>
<div id="content" class="full">
    <div align="center">${errorMsg}</div>

    <c:url value="/" var="siteRoot"/>	
    <div align="center">                
      <button type="button" 
        onclick="javascript:document.location.href='${siteRoot}'">
        Return to main site</button>
    </div>
</div>
<jsp:include page="formSuffix.jsp"/>

<%!

    	private final String DEFAULT_ERROR_MSG ="There was an error in the system while accessing your profile, "
        	+ "please use the contact us form to request assistance.";
	private final String NO_NETID_ERROR_MSG = "<p>No login is available from the current session; "
		+ "Authentication may not be configured on this server.</p>"
		+ "<p>Please use the contact form to let us know that "
		+ "you were unable to edit your profile.</p>";
	
    private final String NO_MATCHING_NETID_ERROR_MSG = "There does not seem to be a profile in the system for your login";
    private final String MULTIPLE_NETID_ERROR_MSG =
            "There is a problem with the system, please use the contact us form to let us "+
            "know that you were unable to edit your profile." ;
    
%>
