<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:acti="http://vivoweb.org/ontology/activity-insight#"	
	xmlns:aiic="http://vivoweb.org/ontology/activity-insight"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'

	exclude-result-prefixes='xs vfx'
	>

<xsl:param name='abyjFile'  required='yes'/>
<xsl:param name='unoMapFile'  required='yes'/>
<xsl:param name='aiicXmlPath' required='yes'/>
<xsl:param name='aiicPrefix' required='yes'/>
<xsl:param name='extPerIn' required='yes'/>
<xsl:param name='extPerOut' required='yes'/>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>

<xsl:include href='commonvars.xsl'/>

<xsl:variable name='alist' 
  select="document($abyjFile)//aiic:ARTICLE_INFO"/>

<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>

<xsl:variable name='extantPersons'
	select="document($extPerIn)/ExtantPersons"/>
<!-- ================================== -->
<xsl:template match='/aiic:AUTHOR_LIST'>
<rdf:RDF>
<!-- =================================== -->

<xsl:variable name='prenewps'>
<xsl:element name='ExtantPersons' inherit-namespaces='no'>
<xsl:for-each select='aiic:ARTICLES_BY_AUTHOR'>

<xsl:if test='vfx:goodName(aiic:FirstName, 
	                   aiic:MiddleName, 
                           aiic:LastName)'>

<xsl:variable name='ctr'  select='@counter'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>

<xsl:variable name='kUri' 
	select='vfx:knownUriByNetidOrName(aiic:FirstName, 
	                       		aiic:MiddleName, 
                               		aiic:LastName,
					aiic:Netid, 
                               		$extantPersons)'/>
<!-- xsl:comment><xsl:value-of select='$kUri'/></xsl:comment -->
<xsl:variable name='furi' 
select="if($kUri != '') then $kUri 
                            else concat($g_instance,$uno)"/>


<xsl:if test='$kUri = ""'>

<xsl:element name='person' inherit-namespaces='no'>
<xsl:element name='uri' inherit-namespaces='no'>
<xsl:value-of select='concat("NEW-",$furi)'/></xsl:element>
<xsl:element name='fname' inherit-namespaces='no'>
<xsl:value-of select='aiic:FirstName'/></xsl:element>
<xsl:element name='mname' inherit-namespaces='no'>
<xsl:value-of select='aiic:MiddleName'/></xsl:element>
<xsl:element name='lname' inherit-namespaces='no'>
<xsl:value-of select='aiic:LastName'/></xsl:element>
<xsl:element name='netid' inherit-namespaces='no'>
<xsl:value-of select='aiic:Netid'/></xsl:element>
</xsl:element>

</xsl:if>
</xsl:if>
</xsl:for-each>
</xsl:element>
</xsl:variable>


<xsl:variable name='newps'>
<xsl:call-template name='newPeople'>
<xsl:with-param name='knowns' select='$prenewps/ExtantPersons'/>
</xsl:call-template>
</xsl:variable>

<!-- =================================== -->


<xsl:call-template name='mkArticles'/>

<xsl:for-each select='aiic:ARTICLES_BY_AUTHOR'>
<xsl:if test='vfx:goodName(aiic:FirstName, 
	                   aiic:MiddleName, 
                           aiic:LastName)'>
<!-- create a foaf:person for this author  OR use one from before -->
<!--
<xsl:comment>Who <xsl:value-of select='concat(aiic:FirstName, 
                     		aiic:MiddleName, 
                     		aiic:LastName)'/></xsl:comment>
-->
<!-- =================================================== -->
<!-- Declare a foaf:Person (use extant person if foaf exists) -->
<xsl:variable name='known' 
select='vfx:knownPersonByNetidOrName(aiic:FirstName, 
                     		aiic:MiddleName, 
                     		aiic:LastName, 
				aiic:Netid,
                     		$extantPersons union 
                     		$prenewps/ExtantPersons)'/>
<!--
<xsl:comment>per <xsl:value-of select='$known' separator=';'/></xsl:comment>
-->
<xsl:variable name='foafuri' 
	select='if(starts-with($known/uri,"NEW-")) then 
		substring-after($known/uri,"NEW-") else 
		$known/uri'/>

<xsl:if test='starts-with($known/uri,"NEW-")'>
<xsl:if test='
not(vfx:hasIsoMatchAuthor(., 
			  preceding-sibling::aiic:ARTICLES_BY_AUTHOR))'>

<rdf:Description rdf:about='{$foafuri}'>
<rdf:type 
rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='http://xmlns.com/foaf/0.1/Person'/>

<rdfs:label>
<xsl:value-of select='concat(vfx:simple-trim($known/lname),", ",
               vfx:simple-trim($known/fname)," ", 
               vfx:simple-trim($known/mname))'/>

