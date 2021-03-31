package com.nwnu.syh.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-04-02 17:01
 */
public class CryptoUtil {
    /**
     * 传入字符串，返回 SHA-256 加密字符串
     *
     * @param strText
     * @return
     */
    public static String getSHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入字符串，返回 SHA-512 加密字符串
     *
     * @param strText
     * @return
     */
    public static String getSHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * 传入字符串，返回 MD5 加密字符串
     *
     * @param strText
     * @return
     */
    public static String getMD5(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * 字符串 SHA 加密
     *
     * @param strText
     * @return
     */
    private static String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象，传入加密类型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 数组
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 数组转换 string 类型
                StringBuffer strHexString = new StringBuffer();
                // 遍历 byte 数组
                for (int i = 0; i < byteBuffer.length; i++) {
                    // 转换成16进制并存储在字符串中
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    public static String UUID(){
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}
