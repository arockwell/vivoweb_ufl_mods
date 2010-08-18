<?xml version="1.0"?>
<xsl:stylesheet version='2.0'
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs='http://www.w3.org/2001/XMLSchema'
	xmlns:core="http://vivoweb.org/ontology/core#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:bibo="http://purl.org/ontology/bibo/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:ai="http://vivoweb.org/activity-insight#"
	xmlns:aiic="http://vivoweb.org/activity-insight"
        xmlns="http://vivoweb.org/activity-insight#"
	xmlns:dm="http://www.digitalmeasures.com/schema/data"
	xmlns:vfx='http://vivoweb.org/ext/functions'	
	exclude-result-prefixes='vfx xs'
	>

<xsl:output method='xml' indent='yes'/>
<xsl:strip-space elements="*"/>
<xsl:variable name='NL'>
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:param name='unoMapFile'  required='yes'/>

<!-- 
for each intellcont in a promulgator make one of these
<rdf:Description about='http://vivoweb.org/individual/AI-10580942849'>
	<core:hasPublicationVenue rdf:resource='http://vivoweb.org/individual/AJ-AC41F'/>
</rdf:Description>

and one of these ...

<rdf:Description about='http://vivoweb.org/individual/AJ-AC41F'>
	<core:publicationVenueFor rdf:resource='http://vivoweb.org/individual/AI-10580942849'/>
</rdf:Description>

for each promulgator (e.g. book) make one of these ...

<rdf:Description about='http://vivoweb.org/individual/AJ-AC41F'>
	<rdf:type rdf:resource='http://purl.org/ontology/bibo/Book'>
	<rdfs:label>Journal of Fun and Games</rdfs:label>
	<core:title>Journal of Fun and Games</core:title>
</rdf:Description>


-->
<xsl:variable name='unomap'
	select="document($unoMapFile)/Mapping"/>


<xsl:template match='/aiic:INTELLCONT_PROMULGATOR_LIST'>
<rdf:RDF>

<xsl:for-each select='aiic:INTELLCONTS_BY_PROMULGATOR'>
<!-- create a promulgator -->
<xsl:variable name='ctr'  select='position()'/>
<xsl:variable name='ontoClassProm'  select='./@ontoClassProm'/>
<xsl:variable name='uno' select='$unomap/map[position()=$ctr]/@nuno'/>
<xsl:variable name='puri' select="concat('http://vivoweb.org/individual/',$uno)"/>


<xsl:if test='vfx:trim(aiic:INTELLCONT_PROMULGATOR_NAME) != "nil" and $ontoClassProm != "nil" '>

<rdf:Description rdf:about="{$puri}">
<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>
<rdf:type rdf:resource='{vfx:expandClass($ontoClassProm)}'/>
<rdfs:label>
<xsl:value-of select='vfx:trim(aiic:INTELLCONT_PROMULGATOR_NAME)'/>
</rdfs:label>
<core:title><xsl:value-of select='vfx:trim(aiic:INTELLCONT_PROMULGATOR_NAME)'/></core:title>
</rdf:Description>

<xsl:for-each select='aiic:INTELLCONT_LIST/aiic:INTELLCONT_INFO'>

<rdf:Description rdf:about='{$puri}'>
<core:publicationVenueFor rdf:resource='{concat("http://vivoweb.org/individual/",.)}'/>
</rdf:Description>

<rdf:Description rdf:about='{concat("http://vivoweb.org/individual/",.)}'>
<core:hasPublicationVenue rdf:resource='{$puri}'/>
</rdf:Description>

</xsl:for-each>
</xsl:if>

</xsl:for-each>
</rdf:RDF>

</xsl:template>

<xsl:template match='//aiic:INTELLCONTS_BY_PROMULGATOR'/>
<xsl:template match='//aiic:INTELLCONT_PROMULGATOR_NAME'/>
<xsl:template match='//aiic:INTELLCONT_PROMULGATOR_ILK'/>
<xsl:template match='//aiic:INTELLCONT_INFO'/>
<xsl:template match='//aiic:INTELLCONT_LIST'/>


<xsl:include href='vivofuncs.xsl'/>

</xsl:stylesheet>
