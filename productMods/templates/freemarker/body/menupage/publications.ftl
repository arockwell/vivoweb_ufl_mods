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

<p>This body is from the the template file 
   vivo/productMods/templates/freemarker/body/menupage/publications.ftl.  
   In the display model, the publications page has a display:requiresBodyTemplate
   property that defines that the publications page overrides the default template. 
   The default template for these pages is at /vitro/webapp/web/templates/freemarker/body/menupage/menupage.ftl  </p>
   
<p> This technique could be used to define pages without menu items, that get 
    their content from a freemarker template.  An example would be the about page.</p>
    
<code>
display:About <br>
    a display:Page ; <br>
    display:requiresBodyTemplate "about.ftl" ; <br>
    display:title "About" ;<br>
    display:urlMapping "/about" .<br>
    <br>
</code>    

<p>This would create a page that would use about.ftl as the body.  The page would be 
accessed via /about and would override all servlet mappings in web.xml</p>

    