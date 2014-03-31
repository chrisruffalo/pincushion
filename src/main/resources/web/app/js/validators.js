// directive for validating numbers in a range
multiTunnelApp.directive('forceNumber', function() {
  return {
	    restrict: 'A',
	    require: 'ngModel',
	    link: function(scope, elem, attr, ctrl) {
		    //when the scope changes, check the number value
		    scope.$watch(attr.ngModel, function(value) {
		    	
		    	var identifier = attr.name || attr.id;
		    	
		    	// if value is null or not a number don't bother
		    	if(!value) {
		    		return;
		    	}
		    	
		    	// if the value is not a number just set invalid without remote check
		    	if(!util.isNumber(value)) {
		    		ctrl.$setValidity(identifier, false);
		    		return;
		    	}
		    	
		    	// force cast to number for value
		    	value = parseFloat(value);
		    	
		    	if(attr.min && util.isNumber(attr.min) && value < parseFloat(attr.min)) {
		    		ctrl.$setValidity(identifier, false);
		    		return;
		    	}
		    	
		    	if(attr.max && util.isNumber(attr.max) && value > parseFloat(attr.max)) {
		    		ctrl.$setValidity(identifier, false);
		    		return;
		    	}
		    	
		    	// make sure to ste valid
		    	ctrl.$setValidity(identifier, true);
		    });
        }
    }
});