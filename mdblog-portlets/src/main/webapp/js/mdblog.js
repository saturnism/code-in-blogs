(function($, ui){
	$("document").ready(function(){
		$("a.mdblogAddSubpageLink").each(function(){
			var id = $(this).attr("data");
			$("#" + id).dialog({
				autoOpen: false,
				title: "Add Subpage",
				height: 300,
				width: 350,
				modal: true,
				buttons: {
					"Add" : function() {
						console.log("add");
						$(this).closest(".ui-dialog").find("form").submit();
					},
					"Cancel" : function() {
						$( this ).dialog( "close" );
					}
				},
				close: function() {
			        // nothing
			    }

			});
		});
		
	    $("a.mdblogAddSubpageLink").click(function(e) {
	    	var id = $(this).attr("data");
	    	$("#" + id).dialog("open");
	    	return false;
	    });
	    
	    console.log("loaded");
	});
})($, ui);