/// <reference path="baseHelpers.d.ts" />
declare module PollHelpers {
    function setupPolling($scope: any, updateFunction: (next: () => void) => void, period?: number, $timeout?: ng.ITimeoutService, jolokia?: Jolokia.IJolokia): () => void;
}
