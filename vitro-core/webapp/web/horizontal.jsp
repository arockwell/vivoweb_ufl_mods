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

<%@ page import="java.util.*, java.lang.String.*"%>
<%@ page import="edu.cornell.mannlib.vedit.beans.ButtonForm" %>

<%
if (request.getAttribute("title") != null) { %>
    <h2><%=request.getAttribute("title")%></h2><%
}
%>

<table style="margin-bottom:1.5ex;">
<tr>
  <td style="width:0;padding:0;margin:0;"/>


<%if (request.getAttribute("horizontalJspAddButtonUrl") != null) {%>
  <td>
    <form action="<%=request.getAttribute("horizontalJspAddButtonUrl")%>" method="get"><input type="submit" class="form-button" value="<%=request.getAttribute("horizontalJspAddButtonText")%>"/>
<%  if (request.getAttribute("horizontalJspAddButtonControllerParam") != null) {%>
        <input type="hidden" name="controller" value="<%=request.getAttribute("horizontalJspAddButtonControllerParam")%>"/>
<%  }
    if (request.getAttribute("home") != null) {%>
        <input type="hidden" name="home" value="<%=request.getAttribute("home")%>"/>
<%  }%>
    </form>
  </td>
<%
}
List <ButtonForm> topButtons = (List)request.getAttribute("topButtons");
if (topButtons!=null) {
    Iterator iter = topButtons.iterator();
    while (iter.hasNext()){
       ButtonForm b = (ButtonForm)iter.next();%>
       <td>
       <form <%=b.getCssClass()%> action="<%=b.getAction()%>" method="get">
<%         HashMap<String,String> params=b.getParams();
           if (params!=null) {
               for (String key : b.getParams().keySet()) {%>
                   <input type="hidden" name="<%=key%>" value="<%=params.get(key)%>"/>
<%             }
           }%>
           <input type="submit" class="form-button" value="<%=b.getLabel()%>"/>
       </form>
       </td>
<%  }
}%>

</tr></table>

<div class="editingForm">

<jsp:useBean id="results" class="java.util.ArrayList" scope="request" />

