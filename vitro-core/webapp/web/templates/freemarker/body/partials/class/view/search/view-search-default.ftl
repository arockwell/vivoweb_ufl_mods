<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

<#-- Default individual search view -->

<#import "listMacros.ftl" as l>

<a href="${individual.profileUrl}">${individual.name}</a>
<ul class="individualData">
    <@l.firstLastList>
        <li>${individual.moniker}</li>,
        <#list individual.links as link>
            <li><a class="externalLink" href="${link.url}">${link.anchor}</a></li>,            
        </#list>
    </@l.firstLastList>
</ul>
