package com.nwnu.syh.service;

import com.alibaba.fastjson.JSON;
import com.nwnu.syh.bean.*;
import com.nwnu.syh.p2p.Message;
import com.nwnu.syh.p2p.P2PService;
import com.nwnu.syh.security.CryptoUtil;

import java.util.*;

/**
 * @description: 使用单例模式创建
 * @author: 司云航
 * @create: 2020-04-02 17:36
 */
public class BlockService {
    private static BlockService blockService = new BlockService();
    /**
     * 区块链存储结构
     */
    private List<Block> blockChain = new ArrayList<>();
    /**
     * 当前节点钱包集合
     */
    private Map<String, Wallet> myWalletMap = new HashMap<>();
    /**
     * 其他节点钱包集合，钱包只包含公钥
     */
    private Map<String, Wallet> otherWalletMap = new HashMap<>();
    /**
     * 转账交易集合
     */
    private List<Transaction> allTransactions = new ArrayList<>();
    /**
     * 已打包转账交易
     */
    private List<Transaction> packedTransactions = new ArrayList<>();

    private BlockService(){
        int nonce = (int)Math.random()*1000+1;
        String firstHash = calculateHash("Chancellor on brink of second bailout for banks"+System.currentTimeMillis(),null,nonce);
        // 新建创始区块
        Block genesisBlock = new Block(1, System.currentTimeMillis(), firstHash, "Chancellor on brink of second bailout for banks", new ArrayList<Transaction>(), nonce);
        blockChain.add(genesisBlock);
        System.out.println("生成创始区块:" + JSON.toJSONString(genesisBlock));
    }

    public static BlockService getInstance(){
        return blockService;
    }

    public List<Block> getBlockChain() {
        return blockChain;
    }

    public void setBlockChain(List<Block> blockChain) {
        this.blockChain = blockChain;
    }

    public Map<String, Wallet> getMyWalletMap() {
        return myWalletMap;
    }

    public void setMyWalletMap(Map<String, Wallet> myWalletMap) {
        this.myWalletMap = myWalletMap;
    }

    public Map<String, Wallet> getOtherWalletMap() {
        return otherWalletMap;
    }

    public void setOtherWalletMap(Map<String, Wallet> otherWalletMap) {
        this.otherWalletMap = otherWalletMap;
    }

    public List<Transaction> getAllTransactions() {
        return allTransactions;
    }

    public void setAllTransactions(List<Transaction> allTransactions) {
        this.allTransactions = allTransactions;
    }

    public List<Transaction> getPackedTransactions() {
        return packedTransactions;
    }

    public void setPackedTransactions(List<Transaction> packedTransactions) {
        this.packedTransactions = packedTransactions;
    }

    /**
     * 获取最后一个区块
     * @return
     */
    public Block getLastBlock(){
        return blockChain.size() > 0 ? blockChain.get(blockChain.size()-1) : null;
    }

    public boolean addBlock(Block newBlock){
        if (isValidNewBlock(newBlock, getLastBlock())){
            blockChain.add(newBlock);
            // 新区块的交易要加入已打包交易中
            packedTransactions.addAll(newBlock.getTransactions());
            /*// 所有交易中加入系统交易
            newBlock.getTransactions().forEach(tx -> {
                if (tx.coinbaseTx()){
                    Transaction sysTx = tx;
                    allTransactions.add(sysTx);
                }
            });*/
            return true;
        }
        return false;
    }

    /**
     * 验证新区块是否有效
     * @param newBlock
     * @param previousBlock
     * @return
     */
    public boolean isValidNewBlock(Block newBlock, Block previousBlock){
        if (!previousBlock.getHash().equals(newBlock.getPreviousHash())){
            System.out.println("新区块的前一个区块hash验证不通过");
        }else{
            // 验证新区块hash值得正确性
            String hash = calculateHash(newBlock.getPreviousHash(), newBlock.getTransactions(), newBlock.getNonce());
            if (!hash.equals(newBlock.getHash())){
                System.out.println("新区块的hash值无效：" + hash + " " + newBlock.getHash());
                return false;
            }
            if (!isValidHash(newBlock.getHash())){
                return false;
            }
        }
        return true;
    }

