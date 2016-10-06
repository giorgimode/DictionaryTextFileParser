import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Created by modeg on 10/1/2016.
 */
public class FileConverter {
    protected Map<String, Map<String, String>> dictionaryMap = new TreeMap<>();
    private String      path;
    private PrintWriter printWriter;

    public String getFullPath() {
        return getPath() + "full\\";
    }

    public String getSplitPath() {
        return getPath() + "split\\";
    }

    public enum ModeType {
        TEST("test"), MAIN("main");

        private final String value;

        ModeType(String value) {
            this.value = value;
        }

        public String getValue(){
            return this.value;
        }
    }

    public static void main(String[] args) throws IOException {
        FileConverter fileConverter = new FileConverter();
        fileConverter.setPath(ModeType.MAIN.getValue());
        fileConverter.parseDirectory();
    }

    protected void parseDirectory() throws IOException {
        File folder = new File(getFullPath());
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

    private void parse(String locale) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getFullPath() + locale + "\\" + locale + ".txt"));
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

    /**
     * removes syntax words only if separate ('to' will not be removed from 'total')
     * removes everything inside bracket family
     * cleans all non alphabetic symbols except a few special ones allowed
     * even allowed symbols are removed if they are in the beginning of the key string
     */
    protected String sanitize(String rawKey, String locale) throws IOException {
        List<String> syntaxWords = getSyntaxWords(locale);
        for (String key : syntaxWords) {
            if (rawKey.contains(key)) {
                String keyWithEscapedCharacters = Pattern.quote(key);
                String regex = "(?U)[\\P{Alpha}]" + keyWithEscapedCharacters + "(?U)[\\P{Alpha}]";
                rawKey = rawKey.replaceAll(regex, " ");
                if (rawKey.startsWith(key + " ") || rawKey.endsWith(" " + key))
                    rawKey = rawKey.replace(key, " ");
            }
        }
        String cleanKey = rawKey.replaceAll("\\{.*?\\} ?", "")
                                .replaceAll("\\[.*?\\] ?", "")
                                .replaceAll("<.*?> ?", "")
                                .replaceAll("\\(.*?\\) ?", "");
        cleanKey = cleanNonAlpha(cleanKey);
        int i = 0;
        while (i < cleanKey.length() && !Character.toString(cleanKey.charAt(i)).matches("(?U)[\\p{Alpha}]")) {
            i++;
        }
        cleanKey = cleanKey.substring(i);
        return cleanKey.trim().toLowerCase();
    }

    protected List<String> getSyntaxWords(String locale) throws IOException {
        List<String> syntaxWords = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(getFullPath() + locale + "\\" + "syntax.txt"));
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
                dictionaryMap.get(cleanKey).put(key, value);
            } else {
                dictionaryMap.get(cleanKey).put(rawKey, value);
            }
        } else {
            dictionaryMap.put(cleanKey, new TreeMap<>());
            dictionaryMap.get(cleanKey).put(rawKey, value);
        }

    }

    private void createSubFiles(String locale) throws IOException {
        File dir = new File(getSplitPath() + locale);
        if (dir.mkdir()) {
            System.out.println("CREATED DIRECTORY: " + getSplitPath() + locale);
        } else {
            throw new IOException("DIRECTORY EXISTS");
        }
        char folderName = '~';
        File file = null;
        for (Map.Entry<String, Map<String, String>> entrySet : dictionaryMap.entrySet()) {
            if (Character.toLowerCase(entrySet.getKey().charAt(0)) != folderName) {
                folderName = Character.toLowerCase(entrySet.getKey().charAt(0));
                file = new File(getSplitPath() + locale + "\\" + folderName + ".txt");
                if (file.createNewFile()) {
                    System.out.println("CREATED FILE: " + getSplitPath() + locale + "\\" + folderName + ".txt");
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter(getSplitPath() + locale + "\\" + folderName + ".txt", true)));
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

    public String getPath() {
        return path;
    }

    public void setPath(String mode) {
        this.path = ".\\src\\" + mode + "\\resources\\";
    }

    public String cleanNonAlpha(String name) {
        List<Character> allowedCharacters = Arrays.asList('-', '\'', '&', '_', ' ');
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c) && !allowedCharacters.contains(c)) {
                name = name.replace(Character.toString(c), "");
            }
        }

        return name;
    }
}
