package com.hh.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelUtil {
    /*
     * 保存文件
     */
    @SuppressWarnings("static-access")
    public static boolean saveFile(File file, String filename, String path) {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                out = new BufferedOutputStream(new FileOutputStream(new File(path + file.separator + filename)));
                byte[] buffer = new byte[1024 * 10];
                while (in.read(buffer) > 0) {
                    out.write(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            }
        } catch (Exception er) {
            er.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 读Excel文件
     *
     * @param file
     * @return
     */
    public static List readXLSFile(File file, String type) {
        if (type.equals("2003")) {
            return readXLSFile2003(file);
        } else if (type.equals("2007")) {
            return readXLSXFile2007(file);
        } else {
            System.out.println("导入文件类型错误");
            return new ArrayList();
        }
    }

    /**
     * .xls后缀
     * 读取excel2003文件数据
     */
    private static List readXLSFile2003(File file) {
        try {
            Workbook book = Workbook.getWorkbook(file);
            Sheet[] mysheet = book.getSheets();        //读页数(支持多页)
            List<String[]> xclList = new ArrayList<String[]>();
            for (int k = 0; k < mysheet.length; k++) {
                Sheet sheet = book.getSheet(k);
                int col = sheet.getColumns();
                int row = sheet.getRows();

                for (int i = 0; i < row; i++) {
                    String[] rowvalue = new String[col];
                    for (int j = 0; j < col; j++) {
                        Cell cell1 = sheet.getCell(j, i);
                        if (cell1.getType() == CellType.DATE) { //手动填写模板文件时为 date 类型，其他情况有可能不是date类型
                            DateCell dc = (DateCell) cell1;
                            Date date = dc.getDate();
                            TimeZone zone = TimeZone.getTimeZone("GMT");
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss");
                            sdf.setTimeZone(zone);
                            String sDate = sdf.format(date);
                            rowvalue[j] = sDate;
                        } else {
                            String result = cell1.getContents().trim();
                            rowvalue[j] = result;
                        }
                    }
                    //整条记录只要有一个字段不为空，就认为记录有效，否则忽略该记录。
                    for (String value : rowvalue) {
                        if (value.length() > 0) {
                            xclList.add(rowvalue);
                            break;
                        }
                    }
                }
            }
            return xclList;
        } catch (Exception e) {
            //LogUtil.logERR(e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读EXCEL2007
     * .xlsx后缀
     *
     * @param file
     * @return
     */
    private static List readXLSXFile2007(File file) {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
            XSSFWorkbook xwb = new XSSFWorkbook(fs);
            List<String[]> xclList = new ArrayList<String[]>();
            for (int s = 0; s < xwb.getNumberOfSheets(); s++) {
                XSSFSheet sheet = xwb.getSheetAt(s);
                XSSFRow row;
                for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {    //行
                    row = sheet.getRow(i);
                    String[] rowvalue = new String[row.getLastCellNum()];
                    for (int j = 0; j < row.getLastCellNum(); j++) {    //列
                        if (row.getCell(j) != null) {
                            int style = row.getCell(j).getCellType();
                            if (style == 0) {
                                rowvalue[j] = row.getCell(j).getRawValue().trim();
                            } else if (style == 1) {
                                rowvalue[j] = row.getCell(j).getStringCellValue().trim();
                            }
                        } else {
                            rowvalue[j] = "";
                        }
                    }
                    //整条记录只要有一个字段不为空，就认为记录有效，否则忽略该记录。
                    for (String value : rowvalue) {
                        if (value.length() > 0) {
                            xclList.add(rowvalue);
                            break;
                        }
                    }
                }
            }
            return xclList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void createExcelStream(HttpServletResponse response, String filename, String[] heads, List<String[]> datalist) {
        OutputStream os = null;
        WritableWorkbook wbook = null;
        try {
            int maxcount = 65536;
            os = response.getOutputStream();
            wbook = Workbook.createWorkbook(os);
            int recodcount = datalist.size();
            int sheetnum = (recodcount % (maxcount - 1) == 0) ? recodcount / (maxcount - 1) : (recodcount) / (maxcount - 1) + 1;
            sheetnum = sheetnum > 0 ? sheetnum : 1;
            for (int m = 0; m < sheetnum; m++) {
                WritableSheet wsheet = wbook.createSheet("Sheet_" + m, m);
                if (heads != null && heads.length > 0) {
                    for (int i = 0; i < heads.length; i++) {
                        Label label = new Label(i, 0, heads[i]);
                        wsheet.addCell(label);
                        label = null;
                    }
                }

                int endindex = (m + 1) * (maxcount - 1);
                if (endindex > datalist.size()) {
                    endindex = datalist.size();
                }
                for (int i = m * (maxcount - 1), k = 0; i < endindex; i++, k++) {
                    for (int j = 0; j < datalist.get(i).length; j++) {
                        Label label = new Label(j, k + 1, ((datalist.get(i)[j] != null && !datalist.get(i)[j].equals("null")) ? datalist.get(i)[j] : ""));
                        wsheet.addCell(label);
                        label = null;
                    }
                }
            }

            response.setHeader("Content-disposition", "attachment;" + "filename=" + new String(filename.getBytes("GBK"), "ISO_8859_1") + ".xls");
            response.setContentType("application/vnd.ms-excel");
            wbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (wbook != null) {
                try {
                    wbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void main(String[] args) {
        try {
            List<HashMap> lists = null;
//            lists =excelDao.loadZdypxListOfExcel();
            List<String[]> list=new ArrayList();
            for(int i=0;i<lists.size();i++){
                list.add(
                        new String[]{
                                lists.get(i).get("c_name").toString(),
                                lists.get(i).get("i_record_location_set")==null?"":lists.get(i).get("i_record_location_set").toString(),
                                lists.get(i).get("c_description")==null?"":lists.get(i).get("c_description").toString()
                        }
                );
            }
            ExcelUtil.createExcelStream(AjaxUtils.getResponseOfHtml(), "文件名", new String[]{"name", "order","gjz"}, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
