// define the application
var multiTunnelApp = angular.module('multiTunnelApp', 
	[
	 'ngResource', 				// json/ajax services
	 'ngRoute', 				// application url router
	 'siyfion.sfTypeahead',		// typeahead widget integration
	 'ui.codemirror'			// code mirror integration
	]
);

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
	        when('/add-module', {
	            templateUrl: 'templates/module_add.html',
	            controller: 'ModuleAddFormController'
	    }).
	    when('/module/:id', {
            templateUrl: 'templates/module_details.html',
            controller: 'ModuleDetailsFormController'
	    }).
            otherwise({
                redirectTo: '/tunnels'
        });
}]);