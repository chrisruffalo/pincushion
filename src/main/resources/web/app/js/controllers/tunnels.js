multiTunnelApp.controller('TunnelTableController', function ($scope, $resource, $timeout, Tunnel) {
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
	
	// pause refreshing
	$scope.pauseRefresh = function() {
		// cancel previous timer if it exists
		if($scope.updateTimer) {
			if($scope.updateTimer.cancel) {
				$scope.updateTimer.cancel();
			}
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