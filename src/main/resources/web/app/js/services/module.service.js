// resource (REST Service) for use in other parts of the application
multiTunnelApp.factory("Module", function ($resource) {
    return $resource('services/module/info', [],
    		{ 
				// get single
    			'get':    {method:'GET', url: 'services/module/:tunnelId', params: {tunnelId:""}},
				
				// query/info (no param)
				'query':  {method:'GET', isArray:true},
			}
    );    
});