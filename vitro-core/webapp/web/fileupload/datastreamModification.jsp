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

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%/* this is used by the FedoraDatastreamController and not by the N3 editing system.*/%>

<h2>Upload a replacement for ${fileName}</h2>
  <form action="<c:url value="/fedoraDatastreamController"/>"
        enctype="multipart/form-data" method="POST">

    <p>File <input type="file" id="fileRes" name="fileRes" /></p>
    
   <%/*  <p><input type="radio" name="useNewName" value="false" checked/>
      use existing file name</p>
    <p><input type="radio" name="useNewName" value="true"/>
      rename file to name of file being uploaded</p> */%>

    <input type="hidden" name="fileUri" value="${fileUri}"/>
    <input type="hidden" name="pid" value="${pid}"/>
    <input type="hidden" name="dsid" value="${dsid}"/>
    <!--Adding use new name set to true so that it is overwritten correctly-->
	<input type="hidden" name="useNewName" value="true"/>
    <input type="submit" id="submit" value="submit" />
  </form>
