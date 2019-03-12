package com.example.atcpassistant.utils;

public class DataSynUtil {
    /**
     * 将字节数组转化为十六进制字符串
     *
     * @param bytes 要转化的字节数组
     * @param a 长度
     * @return 生成的字符串
     */
    public static String bytesToHexString(byte[] bytes, int a) {
        String result = "";
        for (int i = 0; i < a; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);// 将高24位置0
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    /**
     * 将字节数组转化为十六进制字符串
     *
     * @param b 要转化的字节数组
     * @return 生成的字符串
     */
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i =0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);// 将高24位置0
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * 将十六进制字符串转化为字节数组
     *
     * @param paramString 要转化的字符串
     * @return 生成的数组
     */
    public static byte[] hexStr2Bytes(String paramString) {
        int i = paramString.length() / 2;

        byte[] arrayOfByte = new byte[i];
        int j = 0;
        while (true) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + paramString.substring(j * 2, k)
                            + paramString.substring(k, l)));
            ++j;
        }
    }
}
