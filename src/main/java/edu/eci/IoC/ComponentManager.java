package edu.eci.IoC;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

@Component
public class ComponentManager {
    public static String filepath = "src\\main\\resources\\Darwin.png";
    public static String htmlPath = "src\\main\\resources\\Ejemplo.html";

    @GetMapping("/hola")
    public static String getHola() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "Alive and kicking! :v";
    }

    @GetMapping("/imagen")
    public static String getImagen() throws IOException {
        File file = new File(filepath);
        byte[] bytes = Files.readAllBytes(file.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "    <head>\r\n"
                + "        <title>Contenido del archivo </title>\r\n"
                + "    </head>\r\n"
                + "    <body>\r\n"
                + "         <center><h1>" + "Imagen Funcionando"+ "</h1></center>" + "\r\n"
                + "         <center><img src=\"data:image/jpeg;base64," + base64 + "\" alt=\"image\"></center>" + "\r\n"
                + "    </body>\r\n"
                + "</html>";
    }


    @GetMapping("/html")
    public static String getHTMLPaginas() throws IOException {
        File file = new File(htmlPath);
        StringBuilder body = fromArchiveToString(file);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "    <head>\r\n"
                + "        <meta charset=\"UTF-8\">\r\n"
                + "        <title>File Adder</title>\r\n"
                + "    </head>\r\n"
                + "    <body>\r\n"
                + "        <pre>" + body + "</pre>\r\n"
                + "    </body>\r\n"
                + "</html>";
    }


    public static StringBuilder fromArchiveToString(File file) throws IOException {
        StringBuilder body = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line).append("\n");
        }
        reader.close();
        return body;
    }
}
