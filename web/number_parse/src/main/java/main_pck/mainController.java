package main_pck;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


@Controller
public class mainController {
    @RequestMapping
    public String main_page() {
        return "main_page";
    }

    @RequestMapping("/insert")
    public ModelAndView insert_page(HttpServletRequest request) throws Exception {
        //Определение параметров из формы
        String charEqualNumbers = "";
        String charEquals = "";
        String displacement = "1";
        boolean onlyCondition = false;
        try {
            request.getParameter("charEqualNumbers").equals("1");
            charEqualNumbers = "A=1";
        } catch (Exception e) {
            charEqualNumbers = "A!=1";
        }
        try {
            request.getParameter("charEquals").equals("1");
            charEquals = "A=B";
        } catch (Exception e) {
            charEquals = "A!=B";
        }
        if (!request.getParameter("displacement").equals("")) {
            displacement = request.getParameter("displacement");
        }
        try {
            request.getParameter("onlyCondition").equals("1");
            onlyCondition = true;
        } catch (Exception e) {
        }
        //Парсим номера из условия в форме в массив
        char[] arrayOfCharsMSISDN = request.getParameter("listOfNumbersName").toCharArray();
        String MSISDN = "";
        ArrayList<String> listOfMSISDN = new ArrayList<>();
        for (int i = 0; i < arrayOfCharsMSISDN.length; i++) {
            if (i == arrayOfCharsMSISDN.length - 1 || arrayOfCharsMSISDN[i] == ',') {
                if (i == arrayOfCharsMSISDN.length - 1) {
                    MSISDN = MSISDN + arrayOfCharsMSISDN[i];
                }
                listOfMSISDN.add(MSISDN);
                MSISDN = "";
            }
            if (arrayOfCharsMSISDN[i] != ',') {
                MSISDN = MSISDN + arrayOfCharsMSISDN[i];
            }
        }
        //Заполняем параметры
        number_parse.fillParams(new String[]{
                request.getParameter("resourceSystem").toUpperCase(),
                request.getParameter("idClass"),
                charEquals,
                request.getParameter("charNotWorkingWith").toUpperCase(),
                charEqualNumbers,
                displacement,
                request.getParameter("stringForConcatenationCondition"),
                request.getParameter("stringForConcatenationTestMSISDN"),
                request.getParameter("addCondition")
        }, listOfMSISDN);
        //Заполняем результирующий массив
        ArrayList<String> resultList = new ArrayList<>();
        for (int i = 0; i < listOfMSISDN.size(); i++) {
            resultList.add(number_parse.generateInsert(listOfMSISDN.get(i), i+1, onlyCondition));
        }
        //Формируем результат
        ModelAndView mv = new ModelAndView();
        mv.setViewName("main_page");
        mv.addObject("resultlist", resultList);
        mv.addObject("condition", request.getParameter("listOfNumbersName"));
        mv.addObject("paramResourceSystem", request.getParameter("resourceSystem").toUpperCase());
        mv.addObject("paramIdClass", request.getParameter("idClass").toUpperCase());
        mv.addObject("paramCharNotWorkingWith", request.getParameter("charNotWorkingWith").toUpperCase());
        mv.addObject("paramDisplacement", displacement.equals("1") ? "" : displacement);
        mv.addObject("paramStringForConcatenationCondition", request.getParameter("stringForConcatenationCondition"));
        mv.addObject("paramStringForConcatenationTestMSISDN", request.getParameter("stringForConcatenationTestMSISDN"));
        mv.addObject("paramAddCondition", request.getParameter("addCondition"));
        mv.addObject("paramOnlyCondition", onlyCondition);
        mv.addObject("paramCharEqualNumbers", charEqualNumbers);
        mv.addObject("paramCharEquals", charEquals);
        return mv;
    }
}
