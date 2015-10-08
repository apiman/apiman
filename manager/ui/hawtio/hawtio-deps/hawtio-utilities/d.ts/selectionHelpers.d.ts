/// <reference path="baseHelpers.d.ts" />
declare module SelectionHelpers {
    function selectNone(group: any[]): void;
    function selectAll(group: any[], filter?: (any) => boolean): void;
    function toggleSelection(item: any): void;
    function selectOne(group: any[], item: any): void;
    function sync(selections: Array<any>, group: Array<any>, index: string): Array<any>;
    function select(group: any[], item: any, $event: any): void;
    function isSelected(item: any, yes?: string, no?: string): any;
    function clearGroup(group: any): void;
    function toggleSelectionFromGroup(group: any[], item: any, search?: (item: any) => boolean): void;
    function isInGroup(group: any[], item: any, yes?: string, no?: string, search?: (item: any) => boolean): any;
    function filterByGroup(group: any, item: any, yes?: string, no?: string, search?: (item: any) => boolean): any;
    function syncGroupSelection(group: any, collection: any, attribute?: string): void;
    function decorate($scope: any): void;
}
