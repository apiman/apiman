/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

  export var UserAppsController = _module.controller("Apiman.UserAppsController", ['$scope', 'UserSvcs',  ($scope, UserSvcs) => {
    UserSvcs.query({entityType: 'applications'},function(userApps) {
	$scope.applications = userApps;
   }, function(error) {  
        alert("ERROR=" + error);
   });
  }])
  

}
