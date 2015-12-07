/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewApiVersionController = _module.controller("Apiman.NewApiVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, Logger, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.apiversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewApiVersion = function() {
                $scope.createButton.state = 'in-progress';
                Logger.info('Creating new version {0} of api {1} / {2}', $scope.apiversion.version, params.api, params.org);
                OrgSvcs.save({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: ''}, $scope.apiversion, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apis/{1}/{2}', params.org, params.api, reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewApiVersion', 'apiEdit', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-api-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
