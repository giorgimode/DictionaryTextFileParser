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
    private static Map<String, Map<String, String>> dictionaryMap = new TreeMap<>();
    private static final String PATH = ".\\src\\main\\resources\\";
    private static final String PATH_FULL = PATH + "full\\";
    private static final String PATH_SPLIT = PATH + "split\\";

    public static void main(String[] args) throws IOException {


        parseDirectory();
    }

    private static void parse(String locale) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PATH_FULL + locale + "\\"+ locale + ".txt"));
        String currentLine = null;
        String[] rawEntryPair = null;
        while ((currentLine = reader.readLine()) != null) {
            if (!currentLine.isEmpty() && currentLine.charAt(0) != '#') {
                rawEntryPair = currentLine.split("\t", 2);
                String rawKey = rawEntryPair[0];
                String value = rawEntryPair[1];
                String cleanKey = sanitize(rawKey, locale);
                store(cleanKey, rawKey, value);
            }
        }
    }

    private static String sanitize(String rawKey, String locale) throws IOException {
        // String rawKey = "to be [Br.] [zoo] or [zoo] {boboba} not {boboba} to (hooter) {bobobaz} be";
        BufferedReader reader = new BufferedReader(new FileReader(PATH_FULL + locale + "\\" + "syntax.txt"));
        String currentLine = null;
        List<String> keys = new ArrayList<>();
        while ((currentLine = reader.readLine()) != null) {
            if (!currentLine.isEmpty()) {
                keys.add(currentLine.split("\t", 2)[0]);
            }
        }

        //   String cleanKey = null;
        for (String key : keys) {
            if (rawKey.contains(key)) {
                rawKey = rawKey.replace(key, "");
            }
        }
        String cleanKey = rawKey.replaceAll("\\{.*?\\} ?", "").replaceAll("\\[.*?\\] ?", "").replaceAll("\\(.*?\\) ?", "");
        return cleanKey;
    }

    private static void store(String cleanKey, String rawKey, String value) {
        if (dictionaryMap.containsKey(cleanKey)) {
            if (dictionaryMap.get(cleanKey).containsKey(rawKey)) {
                int counter = 1;
                while (dictionaryMap.get(cleanKey).containsKey(rawKey + counter)) {
                    counter++;
                }
                String key = rawKey + counter;
                System.out.println("cleanKey: " + key);
            //    System.out.println("cleanKey: " + cleanKey + ", rawkey: " + (key));
                dictionaryMap.get(cleanKey).put(key, value);
            } else {
                dictionaryMap.get(cleanKey).put(rawKey, value);
            }
        } else {
            dictionaryMap.put(cleanKey, new TreeMap<>());
            dictionaryMap.get(cleanKey).put(rawKey, value);
        }

    }


    private static void parseDirectory() throws IOException {
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

    private static void createSubFiles(String locale) throws IOException {
        File dir = new File(PATH_SPLIT + locale);
        if (dir.mkdir()) {
            System.out.println("CREATED DIRECTORY: " + locale);
        }
        else
        {
            throw new IOException("DIRECTORY EXISTS");
        }
        char folderName = '~';
/*        dictionaryMap.entrySet().forEach(entrySet -> {
            folderName = entrySet.getKey().charAt(0);
     //   File file = new File(PATH_SPLIT + locale + folderName + "txt");
            Path file = Paths.get(PATH_SPLIT + locale + folderName + "txt");
            Files.write(file, lines, Charset.forName("UTF-8"));
        });*/
        File file = null;
        FileWriter fileWritter = null;
        BufferedWriter bufferWritter = null;
        PrintWriter out = null;
        for (Map.Entry<String, Map<String, String>> entrySet: dictionaryMap.entrySet()) {

            if (Character.toLowerCase(entrySet.getKey().charAt(0)) != folderName) {
                folderName = Character.toLowerCase(entrySet.getKey().charAt(0));
             //   Path file = Paths.get(PATH_SPLIT + locale + folderName + ".txt");
                file = new File(PATH_SPLIT + locale + "\\" + folderName + ".txt");
                if (file.createNewFile()) {
                    System.out.println("CREATED FILE: " + locale);
                  //  if (bufferWritter != null) bufferWritter.close();
                  //  fileWritter = new FileWriter(file.getName(),true);
                  //  bufferWritter = new BufferedWriter(fileWritter);
                 //   out = new PrintWriter(bufferWritter);
                }
                else
                {
                    throw new IOException("FILE EXISTS");
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(entrySet.getKey() + "=");

            Map<String, String> rawEntrySet = entrySet.getValue();
            rawEntrySet.entrySet().forEach(raw-> {
                stringBuilder.append(raw.getKey() + "-->" + raw.getValue());
            /*    if (rawEntrySet.size() > 1) {
                    stringBuilder.append("\\");
                }*/
            });


            //bufferWritter.write(stringBuilder.toString());
            //out.println(stringBuilder.toString());
            PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(PATH_SPLIT + locale + "\\" + folderName + ".txt", true)));
            out2.println(stringBuilder.toString());
            out2.close();
        }
    }

}
