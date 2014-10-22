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
            var element = $(this);
                        
            if(jQuery(content).is(":visible")){
            	jQuery(content).hide();
            	if(self.device == "ipad"){
            	
	            	overlayButtons.each(function(){
	            		var el = this;
	        		    var par = el.parentNode;
	        		    var next = el.nextSibling;
	        		    par.removeChild(el);
	        		    setTimeout(function() {par.insertBefore(el, next);}, 0)
	            	});
            	}
            	
            }else{
            	jQuery(".overlaycontent").hide();
                $(content).show(); 
                overlayContents.not($(content)).hide();
                
                if($(this).data('title') == "SHARE" && self.device == "desktop"){
            		jQuery(".permalink input").focus();
            		jQuery(".permalink input").select();
                }
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
};
Template.prototype.hideBookmarking = function(){
	console.log("Template.prototype.hideBookmarking()");
	jQuery("li.bookmark").hide();
}