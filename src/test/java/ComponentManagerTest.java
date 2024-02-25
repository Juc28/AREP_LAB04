
import edu.eci.IoC.ComponentManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
public class ComponentManagerTest {
    private ComponentManager componentManager;

    @Before
    public void setUp() {
        componentManager = new ComponentManager();
    }

    @Test
    public void testGetHola() {
        String helloResponse = ComponentManager.getHola();
        assertNotNull(helloResponse);
        assertTrue(helloResponse.contains("HTTP/1.1 200 OK"));
        assertTrue(helloResponse.contains("Alive and kicking! :v"));
    }

    @Test
    public void testGetImagen() {
        try {
            String imageResponse = ComponentManager.getImagen();
            assertNotNull(imageResponse);
            assertTrue(imageResponse.contains("HTTP/1.1 200 OK"));
            assertTrue(imageResponse.contains("<center><h1>Imagen Funcionando</h1></center>"));
            assertTrue(imageResponse.contains("data:image/jpeg;base64,"));
        } catch (IOException e) {
            fail("IOException occurred while testing getImage: " + e.getMessage());
        }
    }

    @Test
    public void testGetHTMLPagina() {
        try {
            String htmlResponse = ComponentManager.getHTMLPaginas();
            assertNotNull(htmlResponse);
            assertTrue(htmlResponse.contains("HTTP/1.1 200 OK"));
            assertTrue(htmlResponse.contains("<!DOCTYPE html>"));
            assertTrue(htmlResponse.contains("<pre>"));
        } catch (IOException e) {
            fail("IOException occurred while testing getHTMLPages: " + e.getMessage());
        }
    }

    @Test
    public void testFromArchiveToString() {
        try {
            String filePath = "src/main/resources/hola.html";
            File testFile = new File(filePath);
            StringBuilder body = ComponentManager.fromArchiveToString(testFile);
            assertNotNull(body);
            assertTrue(body.toString().contains("<html>"));
        } catch (IOException e) {
            fail("IOException occurred while testing fromArchiveToString: " + e.getMessage());
        }
    }
}
