/// <reference path="angular-route.d.ts"/>
declare module HawtioMainNav {

    var pluginName: string;

    interface IActions {
        ADD: string;
        REMOVE: string;
        CHANGED: string;
    }
    var Actions: IActions;

    interface Registry {
        builder(): NavItemBuilder;
        add(item: NavItem, ...items: NavItem[]): any;
        remove(search: (item: NavItem) => boolean): NavItem[];
        iterate(iterator: (item: NavItem) => void): any;
        on(action: string, key: string, fn: (item: any) => void): void;
        selected(): NavItem;
    }

    interface DefaultPageRanking {
      rank: number;
      isValid: (yes: () => void, no: () => void) => void;
    }

    /* These are gonna get deprecated */
    interface WelcomePage {
      rank: number;
      isValid?: () => boolean;
      href: () => string;
    }

    interface WelcomePageRegistry {
      pages: Array<WelcomePage>;
    }
    /* End These are gonna get deprecated */

    interface BuilderFactory {
        create(): NavItemBuilder;
        join(...paths:string[]):string;
        configureRouting($routeProvider: ng.route.IRouteProvider, tab: NavItem): any;
    }

    interface AttributeMap {
      [name:string]: string;
    }

    interface NavItem {
        id: string;
        rank?: number;
        page?: () => string;
        reload?: boolean;
        context?: boolean;
        title?: () => string;
        tooltip?: () => string;
        href?: () => string;
        click?: ($event: any) => void;
        isValid?: () => boolean;
        show?: () => boolean;
        isSelected?: () => boolean;
        template?: () => string;
        tabs?: NavItem[];
        defaultPage?: DefaultPageRanking;
        attributes?: AttributeMap;
        linkAttributes?: AttributeMap;
        [name:string]: any;
    }

    interface NavItemBuilder {
        id(id: string): NavItemBuilder;
        defaultPage(defaultPage: DefaultPageRanking): NavItemBuilder;
        rank(rank: number): NavItemBuilder;
        reload(reload: boolean): NavItemBuilder;
        page(page: () => string): NavItemBuilder;
        title(title: () => string): NavItemBuilder;
        tooltip(tooltip: () => string): NavItemBuilder;
        context(context: boolean): NavItemBuilder;
        attributes(attributes:AttributeMap): NavItemBuilder;
        linkAttributes(attributes:AttributeMap): NavItemBuilder;
        href(href: () => string): NavItemBuilder;
        click(click: ($event: any) => void): NavItemBuilder;
        isValid(isValid: () => boolean): NavItemBuilder;
        show(show: () => boolean): NavItemBuilder;
        isSelected(isSelected: () => boolean): NavItemBuilder;
        template(template: () => string): NavItemBuilder;
        tabs(item: NavItem, ...items: NavItem[]): NavItemBuilder;
        subPath(title: string, path: string, page?: string, rank?: number, reload?: boolean, isValid?: () => boolean): NavItemBuilder;
        build(): NavItem;
    }

    interface ICreateRegistry {
        (): Registry;
    }

    var createRegistry: ICreateRegistry;
    interface ICreateBuilder {
        (): NavItemBuilder;
    }

    var createBuilder: ICreateBuilder;

}

declare module HawtioPerspective {

  interface Selector {
    content?: string;
    id?: string;
    href?: string;
    title?: string;
    onCondition?: () => boolean;
  }

  interface TabMap {
    includes: Array<Selector>;
    excludes: Array<Selector>;
  }

  interface PerspectiveLabel {
    id: string;
    label: string;
    icon: any;
  }

  interface Perspective {
    label: string;
    icon: any;
    lastPage: string;
    isValid: () => boolean;
    tabs: TabMap;

  }

  interface Registry {
    add(id:string, perspective:Perspective):void;
    remove(id:string):Perspective;
    setCurrent(id:string):void;
    getCurrent():Perspective;
    getLabels():PerspectiveLabel[]
  }

}
