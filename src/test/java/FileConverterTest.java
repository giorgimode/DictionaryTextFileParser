import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by modeg on 10/4/2016.
 */
public class FileConverterTest {
    public static void main(String[] args) {

    }

    @Test
    public void parseTest(){
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
