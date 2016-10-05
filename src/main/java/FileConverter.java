import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by modeg on 10/1/2016.
 */
public class FileConverter {
    protected Map<String, Map<String, String>> dictionaryMap = new TreeMap<>();
    private static final String                           PATH          = ".\\src\\main\\resources\\";
    private static final String                           PATH_FULL     = PATH + "full\\";
    private static final String                           PATH_SPLIT    = PATH + "split\\";
    private static PrintWriter printWriter;

    public static void main(String[] args) throws IOException {
        FileConverter fileConverter = new FileConverter();
        fileConverter.parseDirectory();
    }

    private void parse(String locale) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PATH_FULL + locale + "\\" + locale + ".txt"));
        String currentLine = null;
        String[] rawEntryPair = null;
        while ((currentLine = reader.readLine()) != null) {
            if (!currentLine.isEmpty() && currentLine.charAt(0) != '#') {
                rawEntryPair = currentLine.split("\t", 2);
                if (rawEntryPair.length > 1) {
                    String rawKey = rawEntryPair[0];
                    String value = rawEntryPair[1];
                    String cleanKey = sanitize(rawKey, locale);
                    if (StringUtils.isNotBlank(rawKey) && StringUtils.isNotBlank(value) && StringUtils.isNotBlank(cleanKey)) {
                        store(cleanKey, rawKey, value);
                    }
                }
            }
        }
    }

    /*
    *     removes syntax words only if separate ('to' will not be removed from 'total')
    *     removes everything inside bracket family
    *     cleans all non alphabetic symbols except a few special ones allowed
    *     even allowed symbols are removed if they are in the beginning of the key string
    */
    protected String sanitize(String rawKey, String locale) throws IOException {
        List<String> syntaxWords = getSyntaxWords(locale);
        for (String key : syntaxWords) {
            if (rawKey.contains(key)) {
                String regex = "(?U)[\\P{Alpha}]" + key + "(?U)[\\P{Alpha}]";
                rawKey = rawKey.replaceAll(regex, " ");
            }
        }
        String cleanKey = rawKey.replaceAll("\\{.*?\\} ?", "")
                                .replaceAll("\\[.*?\\] ?", "")
                                .replaceAll("<.*?> ?", "")
                                .replaceAll("\\(.*?\\) ?", "");
        cleanKey = cleanKey.replaceAll("[(?U)[\\P{Alpha}]&&[^-'&_ ]]", "");
        int i = 0;
        while (i < cleanKey.length() && !Character.toString(cleanKey.charAt(i)).matches("(?U)[\\p{Alpha}]")) {
            i++;
        }
        cleanKey = cleanKey.substring(i);


        return cleanKey.trim().toLowerCase();
    }

    protected List<String> getSyntaxWords(String locale) throws IOException {
        List<String> syntaxWords = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(PATH_FULL + locale + "\\" + "syntax.txt"));
        String currentLine = null;
        while ((currentLine = reader.readLine()) != null) {
            if (!currentLine.isEmpty()) {
                syntaxWords.add(currentLine.split("\t", 2)[0]);
            }
        }
        return syntaxWords;
    }

    protected void store(String cleanKey, String rawKey, String value) {
        if (dictionaryMap.containsKey(cleanKey)) {
            if (dictionaryMap.get(cleanKey).containsKey(rawKey)) {
                int counter = 1;
                while (dictionaryMap.get(cleanKey).containsKey(rawKey + counter)) {
                    counter++;
                }
                String key = rawKey + counter;
                System.out.println("cleanKey: " + cleanKey + ", rawkey: " + (key));
                dictionaryMap.get(cleanKey).put(key, value);
            } else {
                dictionaryMap.get(cleanKey).put(rawKey, value);
            }
        } else {
            dictionaryMap.put(cleanKey, new TreeMap<>());
            dictionaryMap.get(cleanKey).put(rawKey, value);
        }

    }


    private void parseDirectory() throws IOException {
        File folder = new File(PATH_FULL);
        File[] listOfFolders = folder.listFiles();
        for (int i = 0; i < listOfFolders.length; i++) {

            if (listOfFolders[i].isDirectory()) {
                dictionaryMap = new TreeMap<>();
                String locale = listOfFolders[i].getName();
                parse(locale);
                createSubFiles(locale);
            }
        }
    }

    private void createSubFiles(String locale) throws IOException {
        File dir = new File(PATH_SPLIT + locale);
        if (dir.mkdir()) {
            System.out.println("CREATED DIRECTORY: " + locale);
        } else {
            throw new IOException("DIRECTORY EXISTS");
        }
        char folderName = '~';
        File file = null;
        for (Map.Entry<String, Map<String, String>> entrySet : dictionaryMap.entrySet()) {
            if (Character.toLowerCase(entrySet.getKey().charAt(0)) != folderName) {
                folderName = Character.toLowerCase(entrySet.getKey().charAt(0));
                file = new File(PATH_SPLIT + locale + "\\" + folderName + ".txt");
                if (file.createNewFile()) {
                    System.out.println("CREATED FILE: " + folderName);
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter(PATH_SPLIT + locale + "\\" + folderName + ".txt", true)));
                } else {
                    throw new IOException("FILE EXISTS");
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(entrySet.getKey() + "=");

            Map<String, String> rawEntrySet = entrySet.getValue();
            rawEntrySet.entrySet().forEach(raw -> stringBuilder.append(raw.getKey() + "-->" + raw.getValue()).append(" && "));


            printWriter.println(stringBuilder.toString());

        }
    }

}
