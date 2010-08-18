<%--
Copyright (c) 2010, Cornell University
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
--%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:set var="portalBean" value="${requestScope.portalBean}" />
<c:set var="themeDir"><c:out value="${portalBean.themeDir}" /></c:set>
<c:set var="contextPath"><c:out value="${pageContext.request.contextPath}" /></c:set>

<c:url var="egoCoAuthorshipDataFeederURL" value="/admin/visQuery">
	<c:param name="vis" value="coauthorship" />
	<c:param name="render_mode" value="data" />
	<c:param name="uri" value="${requestScope.egoURIParam}" />
	<c:param name="labelField" value="label" />
</c:url>

<c:url var="egoCoAuthorsListDataFileURL" value="/admin/visQuery">
	<c:param name="vis" value="person_level" />
	<c:param name="render_mode" value="data" />
	<c:param name="vis_mode" value="coauthors" />
	<c:param name="uri" value="${requestScope.egoURIParam}" />
</c:url>

<c:url var="swfLink" value="/${themeDir}site_icons/visualization/coauthorship/CoAuthor.swf" />

<c:url var="jquery" value="/js/jquery.js"/>
<c:url var="adobeFlashDetector" value="/js/visualization/coauthorship/AC_OETags.js" />
<c:url var="googleVisualizationAPI" value="http://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22visualization%22%2C%22version%22%3A%221%22%2C%22packages%22%3A%5B%22areachart%22%2C%22imagesparkline%22%5D%7D%5D%7D"/>
<c:url var="coAuthorShipJavaScript" value="/js/visualization/personlevel/person_level.js" />

<script type="text/javascript" src="${adobeFlashDetector}"></script>
<script type="text/javascript" src="${jquery}"></script>
<script type="text/javascript" src="${googleVisualizationAPI}"></script>

<script language="JavaScript" type="text/javascript">
<!--
// -----------------------------------------------------------------------------
// Globals
// Major version of Flash required
var requiredMajorVersion = 10;
// Minor version of Flash required
var requiredMinorVersion = 0;
// Minor version of Flash required
var requiredRevision = 0;
// -----------------------------------------------------------------------------


var swfLink = "${swfLink}";
var egoURI = "${requestScope.egoURIParam}";
var egoCoAuthorshipDataFeederURL = "${egoCoAuthorshipDataFeederURL}";
var egoCoAuthorsListDataFileURL = "${egoCoAuthorsListDataFileURL}";
var contextPath = "${contextPath}";

// -->
</script>
<script type="text/javascript" src="${coAuthorShipJavaScript}"></script>


<c:url var="coAuthorStyle" value="/${themeDir}css/visualization/personlevel/coauthor_style.css" />
<c:url var="pageStyle" value="/${themeDir}css/visualization/personlevel/page.css" />
<c:url var="vizStyle" value="/${themeDir}css/visualization/visualization.css" />

<link href="${coAuthorStyle}" rel="stylesheet" type="text/css" />
<link href="${pageStyle}" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" href="${vizStyle}" />