import {Component, Inject, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

declare const SwaggerUIBundle: any;

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.scss']
})
export class SwaggerComponent implements OnInit {

  constructor(@Inject('API_MGMT_UI_REST_URL') private apiMgmtUiRestUrl: string, private route: ActivatedRoute) { }

  /**
   * Load the swagger definition and display it with the swagger ui bundle library on component initialization
   */
  ngOnInit() {
    const developerId = this.route.snapshot.paramMap.get('developerId');
    const organizationId = this.route.snapshot.paramMap.get('orgId');
    const apiId = this.route.snapshot.paramMap.get('apiId');
    const version = this.route.snapshot.paramMap.get('version');

    let apiKey = history.state.data ? history.state.data.apikey : null;
    const apiStatus = history.state.data ? history.state.data.apiStatus : null;

    if (apiKey) {
      // save key for reloading page
      sessionStorage.setItem('lastSwaggerApiKey', apiKey);
    } else {
      apiKey = sessionStorage.getItem('lastSwaggerApiKey');
    }

    const swaggerURL = this.apiMgmtUiRestUrl
      + '/developers/' + developerId
      + '/organizations/' + organizationId
      + '/apis/' + apiId + '/versions/'
      + version + '/definition';

    const CheckAllowTryItOutPlugin = () => {
      return {
        statePlugins: {
          spec: {
            wrapSelectors: {
              // Enable TryOut Button only for active APIs
              allowTryItOutFor: () => () => apiStatus === 'Active'
            }
          }
        }
      };
    };

    const swaggerUI = SwaggerUIBundle({
      dom_id: '#swagger-editor',
      layout: 'BaseLayout',
      presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIBundle.SwaggerUIStandalonePreset
      ],
      plugins: [
        CheckAllowTryItOutPlugin
      ],
      url: swaggerURL,
      docExpansion: 'none',
      operationsSorter: 'alpha',
      onComplete: () => {
        // set api key in swagger view to make it look like authorized for calls
        // this option is not enough to send OPTION requests to gateway protected apis
        // sending this api key as header in option requests are blocked by the browser
        swaggerUI.preauthorizeApiKey('X-API-Key', apiKey);
      },
      requestInterceptor: (request) => {
        if (request.url === swaggerURL) {
          // set bearer token for authentication to get swagger file
          request.headers.Authorization = 'Bearer ' + sessionStorage.getItem('api_mgmt_keycloak_token');
        } else {
          // set api key to authorize all api requests also for option requests
          request.url += '?apiKey=' + apiKey;
        }
        return request;
      }
    });
  }

}
