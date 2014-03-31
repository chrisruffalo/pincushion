multiTunnelApp.controller('TunnelFormController', function ($scope, $resource, $routeParams, $timeout, $location, Tunnel) {
	// set up text editor
    $scope.editorOptions = {
        lineWrapping : false,
        lineNumbers: true,
        mode: 'application/json',
        styleActiveLine: true,
        matchBrackets: true
    };
	
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
				local: [] // source will be updated from ajax configuration bootstrap call
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
    
    // create shadow binding for json editor
    $scope.$watch('modelJsonStringShadow', function(newValue, oldValue) {
    	if(newValue != oldValue) {
    		$scope.modelJsonStringShadow = newValue;
    		try {
    			$scope.bootstrap.configuration = JSON.parse(newValue);
    		} catch (e) {
    			// do nothing for now
    		}
    	}
    });    
   
    // create inverse for shadow binding
    $scope.$watch('bootstrap.configuration', function(newValue, oldValue) {
    	if(newValue != oldValue) {
    		$scope.modelJsonStringShadow = JSON.stringify(newValue, null, 4);
    	}
    }, true);   
    
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
    
    // force update shadow
    $scope.forceShadow = function() {
    	// wait just enough to let it show
    	setTimeout(function() {
	    	var updateThatChanges = new Date();
	    	$scope.shadowForce = updateThatChanges.getTime();
    	},20);
    };
    
    // make sure error is hidden
    $('#tunnelFormError').hide();
    
	// do load
	$scope.load($routeParams.id);
});

// directive for validating ports (example from: http://stackoverflow.com/a/12865401/128339)
multiTunnelApp.directive('portCheck', function(Tunnel) {
  var toId;
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, elem, attr, ctrl) { 
      //when the scope changes, check the email.
      scope.$watch(attr.ngModel, function(value) {
    	// if value is null or not a number don't bother
    	if(!value) {
    		return;
    	}
    	
    	// if the value is not a number just set invalid without remote check
    	if(!util.isNumber(value)) {
    		ctrl.$setValidity('inputTunnelSourcePort', false);
    		return;
    	}
    	
        // if there was a previous attempt, stop it.
        if(scope.portCheckTimer) {
        	clearTimeout(scope.portCheckTimer);
        }

        // start a new attempt with a delay to keep it from
        // getting too "chatty".
        scope.portCheckTimer = setTimeout(function(){
        	var portResponse = Tunnel.available({tunnelPort: value}, {interfaceName: "0.0.0.0"}, function(data){
        		// default state is invalid
        		var valid = false;
        		// check that response is returned and is not "false" string
        		if(portResponse && portResponse.result && portResponse.result != "false") {
        			valid = true;
        		}     
        		//set the validity of the field
                ctrl.$setValidity('inputTunnelSourcePort', valid);
        	});
        }, 200);
      })
    }
  }
});