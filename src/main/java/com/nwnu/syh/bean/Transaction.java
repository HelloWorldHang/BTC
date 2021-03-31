package com.nwnu.syh.bean;

import com.alibaba.fastjson.JSON;
import com.nwnu.syh.security.CryptoUtil;
import com.nwnu.syh.security.RSACoder;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-04-02 16:29
 */
public class Transaction {

    private String id;

    private TransactionInput txIn;

    private TransactionOutput txOut;

    public Transaction(){

    }

    public Transaction(String id, TransactionInput txIn, TransactionOutput txOut) {
        this.id = id;
        this.txIn = txIn;
        this.txOut = txOut;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransactionInput getTxIn() {
        return txIn;
    }

    public void setTxIn(TransactionInput txIn) {
        this.txIn = txIn;
    }

    public TransactionOutput getTxOut() {
        return txOut;
    }

    public void setTxOut(TransactionOutput txOut) {
        this.txOut = txOut;
    }

    /**
     *
     * @param privateKey
     * @param prevTx
     */
    public void sign(String privateKey, Transaction prevTx){
        if (coinbaseTx()){ // 系统交易免签
            return;
        }
        if (!prevTx.getId().equals(txIn.getTxId())){
            System.out.println("交易签名失败");
        }
        Transaction txClone = cloneTx();
        // 加入发送者公钥hash
        txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyHash());
        String sign = "";
        try {
            sign = RSACoder.sign(txClone.hash().getBytes(), privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        txIn.setSignature(sign);
    }

    /**
     *验证签名，只解决了数据传输可能被篡改的问题，并没有解决恶意引用其他人UTXO的问题
     * 再验证一下txOut的公钥hash是否等于发送方的公钥hash
     * @param prevTx
     * @return
     */
    public boolean verify(Transaction prevTx){
        if (coinbaseTx()){
            return true;
        }
        if (!prevTx.getId().equals(txIn.getTxId())){
            System.out.println("验证交易签名失败：当前交易输入引用的前一笔交易与传入的前一笔交易不匹配");
        }
        Transaction cloneTx = cloneTx();
        // 加入发送者公钥hash
        cloneTx.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyHash());
        boolean result = false;
        try {
            // 发送者的公钥验证签名
            result = RSACoder.verify(cloneTx.hash().getBytes(), txIn.getPublicKey(), txIn.getSignature());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 验证是否是系统奖励的交易
     * @return
     */
    public boolean coinbaseTx(){
        return txIn.getTxId().equals("0") && txIn.getValue() == -1;
    }

    /**
     * 生成用于交易签名的交易副本
     * @return
     */
    public Transaction cloneTx(){
        TransactionInput transactionInput = new TransactionInput(txIn.getTxId(), txIn.getValue(), null, null);
        TransactionOutput transactionOutput = new TransactionOutput(txOut.getValue(), txOut.getPublicKeyHash());
        return new Transaction(id, transactionInput, transactionOutput);
    }

    /**
     * 生成交易的hash
     * @return
     */
    public String hash(){
        return CryptoUtil.getSHA256((JSON.toJSONString(this)));
    }
}
