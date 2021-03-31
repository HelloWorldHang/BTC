package com.nwnu.syh;

import com.nwnu.syh.p2p.P2PClient;
import com.nwnu.syh.p2p.P2PServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SyhApplication {

    public static void main(String[] args) {
        P2PServer server = new P2PServer();
        P2PClient client = new P2PClient();
        Integer port = Integer.valueOf(args[1]);
        // 启动服务端
        server.initP2PServer(port);
        if (args.length == 3 && args[2] != null){
            // 客户端连接服务端
            client.connectPeer(args[2]);
        }
        SpringApplication.run(SyhApplication.class, args);
    }


}
