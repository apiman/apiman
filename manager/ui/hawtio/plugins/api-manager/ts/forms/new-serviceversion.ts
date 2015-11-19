/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewServiceVersionController = _module.controller("Apiman.NewServiceVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, Logger, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.svcversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewServiceVersion = function() {
                $scope.createButton.state = 'in-progress';
                Logger.info('Creating new version {0} of service {1} / {2}', $scope.svcversion.version, params.service, params.org);
                OrgSvcs.save({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: ''}, $scope.svcversion, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/services/{1}/{2}', params.org, params.service, reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewServiceVersion', 'svcEdit', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-service-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
