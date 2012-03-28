# spatial-news-tracker
Verktøy for å lagre forsiden til VG.no med tilhørende bilder og CSS. I tillegg blir artikkel-url'ene som det linkes til på forsiden lagret i en sqlite fil for at en senere kan finne ut hvilke forsider den aktuelle saken fremtrer på.

Det er tiltenkt at det skal være mer generisk enn kun til bruk på VG.no, men det er funksjonalitet som ligger litt på vent.


## Bruk


Programmet er delt opp i to deler, den ene delen ([last ned](https://github.com/downloads/maads/spatial-news-tracker/snt.jar)) sørger for å hente ned endringer på forsiden av nettavisen. Den andre delen ([last ned](https://github.com/downloads/maads/spatial-news-tracker/snt-finder.jar)) muliggjør å hente ut artikler som finnes på forsidene lastet ned av den andre. Del en må ha kjørt før del to vil kunne gi et resultat. For å gjøre det lett å kjenne igjen den artikkelen som har blitt valgt, har vi i denne omgang valg å gi den en gul bakgrunn. På senere tidspunkt ser vi for oss at det blir en valgfri farge, eller en annet måte å markere artikkelen på. De endrede forsidene blir lagret i en mappe kalt "output". En kan velge om en vil kopiere bilder og andre filer fra sin originale plassering over i denne mappen. Fordelen med det er at en lett kan ta med seg hele mappen om en vil arbeide med disse filene fra en annen datamaskin eller annen plassering på datamaskinen. Men som sagt, dette er valgfritt.

Bruker bibliotekene [SQLite](http://www.zentus.com/sqlitejdbc/) og [JSoup](http://jsoup.org/)

## Lisens

Copyright (C) 2012 Mads Tordal & contributors.

Distributed under the Eclipse Public License.
