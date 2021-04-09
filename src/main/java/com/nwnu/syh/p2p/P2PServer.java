package com.nwnu.syh.p2p;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-05-09 18:56
 */
public class P2PServer {
    private P2PService p2pService = P2PService.getInstance();
    Logger log = Logger.getLogger(P2PServer.class);
    /*private static P2PServer p2pServer = new P2PServer();
    private P2PServer(){

    }
    public static P2PServer getInstance(){
        return p2pServer;
    }*/

    public void initP2PServer(int port){
        // 可修改new InetSocketAddress(port)，加入ip地址
        final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                log.info("服务端收到"+webSocket.getRemoteSocketAddress() + "的连接请求-------");
                p2pService.getSockets().add(webSocket);
                log.info("服务端时间：" + System.currentTimeMillis());
                log.info("服务端共连接了几个socket-----------------");
                for (WebSocket socket : p2pService.getSockets()) {
                    log.info(socket.getRemoteSocketAddress());
                }
                log.info("sockets-------------------");
                log.info("服务端open函数结束------------------------");
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                System.out.println("onClose---------connection failed to peer:" + webSocket.getRemoteSocketAddress());
                p2pService.getSockets().remove(webSocket);
            }

            @Override
            public void onMessage(WebSocket webSocket, String msg) {
                p2pService.handleMessage(webSocket, msg, p2pService.getSockets());
            }

            @Override
            public void onError(WebSocket webSocket, Exception e) {
                System.out.println("onError------------connection failed to peer:" + webSocket.getRemoteSocketAddress());
                p2pService.getSockets().remove(webSocket);
            }

            @Override
            public void onStart() {

            }
        };
        socketServer.setConnectionLostTimeout(1000*600);
        socketServer.start();
    }

}
