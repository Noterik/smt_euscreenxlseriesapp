var Template = function () {
	console.log("Template()");
    Page.apply(this, arguments);
    
    this.element = jQuery('#template'); 
    
    this.device = "desktop";
    
    this.overlayButtons = jQuery('button[data-overlay]');
    this.overlayContents = jQuery('.overlaycontent');
    
    var overlayButtons = this.overlayButtons;
	var overlayContents = this.overlayContents;
	
	this.sortDescButton = this.element.find('a[data-title="SORT DESCENDING"]');
	this.sortAscButton = this.element.find('a[data-title="SORT ASCENDING"]');
	
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
	
	this.sortDescButton.click(function(e){
		e.preventDefault();
		console.log("sortDescButton::CLICK");
		self.sortDescButton.addClass('hide');
		self.sortAscButton.removeClass('hide');
		eddie.putLou("", "changeSorting(up)");
	});
	
	this.sortAscButton.click(function(e){
		e.preventDefault();
		console.log("sortAscButton::CLICK");
		self.sortAscButton.addClass('hide');
		self.sortDescButton.removeClass('hide');
		eddie.putLou("", "changeSorting(down)");
	})
	
	
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