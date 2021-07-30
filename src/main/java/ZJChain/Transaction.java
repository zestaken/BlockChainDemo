package ZJChain;

import utils.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    /**
     * 交易号
     */
    public String transactionId;
    /**
     * 交易序号，用于记录交易数量
     */
    public static int sequence = 0;
    /**
     * 发送方的地址/public key
     */
    public PublicKey sender;
    /**
     * 接收方的地址/public key
     */
    public PublicKey recipient;
    /**
     * 交易额
     */
    public float value;
    /**
     * 发送方的签名
     */
    public byte[] signature;

    /**
     * 本次交易所涉及到的所有交易输入
     */
    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    /**
     * 本次交易所涉及到的所有交易输出（第0位output是发给别人的，第1位output是发给自己的）
     */
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    /**
     * 计算用于标识交易的transactionId
     * @return
     * @throws Exception
     */
    private String calculateHash() throws Exception {
        sequence++;
            return StringUtil.applySha256(
                    StringUtil.getStringFromKey(sender) +
                    StringUtil.getStringFromKey(recipient) +
                    value + sequence);
    }

    /**
     * 根据私钥和其它数据生成数字签名
     * @param privateKey
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    /**
     * 检查发送方数字签名，以验证数据没有损坏或者被修改
     * @return
     */
    public boolean verifySignature() throws Exception {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    /**
     * 实现一次交易
     * @return
     */
    public boolean processTransaction() {

        //验证交易的发送方的数字签名是否有效
        try {
            if(!verifySignature()) {
                System.out.println("交易签名验证失败");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //根据交易输出的id从整个区块链中有效的UTXO集合中获取对应的UTXO
        for(TransactionInput input : inputs) {
            input.UTXO = ZJChain.UTXOs.get(input.transactionOutputId);
        }

        //检测交易输入额是否符合最小标准
        if(getInputsValue() < ZJChain.minimumTransaction) {
            System.out.println("交易输入数额：" + getInputsValue() + " 小于最小交易额");
            return false;
        }

        //计算交易输入还有多少剩余（类似找零）
        float leftover = getInputsValue() - value;
        if(leftover < 0) {
            System.out.println("金额不足，交易终止！");
            return false;
        }
        //计算交易id
        try {
            transactionId = calculateHash();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //建立指向收款方的交易输出
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        //如果需要找零才找零
        if(leftover > 0) {
            //建立指向发送方的交易输出（将交易输出中没有用完的还给自己，实现找零功能）
            outputs.add(new TransactionOutput(this.sender, leftover, transactionId));
        }

        //将本次交易中的所有交易输出添加到整个区块链的UTXO集合中（实现向所有用户通报这笔交易）
        for(TransactionOutput output : outputs) {
            ZJChain.UTXOs.put(output.id, output);
        }
        //移除整个区块链中本次交易中所有交易输入所对应的UTXO（每个UTXO只能用来支付一次）
        for(TransactionInput input : inputs) {
            if(input.UTXO != null) {
                ZJChain.UTXOs.remove(input.UTXO.id);
            }
        }

        return true;

    }

    /**
     * 获取所有交易输入中的总价值（计算拥有的钱的总数）
     * @return
     */
    public float getInputsValue() {
        float sum = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO != null) {
                sum += i.UTXO.value;
            }
        }
        return sum;
    }

    /**
     * 获取所有交易输出中的总价值（要支付的钱的总数）
     * @return
     */
    public float getOutputsValue() {
        float sum = 0;
        for(TransactionOutput output : outputs) {
            sum += output.value;
        }
        return sum;
    }

}
