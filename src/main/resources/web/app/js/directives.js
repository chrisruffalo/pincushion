// calls utility function for converting a byte value into
// a "pretty" byte value
multiTunnelApp.filter('bytesFormatter', function() {
   return function(bytes) {
	   return style.prettyBytes(bytes);
   }
});
