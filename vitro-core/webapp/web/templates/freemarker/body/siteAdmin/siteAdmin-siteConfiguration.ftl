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

<#-- Template for Site Administration site configuration panel -->

<#if siteConfig??>
    <div class="pageBodyGroup">
        
        <h3>Site Configuration</h3>
        
        <ul>
            <#if siteConfig.urls.portals??>
                <li><a href="${siteConfig.urls.siteInfo}">Current portal information</a></li>
                <li><a href="${siteConfig.urls.portals}">List all portals</a></li>
            <#else>
                <li><a href="${siteConfig.urls.siteInfo}">Site information</a></li>
            </#if>
            
            <#if siteConfig.urls.menuN3Editor??>
                <li><a href="${siteConfig.urls.menuN3Editor}">Menu management</a></li>  
            </#if>
            
            <li><a href="${siteConfig.urls.tabs}">Tab management</a></li>
            
            <#if siteConfig.urls.users??>
                <li><a href="${siteConfig.urls.users}">User accounts</a></li>  
            </#if>
            
        </ul>
    </div>
</#if>