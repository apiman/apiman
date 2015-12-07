/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiEndpointController = _module.controller("Apiman.ApiEndpointController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusSvc',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusSvc) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'endpoint';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                managedEndpoint: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'endpoint' }, resolve, reject);
                })
            });

            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

            // Tooltip

            // Initiates the tooltip (this is required for performance reasons)
            $(function () {
                $('[data-toggle="tooltip"]').tooltip();
            });

            // Called if copy-to-clipboard functionality was successful
            $scope.copySuccess = function () {
                //console.log('Copied!');
            };

            // Called if copy-to-clipboard functionality was unsuccessful
            $scope.copyFail = function (err) {
                //console.error('Error!', err);
            };

            // Called on clicking the button the tooltip is attached to
            $scope.tooltipChange = function() {
                $('[data-toggle="tooltip"]').attr('data-original-title', 'Copied!');

                // This is a workaround for jQuery + TS not playing well together
                (<any>$('[data-toggle="tooltip"]')).tooltip('show');
            };

            // Call when the mouse leaves the button the tooltip is attached to
            $scope.tooltipReset = function() {
                $('[data-toggle="tooltip"]').attr('data-original-title', 'Copy to clipboard');
            };


            PageLifecycle.loadPage('ApiEndpoint', 'apiView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('api-endpoint', [ $scope.api.name ]);
            });
        }]);

}
