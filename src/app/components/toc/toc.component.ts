import {AfterViewInit, Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ITocLink} from '../../interfaces/ITocLink';
import {Router} from "@angular/router";

@Component({
  selector: 'app-toc',
  templateUrl: './toc.component.html',
  styleUrls: ['./toc.component.scss']
})
export class TocComponent implements AfterViewInit{
  @Input() links: ITocLink[] = [];
  linksInViewPort: ITocLink[] = [];

  constructor(private router: Router) {}


  ngAfterViewInit() {
    if (this.links.length > 0)
      this.links[0].active = true;

    this.scroll(window.location.hash.replace('#', ''));
  }

  onWindowScroll() {
    this.highlightActiveElement()
  }

  highlightActiveElement(){
    this.linksInViewPort = [];

    this.links.forEach((link: ITocLink) => {
      link.active = false;
      this.checkIfLinkIsInViewPort(link);

      link.subLinks?.forEach((subLink: ITocLink) => {
        subLink.active = false;
        this.checkIfLinkIsInViewPort(subLink);
      })
    });

    if (this.linksInViewPort[0])
      this.linksInViewPort[0].active = true;
  }

  checkIfLinkIsInViewPort(link: ITocLink){
    const element: HTMLElement = document.getElementById(link.destination)!;
    if (this.isElementInViewport(element))
      this.linksInViewPort.push(link);
  }

  isElementInViewport (el: HTMLElement) {
    const rect = el.getBoundingClientRect();
    // console.log(el.id, rect.bottom, rect.top);
    return !(rect.bottom < 0 || rect.top < 0);
  }

  scroll(id: string) {
    const clientToScroll = document.getElementById(id);

    if (clientToScroll)
      clientToScroll.scrollIntoView({behavior: 'smooth'});

    if(history.pushState && id)
      history.pushState(null, '', document.location.href.split('#')[0] + '#' + id);
  }
}
