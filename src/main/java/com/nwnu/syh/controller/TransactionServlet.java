package com.nwnu.syh.controller;

import com.alibaba.fastjson.JSON;
import com.nwnu.syh.bean.Transaction;
import com.nwnu.syh.bean.Wallet;
import com.nwnu.syh.p2p.Message;
import com.nwnu.syh.p2p.P2PService;
import com.nwnu.syh.service.BlockService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-05-14 08:37
 */
@Controller
@RequestMapping(value = "/transaction")
@ResponseBody
public class TransactionServlet {
    BlockService blockService = BlockService.getInstance();
    P2PService p2pService = P2PService.getInstance();

    @RequestMapping(value = "/create.do", method = RequestMethod.POST)
    public String createTransaction(@RequestParam String senderAddress, @RequestParam String recipientAddress, @RequestParam int amount){
        Wallet senderWallet = blockService.getMyWalletMap().get(senderAddress);
        Wallet recipientWallet = blockService.getMyWalletMap().get(recipientAddress);
        if (recipientWallet == null){
            recipientWallet = blockService.getOtherWalletMap().get(recipientAddress);
        }
        if (senderWallet == null){
            return "发送方钱包不存在";
        }
        if (recipientWallet == null){
            return "接受方钱包不存在";
        }
        Transaction transaction = blockService.createTransaction(senderWallet, recipientWallet, amount);
        if (transaction == null){
            return "钱包余额不足";
        }else{
            Transaction[] txs = {transaction};
            String msg = JSON.toJSONString(new Message(P2PService.RESPONSE_TRANSACTION, JSON.toJSONString(txs)));
            p2pService.broatcast(msg);
            return JSON.toJSONString(transaction);
        }
    }

    @RequestMapping(value = "/unpacked.do")
    public String getUnpackedTransaction(){
        List<Transaction> allTransactions = new ArrayList<>(blockService.getAllTransactions());
        allTransactions.removeAll(blockService.getPackedTransactions());
        return JSON.toJSONString(allTransactions);
    }

    @RequestMapping(value = "/all.do")
    public String getAllTransaction(){
        List<Transaction> allTransactions = new ArrayList<>(blockService.getAllTransactions());
        return JSON.toJSONString(allTransactions);
    }
}
