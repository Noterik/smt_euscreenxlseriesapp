var Episodelist = function(options){
	Component.apply(this, arguments);
	var self = this;
	
	this.element = jQuery("#episodelist");
	this.listElement = this.element.find('.episode-list');
	this.chunkTemplate = this.element.find('#chunk-template').text();
	this.moreButton = this.element.find('.more-button');
	this.currentActiveId = null;
	this.$window = jQuery(window);
	this.mode = "large";
	
	this.moreButton.on('click', function(event){
		event.preventDefault();
		self.getNextChunk();
	});
	
	this.$window.resize(function(){
		self.doStuffForSize.apply(self);
	});
	
	self.doStuffForSize();
};
Episodelist.prototype = Object.create(Component.prototype);
Episodelist.prototype.handleChunk = function(message){
	var data = JSON.parse(message);
	
	var template = _.template(this.chunkTemplate, {items: data.items});
	if(data.clear){
		this.listElement.html(template);
	}else{
		this.listElement.append(template);
	}
	
	
	if(this.currentActiveId){
		this.listElement.find('.media-item[data-id="' + this.currentActiveId + '"]').addClass('active');
	}
	
	this.listElement.find('.media-item').off('click').on('click', function(){
		var activeItem = jQuery(this).data('id');
		var paramsObject = {'id': activeItem};
		eddie.putLou('', 'setActiveItem(' + JSON.stringify(paramsObject) + ')');
	});
};
Episodelist.prototype.setActiveItem = function(message){
	var identifier = JSON.parse(message).id;
	this.currentActiveId = identifier;
	this.listElement.find('.media-item.active').removeClass('active');
	this.listElement.find('.media-item[data-id="' + identifier + '"]').addClass('active');
};
Episodelist.prototype.getNextChunk = function(chunkNo, size){
	eddie.putLou('', 'getNextChunk()');
};
Episodelist.prototype.hideShowMore = function(){
	this.moreButton.hide();
};
Episodelist.prototype.requestAll = function(){
	eddie.putLou('', 'requestAll()');
};
Episodelist.prototype.doStuffForSize = function(){
	if(this.$window.width() < 992 && this.mode != "small"){
		this.mode = "small";
		this.requestAll();
	}
};
Episodelist.prototype.setDevice = function(data){
	data = JSON.parse(data);
};