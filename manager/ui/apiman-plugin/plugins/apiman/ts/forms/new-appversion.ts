/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewAppVersionController = _module.controller("Apiman.NewAppVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', ($q, $location, $scope, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.appversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewAppVersion = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: ''}, $scope.appversion, function(reply) {
                    $location.url(Apiman.pluginName + '/app-overview.html').search('org', params.org).search('app', params.app).search('version', reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewAppVersion', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-app-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
