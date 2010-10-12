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

<%-- rjy7 NIHVIVO-1158 Under core:authorInAuthorship on a person's page, we are now displaying the object property statements
for the related property core:linkedInformationResource, so that we can collate by publication subclass. The subject is the
Authorship, and the object is the InformationResource, so the authorship short view defined on Authorships no longer applies.
We thus define an information resource short view to display the publications. --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:choose>
    <c:when test="${!empty individual}"><%-- individual is the OBJECT of the property referenced -- the InformationResource individual --%>
        <c:choose>
            <c:when test="${!empty predicateUri}">
                <c:choose>
                    <c:when test="${predicateUri == 'http://vivoweb.org/ontology/core#authorInAuthorship'}">      
                        <c:set var="name"  value="${individual.name}"/>      
                        <c:set var="uri" value="${individual.URI}"/>
                        <c:set var="year" value="${individual.dataPropertyMap['http://vivoweb.org/ontology/core#year'].dataPropertyStatements[0].data}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="name" value="unknown predicate - please contact your VIVO support team"/>
                        <c:set var="uri" value="${predicateUri}"/>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${!empty uri}">
                        <c:url var="olink" value="/entity"><c:param name="uri" value="${uri}"/></c:url>
                        <a href="<c:out value="${olink}"/>">${name}</a> ${year}
                    </c:when>
                    <c:otherwise>
                        <strong>${name}</strong> ${year}
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <c:out value="No predicate available for custom rendering ..."/>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:out value="Got nothing to draw here ..."/>
    </c:otherwise>
</c:choose>
