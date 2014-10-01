var Metadata = function(options){
	var self = this;
	console.log("Metadata()");
	Component.apply(this, arguments);
	
	this.element = jQuery("#metadata");
	console.log(jQuery("#metadata-template").text());
	this.template = _.template(jQuery('#metadata-template').text());
	
	
};
Metadata.prototype = Object.create(Component.prototype);
Metadata.prototype.setMetadata = function(message){
	var data = JSON.parse(message);
	
	console.log(data);
	
	this.element.append(this.template({metadata: data}));
	var showExtraMetadata = this.element.find('#show-extra-metadata');
	var hideExtraMetadata = this.element.find('#hide-extra-metadata');
	
	showExtraMetadata.on('click', function(){
		console.log("SHOW MORE BUTTON CLICKED!");
		jQuery(this).hide();
		hideExtraMetadata.show();
	});
	
	hideExtraMetadata.on('click', function(){
		console.log("HIDE MORE BUTTON CLICKED!");
		jQuery(this).hide();
		showExtraMetadata.show();
	});
}