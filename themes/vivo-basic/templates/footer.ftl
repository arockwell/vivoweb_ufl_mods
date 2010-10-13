<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<div id="footer">
  <div class="footerLinks">
		<ul class="otherNav">
        <li><a href="/about" title="more about this web site">About VIVO</a></li>
        <li><a href="/contact" title="feedback form">Contact Us</a></li>
	<li><a href="http://privacy.ufl.edu/privacystatement.html">Privacy Policy</a></li>
	<li><a href="http://www.ufl.edu/">University of Florida</a></li>
	<#if loginName??>
		<li class="last">logged in as <strong>${loginName}</strong> - <a href="${urls.logout}">logout</a></li>
	<#else>
		<li class="last"><a href="${urls.login}">Admin</a></li>
	</#if>
        </ul>
		<div id="uflogo"><a href="http://www.ufl.edu/"><img src="/themes/vivo-basic/images/UF_white.png" width="196" height="35" alt="University of Florida"></a></div>
    </div>
    <div class="copyright">
		    &copy;2010&nbsp;
			VIVO Project</div>
	    <div class="copyright">
		    All Rights Reserved. <a href="/termsOfUse?home=1">Terms of Use</a>
	    </div>
	</div>
</div>
