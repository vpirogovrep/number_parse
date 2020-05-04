package main_pck;

import java.util.*;

public class number_parse {

    static private String resourceSystem;
    static private String idClass;
    static private ArrayList<String> alphabet;
    static private String charEquals;
    static private ArrayList<String> charNotWorkingWith;
    static private String charEqualNumbers;
    static private String displacement;
    static private String stringForConcatenationCondition;
    static private String stringForConcatenationTestMSISDN;
    static private String addCondition;

    public static void fillParams(String[] args, ArrayList<String> listOfMSISDN) throws Exception {
        //Формируем алфавит для генерации тестовых номеров
        ArrayList<String> listOfAlphabet = new ArrayList<>();
        for (int u = 0; u < listOfMSISDN.size(); u++) {
            for (int i = 0; i < listOfMSISDN.get(u).toCharArray().length; i++) {
                if (isChar(Character.toString(listOfMSISDN.get(u).toCharArray()[i])) && !listOfAlphabet.contains(Character.toString(listOfMSISDN.get(u).toCharArray()[i]))) {
                    listOfAlphabet.add(Character.toString(listOfMSISDN.get(u).toCharArray()[i]));
                }
            }
        }
        //Формируем алфавит из символов, которые не нужно учитывать
        ArrayList<String> listOfCharNotWorkWith = new ArrayList<>();
        for (int i = 0; i < args[3].toCharArray().length; i++) {
            listOfCharNotWorkWith.add(Character.toString(args[3].toCharArray()[i]));
        }
        //Добавляем в исключения символы из дополнительного условия
        if (!args[8].equals("")) {
            for (int i = 0; i < args[8].toCharArray().length; i++) {
                if (isChar(Character.toString(args[8].toCharArray()[i]))) {
                    listOfCharNotWorkWith.add(Character.toString(args[8].toCharArray()[i]));
                }
            }
        }
        //Проверяем дополнительное условие на валидность.
        if (args[8].length() > 0 && (args[8].length() > 4 || args[8].length() < 3)) {
            throw new Exception("Дополнительное условие не верно");
        }

        resourceSystem = args[0];
        idClass = args[1];
        alphabet = listOfAlphabet;
        charEquals = args[2];
        charNotWorkingWith = listOfCharNotWorkWith;
        charEqualNumbers = args[4];
        displacement = args[5];
        stringForConcatenationCondition = args[6];
        stringForConcatenationTestMSISDN = args[7];
        addCondition = args[8];
    }

    public static String generateInsert(String MSISDN, int numberOfRowInCondition, boolean onlyCondition) throws Exception {
        //Проверка номера на невалидные символы.
        isInvalidCharacter(MSISDN, numberOfRowInCondition);

        String insert = "";
        if (!onlyCondition) {
            if (resourceSystem.equals("LIS")) {
                insert = "insert into LIS_NUMBER_CLASS_CONDITIONS (NCLSC_ID, NAVI_DATE, NAVI_USER, CONDITION_STRING, IS_ACTIVE, NAME, TEST_MSISDN, MRGN_MRGN_ID, NCLST_NCLST_ID)\n" +
                        "values (NCLSC_SEQ.nextval, sysdate, 'Admin', '" + stringForConcatenationCondition + parseMSISDN(MSISDN) + "', 'Y', '" + MSISDN + "', '" + stringForConcatenationTestMSISDN + genTestMSISDN(MSISDN) + "', 1, " + idClass + ");\n";
            }
            if (resourceSystem.equals("RIM")) {
                insert = "insert into NUMBER_CLASS_TEMPLATES (NCTE_ID, NCLS_NCLS_ID, DEF, TEMPLATE, ACTIVE_YN, TEST_NUMBER, NAVI_USER, NAVI_DATE, BRNC_BRNC_ID)\n" +
                        "values (NCTE_SEQ.nextval, " + idClass + ", '" + MSISDN + "', '" + stringForConcatenationCondition + parseMSISDN(MSISDN) + "', 'Y', '" + stringForConcatenationTestMSISDN + genTestMSISDN(MSISDN) + "', 'BIS', sysdate, 0);\n";
            }
        } else {
            insert = stringForConcatenationCondition + parseMSISDN(MSISDN);
        }

        return insert;
    }

