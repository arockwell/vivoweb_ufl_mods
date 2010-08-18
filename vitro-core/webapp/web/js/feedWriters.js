jQuery(document).ready(function(){
  //VIVO News
  jQuery("#latestVIVOFeed").empty().load(document.location.protocol+'//'+document.location.hostname+'/feed2list.jsp?url=http://vivoweb.org/blog/feed');
  //Research News
  jQuery("#latestResearchFeed").empty().load(document.location.protocol+'//'+document.location.hostname+'/feed2list.jsp?url=http://feeds.feedburner.com/UniversityOfFloridaNews');
});
