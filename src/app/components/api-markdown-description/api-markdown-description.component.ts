import { Component, Input, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-api-markdown-description',
  templateUrl: './api-markdown-description.component.html',
  styleUrls: ['./api-markdown-description.component.scss']
})
export class ApiMarkdownDescriptionComponent implements OnInit {
  @Input() markdownText = '';

  constructor(private translator: TranslateService) {}

  ngOnInit(): void {
    if (!this.markdownText) {
      this.markdownText = this.translator.instant(
        'API_DETAILS.NO_EXT_DESCRIPTION'
      ) as string;
    }
  }
}
