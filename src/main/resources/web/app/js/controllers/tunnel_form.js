multiTunnelApp.controller('TunnelFormController', function ($scope, $resource, $routeParams, $timeout, $location, Tunnel) {
	
    // common go back to tunnel screen
    $scope.returnToTunnels = function() {
    	$location.path("tunnels");
    }
    
	// options do not need to be initialized at load time for bloodhound
	$scope.typeAheadOptions = {
		 highlight: true
	};
	
	// create a bloodhound
	$scope.createBloodhound = function() {
		
		var bloodhoundItem = new Bloodhound({
				datumTokenizer: function(d) { 
					return Bloodhound.tokenizers.whitespace(d.value); 
				},
				queryTokenizer: Bloodhound.tokenizers.whitespace,
				local: []
			});
		bloodhoundItem.initialize();
		
		return bloodhoundItem;
	}

	// create configuration item
	$scope.createConfigurationItem = function(bloodhound, heading) {
		var item = {
			name: 'heading',
			displayKey: 'value',
			source: bloodhound.ttAdapter(),
			templates: {
				header: '<h3 class="category-name">' + heading + '</h3>'
			}
		}

		return item;
	};
	
	// need to store bloodhound items for each source
	$scope.bloodhounds = {
		physical: $scope.createBloodhound(),
		hosts: $scope.createBloodhound(),
		ipv4: $scope.createBloodhound(),
		ipv6: $scope.createBloodhound()
	}
	
	// configuration sources
	$scope.suggestionSources = [
	    $scope.createConfigurationItem($scope.bloodhounds.physical, "Physical"),
	    $scope.createConfigurationItem($scope.bloodhounds.hosts, "Hostname"),
	    $scope.createConfigurationItem($scope.bloodhounds.ipv4, "IPV4"),
	    $scope.createConfigurationItem($scope.bloodhounds.ipv6, "IPV6")
	];
		
	// init typeahead suggestion mechanism
	$scope.typeaheadUpdate = function(bootstrap) {
		
		// get interfaces from bootstrap
		var interfaces = bootstrap.validInterfaces;

		var update = function(source, newData) {
			if(!source || !newData) {
				return;
			}
			
			// add map to source
			source.add($.map(newData, 
				function(data){ 
					return {value: data}; 
				}
			));
		};
		
		// update bloodhound indexes for each search provider
		update($scope.bloodhounds.physical, interfaces.Physical);
		update($scope.bloodhounds.hosts, interfaces.Hostname);
		update($scope.bloodhounds.ipv4, interfaces.IPV4);
		update($scope.bloodhounds.ipv6, interfaces.IPV6);
	};
	
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

				// init typeahead
				$scope.typeaheadUpdate($scope.bootstrap);
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
				
				// init typeahead
				$scope.typeaheadUpdate($scope.bootstrap);
			}, 
				$scope.returnToTunnels // return on error
			);
		}		
	};
	
	// go back
	$scope.cancel = function() {
		$scope.returnToTunnels();
	};
	
	// reset to master copy
	$scope.reset = function() {
		$scope.bootstrap = angular.copy($scope.master);
    };	 
    
    // watch scope bootstrap.configuration.sourceInterface and
    // if an object is set there with a name property, overwrite
    // it with the name property... this is because bloodhound/typeahead
    // doesn't seem to have any sort of valuekey, value selection idea
    $scope.$watch('bootstrap.configuration.sourceInterface', function(newValue, oldValue) {
    	if(newValue && newValue.value && $scope.bootstrap && $scope.bootstrap.configuration) {
    		$scope.bootstrap.configuration.sourceInterface = newValue.value;
    	}
    });
    
    // what to do when an http error is returned
    $scope.onError = function(errorHttpResponse) {
    
    	// get and normalize response
    	var response = errorHttpResponse.data;
    	var message = "An error occured while submitting the form, check your values and submit again.";
    	if(response && response.message && response.message != "") {
    		message = response.message;    		
    	}
    	
    	// set tunnel form error text
    	$('#tunnelFormError').html('<h4>Oh no!</h4>' + message + '</p>');
    	
    	// show the error alert
    	$('#tunnelFormError').show();
    }
    
    // save
    $scope.save = function(configuration) {
    	// make sure it has its tunnel ref
    	if(!configuration.$start || !configuration.$update) {
    		configuration = new Tunnel(configuration);
    	}
    	
    	// do save functions
    	if(!$scope.id) {
    		configuration.$start(function(){
	    		$scope.returnToTunnels();
	    	}, $scope.onError);
    	} else {
    		configuration.$update({tunnelId: $scope.id}, function(){
    			$scope.returnToTunnels();
	    	}, $scope.onError);
    	}
    };
    
    // make sure error is hidden
    $('#tunnelFormError').hide();
    
	// do load
	$scope.load($routeParams.id);
});