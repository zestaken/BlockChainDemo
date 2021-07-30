package ZJChain;

import org.junit.jupiter.api.Test;

import java.security.Security;

public class ZJChainTest {
    @Test
    public void test1() {
        //初始化区块链
        ZJChain zjChain = new ZJChain();
        //向区块链中添加10个块
        for(int i = 0; i < 10; i++) {
            //创建新块
            Block block;
            if(zjChain.blockChain.size() == 0) {
                block = new Block("0");
                block.mineBlock(5);
            } else {
                block = new Block(zjChain.blockChain.get(
                        zjChain.blockChain.size() - 1).hash);
                block.mineBlock(5);
            }
            zjChain.addBlock(block);
        }

        for(Block block : zjChain.blockChain) {
            System.out.println("hash: " + block.hash + " prevHash: " + block.prevHash);
        }
        System.out.println("isvalid：" + zjChain.isChainValid());
    }

    @Test
    public void test2() {
        //用于验证签名的部分
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

        //初始化区块链
        ZJChain zjChain = new ZJChain();

        //创建钱包
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        //初始交易的钱包
        Wallet coinBase = new Wallet();

        //创建初始交易
        //最初的交易的value从coinbase凭空出现
        System.out.println("第一次交易：coinbase向walletA转账100");
        ZJChain.genesisTransaction = new Transaction(coinBase.publicKey, walletA.publicKey, 100f, null);
        //生成coinbase对此次交易的签名
        ZJChain.genesisTransaction.generateSignature(coinBase.privateKey);
        //初始交易id设为0
        ZJChain.genesisTransaction.transactionId = "0";
        //因为初始交易是凭空生成，所以很多参数需要手动设置
        ZJChain.genesisTransaction.outputs.add(new TransactionOutput(
                ZJChain.genesisTransaction.recipient,
                ZJChain.genesisTransaction.value,
                ZJChain.genesisTransaction.transactionId));
        //将本次交易输出添加到UTXOs
        ZJChain.UTXOs.put(ZJChain.genesisTransaction.outputs.get(0).id,
                ZJChain.genesisTransaction.outputs.get(0));

        System.out.println("挖矿生成第一个区块。。。");
        //前面的哈希值为手动设为0
        Block genesis = new Block("0");
        //添加交易
        genesis.addTransaction(ZJChain.genesisTransaction);
        //将该块加入区块链中
        zjChain.addBlock(genesis);

        System.out.println("第二笔交易： walletA向walletB转账20");
        System.out.println("WalletA的余额：" + walletA.getBalance());
        System.out.println("WalletB的余额：" + walletB.getBalance());
        //新生成一个区块用于记账
        System.out.println("挖矿生成第二个区块。。。");
        Block block1 = new Block(genesis.hash);
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
        zjChain.addBlock(block1);
        System.out.println("第二笔交易结束");
        System.out.println("WalletA的余额：" + walletA.getBalance());
        System.out.println("WalletB的余额：" + walletB.getBalance());

        zjChain.isChainValid();
    }
}
