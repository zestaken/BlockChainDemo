package ZJChain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    //公私钥
    public PublicKey publicKey;
    public PrivateKey privateKey;

    /**
     * 钱包存储属于自己的UTXO（未消费交易输出）
     */
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet() {
        generateKeyPair();
    }

    /**
     * 生成公私钥
     */
    public void generateKeyPair() {
        try {
            //指定算法ECDSA生成密钥对
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            //初始化并生成密钥对
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            //获取公私钥
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算钱包的总余额
     * @return
     */
    public float getBalance() {
        float sum = 0;
        //遍历Map集合获取键值对对象
        for(Map.Entry<String, TransactionOutput> item : ZJChain.UTXOs.entrySet()) {
            TransactionOutput UTXO =  item.getValue();
            //检查该UTXO是否属于该钱包
            if(UTXO.isMine(publicKey)) {
                //添加到钱包的UTXOs集合中
                UTXOs.put(UTXO.id, UTXO);
                sum += UTXO.value;
            }
        }
        return sum;
    }

    /**
     * 创建交易，支出
     * @param _recipient
     * @param value
     * @return
     */
    public Transaction sendFunds(PublicKey _recipient, float value) {
        //检查余额是否足够
        if(getBalance() < value) {
            System.out.println("余额不足，交易终止！");
            return null;
        }
        //建立动态数组用来记录作为交易输入使用的UTXO
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        //查找钱包的UTXO，直到总金额达到要支付的金额
        float total = 0;
        for(Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total >= value) {
                break;
            }
        }

        //创建交易
        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        //将已经使用的UTXO从钱包中移除
        for(TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }

        return  newTransaction;
    }

}
