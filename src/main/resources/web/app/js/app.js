// define the application
var multiTunnelApp = angular.module('multiTunnelApp', ['ngResource','ngRoute','siyfion.sfTypeahead']);

// global routes for the application
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

// resource (REST Service) for use in other parts of the application
multiTunnelApp.factory("Tunnel", function ($resource) {
    return $resource('services/tunnel/info', [],
    		{ 
				// get single
    			'get':    {method:'GET', url: 'services/tunnel/:tunnelId', params: {tunnelId:""}},
				
				// query/info (no param)
				'query':  {method:'GET', isArray:true},
				'bootstrap': {method:'GET', url: 'services/tunnel/bootstrap'},
				'blocked': {method:'GET', url: 'services/tunnel/blocked'},

				// instance
				'strap':  {method:'GET', url: 'services/tunnel/:tunnelId/bootstrap', params: {tunnelId:"@id"}},
				'start':  {method:'PUT', url: 'services/tunnel/start'},
				'update': {method:'PUT', url: 'services/tunnel/:tunnelId/update', params: {tunnelId:""}},		
				'pause':  {method:'POST', url: 'services/tunnel/:tunnelId/pause', params: {tunnelId:"@id"}},
				'resume': {method:'POST', url: 'services/tunnel/:tunnelId/resume', params: {tunnelId:"@id"}},
				'remove': {method:'DELETE', url: 'services/tunnel/:tunnelId/remove', params: {tunnelId:"@id"}}
    		}
    );    
});

// calls utility function for converting a byte value into
// a "pretty" byte value
multiTunnelApp.filter('bytesFormatter', function() {
   return function(bytes) {
	   return style.prettyBytes(bytes);
   }
});