package edu.eci;

import edu.eci.IoC.Component;
import edu.eci.IoC.GetMapping;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

public class HttpServer {
    public static File file;
    public static PrintWriter out;
    public static Map<String, Method> services = new HashMap<>();
    public static final String pathToClasses = "edu/eci/IoC";
    private static HttpServer instance = new HttpServer();

    private HttpServer() {
    }

    public static HttpServer getInstance() {
        return instance;
    }

    public void start(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        inversionOfControl();
        ServerSocket serverSocket = createServerSocket();
        boolean running = true;
        while (running) {
            Socket clientSocket = acceptClientConnection(serverSocket);
            processClientRequest(clientSocket);
        }
        serverSocket.close();
    }


    /**
     * Crea un socket de servidor que escucha en el puerto 35000.
     * @return  El socket de servidor creado, o null si se produce un error.
     */
    private ServerSocket createServerSocket() {
        try {
            return new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
            return null;
        }
    }
    /**
     * Acepta una conexión entrante de un cliente en el socket de servidor especificado.
     * @param serverSocket  El socket de servidor que está esperando conexiones.
     * @return El socket conectado al cliente, o null si se produce un error.
     */
    private Socket acceptClientConnection(ServerSocket serverSocket) {
        try {
            System.out.println("Listo para recibir ...");
            return serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
            return null;
        }
    }

