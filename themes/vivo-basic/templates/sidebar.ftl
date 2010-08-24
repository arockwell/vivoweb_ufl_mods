<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
        <div id="sidebar">
<#if loginName??>
          <h2>Log out of VIVO</h2>
          <p>
            Logged in as<br/>
            <strong>${loginName}</strong><br/>
            <a href="${urls.siteAdmin}">Site Admin</a>
          </p>
          <p>
            <a href="${urls.logout}">
              <img src="/themes/vivo-basic/images/logout.jpg" width="186" height="43" alt="Logout">
            </a>
          </p>
<#else>
          <h2>Log into VIVO</h2>
          <#-- p>
            <img src="/themes/vivo-basic/images/profile_thumb.jpg" width="54" height="58" class="alignleft border">
            Log in now to manage your profile page.
          </p -->
          <p>
            <a href="${urls.login}">
              <img src="/themes/vivo-basic/images/login.jpg" width="186" height="43" alt="Login">
            </a>
          </p>
</#if>
          <h2>Latest from VIVO</h2>
          <ul id="latestVIVOFeed">
            <li>Please enable javascript for best viewing experience.</li>
          </ul>
          <p>
            <a href="http://vivoweb.org/blog" class="more">More from the VIVO blog</a>
          </p>
        </div> <!-- sidebar -->
