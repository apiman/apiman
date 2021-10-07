import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {HeroService} from "../../services/hero/hero.service";
import {TranslateService} from "@ngx-translate/core";
declare const SwaggerUIBundle: any;

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.sass'],
})
export class SwaggerComponent implements OnInit {
  private apiMgmtUiRestUrl = 'https://vagrantguest/pas/apiman';

  constructor(private route: ActivatedRoute,
              private heroService: HeroService,
              private translator: TranslateService) {}

  /**
   * Load the swagger definition and display it with the swagger ui bundle library on component initialization
   */
  ngOnInit() {
    const organizationId = this.route.snapshot.paramMap.get('orgId');
    const apiId = this.route.snapshot.paramMap.get('apiId');
    const apiVersion = this.route.snapshot.paramMap.get('apiVersion');
    this.heroService.setUpHero({
      title: this.translator.instant('COMMON.API_DOCS'),
      subtitle: `${apiId} - ${apiVersion}`
    });

    const isPublicApi = history.state.data
      ? history.state.data.publicAPI
      : false;
    let apiStatus = history.state.data ? history.state.data.apiStatus : null;
    let apiKey = history.state.data ? history.state.data.apikey : null;

    if (!isPublicApi) {
      if (apiStatus) {
        // save status for reloading page
        sessionStorage.setItem('lastApiStatus', apiStatus);
      } else {
        apiStatus = sessionStorage.getItem('lastApiStatus');
      }

      if (apiKey) {
        // save key for reloading page
        sessionStorage.setItem('lastSwaggerApiKey', apiKey);
      } else {
        apiKey = sessionStorage.getItem('lastSwaggerApiKey');
      }
    }

    const swaggerURL =
      this.apiMgmtUiRestUrl +
      `/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`;
    const CheckAllowTryItOutPlugin = () => {
      return {
        statePlugins: {
          spec: {
            wrapSelectors: {
              // Enable TryOut Button only for active APIs
              allowTryItOutFor: () => () => apiStatus === 'Active',
            },
          },
        },
      };
    };

    const DisableAuthorizePlugin = () => ({
      wrapComponents: { authorizeBtn: () => () => null },
    });

    let apiKeySecuritySettingName = 'X-API-Key';

    const swaggerOptions = {
      dom_id: '#swagger-editor',
      layout: 'BaseLayout',
      presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIBundle.SwaggerUIStandalonePreset,
      ],
      plugins: [CheckAllowTryItOutPlugin],
      url: swaggerURL,
      docExpansion: 'none',
      operationsSorter: 'alpha',
      requestInterceptor: (request: any) => {
        if (request.loadSpec) {
          // Fetch the spec using Basic auth, replace "user" and "password" with yours
          request.headers.Authorization = 'Basic ' + btoa('support.is:Sch33rBP44$');

          // or API key
          // req.headers.MyApiKey = 'abcde12345';

          // or bearer token
          // req.headers.Authorization = 'Bearer abcde12345';
        }
        /*        request.headers.Authorization = 'Bearer ' + sessionStorage.getItem('api_mgmt_keycloak_token');
        if (!isPublicApi) {
          const url = new URL(request.url);
          // set api key to authorize all api requests also for option requests
          url.searchParams.append('apiKey', apiKey);
          request.url = url.toString();
        }*/
        return request;
      },
      responseInterceptor: (response: any) => {
        if (response.url === swaggerURL) {
          // determine apiKeySecuritySettingName by request of getting swagger definition
          if (response.body && response.body.securityDefinitions) {
            const securityDefinitions = Object.values(
              response.body.securityDefinitions
            );
            // @ts-ignore
            const apiKeySecuritySetting = securityDefinitions.find(
              (securitySetting: any) => securitySetting.type === 'apiKey'
            );
            if (apiKeySecuritySetting) {
              // @ts-ignore
              // overwrite the default api key name
              apiKeySecuritySettingName = apiKeySecuritySetting.name;
            }
          }
        }
      },
      onComplete: () => {
        // set api key in swagger view to make it look like authorized for calls
        // this option is not enough to send OPTION requests to gateway protected apis
        // sending this api key as header in option requests are blocked by the browser
        swaggerUI.preauthorizeApiKey(apiKeySecuritySettingName, apiKey);
      },
    };
    if (apiStatus === 'Inactive') {
      // @ts-ignore
      swaggerOptions.plugins.push(DisableAuthorizePlugin);
    }

    const swaggerUI = SwaggerUIBundle(swaggerOptions);
  }
}
