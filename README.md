# SpatialADSB
Progetto del corso di Basi di Dati 2. 


L’obiettivo del nostro progetto è analizzare le rotte del traffico aereo. 

I dati dei tracciati riguardano le informazioni dei velivoli che transitano nei nostri cieli. 
Questi dati verranno salvati inizialmente in un DBMS non relazionale (MongoDB), in modo da permettere 
una memorizzazione più flessibile. In seguito, tramite task asincrono, la parte spaziale dei dati verrà 
salvata un DBMS relazionale spaziale (PostGIS). Il DBMS spaziale verrà utilizzato per eseguire delle query 
spaziali sui dati, rese semplici grazie proprio all’ausilio di PostGIS. Le interrogazioni che verranno eseguite sul
dataset saranno: 
* Analisi dell’inquinamento acustico di determinate zone, ad esempio: 
l’inquinamento presente nei quartieri di Cagliari o nei diversi paesi 
* Numero di sorvoli in un determinato punto in un arco di tempo 


I dati sono ottenuti attraverso un ricevitore per i messaggi ADSB (Automatic Dependent Surveillance Broadcast), 
standard che permette ai velivoli di trasmettere in broadcast informazioni sulla propria posizione 
per coadiuvare il controllo del traffico aereo e la coordinazione tra velivoli.
