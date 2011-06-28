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

<#-- Browse widget -->

<#macro assets>
  <#-- 
   Are there stylesheets or scripts needed? 
   ${stylesheets.add("/css/browse.css")} 
   ${scripts.add("/js/browse.js")}
   -->        
</#macro>

<#macro allClassGroups>
    <section id="browse" role="region">
        <h4>Browse</h4>
        
        <ul id="browse-classgroups" role="list">
        <#list vclassGroupList as group>
            <#if (group.individualCount > 0)>
                <li role="listitem"><a href="${urls.currentPage}?classgroupUri=${group.uri?url}">${group.displayName?capitalize} <span class="count-classes">(${group.individualCount})</span></a></li>
            </#if>
        </#list>
        </ul>
    </section>
    
    <#-- <@classGroup /> -->
</#macro>

<#macro classGroup>
    <section id="browse" role="region">
        <h4>Browse</h4>
        
         <section id="browse-classes" role="navigation">
             <nav>
                 <ul id="classes-in-classgroup" role="list">
                     <#list classes as class>
                        <#if (class.individualCount > 0)>
                            <li role="listitem"><a href="${urls.currentPage}?classgroupUri=${classGroup.uri?url}&vclassUri=${class.uri?url}">${class.name} <span class="count-individuals"> (${class.individualCount})</span></a></li>
                        </#if>
                     </#list>
                 </ul>
             </nav>
        </section>
    </section>
</#macro>

<#macro vclass>
    <section id="browse" role="region">
    <h4>Browse</h4>    
        <div>
            vclass ${class.name} from ${classGroup.displayName}
            This has classGroup, classes, individualsInClass and class.
        </div> 
         
        <ul>
            <#list individualsInClass as ind>
                <li><a href="${urls.base}/individual?uri=${ind.uri?url}">${ind.name}</a></li>
            </#list>
        </section>
</#macro>

<#macro vclassAlpha>
    <section id="browse" role="region">
    <h4>Browse</h4>     
        <div>vclassAlpha is not yet implemented.</div> 
    </section>
</#macro>
