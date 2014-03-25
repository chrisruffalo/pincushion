// resource (REST Service) for use in other parts of the application
multiTunnelApp.factory("Tunnel", function ($resource) {
    return $resource('services/tunnel/info', [],
    		{ 
				// get single
    			'get':    {method:'GET', url: 'services/tunnel/:tunnelId', params: {tunnelId:""}},
				
				// query/info (no param)
				'query':  {method:'GET', isArray:true},
				'bootstrap': {method:'GET', url: 'services/tunnel/bootstrap'},
				'available': {method:'POST', url: 'services/tunnel/:tunnelPort/available', params: {tunnelPort:""}},

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