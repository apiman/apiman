/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {
    
    var NINETY_DAYS = 90 * 24 * 60 * 60 * 1000;
    var THIRTY_DAYS = 30 * 24 * 60 * 60 * 1000;
    var SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;
    var ONE_DAY = 1 * 24 * 60 * 60 * 1000;
    var ONE_HOUR = 1 * 60 * 60 * 1000;

    export var ServiceMetricsController = _module.controller("Apiman.ServiceMetricsController",
        ['$q', 'Logger', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'MetricsSvcs', '$routeParams', '$timeout',
        ($q, Logger, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, MetricsSvcs, $routeParams, $timeout) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'metrics';
            $scope.version = params.version;
            $scope.metricsRange = '7days';
            
            var usageChart, usageByAppChart, usageByPlanChart;
            
            var getTimeSeriesFormat = function() {
                var format = '%Y-%m-%d';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours' || $scope.metricsRange == 'hour') {
                    format = '%H:%M';
                }
                return format;
            };
            
            var renderUsageChart = function(data) {
                var xcol = [];
                xcol.push('x');
                var reqCol = ['# Requests']
                
                angular.forEach(data.data, function(dataPoint) {
                    xcol.push(Date.parse(dataPoint.label));
                    reqCol.push(dataPoint.count);
                });
                if (xcol.length == 1) {
                    $scope.usageChartNoData = true;
                } else {
                    Logger.log("======= xcol: " + JSON.stringify(xcol));
                    Logger.log("======= reqs: " + JSON.stringify(reqCol));
                    var usageChart = c3.generate({
                        size: {
                            height: 200
                        },
                        data: {
                            x: 'x',
                            columns: [
                                xcol,
                                reqCol
                            ],
                            types: {
                                '# Requests': 'bar'
                            }
                        },
                        bindto: '#usage-chart',
                        axis: {
                            x: {
                                type: 'timeseries',
                                tick: {
                                    format: getTimeSeriesFormat()
                                }
                            }
                        }
                    });
                }
            };
            
            var renderAppUsageChart = function(data) {
                var columns = [];
                angular.forEach(data.data, function(numRequests, appName) {
                    columns.push([appName, numRequests]);
                });
                if (columns.length == 0) {
                    $scope.appUsageChartNoData = true;
                } else {
                    usageByAppChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            columns: columns,
                            type : 'pie'
                        },
                        bindto: '#app-usage-chart'
                    });
                }
            };
            
            var renderPlanUsageChart = function(data) {
                var columns = [];
                angular.forEach(data.data, function(numRequests, planName) {
                    columns.push([planName, numRequests]);
                });
                if (columns.length == 0) {
                    $scope.planUsageChartNoData = true;
                } else {
                    usageByPlanChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            columns: columns,
                            type : 'pie'
                        },
                        bindto: '#plan-usage-chart'
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
            
            var refreshCharts = function() {
                $scope.usageChartLoading = true;
                $scope.appUsageChartLoading = true;
                $scope.planUsageChartLoading = true;
                
                // TODO get the right date range based on the value of $scope.metricsRange
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
                MetricsSvcs.getUsage(params.org, params.service, params.version, interval, from, to, function(data) {
                    $scope.usageChartLoading = false;
                    renderUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading usage chart data: {0}', JSON.stringify(error));
                    $scope.usageChartLoading = false;
                    $scope.usageChartNoData = true;
                });
                
                // Refresh the app usage chart
                MetricsSvcs.getUsagePerApp(params.org, params.service, params.version, from, to, function(data) {
                    $scope.appUsageChartLoading = false;
                    renderAppUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading app usage chart data: {0}', JSON.stringify(error));
                    $scope.appUsageChartLoading = false;
                    $scope.appUsageChartNoData = true;
                });
                
                // Refresh the plan usage chart
                MetricsSvcs.getUsagePerPlan(params.org, params.service, params.version, from, to, function(data) {
                    $scope.planUsageChartLoading = false;
                    renderPlanUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading plan usage chart data: {0}', JSON.stringify(error));
                    $scope.planUsageChartLoading = false;
                    $scope.planUsageChartNoData = true;
                });
            }
            
            $scope.$watch('metricsRange', function(newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);

            PageLifecycle.loadPage('ServiceMetrics', pageData, $scope, function() {
                PageLifecycle.setPageTitle('service-metrics', [ $scope.service.name ]);
                refreshCharts();
            });
        }])

}
