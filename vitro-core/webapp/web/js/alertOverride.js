// To fix a problem involving dwr displaying pop ups in the admin section, overwrite the alert function.
(function() {
  var proxied = window.alert;
  window.alert = function() {
    // do something here
    console.log(arguments);
  };
})();
