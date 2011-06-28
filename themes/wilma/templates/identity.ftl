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

<header id="branding" role="banner">
    <h1 class="vivo-logo"><a title="VIVO | enabling national networking of scientists" href="${urls.home}"><span class="displace">${siteName}</span></a></h1>
    <#-- Since we are using a graphic text for the tagline, we won't render ${siteTagline}
    <#if siteTagline?has_content>
        <em>${siteTagline}</em>
    </#if>-->

    <nav role="navigation">
        <ul id="header-nav" role="list">
            <#if user.loggedIn>
                <li role="listitem"><img class="middle" src="${urls.images}/userIcon.png" alt="user icon" />${user.loginName}</li>
                <li role="listitem"><a href="${urls.logout}">Log out</a></li>
                <#if user.hasSiteAdminAccess>
                    <li role="listitem"><a href="${urls.siteAdmin}">Site Admin</a></li>
                </#if>
            <#else>
                <li role="listitem"><a title="log in to manage this site" href="${urls.login}">Log in</a></li>
            </#if>
            <#-- List of links that appear in submenus, like the header and footer. -->
                <li role="listitem"><a href="${urls.about}">About</a></li>
            <#if urls.contact??>
                <li role="listitem"><a href="${urls.contact}">Contact Us</a></li>
            </#if>
                <li role="listitem"><a href="http://www.vivoweb.org/support" target="blank">Support</a></li>
                <li role="listitem"><a href="${urls.index}">Index</a></li>
        </ul>
    </nav>
    
    <section id="search" role="region">
        <fieldset>
            <legend>Search form</legend>
            
            <form id="search-form" action="${urls.search}" name="search" role="search"> 
                <#if user.showFlag1SearchField>
                    <select id="search-form-modifier" name="flag1" class="form-item" >
                        <option value="nofiltering" selected="selected">entire database (${user.loginName})</option>
                        <option value="${portalId}">${siteTagline!}</option>
                    </select>
                <#else>
                    <input type="hidden" name="flag1" value="${portalId}" /> 
                </#if> 
                
                <div id="search-field">
                    <input type="text" name="querytext" class="search-vivo" value="${querytext!}" />
                    <input type="submit" value="Search" class="search">
                </div>
            </form>
        </fieldset>
    </section>
</header>