multiTunnelApp.controller('ModulesTableController', function ($scope, $http, $timeout) {
	
	// what to do when the route has changed
	$scope.$on('$routeChangeSuccess', function () {
		  style.toggleActive('modules');
	});
	
});