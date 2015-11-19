/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceEndpointController = _module.controller("Apiman.ServiceEndpointController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusService',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusService) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'endpoint';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                managedEndpoint: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'endpoint' }, resolve, reject);
                })
            });

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };


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


            PageLifecycle.loadPage('ServiceEndpoint', 'svcView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('service-endpoint', [ $scope.service.name ]);
            });
        }]);

}
