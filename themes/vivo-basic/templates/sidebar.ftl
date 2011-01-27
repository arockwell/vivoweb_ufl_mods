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
          <h2>Faculty & Staff</h2>
          <p>
            <img src="/themes/vivo-basic/images/profile_thumb.jpg" width="54" height="58" class="alignleft border">
            <#if shibbolethUri??>
              <#if shibbolethLoginName??>
                Logged in as ${shibbolethLoginName}
              </#if>
            <#else>
              Log in now to manage your profile page.
            </#if>
	<a href="/edit/login.jsp"> 
             <img src="/themes/vivo-basic/images/manage.jpg" width="186" height="43" alt="Login">
	</a>
          </p>
       <#if shibbolethLoginName??>
           <h2>Need Help?</h2> 
		<p>
		<li class="last"><a href="/themes/vivo-basic/help/how_to_update_your_profile.pdf">How to update your profile</a></li>
		<li class="last"><a href="/?primary=1773136708&home=1">Contact us</a></li>
		<li class="last"><a href="/?primary=304347269&home=1">Send us your CV</a></li>
		</p>
       </#if>
     </#if>


          <h2>Latest from VIVO</h2>
          <ul id="latestVIVOFeed">
            <li>Please enable javascript for best viewing experience.</li>
          </ul>
          <p>
            <a href="http://vivoweb.org/blog" class="more">More from the VIVO blog</a>
          </p>

        </div> <!-- sidebar -->
