# FaunaSV_APP.

FaunaSV APP es una aplicación android que utiliza Retrofit, Google Maps API y se conecta a una REST API basada en GeoJango y PostGis,
que tiene el objetivo de poder facilitar el registro de avistamientos de manera colaboratiba de la fauna de El Salvador. 

El objetivo de la APP es permitirles a los usuarios tener una mejor idea de los lugares en que las diferentes especies de animales 
habitan, para así poder saber a qué lugar tienen que viajar para tener la mayor oportunidad de documentar las especies que ellos deseen. 

Requisitos.
-
    -Android 5.0 o superior.
    
Permisos.
-
    -Permiso de GPS.
    -Permiso de escritura y lectura de archivos.
    -Permiso de instalación de aplicaciones de terceros.
    
    
Pasos para su instalación.
-
Para poder instalar la aplicación sera necesario tener el archivo APK de esta en su dispositivo móvil, navegar en su dispositivo movil, 
por medio de un file managuer, hasta la locación de la APK y correr la APK. Notese que se necesitaran permiso especial de instalación 
de aplicaciones de terceros.

Consideraciones.
-

* La funcionalidad de la ventana de registro no tiene funcionalidad en este momento, se puede revisar la documentacion de [Retrofit](https://square.github.io/retrofit/)
para obtener más información de como habilitarlo.
* La funcionalidad de agregar un avistamiento desde la APP no esta funcionando, esta se hace por medio de un form de un navegador web, esto
se debe a un problema de Retrofit con Django, consultar la documentacion de @Multipart de [Retrofit](https://square.github.io/retrofit/) y
su posible comunicación con Django.

Reconocimiento a liberias implementadas
-
* Retrofit:
  Copyright (c) Apache License Version 2.0, January 2004
* Google Maps API:
  Copyright (c) Apache License Version 2.0, January 2004
