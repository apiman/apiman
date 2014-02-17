
/**
 * Header data used when viewing the site statically.
 */
var OVERLORD_HEADER_DATA = {
  "username" : "eric",
  "logoutLink" : "http://www.logout.com",
  "primaryBrand" : "JBoss Overlord",
  "secondaryBrand" : "API Management",
  "tabs" : [ {
    "app-id" : "apiman",
    "href" : "/apiman-ui/",
    "label" : "APIMan",
    "active" : true
  } ]
};


/**
 * Loads a resource.  The type of resource 
 * @param filename
 * @param filetype
 */
function loadResource(filename, filetype) {
	var fileref;
	if (filetype == "js") { // if filename is a external JavaScript file
		fileref = document.createElement('script');
		fileref.setAttribute("type", "text/javascript");
		fileref.setAttribute("src", filename);
		document.getElementsByTagName("head")[0].appendChild(fileref);
	} else if (filetype == "css") { // if filename is an external CSS file
		fileref = document.createElement("link");
		fileref.setAttribute("rel", "stylesheet");
		fileref.setAttribute("type", "text/css");
		fileref.setAttribute("href", filename);
		document.getElementsByTagName("head")[0].appendChild(fileref);
	}
}

/**
 * Loads all of the resources needed by the templates so that they look
 * like they will in the deployed app.  This is done dynamically so that
 * the templates are as slim and trim as possible.  It also means that,
 * if we need to add another CSS or JS file, we can add it here rather 
 * than in every single template file.
 */
function loadAll() {
	loadResource("../../../../../../../../../webapp/bootstrap-3.1.1/css/bootstrap.min.css", "css");
	loadResource("https://rawgithub.com/Governance/overlord-commons/master/overlord-commons-uiheader/src/main/resources/META-INF/resources/css/overlord-header.css", "css");
	loadResource("../../../../../../../../../webapp/css/apiman.css", "css");
	loadResource("../../../../../../../../../webapp/css/apiman-responsive.css", "css");
	
	loadResource("../../../../../../../../../webapp/jquery-1.10.2/jquery-1.10.2.min.js", "js");
	loadResource("https://rawgithub.com/Governance/overlord-commons/master/overlord-commons-uiheader/src/main/resources/META-INF/resources/js/overlord-header.js", "js");
	setTimeout(function() {
		loadResource("../../../../../../../../../webapp/bootstrap-3.1.1/js/bootstrap.min.js", "js");
		loadResource("init.js", "js");
	}, 500);
}

loadAll();