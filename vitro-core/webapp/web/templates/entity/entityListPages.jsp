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

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%><%/* this odd thing points to something in web.xml */ %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- Show pages to select from --%>
<%  
if( request.getAttribute("alpha") != null && ! "all".equalsIgnoreCase((String)request.getAttribute("alpha"))) {  
  request.setAttribute("pageAlpha",request.getAttribute("alpha"));
}else{
  request.setAttribute("pageAlpha",request.getAttribute("all"));
}
%>

<c:if test="${ requestScope.showPages }">
    <div class="searchpages minimumFontMain">    
    
    Pages:
    <c:forEach items='${requestScope.pages }' var='page'>
       <c:url var='pageUrl' value=".${requestScope.servlet}">                           
         <c:param name="page">${page.index}</c:param>        
         <c:if test="${not empty requestScope.alpha}">       
           <c:param name="alpha">${requestScope.pageAlpha}</c:param>
          </c:if>
       </c:url>
       <c:if test="${ page.selected }">
         ${page.text}
       </c:if>
       <c:if test="${ not page.selected }">
         <a class="minimumFontMain" href="${pageUrl}&amp;${requestScope.controllerParam}">${page.text} </a>    
       </c:if>   
    </c:forEach>
    </div>
</c:if>