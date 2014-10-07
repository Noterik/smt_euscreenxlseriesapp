var Template = function () {
	console.log("Template()");
    Page.apply(this, arguments);
    
    this.element = jQuery('#template');   
};

Template.prototype = Object.create(Page.prototype);
Template.prototype.createTooltips = function(){
	console.log("createTooltips()");
	this.element.find('[data-toggle]').tooltip();
	
	jQuery('button[data-overlay]').popupOverlayJS({
		$overlayContents : jQuery('.overlaycontent'),
		contentOverlayIdAttr : 'data-overlay'
	});
}