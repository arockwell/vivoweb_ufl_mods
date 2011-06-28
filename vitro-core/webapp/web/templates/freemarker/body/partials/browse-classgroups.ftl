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

<#-- Browse class groups on the home page. Could potentially become a widget -->

${stylesheets.add("/css/browseClassGroups.css")}

<#macro allClassGroups classGroups>
    <#-- Loop through classGroups first so we can account for situations when all class groups are empty -->
    <#assign selected = 'class="selected" ' />
    <#assign classGroupList>
        <#list classGroups as group>
            <#-- Only display populated class groups -->
            <#if (group.individualCount > 0)>
                <#-- Catch the first populated class group. Will be used later as the default selected class group -->
                <#if !firstPopulatedClassGroup??>
                    <#assign firstPopulatedClassGroup = group />
                </#if>
                <#-- Determine the active (selected) group -->
                <#assign activeGroup = "" />
                <#if !classGroup??>
                    <#if group_index == 0>
                        <#assign activeGroup = selected />
                    </#if>
                <#elseif classGroup.uri == group.uri>
                    <#assign activeGroup = selected />
                </#if>
                <li role="listitem"><a ${activeGroup}href="${urls.currentPage}?classgroupUri=${group.uri?url}#browse" title="Browse ${group.displayName?capitalize}" data-uri="${group.uri}" data-count="${group.individualCount}">${group.displayName?capitalize} <span class="count-classes">(${group.individualCount})</span></a></li>
            </#if>
        </#list>
    </#assign>
    
    <#-- Display the class group browse only if we have at least one populated class group -->
    <#if firstPopulatedClassGroup??>
        <section id="browse" role="region">
            <h4>Browse by</h4>
            
            <ul id="browse-classgroups" role="list">
                ${classGroupList}
            </ul>
            
            <#-- If requesting the home page without any additional URL parameters, select the first populated class group-->
            <#assign defaultSelectedClassGroup = firstPopulatedClassGroup />
            
            <section id="browse-classes" role="navigation">
                <nav>
                    <ul id="classes-in-classgroup" class="vis" role="list">
                        <#if classes??>
                            <#-- We don't need to send parameters because the data we need is delivered as template variables -->
                            <@classesInClassgroup />
                        <#else>
                            <#-- We need to pass the data to the macro because the only template variable provided by default is classGroups -->
                            <@classesInClassgroup classes=defaultSelectedClassGroup.classes classGroup=defaultSelectedClassGroup />
                        </#if>
                    </ul>
                </nav>
                <#if classes??>
                    <#-- We don't need to send parameters because the data we need is delivered as template variables -->
                    <@visualGraph />
                <#else>
                    <#-- We need to pass the data to the macro because the only template variable provided by default is classGroups -->
                    <@visualGraph classes=defaultSelectedClassGroup.classes classGroup=defaultSelectedClassGroup />
                </#if>
            </section> <!-- #browse-classes -->
        </section> <!-- #browse -->
        
        <#-- For v1.3: The controller should pass in the dataservice url. -->
        <script type="text/javascript">
            var browseData = {
                baseUrl: '${urls.base}',
                dataServiceUrl: '${urls.base}/dataservice?getVClassesForVClassGroup=1&classgroupUri=',
                defaultBrowseClassGroupUri: '${firstPopulatedClassGroup.uri!}',
                defaultBrowseClassGroupCount: '${firstPopulatedClassGroup.individualCount!}'
            };
        </script>

        ${scripts.add("/js/browseClassGroups.js")}
    <#else>
        <#-- Would be nice to update classgroups-checkForData.ftl with macro so it could be used here as well -->
        <#-- <#include "classgroups-checkForData.ftl"> -->
        <h3>There is currently no content in the system</h3>
        
        <#if user.loggedIn>
            <#if user.hasSiteAdminAccess>
                <p>You can <a href="${urls.siteAdmin}" title="Manage content">add content and manage this site</a> from the Site Administration page.</p>
            </#if>
        <#else>
            <p>Please <a href="${urls.login}" title="log in to manage this site">log in</a> to manage content.</p>
        </#if>
    </#if>
</#macro>


<#macro classesInClassgroup classes=classes classGroup=classGroup>
     <#list classes as class>
        <#if (class.individualCount > 0)>
            <li role="listitem"><a href="${urls.base}/individuallist?vclassId=${class.uri?url}" title="Browse all ${class.name} content">${class.name}</a></li>
        </#if>
     </#list>
</#macro>


<#macro visualGraph classes=classes classGroup=classGroup>
    <section id="visual-graph" class="barchart" role="region">
        <#-- Will be populated dynamically via AJAX request -->
    </section>
    
    ${scripts.add("/js/raphael/raphael.js", "/js/raphael/g.raphael.js", "/js/raphael/g.bar.js")}
</#macro>