    /**
     * 找零时应该再改动一下
     * 查找未花费交易
     * @param address
     * @return
     */
    public List<Transaction> findUnspentTransactions(String address){
        List<Transaction> unspentTxs = new ArrayList<>();
        // 只添加交易id
        Set<String> spentTxs = new HashSet<>();
        // 查找已花费的UTXO,使用allTransactions考虑未打包交易
        for (Transaction tx : allTransactions){
            if (tx.coinbaseTx()){
                continue;
            }
            // 自己的公钥和发送者的公钥做对比，相等则说明该笔交易属于自己花费的
            if (address.equals(Wallet.getAddress(tx.getTxIn().getPublicKey()))){
                spentTxs.add(tx.getTxIn().getTxId());
            }
        }
        // 查找未花费的UTXO
        for (Block block : blockChain){
            List<Transaction> transactions = block.getTransactions();
            for (Transaction tx: transactions
                 ) {
                // 如果是别人发送给他的交易
                if (address.equals(CryptoUtil.getMD5(tx.getTxOut().getPublicKeyHash()))){
                    // 如果该交易花费了则不添加
                    if (!spentTxs.contains(tx.getId())){
                        unspentTxs.add(tx);
                    }
                }
            }
        }
        return unspentTxs;
    }

    /**
     * 创建交易
     * @param senderWallet
     * @param recipientWallet
     * @param amount
     * @return
     */
    public Transaction createTransaction(Wallet senderWallet, Wallet recipientWallet, int amount){
        List<Transaction> unspentTxs = findUnspentTransactions(senderWallet.getAddress());
        // 引用的UTXO
        Transaction prevTx = null;
        for (Transaction transaction : unspentTxs){
            // TODO 找零
            if (transaction.getTxOut().getValue() == amount){
                prevTx = transaction;
                break;
            }
        }
        if (prevTx == null){
            return null;
        }
        TransactionInput txIn = new TransactionInput(prevTx.getId(), amount, null, senderWallet.getPublicKey());
        TransactionOutput txOut = new TransactionOutput(amount, recipientWallet.getHashPubKey());
        Transaction transaction = new Transaction(CryptoUtil.UUID(), txIn, txOut);
        transaction.sign(senderWallet.getPrivateKey(), prevTx);
        allTransactions.add(transaction);
        return transaction;
    }

    public Block mine(String toAddress){
        // 创建系统奖励的交易
        Transaction sysTx = newCoinbaseTx(toAddress);
        allTransactions.add(sysTx);
        // 对所有交易进行一个复制
        List<Transaction> blockTxs = new ArrayList<>(allTransactions);
        // 去除已打包进区块的交易
        blockTxs.removeAll(packedTransactions);
        // 验证所有未打包交易
        verifyAllTransaction(blockTxs);
        String newBlockHash = "";
        int nonce = 0;
        long start = System.currentTimeMillis();
        System.out.println("开始挖矿");
        while (true){
            // 计算区块hash值
            newBlockHash = calculateHash(getLastBlock().getHash(), blockTxs, nonce);
            // 检验hash
            if (isValidHash(newBlockHash)){
                System.out.println("挖矿成功，正确的hash值：" + newBlockHash);
                System.out.println("挖矿耗费时间：" + (System.currentTimeMillis() - start) + "ms");
                break;
            }
            nonce++;
        }
        // 创建新的区块，新区块验证不通过时返回null
        Block block = createNewBlock(nonce, getLastBlock().getHash(), newBlockHash, blockTxs);
        return block;
    }

