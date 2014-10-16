var Template = function () {
	console.log("Template()");
    Page.apply(this, arguments);
    
    this.element = jQuery('#template'); 
    
    this.device = "desktop";
    
    this.overlayButtons = jQuery('button[data-overlay]');
    this.overlayContents = jQuery('.overlaycontent');
    
    var overlayButtons = this.overlayButtons;
	var overlayContents = this.overlayContents;
	
	var self = this;
	
	overlayButtons.each(function(){
		var $this = jQuery(this);
        var content = $this.attr("data-overlay");
        $this.click(function(e){
            e.preventDefault();
            var element = this;
            if($(content).is(":visible")) { 
                $(content).hide(); $(element).removeClass('active');
            } else { 
            	jQuery(".overlaycontent").hide();
                $(content).show(); $(element).addClass('active');
                overlayButtons.not(element).removeClass('active');
                overlayContents.not($(content)).hide();
                
                if($(this).data('title') == "SHARE" && self.device == "desktop"){
                	jQuery(".permalink input").focus();
                }
                jQuery(".permalink input")[0].setSelectionRange(0, 9999);
            }
            
        });
	});
};

Template.prototype = Object.create(Page.prototype);
Template.prototype.setDevice = function(data){
	console.log("Template.setDevice(" + data)
	var data = JSON.parse(data);
	
	this.device = data.device;
};
Template.prototype.createTooltips = function(){
	console.log("createTooltips()");
	this.element.find('[data-toggle]').tooltip();
}