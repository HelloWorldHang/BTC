package com.nwnu.syh.controller;

import com.alibaba.fastjson.JSON;
import com.nwnu.syh.bean.Block;
import com.nwnu.syh.bean.Wallet;
import com.nwnu.syh.p2p.Message;
import com.nwnu.syh.p2p.P2PService;
import com.nwnu.syh.service.BlockService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-05-11 18:00
 */
@Controller
public class ChainServlet {
    BlockService blockService = BlockService.getInstance();
    P2PService p2pService =  P2PService.getInstance();

    @RequestMapping(value = "/chain.do", method = RequestMethod.GET)
    @ResponseBody
    public List<Block> getChain(){
        return blockService.getBlockChain();
    }

    @RequestMapping(value = "/mine.do", method = RequestMethod.POST)
    @ResponseBody
    public String mine(@RequestParam String address){
        Wallet wallet = blockService.getMyWalletMap().get(address);
        if (wallet == null){
            return "挖矿钱包不存在";
        }
        Block block = blockService.mine(address);
        if (block == null){
            return "挖矿失败，其他节点已挖出该区块";
        }

        String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_LATEST_BLOCK, JSON.toJSONString(block)));
        // 所有的sockets都加入到p2pService类里面了
        // 所以一次调用即遍历了所有sockets即对所有连接的节点都广播了，不管是客户端还是服务端
        p2pService.broatcast(msg);
        return ("挖矿成功" + JSON.toJSONString(block));
    }

}