</rdfs:label>

<core:middleName><xsl:value-of select='$known/mname'/></core:middleName>
<core:firstName><xsl:value-of select='$known/fname'/></core:firstName>
<foaf:firstName><xsl:value-of select='$known/fname'/></foaf:firstName>
<core:lastName><xsl:value-of select='$known/lname'/></core:lastName>
<foaf:lastName><xsl:value-of select='$known/lname'/></foaf:lastName>

<xsl:if test='$known/netid != ""'>

<xsl:variable name='nidxml' 
select="concat($aiicXmlPath,'/',$aiicPrefix,$known/netid , '.xml')"/>

<xsl:if test='doc-available($nidxml)'>
<rdf:type rdf:resource=
	'http://vivoweb.org/ontology/activity-insight#ActivityInsightPerson'/>

<xsl:variable name='pci' select="document($nidxml)//dm:PCI"/>
<core:workEmail><xsl:value-of select='$pci/dm:EMAIL'/></core:workEmail>
<bibo:prefixName><xsl:value-of select='$pci/dm:PREFIX'/> </bibo:prefixName>
<core:workFax>
<xsl:value-of select='$pci/dm:FAX1'/>-
<xsl:value-of select='$pci/dm:FAX2'/>-
<xsl:value-of select='$pci/dm:FAX3'/>
</core:workFax>
<core:workPhone>
<xsl:value-of select='$pci/dm:OPHONE1'/>-
<xsl:value-of select='$pci/dm:OPHONE2'/>-
<xsl:value-of select='$pci/dm:OPHONE3'/>
</core:workPhone>
</xsl:if>
</xsl:if>
</rdf:Description>
</xsl:if>
</xsl:if>
<!-- =================================================== -->
<!-- now process the articles attributed to this author -->

<xsl:call-template name='process-author'>
<xsl:with-param name='abya' select='aiic:ARTICLE_LIST'/>
<xsl:with-param name='foafref' select="$foafuri"/>
</xsl:call-template>
</xsl:if>
</xsl:for-each>

<!-- =================================================== 
 at this point we save the new persons in the extant Persons Out xml file
-->

<xsl:call-template name='NewPeopleOut'>
<xsl:with-param name='file' select='$extPerOut'/>
<xsl:with-param name='newpeople' select='$newps'/>
</xsl:call-template>

</rdf:RDF>
</xsl:template>

<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='process-author'>
<xsl:param name='abya'/>
<xsl:param name='foafref'/>

<xsl:for-each select='$abya/aiic:ARTICLE_INFO'>
<xsl:if test='./@hasTitle = "Yes"'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rank' select='@authorRank'/>

<!-- =================================================== -->
<!-- Declare property mapping bibo:AcademicArticle to core:Authorship -->

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<core:informationResourceInAuthorship 
	rdf:resource="{concat($g_instance,$aiid,'-',$rank)}"/>
</rdf:Description>

<!-- =================================================== -->
<!-- Declare core:Authorship Individual Triples-->

<rdf:Description rdf:about="{concat($g_instance,$aiid,'-',$rank)}">

<rdfs:label>
<xsl:value-of select='vfx:simple-trim(../../aiic:AUTHOR_NAME)'/>
</rdfs:label>

<acti:authorNameAsListed>  
<xsl:value-of select='vfx:simple-trim(../../aiic:AUTHOR_NAME)'/>
</acti:authorNameAsListed> 

<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Authorship'/>

<rdf:type rdf:resource=
	'http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>

<core:linkedAuthor rdf:resource='{$foafref}'/>

<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select='$rank'/>
</core:authorRank>

<core:linkedInformationResource 
	rdf:resource="{concat($g_instance,$aiid)}"/>

</rdf:Description>

<!-- =================================================== -->
<!-- Deal with public/private issue here -->
<xsl:if test='@public != "No"'>
<rdf:Description rdf:about="{$foafref}">
<core:authorInAuthorship rdf:resource="{concat($g_instance,$aiid,'-',$rank)}"/>
</rdf:Description>
</xsl:if>

</xsl:if>
</xsl:for-each>

</xsl:template>

<!-- ================================== -->
<xsl:template match='aiic:ARTICLE_LIST'/>

<xsl:template match='aiic:ALT_SRC_ARTICLE_INFO'/>

<xsl:template match='aiic:ARTICLE_INFO'/>


<!-- =================================================== -->
<!-- =================================================== -->
<xsl:template name='mkArticles'>

