package com.nwnu.syh.bean;

import java.util.Objects;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-04-02 16:37
 */
public class TransactionOutput {
    // 交易金额
    private int value;
    // 交易接受方的钱包公钥的hash
    private String publicKeyHash;

    public TransactionOutput(){

    }

    public TransactionOutput(int value, String publicKeyHash) {
        this.value = value;
        this.publicKeyHash = publicKeyHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        TransactionOutput that = (TransactionOutput) o;
        return value == that.value &&
                Objects.equals(publicKeyHash, that.publicKeyHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, publicKeyHash);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }
}