<% int columns = 0;
    boolean havePostQueryData = false;

    String editFormStr = (String)request.getAttribute("editform");
    String minEditRoleStr = (String)request.getAttribute("min_edit_role");

    String firstValue = "null", secondValue = "null";
    Integer columnCount = (Integer)request.getAttribute("columncount");
    columns = columnCount.intValue();

    String clickSortStr = (String)request.getAttribute("clicksort");

    if ( columns > 0 && results.size() > 0) {    // avoid divide by zero error in next statement
        /* start enclosing table cell that holds all results */
%>

<%      String suppressStr = null;
        boolean isPostQHeaderRow = false;

        if ( ( suppressStr = (String)request.getAttribute("suppressquery")) == null ) { // only inserted into request if true
%>
<i><b><%=(results.size() - columns) / columns %></b> rows of results were retrieved in <b><%= columns %></b> columns for query "<%=request.getAttribute("querystring")%>".</i>
<br/>
<%      }
        Iterator iter = results.iterator();
        int resultCount = 0, primaryRowNumber=0, pageRowNumber=0;
        while (iter.hasNext()) {
            String classString;

            String thisResult = (String)iter.next();
            if ( "+".equals(thisResult) ) {
                havePostQueryData = true;
                classString = "database_postheader";
                isPostQHeaderRow = true;
                thisResult = "&nbsp;";
            } else if ( thisResult != null && thisResult.indexOf("@@")== 0) {
                classString=thisResult.substring(2);
                thisResult ="&nbsp;"; //leave as follows for diagnostics: thisResult.substring(2);
                isPostQHeaderRow = false;
            } else {
                classString = isPostQHeaderRow ? "database_postheader" : "row";
                if ( thisResult == null || "".equals(thisResult) )
                    thisResult = "&nbsp;";
            }
                if ( resultCount == 0 ) { // first pass : column names
%>

<table border="0" cellpadding="2" cellspacing="0" width = "100%">
                        <%              if ( clickSortStr != null && "true".equals(clickSortStr) ) {
                    if ( (results.size() - columns) / columns > 2 ) {
%>
<tr>
<td class="database_upperleftcorner" colspan="<%=columns%>">
<i>Click on the column header to sort rows by that column.</i>
</td>
</tr>
<%                  }
                }
%>

<tr>
    <td class="rownumheader">#</td>
     <%             if ( !("XX".equals(thisResult) )) {
%>
    <td class="database_header">
<%              }
                } else if ( resultCount == columns ) {  // end column names and start numbered list
                ++ primaryRowNumber;
                ++ pageRowNumber;
                firstValue = thisResult;
%>

</tr>
<tr valign="top" class="row">
    <td class="rownum">1</td>
      <%                if ( !("XX".equals(thisResult) )) { %>
    <td class="row">
<%              }
            } else if ( resultCount % columns == 0 ) {  // end row and start next row with calculated row number
                ++ pageRowNumber;
%>
</tr>
<!-- <tr valign="top" class="<%=classString%>" > -->
      <%                if ( "row".equals(classString) ) {
                    if ( havePostQueryData ) {
%>
<tr><td>&nbsp;</td></tr>
<%                      havePostQueryData = false;
                    }
                    ++ primaryRowNumber;
%>
<tr valign="top" class="row">
    <td class="rownum"><%= primaryRowNumber /*resultCount / columns*/%></td>
<%              } else { // classString does not equal "row"
%>
<tr valign="top" class="<%=classString%>" >
<%              }
                if ( !("XX".equals(thisResult) )) {
%>
    <td class="<%=classString%>">
<%              }
            } else { // not the end of a row
                if ( resultCount <= columns ) { // header rows
                    if ( !("XX".equals(thisResult) )) {
%>
    <td class="database_header">
<%
                    }
                } else if ( resultCount == columns + 1 ) {
                    secondValue=thisResult;
                    if ( !( "XX".equals(thisResult) )) {
%>
    <td class="row">
<%
                    }
                } else  { // cells in later rows
                    if ( !( "XX".equals(thisResult) )) {
                        if ( "row".equals(classString) ) {
                            if ( primaryRowNumber % 2 == 0 ) {
                                if ( pageRowNumber % 2 == 0 ) {
%>
    <td class="rowalternate">
<%                              } else {
%>
    <td class="row">
<%                              }
                            } else if ( pageRowNumber % 2 == 0 ) {
%>
    <td class="rowalternate">
<%                          } else {
%>
    <td class="row">
<%                          }
                        } else {
%>
    <td class="<%=classString%>" >
<%                      }
                    }
                }
            }
                if ( !( "XX".equals(thisResult) )) {
%>
                    <%= thisResult %>
    </td>
<%
            }
            ++ resultCount;
        }
%>
  </tr>
</table>
<%
    } else { /* results not > 0 */
        Iterator errorIter = results.iterator();
        while ( errorIter.hasNext()) {
            String errorResult = (String)errorIter.next();
%>
            <p>Error returned: <%= errorResult%></p>
<%
        }
    }
%>
<%
if ( editFormStr != null  && minEditRoleStr != null ) {
    String loginStatus =(String)session.getAttribute("loginStatus");
    if ( loginStatus != null &&  "authenticated".equals(loginStatus) ) {
        String currentRemoteAddrStr = request.getRemoteAddr();
        String storedRemoteAddr = (String)session.getAttribute("loginRemoteAddr");
        if ( storedRemoteAddr != null && currentRemoteAddrStr.equals( storedRemoteAddr ) ) {
            int minEditRole = Integer.parseInt(  minEditRoleStr );
            String authorizedRoleStr = (String)session.getAttribute("loginRole");
            if ( authorizedRoleStr != null ) {
                int authorizedRole = Integer.parseInt( authorizedRoleStr );
                if ( authorizedRole >= minEditRole ) { %>
                    <jsp:include page="<%=editFormStr%>" flush="true">
                    <jsp:param name="firstvalue" value="<%=firstValue%>" />
                    <jsp:param name="secondvalue" value="<%=secondValue%>" />
                    </jsp:include>
<%                      } else { %>

<%                      }
            } else {    %>

<%                  }
        } else { %>

<%              }
    } else { %>

<% }
} else { %>

<%
} %>

</div>
