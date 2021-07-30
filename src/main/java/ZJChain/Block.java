package ZJChain;

import utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String prevHash;
    public long timestamp;
    public int nonce; //用于挖矿的变量
    public ArrayList<Transaction> transactions = new ArrayList<>();
    //merkleRoot充当data的作用（因为区块block本质就是个账本，用交易来充当数据最合理）
    public String merkleRoot;

    public Block(String prevHash) {
        this.prevHash = prevHash;
        this.timestamp = new Date().getTime();
        //初始化哈希值必须在其它属性都已初始化之后
        try {
            this.hash = calculateHash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算当前块的哈希值
     * @return
     * @throws Exception
     */
    public String calculateHash() throws Exception {
        //取消使用data生成hash而使用merkleRoot
        String calculatedHash = StringUtil.applySha256(prevHash+merkleRoot+timestamp+nonce);
        return calculatedHash;
    }

    /**
     * 挖矿计算
     * @param difficulty
     */
    public void mineBlock(int difficulty) {
        //挖矿前计算merkleRoot值
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        //生成目标字符串：此处是包含指定数量（difficulty）个连续的0的字符串
        String target = new String(new char[difficulty]).replace('\0', '0');
        //检查当前块的hash值中从0到difficulty部分是否与target字符串相同，如果不相同，则修改nonce，重新计算hash
        while(!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            try {
                hash = calculateHash();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("nonce：" + nonce);
    }

    /**
     * 在将交易添加到块时执行交易
     * @param transaction
     * @return
     */
    public boolean addTransaction(Transaction transaction) {
        //验证交易的有效性
        if(transaction == null) return false;
        if(!prevHash.equals("0")) {
            if(!transaction.processTransaction()) {
                System.out.println("交易处理失败！");
                return false;
            }
        }
        //将交易添加到
        transactions.add(transaction);
        System.out.println("交易成功添加到Block中！");
        return true;
    }
}
