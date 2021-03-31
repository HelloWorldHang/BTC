package com.nwnu.syh.bean;

import java.util.List;
/**
 * @description: *
 * @author: 司云航
 * @create: 2020-04-02 16:05
 */
public class Block {
    private int index;
    private long time;
    private String hash;
    private String previousHash;
    private List<Transaction> transactions;
    private int nonce;

    public Block(int index, long time, String hash, String previousHash, List<Transaction> transactions, int nonce){
        this.index = index;
        this.time = time;
        this.hash = hash;
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.nonce = nonce;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
}
