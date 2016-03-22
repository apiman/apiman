interface ChecklistConfig {
    done?: boolean;
    iconClass?: string;
    id?: string;
    name?: string;
    path?: string;
    rowClass?: string;
}

interface PopoverConfig extends ng.IScope {
    etarget?: any;
    autocomplete?: any;
    tagArray?: any;
    inputTags?: any;
    addTag?: any;
    tagText?: any;
    inputWidth?: any;
    defaultWidth?: any;
    autocompleteFocus?: any;
    autocompleteSelect?: any;
    placeholder?: any;
}

interface TagInput extends ng.IAttributes {
    placeholder?: any;
}

