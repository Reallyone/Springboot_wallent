package com.really.spring_wallent.common.EthWeb3J.Connect;

import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;

public class ParityClient {
    private static String ip ="";

    private ParityClient() {
    }

    private static class ClientHolder {
        private static final Parity parity = Parity.build(new HttpService(ip));
    }

    public static final Parity getParity() {
        return ClientHolder.parity;
    }
}
