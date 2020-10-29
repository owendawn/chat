package cn.hs.guangzhou.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.CellBase;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2020/7/20 10:50
 * Excel操作工具类
 *
 *
 *       <dependency>
 *             <groupId>org.apache.poi</groupId>
 *             <artifactId>poi-ooxml</artifactId>
 *             <version>4.1.0</version>
 *         </dependency>
 *
 *         
 * @author owen pan
 */
public class ExcelUtil {

    public static String getStringVal(CellBase cell) {
        if (cell == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case NUMERIC:
                //日期数据返回LONG类型的时间戳
                if ("yyyy\"年\"m\"月\"d\"日\";@".equals(cell.getCellStyle().getDataFormatString())) {
                    //System.out.println(cell.getNumericCellValue()+":日期格式："+cell.getCellStyle().getDataFormatString());
                    return format.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()).getTime());
                } else {
                    //数值类型返回double类型的数字
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }

    /**
     * 2020/7/15 12:30
     * 要求excel版本在2003及以前 .xls
     *
     * @param file       文件
     * @param sheetIndex tab页游标编码,从0开始
     * @return {@code java.util.List<java.util.List<java.lang.String>>}
     * @author owen pan
     */
    public static List<List<String>> readExcelOfXls(File file, int sheetIndex) throws Exception {
        if (!file.exists()) {
            throw new Exception("找不到文件:" + file.getAbsolutePath());
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return readExcelOfXls(fis, sheetIndex);
        }
    }

    public static List<List<String>> readExcelOfXls(InputStream inputStream, int sheetIndex) throws Exception {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
        int size = hssfWorkbook.getNumberOfSheets();
        System.out.println("sheet页数总量：" + size + "，读取第" + (sheetIndex + 1) + "页");
        // 读取表格内容
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(sheetIndex);
        List<List<String>> result = new ArrayList<>();
        if (hssfSheet != null) {
            for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                if (hssfRow == null) {
                    continue;
                }
                int minColIx = hssfRow.getFirstCellNum();
                int maxColIx = hssfRow.getLastCellNum();
                List<String> rowList = new ArrayList<>();
                for (int colIx = minColIx; colIx < maxColIx; colIx++) {
                    HSSFCell cell = hssfRow.getCell(colIx);

                    rowList.add(getStringVal(cell));
                }
                result.add(rowList);
            }
        }
        return result;
    }


    /**
     * 2020/7/15 12:42
     * 要求excel版本在2007以上
     *
     * @param file       文件
     * @param sheetIndex tab页游标编码,从0开始
     * @return {@code java.util.List<java.util.List<java.lang.Object>>}
     * @author owen pan
     */
    public static List<List<String>> readExcelOfXlsx(File file, int sheetIndex) throws Exception {
        if (!file.exists()) {
            throw new Exception("找不到文件:" + file.getAbsolutePath());
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return readExcelOfXlsx(fis, sheetIndex);
        }
    }

    public static List<List<String>> readExcelOfXlsx(InputStream fileInputStream, int sheetIndex) throws Exception {
        XSSFWorkbook xwb = new XSSFWorkbook(fileInputStream);
        int size = xwb.getNumberOfSheets();
        System.out.println("sheet页数总量：" + size + "，读取第" + (sheetIndex + 1) + "页");
        // 读取表格内容
        XSSFSheet sheet = xwb.getSheetAt(sheetIndex);
        List<List<String>> list = new LinkedList<>();
        for (int i = (sheet.getFirstRowNum() + 1); i <= (sheet.getPhysicalNumberOfRows() - 1); i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            List<String> linked = new LinkedList<>();
            for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
                linked.add(getStringVal(row.getCell(j)));
            }
            if (linked.size() != 0) {
                list.add(linked);
            }
        }
        return list;
    }


    /**
     * 2020/7/15 13:42
     * 智能识别并读取excel版本
     *
     * @param file       文件
     * @param sheetIndex tab页游标编码,从0开始
     * @return {@code java.util.List<java.util.List<java.lang.String>>}
     * @author owen pan
     */
    public static List<List<String>> readExcel(File file, int sheetIndex) throws Exception {
        try (
                InputStream is = new FileInputStream(file);
                PushbackInputStream pis = new PushbackInputStream(is, 8);
        ) {
            new HSSFWorkbook(is);
            System.out.println("Excel2003 版本");
            if (pis != null) {
                pis.close();
            }
            if (is != null) {
                is.close();
            }
            return readExcelOfXls(file, sheetIndex);
        } catch (OfficeXmlFileException e) {
            if (e.getMessage().contains("The supplied data appears to be in the Office 2007+ XML")) {
                System.out.println("Excel2007 版本");
                return readExcelOfXlsx(file, sheetIndex);
            } else {
                throw e;
            }
        }
    }

    /**
     * 2020/7/15 12:43
     * 导出excel
     *
     * @param target   导出的excel路径（需要带.xlsx)
     * @param headList excel的标题备注名称
     * @param dataList excel数据
     * @author owen pan
     */
    public static void createExcelOfXlsx(File target, List<String> headList, List<List<String>> dataList) throws Exception {
        try (FileOutputStream fis = new FileOutputStream(target)) {
            createExcelOfXlsx(fis, headList, dataList);
        }
    }

    public static void createExcelOfXlsx(OutputStream os, List<String> headList, List<List<String>> dataList) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headList.size(); i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(headList.get(i));
        }
        // ===============================================================
        for (int n = 0; n < dataList.size(); n++) {
            XSSFRow row_value = sheet.createRow(n + 1);
            List<String> cellList = dataList.get(n);
            for (int i = 0; i < cellList.size(); i++) {
                XSSFCell cell = row_value.createCell(i);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(cellList.get(i));
            }
        }
        workbook.write(os);
        os.flush();
        os.close();
    }

    /**
     * 2020/7/20 10:51
     * 下载excel
     * @param response 返回请求体
     * @param fileName 下载文件名
     * @param headList excel头
     * @param dataList  excel数据
     * @author owen pan
     */
    public static void downloadExcelOfXlsx(HttpServletResponse response, String fileName, List<String> headList, List<List<String>> dataList) throws Exception {
        response.setContentType("octets/stream");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + new String(fileName.getBytes("UTF-8"), "iso8859-1") + "\"");
        OutputStream outputStream = response.getOutputStream();
        createExcelOfXlsx(outputStream, headList, dataList);
    }

    public static void main(String[] args) {
        List<List<String>> ll = null;
        try {
//            ll = readExcel(new File("C:\\Users\\owen-c2-pc\\Desktop/截面串口服务器IP信息.xls"), 0);
            ll = readExcel(new File("C:\\Users\\owen-c2-pc\\Desktop/衢州航标详细清单表-20200607.xlsx"), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Optional.ofNullable(ll).orElse(Collections.emptyList()).stream().map(Object::toString).collect(Collectors.joining("\n")));
    }
}