    private static String parseMSISDN(String MSISDN) {//Метод парсит маски номеров в условия для БД.
        char[] arrayOfMSISDN = MSISDN.toCharArray();
        ArrayList<ArrayList<Integer>> whereFound = new ArrayList<>();
        ArrayList<Integer> howMachRepeat = new ArrayList<>();
        //Начало блока заполенения массивов, где именно (whereFound) и сколько раз повтаряются (howMachRepeat) символы в маске.
        //Заполнение стандартными значениями.
        for (int i = 0; i < arrayOfMSISDN.length; i++) {
            if (i >= 10) {
                whereFound.add(new ArrayList<Integer>());
                whereFound.get(i).add(i);
                howMachRepeat.add(0);
            }
            if (i < 10) {
                whereFound.add(new ArrayList<Integer>());
                whereFound.get(i).add(i);
                howMachRepeat.add(0);
            }
        }
        //Заполнение конкретными значениями.
        for (int i = 0; i < arrayOfMSISDN.length - 1; i++) {//Берем каждый символ в маске и сравниваем его со всеми последующими
            for (int a = i + 1; a < arrayOfMSISDN.length; a++) {
                String letterWhichCompare = Character.toString(arrayOfMSISDN[i]);
                String letterWithWhichCompared = Character.toString(arrayOfMSISDN[a]);
                if (charNotWorkingWith.size() != 0) {
                    if (letterWhichCompare.equals(letterWithWhichCompared) && isChar(letterWhichCompare) && !wasBefore(letterWhichCompare, MSISDN, howMachRepeat, i)/*Метод проверяет, обрабатывался ли символ до текущей итерации цикла или нет*/ && !charNotWorkingWith.contains(letterWhichCompare)) {
                        whereFound.get(i).add(a);//При совпадении добавляем адрес, где встретился символ
                        howMachRepeat.set(i, howMachRepeat.get(i) + 1);//И добавляем количесвто повторений
                    }
                }
                if (charNotWorkingWith.size() == 0) {
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
                            if (charNotWorkingWith.size() != 0) {
                                if (!charNotWorkingWith.contains(Character.toString(arrayOfMSISDN[k])) && !charNotWorkingWith.contains(Character.toString(arrayOfMSISDN[j]))) {
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
                            if (charNotWorkingWith.size() == 0) {
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
                            if (charNotWorkingWith.size() != 0) {
                                if (!charNotWorkingWith.contains(Character.toString(arrayOfMSISDN[j]))) {
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
                            if (charNotWorkingWith.size() == 0) {
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
        //КОД НИЖЕ ВСЕГДА ДОЛЖЕН БЫТЬ ПОСЛЕДНИМ, ТАК КАК ЧИСТЯТСЯ МАССИВЫ whereFound и howMachRepeat.
        if (!addCondition.equals("")) {
            //Чистим массивы
            whereFound = new ArrayList<ArrayList<Integer>>();
            howMachRepeat = new ArrayList<Integer>();
            //Заполняем заново по той же схеме, но только с учетом символов из доп условия, которые были проигнорированны в первый раз.
            for (int i = 0; i < arrayOfMSISDN.length; i++) {
                if (i >= 10) {
                    whereFound.add(new ArrayList<Integer>());
                    whereFound.get(i).add(i);
                    howMachRepeat.add(0);
                }
                if (i < 10) {
                    whereFound.add(new ArrayList<Integer>());
                    whereFound.get(i).add(i);
                    howMachRepeat.add(0);
                }
            }
            for (int i = 0; i < arrayOfMSISDN.length - 1; i++) {//Берем каждый символ в маске и сравниваем его со всеми последующими
                for (int a = i + 1; a < arrayOfMSISDN.length; a++) {
                    String letterWhichCompare = Character.toString(arrayOfMSISDN[i]);
                    String letterWithWhichCompared = Character.toString(arrayOfMSISDN[a]);
                    if (charNotWorkingWith.size() != 0) {
                        if (letterWhichCompare.equals(letterWithWhichCompared) && isChar(letterWhichCompare) && !wasBefore(letterWhichCompare, MSISDN, howMachRepeat, i)/*Метод проверяет, обрабатывался ли символ до текущей итерации цикла или нет*/
                                && charNotWorkingWith.contains(letterWhichCompare)/*!!!!!Все тоже самое кроме этой строчки, здесь учитываются только те символы, которые включены в исключения т.к.
                                это обязательно для сепарирования дополнительного условия от основного, так же все буквы из дополнительного условия должны быть в charNotWorkingWith!!!!!!!!*/) {
                            whereFound.get(i).add(a);//При совпадении добавляем адрес, где встретился символ
                            howMachRepeat.set(i, howMachRepeat.get(i) + 1);//И добавляем количесвто повторений
                        }
                    }
                }
            }
            for (int b = 0; b < arrayOfMSISDN.length; b++) {
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
            //Парсим условие
            String operator = addCondition.toCharArray().length == 4 ? "!=" : "=";
            ArrayList<String> listOfMSISDN = new ArrayList<>();
            for (int m = 0; m < arrayOfMSISDN.length; m++) {//массив с символами из номера переносим в лист для удобства
                listOfMSISDN.add(Character.toString(arrayOfMSISDN[m]));
            }
            //Если в номере оба символа из условия то ставим между ними оператор
            if (listOfMSISDN.contains(Character.toString(addCondition.toCharArray()[0])) && listOfMSISDN.contains(Character.toString(addCondition.toCharArray()[addCondition.toCharArray().length - 1]))) {
                result = result + " and :" + (MSISDN.indexOf(addCondition.toCharArray()[0]) + Integer.parseInt(displacement)) + " " + operator + " :" + (MSISDN.indexOf(addCondition.toCharArray()[addCondition.toCharArray().length - 1]) + Integer.parseInt(displacement));
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

    private static String genTestMSISDN(String MSISDN) throws Exception {
        ArrayList<String> listOfNumbersForGenerate = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        String result = "";
        if (charEqualNumbers.equals("A=1")) {
            char[] arrayOfMSISDN = MSISDN.toCharArray();
            for (int p = 0; p < arrayOfMSISDN.length; p++) {
                if (isChar(Character.toString(arrayOfMSISDN[p]))) {
                    for (int i = 0; i < alphabet.size(); i++) {
                        if (alphabet.get(i).equals(Character.toString(arrayOfMSISDN[p]))) {
                            //Если в маске встретился символ и он совпал с символом из алфавита, то замещаем его цифрой, которой соответсвтует тот же интдекс что и символу из алфавита
                            arrayOfMSISDN[p] = listOfNumbersForGenerate.get(i).charAt(0);
                        }
                    }
                }
            }
            for (int y = 0; y < arrayOfMSISDN.length; y++) {
                result = result + arrayOfMSISDN[y];
            }
            return result;
        }
        if (charEqualNumbers.equals("A!=1")) {
            char[] arrayOfMSISDN = MSISDN.toCharArray();
            //Формируем отдельно для каждой маски массив букв и цифр, а так же массив тех цифр, что не встречаются в маске
            ArrayList<String> ArrayOfNumeralInNumber = new ArrayList<>();
            ArrayList<String> ArrayOfCharInNumber = new ArrayList<>();
            ArrayList<String> ArrayOfNumeralForReplace = new ArrayList<>();
            //Заполняем массивы букв и цифр
            for (int p = 0; p < arrayOfMSISDN.length; p++) {
                if (!isChar(Character.toString(arrayOfMSISDN[p]))) {
                    ArrayOfNumeralInNumber.add(Character.toString(arrayOfMSISDN[p]));
                }
                if (isChar(Character.toString(arrayOfMSISDN[p])) && !ArrayOfCharInNumber.contains(Character.toString(arrayOfMSISDN[p]))) {
                    ArrayOfCharInNumber.add(Character.toString(arrayOfMSISDN[p]));
                }
            }
            //Если их сумма больше 10, то выбрасываем исключение
            if (ArrayOfCharInNumber.size() + ArrayOfNumeralInNumber.size() > 10) {
                throw new Exception("В маске " + MSISDN + " слишком много символов, так как цифры на их месте не могут быть равны цифрам из маски, то цифр просто не хватит.");
            }
            //Теперь формируем из массива всех цифр, массив тех, которых нет в маске
            for (int i = 0; i < listOfNumbersForGenerate.size(); i++) {
                if (!ArrayOfNumeralInNumber.contains(listOfNumbersForGenerate.get(i))) {
                    ArrayOfNumeralForReplace.add(listOfNumbersForGenerate.get(i));
                }
            }

            //
            for (int p = 0; p < arrayOfMSISDN.length; p++) {
                if (isChar(Character.toString(arrayOfMSISDN[p]))) {
                    for (int i = 0; i < ArrayOfCharInNumber.size(); i++) {
                        if (ArrayOfCharInNumber.get(i).equals(Character.toString(arrayOfMSISDN[p]))) {
                            //Если в маске встретился символ и он совпал с символом из ArrayOfCharInNumber, то замещаем его цифрой из ArrayOfNumeralForReplace, которой соответсвтует тот же интдекс что и символу из алфавита
                            arrayOfMSISDN[p] = ArrayOfNumeralForReplace.get(i).charAt(0);
                        }
                    }
                }
            }

            for (int y = 0; y < arrayOfMSISDN.length; y++) {
                if (!isChar(Character.toString(arrayOfMSISDN[y]))) {
                    result = result + arrayOfMSISDN[y];
                }
                if (isChar(Character.toString(arrayOfMSISDN[y]))) {
                    result = result + arrayOfMSISDN[y];
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