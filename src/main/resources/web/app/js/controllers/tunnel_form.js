multiTunnelApp.controller('TunnelFormController', function ($scope, $resource, $routeParams, $timeout, $location, Tunnel) {
	
    // common go back to tunnel screen
    $scope.returnToTunnels = function() {
    	$location.path("tunnels");
    }

    // load form
	$scope.load = function(id) {
		// set initial values
		$scope.tunnel = null;
		$scope.id = null;

		// bootstrap from existing configuration
		if(id) {
			// save off id
			$scope.id = id;
			
			// bootstrap object from server resource
			var localBootstrap = Tunnel.strap({tunnelId:id}, function(){
				// get configuration
				$scope.bootstrap = new Tunnel(localBootstrap);
				
				// copy master from configuration
				$scope.master = angular.copy($scope.bootstrap);
			}, 
				$scope.returnToTunnels // return on error
			);
		} else {
			// bootstrap from default 
			var localBootstrap = Tunnel.bootstrap(function() {
				
				// get configuration
				$scope.bootstrap = new Tunnel(localBootstrap);
				
				// copy master from configuration
				$scope.master = angular.copy($scope.bootstrap);
			}, 
				$scope.returnToTunnels // return on error
			);
		}		
	}
	
	// go back
	$scope.cancel = function() {
		$scope.returnToTunnels();
	}
	
	// reset to master copy
	$scope.reset = function() {
		$scope.bootstrap = angular.copy($scope.master);
    };	 
    
    // save
    $scope.save = function(configuration) {
    	// make sure it has it's tunnel ref
    	if(!configuration.$start || !configuration.$update) {
    		configuration = new Tunnel(configuration);
    	}
    	
    	// do functions
    	if(!$scope.id) {
    		configuration.$start(function(){
	    		$scope.returnToTunnels();
	    	});
    	} else {
    		configuration.$update({tunnelId: $scope.id}, function(){
    			$scope.returnToTunnels();
	    	});
    	}
    }
    
	// do load
	$scope.load($routeParams.id)
});