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

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@page import="edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep"%>

<c:set var='themeDir'><c:out value='${portalBean.themeDir}' /></c:set>
                </div> <!-- #content.form -->

            </div>
        <div class="push"></div>

        <jsp:include page="/templates/page/freemarkerTransition/footer.jsp" flush="true"/>

    </div><!-- end wrap -->

    <script type="text/javascript" src="<c:url value="/js/extensions/String.js"/>"></script></script>
    <script type="text/javascript" src="<c:url value="/js/jquery.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/js/jquery_plugins/jquery.bgiframe.pack.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/js/jquery_plugins/thickbox/thickbox-compressed.js"/>"></script>
    <!-- <script type="text/javascript" src="<c:url value="/js/jquery_plugins/ui.datepicker.js"/>"></script> -->
    
<%  String useAutoComplete = (useAutoComplete=request.getParameter("useAutoComplete")) != null && !(useAutoComplete.equals("")) ? useAutoComplete : "false";
    if (useAutoComplete.equalsIgnoreCase("true")) { %>
        <script type="text/javascript" src="<c:url value="/js/jquery_plugins/jquery-autocomplete/jquery.autocomplete.pack.js"/>"></script> 
<%  } %>
        
    <c:forEach var="jsFile" items="${customJs}">
        <script type="text/javascript" src="<c:url value="${jsFile}"/>"></script>
    </c:forEach>  
    
</body>
</html>