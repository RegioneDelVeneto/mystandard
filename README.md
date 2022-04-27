
# MyStandard


## INTRODUZIONE

La soluzione MyStandar è composta dalle seguenti componenti:

 - L'**applicazione MyStandard App** orientata agli operatori sia degli Enti Locali EELL ( proponenti degli Standard ) sia dell'Ente Standardizzatore.L'applicazione prevede sia un'area pubblica per la consultazione dei cataloghi delle entità e delle loro relazioni in modalità non autenticata sia una modalità autenticata attraverso il quale gli operatori possono in base al proprio ruolo svolgere diverse funzioni.  Si riporta a titolo esemplificativo lo use case con le funzionalità.
![usecase](https://github.com/RegioneDelVeneto/mystandard/blob/main/usecase.png)
 
	- Gli utenti con il ruolo di **Operatore EE.LL** o **Operatore EC** posssono:
	 	- Inserire Nuovi Entità ( Proporre un nuovo standard ) 
	 	- Modificare le Entità Esistenti ( se non ancora approvate come standard )
	 	- Cancellare gli standard non approvati ( se inseriti da Operatori dello stesso ente )
 	- In aggiunta alle funzioni degli Operatori, gli utenti **Responsabili di Dominio** possono:
	 	- Accettare uno standard inserito come "Pubblicato Ente"
	 	- Tramesttere uno standard in stato "Pubblicato Ente" per l'approvazione come standard da parte dell'Ente Centrale
 	- I **Responsabili degli Standard** sull'Ente Standardizzatore in aggiunta ai ruoli precedenti possono:
	 	- Pubblicare come "Standard" un'entità trasmessa
	 	- Rigettare come "Standard" un'entità e richiedere un'integrazione / nuova versione
	
 - **Il catalogo pubblico** pubblicato sul portale dell'ente starndardizzatore nella sezione MyStandard / Servizi. Questa sezione è gestita direttamente attraverso i modulo MyCMS nella redazione del portale MyPortal per l'ente standardizzatore.
 
 - **L'integrazione dell'applicazione sulla MyIntranet** gestita attraverso una voce specifica di Menu specifica sull'applicativo MyIntranet che permette l'accesso all'applicativo MyStandard agli operatori direttamete dalla MyIntranet mantenendo quindi coerente il modello della piattaforma MyPortal 3 che prevede che tutti gli applicativi di Back Office siano raggiungibili dalla MyIntranet


## STRUTTURA DEL REPOSITORY
Il repository git di MyStandard ha le seguente struttura:

**/mystandard.sources**: E’ la cartella che contiene i sorgenti e gli script gradle per la compilazione la creazione dell’immagine e la pubblicazione sul Repository Nexus.

**/mystandard.deploy:** E’ la cartella che contiene i descrittori di base per il dispiegamento su kubernets e gli overlay specifici per ogni ambiente target di deploy.


## I SORGENTI
La cartella **mystandard.sources*** contiene i sorgenti dell'applicazione che   ed è così strutturata:

`mystandard-be`: contiene i sorgenti del back-end 
`mystandard-fe`: contiene i sorgenti del front-end


## Esecuzione in modalità standalone
Per l'esecuzione in modalità standalone è necessario generare il file jar dopo aver aggiornato i file di confugurazione

### Prerequisiti Infrastrutturali
La soluzione MyStandard necessità di alcuni requisiti Infrastrutturali:

- **Redis** : Per la gestione delle sessioni
- **MongoDB** : Per la gestione e il salvataggio delle query semantiche preconfigurate e/o predisposte dagli operratori
- **CEPH (Object Store)**: MyStandard utilizza bucket dedicati per cui può utilizzare anche un’istanza di MyBox separata dagli altri servizi MyPlace.
- **Fuseki (Persistent Store RDF)**: Richiesta la sua installazione in questo progetto
- **LogStash**: Per i log applicativi
- **ELK**: Per le ricerche full text

In caso di installazione K8s ( si veda il docuemnto MI ) sono necessari anche:

- **Cluster K8s** :  Kubernetes (1.7.2 +) con support di container Docker (1.12.x +)
- **Repository Nexus** : Per la pubblicazione di artefatti e immagini docker una volta compilati i sorgenti


### Prerequisiti servizi MyPlace e verticali MyP3
La soluzione MyStandard ha dipendenza verso alcuni servizi della piattaforma MyPlace:

- **MyId**: Per al gestione dell’autenticazione
- **MyProfile**: Per la gestione delle autorizzazioni e dei profili
- **MySearch**: Per le ricerche full text


Mystandard eroga ( opzionalmente ) alcune delle funzionalità attraverso API per   i verticali:

- **MyIntranet**: Per l’accesso all’applicazione MyStandard da parte degli operatori degli Enti direttamente attraverso la MyIntranet.

- **MyPortal**: Per la pubblicazione e la consultazione pubblica dei cataloghi direttamente da MyPortal

MyStandard deve quindi essere raggiungibile a livello di rete da queste due applicazioni.

E’ richiesta inoltre la comunicazione verso i componenti:

- myportal-ro
- myportal-rw
- myintranet-rw
- myintranet-ro

per l’ente MyPortal dedicato all’applicazione MyExtranet.

### Configurazione MyStandard
Per poter eseguire lo start dell’applicazione, è importante inserire i puntamenti dei propri ambienti per i componenti da cui dipende MyStandard.


Nel caso si voglia utilizzare un nexus per scaricare o pubblicare pacchetti, modificare il file settings.gradle all’interno della directory mystandard.sources e modificare:

- parametri gradle.ext.* con i parametri del nexus a cui si vuole puntare

Se si vuole far partire l’applicazione nei propri ambienti, è necessario aggiungere le seguenti configurazioni:

- `mystandard-be/src/main/resources/application.yml`
	- mystandard.owl.filename.\*: indicare il path dove trovare i file
	- mystandard.myBoxConfigurationFilePath*: indicare il path dove trovare il file
	- mystandard.stateMachine: indicare il path dove trovare il file
- `mystandard-be/src/main/resources/application-default.yml`:
	- server.ssl.*: se si vuole abilitare HTTPS per Spring Boot
	- auth.fake.enabled: se il parametro è a true, si esegue un’autenticazione fake senza invocare MyID
	- saml.proxy.*: per la connessione a MyID, se si è in presenza di Load Balancer o Reverse Proxy
	- saml.key-*: keystore con la chiave per firmare asserzioni SAML
	- saml.app-entity-id: Nome dell'applicazione nella configurazione per l'IDP
	- saml.app-base-url: URL dell'applicazione da fornire nel processo di generazione dei metadati per MyID
	- saml.idp-metadata-url: URL da cui ricavare i metadata IDP
	- jwt.secret: chiave con cui firmare i JWT
	- myprofile.baseUrl: url di MyProfile
	- myprofile.fake: se il parametro è true, non si contatta MyProfile ma si prendono le configurazioni di test del file test_fake_users.json
	- mysearch.hosts: url in formato <host>:<port>:<protocol> da utilizzare per contattare mySearch
	- mysearch.entitiesMappingConfigurationAbsolutePath:  indicare il path dove trovare il file
	- logstash.*: riferimenti per contattare logstash
	- mystandard.enteNazionale: nome ente standardizzatore
	- mystandard.fuseki-server-url: Url in cui contattare Fuseki
	- mongodb.*: Riferimenti per contattare MongoDB
	- cache.sentinel.*: Riferimenti di Redis se si usa la modalità sentinel
	- cache.standalone.*: Riferimenti di Redis se si usa la modalità normale
- `mystandard-be/src/main/resources/mybox.properties`
	- s3.*: indicare i riferimenti a s3 se si usa s3 come Object Store
	- swift.*: indicare i riferimenti a SWIFT se si usa SWIFT come Object Store


### Creazione del file jar
Per eseguire la compilazione dei sorgenti e l’esecuzione degli stessi in un unico comando eseguire il comando

    ./gradlew bootJar

che genera il file jar nella cartella /lib.
Nel caso di ambiente Microsoft Windows, eseguire il comando

    gradlew.bat bootRun

### Esecuzione dell'applicazione
Per mettere in esecuzione l’applicativo, posizionarsi nella directory dove l’artefatto è stato generato, ed eseguire il comando di

    java -jar <nome artefatto>.jar

L'applicazione è raggiungibile ad indirizzi del tipo:

 - https://localhost/mystandard
 
## Esecuzione su cluster kubernetes
Per l'esecuzione su cluster kubernetes si rmanda al manuale di istallazione presente sotto documentazione.



