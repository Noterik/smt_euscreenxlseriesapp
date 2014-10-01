var Itempagelink = function(options){
	Component.apply(this, arguments);
	this.element = jQuery("#itempagelink");
};

Itempagelink.prototype = Object.create(Component.prototype);
Itempagelink.prototype.setIdentifier = function(message){
	console.log("Itempagelink.setIdentifier(" + message + ")");
	var data = JSON.parse(message);
	
	this.element.find('a').attr('href', '/item.html?id=' + data.id);
	jQuery(".episode-link.visible-xs a").attr('href', '/item.html?id=' + data.id);
}