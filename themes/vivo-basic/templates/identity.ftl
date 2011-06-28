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

<#import "lib-list.ftl" as l>

<#include "vivo-basic-deprecation.ftl">

<div id="wrap" class="container">
    <div id="header">

        <#-- uncomment this div to place your institutional branding/identity at the top of every page
        <div id="institution">
        </div>
        -->

        <div id="identity">

            <h1><a title="Home" href="${urls.home}">${siteName}</a></h1>

            <ul id="otherMenu">  
                <@l.firstLastList>  
                    <#if user.loggedIn>
                        <li>
                            Logged in as <strong>${user.loginName}</strong> (<a href="${urls.logout}">Log out</a>)
                        </li>                                        
                        <#if user.hasSiteAdminAccess>
                            <li><a href="${urls.siteAdmin}">Site Admin</a></li>
                        </#if>
                    <#else>
                         <li><a title="log in to manage this site" href="${urls.login}">Log in</a></li>
                    </#if> 
            
                    <#include "subMenuLinks.ftl">
                </@l.firstLastList>
            </ul>   
        </div>