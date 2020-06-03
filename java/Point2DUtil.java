package com.hh.gismiddleware.util;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 2018/12/24 15:35
 *
 * @author owen pan
 */
public class Point2DUtil {

    /**
     * 2018/12/24 16:07
     * 转换经纬度字符串
     *
     * @param str 118.289794,30.66791;120.767211,30.66791;120.767211,29.152221;118.289794,29.152221
     * @return java.util.List<java.awt.geom.Point2D.Double>
     * @author owen pan
     */
    public static List<Point2D.Double> parseToPointList(String str) {
        if (str != null) {
            str = str.replaceAll("\\s", "");
            if (str.length() > 0) {
                try {
                    List<Point2D.Double> list = new ArrayList<>();
                    String[] pstrs = str.split(";");
                    for (String pstr : pstrs) {
                        String[] ps = pstr.split(",");
                        list.add(new Point2D.Double(Double.parseDouble(ps[0]), Double.parseDouble(ps[1])));
                    }
                    return list;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 2018/12/25 10:24
     * 生成多边形点集合对象
     *
     * @param polygon 坐标list
     * @return java.awt.geom.GeneralPath
     * @author owen pan
     */
    public static GeneralPath newGeneralPath(List<Point2D.Double> polygon) {
        if (polygon != null && polygon.size() > 0) {
            GeneralPath peneralPath = new GeneralPath();
            Point2D.Double first = polygon.get(0);
            peneralPath.moveTo(first.x, first.y);
            polygon.remove(0);
            for (Point2D.Double d : polygon) {
                peneralPath.lineTo(d.x, d.y);
            }
            peneralPath.closePath();
            return peneralPath;
        }
        return null;
    }

    public static void main(String[] args) {
        boolean contain = Point2DUtil.newGeneralPath(new ArrayList<Point2D.Double>()).contains(new Point2D.Double());
        System.out.println(contain);
    }
}
