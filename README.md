# LABORATORIO 4 : Arquitecturas de Servidores de Aplicaciones, Meta protocolos de objetos, Patrón IoC, Reflexión
En este laboratorio, se describe el proceso de construcción de un servidor web en Java utilizando la plataforma de Spring Boot. 
El servidor será capaz de entregar páginas HTML e imágenes tipo PNG. Además, el servidor proporcionará un framework IoC
(Inversion of Control) para la construcción de aplicaciones web a partir de POJOS (Plain Old Java Objects).

El objetivo de este laboratorio es desarrollar un prototipo mínimo que demuestre las capacidades reflexivas de Java y permita cargar un bean (POJO) y derivar una aplicación web a partir de él. El servidor deberá atender múltiples solicitudes no concurrentes.

Durante el desarrollo del prototipo, se utilizará la anotación @RequestMapping para mapear las solicitudes HTTP a los métodos de los POJOs, y se utilizará la anotación @Component para indicar que una clase es un componente que debe ser cargado por el framework IoC.


# Instalación 
## Herramientas 
- [MAVEN](https://maven.apache.org) : Para el manejo de las dependecias. 
- [GIT](https://git-scm.com) : Para el manejo de las versiones.
- [JAVA](https://www.java.com/es/) : Lenguaje de programación manejado.

## Para correr el laboratorio 

+ Se clona el repositorio en una máquina local con el siguiente comando:
  
    ```
  git clone https://github.com/Juc28/AREP_LAB04.git
    ```

+ Entrar al directorio del proyecto con el siguiente comando:
    ```
    cd AREP_LAB04
    ```
+ Compilar el proyecto con el siguiente comando:

  ```
  mvn clean install
    ```
  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/e120fd7d-03c3-4c77-919f-726c71126b80)

+ Para ejecutar el proyecto con el siguiente comando:
    ```
    java -cp target/classes edu.eci.ServerExecuter
    ```
  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/1e654b5b-3a11-48bd-a6ca-29910c8bbf9f)

+ Para ejecutar las pruebas es el siguiente comando:

  ```
  mvn test
    ```
  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/8d4af977-5fa3-48d7-951f-6425faa75420)

# Arquitectura
El servidor utiliza la anotación @Component para identificar clases como componentes que gestionará. La anotación @GetMapping se utiliza para vincular métodos específicos dentro de estos componentes a solicitudes GET para rutas específicas. El método inversionOfControl recorre todas las clases, buscando las que están anotadas con @Component. Para cada clase, busca métodos anotados con @GetMapping y los almacena en un mapa llamado services. La clave del mapa es la ruta especificada en la anotación, y el valor es el método correspondiente.

* El servidor HTTP realiza lo siguiente:

  1. Ejecuta la inversión de control (IoC) para registrar componentes y servicios.
  2. Crea un socket de servidor para escuchar conexiones entrantes en el puerto 35000.
  3. Entra en un bucle para aceptar continuamente conexiones de clientes, procesar solicitudes y enviar respuestas.

* El procesamiento de las solicitudes del cliente se realiza de la siguiente manera:

  1. Lee la solicitud del cliente desde el flujo de entrada.
  2. Extrae el verbo (por ejemplo, GET) y la ruta (por ejemplo, /hola) de la línea de solicitud.
  3. Recibe el método de servicio correspondiente del mapa services basado en la ruta.
  4. Si se encuentra un servicio coincidente, lo invoca y envía la respuesta al cliente. De lo contrario, envía una respuesta para la página de inicio.
  5. Cierra el socket del cliente.
* Puede manejar los siguientes recursos:
  
    **/hola**: Devuelve una simple respuesta de texto.
  
    **/imagen:** Lee un archivo de imagen PNG de una ubicación específica, lo convierte a codificación Base64 y lo envía como una respuesta HTML con la imagen mostrada.
  
    **/html:** Lee un archivo HTML de una ubicación específica y lo envía como una respuesta HTML.


# Pruebas 
* Abre tu navegador web favorito e ingresa la dirección http://localhost:35000 : 
  
   ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/b36e8ca5-83d4-4858-9bc9-fdaec78eaa07)


* Saludo personalizado: Dirígete a http://localhost:35000/hola para obtener un saludo personalizado:

  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/6ad3b97f-8d5f-4071-a186-bfa767fbb6a0)

* Imagen codificada en base64: Dirígete a http://localhost:35000/imagen para ver una imagen codificada en base64:

  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/a7925f39-b666-4e33-aaf8-f223320e2d5a)

  
* Página HTML: Dirígete a http://localhost:35000/html para cargar una página HTML:

  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/b5e947d1-4524-4bc3-87c9-a70dfec74067)

* Pruebas automáticas:

  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/4014b626-a286-4107-942c-b5ebaac35d43)

  ![imagen](https://github.com/Juc28/AREP_LAB04/assets/118181224/cde2975d-02ca-4f71-951c-a9376cb6ccf6)





# Autor 
Erika Juliana Castro Romero [Juc28](https://github.com/Juc28)
