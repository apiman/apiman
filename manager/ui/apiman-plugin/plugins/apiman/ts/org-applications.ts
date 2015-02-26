/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

  export var OrgAppsController = _module.controller("Apiman.OrgAppsController", ['$scope', '$location', 'OrgSvcs',  ($scope, $location, OrgSvcs) => {
    var params = $location.search();
    OrgSvcs.get({organizationId: params.org, entityType: ''}, function(org) {
        $scope.org = org;
    } , function(error) {
        alert("ERROR=" + error);
    });    
    OrgSvcs.query({organizationId: params.org, entityType: 'members'}, function(members) {
        $scope.members = members;
    } , function(error) {
        alert("ERROR=" + error);
    });
    OrgSvcs.query({organizationId: params.org, entityType: 'applications'}, function(apps) {
        $scope.apps = apps;
    } , function(error) {
        alert("ERROR=" + error);
    });
  }])
  

}
