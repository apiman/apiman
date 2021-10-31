import angular = require("angular");
import * as c3 from 'c3'
import { _module } from '../apimanPlugin'

    export const NINETY_DAYS = 90 * 24 * 60 * 60 * 1000;
    export const THIRTY_DAYS = 30 * 24 * 60 * 60 * 1000;
    export const SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;
    export const ONE_DAY = 1 * 24 * 60 * 60 * 1000;
    export const ONE_HOUR = 1 * 60 * 60 * 1000;

    _module.controller("Apiman.ApiMetricsController",
        ['$q', 'Logger', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'MetricsSvcs', '$routeParams', '$timeout', 'Configuration',
       function ($q, Logger, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, MetricsSvcs, $routeParams, $timeout, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'metrics';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.metricsRange = '7days';
            $scope.metricsType = 'usage';
            
            var usageChart, usageByClientChart, usageByPlanChart;
            var responseTypeChart, responseTypeSuccessChart, responseTypeFailuresChart, responseTypeErrorsChart;
            
            var getTimeSeriesFormat = function() {
                var format = '%Y-%m-%d';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours' || $scope.metricsRange == 'hour') {
                    format = '%Y-%m-%d %H:%M';
                } else if ($scope.metricsRange == '24hours' || $scope.metricsRange == 'hour') {
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
                    usageChart = c3.generate({
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
                        legend: {
                            hide: true
                        },
                        axis: {
                            x: {
                                type: 'timeseries',
                                tick: {
                                    format: getTimeSeriesFormat()
                                }
                            },
                            y: {
                                label: 'Total Requests'
                            }
                        }
                    });
                }
            };
            
            var renderClientUsageChart = function(data) {
                var columns = [];
                angular.forEach(data.data, function(numRequests, clientName) {
                    columns.push([clientName, numRequests]);
                });
                if (columns.length == 0) {
                    $scope.clientUsageChartNoData = true;
                } else {
                    usageByClientChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            columns: columns,
                            type : 'pie'
                        },
                        bindto: '#client-usage-chart'
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
            
            var renderResponseTypeHistogramChart = function(data) {
                var xcol = [];
                xcol.push('x');
                var successCol = ['Success']
                var failureCol = ['Fail']
                var errorCol = ['Error']

                angular.forEach(data.data, function(dataPoint) {
                    xcol.push(Date.parse(dataPoint.label));
                    successCol.push((dataPoint.total - dataPoint.failures - dataPoint.errors).toString());
                    failureCol.push(dataPoint.failures);
                    errorCol.push(dataPoint.errors);
                });
                if (xcol.length == 1) {
                    $scope.responseTypeChartNoData = true;
                } else {
                    Logger.log("======= xcol: " + JSON.stringify(xcol));
                    Logger.log("======= successCol: " + JSON.stringify(successCol));
                    Logger.log("======= failureCol: " + JSON.stringify(failureCol));
                    Logger.log("======= errorCol: " + JSON.stringify(errorCol));
                    responseTypeChart = c3.generate({
                        size: {
                            height: 200
                        },
                        data: {
                            x: 'x',
                            columns: [
                                xcol, successCol, failureCol, errorCol
                            ],
                            colors: {
                                'Success': '#71B56E',
                                'Fail': '#E37B4F',
                                'Error': '#E34F4F',
                            },
                            types: {
                                'Success': 'bar',
                                'Fail': 'bar',
                                'Error': 'bar',
                            },
                            groups: [
                                ['Success', 'Fail', 'Error']
                            ]
                        },
                        bindto: '#responseType-chart',
                        axis: {
                            x: {
                                type: 'timeseries',
                                tick: {
                                    format: getTimeSeriesFormat()
                                }
                            },
                            y: {
                                label: 'Responses'
                            }
                        }
                    });
                }
            };

            var renderResponseTypeSummaryCharts = function(data) {
                var total = data.total;
                var success = data.total - data.failures - data.errors;
                var failures = data.failures;
                var errors = data.errors;
                
                responseTypeSuccessChart = c3.generate({
                    size: {
                        height: 150
                    },
                    data: {
                        columns: [
                            ['data', success]
                        ],
                        colors: {
                            data: '#71B56E'
                        },
                        type: 'gauge'
                    },
                    gauge: {
                        max: total
                    },
                    bindto: '#responseType-chart-success'
                });
                
                responseTypeFailuresChart = c3.generate({
                    size: {
                        height: 150
                    },
                    data: {
                        columns: [
                            ['data', failures]
                        ],
                        colors: {
                            data: '#E37B4F'
                        },
                        type: 'gauge'
                    },
                    gauge: {
                        max: total
                    },
                    bindto: '#responseType-chart-failed'
                });

                responseTypeErrorsChart = c3.generate({
                    size: {
                        height: 150
                    },
                    data: {
                        columns: [
                            ['data', errors]
                        ],
                        colors: {
                            data: '#E34F4F'
                        },
                        type: 'gauge'
                    },
                    gauge: {
                        max: total
                    },
                    bindto: '#responseType-chart-error'
                });

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
                $scope.usageChartLoading = true;
                $scope.clientUsageChartLoading = true;
                $scope.planUsageChartLoading = true;
                
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
                if (usageChart) {
                    usageChart.destroy();
                    usageChart = null;
                }
                MetricsSvcs.getUsage(params.org, params.api, params.version, interval, from, to, function(data) {
                    $scope.usageChartLoading = false;
                    renderUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading usage chart data: {0}', JSON.stringify(error));
                    $scope.usageChartLoading = false;
                    $scope.usageChartNoData = true;
                });
                
                // Refresh the client usage chart
                if (usageByClientChart) {
                    usageByClientChart.destroy();
                    usageByClientChart = null;
                }
                MetricsSvcs.getUsagePerClient(params.org, params.api, params.version, from, to, function(data) {
                    $scope.clientUsageChartLoading = false;
                    renderClientUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading client usage chart data: {0}', JSON.stringify(error));
                    $scope.clientUsageChartLoading = false;
                    $scope.clientUsageChartNoData = true;
                });
                
                // Refresh the plan usage chart
                if (usageByPlanChart) {
                    usageByPlanChart.destroy();
                    usageByPlanChart = null;
                }
                MetricsSvcs.getUsagePerPlan(params.org, params.api, params.version, from, to, function(data) {
                    $scope.planUsageChartLoading = false;
                    renderPlanUsageChart(data);
                }, function(error) {
                    Logger.error('Error loading plan usage chart data: {0}', JSON.stringify(error));
                    $scope.planUsageChartLoading = false;
                    $scope.planUsageChartNoData = true;
                });
            }

            // *******************************************************
            // Refresh the response type charts
            // *******************************************************
            var refreshResponseTypeCharts = function() {
                $scope.responseTypeChartLoading = true;
                $scope.responseTypeSuccessChartLoading = true;
                $scope.responseTypeFailedChartLoading = true;
                $scope.responseTypeErrorChartLoading = true;
                
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

                // Refresh the response type chart
                if (responseTypeChart) {
                    responseTypeChart.destroy();
                    responseTypeChart = null;
                }
                MetricsSvcs.getResponseStats(params.org, params.api, params.version, interval, from, to, function(data) {
                    $scope.responseTypeChartLoading = false;
                    renderResponseTypeHistogramChart(data);
                }, function(error) {
                    Logger.error('Error loading response type stats histogram data: {0}', JSON.stringify(error));
                    $scope.responseTypeChartLoading = false;
                    $scope.responseTypeChartNoData = true;
                });
                
                // Refresh the success, failure, and error charts
                if (responseTypeSuccessChart) {
                    responseTypeSuccessChart.destroy();
                    responseTypeSuccessChart = null;
                }
                if (responseTypeFailuresChart) {
                    responseTypeFailuresChart.destroy();
                    responseTypeFailuresChart = null;
                }
                if (responseTypeErrorsChart) {
                    responseTypeErrorsChart.destroy();
                    responseTypeErrorsChart = null;
                }
                MetricsSvcs.getResponseStatsSummary(params.org, params.api, params.version, from, to, function(data) {
                    $scope.responseTypeSuccessChartLoading = false;
                    $scope.responseTypeFailedChartLoading = false;
                    $scope.responseTypeErrorChartLoading = false;
                    renderResponseTypeSummaryCharts(data);
                }, function(error) {
                    Logger.error('Error loading response type summary stats chart data: {0}', JSON.stringify(error));
                    $scope.responseTypeSuccessChartLoading = false;
                    $scope.responseTypeFailedChartLoading = false;
                    $scope.responseTypeErrorChartLoading = false;
                    $scope.responseTypeSuccessChartNoData = true;
                    $scope.responseTypeFailedChartNoData = true;
                    $scope.responseTypeErrorChartNoData = true;
                });
            }
            
            var refreshCharts = function() {
                Logger.debug("Refreshing charts!");
                if ($scope.metricsType == 'usage') {
                    refreshUsageCharts();
                }
                if ($scope.metricsType == 'responseType') {
                    refreshResponseTypeCharts();
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
            
            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            PageLifecycle.loadPage('ApiMetrics', 'apiView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('api-metrics', [ $scope.api.name ]);
                refreshCharts();
            });
        }]);