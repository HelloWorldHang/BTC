package com.nwnu.syh.p2p;


import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-05-09 18:57
 */
public class P2PClient {
    private static P2PService p2pService = P2PService.getInstance();
    private static Logger log = Logger.getLogger(P2PClient.class);

    //private static P2PClient p2pClient = new P2PClient();
    /*private P2PClient(){

    }
    public static P2PClient getInstance(){
        return p2pClient;
    }*/

    public void connectPeer(String peer){
        try{
            final  WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("客户端请求" + this.getRemoteSocketAddress() + "建立连接--------------");
                    // 请求最后一个区块，若最后一个区块index大于本地最后一个区块index则请求整条区块链进行同步
                    p2pService.write(this, p2pService.queryLatestBlockMsg());

                    p2pService.write(this, p2pService.queryWalletMsg());
//                     System.out.println("client-----" + p2pService.queryWalletMsg());
                    p2pService.getSockets().add(this);
                    log.info("客户端请求时间：" + System.currentTimeMillis());
                    p2pService.write(this, p2pService.querySockets());

                    log.info("客户端共连接了几个sockets-----------------");
                    for (WebSocket socket : p2pService.getSockets()) {
                        log.info(socket.getRemoteSocketAddress());
                    }
                    log.info("sockets----------------------");
                    log.info("客户端open方法结束-------------------------");
                }

                @Override
                public void onMessage(String msg) {
                    p2pService.handleMessage(this, msg,  p2pService.getSockets());
                }

                @Override
                public void onClose(int i, String msg, boolean b) {
                    System.out.println("onClose----------connection failed");
                    p2pService.getSockets().remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("onError-----------connection failed");
                }
            };
            socketClient.setConnectionLostTimeout(1000*600);
            socketClient.connect();
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public static void connectOtherPeer(String peer){
        try{
            final  WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("connectOtherPeer----客户端请求" + this.getRemoteSocketAddress() + "建立连接--------------");
                    p2pService.getSockets().add(this);
                    log.info("connectOtherPeer----客户端open方法结束-------------------------");
                }

                @Override
                public void onMessage(String msg) {
                    log.info("connectOtherPeer-----onMessage方法----------  ");
                    p2pService.handleMessage(this, msg,  p2pService.getSockets());
                    log.info("connectOtherPeer-----onMessage方法结束--------");
                }

                @Override
                public void onClose(int i, String msg, boolean b) {
                    System.out.println("onClose----------connection failed");
                    p2pService.getSockets().remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("onError-----------connection failed");
                }
            };
            socketClient.setConnectionLostTimeout(1000*600);
            socketClient.connect();
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public void write(WebSocket ws, String message){
        System.out.println("发送给：" + ws.getRemoteSocketAddress().getPort() + "的p2p消息: " + message);
        ws.send(message);
    }
}
