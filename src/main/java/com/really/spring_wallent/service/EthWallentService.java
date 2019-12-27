package com.really.spring_wallent.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public interface EthWallentService {

    /**
     * wallentBalance 获取钱包余额
     *
     * @return
     */
    String wallentBalance(String pay_address);


    /**
     * 得到账户地址
     */
    Object GetAccount();


    /**
     * 是否能连接
     */
    boolean CanConnect();


    /**
     * help 命令
     *
     * @return
     */
    boolean help();


    /**
     * 验证地址有效性
     */
    boolean validateAddress(String coinAddress);

    /**
     * 转账
     *
     * @param coinAddress
     * @param amount
     * @return
     */
    Map<String, String> sendToAddress(String fromAddress,
                                      String toAddress,
                                      String coinAddress,
                                      BigDecimal amount);


    /**
     * 转账
     *
     * @param amount
     * @return
     */
    Map<String, String> sendToAddressByNonce(String fromAddress,
                                             String toAddress,
                                             BigDecimal amount,
                                             String pwd,
                                             BigInteger nonce);

    /**
     * 获取交易状态
     *
     * @param rpcTxid
     * @return
     */
    Integer getTransaction(String rpcTxid);
}
