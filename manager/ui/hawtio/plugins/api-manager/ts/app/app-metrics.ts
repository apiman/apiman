/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var AppMetricsController = _module.controller("Apiman.AppMetricsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', '$routeParams', 'MetricsSvcs', 'Configuration',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, $routeParams, MetricsSvcs, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'metrics';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.metricsRange = '7days';
            $scope.metricsType = 'usage';

            var usageByApiChart;
            
            var renderApiUsageChart = function(data) {
                var columns = [];
                var x = ['x'];
                var dataPoints = ['data'];
                angular.forEach(data.data, function(numRequests, apiName) {
                    x.push(apiName);
                    dataPoints.push(numRequests);
                });
                if (data.data.length == 0) {
                    $scope.apiUsageChartNoData = true;
                } else {
                    columns.push(x);
                    columns.push(dataPoints);
                    usageByApiChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            x: 'x',
                            columns: columns,
                            type : 'bar'
                        },
                        axis: {
                            x: {
                                type: 'category'
                            }
                        },
                        bar: {
                            width: {
                                ratio: 0.9
                            }
                        },
                        legend: {
                            hide: true
                        },
                        bindto: '#api-usage-chart'
                    });
                }
            };

            var truncateToDay = function(date) {
                truncateToHour(date);
                date.setHours(0);
                return date;
            };
            
            var truncateToHour = function(date) {
                date.setMinutes(0);
                date.setSeconds(0);
                date.setMilliseconds(0);
                return date;
            };
            
            var getChartDateRange = function() {
                var from = new Date();
                var to = new Date();
                if ($scope.metricsRange == '90days') {
                    from = new Date(from.getTime() - NINETY_DAYS);
                    truncateToDay(from);
                } else if ($scope.metricsRange == '30days') {
                    from = new Date(from.getTime() - THIRTY_DAYS);
                    truncateToDay(from);
                } else if ($scope.metricsRange == '7days') {
                    from = new Date(from.getTime() - SEVEN_DAYS);
                    truncateToDay(from);
                } else if ($scope.metricsRange == '24hours') {
                    from = new Date(from.getTime() - ONE_DAY);
                    truncateToHour(from);
                } else if ($scope.metricsRange == 'hour') {
                    from = new Date(from.getTime() - ONE_HOUR);
                }
                return {
                    from: from,
                    to: to
                }
            };
            
            // *******************************************************
            // Refresh the usage charts
            // *******************************************************
            var refreshUsageCharts = function() {
                $scope.apiUsageChartLoading = true;
                
                var range = getChartDateRange();
                var from = range.from;
                var to = range.to;
                var interval = 'day';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours') {
                    interval = 'hour';
                }
                if ($scope.metricsRange == 'hour') {
                    interval = 'minute';
                }
                
                // Refresh the usage chart
                if (usageByApiChart) {
                    usageByApiChart.destroy();
                    usageByApiChart = null;
                }
                MetricsSvcs.getAppUsagePerApi(params.org, params.app, params.version, from, to, function(data) {
                    $scope.apiUsageChartLoading = false;
                    renderApiUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading usage chart data: {0}', JSON.stringify(error));
                    $scope.usageChartLoading = false;
                    $scope.usageChartNoData = true;
                });
            }

            var refreshCharts = function() {
                if ($scope.metricsType == 'usage') {
                    refreshUsageCharts();
                }
            };
            $scope.refreshCharts = refreshCharts;
            
            $scope.$watch('metricsRange', function(newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            $scope.$watch('metricsType', function(newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('AppMetrics', 'appView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('app-metrics', [ $scope.app.name ]);
                refreshCharts();
            });
        }])

}
