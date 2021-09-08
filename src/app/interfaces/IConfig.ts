export interface IConfig {
  "language": string
  "supportedLanguages": string[],
  "hero": IHero,
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

export interface IFooter{
  links: ILink[]
}

export interface ILink{
  "name": string
  "link": string
}


