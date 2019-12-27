package com.really.spring_wallent.utils;

import lombok.Data;

import java.math.BigInteger;

@Data
public class EthNonceObtain {

    public static BigInteger ethNonce = new BigInteger("-1");

    public static synchronized BigInteger GetEthNonce() {
        ethNonce = ethNonce.add(new BigInteger("1"));
        return ethNonce;
    }


    private static final EthNonceObtain INSTANCE = new EthNonceObtain();

    private EthNonceObtain() {
    }

    public static final EthNonceObtain getInstance() {
        return EthNonceObtain.INSTANCE;
    }

    public BigInteger ethNonceTest = new BigInteger("-1");


    public void addEthNonceTest(BigInteger data) {
        /*synchronized(this) {
            ethNonceTest = data;
        }*/
        ethNonceTest = data;
    }

    public BigInteger getEthNonceTest() {
        /*synchronized (this) {
            return ethNonceTest;
        }*/
        return ethNonceTest;
    }

    public BigInteger addEthNonceTest() {
        /*synchronized (this) {
            ethNonceTest = ethNonceTest.add(new BigInteger("1"));
            return ethNonceTest;
        }*/
        ethNonceTest = ethNonceTest.add(new BigInteger("1"));
        return ethNonceTest;
    }

}
