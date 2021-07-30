package ZJChain;

import utils.StringUtil;

import java.security.PublicKey;

public class TransactionOutput {
    /**
     * 交易输出编号id
     */
    public String id;
    /**
     * 这笔交易输出的接收方公钥（类似收款方银行账号）
     */
    public PublicKey recipient;
    /**
     * 交易输出额
     */
    public float value;
    /**
     * 创建这个交易输出的交易id
     */
    public String parentTransactionId;


    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        //前面属性均赋值后再计算id
        try {
            this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + this.value + this.parentTransactionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //检查UTXO（未消费的交易输出）是否是指定publickey的拥有者的
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}
