import { Component, OnInit } from '@angular/core';
import { HeroService } from '../../services/hero/hero.service';
import { TranslateService } from '@ngx-translate/core';
import {
  BackendService,
  SearchCriteriaBean,
} from '../../services/backend/backend.service';
import { ISection } from '../../interfaces/ISection';
import { statusColorMap } from '../../interfaces/IStatus';
import { zip } from 'rxjs';

@Component({
  selector: 'app-my-apps',
  templateUrl: './my-apps.component.html',
  styleUrls: ['./my-apps.component.scss'],
})
export class MyAppsComponent implements OnInit {
  apps: any[] = [];

  tmpUrl = 'https://pbs.twimg.com/media/Ez-AaifWYAIiFSQ.jpg';
  tmpUrl2 =
    'https://cdn0.iconfinder.com/data/icons/customicondesignoffice5/256/examples.png';

  constructor(
    private heroService: HeroService,
    private translator: TranslateService,
    private backend: BackendService
  ) {
    this.initMockApps();
  }

  ngOnInit(): void {
    this.setUpHero();

    // this.backend.searchApis()
  }

  private setUpHero() {
    this.heroService.setUpHero({
      title: this.translator.instant('APPS.TITLE'),
      subtitle: this.translator.instant('APPS.SUBTITLE'),
    });
  }

  private getDescription() {
    return 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et';
  }

  private getApis() {
    return [
      {
        name: 'My First API',
        version: 'v2',
        plan: 'Sandbox Plan 1',
        policies: this.getPolicies(),
        labels: this.getLabels(),
        section: 'summary',
      },
      {
        name: 'My Second API',
        version: 'v4',
        plan: 'Silver Plan',
        policies: this.getPolicies(),
        labels: this.getLabels(),
        section: 'use-api',
      },
    ];
  }

  private initMockApps() {
    for (let i = 1; i < 6; i++) {
      this.apps.push({
        name: `App ${i}`,
        description: this.getDescription(),
        apis: this.getApis(),
      });
    }
  }

  private getPolicies() {
    return [
      {
        name: 'Example Policy 1',
        icon: 'analytics',
      },
      {
        name: 'Example Policy 2',
        icon: 'analytics',
      },
      {
        name: 'Example Policy 3',
        icon: 'analytics',
      },
    ];
  }

  private getLabels() {
    return [
      {
        name: 'Ready',
        color: statusColorMap.get('Ready'),
      },
      {
        name: 'Retired',
        color: statusColorMap.get('Retired'),
      },
      {
        name: 'Created',
        color: statusColorMap.get('Created'),
      },
    ];
  }

  setSection(api: any, sectionName: ISection) {
    api.section = api.section === sectionName ? api.section : sectionName;
  }
}
