export interface ITocLink {
  name: string;
  destination: string;
  active?: boolean;
  subLinks?: ITocLink[];
}
