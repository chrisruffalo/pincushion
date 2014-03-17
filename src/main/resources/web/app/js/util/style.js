var style = {

		'toggleActive': function(moduleNameToActivate) {
			// remove active from list items that are active and whose module name is not the one to activate
			$("li.active:has(a[href!='#" + moduleNameToActivate + "'])").removeClass('active');
			
			// add active to items that point to the named module
			$("li:has(a[href='#" + moduleNameToActivate + "'])").addClass('active');
		},
		'prettyBytes': function bytesToSize(bytes) {
			var sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
			if (bytes == 0) return '0 B';
			var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
			return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
		}

};
