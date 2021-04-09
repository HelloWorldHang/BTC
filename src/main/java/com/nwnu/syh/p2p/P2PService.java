package com.nwnu.syh.p2p;

import com.alibaba.fastjson.JSON;
import com.nwnu.syh.bean.Block;
import com.nwnu.syh.bean.Transaction;
import com.nwnu.syh.bean.Wallet;
import com.nwnu.syh.service.BlockService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.java_websocket.AbstractWebSocket;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @description: 使用单例模式，server和client同用该类，sockets是server和client的sockets集合
 * @author: 司云航
 * @create: 2020-05-09 18:56
 */
public class P2PService {
    private static P2PService p2pService = new P2PService();
    // 初始化为空
    private static List<WebSocket> sockets = new ArrayList<>();
    private static BlockService blockService = BlockService.getInstance();
    Logger log = Logger.getLogger(P2PClient.class);
    private P2PService(){

    }

    public static P2PService getInstance(){
        return p2pService;
    }
    // 查询最新区块
    public final static int QUERY_LATEST_BLOCK = 0;
    // 查询整个区块链
    public final static int QUERY_BLOCKCHAIN = 1;
    // 查询交易集合
    public final static int QUERY_TRANSACTION = 2;
    // 查询已打包交易
    public final static int QUERY_PACKED_TRANSACTION = 3;
    // 查询钱包集合
    public final static int QUERY_WALLET = 4;
    // 返回区块链
    public final static int RESPONSE_BLOCKCHAIN = 5;
    // 返回交易集合
    public final static int RESPONSE_TRANSACTION = 6;
    // 返回已打包交易集合
    public final static int RESPONSE_PACKED_TRANSACTION = 7;
    // 返回钱包集合
    public final static int RESPONSE_WALLET = 8;
    // 返回最后一个块
    public final static int RESPONSE_LATEST_BLOCK = 9;
    // 请求所连接服务端的所有socket连接
    public final static int QUERY_SOCKETS = 10;
    // 返回本节点的所有socket连接
    public final static int RESPONSE_SOCKETS = 11;

    public List<WebSocket> getSockets() {
        return sockets;
    }

