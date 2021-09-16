export interface Plan {
  id: string;
  title: string;
  subtitle: string;
  policies: {
    title: string;
    configuration: string;
  }[];
}
