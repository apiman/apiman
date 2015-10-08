/// <reference path="folder.d.ts" />
/// <reference path="workspace.d.ts" />
/**
 * @module Jmx
 */
declare module Jmx {
    var log: Logging.Logger;
    function findLazyLoadingFunction(workspace: any, folder: any): any;
    function registerLazyLoadHandler(domain: string, lazyLoaderFactory: (folder: Core.Folder) => any): void;
    function unregisterLazyLoadHandler(domain: string, lazyLoaderFactory: (folder: Core.Folder) => any): void;
    /**
     * Registers a toolbar template for the given plugin name, jmxDomain.
     * @method addAttributeToolBar
     * @for Jmx
     * @param {String} pluginName used so that we can later on remove this function when the plugin is removed
     * @param {String} jmxDomain the JMX domain to avoid having to evaluate too many functions on each selection
     * @param {Function} fn the function used to decide which attributes tool bar should be used for the given select
     */
    function addAttributeToolBar(pluginName: string, jmxDomain: string, fn: (NodeSelection: any) => string): void;
    /**
     * Try find a custom toolbar HTML template for the given selection or returns the default value
     * @method getAttributeToolbar
     * @for Jmx
     * @param {Core.NodeSelection} node
     * @param {String} defaultValue
     */
    function getAttributeToolBar(node: NodeSelection, defaultValue?: string): any;
    function updateTreeSelectionFromURL($location: any, treeElement: any, activateIfNoneSelected?: boolean): void;
    function updateTreeSelectionFromURLAndAutoSelect($location: any, treeElement: any, autoSelect: any, activateIfNoneSelected?: boolean): void;
    function getUniqueTypeNames(children: any): string[];
    function enableTree($scope: any, $location: ng.ILocationService, workspace: Core.Workspace, treeElement: any, children: any, redraw?: boolean, onActivateFn?: any): void;
}
