import {Component, OnInit} from '@angular/core';
import {Api} from "../../interfaces/api";

@Component({
  selector: 'app-marketplace-signup-stepper',
  templateUrl: './marketplace-signup-stepper.component.html',
  styleUrls: ['./marketplace-signup-stepper.component.sass']
})
export class MarketplaceSignupStepperComponent implements OnInit {

  // ToDo replace fixed api mockData with backendCall
  api: Api = {
    id: "2",
    title: "Lorem ipsum dolor",
    shortDescription: "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
    longDescription: "# Fere in tibi de aula actusque\n" +
      "\n" +
      "## Circuitu gurgite dum inmensum litora pater quid\n" +
      "\n" +
      "Lorem markdownum Ismenis est pares videt tacito; tibi gestit senem *unda*.\n" +
      "Consumpserat dedit retinentibus recepta venisset digitis. Miratur commenta suae,\n" +
      "venatrixque notitiam.\n" +
      "\n" +
      "> **Tamen tanta retardat** et inane vivere tremescere laetus, erat et virisque\n" +
      "> iussa, non nisi humano humum factas praeceps. Amissum palmae. Apri vellent\n" +
      "> Cadmo, *cremabit* haruspex liquefacta ferrum.\n" +
      "\n" +
      "[Ungues de](http://est-nato.io/desierim-illa) ne petiere fuit. **Mora** cur\n" +
      "altera Pallados [suarum quae](http://alii-iungitur.io/animo) colla; e, cava tela\n" +
      "motae lacrimas: pendens vultus fateri.\n" +
      "\n" +
      "## Volantes vulnus defendentia divis viscera\n" +
      "\n" +
      "*Nec nescio* modo, circum doluit tetigere ille ingemuit refovet, vallis dant, in\n" +
      "fretum cumque Laurentes dabis procul. Crescente *sinitis*. *Fossa* siccis neci\n" +
      "nullo monstro Saeculaque sororem ipsaque concubitus pecudesque vidit clipeo\n" +
      "pruniceum, cuncta [Castrumque](http://www.hanc-opus.net/) in amat. Et integer\n" +
      "inducit Cadmus robora lumina *nitor*; que miser.\n" +
      "\n" +
      "- Amorum mitia quaerit\n" +
      "- Mater haec cum trans nemoris feriente vox\n" +
      "- Potuisset nec\n" +
      "- Ignis optat tollens gaudebat\n" +
      "- Haec soli creditur pressam",
    icon: "data:image/png;base64,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",
    featuredApi: true
  };

  features: string[] = ['fast', 'free', 'fancy features'];
  markdown: string = '# In inmotae messes et tamen sustinuit fieri\n' +
    '\n' +
    '## Iam nataque prospexit\n' +
    '\n' +
    'Lorem markdownum accingere levatae inspicitur leve Nereides offensa *renidenti\n' +
    'deflent illis* licet operum sonitum descendere multo, errorem nec ille. Monstra\n' +
    'superest, si negat diebus alimenta vibrantia aderas loquendo in nec **bicorni**\n' +
    'advena dixit herosmaxime adpulit arduus dum!\n' +
    '\n' +
    '1. Cristis vetus totumque celeremque creber sacris et\n' +
    '2. Care membra positis centum\n' +
    '3. Sparsitque Bacchus\n' +
    '4. Ausim dixi vittam orbem\n' +
    '5. Tempora pars mutua Oceano soporiferam Iove imis\n' +
    '6. Quem metus\n' +
    '\n' +
    'Se vitae, sede visus illa, virgaque amnis matris protinus parentis se furor\n' +
    'quotiens, tempora. Nec iuro et fieri et flere redit opta arma et quidem\n' +
    '[plurimus](http://negat.org/) cuius. Palustris Bromumque transformat tu virtus\n' +
    'tritumque armat formam iube varias crudelesque dedit. Supplex ipse, omnes neque\n' +
    'Haemoniam ipsoque sanguine mox suppressa montes. Cervos renuente, pavor aere\n' +
    'durat revocata?\n' +
    '\n' +
    '    var ios = -2;\n' +
    '    fileItunesSchema = software;\n' +
    '    cold_meta_wimax += boxExpansion - ribbonWord * version_ripcording_os(\n' +
    '            languageHyperlinkStatus);\n' +
    '\n' +
    '## De pastor natos refluum\n' +
    '\n' +
    'Et ora, est et moderere intra procorum, suam ut instare nam. Obstipuere\n' +
    '**secundi vertice**. Levavit cum molitur caput dederat lumina.\n' +
    '\n' +
    '1. Ibat pependit auguris quid\n' +
    '2. Lupos vietum\n' +
    '3. Rerum sumptis percusso texitur ferrum novitatis quodque\n' +
    '\n' +
    'Producet fluentia prope Titania extenuatur Arethusa corpora *tela geras reddita*\n' +
    'fallaces sola tibi et admonitu, formosior. Suos reduxit *supremo* amicius!\n' +
    '\n' +
    '> Regione magorum iuvenis latosque coeunt: necis enim,\n' +
    '> [Theseu](http://www.exsiccata-vultu.net/) Arethusae vincat *altera carcere*\n' +
    '> vetito tenetque regales, leonem? Praestiteris alas: sagitta satis sua retexit\n' +
    '> restare amore aliquis montesque. Quoque egressa exhalat inque; si Procris?\n' +
    '\n' +
    'Inplevere maior. Ubi aut supplicium Averna, ciet percussis Praebuimus saepe\n' +
    'fugisse quae est: a.';

  constructor() { }

  ngOnInit(): void {
  }

}
