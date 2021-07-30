package ZJChain;

import com.alibaba.fastjson.JSON;
import sun.util.resources.cldr.zh.CalendarData_zh_Hans_SG;

import java.util.ArrayList;
import java.util.HashMap;

public class ZJChain {

    //blockChain为静态属性，所有对象都是在对同一个blockchain修改
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    public static int difficulty = 5;
    /**
     * 用于记录所有有效的UTXO，键是String类型的TransactionOutputId
     */
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    /**
     * 每次交易的最小交易额
     */
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    /**
     * 初始交易（创建区块链时初始化第一笔交易）
     */
    public static Transaction genesisTransaction;

    /**
     * 检查区块链的有效性
     * @return
     */
    public boolean isChainValid() {
        Block curBlock;
        Block prevBlock;
        //用于检验挖矿难度是否达标的字符串
        String target = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));


        //遍历blockchain，从1开始，保证prevblock的有效性
        for(int i = 1; i < blockChain.size(); i++) {
            curBlock = blockChain.get(i);
            prevBlock = blockChain.get(i - 1);


            try {
                //检查hash值计算有效性
                if(!curBlock.hash.equals(curBlock.calculateHash())) {
                    System.out.println("block的hash值计算错误");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //检查hash值前后对应关系正确性
            if(!prevBlock.hash.equals(curBlock.prevHash)) {
                System.out.println("当前block与前面block的hash值不对应");
                return false;
            }

            if(!curBlock.hash.substring(0, difficulty).equals(target)) {
                //如果不满足难度标准，也无效
                System.out.println("当前块未满足挖矿难度标准！");
                return false;
            }

            TransactionOutput tempOutput;
            for(int t = 0; i < curBlock.transactions.size(); t++) {
                Transaction currentTransaction = curBlock.transactions.get(t);

                //检查交易的签名
                try {
                    if(!currentTransaction.verifySignature()) {
                        System.out.println("第" + t + "个交易的签名无效！");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //检查交易的交易输出额和交易输入额是否相等
                if(!(currentTransaction.getInputsValue() == currentTransaction.getOutputsValue())) {
                    System.out.println("第" + t + "个交易的交易输出与交易输入额不相等！");
                    return false;
                }

                //检查交易输入是否正确（交易输入要么来源于初始交易（和矿工），要么来源于其它交易输出）
                for(TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("第" + t + "个交易的交易输入不存在！");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("第" + t + "个交易的交易输入的值无效！");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                //将交易输出加入临时UTXOs
                for(TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if(currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    System.out.println("第" + t + "个交易的交易输出目的方错误！");
                    return false;
                }

                if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    System.out.println("第" + t + "个交易的找零的交易输出没有发给发送者！");
                    return false;
                }
            }
        }
         System.out.println("区块链有效！");
         return true;
    }

    /**
     * 向区块链中添加块
     * @param newBlock
     */
    public void addBlock(Block newBlock) {
        //先完成挖矿工作才能加入区块链中
        newBlock.mineBlock(difficulty);
        blockChain.add(newBlock);
    }

    /**
     * 将blockChain转换为json字符串本地存储
     * @return
     */
    public String toJson() {
        String blockChainString = JSON.toJSONString(blockChain);
        return blockChainString;
    }

    /**
     * 设置挖矿难度
     * @param difficulty
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

}