    public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets){
        //System.out.println(msg);

        Message message = JSON.parseObject(msg, Message.class);
        //System.out.println(message);
        System.out.println("接受到" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息：" + JSON.toJSONString(message));
        switch (message.getType()){
            case QUERY_LATEST_BLOCK:
                write(webSocket, responseLatestBlockMsg());
                break;
            case QUERY_BLOCKCHAIN:
                write(webSocket, responseBlockChainMsg());
                break;
            case QUERY_TRANSACTION:
                write(webSocket, responseTransactions());
                break;
            case QUERY_PACKED_TRANSACTION:
                write(webSocket, responsePackedTransaction());
                break;
            case QUERY_WALLET:
                write(webSocket, responseWallets());
                break;
            case QUERY_SOCKETS:
                write(webSocket, responseSockets(webSocket));
                break;
            case RESPONSE_SOCKETS:
                handleSocketsResponse(message.getData());
                break;
            case RESPONSE_BLOCKCHAIN:
                handleBlockChainResponse(message.getData(), sockets);
                break;
            case RESPONSE_TRANSACTION:
                handleTransactionResponse(message.getData());
                break;
            case RESPONSE_PACKED_TRANSACTION:
                handlePackedTransactionResponse(message.getData());
                break;
            case RESPONSE_WALLET: // 接收方处理发送方响应过来的钱包
                handleWalletResponse(message.getData());
                break;
            case RESPONSE_LATEST_BLOCK:
                handleLatestBlockResponse(message.getData(), sockets);
                break;
            default:
                System.out.println("default");
                break;
        }
    }

    public void handlePackedTransactionResponse(String msg) {
        List<Transaction> unpacked = JSON.parseArray(msg, Transaction.class);
        blockService.getPackedTransactions().addAll(unpacked);
    }

    /**
     *
     * 向所有交易中增加新的交易
     * @param message
     */
    public void handleTransactionResponse(String message) {
        List<Transaction> txs = JSON.parseArray(message, Transaction.class);
        blockService.getAllTransactions().addAll(txs);
    }

    public synchronized void handleBlockChainResponse(String message, List<WebSocket> sockets){
        List<Block> receiveBlockchain = JSON.parseArray(message, Block.class);
        Collections.sort(receiveBlockchain, new Comparator<Block>() {
            @Override
            public int compare(Block block1, Block block2) {
                return block1.getIndex() - block2.getIndex();
            }
        });
        // 重点
        Block latestBlockReceived = receiveBlockchain.get(receiveBlockchain.size() - 1);
        Block lastBlock = blockService.getLastBlock();
        if (latestBlockReceived.getIndex() > lastBlock.getIndex()){
            if (lastBlock.getHash().equals((latestBlockReceived.getPreviousHash()))){
                System.out.println("将接收到的区块加入到本地的区块");
                if (blockService.addBlock(latestBlockReceived)){
                    // 向连接自己的节点广播
//                    broatcast(responseLatestBlockMsg());
                }
            }else{
                // 用长链替换本地的短链
                blockService.replaceChain(receiveBlockchain);
            }
        }else{
            System.out.println("接收到的区块链不比本地区块链长，不处理");
        }
    }

    public synchronized void handleLatestBlockResponse(String message, List<WebSocket> sockets){
        Block latestBlock = JSON.parseObject(message, Block.class);

        // 重点
        Block lastBlock = blockService.getLastBlock();
        if (latestBlock.getIndex() > lastBlock.getIndex()){
            if (lastBlock.getHash().equals((latestBlock.getPreviousHash()))){
                System.out.println("将接收到的最后一个区块加入到本地的区块链");
                if (blockService.addBlock(latestBlock)){
                    // 向连接自己的节点广播
//                    broatcast(responseLatestBlockMsg());
                }
            }else{// 进行整条链的同步，选择最长的链同步
                System.out.println("查询所有通讯节点上的区块链，并选择最长的同步");
                broatcast(queryBlockChainMsg());
            }
        }else{
            System.out.println("接收到的区块链不比本地区块链长，不处理");
        }
    }

    /**
     * 接收方处理响应数据
     * @param message
     */
    public void handleWalletResponse(String message){
        List<Wallet> wallets = JSON.parseArray(message, Wallet.class);
        wallets.forEach(wallet -> {
            blockService.getOtherWalletMap().put(wallet.getAddress(), wallet);
        });
    }

    public void handleSocketsResponse(String msg){
        log.info("接收到的信息：" + msg);
        List<InetSocketAddress> listAddr = JSON.parseArray(msg, InetSocketAddress.class);
        if (CollectionUtils.isNotEmpty(listAddr)){
            for (InetSocketAddress addr : listAddr) {
                P2PClient.connectOtherPeer("ws://" + addr.getHostName() + ":" + addr.getPort());
            }
        }

        log.info("接收到服务端的所有addr---------------" + listAddr);
        for (InetSocketAddress addr : listAddr) {
            log.info(addr.getAddress() + "-----------" + addr.getPort());
        }
        log.info("接收到服务端的所有addr---------------");
    }

    public String queryLatestBlockMsg() {
        return JSON.toJSONString(new Message(QUERY_LATEST_BLOCK));
    }

    public String queryTransactionMsg() {
        return JSON.toJSONString(new Message(QUERY_TRANSACTION));
    }

    public String queryPackedTransactionMsg() {
        return JSON.toJSONString(new Message(QUERY_PACKED_TRANSACTION));
    }

    private String queryBlockChainMsg() {
        return JSON.toJSONString(new Message(QUERY_BLOCKCHAIN));
    }

    public String queryWalletMsg(){
        return JSON.toJSONString(new Message(QUERY_WALLET));
    }

    public String querySockets(){
        return JSON.toJSONString(new Message(QUERY_SOCKETS));
    }

    public String responseSockets(WebSocket webSocket){
        List<InetSocketAddress> list = new ArrayList<>();
        // TODO
        // 打印一下相应信息
        log.info("服务端响应:" + p2pService.getSockets());
        log.info("请求来自：" + webSocket.getRemoteSocketAddress().getPort());
        for (WebSocket socket : p2pService.getSockets()) {
            if (socket.getRemoteSocketAddress().getPort() != webSocket.getRemoteSocketAddress().getPort()){
                log.info(socket.getRemoteSocketAddress());
                InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                list.add(remoteSocketAddress);
            }
        }
        return JSON.toJSONString(new Message(RESPONSE_SOCKETS, JSON.toJSONString(list)));
    }

    public String responsePackedTransaction() {
        return JSON.toJSONString(new Message(RESPONSE_PACKED_TRANSACTION, JSON.toJSONString(blockService.getPackedTransactions())));
    }

    public String responseTransactions() {

        return JSON.toJSONString(new Message(RESPONSE_TRANSACTION, JSON.toJSONString(blockService.getAllTransactions())));
    }

    /**
     * 携带的信息为本地的整个区块链
     * @return
     */
    public String responseBlockChainMsg() {
        return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockChain())));
    }

    /**
     * 携带的信息为最后一个区块
     * @return
     */
    public String responseLatestBlockMsg() {
        Block block =blockService.getLastBlock();
        return JSON.toJSONString(new Message(RESPONSE_LATEST_BLOCK, JSON.toJSONString(block)));
    }

    /**
     * 节点加入时的钱包同步（发送给请求者）
     * @return
     */
    public String responseWallets() {
        List<Wallet> wallets = new ArrayList<>();
        // lambda表达式（key，value）
        blockService.getMyWalletMap().forEach((address, wallet) -> {
            // 只取自己钱包的公钥
            wallets.add(new Wallet(wallet.getPublicKey()));
        });
        // 别人钱包中存的只有公钥
        blockService.getOtherWalletMap().forEach((address, wallet) -> {
            wallets.add(wallet);
        });

        return JSON.toJSONString(new Message(RESPONSE_WALLET, JSON.toJSONString(wallets)));
    }

    public void write(WebSocket ws, String message){
        System.out.println("发送给：" + ws.getRemoteSocketAddress().getPort() + "的p2p消息: " + message);
        ws.send(message);
    }

    public void broatcast(String message){
        if (sockets.size() == 0){
            return;
        }
        System.out.println("=======广播消息开始");
        for (WebSocket socket : sockets){
            this.write(socket, message);
        }
        System.out.println("========广播消息结束");
    }
}
