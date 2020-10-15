# Api Management - Developer Portal

This project was generated with [Angular CLI](https://github.com/angular/angular-cli).

## Development server

```json
export const environment = {
  production: false,
  apiMgmtRealm: 'PAS',
  apiMgmtUiRestUrl: 'https://vagrantguest/pas/apiman',
  keycloakAuthUrl: 'https://vagrantguest/pas/keycloak'
};
```

* Edit `src/environments/environment.ts` if needed
* Edit `src/index.html` and set the base tag to `<base href="/pas/devportal/">`
* Run `vagrant --local-services=api-management-devportal up`
* Run `ng serve` for a dev server. Navigate to `http://localhost:80/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

### Build docker image locally

To build the docker image locally you have to run the following command or use Intellij run configurations:

`docker build -t gitlab.scheer-group.com:8080/api-mgmt/devportal:latest .`

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
