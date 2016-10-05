import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Created by modeg on 10/4/2016.
 */
public class FileConverterTest {

    @Test
    public void sanitizeTest() throws Exception {
        List<String> syntaxWords = Arrays.asList("[Br.]", "[Aus.]", "sb.'s", "to");
        FileConverter fileConverter = spy(FileConverter.class);
        doReturn(syntaxWords).when(fileConverter).getSyntaxWords(any());
        String cleanKey = fileConverter.sanitize("bigtotality to magificent", "en-de");
        assertEquals("bigtotality magificent", cleanKey);

        cleanKey = fileConverter.sanitize("bigtotality to magificent", "en-de");
        assertEquals("bigtotality magificent", cleanKey);

        cleanKey = fileConverter.sanitize("bite sb.'s ass", "en-de");
        assertEquals("bite ass", cleanKey);

        cleanKey = fileConverter.sanitize("[Br.] bad with Jesse Pinkman", "en-de");
        assertEquals("bad with jesse pinkman", cleanKey);

        cleanKey = fileConverter.sanitize("kill a mockingbird, {Lee}", "en-de");
        assertEquals("kill a mockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("kill a mocking_bird .[Lee]", "en-de");
        assertEquals("kill a mocking_bird", cleanKey);

        cleanKey = fileConverter.sanitize("kill & m'ockingbird <Lee>", "en-de");
        assertEquals("kill & m'ockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("kill a-mockingbird (Lee)", "en-de");
        assertEquals("kill a-mockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("[Br.] - 'kill a-mockingbird (Lee)", "en-de");
        assertEquals("kill a-mockingbird", cleanKey);
    }

    @Test
    public void testStore() {
        FileConverter fileConverter = spy(FileConverter.class);
        fileConverter.store("key", "key {raw}", "value");
        assertTrue(fileConverter.dictionaryMap.get("key").get("key {raw}").equals("value"));
        assertTrue(fileConverter.dictionaryMap.size() == 1);
        assertTrue(fileConverter.dictionaryMap.get("key").size() == 1);

        fileConverter.store("key", "key {raw2}", "value2");
        assertTrue(fileConverter.dictionaryMap.get("key").get("key {raw2}").equals("value2"));
        assertTrue(fileConverter.dictionaryMap.size() == 1);
        assertTrue(fileConverter.dictionaryMap.get("key").size() == 2);

        fileConverter.store("key", "key {raw}", "value3");
        assertTrue(fileConverter.dictionaryMap.get("key").get("key {raw}1").equals("value3"));
        assertTrue(fileConverter.dictionaryMap.size() == 1);
        assertTrue(fileConverter.dictionaryMap.get("key").size() == 3);
    }

    @Ignore
    @Test
    public void parseTest() {
/*        String[] test = {"JeanMarieLeBlanc", "Żółć", "Ὀδυσσεύς", "原田雅彦", "კოლოფი"};
        for (String str : test) {
            System.out.print(str.matches("^(?U)[\\p{Alpha}]+") + " ");
        }
        System.out.println("\n");
        String[] test2 = {"j", "Ż", "ύ", "原", "კ"};
        for (String str : test2) {
            System.out.print(str.matches("(?U)[\\p{Alpha}]") + " ");
        }*/

        String cleanKey = "that / which / who <GIORGI>";
        cleanKey = cleanKey.replaceAll("<.*?> ?", "");
        assertFalse(cleanKey.contains("<GIORGI>"));
        int i = 0;
        while (i < cleanKey.length() && !Character.toString(cleanKey.charAt(i)).matches("(?U)[\\p{Alpha}]")) {
            i++;
        }

        assertFalse(cleanKey.contains("/"));

    }
}
