import {_module} from "../apimanPlugin";

_module.controller("Apiman.AdminGatewaysController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', 
        function ($q, $scope, ApimanSvcs, PageLifecycle) {
            $scope.tab = 'gateways';
            var pageData = {
                gateways: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'gateways' }, function(adminGateways) {
                        resolve(adminGateways);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminGateways', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-gateways');
            });
    }]);