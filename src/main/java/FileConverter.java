import com.google.common.collect.ImmutableMap;
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

import static org.apache.commons.lang3.StringUtils.isBlank;

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

        public String getValue() {
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
        for (String currentSyntaxWord : syntaxWords) {
            if (rawKey.contains(currentSyntaxWord)) {
                String keyWithEscapedCharacters = Pattern.quote(currentSyntaxWord);
                String regex = "(?U)[\\P{Alpha}]" + keyWithEscapedCharacters + "(?U)[\\P{Alpha}]";
                rawKey = rawKey.replaceAll(regex, " ");
                if (rawKey.startsWith(currentSyntaxWord + " ") || rawKey.endsWith(" " + currentSyntaxWord)) {
                    rawKey = rawKey.replace(currentSyntaxWord, " ");
                }
            }
        }
        String cleanKey = rawKey.replaceAll("\\{.*?\\} ?", "")
                                .replaceAll("\\[.*?\\] ?", "")
                                .replaceAll("<.*?> ?", "")
                                .replaceAll("\\(.*?\\) ?", "");
        // if the whole string is in brackets, then ignore above operation and not clean everything
        if (isBlank(cleanKey)) {
            cleanKey = rawKey;
        }
        cleanKey = cleanNonAlpha(cleanKey);
        int i = 0;
        while (i < cleanKey.length() && !Character.toString(cleanKey.charAt(i)).matches("(?U)[\\p{Alpha}]")) {
            i++;
        }
        cleanKey = cleanKey.substring(i).trim().toLowerCase();

        return cleanKey.replace(" ", "\\ ");
    }

    protected List<String> getSyntaxWords(String locale) throws IOException {
        List<String> syntaxWords = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(getFullPath() + locale + "\\" + ConverterConstants.SYNTAX_FILE_NAME));
        String currentLine = null;
        while ((currentLine = reader.readLine()) != null) {
            if (StringUtils.isNotBlank(cleanNonAlpha(currentLine))) {
                syntaxWords.add(currentLine.split("\t", 2)[0]);
            }
        }
        return syntaxWords;
    }


    protected void store(String cleanKey, String rawKey, String value) {
        if (dictionaryMap.containsKey(cleanKey)) {
            if (dictionaryMap.get(cleanKey).containsKey(rawKey)) {
                int counter = 1;
                String sCounter = "~" + counter + "~";
                while (dictionaryMap.get(cleanKey).containsKey(rawKey + sCounter)) {
                    counter++;
                    sCounter = "~" + counter + "~";
                }
                String key = rawKey + sCounter;
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
                file = new File(getSplitPath() + locale + "\\" + folderName + ConverterConstants.FILE_FORMAT_PROPERTY);
                if (file.createNewFile()) {
                    System.out.println("CREATED FILE: " + getSplitPath() + locale + "\\" + folderName + ConverterConstants.FILE_FORMAT_PROPERTY);
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter(
                            getSplitPath() + locale + "\\" + folderName + ConverterConstants.FILE_FORMAT_PROPERTY, true)));
                } else {
                    throw new IOException("FILE EXISTS");
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(entrySet.getKey() + "=");

            Map<String, String> rawEntrySet = entrySet.getValue();
            rawEntrySet.entrySet()
                       .forEach(raw -> stringBuilder
                               .append(raw.getKey() + ConverterConstants.KEY_VALUE_SPLIT + wordTypeSyntaxFormatter(raw.getValue()))
                               .append(ConverterConstants.DEFINITION_SPLIT));

            String output = stringBuilder.toString();
            printWriter.println(output.substring(0, output.length() - ConverterConstants.DEFINITION_SPLIT.length()));
        }
    }

    protected String wordTypeSyntaxFormatter(String translation) {
        if (isBlank(translation)) {
            return translation;
        }
        String[] words = translation.split("\\t");
        if (words.length > 1) {
            String syntaxWordString = words[words.length - 1];
            String syntaxWords[] = syntaxWordString.split("\\s+");
            String syntaxFormatted = "";
            for (int i = 0; i < syntaxWords.length; i++) {
                if (syntaxWordMap.containsKey(syntaxWords[i])) {
                    syntaxFormatted += syntaxWordMap.get(syntaxWords[i]);
                    translation = translation.replace(syntaxWords[i], "");
                    if (i == syntaxWords.length - 1) {
                        translation = syntaxFormatted + " " + translation;
                    }
                }
            }
        }
        return translation.trim();
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
            if (!Character.isLetter(c) && !allowedCharacters.contains(c)) {
                name = name.replace(Character.toString(c), "");
            }
        }

        return name;
    }

    ImmutableMap<String, String> syntaxWordMap = new ImmutableMap.Builder<String, String>()
            .put("adj", "(adj.)")
            .put("adv", "(adv.)")
            .put("past-p", "(past-p.)")
            .put("verb", "(verb)")
            .put("pres-p", "(pres-p.)")
            .put("prep", "(prep.)")
            .put("conj", "(conj.)")
            .put("pron", "(pron.)")
            .put("prefix", "(prefix)")
            .put("suffix", "(suffix)")
            .put("noun", "(noun)")
            .build();

}