    /**
     * Procesa una solicitud del cliente recibida a través del socket especificado.
     * @param clientSocket  El socket conectado al cliente.
     * @throws IOException          Si se produce un error de E/S durante la comunicación.
     * @throws InvocationTargetException  Si se produce un error al invocar el método {@code handleClientRequest}.
     * @throws IllegalAccessException    Si no se tiene acceso al método {@code handleClientRequest}.
     */
    private void processClientRequest(Socket clientSocket) throws IOException, InvocationTargetException, IllegalAccessException {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String request = readClientRequest(in);
            String response = handleClientRequest(request);
            sendServerResponse(out, response);

        } finally {
            clientSocket.close();
        }
    }

    /**
     * Lee y analiza la solicitud del cliente del flujo de entrada proporcionado.
     * @param in  El BufferedReader del que se leerá la solicitud.
     * @return    La solicitud analizada, como una cadena en el formato "verbo solicitud".
     * @throws IOException  Si se produce un error de E/S al leer la solicitud.
     */
    private String readClientRequest(BufferedReader in) throws IOException {
        String inputLine, request = "/simple";
        String verb = "";
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);

            // Dividir la primera línea en verb y request
            if (verb.isEmpty()) {
                String[] requestTokens = inputLine.split(" ");
                if (requestTokens.length >= 2) {
                    verb = requestTokens[0];
                    request = requestTokens[1];
                }
            }
            // Salir del bucle si no hay más datos para leer
            if (!in.ready()) {
                break;
            }
        }

        return verb + " " + request;
    }

    /**
     * Maneja una solicitud del cliente y genera la respuesta correspondiente.
     * @param request  La solicitud del cliente, en formato "verbo ruta".
     * @return  La respuesta generada para la solicitud.
     * @throws InvocationTargetException  Si se produce un error al invocar el método de servicio.
     * @throws IllegalAccessException     Si no se tiene acceso al método de servicio.
     */
    private String handleClientRequest(String request) throws InvocationTargetException, IllegalAccessException {
        String[] requestParts = request.split(" ");
        String verb = requestParts[0];
        String path = requestParts[1];
        // Obtener el servicio para la ruta especificada
        Method service = services.get(path);
        // Manejar diferentes tipos de solicitudes
        if ("GET".equalsIgnoreCase(verb)) {
            return service != null ? service.invoke(null).toString() : getHomeIndex();
        } else {
            return getHomeIndex();
        }
    }

    /**
     * Envía una respuesta al cliente a través del escritor de salida proporcionado.
     * @param out El escritor de salida conectado al cliente.
     * @param response La respuesta que se enviará al cliente.
     */
    private void sendServerResponse(PrintWriter out, String response) {
        out.println(response);
        out.close();
    }



    /**
     * Obtiene el nombre de archivo y la ruta desde una cadena de entrada.
     * @param inputString La cadena de entrada que contiene la información del nombre de archivo.
     * @return Una cadena con la representación del archivo como respuesta HTTP (HTML, imagen, etc.), o un mensaje de error si no se encuentra el archivo o no se soporta el tipo.
     * @throws IOException Si se produce un error al leer el archivo.
     */
    public static String findBoundaries(String inputString) {
        System.out.println(inputString);
        String[] parts = inputString.split(";");
        String filename = null;
        for (String part : parts) {
            if (part.trim().startsWith("filename")) {
                // Extract the name parameter
                String[] nameParts = part.split("=");
                if (nameParts.length > 1) {
                    filename = nameParts[1].trim().replace("\"", "");
                }
            }
        }
        String path = "src\\main\\resource\\";
        return getTheArchive(filename, path);
    }

    /**
     * Obtiene y procesa un archivo en función de su tipo.
     * @param filename El nombre del archivo a obtener.
     * @param path La ruta donde se encuentra el archivo.
     * @return El contenido del archivo procesado, o "404" si no existe o no se puede procesar.
     */
    public static String getTheArchive(String filename, String path) {
        String completePath = path + filename;
        file = new File(completePath);
        int extensionIndex = filename.lastIndexOf(".");
        String type = extensionIndex != -1 ? filename.substring(extensionIndex + 1) : null;
        if (file.exists()) {
            System.out.println("Existe");
            try {
                switch (type) {
                    case "html":
                        return toHTML(file);
                    case "png":
                        return toImage(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("NO existe");
        }
        return "404";
    }

    /**
     * Convierte una imagen de un archivo en una cadena codificada en Base64, encapsulada en una respuesta HTTP.
     * @param file El archivo de imagen a convertir.
     * @return Una cadena HTML que contiene la imagen codificada en Base64, formateada como una respuesta HTTP y centrada.
     * @throws IOException Si se produce un error al leer el archivo.
     */
    public static String toImage(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "    <head>\r\n"
                + "        <title>File Content</title>\r\n"
                + "    </head>\r\n"
                + "    <body>\r\n"
                + "         <center><img src=\"data:image/jpeg;base64," + base64 + "\" alt=\"image\"></center>" + "\r\n"
                + "    </body>\r\n"
                + "</html>";
    }
    /**
     * Convierte el contenido de un archivo en una cadena HTML, formateada como una respuesta HTTP.
     * @param file El archivo a convertir.
     * @return Una cadena HTML que contiene el contenido del archivo, envuelta en una cabecera de respuesta HTTP y centrada dentro del cuerpo.
     * @throws IOException Si se produce un error al leer el archivo.
     */
    public static String toHTML(File file) throws IOException {
        StringBuilder body = fileToString(file);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\r\n" + //
                "<html>\r\n" + //
                "    <head>\r\n" + //
                "        <meta charset=\"UTF-8\">\r\n" + //
                "        <title>File Adder</title>\r\n" + //
                "    </head>\r\n" + //
                "    <body>\r\n" + //
                "        <pre>" + body + "</pre>\r\n" + //
                "    </body>\r\n" + //
                "</html>";
    }

    /**
     * Lee el contenido de un archivo y lo devuelve como un StringBuilder.
     * @param file El archivo a leer.
     * @return Un StringBuilder con el contenido del archivo.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public static StringBuilder fileToString(File file) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line).append("\n");
            }
        }
        return body;
    }


    /**
     * Obtiene una lista de clases desde las rutas de clase especificadas.
     * @return  Una lista de objetos {@code Class} representando las clases encontradas.
     */
    private List<Class<?>> getClasses() {
        List<Class<?>> classes = new ArrayList<>();
        try {
            for (String cp : getClassPaths()) {
                File file = new File(cp + "/" + pathToClasses);
                if (file.exists() && file.isDirectory()) {
                    for (File cf : Objects.requireNonNull(file.listFiles())) {
                        if (cf.isFile() && cf.getName().endsWith(".class")) {
                            String rootTemp = pathToClasses.replace("/", ".");
                            String className = rootTemp + "." + cf.getName().replace(".class", "");
                            Class<?> classObj = Class.forName(className);
                            classes.add(classObj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * Obtiene una lista de las rutas de clase configuradas en el sistema.
     * @return  Una lista de cadenas que representan las rutas de clase.
     */
    public ArrayList<String> getClassPaths(){
        String classPath = System.getProperty("java.class.path");
        String[] classPaths =  classPath.split(System.getProperty("path.separator"));
        return new ArrayList<>(Arrays.asList(classPaths));
    }
    /**
     * Implementa la inversión de control (IoC) mediante la detección automática de clases anotadas con {@code @Component}
     * y registrando sus métodos anotados con {@code @GetMapping}.
     * @throws ClassNotFoundException Si se produce un error al cargar una clase.
     */
    public  void inversionOfControl() throws ClassNotFoundException{
        List<Class<?>> classes = getClasses();
        for (Class<?> clasS : classes) {
            if (clasS.isAnnotationPresent(Component.class)) {
                Object instance;
                try {
                    instance = clasS.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    // Manejo de excepciones
                    e.printStackTrace();
                    continue;
                }
                Method[] methods = clasS.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        String key = method.getAnnotation(GetMapping.class).value();
                        method.setAccessible(true); // Permitir acceso a métodos privados
                        services.put(key, method);
                    }
                }
            }
        }
    }


    public static String getHomeIndex() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>File Upload</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css\">\n" +
                "    <style>\n" +
                "        body {\n" +
                "            background-image: url(https://i.pinimg.com/originals/c5/e4/b5/c5e4b54952c6857bf00a02929abffe51.gif);\n" +
                "background-size: cover;"+
                "background-repeat: no-repeat;"+
                "background-position: center center;"+
                "            font-family: \"Ubuntu\", sans-serif;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        .container {\n" +
                "            max-width: 1000px;\n" +
                "            padding: 50px;\n" +
                "            background-color: #ffffff;\n" +
                "            border-radius: 5px;\n" +
                "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "\n" +
                "        .form-group {\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .btn {\n" +
                "            background-color: #D9B9EA;\n" +
                "            color: #ffffff;\n" +
                "            border: none;\n" +
                "            border-radius: 5px;\n" +
                "            padding: 10px 20px;\n" +
                "            cursor: pointer;\n" +
                "            transition: background-color 0.3s ease;\n" +
                "        }\n" +
                "\n" +
                "        .btn:hover {\n" +
                "            background-color: #7663C6;\n" +
                "        }\n" +
                "\n" +
                "        #uploadMsg {\n" +
                "            margin-top: 20px;\n" +
                "            color: #007bff;\n" +
                "        }\n" +
                "\n" +
                "        .btn-file {\n" +
                "            position: relative;\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "\n" +
                "        .btn-file input[type=\"file\"] {\n" +
                "            position: absolute;\n" +
                "            top: 0;\n" +
                "            right: 0;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            font-size: 20px;\n" +
                "            cursor: pointer;\n" +
                "            opacity: 0;\n" +
                "            filter: alpha(opacity=0);\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            position: absolute;\n" +
                "            left: 0;\n" +
                "            top: 0;\n" +
                "            z-index: 20;\n" +
                "        }\n" +
                "\n" +
                "        .btn-file:before {\n" +
                "            content: \"Escoge un archivo:\";\n" +
                "            display: inline-block;\n" +
                "            padding: 10px 20px;\n" +
                "            background-color: #D9B9EA;\n" +
                "            color: #ffffff;\n" +
                "            border-radius: 5px;\n" +
                "            margin-right: 10px;\n" +
                "            font-size: 16px;\n" +
                "            line-height: 24px;\n" +
                "            vertical-align: middle;\n" +
                "        }\n" +
                "\n" +
                "        .filename {\n" +
                "            display: inline-block;\n" +
                "            margin-left: 10px;\n" +
                "            vertical-align: middle;\n" +
                "            line-height: 24px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1 class=\"text-center\">CARGAR UN ARCHIVO</h1>\n" +
                "        <form action=\"/upload\" method=\"POST\" enctype=\"multipart/form-data\">\n" +
                "            <div class=\"form-group\">\n" +
                "                <div class=\"btn btn-file\">\n" +
                "                    <input type=\"file\" id=\"file\" name=\"file\" class=\"form-control\">\n" +
                "                </div>\n" +
                "                <span id=\"filename\" class=\"filename\"></span>\n" +
                "            </div>\n" +
                "            <button type=\"button\" class=\"btn btn-block\" onclick=\"uploadFile()\">Subir</button>\n" +
                "        </form>\n" +
                "        <div id=\"uploadMsg\"></div>\n" +
                "    </div>\n" +
                "\n" +
                "    <script>\n" +
                "        function uploadFile() {\n" +
                "            const fileInput = document.getElementById(\"file\");\n" +
                "            const filenameSpan = document.getElementById(\"filename\");\n" +
                "            filenameSpan.textContent = fileInput.value.split(\"\\\\\").pop();\n" +
                "\n" +
                "            const formData = new FormData();\n" +
                "            formData.append(\"file\", fileInput.files[0]);\n" +
                "\n" +
                "            const xhr = new XMLHttpRequest();\n" +
                "            xhr.onload = function () {\n" +
                "                document.getElementById(\"uploadMsg\").innerHTML = this.responseText;\n" +
                "            };\n" +
                "            xhr.open(\"POST\", \"/upload\");\n" +
                "            xhr.send(formData);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
