package ZJChain;

public class TransactionInput {
    /**
     * 这笔交易输入从该ID的交易输出来（类似你曾经收到的某张钞票的编号）
     */
    public String transactionOutputId;
    /**
     * 由交易输入产生了UTXO 未花费交易输出（你要使用的具体钞票）
     */
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

}
