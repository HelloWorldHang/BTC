package com.nwnu.syh.controller;

import com.alibaba.fastjson.JSON;
import com.nwnu.syh.bean.Wallet;
import com.nwnu.syh.p2p.Message;
import com.nwnu.syh.p2p.P2PService;
import com.nwnu.syh.service.BlockService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-05-13 11:33
 */
@Controller
@RequestMapping(value = "/wallet")
@ResponseBody
public class WalletServlet {
    BlockService blockService = BlockService.getInstance();
    P2PService p2pService = P2PService.getInstance();

    @RequestMapping(value = "/create.do", method = RequestMethod.GET)
    public String createWallet(){
        Wallet wallet = blockService.createWallet();
        Wallet[] wallets = {new Wallet(wallet.getPublicKey())};
        String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_WALLET, JSON.toJSONString(wallets)));
        p2pService.broatcast(msg);
        return JSON.toJSONString(wallet);
    }

    @RequestMapping(value = "/get.do", method = RequestMethod.GET)
    public String getWallets(){
        Map<String, Wallet> myWalletMap = blockService.getMyWalletMap();
        return JSON.toJSONString(myWalletMap.values());
    }

    @RequestMapping(value = "/balance.do", method = RequestMethod.POST)
    public String getWalletBalance(@RequestParam String address){
        int walletBalance = blockService.getWalletBalance(address);
        return String.valueOf(walletBalance);
    }

}
