package com.nwnu.syh.bean;

import java.util.Objects;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-04-02 16:36
 */
public class TransactionInput {
    // 所引用的交易的id
    private String txId;
    // 交易金额
    private int value;
    // 交易签名
    private String signature;
    // 交易发送方的钱包公钥，验证交易签名
    private String publicKey;

    public TransactionInput() {
    }

    public TransactionInput(String txId, int value, String signature, String publicKey) {
        this.txId = txId;
        this.value = value;
        this.signature = signature;
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        TransactionInput that = (TransactionInput) o;
        return value == that.value &&
                Objects.equals(txId, that.txId) &&
                Objects.equals(signature, that.signature) &&
                Objects.equals(publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txId, value, signature, publicKey);
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
