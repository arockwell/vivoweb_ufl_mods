<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->
<#include "doctype.html">
  <#include "head.ftl">
  <body>
    <div id="wrap" class="container">
      <div id="header">
        <#include "identity.ftl">
        <#-- Note to UI team: do not change this div without also making the corresponding change in menu.jsp -->
        <div id="navAndSearch" class="block">
          <#include "menu.ftl">  
          <#include "search.ftl">                
        </div> <!-- navAndSearch --> 
        <#include "breadcrumbs.ftl">         
        <hr class="hidden" />
      </div> <!-- header -->
      <div id="contentwrap" class="withSidebar">      
        <div id="content">
          <#-- We don't do title here because some pages don't get a title, or it may not be the same as the <title> text.
          <h2>${title}</h2> -->
          ${body} 
        </div> <!-- content -->
        <#include "sidebar.ftl">
      </div> <!-- contentwrap -->
      <#include "footer.ftl">
      <#include "scripts.ftl">
    </div> <!-- wrap -->
  </body>
</html>

<#-- 
Three ways to add a stylesheet:

A. In theme directory:
${stylesheets.addFromTheme("/sample.css")}
${stylesheets.add(themeStylesheetDir + "/sample.css")}

B. Any location
${stylesheets.add("/edit/forms/css/sample.css)"}

To add a script: 

A. In theme directory:
${scripts.addFromTheme("/sample.js")}

B. Any location
${scripts("/edit/forms/js/sample.js)"}
-->
