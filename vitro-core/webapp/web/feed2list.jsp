<%@ page language="java" %>
<%@ page import="java.net.URL" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="org.w3c.dom.CharacterData" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="org.w3c.dom.Node" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="java.lang.*" %>
<%
String search;
if(request.getParameter("url") == null) {
	search = "http://feeds.feedburner.com/UniversityOfFloridaNews";
} else {
	search = request.getParameter("url");
}
String desc = null;
try {
	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	URL u = new URL(search);   // feed address
	Document doc = builder.parse(u.openStream());
	String title;
	NodeList nodes = doc.getElementsByTagName("item");
	for(int i=0; (i<nodes.getLength() && i<5); i++) {
		Element element = (Element)nodes.item(i);
		out.println("            <li><a href=\""+getElementValue(element,"link")+"\">"+getElementValue(element,"title")+"</a></li>");
	}
} catch(Exception e) {
	e.printStackTrace();
}
%>
<%!
public String getElementValue(Element parent,String label) {
	return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));
}

public String getCharacterDataFromElement(Element el) {
	try {
		Node child = el.getFirstChild();
		if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
	} catch(Exception e) {
		//Ignore
	}
	return " ";
}
%>