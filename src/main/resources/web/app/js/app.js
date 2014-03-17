var multiTunnelApp = angular.module('multiTunnelApp', ['ngResource','ngRoute']);

multiTunnelApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/tunnels', {
                templateUrl: 'templates/tunnels.html',
                controller: 'TunnelTableController'
        }).
            when('/add-tunnel', {
                templateUrl: 'templates/tunnel_form.html',
                controller: 'TunnelFormController'                	
        }).
        when('/edit-tunnel/:id', {
            templateUrl: 'templates/tunnel_form.html',
            controller: 'TunnelFormController'
        }).
            when('/modules', {
                templateUrl: 'templates/modules.html',
                controller: 'ModulesTableController'
        }).
            otherwise({
                redirectTo: '/tunnels'
        });
}]);

multiTunnelApp.factory("Tunnel", function ($resource) {
    return $resource('services/tunnel/info', [],
    		{ 
				'get':    {method: 'GET', url: 'services/tunnel/:tunnelId', params: {tunnelId:""}},
				'start':  {method:'PUT', url: 'services/tunnel/start'},
				'update': {method:'PUT', url: 'services/tunnel/:tunnelId/update', params: {tunnelId:""}},
				'query':  {method:'GET', isArray:true},
				'pause':  {method:'POST', url: 'services/tunnel/:tunnelId/pause', params: {tunnelId:"@id"}},
				'resume': {method:'POST', url: 'services/tunnel/:tunnelId/resume', params: {tunnelId:"@id"}},
				'remove': {method:'DELETE', url: 'services/tunnel/:tunnelId/remove', params: {tunnelId:"@id"}},
    		}
    );    
});

multiTunnelApp.filter('bytesFormatter', function() {
   return function(bytes) {
	   return style.prettyBytes(bytes);
   }
});