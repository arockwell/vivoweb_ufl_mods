<%--
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
--%>

<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean"%>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.Individual" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.beans.VClass" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.flags.PortalFlagChoices" %>
<%@ page import="edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page errorPage="/error.jsp"%>
<%  /***********************************************
    Displays the sparkline visualizations on individual profile pages

    request.attributes:
    an Entity object with the name "entity" 


    request.parameters:
    None, should only work with requestScope attributes for security reasons.

    Consider sticking < % = MiscWebUtils.getReqInfo(request) % > in the html output
    for debugging info.
            
    **********************************************/ 
    Individual entity = (Individual)request.getAttribute("entity");
    boolean displayVisualization = false;

    if (entity == null){
        String e = "sparklineVisuzalition.jsp expects that request attribute 'entity' be set to the Entity object to display.";
        displayVisualization = false;
        throw new JspException(e);
    } else {
        for (VClass currClass : entity.getVClasses()) {
            if ("http://xmlns.com/foaf/0.1/Person".equalsIgnoreCase(currClass.getURI())) {
                displayVisualization = true;
                break;
            }
        }	
    }
    //System.out.println("visualization is supposed to be displayed? > " + displayVisualization);
    if (displayVisualization) {

%>


        <c:set var='portalBean' value='${currentPortal}'/>
        <c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>
        <c:url var="loadingImageLink" value="/${themeDir}site_icons/visualization/ajax-loader.gif"></c:url>

        <!-- START Visualization Code -->            
        <c:url var="visualizationURL" value="/visualization">
            <c:param name="render_mode" value="dynamic"/>
            <c:param name="container" value="vis_container"/>
            <c:param name="vis" value="person_pub_count"/>
            <c:param name="vis_mode" value="short"/>
            <c:param name="uri" value="${entity.URI}"/>
        </c:url>
	
        <%-- PDF Visualization URL

        For now we have disabled this.
       
        <c:url var="pdfURL" value="/visualization">
            <c:param name="render_mode" value="pdf"/>
            <c:param name="container" value="vis_container"/>
            <c:param name="vis" value="person_pub_count"/>
            <c:param name="vis_mode" value="full"/>
            <c:param name="uri" value="${entity.URI}"/>
        </c:url>

        --%>

        <style type="text/css">
            #vis_container {
                cursor:pointer;
                /*height:36px;
                margin-left:24%;
                margin-top:-2%;
                position:absolute;*/
                /*width:380px;*/
            }
        </style>
	
        <script type="text/javascript">
        <!--

        $(document).ready(function() {

            function renderVisualization(visualizationURL) {
                <%--  
                $("#vis_container").empty().html('<img src="${loadingImageLink}" />');
                --%>
                $.ajax({
                    url: visualizationURL,
                    dataType: "html",
                    success:function(data){
                     $("#vis_container").html(data);

                    }
                });
            }
            
           renderVisualization('${visualizationURL}');

        });

        //-->
        </script>
        
        <div id="vis_container">&nbsp;</div>

        <!--[if lte IE 7]>
        <style type="text/css">

        #vis_container a{
            padding-bottom:5px;
        }

        .vis_link a{
            margin-top: 15px;
            padding:10px;
            display: block;
        }
        </style>
        <![endif]-->

        <%-- 

        For now we have disabled PDF report vis.

        <div id="pdf_url">
            This is the <a href="${pdfURL}">link</a> to PDF report.
        </div>

        --%>

        <!-- END Visualization Code -->

<%

    }

%>