    public Transaction newCoinbaseTx(String toAddress){

        Wallet recipientWallet = null;
        for (Map.Entry<String, Wallet> map:
             myWalletMap.entrySet()) {
            if (map.getKey().equals(toAddress)){
                recipientWallet = map.getValue();
            }
        }
        if (recipientWallet == null){
            System.out.println("创建系统奖励交易失败，没有找到该钱包地址");
            return null;
        }
        TransactionInput txIn = new TransactionInput("0", -1, null, null);
        TransactionOutput txOut = new TransactionOutput(10, recipientWallet.getHashPubKey());
        Transaction sysTx = new Transaction(CryptoUtil.UUID(), txIn, txOut);
        return sysTx;
    }

    /**
     * 根据交易id遍历所有交易找到交易
     * @param id
     * @return
     */
    public Transaction findTransaction(String id){
        for (Transaction tx: allTransactions
             ) {
            if (id.equals(tx.getId())){
                return tx;
            }
        }
        return null;
    }

    /**
     * 验证交易，调用交易的验证签名方法
     * @param tx
     * @return
     */
    public boolean verifyTransaction(Transaction tx){
        if (tx.coinbaseTx()){
            return true;
        }
        Transaction prevTx = findTransaction(tx.getTxIn().getTxId());
        return tx.verify(prevTx);
    }

    /**
     * 逐个验证交易
     * @param blockTxs
     */
    public void verifyAllTransaction(List<Transaction> blockTxs){
        List<Transaction> invalidTxs = new ArrayList<>();
        for (Transaction tx : blockTxs){
            if (!verifyTransaction(tx)){
                invalidTxs.add(tx);
            }
        }
        // 在未打包交易中去除无效交易
        blockTxs.removeAll(invalidTxs);
        // 在所有交易中去除无效的交易
        allTransactions.removeAll(invalidTxs);
    }

    /**
     * 创建钱包
     * @return
     */
    public Wallet createWallet(){
        Wallet wallet = Wallet.generateWallet();
        String address = wallet.getAddress();
        myWalletMap.put(address, wallet);
        return wallet;
    }

    /**
     * 获取钱包余额
     * @param address
     * @return
     */
    public int getWalletBalance(String address){
        List<Transaction> unspentTxs = findUnspentTransactions(address);
        int balance = 0;
        for (Transaction transaction : unspentTxs){
            balance += transaction.getTxOut().getValue();
        }
        return balance;
    }

    /**
     * 创建新区块并验证，验证不通过返回null
     * @param nonce
     * @param previousHash
     * @param hash
     * @param blockTxs
     * @return
     */
    private Block createNewBlock(int nonce, String previousHash, String hash, List<Transaction> blockTxs){
        Block block = new Block(blockChain.size() + 1, System.currentTimeMillis(), hash, previousHash, blockTxs, nonce);
        if (addBlock(block)){
            return block;
        }
        return null;
    }

    private boolean isValidHash(String hash){
        return hash.startsWith("0000");
    }

    /**
     * 计算区块hash
     * @param previousHash
     * @param currentTransactions
     * @param nonce
     * @return
     */
    private String calculateHash(String previousHash, List<Transaction> currentTransactions, int nonce){
        return CryptoUtil.getSHA256(previousHash + JSON.toJSONString(currentTransactions) + nonce);
    }

    public void replaceChain(List<Block> newBlocks) {
        if (isValidChain(newBlocks) && newBlocks.size() > blockChain.size()){
            blockChain = newBlocks;
            packedTransactions.clear();
            blockChain.forEach(block -> {
                packedTransactions.addAll(block.getTransactions());
            });
            // 再做一个请求，请求所有交易
            allTransactions.clear();
            allTransactions.addAll(packedTransactions);
        }else {
            System.out.println("接受的区块链无效");
        }
    }

    private boolean isValidChain(List<Block> chain) {
        Block block = null;
        Block lastBlock = chain.get(0);
        int currentIndex = 1;
        while (currentIndex < chain.size()){
            block = chain.get(currentIndex);
            if (!isValidNewBlock(block, lastBlock)){
                return false;
            }
            lastBlock = block;
            currentIndex++;
        }
        return true;
    }

}
