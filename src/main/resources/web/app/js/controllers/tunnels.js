multiTunnelApp.controller('TunnelTableController', function ($scope, $resource, $timeout) {
	// create tunnel service
	$scope.TunnelService = $resource('services/tunnel/info', [],
		{ 
			'save':   {method:'POST'},
			'query':  {method:'GET', isArray:true},
			'pause': {method:'POST', url: 'services/tunnel/:tunnelId/pause', params: {tunnelId:"@id"}},
			'resume': {method:'POST', url: 'services/tunnel/:tunnelId/resume', params: {tunnelId:"@id"}},
			'remove': {method:'DELETE', url: 'services/tunnel/:tunnelId/remove', params: {tunnelId:"@id"}},
		}
	);
	
	// data
	$scope.tunnels = [];
	
	// pause
	$scope.pause = function(port) {
		$scope.pauseRefresh();
		
	};
	
	// what to do when the route has changed
	$scope.$on('$routeChangeSuccess', function () {
		  style.toggleActive('tunnels');
	});
	
	$scope.add = function(tunnel) {
		// wait to update
		$scope.pauseRefresh();
				
	};
	
	$scope.pauseRefresh = function() {
		// cancel previous timer if it exists
		if($scope.updateTimer) {
			if($scope.updateTimer.cancel) {
				$scope.updateTimer.cancel();
			}
		}
	}
	
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
	
	// scope update function
	$scope.updateTable = function() {
		
		// don't double-refresh
		$scope.pauseRefresh();
		
		(function tick(){
			// use service to query for all resources
			var newTunnels = $scope.TunnelService.query(function() {
				$scope.tunnels = newTunnels;
			    $scope.startRefresh();
			});	
		})();
	};
	
	// initial load
	$scope.updateTable();
});