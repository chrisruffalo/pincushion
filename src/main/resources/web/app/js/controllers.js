var multiTunnelApp = angular.module('multiTunnelApp', ['ngRoute']);

multiTunnelApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/tunnels', {
                templateUrl: 'templates/tunnels.html',
                controller: 'TunnelTableController'
        }).
            when('/modules', {
                templateUrl: 'templates/modules.html',
                controller: 'ModulesTableController'
        }).
            otherwise({
                redirectTo: '/tunnels'
        });
}]);
 
multiTunnelApp.controller('TunnelTableController', function ($scope, $http, $timeout) {
	$scope.pause = function(port) {
		$scope.pauseRefresh();
		
	};
	
	$scope.remove = function(port) {
		// wait to update
		$scope.pauseRefresh();
		
		// delete
		$.ajax({
			type: 'DELETE',
			url: 'services/tunnel/' + port + '/remove'			
		}).always(function() {
			$scope.updateTable();
		})
	}
	
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
		
		$http.get('services/tunnel/info').success(function(data) {
			// save new data
			$scope.tunnels = data;
			// start refresh
			$scope.startRefresh();
		});
	};
	
	// initial load
	$scope.updateTable();
});

multiTunnelApp.controller('ModulesTableController', function ($scope, $http, $timeout) {
	
});