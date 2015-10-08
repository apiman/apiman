/// <reference path="jolokiaInterfaces.d.ts" />
/// <reference path="folder.d.ts" />
/// <reference path="jmxHelpers.d.ts" />
/**
 * @module Core
 */
declare module Core {
    /**
     * @class NavMenuItem
     */
    interface NavMenuItem {
        id: string;
        content: string;
        title?: string;
        isValid?: (workspace: Workspace, perspectiveId?: string) => any;
        isActive?: (worksace: Workspace) => boolean;
        href: () => any;
    }
    /**
     * @class Workspace
     */
    class Workspace {
        jolokia: any;
        jolokiaStatus: any;
        jmxTreeLazyLoadRegistry: any;
        $location: any;
        $compile: ng.ICompileService;
        $templateCache: ng.ITemplateCacheService;
        localStorage: WindowLocalStorage;
        $rootScope: any;
        userDetails: any;
        operationCounter: number;
        selection: NodeSelection;
        tree: Folder;
        mbeanTypesToDomain: {};
        mbeanServicesToDomain: {};
        attributeColumnDefs: {};
        treePostProcessors: any[];
        topLevelTabs: NavMenuItem[];
        subLevelTabs: any[];
        keyToNodeMap: {};
        pluginRegisterHandle: any;
        pluginUpdateCounter: any;
        treeWatchRegisterHandle: any;
        treeWatcherCounter: any;
        treeElement: any;
        mapData: {};
        constructor(jolokia: any, jolokiaStatus: any, jmxTreeLazyLoadRegistry: any, $location: any, $compile: ng.ICompileService, $templateCache: ng.ITemplateCacheService, localStorage: WindowLocalStorage, $rootScope: any, userDetails: any);
        /**
         * Creates a shallow copy child workspace with its own selection and location
         * @method createChildWorkspace
         * @param {ng.ILocationService} location
         * @return {Workspace}
         */
        createChildWorkspace(location: any): Workspace;
        getLocalStorage(key: string): any;
        setLocalStorage(key: string, value: any): void;
        loadTree(): void;
        /**
         * Adds a post processor of the tree to swizzle the tree metadata after loading
         * such as correcting any typeName values or CSS styles by hand
         * @method addTreePostProcessor
         * @param {Function} processor
         */
        addTreePostProcessor(processor: (tree: any) => void): void;
        maybeMonitorPlugins(): void;
        maybeUpdatePlugins(response: any): void;
        maybeReloadTree(response: any): void;
        folderGetOrElse(folder: any, value: any): any;
        populateTree(response: any): void;
        private enableLazyLoading(folder);
        /**
         * Returns the hash query argument to append to URL links
         * @method hash
         * @return {String}
         */
        hash(): string;
        /**
         * Returns the currently active tab
         * @method getActiveTab
         * @return {Boolean}
         */
        getActiveTab(): NavMenuItem;
        private getStrippedPathName();
        linkContains(...words: String[]): boolean;
        /**
         * Returns true if the given link is active. The link can omit the leading # or / if necessary.
         * The query parameters of the URL are ignored in the comparison.
         * @method isLinkActive
         * @param {String} href
         * @return {Boolean} true if the given link is active
         */
        isLinkActive(href: string): boolean;
        /**
         * Returns true if the given link is active. The link can omit the leading # or / if necessary.
         * The query parameters of the URL are ignored in the comparison.
         * @method isLinkActive
         * @param {String} href
         * @return {Boolean} true if the given link is active
         */
        isLinkPrefixActive(href: string): boolean;
        /**
         * Returns true if the tab query parameter is active or the URL starts with the given path
         * @method isTopTabActive
         * @param {String} path
         * @return {Boolean}
         */
        isTopTabActive(path: string): boolean;
        /**
         * Returns the selected mbean name if there is one
         * @method getSelectedMBeanName
         * @return {String}
         */
        getSelectedMBeanName(): string;
        /**
         * Returns true if the path is valid for the current selection
         * @method validSelection
         * @param {String} uri
         * @return {Boolean}
         */
        validSelection(uri: string): any;
        /**
         * In cases where we have just deleted something we typically want to change
         * the selection to the parent node
         * @method removeAndSelectParentNode
         */
        removeAndSelectParentNode(): void;
        selectParentNode(): void;
        /**
         * Returns the view configuration key for the kind of selection
         * for example based on the domain and the node type
         * @method selectionViewConfigKey
         * @return {String}
         */
        selectionViewConfigKey(): string;
        /**
         * Returns a configuration key for a node which is usually of the form
         * domain/typeName or for folders with no type, domain/name/folder
         * @method selectionConfigKey
         * @param {String} prefix
         * @return {String}
         */
        selectionConfigKey(prefix?: string): string;
        moveIfViewInvalid(): boolean;
        updateSelectionNode(node: any): void;
        /**
         * Redraws the tree widget
         * @method redrawTree
         */
        redrawTree(): void;
        /**
         * Expand / collapse the current active node
         * @method expandSelection
         * @param {Boolean} flag
         */
        expandSelection(flag: any): void;
        private matchesProperties(entries, properties);
        hasInvokeRightsForName(objectName: string, ...methods: string[]): any;
        hasInvokeRights(selection: NodeSelection, ...methods: string[]): boolean;
        treeContainsDomainAndProperties(domainName: any, properties?: any): boolean;
        private matches(folder, properties, propertiesCount);
        hasDomainAndProperties(domainName: any, properties?: any, propertiesCount?: any): boolean;
        findMBeanWithProperties(domainName: any, properties?: any, propertiesCount?: any): any;
        findChildMBeanWithProperties(folder: any, properties?: any, propertiesCount?: any): any;
        selectionHasDomainAndLastFolderName(objectName: string, lastName: string): boolean;
        selectionHasDomain(domainName: string): boolean;
        selectionHasDomainAndType(objectName: string, typeName: string): boolean;
        /**
         * Returns true if this workspace has any mbeans at all
         */
        hasMBeans(): boolean;
        hasFabricMBean(): boolean;
        isFabricFolder(): boolean;
        isCamelContext(): boolean;
        isCamelFolder(): boolean;
        isEndpointsFolder(): boolean;
        isEndpoint(): boolean;
        isRoutesFolder(): boolean;
        isRoute(): boolean;
        isOsgiFolder(): boolean;
        isKarafFolder(): boolean;
        isOsgiCompendiumFolder(): boolean;
    }
}
declare class Workspace extends Core.Workspace {
}
