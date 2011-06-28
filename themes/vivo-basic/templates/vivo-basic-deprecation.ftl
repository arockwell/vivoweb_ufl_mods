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

<div id="deprecated" role="alert">
   <h1>The vivo-basic theme has been deprecated with the 1.2 release and is not recommended for production instances.</h1>

   <p>Since vivo-basic was the default theme for all previous releases, it is included as part of VIVO 1.2 to help with the transition of upgrading existing installations to the latest code, but all vivo-basic development has ceased and it will not be distributed in future releases.</p>

   <p>Please note that vivo-basic does not support all of the new 1.2 features. Most notably, in choosing to use vivo-basic you will be missing out on the following:</p>

   <ul>
       <li>new primary menu for site navigation (replaces tabs)</li>
       <li>home page with class group browse and visual graph</li>
       <li>menu pages with class group and individual browse</li>
   </ul>

   <p>The new default theme shipped with the application is called <strong>wilma</strong> and fully supports all 1.2 features. For details on how to create a custom theme using wilma as a starting point, please review the <a href="http://www.vivoweb.org/support/user-guide/administration" title="Download VIVO documentation" target="_blank">Site Administrator's Guide</a>. You can select your active theme on the site information page, located at <em>Site Admin > Site Information</em>.</p>

   <p><strong>To remove this notification, simply comment out the include for vivo-basic-deprecation.ftl at the top of themes/vivo-basic/templates/identity.ftl.</strong></p>
</div>