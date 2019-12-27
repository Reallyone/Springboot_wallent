package com.really.spring_wallent.common.EthWeb3J.operation;

import com.alibaba.fastjson.JSONObject;
import com.example.xnhproseservice.utils.HttpURLConnUtils;
import com.example.xnhproseservice.utils.SHA256Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 交易操作
 */
@Service
@Slf4j
public class Trade {

    @Autowired
    private EthCoreWeb3jService web3jService;


    public Boolean Sign(String from, String s) {
        //加密
        String hash = SHA256Util.getSHA256StrJava("0x0");
        List<String> putList = new ArrayList<>();
        putList.add(from);
        putList.add("0x" + hash);
        JSONObject resultValue = HttpURLConnUtils.doPost(putList, "eth_sign");
        if (resultValue != null) {
            String result = resultValue.getString("result").substring(2);
            try {
                Integer bi = Integer.parseInt(result, 16);
                return false;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }


    /**
     * rpc 转账
     *
     * @param fromAddr
     * @param toAddress
     * @param value
     * @param autoGas
     * @return
     */
    public String sendTransaction(String url,
                                  String pwd,
                                  String fromAddr,
                                  String toAddress,
                                  String value,
                                  BigInteger nonce,
                                  boolean autoGas) {
        //初始化url
        HttpURLConnUtils.url = url;

        if (!Sign(fromAddr, pwd)) {
            log.error(fromAddr + "账户解锁失败:" + pwd);
            return "";
        } else {
            Map<String, String> map = sendTransactionRpc(fromAddr, toAddress, value,
                    "0x" + Integer.toHexString(100),
                    "0x" + Integer.toHexString(100),
                    "0x" + nonce.toString(16),
                    autoGas);

            //转换数据
            ArrayList<Map<String, String>> sendMap = new ArrayList<>();

            sendMap.add(map);

            JSONObject resultJson = HttpURLConnUtils.doPost(sendMap, "eth_sendTransaction");

            log.info("eth_sendTransaction 发送rpc 数据 请求 获取的数据:" + resultJson);

            if (resultJson != null) {
                //支付失败
                JSONObject err = resultJson.getJSONObject("error");
                if (err != null) {
                    log.error("从{} 转出 {}个以太坊到{}失败,nonce为{}，error:{}", fromAddr, value, toAddress, nonce, err.getString("message"));
                    log.info("从{} 转出 {}个以太坊到{}失败,nonce为{}，error:{}", fromAddr, value, toAddress, nonce, err.getString("message"));
                    return "";
                } else {
                    //支付成功
                    String rpcTx = resultJson.getString("result");
                    log.info("从{} 转出 {}个以太坊到{}成功 txid:{},nonce:{}", fromAddr, value, toAddress, rpcTx, nonce);
                    return rpcTx;
                }
            } else {
                log.error("从{}--eth_sendTransaction 发送rpc 数据 请求 获取的数据为空,nonce为{}", fromAddr, nonce);
                return "";
            }
        }
    }


    public String sendToAddressByNonce(String url,
                                       String pwd,
                                       String fromAddr,
                                       String toAddress,
                                       String value,
                                       BigInteger nonce,
                                       boolean autoGas) {
        //初始化url
        HttpURLConnUtils.url = url;

        if (Sign(fromAddr, pwd)) {
            Map<String, String> map = sendTransactionRpc(fromAddr, toAddress, value,
                    "0x" + Integer.toHexString(100),
                    "0x" + Integer.toHexString(100),
                    "0x" + nonce.toString(16),
                    autoGas);
            //转换数据
            ArrayList<Map<String, String>> sendMap = new ArrayList<>();

            sendMap.add(map);

            JSONObject resultJson = HttpURLConnUtils.doPost(sendMap, "eth_sendTransaction");

            log.info("eth_sendTransaction 发送rpc 数据 请求 获取的数据:" + resultJson);

            if (resultJson != null) {
                //支付失败
                JSONObject err = resultJson.getJSONObject("error");
                if (err != null) {
                    log.error("从{} 转出 {}个以太坊到{}失败{}", fromAddr, value, toAddress, err.getString("message"));
                    log.info("从{} 转出 {}个以太坊到{}失败{}", fromAddr, value, toAddress, err.getString("message"));
                    return "";
                } else {
                    //支付成功
                    String rpcTx = resultJson.getString("result");
                    log.info("从{} 转出 {}个以太坊到{} txid:{}", fromAddr, value, toAddress, rpcTx);
                    return rpcTx;
                }
            } else {
                log.error("从{}--eth_sendTransaction 发送rpc 数据 请求 获取的数据为空", fromAddr);
                log.info("从{}--eth_sendTransaction 发送rpc 数据 请求 获取的数据为空", fromAddr);
                return "";
            }
        } else {
            log.info(fromAddr + "账户解锁失败");
            log.error(fromAddr + "账户解锁失败");
            return "";
        }

    }

    /**
     * rpc 数据格式转换
     *
     * @param from
     * @param to
     * @param value
     * @param gas
     * @param gasPrice
     * @param autoGas
     * @return
     */
    public static Map<String, String> sendTransactionRpc(String from,
                                                         String to, String value, String gas,
                                                         String gasPrice,
                                                         String nonce,
                                                         boolean autoGas) {
        BigInteger val = Convert.toWei(value, Convert.Unit.ETHER).toBigInteger();
        Map<String, String> map = new HashMap<String, String>();
        map.put("from", from);
        map.put("to", to);
        map.put("value", "0x" + val.toString(16));
        map.put("nonce", nonce);
        if (!autoGas) {
            map.put("gas", gas);
            map.put("gasPrice", gasPrice);
        }

        return map;
    }


    /**
     * 获取当前以太坊网络中最近一笔交易的gasPrice
     */
    public static BigInteger requestCurrentGasPrice(Web3j web3j) {
        EthGasPrice ethGasPrice = null;
        try {
            ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return ethGasPrice.getGasPrice();
    }


}
