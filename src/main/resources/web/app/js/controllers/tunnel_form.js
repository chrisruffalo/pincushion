multiTunnelApp.controller('TunnelFormController', function ($scope, $routeParams, $timeout, $location, Tunnel) {
	// set up text editor
    $scope.editorOptions = {
        lineWrapping : false,
        lineNumbers: true,
        mode: 'application/json',
        styleActiveLine: true,
        matchBrackets: true,
        indentUnit: 4
    };
    
    // set up error holder
    $scope.status = {};
	
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

				// copy original port
				$scope.originalPort = angular.copy($scope.bootstrap.configuration.sourcePort);
				
				// init typeahead
				$scope.typeaheadUpdate($scope.bootstrap);
			}, 
				$scope.returnToTunnels // return on error
			);
		} else {
			// bootstrap from default 
			var localBootstrap = Tunnel.bootstrap(function() {
				
				// no original port value
				$scope.originalPort = null;
				
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
		$scope.tunnelForm.$setPristine();
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
    	
    	// save message so that it can be shown in the ui
    	$scope.status.alert = message;
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
    
    // create a function for manually refreshing the code editor
    $scope.updateMirror = function() {
    	setTimeout(function(){
	    	if($scope.mirror) {
	    		$scope.mirror.refresh();
	    	}
    	}, 100); // after 100ms
    };
        
    // make sure error is hidden
    $('#tunnelFormError').hide();
    
	// do load
	$scope.load($routeParams.id);
});

// directive for validating ports (example from: http://stackoverflow.com/a/12865401/128339)
multiTunnelApp.directive('portCheck', function(Tunnel) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, elem, attr, ctrl) { 
      // get form item id
      var identifier = attr.name || attr.id;
      
      // when the port changes, check the port
      scope.$watch(attr.ngModel, function(newValue, oldValue) {
    	// if there was a previous attempt, stop it.
        if(scope.portCheckTimer) {
        	clearTimeout(scope.portCheckTimer);
        }

        // if value is null or not a number don't bother
    	if(!newValue) {
    		return;
    	}
    	
    	// if the value is not a number just set invalid without remote check
    	if(!util.isNumber(newValue)) {
        	scope.status[identifier] = "Port value should be a number between 1 and 65536.";
    		ctrl.$setValidity(identifier, false);
    		return;
    	}
    	        
        // get original port from id
        var originalPort = -1;
        if(attr.originalPort) {
      	  originalPort = scope.$eval(attr.originalPort);
      	  if(originalPort && originalPort != "" && originalPort != "null" && util.isNumber(originalPort)) {
      		  originalPort = parseFloat(originalPort);
      	  }
        }
            	
    	// don't complain if port is the original value
    	if(originalPort && originalPort > 0) {
    		if(newValue == originalPort) {
            	scope.status[identifier] = null;
    			ctrl.$setValidity(identifier, true);
    			return;
    		}
    	}
    	
    	// look up interface from form value
    	var interfaceInput = "0.0.0.0";
    	if(attr.checkInterface) {
    		var attrInterface = scope.$eval(attr.checkInterface);
    		if(attrInterface != null) {
    			interfaceInput = attrInterface;
    		}
    	}
    	
    	// complain if a bad/null interface is somehow given
    	if(!attrInterface) {
    		// save port error
    		scope.status[identifier] = "A valid source interface is required to check the validity of a port";
    		//set the validity of the field
            ctrl.$setValidity(identifier, false);
    		// can't submit with invalid interface
    		return;
    	}
        
        // start a new attempt with a delay to keep it from
        // getting too "chatty".
        scope.portCheckTimer = setTimeout(function(){
        	// clear error status
        	scope.status[identifier] = null;       	
        	// get validity response from server
        	var portResponse = Tunnel.available({tunnelPort: newValue}, {interfaceName: interfaceInput}, function(data){
        		// default state is invalid
        		var valid = false;
        		// check that response is returned and is not "false" string
        		if(portResponse && portResponse.result && portResponse.result != "false") {
        			valid = true;
        			scope.status[identifier] = null; // clear errors
        		} else {
        			scope.status[identifier] = "The provided port is not available or is currently in use.";
        			if(newValue < 1024) {
        				scope.status[identifier] += "  Check that you have permissions to use this port.";
        			}
        		}
        		//set the validity of the field
                ctrl.$setValidity(identifier, valid);
        	});
        }, 400);
      })
    }
  }
});

//directive for validating interfaces
multiTunnelApp.directive('interfaceCheck', function(Tunnel) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function(scope, elem, attr, ctrl) { 
      // get form item id
      var identifier = attr.name || attr.id;

      // when the interface changes, check the interface
      scope.$watch(attr.ngModel, function(newValue, oldValue) {
    	// if there was a previous attempt, stop it.
        if(scope.interfaceCheckTimer) {
        	clearTimeout(scope.interfaceCheckTimer);
        }
        
        if(!newValue) {
        	// clear error status
        	scope.status[identifier] = null;       	
        	return;
        }
        
        // start a new attempt with a delay to keep it from
        // getting too "chatty".
        scope.interfaceCheckTimer = setTimeout(function(){
        	// get validity response from server
        	var portResponse = Tunnel.checkInterface({}, {interfaceName: newValue}, function(data){
        		
        		if(!portResponse) {
        			return;
        		}
        		
        		// make status empty
        		scope.status[identifier] = {};
        		
        		// add messages/error status, etc
        		if(portResponse.message) {
        			scope.status[identifier].message = portResponse.message;
        			console.log("got response message: " + portResponse.message);
        		}
        	});
        }, 400);
      })
    }
  }
});