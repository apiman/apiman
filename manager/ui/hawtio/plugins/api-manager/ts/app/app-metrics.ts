/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppMetricsController = _module.controller("Apiman.AppMetricsController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', '$routeParams', 'MetricsSvcs',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, $routeParams, MetricsSvcs) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'metrics';
            $scope.version = params.version;
            $scope.metricsRange = '7days';
            $scope.metricsType = 'usage';

            var usageByServiceChart;
            
            var renderServiceUsageChart = function(data) {
                var columns = [];
                var x = ['x'];
                var dataPoints = ['data'];
                angular.forEach(data.data, function(numRequests, serviceName) {
                    x.push(serviceName);
                    dataPoints.push(numRequests);
                });
                if (data.data.length == 0) {
                    $scope.serviceUsageChartNoData = true;
                } else {
                    columns.push(x);
                    columns.push(dataPoints);
                    usageByServiceChart = c3.generate({
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
                        bindto: '#service-usage-chart'
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
                $scope.serviceUsageChartLoading = true;
                
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
                if (usageByServiceChart) {
                    usageByServiceChart.destroy();
                    usageByServiceChart = null;
                }
                MetricsSvcs.getAppUsagePerService(params.org, params.app, params.version, from, to, function(data) {
                    $scope.serviceUsageChartLoading = false;
                    renderServiceUsageChart(data);
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
            PageLifecycle.loadPage('AppMetrics', pageData, $scope, function() {
                PageLifecycle.setPageTitle('app-metrics', [ $scope.app.name ]);
                refreshCharts();
            });
        }])

}
