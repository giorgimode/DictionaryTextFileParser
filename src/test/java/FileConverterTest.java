import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
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
        List<String> syntaxWords = Arrays.asList("[Br.]", "[Aus.]", "sb.'s", "to", "{pl}");
        FileConverter fileConverter = spy(FileConverter.class);
        doReturn(syntaxWords).when(fileConverter).getSyntaxWords(any());

        String cleanKey = fileConverter.sanitize("bigtotality to magificent", "en-de");
        assertEquals("bigtotality\\ magificent", cleanKey);

        cleanKey = fileConverter.sanitize("bigtotality to magificent", "en-de");
        assertEquals("bigtotality\\ magificent", cleanKey);

        cleanKey = fileConverter.sanitize("bite sb.'s ass", "en-de");
        assertEquals("bite\\ ass", cleanKey);

        cleanKey = fileConverter.sanitize("[Br.] bad with Jesse Pinkman", "en-de");
        assertEquals("bad\\ with\\ jesse\\ pinkman", cleanKey);

        cleanKey = fileConverter.sanitize("kill a mockingbird, {Lee}", "en-de");
        assertEquals("kill\\ a\\ mockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("kill a mocking_bird .[Lee]", "en-de");
        assertEquals("kill\\ a\\ mocking_bird", cleanKey);

        cleanKey = fileConverter.sanitize("kill & m'ockingbird <Lee>", "en-de");
        assertEquals("kill\\ &\\ m'ockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("kill a-mockingbird (Lee)", "en-de");
        assertEquals("kill\\ a-mockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("[Br.] - 'kill a-mockingbird (Lee)", "en-de");
        assertEquals("kill\\ a-mockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("[Br.] - 'kill {pl} a-mockingbird (Lee)", "en-de");
        assertEquals("kill\\ a-mockingbird", cleanKey);

        cleanKey = fileConverter.sanitize("aspirations {pl}", "en-de");
        assertEquals("aspirations", cleanKey);

        cleanKey = fileConverter.sanitize("[whole string - in brackets]", "en-de");
        assertEquals("whole\\ string\\ -\\ in\\ brackets", cleanKey);

        cleanKey = fileConverter.sanitize("to", "en-de");
        assertEquals("to", cleanKey);
    }

    @Test
    public void sanitizeForeignTest() throws Exception {
        List<String> syntaxWords = Arrays.asList("[მრ.]", "[აშ.]", "ბრ.'ლ", "და", "{ვა}");
        FileConverter fileConverter = spy(FileConverter.class);
        doReturn(syntaxWords).when(fileConverter).getSyntaxWords(any());

        String cleanKey = fileConverter.sanitize("ნახეს უცხო მოყმე [მრ.] ვინმე", "en-de");
        assertEquals("ნახეს\\ უცხო\\ მოყმე\\ ვინმე", cleanKey);

        cleanKey = fileConverter.sanitize("ნახეს უცხო-მოყმე [მრ.] ვინმე", "en-de");
        assertEquals("ნახეს\\ უცხო-მოყმე\\ ვინმე", cleanKey);

        cleanKey = fileConverter.sanitize("და - ნახეს უცხო-მოყმე [მრ.] ვინმე", "en-de");
        assertEquals("ნახეს\\ უცხო-მოყმე\\ ვინმე", cleanKey);
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
    public void parseTest() throws IOException {
        FileConverter fileConverter = new FileConverter();
        fileConverter.setPath(FileConverter.ModeType.TEST.getValue());
        fileConverter.parseDirectory();
    }

    @Ignore
    @Test
    public void parseTest2() throws IOException {
/*
        String string = Pattern.quote("{test}");
        String txt = "testing {test} something";
        String regex = "(?U)[\\P{Alpha}]"+ string + "(?U)[\\P{Alpha}]";
        txt =  txt.replaceAll(regex, " ");
        System.out.println(regex);
        System.out.println(txt);
*/

        Properties prop = new Properties();
        prop.load(new FileReader(".\\src\\main\\resources\\split\\en-de\\s.properties"));
        long start = System.currentTimeMillis();
        System.out.println(prop.get("smooth a board"));
        long end = System.currentTimeMillis();
        long diff = end - start;

        System.out.println("time: " + diff);
        start = System.currentTimeMillis();
        System.out.println(prop.get("stonemason"));
        end = System.currentTimeMillis();
        diff = end - start;
        System.out.println("time: " + diff);
    }
}
