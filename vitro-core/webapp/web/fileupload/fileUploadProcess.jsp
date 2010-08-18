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

<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.File" %>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%-- This is a jsp to handle uploading files. --%>
<%-- It was written with Datastar in mind --%>

<%-- This JSP code expects that the user is logged in, 
     that a datastar project is specified, and that the
     request is a multipart POST.      --%>
     
<%-- Since this is for Datastar we need the URI of the user
     who is uploading this file, the URI of the project it is
     associated with and a project spcific directory to save 
     the file to. %>     
     
<%! static final int MAX_IN_MEM_SIZE = 1024*1024 * 50 ; //50 Mbyte file limit %>


<%-- http://vivo.library.cornell.edu/ns/0.1#coordinatorOf --%>
<%-- http://vivo.library.cornell.edu/ns/0.1#resourceAuthor --%>

<%! static final int MAX_IN_MEM_SIZE = 1024*1024 * 50; %>

<%
if( ! ServletFileUpload.isMultipartContent(request) ){
    //return an error message0.
    out.write("<html><p>you need to submit a file.</p></html>");
    return;
}

//Create a factory for disk-based file items
DiskFileItemFactory factory = new DiskFileItemFactory();
factory.setSizeThreshold(MAX_IN_MEM_SIZE);

// Create a new file upload handler
ServletFileUpload upload = new ServletFileUpload(factory);
upload.setSizeMax(MAX_IN_MEM_SIZE);

// Parse the request
List /* FileItem */ items = upload.parseRequest(request);

String user = getUserUri(request);
String project = getProjectUri(request);
String dir = getDirectory(user,project,request);

if( ! canWriteDirectory( dir )){
    out.write( doDirectoryWriteError( dir ) );
    return;
}

Iterator iter = items.iterator();
while( iter.hasNext()){
    
    FileItem item = (FileItem)iter.next();
    if( item.isFormField()){              
        out.write("<p>");       
        out.write("fieldname: " + item.getFieldName() + " value: " + item.getString() );
        out.write("</p>");        
    }else{
        out.write("<p>");        
        out.write("form field name: " + item.getFieldName()
                +" filename: " + item.getName()
                +" type: " + item.getContentType() 
                +" isInMem: " + item.isInMemory() );               
        out.write("</p>");
        
        String fileLocation = dir + '/' + item.getName();
        if( fileWithNameAlreadyExists( fileLocation ) ){
            doFileExistsError( fileLocation );
            return; 
        }
        
        File uploadedFile = new File( fileLocation );
        item.write( uploadedFile );
    }
}
%>

<%!
public String getDirectory(String userUri, String projectUri, HttpServletRequest request){
    //this should be stored in the model as a dataproperty on the project
    return "/usr/local/datastar/data";
}

public String getUserUri( HttpServletRequest request ){
    return "http://vivo.library.cornell.edu/ns/0.1#individual762"; //Medha Devare
}

public String getProjectUri( HttpServletRequest request ){
    return "http://vivo.library.cornell.edu/ns/0.1#individual1"; //Mann Lib.
}
%>

<%!
public boolean canWriteDirectory( String dir ){ 
    File df = new File(dir);
    //attempt to create directory if not found
    if( ! df.exists() ){
        if( !df.mkdir()  )
            return false;
    }    
    return df.canWrite();    
}   

public String doDirectoryWriteError( String dir ) {
    return "The system could not save your file, contact you system"
    +" administrators and inform them that the directory "  
    + dir + " is not writeable";        
} 

public boolean fileWithNameAlreadyExists( String fileLocation ){
    File f = new File(fileLocation);
    return f.exists();
}

public String doFileExistsError( String file ) {
    return "There is already a file named " + file + " on the system. Please rename your file " ;    
}
%>