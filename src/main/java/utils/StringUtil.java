package utils;

import ZJChain.Transaction;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {

    /**
     * 应用SHA256算法接收输入字符串计算并返回哈希字符串
     * @param input
     * @return
     * @throws Exception
     */
    public static String applySha256(String input) throws Exception {
            //返回实现指定摘要算法的 MessageDigest 对象。此处是SHA-256算法
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); //getInstance有异常
            //根据输入的bytes数组完成哈希计算。
            byte[] hash = digest.digest(input.getBytes("UTF-8"));//getBytes有异常
            StringBuffer hexString = new StringBuffer();
            for(int i = 0; i < hash.length; i++) {
                //将生成的哈希字节数组每一字节（8bit）转换16进制数字符串
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) {
                    //当生成的16进制数只有一位时，在末尾添0，丢弃生成的16进制数（因为8位应是两位的16进制数，除非前面全为0）
                    hexString.append("0");
                }
                //将每一个字节的转换结果连接
                hexString.append(hex);
            }
            return hexString.toString();
    }

    /**
     * 根据秘钥获得字符串
     * @param key
     * @return
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 根据ECDSA算法，由privatekey生成数字签名（字节数组）
     * @param privateKey
     * @param data
     * @return
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String data) {
        Signature dsa;
        //提前声明变量，避免最后不能返回有效值
        byte[] output = new byte[0];
        try{
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = data.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * 由publickey验证数字签名是否正确
     * @param publicKey
     * @param data
     * @param signature
     * @return
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) throws Exception {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
    }

    /**
     * 根据交易生成merkleRoot标志区块
     * @param transactions
     * @return
     */
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }

        ArrayList<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<>();
            for(int i = 1; i < previousTreeLayer.size(); i++) {
                try {
                    treeLayer.add(StringUtil.applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count = treeLayer.size();
                previousTreeLayer = treeLayer;
            }
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";

        return merkleRoot;
    }
}
