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

<%@ taglib prefix="form" uri="http://vitro.mannlib.cornell.edu/edit/tags" %>
	<tr class="editformcell">
		<td valign="bottom" colspan="2">
			<b>Tab Title*</b> <i>(a short phrase to appear on headings, menus or pick lists)</i><br/>
				<input type="text" name="Title" value="<form:value name="Title"/>" style="width:60%;" maxlength="80" />
				<font size="2" color="red"><form:error name="Title"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Tab Shorthand</b> <i>(an alternate phrase for display as <strong>More ...</strong> in the home portal)</i><br/>
				<input type="text" name="ShortHand" value="<form:value name="MoreTag"/>" style="width:80%;" maxlength="80" />
				<font size="2" color="red"><form:error name="MoreTag"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Tab Description</b> <i>A single short sentence to summarize the contents for users</i><br/>
			<strong>Note that this now becomes the tool tip when the mouse rolls over a primary or secondary tab name</strong><br/>
				<input type="text" name="Description" value="<form:value name="Description"/>" style="width:90%;" maxlength="255" />
				<font size="2" color="red"><form:error name="Description"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>Tab Body</b> <i>enter any longer text or comment here</i><br/>
				<textarea name="Body" rows="10" wrap="soft"><form:value name="Rows"/><form:value name="Body"/></textarea>
				<font size="2" color="red"><form:error name="Body"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="top" colspan="1">
		<% /*
			<b>Originator</b><br>
				<input type="text" disabled="disabled" name="UserId" value="<form:value name="UserId"/>"/>
				<font size="2" color="red"><form:error name="UserId"/></font>
				*/ %>
		</td>
		<td valign="bottom" colspan="1">
			<b>Tab Type</b><br>
				<select name="TabtypeId" >
					<form:option name="TabtypeId"/>
				</select>
				<font size="2" color="red"><form:error name="TabtypeId"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Individual link method</b><br /><i>(if method is "by tab-type relationships", select types after editing this tab itself)</i><br />
				<select name="EntityLinkMethod" >
					<form:option name="EntityLinkMethod"/>
				</select>
				<font size="2" color="red"><form:error name="EntityLinkMethod"/></font>
		</td>	</tr>
	<tr class="editformcell"><td colspan="3"><hr /></td></tr>
	<% /*
	<tr class="editformcell">
		<td valign="bottom" colspan="3">
			<b>URL of RSS feed</b> (must include the full http://... path)<br/>
					<input type="text" name="RssURL" value="<form:value name="RssURL"/>" style="width:60%;" maxlength="255" />
				<font size="2" color="red"><form:error name="RssURL"/></font>
		</td>
	</tr>
	
	<tr class="editformcell">
		<td valign="top" colspan="3">
		
			<b>Portal</b> <i>(like-named tabs in multiple portals are created and modified separately)</i><br />
				<input type="text" name="PortalId" value="<form:value name="PortalId"/>">
				<br><font size="2" color="red"><form:error name="PortalId"/></font>
		</td>

	</tr>

	*/ %>
	
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Order for display within parent tab</b><br />
				<input type="text" name="DisplayRank" value="<form:value name="DisplayRank"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="DisplayRank"/></font>
		</td>
		<td valign="bottom" colspan="2">
			<b>Optional time limit for entities</b> <i>in days; use negative values for the past; not active for manual linking or image galleries</i><br/>
			<i>positive values will key off entity <b>timekey</b> field; negative values off entity <b>sunrise</b> field</i><br/>
				<input type="text" name="DayLimit" value="<form:value name="DayLimit"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="DayLimit"/></font>
		</td>
	</tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Sort field for related entities</b><br /><i><strong>name</strong>,rand(),timekey,sunset,sunrise</i><br/>
				<input type="text" name="EntitySortField" value="<form:value name="EntitySortField"/>" size="10" maxlength="50" />
				<font size="2" color="red"><form:error name="EntitySortField"/></font>
		</td>
		<td valign="bottom" colspan="2">
			<b>Sort direction for related entities</b><br /><i>blank for ascending,"desc" for descending</i><br/>
				<input type="text" name="EntitySortDirection" value="<form:value name="EntitySortDirection"/>" size="4" maxlength="4" />
				<font size="2" color="red"><form:error name="EntitySortDirection"/></font>
		</td>
	</tr>
	<tr class="editformcell"><td colspan="3"><hr /></td></tr>
	<tr class="editformcell"><td colspan="3">The following fields apply only when images are involved</td></tr>
	<tr class="editformcell">
		<td valign="bottom" colspan="1">
			<b>Columns of images to display</b><br/>
			<i>9 columns of images at 94 pixels wide fits a 1024x768 display window</i><br/>
				<input type="text" name="GalleryCols" value="<form:value name="GalleryCols"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="GalleryCols"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Rows of images to display</b><br/>
			<i>if only a single row is specified, the A|B|C...|Z choices don't appear with image galleries</i><br/>
				<input type="text" name="GalleryRows" value="<form:value name="GalleryRows"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="GalleryRows"/></font>
		</td>
		<td valign="bottom" colspan="1">
			<b>Width for image display</i><br/>
				<input type="text" name="ImageWidth" value="<form:value name="ImageWidth"/>" size="5" maxlength="11" />
				<font size="2" color="red"><form:error name="ImageWidth"/></font>
		</td>
	</tr>
	<tr class="editformcell">
	    <td valign="bottom" colspan="1">
	      <b>Flag 2 Mode</b><em> to support legacy applications - ignore</em><br/>
	      <input type="text" name="Flag2Mode" value="<form:value name="Flag2Mode"/>" size="10"/>  
	    </td>
	    <td valign="bottom" colspan="2">
	      <b>Flag 2 Set</b><br/>
	      <input type="text" name="Flag2Set" value="<form:value name="Flag2Set"/>" size="50"/>  
	    </td>
	</tr>
