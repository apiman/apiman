export interface IConfig {
  "language": string
  "supportedLanguages": string[],
  "hero": IHero,
  "navigation": INavigation,
  "footer": IFooter
}

export interface IHero {
  title: string;
  subtitle: string;
  heroImgUrl: string;
  large: boolean;
  fontColor: string;
  overlayColor: string;
}

export interface INavigation{
  links: ILink[],
  seperator: string
}

export interface IFooter{
  links: ILink[],
  seperator: string
}

export interface ILink{
  "name": string
  "link": string,
  "openInNewTab": boolean
}


