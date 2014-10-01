var Episodetitle = function(options){
	Component.apply(this, arguments);
	
	this.element = jQuery('#episodetitle');
	this.template = this.element.find('#episode-title-template').text();
	this.wrapper = this.element.find('.title-wrapper');
};
Episodetitle.prototype = Object.create(Component.prototype);
Episodetitle.prototype.setTitle = function(message){
	console.log("Episodetitle.setTitle(" + message + ")");
	var data = JSON.parse(message);
	var filledTemplate = _.template(this.template, {item: data});
	this.wrapper.html(filledTemplate);
}