multiTunnelApp.controller('ModuleAddFormController', function ($scope, $resource, $routeParams, $timeout, $location, Module) {
	
    // common go back to tunnel screen
    $scope.returnToModules = function() {
    	$location.path("modules");
    }
    
    // load form
	$scope.load = function(id) {

	};
	
	// go back
	$scope.cancel = function() {
		$scope.returnToModules();
	};
	
	// reset to master copy
	$scope.reset = function() {

	};	 
    
    // what to do when an http error is returned
    $scope.onError = function(errorHttpResponse) {
    
    }
    
    // save
    $scope.save = function(configuration) {
    	
    };
    
	// do load
	$scope.load($routeParams.id);
});