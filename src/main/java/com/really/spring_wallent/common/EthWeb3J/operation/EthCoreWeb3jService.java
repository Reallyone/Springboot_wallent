package com.really.spring_wallent.common.EthWeb3J.operation;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.really.spring_wallent.utils.HttpURLConnUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.parity.Parity;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 账户相关操作
 */
@Service
@Slf4j
public class EthCoreWeb3jService {

    /**
     * 得到所有账户
     *
     * @return
     */
    public List<String> getAccountList(Parity parity) {
        try {
            return parity.personalListAccounts().send().getAccountIds();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //得到当前块高度
    public BigInteger getCurrentBlockNumber(Web3j web3j) {
        Request<?, EthBlockNumber> request = web3j.ethBlockNumber();
        try {
            return request.send().getBlockNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigInteger.valueOf(0L);
    }


    /**
     * 得到账户余额
     *
     * @return
     */
    public BigDecimal getBalanceByAddress(Web3j web3j, String payAddress) {
        web3j.ethBlockNumber();
        AtomicReference<BigDecimal> resultBalance = new AtomicReference<>();
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(payAddress, DefaultBlockParameterName.LATEST).send();
            if (ethGetBalance != null) {
                resultBalance.set(Convert.fromWei(new BigDecimal(ethGetBalance.getBalance()), Convert.Unit.ETHER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultBalance.get();
    }

    //全部用户
    public List<String> accounts(Web3j web3j) {
        List<String> ids = new ArrayList<>();
        try {
            Request<?, EthAccounts> ethAccountsRequest = web3j.ethAccounts();
            ids = ethAccountsRequest.send().getAccounts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }


    /**
     * eth 获取难度
     */
    public BigDecimal getdifficulty(Parity parity) {
        BigDecimal difficult = new BigDecimal("0");
        try {
            //当前区块
            BigInteger nuber = getCurrentBlockNumber(parity);
            //当前难度
            Request<?, EthBlock> ethBlockRequest = parity.ethGetBlockByNumber(DefaultBlockParameter.valueOf(nuber), true);
            EthBlock.Block ethBlock = ethBlockRequest.send().getBlock();
            difficult = new BigDecimal(ethBlock.getDifficulty());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return difficult;
    }

    /**
     * 交易状态
     * <p>
     * 0: 等待  1： 成功  2 失败
     */
    public int getStatus(String url, String txhash) {
        int statusInte = 0;

        List<String> tsHashStr = new ArrayList<>();
        tsHashStr.add(txhash);
        //初始化url
        HttpURLConnUtils.url = url;
        JSONObject jsonObject = HttpURLConnUtils.doPost(tsHashStr, "eth_getTransactionReceipt");
        log.info("交易ID == {} --返回的交易信息{}====", txhash, jsonObject);

        //判断blockHash 是否大于0
        if (jsonObject != null) {
            //错误情况
            JSONObject error = jsonObject.getJSONObject("error");
            if (error != null) {
                //直接返回失败
                log.info("交易Id {} 获取交易状态错 {}", txhash, error.getString("message"));
                statusInte = 2;
            } else {
                JSONObject result = jsonObject.getJSONObject("result");
                if (result != null) {
                    /**还未打包
                     * "result":{
                     * "logsBloom":"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000","transactionIndex":"0x0","transactionHash":"0xb449070e8c2fc36d221e06bd1150c68459b97984a054b65ac801c2ebb919594e","gasUsed":"0x5208",
                     * "root":"0xfbc4e4fb7663f1b9f3ba036aa2a825ab8cb6a06d75174b89a2f432bb1b95c12a","cumulativeGasUsed":"0x5208","logs":[]},
                     * "id":0,"jsonrpc":"2.0"
                     */
                    String blockHash = result.getString("blockHash");
                    if (blockHash == null || "null".equals(blockHash) || "".equals(blockHash)) {
                        statusInte = 0;
                    } else {
                        String status = result.getString("status");
                        if (status == null || "null".equals(status) || "".equals(status)) {
                            statusInte = 0;
                        } else {
                            if (status.equals("0x1")) {
                                statusInte = 1;
                            } else {
                                statusInte = 2;
                            }
                        }
                    }
                } else {
                    log.warn("交易Id --{}返回的返回的result{},交易id={}", txhash, result, 0);
                    statusInte = 0;
                }
            }
        }
        return statusInte;
    }


    /**
     * 获取blockhash
     * 区块确认数=当前区块高度-交易被打包时的区块高度
     *
     * @param txHash
     * @return
     */
    public static String getBlockHash(Web3j web3j, String txHash) {
        try {
            EthTransaction transaction = web3j.ethGetTransactionByHash(txHash).sendAsync().get();
            Transaction result = transaction.getResult();
            String blockHash = result.getBlockHash();
            boolean isSuccess = Numeric.toBigInt(blockHash).compareTo(BigInteger.valueOf(0)) != 0;
            if (isSuccess) {
                blockHash = getTransactionReceipt(web3j, txHash);
            }
            return blockHash;
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static String getTransactionReceipt(Web3j web3j, String transactionHash) {
        try {
            EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
            TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();
            String blockHash = receipt.getBlockHash();
            return blockHash;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * 获取none
     *
     * @param addr
     * @return
     */
    public static BigInteger getNonce(Web3j web3j, String addr) {
        EthGetTransactionCount getNonce = null;
        try {
            getNonce = web3j.ethGetTransactionCount(addr, DefaultBlockParameterName.PENDING).send();
            if (getNonce == null) {
                return BigInteger.valueOf(-1);
            }
            return getNonce.getTransactionCount();
        } catch (IOException e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }
    }


    /**
     * 获取钱包地址
     *
     * @return
     */
    public Object getAccount(String url) {
        //初始化url
        HttpURLConnUtils.url = url;
        List<String> tsHashStr = new ArrayList<>();
        JSONObject jsonObject = HttpURLConnUtils.doPost(tsHashStr, "eth_accounts");
        if (jsonObject != null) {
            JSONArray blockHash = jsonObject.getJSONArray("result");
            return blockHash;
        }
        return null;
    }

    public Object getblockByNumber(String url, String blockNumber) {
        //初始化url
        HttpURLConnUtils.url = url;
        List<Object> tsHashStr = new ArrayList<>();
        tsHashStr.add("0x" + Integer.toHexString(Integer.valueOf(blockNumber)));
        tsHashStr.add(true);
        JSONObject jsonObject = HttpURLConnUtils.doPost(tsHashStr, "eth_getBlockByNumber");
        if (jsonObject != null) {
            JSONObject block = jsonObject.getJSONObject("result");
            return block;
        }
        return null;
    }

    public Integer getEth_blockNumber(String url) {
        //初始化url
        HttpURLConnUtils.url = url;
        List<Object> tsHashStr = new ArrayList<>();
        JSONObject jsonObject = HttpURLConnUtils.doPost(tsHashStr, "eth_blockNumber");
        Integer dec_num = 0;
        if (jsonObject != null) {
            String block = jsonObject.getString("result");
            //截取"0x"
            String[] splitstr = block.split("0x");
            String number = splitstr[1];
            //16进制转10进制数
            dec_num = Integer.parseInt(number, 16);
        }
        return dec_num;
    }

    public Integer GetPeerCount(String url) {
        //初始化url
        HttpURLConnUtils.url = url;
        //转换数据
        ArrayList<Map<String, String>> sendMap = new ArrayList<>();
        JSONObject resultJson = HttpURLConnUtils.doPost(sendMap, "net_peerCount");
        Integer dec_num = 0;
        if (resultJson != null) {
            //支付失败
            JSONObject err = resultJson.getJSONObject("error");
            if (err == null) {
                String result = resultJson.getString("result");
                //截取"0x"
                String[] splitstr = result.split("0x");
                //16进制转10进制数
                dec_num = Integer.parseInt(splitstr[1], 16);
            }
        }
        return dec_num;
    }

}
