multiTunnelApp.controller('TunnelTableController', function ($scope, $resource, $timeout, $location, Tunnel) {
	// data
	$scope.tunnels = [];
	
	// what to do when the route has changed
	$scope.$on('$routeChangeSuccess', function () {
		// update styles
		style.toggleActive('tunnels');
		  
		// initial load of table elements
		$scope.updateTable();
	});
	
	// what to do when leaving the page
	$scope.$on('$locationChangeStart', function () {
		// stop table update
		$scope.pauseRefresh();
	});
	
	$scope.edit = function(id) {
		// pause update
		$scope.pauseRefresh();
		
		// change location
		$location.path('edit-tunnel/' + id);
	}
	
	$scope.remove = function(tunnel) {
		if(!tunnel.$remove) {
			tunnel = new Tunnel(tunnel);
		}
		
		// pause update
		$scope.pauseRefresh();
		
		// update table after remove 
		tunnel.$remove($scope.updateTable);
	}

	$scope.pause = function(tunnel) {
		if(!tunnel.$pause) {
			tunnel = new Tunnel(tunnel);
		}
		
		// pause update
		$scope.pauseRefresh();
		
		// update table after remove 
		tunnel.$pause($scope.updateTable);
	}
	
	$scope.resume = function(tunnel) {
		if(!tunnel.$resume) {
			tunnel = new Tunnel(tunnel);
		}
		
		// pause update
		$scope.pauseRefresh();
		
		// update table after remove 
		tunnel.$resume($scope.updateTable);
	}
	
	// pause refreshing
	$scope.pauseRefresh = function() {
		// cancel (and nullify) previous timer if it exists
		if($scope.updateTimer) {
			$timeout.cancel($scope.updateTimer);
			$scope.updateTimer = null;
		}
	}
	
	// start refreshing the table
	$scope.startRefresh = function() {
		// pause refresh
		$scope.pauseRefresh();
		
	    // create update timeout
	    $scope.updateTimer = $timeout(
	    	function(){
	    		$scope.updateTable();
	    	}
	    	, 2000 // 2 seconds
	    )
	}
	
	// load values to the table
	$scope.updateTable = function() {
				
		// don't double-refresh
		$scope.pauseRefresh();
		
		(function tick(){
			// use service to query for all resources
			var newTunnels = Tunnel.query(function() {
				$scope.tunnels = newTunnels;
			    $scope.startRefresh();
			});	
		})();
	};
	

});