<xsl:for-each select='$alist'>
<xsl:if test='./@hasTitle = "Yes" and ./@hasGoodAuthor = "Yes"'>
<xsl:variable name='aiid' select='.'/>
<xsl:variable name='rawaiid' select='substring($aiid,$pfxlen)'/>
<xsl:variable name='rid' select='./@ref_netid'/>
<!-- xsl:variable name='path' 
	select="concat('../AIIC_XMLs/AIIC_', $rid, '.xml')"/ -->
<xsl:variable name='path' 
	select="concat($aiicXmlPath,'/',$aiicPrefix, $rid, '.xml')"/>
<xsl:variable name='ijpath' 
	select="document($path)//dm:INTELLCONT_JOURNAL[@id=$rawaiid]"/>

<xsl:if test='$ijpath/dm:STATUS = "Published"'>
<xsl:call-template name='mkAcademicArticle'>
<xsl:with-param name='ijp' select="$ijpath"/>
<xsl:with-param name='aiid' select='$aiid'/>
<xsl:with-param name='rid' select='$rid'/>
</xsl:call-template>
</xsl:if>
</xsl:if>

</xsl:for-each>
</xsl:template>

<!-- ================================== -->
<xsl:template name='mkAcademicArticle'>
<xsl:param name='ijp'/>
<xsl:param name='aiid'/>
<xsl:param name='rid'/>

<rdf:Description rdf:about="{concat($g_instance,$aiid)}" >
<rdf:type rdf:resource='http://purl.org/ontology/bibo/AcademicArticle'/>
<rdfs:label>
<xsl:value-of select="$ijp/dm:TITLE"/>
</rdfs:label>
<xsl:call-template name='pages'>
<xsl:with-param name='pgnoinfo' select="$ijp/dm:PAGENUM"/>
</xsl:call-template>
<xsl:if test='$ijp/dm:DTY_PUB != ""'>
<core:year rdf:datatype=
	"http://www.w3.org/2001/XMLSchema#gYear">
	<xsl:value-of select="$ijp/dm:DTY_PUB"/></core:year>
</xsl:if>
<bibo:volume><xsl:value-of select="$ijp/dm:VOLUME"/></bibo:volume>
</rdf:Description>

</xsl:template>





<xsl:template name='pages'>
<xsl:param name='pgnoinfo'/>
<xsl:choose>
<xsl:when test='$pgnoinfo != ""'>
<xsl:analyze-string select='$pgnoinfo' regex='^\s*(\d+)\s*(-|,)?\s*(\d*)\s*$'>
<xsl:matching-substring>
<bibo:pageStart rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select="regex-group(1)"/>
</bibo:pageStart>
<xsl:if test="regex-group(3)">
<bibo:pageEnd rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>
<xsl:value-of select="regex-group(3)"/>
</bibo:pageEnd>
</xsl:if>
</xsl:matching-substring>
</xsl:analyze-string>
</xsl:when>
<xsl:otherwise>
<xsl:text></xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template name='hasIsoMatchAuthor'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:param name='res' select='false()'/>
<xsl:choose>
<xsl:when test='$nlist and not($res)'>
<!-- 
<xsl:variable name='comp' select='IsoMatch:iso($n/aiic:FirstName,
<xsl:variable name='comp' select='vfx:isoName($n/aiic:FirstName,

<xsl:variable name='comp'  xmlns:IsoMatch='java:edu.cornell.saxonext.IsoMatch'
			   select='IsoMatch:iso($n/aiic:FirstName,
-->
<xsl:variable name='comp' select='vfx:isoName($n/aiic:FirstName,
						$n/aiic:MiddleName,
						$n/aiic:LastName,
						$nlist[1]/aiic:FirstName,
						$nlist[1]/aiic:MiddleName,
						$nlist[1]/aiic:LastName)'/>
<!-- xsl:variable name='comp' select='$n = $nlist[1]'/ -->
<xsl:call-template name='hasIsoMatchAuthor'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist[position()>1]'/>
<xsl:with-param name='res' select='$res or $comp'/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select='$res'/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:function name='vfx:hasIsoMatchAuthor' as='xs:boolean'>
<xsl:param name='n'/>
<xsl:param name='nlist'/>
<xsl:call-template name='hasIsoMatchAuthor'>
<xsl:with-param name='n' select='$n'/>
<xsl:with-param name='nlist' select='$nlist'/>
</xsl:call-template>
</xsl:function>

<xsl:function name='vfx:hasAtLeastOneTitledPublicArticle' as='xs:boolean'>
<xsl:param name='list'/>

<xsl:variable name='entitled' select=
	'$list/aiic:ARTICLE_INFO[@hasTitle = "Yes" and @public = "Yes"][1]'/>
<xsl:value-of select='if($entitled) then true() else false()'/>

</xsl:function>
<!-- ================================== -->


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
