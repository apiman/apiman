import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HeroService} from '../../services/hero/hero.service';
import {TranslateService} from '@ngx-translate/core';
import {ConfigService} from '../../services/config/config.service';
import SwaggerUI from 'swagger-ui';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.sass']
})
export class SwaggerComponent implements OnInit {
  endpoint = '';

  constructor(
    private route: ActivatedRoute,
    private heroService: HeroService,
    private translator: TranslateService,
    private config: ConfigService
  ) {
    this.endpoint = config.getEndpoint();
  }

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

    const swaggerURL = `${this.endpoint}/devportal/organizations/${organizationId}/apis/${apiId}/versions/${apiVersion}/definition`;

    const swaggerOptions: SwaggerUI.SwaggerUIOptions = {
      dom_id: '#swagger-editor',
      layout: 'BaseLayout',
      url: swaggerURL,
      docExpansion: 'list',
      operationsSorter: 'alpha',
      tryItOutEnabled: false,
      requestInterceptor: (request: any) => {
        return request;
      },
      responseInterceptor: (response: any) => {
        return response;
      },
      onComplete: () => {}
    };

    // Loads the swagger ui with its options
    SwaggerUI(swaggerOptions);
  }
}
