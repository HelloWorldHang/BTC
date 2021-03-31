package com.nwnu.syh.bean;

import com.nwnu.syh.security.CryptoUtil;
import com.nwnu.syh.security.RSACoder;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @description: 钱包地址就是公钥hash后MD5加密
 * @author: 司云航
 * @create: 2020-04-02 16:37
 */
public class Wallet {
    private String publicKey;
    private String privateKey;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Wallet(){

    }

    public Wallet(String publicKey){
        this.publicKey = publicKey;
    }

    public Wallet(String publicKey, String privateKey){
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static Wallet generateWallet(){
        Map<String, Object> initKey;
        try {
            initKey = RSACoder.initKey();
            String publicKey = RSACoder.getPublicKey(initKey);
            String privateKey = RSACoder.getPrivateKey(initKey);
            return new Wallet(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用于钱包对象得到地址
     * @return
     */
    public String getAddress(){
        String publicKeyHash = hashPubKey(publicKey);
        return CryptoUtil.getMD5(publicKeyHash);
    }

    /**
     * 静态方法，供外部调用
     * 根据钱包公钥生成钱包地址
     * @param publicKey
     * @return
     */
    public static String getAddress(String publicKey){
        String publicKeyHash = hashPubKey(publicKey);
        return CryptoUtil.getMD5(publicKeyHash);
    }

    /**
     * 每个对象的
     * 获取钱包公钥hash
     * @return
     */
    public String getHashPubKey(){
        return CryptoUtil.getSHA256(publicKey);
    }

    /**
     * 整个类的
     * 生成钱包公钥hash
     * @param publicKey
     * @return
     */
    public static String hashPubKey(String publicKey){
        return CryptoUtil.getSHA256(publicKey);
    }
}
