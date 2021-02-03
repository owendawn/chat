package com.uv.gas.detection.utils;

import io.netty.buffer.ByteBuf;

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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public static String bytesToHex(byte[] bytes) {
        String strHex;
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            // 每个字节由两个字符表示，位数不够，高位补0
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }

    /**
     * Byte[] 转 byte[]
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public static byte[] shortToBytes(short val) {
        byte[] b = new byte[2];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        return b;
    }

    /**
     * byte[] 转 long
     */
    @SuppressWarnings("unused")
    public static long bytesToLong(byte[] bytes) {
        long values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8;
            values |= (bytes[i] & 0xff);
        }
        return values;
    }

    /**
     * byte[] 转 int
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public static short bytesToShort(byte[] bytes) {
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xff));
    }

    @SuppressWarnings("unused")
    public static <T> T bytesToNumber(byte[] bytes, Class<T> tClass) {
        if ("java.lang.Integer".equals(tClass.getName())) {
            return tClass.cast(bytesToInt(bytes));
        } else if ("java.lang.Short".equals(tClass.getName())) {
            return tClass.cast(bytesToShort(bytes));
        } else {
            return tClass.cast(bytesToLong(bytes));
        }
    }

    /**
     * byte[a] append byte[b] to new byte[a+b]
     */
    @SuppressWarnings("unused")
    public static byte[] append(byte[] a, byte[] b) {
        byte[] arr = new byte[a.length + b.length];
        System.arraycopy(a, 0, arr, 0, a.length);
        System.arraycopy(b, 0, arr, a.length, b.length);
        return arr;
    }

    @SuppressWarnings("unused")
    public static byte[] subArray(byte[] a, int from, int to) {
        byte[] arr = new byte[to - from];
        System.arraycopy(a, from, arr, 0, to - from);
        return arr;
    }

    @SuppressWarnings("unused")
    public static byte[] appendArray(byte[] a, byte[] b) {
        byte[] arr = new byte[a.length + b.length];
        System.arraycopy(a, 0, arr, 0, a.length);
        System.arraycopy(b, 0, arr, a.length, b.length);
        return arr;
    }

    @SuppressWarnings("unused")
    public static float bytesToFloat(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocateDirect(4);
        //默认大端，小端用这行
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(bytes);
        buf.rewind();
        return buf.getFloat();
    }

    @SuppressWarnings("unused")
    public static int bytesIndexOf(byte[] bytes, byte[] search) {
        if (bytes == null || bytes.length <= 0) {
            throw new RuntimeException("error raw bytes");
        }
        if (search == null || search.length <= 0) {
            throw new RuntimeException("error search bytes");
        }
        for (int i = 0; i < bytes.length; i++) {
            byte first = bytes[i];
            if (first == search[0]) {
                boolean has = true;
                if (i + search.length <= bytes.length) {
                    for (int j = 1; j < search.length; j++) {
                        if (bytes[i + j] != search[j]) {
                            has = false;
                        }
                    }
                }
                if (has) {
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public static int bytesIndexOf(byte[] bytes, byte search) {
        return bytesIndexOf(bytes, search, 0);
    }

    @SuppressWarnings("unused")
    public static int bytesIndexOf(byte[] bytes, byte search, int from) {
        if (bytes == null || bytes.length <= 0) {
            throw new RuntimeException("error raw bytes");
        }
        for (int i = from; i < bytes.length; i++) {
            if (bytes[i] == search) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public static String bytesToHexWithPlaceholder(byte[] bytes, String placeholder) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte aByte : bytes) {
            String sTemp = Integer.toHexString(0xFF & aByte);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
            sb.append(placeholder);
        }
        return sb.toString().substring(0, sb.length() - placeholder.length());
    }

    @SuppressWarnings("unused")
    public static ByteBuffer bigToLittileByteBuffer(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.compact();
        return buffer;
    }

    @SuppressWarnings("unused")
    public static byte[] bigToLittileBytes(byte[] bytes) {
        byte[] reverseArray = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reverseArray[i] = bytes[bytes.length - i - 1];
        }
        return reverseArray;
    }

    @SuppressWarnings("unused")
    public static ByteBuffer toByteBufferOfBig(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(bytes);
        buffer.compact();
        return buffer;
    }

    /**
     * ======================================= 以下仅提供给netty =================================
     */
    @SuppressWarnings("unused")
    public static String byteBuffToHex(ByteBuf byteBuf, String placeHolder) {
        byte[] bytes;
        if (byteBuf.hasArray()) {
            bytes = byteBuf.array();
        } else {
            bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(0, bytes);
        }
        return bytesToHexWithPlaceholder(bytes, placeHolder);
    }

    public static void main(String[] args) {
 
    }


}
