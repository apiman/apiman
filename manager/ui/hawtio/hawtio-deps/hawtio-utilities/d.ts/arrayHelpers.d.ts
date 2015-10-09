/// <reference path="includes.d.ts" />
declare module ArrayHelpers {
    /**
     * Removes elements in the target array based on the new collection, returns true if
     * any changes were made
     */
    function removeElements(collection: Array<any>, newCollection: Array<any>, index?: string): boolean;
    /**
     * Changes the existing collection to match the new collection to avoid re-assigning
     * the array pointer, returns true if the array size has changed
     */
    function sync(collection: Array<any>, newCollection: Array<any>, index?: string): boolean;
}
