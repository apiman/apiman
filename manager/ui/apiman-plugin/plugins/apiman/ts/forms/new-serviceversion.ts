/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewServiceVersionController = _module.controller("Apiman.NewServiceVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'Logger',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            $scope.svcversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewServiceVersion = function() {
                $scope.createButton.state = 'in-progress';
                Logger.info('Creating new version {0} of service {1} / {2}', $scope.svcversion.version, params.service, params.org);
                OrgSvcs.save({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: ''}, $scope.svcversion, function(reply) {
                    $location.url(Apiman.pluginName + '/service-overview.html').search('org', params.org).search('service', params.service).search('version', reply.version);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewServiceVersion', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-service-version');
                $scope.$applyAsync(function() {
                    $('#apiman-version').focus();
                });
            });
        }]);

}
