import java.io.*;
import java.util.*;

public class number_parse {

    static private String pathToConditions;
    static private String pathToResult;
    static private String resourceSystem;
    static private String idClass;
    static private String alphabet;
    static private String charEquals;
    static private String charNotWorkingWith;
    static private String charEqualNumbers;
    static private String displacement;
    static private String stringForConcatenationCondition;
    static private String stringForConcatenationTestMSISDN;

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {//Если ключи заданы.
            fillParams(args);//Заполняем параметры значенями ключей.
            generateInsert();//Вызываем метод генерации инсертов.
        } else {//Если ключи не заданы.
            System.out.println("Программа не выполняется без ключей.");
        }
    }

    private static void fillParams(String[] args) {
        pathToConditions = args[0];
        pathToResult = args[1];
        resourceSystem = args[2];
        idClass = args[3];
        alphabet = args[4];
        charEquals = args[5];
        charNotWorkingWith = args[6];
        charEqualNumbers = args[7];
        displacement = args[8];
        stringForConcatenationCondition = args[9];
        //stringForConcatenationTestMSISDN = args[10];
    }

    private static void generateInsert() throws Exception {
        //Начало блока проверки файла на невалидные символы.
        BufferedReader bufferForCheck = new BufferedReader(new InputStreamReader(new FileInputStream(pathToConditions)));
        String lineFromFile;
        int indexOfLineFromFile = 0;
        while ((lineFromFile = bufferForCheck.readLine()) != null) {
            indexOfLineFromFile++;
            isInvalidCharacter(lineFromFile, indexOfLineFromFile);
        }
        bufferForCheck.close();
        //Конец блока проверки файла на невалидные символы.
        //Начало блока генерации инсертов.
        File outputFile = new File(pathToResult);
        outputFile.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(pathToConditions)));
        String MSISDN;
        while ((MSISDN = bufferedReader.readLine()) != null) {
            String insert = "";
            if (resourceSystem.equals("LIS")) {
                insert = "insert into LIS_NUMBER_CLASS_CONDITIONS (NCLSC_ID, NAVI_DATE, NAVI_USER, CONDITION_STRING, IS_ACTIVE, NAME, TEST_MSISDN, MRGN_MRGN_ID, NCLST_NCLST_ID)\n" +
                        "values (NCLSC_SEQ.nextval, sysdate, 'Admin', '" + stringForConcatenationCondition + parseMSISDN(MSISDN) + "', 'Y', '" + MSISDN + "', '" /*+ stringForConcatenationTestMSISDN*/ + genTestMSISDN(MSISDN) + "', 1, " + idClass + ");\n";
            }
            if (resourceSystem.equals("RIM")) {
                insert = "insert into NUMBER_CLASS_TEMPLATES (NCTE_ID, NCLS_NCLS_ID, DEF, TEMPLATE, ACTIVE_YN, TEST_NUMBER, NAVI_USER, NAVI_DATE, BRNC_BRNC_ID)\n" +
                        "values (NCTE_SEQ.nextval, " + idClass + ", '" + MSISDN + "', '" + stringForConcatenationCondition + parseMSISDN(MSISDN) + "', 'Y', '" /*+ stringForConcatenationTestMSISDN*/ + genTestMSISDN(MSISDN) + "', 'BIS', sysdate, 0);\n";
            }
            bufferedWriter.write(insert);
        }

        bufferedWriter.close();
        bufferedReader.close();
        //Конец блока генерации инсертов.
    }

    private static String parseMSISDN(String MSISDN) {//Метод парсит маски номеров в условия для БД.
        char[] arrayOfMSISDN = MSISDN.toCharArray();
        ArrayList<ArrayList<Integer>> whereFound = new ArrayList<>();
        ArrayList<Integer> howMachRepeat = new ArrayList<>();
        //Начало блока заполенения массивов, где именно (whereFound) и сколько раз повтаряются (howMachRepeat) символы в маске.
        //Заполнение стандартными значениями.
        for (int i = 0; i < arrayOfMSISDN.length; i++) {
            if (i >= 10) {
                whereFound.add(new ArrayList<>());
                whereFound.get(i).add(i);
                howMachRepeat.add(0);
            }
            if (i < 10) {
                whereFound.add(new ArrayList<>());
                whereFound.get(i).add(i);
                howMachRepeat.add(0);
            }
        }
        //Заполнение конкретными значениями.
        for (int i = 0; i < arrayOfMSISDN.length - 1; i++) {//Берем каждый символ в маске и сравниваем его со всеми последующими
            for (int a = i + 1; a < arrayOfMSISDN.length; a++) {
                String letterWhichCompare = Character.toString(arrayOfMSISDN[i]);
                String letterWithWhichCompared = Character.toString(arrayOfMSISDN[a]);
                if (charNotWorkingWith.equals("!X")) {
                    if (letterWhichCompare.equals(letterWithWhichCompared) && isChar(letterWhichCompare) && !wasBefore(letterWhichCompare, MSISDN, howMachRepeat, i)/*Метод проверяет, обрабатывался ли символ до текущей итерации цикла или нет*/&& !letterWhichCompare.equals("X")) {
                        whereFound.get(i).add(a);//При совпадении добавляем адрес, где встретился символ
                        howMachRepeat.set(i, howMachRepeat.get(i) + 1);//И добавляем количесвто повторений
                    }
                }
                if (charNotWorkingWith.equals("X")) {
                    if (letterWhichCompare.equals(letterWithWhichCompared) && isChar(letterWhichCompare) && !wasBefore(letterWhichCompare, MSISDN, howMachRepeat, i)) {
                        whereFound.get(i).add(a);
                        howMachRepeat.set(i, howMachRepeat.get(i) + 1);
                    }
                }
            }
        }
        //Конец блока заполенения массивов, где именно (whereFound) и сколько раз повтаряются (howMachRepeat) символы в маске.
        String result = "";
        //Начало блока заполенения условия для БД цифрами и повторяющимися символами из маски.
        for (int b = 0; b < arrayOfMSISDN.length; b++) {
            if (!isChar(Character.toString(arrayOfMSISDN[b]))) {//Если перед нами цифра, то она просто подставляется в условие под соответствующим индексом
                if (b != 0 && !result.equals("")) {
                    result = result + " and ";
                }
                result = result + ":" + (b + Integer.parseInt(displacement)) + " = " + arrayOfMSISDN[b];
            }
            if (isChar(Character.toString(arrayOfMSISDN[b]))) {//Если перед нами символ, причем только тот, что повторялся, то с помощью массивов, что заполнялись ранее, заполняется результат с учетом повторения этого символа
                if (howMachRepeat.get(b) != 0) {
                    ArrayList<Integer> whereFoundChar = whereFound.get(b);
                    for (int k = 0; k < whereFoundChar.size() - 1; k++) {
                        if (k != whereFoundChar.size() - 1 && !result.equals("")) {
                            result = result + " and ";
                        }
                        result = result + ":" + (whereFoundChar.get(k) + Integer.parseInt(displacement)) + " = :" + (whereFoundChar.get(k + 1) + Integer.parseInt(displacement));
                    }
                }
            }
        }
        //Конец блока заполенения условия для БД цифрами и повторяющимися символами из маски.
        ArrayList<Character> listOfLettersWhichCompere = new ArrayList<>();
        ArrayList<Character> listOfLettersWithWhichCompered = new ArrayList<>();
        //Начало блока добавления в условие БД дополнительных условий.
        if (charEquals.equals("A!=B")) {
            for (int j = 0; j < arrayOfMSISDN.length; j++) {
                if (isChar(Character.toString(arrayOfMSISDN[j]))) {
                    for (int k = j + 1; k < arrayOfMSISDN.length; k++) {//Если перед нами буква, то сравниваем ее со всеми последующими буквами
                        if (!listOfLettersWhichCompere.contains(arrayOfMSISDN[j])//Если буква еще не была обработана
                                && !listOfLettersWithWhichCompered.contains(arrayOfMSISDN[k]) //Если буква, с которой сравнивают обрабатываемую, еще не встречалась
                                && isChar(Character.toString(arrayOfMSISDN[k])) //Если символ, с которым сравнивается обрабатываемая буква, это буква
                                && !Character.toString(arrayOfMSISDN[k]).equals(Character.toString(arrayOfMSISDN[j]))//Если две буквы не совпадают
                        ) {
                            if (charNotWorkingWith.equals("!X")) {
                                if (!Character.toString(arrayOfMSISDN[k]).equals("X") && !Character.toString(arrayOfMSISDN[j]).equals("X")) {
                                    if (resourceSystem.equals("LIS")) {
                                        if (!result.equals("")) {
                                            result = result + " and not ";
                                        }
                                        result = result + ":" + (j + Integer.parseInt(displacement)) + " = :" + (k + Integer.parseInt(displacement));
                                        listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);//Добавляем ту букву, которая уже сравнивалась с обрабатываемой
                                    }
                                    if (resourceSystem.equals("RIM")) {
                                        if (!result.equals("")) {
                                            result = result + " and ";
                                        }
                                        result = result + ":" + (j + Integer.parseInt(displacement)) + " != :" + (k + Integer.parseInt(displacement));
                                        listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);
                                    }
                                }
                            }
                            if (charNotWorkingWith.equals("X")) {
                                if (resourceSystem.equals("LIS")) {
                                    if (!result.equals("")) {
                                        result = result + " and not ";
                                    }
                                    result = result + ":" + (j + Integer.parseInt(displacement)) + " = :" + (k + Integer.parseInt(displacement));
                                    listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);
                                }
                                if (resourceSystem.equals("RIM")) {
                                    if (!result.equals("")) {
                                        result = result + " and ";
                                    }
                                    result = result + ":" + (j + Integer.parseInt(displacement)) + " != :" + (k + Integer.parseInt(displacement));
                                    listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);
                                }
                            }

                        }
                    }
                }
                listOfLettersWhichCompere.add(arrayOfMSISDN[j]); //Добавляем букву в список уже обработанных
                listOfLettersWithWhichCompered = new ArrayList<>();//Чистим список, с которыми будет сравниваться новая буква
            }
        }
        if (charEqualNumbers.equals("A!=1")) {
            listOfLettersWhichCompere = new ArrayList<>();//Чистим списки
            listOfLettersWithWhichCompered = new ArrayList<>();
            for (int j = 0; j < arrayOfMSISDN.length; j++) {
                if (isChar(Character.toString(arrayOfMSISDN[j]))) {
                    for (int k = 0; k < arrayOfMSISDN.length; k++) {//Если перед нами буква, то сравниваем ее со всеми последующими цифрами
                        if (!listOfLettersWhichCompere.contains(arrayOfMSISDN[j])//Если буква еще не была обработана
                                && !listOfLettersWithWhichCompered.contains(arrayOfMSISDN[k])//Если цифра, с которой сравнивают обрабатываемую, еще не встречалась
                                && !isChar(Character.toString(arrayOfMSISDN[k]))//Если символ, с которым сравнивается обрабатываемая буква, это цифра
                        ) {
                            if (charNotWorkingWith.equals("!X")) {
                                if (!Character.toString(arrayOfMSISDN[j]).equals("X")) {
                                    if (resourceSystem.equals("LIS")) {
                                        if (!result.equals("")) {
                                            result = result + " and not ";
                                        }
                                        result = result + ":" + (j + Integer.parseInt(displacement)) + " = " + arrayOfMSISDN[k];
                                        listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);//Добавляем ту цифру, которая уже сравнивалась с обрабатываемой
                                    }
                                    if (resourceSystem.equals("RIM")) {
                                        if (!result.equals("")) {
                                            result = result + " and ";
                                        }
                                        result = result + ":" + (j + Integer.parseInt(displacement)) + " != " + arrayOfMSISDN[k];
                                        listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);
                                    }
                                }
                            }
                            if (charNotWorkingWith.equals("X")) {
                                if (resourceSystem.equals("LIS")) {
                                    if (!result.equals("")) {
                                        result = result + " and not ";
                                    }
                                    result = result + ":" + (j + Integer.parseInt(displacement)) + " = " + arrayOfMSISDN[k];
                                    listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);
                                }
                                if (resourceSystem.equals("RIM")) {
                                    if (!result.equals("")) {
                                        result = result + " and ";
                                    }
                                    result = result + ":" + (j + Integer.parseInt(displacement)) + " != " + arrayOfMSISDN[k];
                                    listOfLettersWithWhichCompered.add(arrayOfMSISDN[k]);
                                }
                            }

                        }
                    }
                }
                listOfLettersWhichCompere.add(arrayOfMSISDN[j]);//Добавляем букву в список уже обработанных
                listOfLettersWithWhichCompered = new ArrayList<>();//Чистим список, с которыми будет сравниваться новая буква
            }
        }
        // Конец блока добавления в условие БД дополнительных условий.
        return result;
    }

    private static boolean isChar(String letter) {
        char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        for (int v = 0; v < chars.length; v++) {
            String l = Character.toString(chars[v]);
            if (l.equals(letter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean wasBefore(String letter, String MSISDN, ArrayList<Integer> howMachRepeat, int index) {
        char[] arrayOfMSISDN = MSISDN.toCharArray();
        for (int i = 0; i < arrayOfMSISDN.length; i++) {
            if (letter.equals(Character.toString(arrayOfMSISDN[i]))) {
                if (howMachRepeat.get(i) != 0 && index != i) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String genTestMSISDN(String MSISDN) {
        String result = "";
        if (alphabet.toCharArray().length == 10 && charEqualNumbers.equals("A=1")) {
            char[] arrayOfMSISDN = MSISDN.toCharArray();
            for (int p = 0; p < arrayOfMSISDN.length; p++) {
                if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[0]))) {
                    arrayOfMSISDN[p] = '1';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[1]))) {
                    arrayOfMSISDN[p] = '2';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[2]))) {
                    arrayOfMSISDN[p] = '3';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[3]))) {
                    arrayOfMSISDN[p] = '4';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[4]))) {
                    arrayOfMSISDN[p] = '5';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[5]))) {
                    arrayOfMSISDN[p] = '6';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[6]))) {
                    arrayOfMSISDN[p] = '7';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[7]))) {
                    arrayOfMSISDN[p] = '8';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[8]))) {
                    arrayOfMSISDN[p] = '9';
                } else if (Character.toString(arrayOfMSISDN[p]).equals(Character.toString(alphabet.toCharArray()[9]))) {
                    arrayOfMSISDN[p] = '0';
                }
            }
            for (int y = 0; y < arrayOfMSISDN.length; y++) {
                result = result + arrayOfMSISDN[y];
            }
            return result;
        }
        if (charEqualNumbers.equals("A!=1")) {
            char[] arrayOfMSISDN = MSISDN.toCharArray();
            ArrayList<String> ArrayOfNumeralInNumber = new ArrayList<>();
            ArrayList<String> ArrayOfCharInNumber = new ArrayList<>();
            HashMap<String, Integer> ArrayOfCharWithNumeralInNumber = new HashMap<>();
            for (int p = 0; p < arrayOfMSISDN.length; p++) {
                if (!isChar(Character.toString(arrayOfMSISDN[p]))) {
                    ArrayOfNumeralInNumber.add(Character.toString(arrayOfMSISDN[p]));
                }
                if (isChar(Character.toString(arrayOfMSISDN[p]))) {
                    ArrayOfCharInNumber.add(Character.toString(arrayOfMSISDN[p]));
                }
            }
            ArrayList<Integer> ArrayOfNumeralForReplace = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            for (int z = 0; z < ArrayOfCharInNumber.size(); z++) {
                for (int g = 0; g < 10; g++) {
                    if (!ArrayOfNumeralInNumber.contains(Integer.toString(g)) && !ArrayOfCharWithNumeralInNumber.containsKey(ArrayOfCharInNumber.get(z)) && !ArrayOfCharWithNumeralInNumber.containsValue(g)) {
                        ArrayOfCharWithNumeralInNumber.put(ArrayOfCharInNumber.get(z), g);
                        if (g < ArrayOfNumeralForReplace.size()) {
                            ArrayOfNumeralForReplace.remove(g);
                        }
                        break;
                    }
                }
            }
            for (int y = 0; y < arrayOfMSISDN.length; y++) {
                if (!isChar(Character.toString(arrayOfMSISDN[y]))) {
                    result = result + arrayOfMSISDN[y];
                }
                if (isChar(Character.toString(arrayOfMSISDN[y]))) {
                    result = result + ArrayOfCharWithNumeralInNumber.get(Character.toString(arrayOfMSISDN[y]));
                }
            }
            return result;
        }
        return result;
    }

    public static boolean isInvalidCharacter(String stringFromFile, int indexOfLineFromFile) throws Exception {
        ArrayList<Character> numbers = new ArrayList<>(Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'));
        for (int i = 0; i < stringFromFile.toCharArray().length; i++) {
            if (!numbers.contains(stringFromFile.toCharArray()[i])) {
                if (!isChar((Character.toString(stringFromFile.toCharArray()[i])))) {
                    throw new Exception("В файле некорректный символ:" + stringFromFile.toCharArray()[i] + " в строке под номером " + indexOfLineFromFile + ":" + stringFromFile + " на " + (i + 1) + " месте");
                }
            }
        }
        return false;
    }
}