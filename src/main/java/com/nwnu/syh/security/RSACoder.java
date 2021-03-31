package com.nwnu.syh.security;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;


public class RSACoder {
    //非对称密钥算法
    public static final String KEY_ALGORITHM = "RSA";

    //公钥
    private static final String PUBLIC_KEY = "RSAPublicKey";

    //私钥
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    // 数字签名
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 初始化密钥对
     * @return 密钥对
     * @throws NoSuchAlgorithmException
     */
    public static Map<String, Object> initKey() throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 公钥
        PublicKey publicKey = keyPair.getPublic();
        // 私钥
        PrivateKey privateKey = keyPair.getPrivate();

        HashMap<String, Object> keyMap = new HashMap<>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 取得公钥字符串
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return getKeyString(key);
    }

    /**
     * 取得私钥字符串
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return getKeyString(key);
    }

    /**
     * 公钥加密
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
        // 将公钥字符串转化为公钥对象
        PublicKey publicKey = getPublicKey(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 私钥解密
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] dencryptByPrivateKey(byte[] data, String key) throws Exception {
        // 将公钥字符串转化为公钥对象
        PrivateKey privateKey = getPrivateKey(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 生成数字签名
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        // 获取私钥对象
        PrivateKey priKey = getPrivateKey(privateKey);
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);
        byte[] sign = signature.sign();
        return ecryptBASE64(sign);
    }

    /**
     * 验证数字签名
     * @param data
     * @param publicKey
     * @param sign
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        // 得到公钥对象
        PublicKey pubKey = getPublicKey(publicKey);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);
        // 验证签名
        return signature.verify(decryptBASE64(sign));
    }

    /**
     * 得到公钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }
    /**
     * 得到私钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * 将二进制数组转化为String
     * @param data
     * @return
     */
    public static String ecryptBASE64(byte[] data){
        return (new BASE64Encoder()).encode(data);
    }

    /**
     * 将String转化为二进制流
     * @param str
     * @return
     * @throws IOException
     */
    public static byte[] decryptBASE64(String str) throws IOException {
        return (new BASE64Decoder()).decodeBuffer(str);
    }

    /**
     * 得到密钥字符串（经过base64编码）
     * @return
     */
    public static String getKeyString(Key key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        String s = (new BASE64Encoder()).encode(keyBytes);
        return s;
    }
}