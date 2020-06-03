package com.hh.gismiddleware.util;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2018/12/25 11:21
 *
 * @author owen pan
 */
public class CoordsUtil {
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

    public static boolean contain(List<Side> sides, Point2D.Double point) {
        //水平射线，交点算法，奇数为内部，偶数为外部
        if (sides.size() >= 3) {
            AtomicInteger intersectR = new AtomicInteger(0);
            AtomicInteger intersectL = new AtomicInteger(0);
            for (Side it : sides){
                //ax+b=0x+c --> x=(c-b)/a
                if (it.a == 0 && it.b == point.y) {
                    return true;
                }else if(it.a!=0){
                    double x=(point.y-it.b)/it.a;
                    if(it.between(x)){
                        if(x>=point.x) {
                            intersectR.incrementAndGet();
                        }else {
                            intersectL.incrementAndGet();
                        }
                    }
                }
            }
            return intersectR.get() % 2 != 0||intersectL.get() % 2 != 0;
        }
        return false;
    }

    /**
     * 2018/12/25 10:24
     * 生成多边形 边集合
     *
     * @param polygon 坐标list
     * @return java.awt.geom.GeneralPath
     * @author owen pan
     */
    public static List<Side> newSides(List<Point2D.Double> polygon) {
        if (polygon != null && polygon.size() > 0) {
            List<Side> sides = new ArrayList<>();
            for (int i = 0; i < polygon.size() - 1; i++) {
                sides.add(new Side(polygon.get(i), polygon.get(i + 1)));
            }
            return sides;
        }
        return null;
    }

    public static class Side {
        /**
         * y=ax=b
         */
        private double a;
        private double b;
        private double min;
        private double max;

        public Side(Point2D.Double point1, Point2D.Double point2) {
            a = (point1.y - point2.y) / (point1.x - point2.x);
            b = (point1.x * point2.y - point2.x * point1.y) / (point1.x - point2.x);
            if (point1.x < point2.x) {
                min = point1.x;
                max = point2.x;
            } else {
                min = point2.x;
                max = point1.x;
            }
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }

        public boolean between(double x) {
            return x >= min && x <= max;
        }
    }
}
