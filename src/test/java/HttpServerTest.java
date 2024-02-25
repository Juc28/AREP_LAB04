import edu.eci.HttpServer;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {
    private HttpServer httpServer;

    @Before
    public void setUp() {
        httpServer = HttpServer.getInstance();
    }

    @Test
    public void testToHTML() throws IOException {
        String filename = "src/main/resources/hola.html";
        String htmlContent = httpServer.toHTML(new File(filename));
        assertNotNull(htmlContent);
        assertTrue(htmlContent.contains("<html>"));
    }

    @Test
    public void testToImage() throws IOException {
        String filename = "src/main/resources/Darwin.png";
        String imageContent = httpServer.toImage(new File(filename));
        assertNotNull(imageContent);
        assertTrue(imageContent.contains("data:image/jpeg;base64,"));
    }


}
