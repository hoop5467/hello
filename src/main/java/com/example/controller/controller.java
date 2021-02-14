package com.example.controller;


import ch.qos.logback.classic.Logger;
import com.example.HelloApplication;
import com.sun.tools.javac.Main;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.TransformerException;

import javax.xml.xpath.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


@Controller
public class controller {
    private static Logger logger = (Logger) LoggerFactory.getLogger(HelloApplication.class);
    @PostMapping("/exelInput")
    public String home(MultipartHttpServletRequest request) throws IOException, ParserConfigurationException, SAXException, InterruptedException, XPathExpressionException, TransformerException {
        ArrayList<String> comNamelist = excel(request,0);
        ArrayList<String> list = excel(request, 1);
        ArrayList<String> result = req(list);
        mkExel(comNamelist,list,result);
        return "home";
    }

    public ArrayList<String> req(ArrayList<String> list) throws IOException, SAXException, ParserConfigurationException, InterruptedException, XPathExpressionException, TransformerException {
        ArrayList<String> result = new ArrayList<>();

        String postUrl = "https://teht.hometax.go.kr/wqAction.do?actionId=ATTABZAA001R08&screenId=UTEABAAA13&popupYn=false&realScreenId=";
        for(String value : list) {
            String val = value.replace("-","");
            String xmlRaw = "<map id=\"ATTABZAA001R08\"><pubcUserNo/><mobYn>N</mobYn><inqrTrgtClCd>1</inqrTrgtClCd><txprDscmNo>"+val+"</txprDscmNo><dongCode>15</dongCode><psbSearch>Y</psbSearch><map id=\"userReqInfoVO\"/></map>";
            URL url = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            con.setRequestProperty("Accept",  "application/xml; charset=UTF-8");
            con.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            writer.write(xmlRaw);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
            String xml = br.readLine();

            logger.warn(xml);
            if(xml.matches(".*폐업자.*")){
                result.add("폐업자 입니다.");
            }else if(xml.matches(".*휴업자.*")){
                result.add("휴업자 (과세유형: 부가가치세 일반과세자) 입니다.");
            }else if(xml.matches(".*부가가치세 일반과세자 입니다..*")){
                result.add("부가가치세 일반과세자 입니다.");
            }



            Thread.sleep(500);
            br.close();
            con.disconnect();
        }

        return result;
    }




    public ArrayList<String> excel(MultipartHttpServletRequest request, int colnum) throws IOException {
        request.setCharacterEncoding("UTF-8");
        MultipartFile file = request.getFile("exel");
        InputStream fis = file.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        int rowindex = 0;
        int columnindex = colnum;
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows();
        ArrayList<String> list = new ArrayList<>();
        for(rowindex = 1; rowindex<rows;rowindex++){
            XSSFRow row = sheet.getRow(rowindex);
            if(row !=null){
                int cells = row.getPhysicalNumberOfCells();
                    XSSFCell cell = sheet.getRow(rowindex).getCell(columnindex);
                    String value= "";
                    if(cell == null){
                        continue;
                    }else{
                        value = cell.getStringCellValue();
                        list.add(value);
                    }
                }
        }

        return list;
    }
    public void mkExel(ArrayList<String> comlist,ArrayList<String> numlist,ArrayList<String> reslist) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("companySheet");
        // 행 생성
        XSSFRow row = sheet.createRow(0);
        // 쎌 생성
        XSSFCell cell;


        cell = row.createCell(0);
        cell.setCellValue("거래처명");

        cell = row.createCell(1);
        cell.setCellValue("사업자번호");

        cell = row.createCell(2);
        cell.setCellValue("휴폐업조회결과");

        for(int rowidx=0; rowidx<reslist.size();rowidx++){
            row = sheet.createRow(rowidx+1);
            cell=row.createCell(0);
            cell.setCellValue(comlist.get(rowidx));
            cell=row.createCell(1);
            cell.setCellValue(numlist.get(rowidx));
            cell=row.createCell(2);
            cell.setCellValue(reslist.get(rowidx));

        }
        File path= new File("C:\\nunababo");

        if(!path.exists()){
            path.mkdirs();
        }
        String filename = "companylist"+System.currentTimeMillis();
        File file = new File("C:\\nunababo\\"+filename+".xlsx");
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        if(workbook!=null) workbook.close();
        if(fos!=null) fos.close();





    }
}
