# spatial-news-tracker
Verktøy for å lagre forsiden til VG.no med tilhørende bilder og CSS. I tillegg blir artikkel-url'ene som det linkes til på forsiden lagret i en sqlite fil for at en senere kan finne ut hvilke forsider den aktuelle saken fremtrer på.

Det er tiltenkt at det skal være mer generisk enn kun til bruk på VG.no, men det er funksjonalitet som ligger litt på vent.


## Bruk

En kjørbar fil kan [lastes ned](https://github.com/downloads/maads/spatial-news-tracker/snt.jar), slik at du kan se selv hvordan det fungerer uten at du trenger å kompilere kildekoden.

Bruker bibliotekene [SQLite](http://www.zentus.com/sqlitejdbc/) og [JSoup](http://jsoup.org/)

Lag kjørbar jar-fil. Kjør denne fra console:

java -jar filnavn.jar

Sjekker vg.no hvert 30 sekund ved kjøring uten GUI. Ved bruk av GUI kan en velge selv hvilket intervall som skal brukes.

## Lisens

Copyright (C) 2012 Mads Tordal & contributors.

Distributed under the Eclipse Public License.
