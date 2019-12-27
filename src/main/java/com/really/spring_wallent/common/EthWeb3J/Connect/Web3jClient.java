package com.really.spring_wallent.common.EthWeb3J.Connect;


import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * 连接以太坊客户端
 */

public class Web3jClient {
    public static String ip = "" ;

    private Web3jClient(){};

    private volatile static Web3j web3j;

    public static Web3j getClient(){
        if (web3j == null){
            synchronized (Web3jClient.class){
                if (web3j == null){
                    web3j = Web3j.build(new HttpService(ip));
                }
            }
        }
        return  web3j;
    }








}
