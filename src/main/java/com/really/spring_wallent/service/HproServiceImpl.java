package com.really.spring_wallent.service;

import com.alibaba.fastjson.JSON;
import com.really.spring_wallent.common.EthWeb3J.operation.EthCoreWeb3jService;
import com.really.spring_wallent.common.EthWeb3J.operation.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Service
@Slf4j
public class HproServiceImpl implements HproService {


    @Autowired
    private EthCoreWeb3jService ethCoreWeb3jService;


    private static String rpcHost;

    @Value("${rpc.host}")
    public void getRpcHost(String host) {
        rpcHost = host;
    }

    private static String rpcPort;

    @Value("${rpc.port}")
    public void getRpcPortt(String port) {
        rpcPort = port;
    }


    private static BigInteger ETHNONCE = BigInteger.valueOf(-1);

    /**
     * 初始化web3j普通api调⽤用
     *
     * @return web3j
     */
    private Web3j initWeb3j() {
        String ethIp = String.format("http://%s:%s", rpcHost, rpcPort);
        System.out.println("eth 的IP 和地址 ================" + ethIp);
        return Web3j.build(new HttpService(ethIp));
    }

    /**
     * 初始化web3j普通api调⽤用
     *
     * @return web3j
     */
    private Parity initParity() {
        String ethIp = String.format("http://%s:%s", rpcHost, rpcPort);
        return Parity.build(new HttpService(ethIp));
    }


    /**
     * 获取钱包余额
     *
     * @return
     */
    @Override
    public String wallentBalance(String pay_address) {
        BigDecimal balance = new BigDecimal(0);

        //以太坊
        Parity parity = initParity();

        balance = ethCoreWeb3jService.getBalanceByAddress(parity, pay_address);
        return JSON.toJSONString(balance);
    }


    /**
     * 获取支付地址
     *
     * @return
     */
    @Override
    public Object GetAccount() {
        //以太坊
        String ethIp = String.format("http://%s:%s", rpcHost, rpcPort);
        return ethCoreWeb3jService.getAccount(ethIp);
    }

    /**
     * 判断是否能力连接
     *
     * @return
     */
    @Override
    public boolean CanConnect() {
        boolean isCan_connnet = false;
        Web3j web3j = initWeb3j();
        //当前区块
        BigInteger nuber = ethCoreWeb3jService.getCurrentBlockNumber(web3j);
        if (nuber.intValue() > 0) {
            isCan_connnet = true;
        }
        return isCan_connnet;
    }


    /**
     * help 命令
     *
     * @return
     */
    @Override
    public boolean help() {
        boolean sendmanyAvailable = false;

        Web3j web3j = initWeb3j();
        List<String> accounts = ethCoreWeb3jService.accounts(web3j);
        if (accounts.size() > 0) {
            sendmanyAvailable = true;
        }
        return sendmanyAvailable;
    }


    public static boolean isETHValidAddress(String input) {
        if (StringUtils.isEmpty(input) || !input.startsWith("0x"))
            return false;
        return isValidAddress(input);
    }

    public static boolean isValidAddress(String input) {
        String cleanInput = Numeric.cleanHexPrefix(input);
        try {
            Numeric.toBigIntNoPrefix(cleanInput);
        } catch (NumberFormatException e) {
            return false;
        }
        return cleanInput.length() == 40;
    }

    /**
     * 验证地址有效性
     * @param coinAddress
     * @return
     */
    @Override
    public boolean validateAddress(String coinAddress) {
        return isETHValidAddress(coinAddress);
    }


    /**
     * 钱包地址
     *
     * @param amount
     * @return
     */
    @Override
    public Map<String, String> sendToAddress(
                                             String fromAddress,
                                             String toAddress,
                                             String pwd,
                                             BigDecimal amount) {
        Map<String, String> resultMap = new HashMap<>();

        String rpcTxid = "";

        //指定钱包支付
        Trade trade = new Trade();

        System.out.println("钱包地址" + toAddress + "需要支付的amount" + amount);

        if (fromAddress != null && !"".equals(fromAddress)) {

            Web3j web3j = initWeb3j();
            //先获取 nonce
            BigInteger nonce = queryEthNonce(web3j, fromAddress);

            String ethIp = String.format("http://%s:%s", rpcHost, rpcPort);

            log.info("支付的钱包地址:{},要支付的地址：{}，金额数：{},ethIP:{},nonce:{}", fromAddress, toAddress, amount, ethIp, nonce);
            if (nonce.intValue() != -1) {
                rpcTxid = trade.sendTransaction(ethIp, pwd, fromAddress, toAddress, amount.toString(), nonce, true);
                resultMap.put("rpcTxid", rpcTxid);
                resultMap.put("nonce", nonce.toString());
            }
        } else {
            log.error("从{} 转出 {}个以太坊到{}失败,没有支付钱包", fromAddress, amount, toAddress);
        }

        if (resultMap.size() == 0) {
            resultMap.put("rpcTxid", rpcTxid);
            resultMap.put("nonce", "-1");
        }

        return resultMap;
    }

    public synchronized BigInteger queryEthNonce(Web3j web3j, String payAddress) {
        if (ETHNONCE.intValue() == -1) {
            ETHNONCE = ethCoreWeb3jService.getNonce(web3j, payAddress);
        } else {
            ETHNONCE = ETHNONCE.add(new BigInteger("1"));
        }
        return ETHNONCE;
    }


    @Override
    public Map<String, String> sendToAddressByNonce(
                                                    String fromAddress,
                                                    String toAddress,
                                                    BigDecimal amount,
                                                    String pwd,
                                                    BigInteger nonce) {
        Map<String, String> resultMap = new HashMap<>();

        String rpcTxid = "";

        //指定钱包支付
        Trade trade = new Trade();

        System.out.println("的钱包地址" + fromAddress + "需要支付的amount:" + amount + "参数传递的nonce:" + nonce);

        if (fromAddress != null && !"".equals(fromAddress)) {

            Web3j web3j = initWeb3j();
            String ethIp = String.format("http://%s:%s", rpcHost, rpcPort);

            log.info("ethIp=============" + ethIp);

            rpcTxid = trade.sendToAddressByNonce(ethIp, pwd, fromAddress, toAddress, amount.toString(), nonce, true);
            log.info(" {} 转出 {}个以太坊到{}获取到的支付ID 为 {}，nonce为{}", fromAddress, amount, toAddress, rpcTxid, nonce);
        } else {
            log.error("从{} 转出 {}个以太坊到{}失败,nonce为{},没有支付钱包", fromAddress, amount, toAddress, nonce);
        }

        resultMap.put("rpcTxid", rpcTxid);
        resultMap.put("nonce", nonce.toString());

        return resultMap;
    }


    @Override
    public Integer getTransaction(String rpcTxid) {
        String ethIp = String.format("http://%s:%s", rpcHost, rpcPort);
        return ethCoreWeb3jService.getStatus(ethIp, rpcTxid);
    }

}
