
import com.simple.logging.LoggingApplication;
import com.simple.logging.application.model.CustomLogProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {LoggingApplication.class})
class CustomLogPropertiesTest {

    @AfterEach
    public void tearDown() {
        CustomLogProperties.clearCustomProperties();
        CustomLogProperties.clearIgnoredProperties();
    }

    @Test
    void testSetAndGetCustomProperty() {
        CustomLogProperties.setCustomProperty("key1", "value1");
        assertEquals("value1", CustomLogProperties.getCustomProperty("key1"));
    }

    @Test
    void testSetCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        CustomLogProperties.setCustomProperties(properties);

        assertEquals("value1", CustomLogProperties.getCustomProperty("key1"));
        assertEquals("value2", CustomLogProperties.getCustomProperty("key2"));
    }

    @Test
    void testGetCustomProperties() {
        CustomLogProperties.setCustomProperty("key1", "value1");
        CustomLogProperties.setCustomProperty("key2", "value2");

        Map<String, String> properties = CustomLogProperties.getCustomProperties();
        assertEquals(2, properties.size());
        assertEquals("value1", properties.get("key1"));
        assertEquals("value2", properties.get("key2"));
    }

    @Test
    void testClearCustomProperties() {
        CustomLogProperties.setCustomProperty("key1", "value1");
        CustomLogProperties.clearCustomProperties();

        assertNull(CustomLogProperties.getCustomProperty("key1"));
        assertTrue(CustomLogProperties.getCustomProperties().isEmpty());
    }

    @Test
    void testAddAndGetIgnoredProperty() {
        CustomLogProperties.addIgnoredProperty("prop1");
        CustomLogProperties.addIgnoredProperty("prop2");

        List<String> ignoredProperties = CustomLogProperties.getIgnoredProperties();
        assertEquals(2, ignoredProperties.size());
        assertTrue(ignoredProperties.contains("prop1"));
        assertTrue(ignoredProperties.contains("prop2"));
    }

    @Test
    void testClearIgnoredProperties() {
        CustomLogProperties.addIgnoredProperty("prop1");
        CustomLogProperties.clearIgnoredProperties();

        assertTrue(CustomLogProperties.getIgnoredProperties().isEmpty());
    }
}
