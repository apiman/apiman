/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewServiceVersionController = _module.controller("Apiman.NewServiceVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'Logger',
        ($q, $location, $scope, OrgSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            $scope.svcversion = {
                clone: true
            };
            $scope.saveNewServiceVersion = function() {
                $scope.createButton.state = 'in-progress';
                Logger.info('Creating new version {0} of service {1} / {2}', $scope.svcversion.version, params.service, params.org);
                OrgSvcs.save({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: ''}, $scope.svcversion, function(reply) {
                    $location.url(Apiman.pluginName + '/service-overview.html').search('org', params.org).search('service', params.service).search('version', reply.version);
                }, function(error) {
                    if (error.status == 409) {
                        $location.url('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('NewServiceVersion', undefined, $scope);
            $('#apiman-version').focus();
        }]);

}
