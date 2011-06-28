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

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
    
<%@page import="java.util.List"%><h2><a class="ingestMenu" href="ingest">Ingest Menu</a> > Connect to Jena Database</h2>

    <form action="ingest" method="post">
        <input type="hidden" name="action" value="connectDB"/>

    <label for="JDBC URL">JDBC URL</label>
    <input type="text" style="width:80%;" name="jdbcUrl" value="jdbc:mysql://localhost/"/>
 
    <label for="username">Username</label>
    <input type="text" name="username"/>
    
    <label for="password">Password</label>
    <input type="password" name="password" class="block"/>

    <input id="tripleStoreRDB" name="tripleStore" type="radio" checked="checked" value="RDB"/>
    <label for="tripleStoreRDB" class="inline">Jena RDB</label>
    
    <input id="tripleStoreSDB" name="tripleStore" type="radio" value="SDB"/>
    <label for="tripleStoreRDB" class="inline">Jena SDB (hash layout)</label>
        
    <label for="database type">Database type</label>
    <select name="dbType">
        <c:forEach items="${requestScope.dbTypes}" var="typeName">
            <c:choose>
                <c:when test="${typeName eq 'MySQL'}">
                <option value="${typeName}" selected="selected">${typeName}</option>
                </c:when>
                    <c:otherwise>
                        <option value="${typeName}">${typeName}</option>
                    </c:otherwise>
            </c:choose>
        </c:forEach>
    </select>


    <input class="submit" type="submit" value="Connect Database" />
