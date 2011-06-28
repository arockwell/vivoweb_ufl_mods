<#--
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
-->

<#include "menupage-checkForData.ftl">

<#if !noData>
    <section id="menupage-intro" class="people" role="region">
        <h2>${page.title}</h2>
        
        <section id="find-by" role="region">
            <nav role="navigation">
                <h3>Find By: </h3>
                
                <#assign subjectAreaUri = "http://vivoweb.org/ontology/core#SubjectArea" />
                <#assign departmentUri = "http://vivoweb.org/ontology/core#Department" />
                <#assign courseUri = "http://vivoweb.org/ontology/core#Course" />
                
                <ul id="find-filters">
                    <li><a href="${urls.base}/individuallist?vclassId=${subjectAreaUri?url}">Subject Area</a></li>
                    <li><a href="${urls.base}/individuallist?vclassId=${departmentUri?url}">Department</a></li>
                    <li><a href="${urls.base}/individuallist?vclassId=${courseUri?url}">Courses</a></li>
                </ul>
            </nav>
        </section>
    </section>
    
    <#include "menupage-browse.ftl">
    
    ${stylesheets.add("/css/menupage/menupage.css")}
    
    <#include "menupage-scripts.ftl">
    
    ${scripts.add("/js/menupage/browseByVClassPeople.js")}
<#else>
    ${noDataNotification}
</#if>