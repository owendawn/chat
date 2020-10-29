package com.uv.hydrology.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 2019/11/12 10:25
 *
 * @author owen pan
 */
public class ByteUtil {
    /**
     * hex转byte数组
     */
    public static byte[] hexToBytes(String hex) {
        int hexlen = hex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            hex = "0" + hex;
        } else {
            //偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = (byte) Integer.parseInt((hex.substring(i, i + 2)), 16);
            j++;
        }
        return result;
    }

    /**
     * byte数组转hex
     */
    public static String bytesToHex(byte[] bytes) {
        String strHex;
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            // 每个字节由两个字符表示，位数不够，高位补0
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }

    /**
     *  Byte[] 转 byte[]
     */
    public static byte[] byteObjectsToBytes(Byte[] arr) {
        byte[] bytes = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            bytes[i] = arr[i];
        }
        return bytes;
    }

    /**
     * byte[] 转 Byte[]
     */
    public static Byte[] bytesToByteObjects(byte[] arr) {
        Byte[] bytes = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            bytes[i] = arr[i];
        }
        return bytes;
    }
    /**
     * int转bytes
     */
    public static byte[] longToBytes(long val) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((val >> offset) & 0xff);
        }
        return buffer;
    }
    /**
     * int转bytes
     */
    public static byte[] intToBytes(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);
        return b;
    }

    /**
     * short 转bytes
     */
    public static byte[] shortToBytes(short val) {
        byte[] b = new byte[2];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        return b;
    }

    /**
     * byte[] 转 long
     */
    public static long bytesToLong(byte[] bytes) {
        long  values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8;
            values|= (bytes[i] & 0xff);
        }
        return values;
    }
    /**
     * byte[] 转 int
     */
    public static int bytesToInt(byte[] bytes) {
        int int1 = bytes[0] & 0xff;
        int int2 = (bytes[1] & 0xff) << 8;
        int int3 = (bytes[2] & 0xff) << 16;
        int int4 = (bytes[3] & 0xff) << 24;
        return int1 | int2 | int3 | int4;
    }
    /**
     * byte[] 转 short
     */
    public static short bytesToShort(byte[] bytes) {
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xff));
    }

    public static <T> T bytesToNumber(byte[] bytes, Class<T> tClass) {
        if (tClass.getName().equals("java.lang.Integer")) {
            return tClass.cast(bytesToInt(bytes));
        } else if (tClass.getName().equals("java.lang.Short")) {
            return tClass.cast(bytesToShort(bytes));
        } else {
            return tClass.cast(bytesToLong(bytes));
        }
    }

    /**
     * byte[a] append byte[b] to new byte[a+b]
     */
    public static byte[] append(byte[] a, byte[] b) {
        byte[] arr=new byte[a.length+b.length];
        System.arraycopy(a,0,arr,0,a.length);
        System.arraycopy(b,0,arr,a.length,b.length);
        return arr;
    }

    public static byte[] subArray(byte[] a,int from ,int to) {
        byte[] arr=new byte[to-from];
        System.arraycopy(a,from,arr,0,to-from);
        return arr;
    }

    public static float bytesToFloat(byte[] bytes){
        ByteBuffer buf=ByteBuffer.allocateDirect(4);
        //默认大端，小端用这行
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(bytes);
        buf.rewind();
        return buf.getFloat();
    }

    public static void main(String[] args) {
        System.out.println(ByteUtil.bytesToFloat(ByteUtil.hexToBytes("1904563e")));
//        String str = "hello";
//        long begin = System.currentTimeMillis();
//        long now;
//        System.out.println(bytesToHex(str.getBytes()));
//
//        System.out.println(System.currentTimeMillis() - begin);
//        now = System.currentTimeMillis();
////        System.out.println(byteToHex2(str.getBytes()));
//        System.out.println(System.currentTimeMillis() - now);
//
//        System.out.println(bytesToInt(intToBytes(11)));
//        System.out.println(bytesToNumber(intToBytes((short) 11), Short.class));
    }


}
