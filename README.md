# RDT in UDP

Tietoliikenneprotokollat 2 -kurssin tehtäväsuorituksia. Chattisovellus, missä client lähettää viestin serverille. Server vastaa valitun protokollan mukaan. Kaikki java-tiedostot käännetään java-kääntäjällä, jonka jälkeen voidaan ajaa server ja client. Toteuttamiseen on käytetty apuna Kurose & Ross: Computer Networking: A Top-Down Approach lukua 3.4. Lisäksi kurssilla esiteltiin alkupohja virtual socketin toteuttamiseen. Toteutukset sisältävät melko paljon toisteisuutta, mutta tarkoituksena oli harjoitella rdt:n toteuttamista eri menetelmin.

## rdt3

Tässä versiossa client odottaa käynnistämisen jälkeen käyttäjän syötettä. Versioita ei tarvitse valita vaan tarvitsee vain käynnistää Server.java ja Client.java.

## Ack and Nak

Ack and Nak -versiossa client lähettää yhden viestin mikä annetaan clientiä ajettaessa komentoriviltä.

Esimerkiksi:

java Client Hei maailma!

Kun client toteaa tiedonsiirron onnistuneen, soketti suljetaan. rdt3-versiossa chat-sovellus on toteutettu mielekkäämmin ja viestejä voidaan syöttää useita.

Tässä on eri Client-vaihtoehtoja sen mukaan odottaako se lähettämälleen viestille ack, nak vai molempia paketteja. Client-vaihtoehtoa vastaava koodirivi tulee valita ReliabilityLayer.java tiedostosta, ja näin server vastaa joko ack, nak tai molemmilla paketeilla.

Esimerkiksi:

soketti.responseACKAndNak(paketti, bittivirhe);  
// soketti.responseOnlyACK(paketti,bittivirhe);  
// soketti.responseOnlyNAK(paketti,bittivirhe);
