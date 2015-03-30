/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewPlanVersionController = _module.controller("Apiman.NewPlanVersionController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 
        ($q, $location, $scope, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.planversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewPlanVersion = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: ''}, $scope.planversion, function(reply) {
                    $location.url(Apiman.pluginName + '/plan-overview.html').search('org', params.org).search('plan', params.plan).search('version', reply.version);
                }, function(error) {
                    if (error.status == 409) {
                        $location.url('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('NewPlanVersion', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-plan-version');
                $('#apiman-version').focus();
            });
        }]);

}
