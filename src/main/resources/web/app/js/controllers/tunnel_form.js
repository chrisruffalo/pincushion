multiTunnelApp.controller('TunnelFormController', function ($scope, $resource, $routeParams, $timeout, $location, Tunnel) {
	

	// load form
	$scope.loadForm = function(id) {
		if(id) {
			// set initial values
			$scope.tunnel = new Tunnel({});
			$scope.id = null;
			
			// get object from server resource
			var localTunnel = Tunnel.get({tunnelId:id}, function(){
				if(localTunnel.configruation) {
					// save off id
					$scope.id = id;
					
					// get configuration
					$scope.tunnel = new Tunnel(localTunnel.configruation);
				}
			});
		} 
		$scope.master = angular.copy($scope.tunnel);
	}
	
	// reset to master copy
	$scope.reset = function() {
		$scope.tunnel = angular.copy($scope.master);
    };	 
    
    $scope.save = function(tunnel) {
    	// make sure it has it's tunnel ref
    	if(!tunnel.$start || !tunnel.$update) {
    		tunnel = new Tunnel(tunnel);
    	}
    	
    	// do functions
    	if(!$scope.id) {
	    	tunnel.$start(function(){
	    		$location.path("#tunnels").replace();
	    	});
    	} else {
    		tunnel.$update({tunnelId: $scope.id}, function(){
	    		$location.path("#tunnels").replace();
	    	});
    	}
    }
		
	// do load
	$scope.loadForm($routeParams.id)
});