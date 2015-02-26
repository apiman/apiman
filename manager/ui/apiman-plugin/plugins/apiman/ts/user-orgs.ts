/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

  export var UserOrgsController = _module.controller("Apiman.UserOrgsController", ['$scope', 'UserSvcs',  ($scope, UserSvcs) => {
    UserSvcs.query({entityType: 'organizations'},function(userOrgs) {
	$scope.organizations = userOrgs;
   }, function(error) {  
        alert("ERROR=" + error);
   });
  }])
  

}
