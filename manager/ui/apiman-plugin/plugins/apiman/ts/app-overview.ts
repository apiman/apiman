/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

  export var AppOverviewController = _module.controller("Apiman.AppOverviewController", ['$scope', '$location', 'OrgSvcs',  ($scope, $location, OrgSvcs) => {
    var params = $location.search();
    $scope.version = params.version;
    OrgSvcs.get({organizationId: params.org}, function(org) {
        $scope.org = org;
    } , function(error) {
        alert("ERROR=" + error);
    });
    
    OrgSvcs.get({organizationId: params.org, entityType: 'applications', entityId: params.app}, function(app) {
        $scope.app = app;
    } , function(error) {
        alert("ERROR=" + error);
    });    

    $scope.setVersion = function(app) {
      $scope.selectedAppVersion = app;
      $location.path( Apiman.pluginName + "/app-overview.html").search('org', params.org).search('app', params.app).search('version', app.version);
    };

    OrgSvcs.query({organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version}, function(versions) {
        $scope.versions = versions;
        if (params.version != null) { 
          for (var i=0; i<versions.length; i++) {
            if (params.version == versions[i].version) { 
              $scope.selectedAppVersion = versions[i];
              break;
            }
          }
        } else {
           $scope.selectedAppVersion = versions[0];
        }
    } , function(error) {
        alert("ERROR=" + error);
    });
    OrgSvcs.query({organizationId: params.org, entityType: 'members'}, function(members) {
        $scope.members = members;
    } , function(error) {
        alert("ERROR=" + error);
    });
  }])
  

}
