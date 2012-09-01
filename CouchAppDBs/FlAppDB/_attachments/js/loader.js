function bbd3_load(scripts, base) {
  for (var i=0; i < scripts.length; i++) {
    document.write('<script src="'+ base + scripts[i]+'"><\/script>')
  };
};

bbd3_load([
  "jquery-1.7.2.js",
  "jquery.couch.js",
  "underscore-min.js",
  "backbone-0.9.2.js",
  "backbone.couchdb.js",
  "d3.min.js",
  "d3.time.min.js",
  "d3.layout.min.js",
  "Markdown.Converter.js",
  "Markdown.Sanitizer.js",
  "backbone-d3.js", 
], "../js/lib/");

bbd3_load(["backbone-d3.couchdb.js", "footer.js"], "../js/");
	
	
	
