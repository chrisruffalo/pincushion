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

multiTunnelApp.filter('bytesFormatter', function() {
   return function(bytes) {
	   return style.prettyBytes(bytes);
   }
});