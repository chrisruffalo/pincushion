var style = {

		'toggleActive': function(moduleNameToActivate) {
			// remove active from list items that are active and whose module name is not the one to activate
			$("li.active:has(a[href!='#" + moduleNameToActivate + "'])").removeClass('active');
			
			// add active to items that point to the named module
			$("li:has(a[href='#" + moduleNameToActivate + "'])").addClass('active');
		}

};
