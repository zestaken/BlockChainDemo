# 区块链概念

* 区块链（BlockChain）起源于比特币，2008年11月1日，一位自称中本聪(Satoshi Nakamoto)的人发表了《比特币:一种点对点的电子现金系统》一文，阐述了基于P2P网络技术、加密技术、时间戳技术、区块链技术等的电子现金系统的构架理念，这标志着比特币的诞生。
* 在比特币形成过程中，区块（ZJChain.Block）是一个一个的存储单元，记录了一定时间内各个区块节点全部的交流信息。各个区块之间通过随机散列(也称哈希算法)实现链接，后一个区块包含前一个区块的哈希值，随着信息交流的扩大，一个区块与一个区块相继接续，形成的结果就叫区块链（BlockChain）。
* 看着这些概念头脑中也难以形成一个具体的印象，不如实现一个区块链的demo来看一看。
* [参考教程](https://medium.com/programmers-blockchain/create-simple-blockchain-java-tutorial-from-scratch-6eeed3cb03fa)
* [代码地址](https://github.com/zestaken/BlockChainDemo)

# 1. 实现Block结构

* 区块链（BlockChain）顾名思义，是将一个个区块（ZJChain.Block）链接起来形成。所以我们实现区块链的第一步是实现Block结构。
* 区块链的链不是传统的通过指针等技术实现，而是通过哈希值来链接。所以一个Block中需要包含自身的哈希值，前一个Block的哈希值，还有自身的数据。而当前块的哈希值是通过前一个块的哈希值、当前块的创建的时间以及当前块的数据三者根据加密算法计算得出的。所以Block中还要包含时间戳变量表示块创建时间。
* Block类实现如下：
```java
public class ZJChain.Block {

    public String hash;
    public String prevHash;
    private String data;
    private long timestamp;

    public ZJChain.Block(String data, String prevHash) {
        this.data = data;
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
        String calculatedHash = StringUtil.applySha256(prevHash+data+timestamp);
        return calculatedHash;
    }
}
```
* 应用SHA256算法来计算哈希值：
```java
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
```
  * SHA256:
    * SHA256的中文全称叫做“安全哈希算法”。所谓的“哈希”是Hash的音译，而Hash就是进行Hash函数的意思。通常来说，Hash函数的运算有一个共同特点。就是不论原始数据有多少位，只要通过Hash运算后，得到结果的长度都是固定的。
    * Hash函数的类型有很多种，包括SHA224、SHA256、SHA384、SHA512、SHA512/224、SHA512/256等。但是比特币仅选用了SHA256。这个256代表的意思是，数据经过函数运算后得到的结果必须是一个256位的2进制数字。
    * 每次Hash计算后得到的结果有三个要求：第一、输入Hash函数之前的数据和通过Hash函数处理过后得到的编号必须一一对应。第二、每一个编号的长度都是固定的。第三、我们无法通过编号倒推出数据的内容。
  * 因为哈希值的涉及到前一个块的哈希值，时间，数据等，所以哈希链表是无法在中间插入修改的。

# 2. 实现区块链（BlockChain）结构

* 前面构造来区块（ZJChain.Block），现在把他们连接起来存储就形成了区块链。我们采用ArrayList结构来组织这些Block。
```java
public class ZJChain.ZJChain {

    //blockChain为静态属性，所有对象都是在对同一个blockchain修改
    public static ArrayList<Block> blockChain = new ArrayList<Block>();

    /**
     * 检查区块链的有效性
     * @return
     */
    public boolean isChainValid() {
        Block curBlock;
        Block prevBlock;

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
        }
        return true;
    }

    /**
     * 向区块链中添加块
     * @param block
     */
    public void addBlock(Block block) {
        blockChain.add(block);
    }

    /**
     * 将blockChain转换为json字符串本地存储
     * @return
     */
    public String toJson() {
        String blockChainString = JSON.toJSONString(blockChain);
        return blockChainString;
    }
}
```
  * 因为ArrayList是内存中的数据结构，需要长期保存的话需要转换为JSON字符串写入文件中保存,通过alibaba的fastjson包实现。 
* 构建区块链的测试：
```java
   @Test
    public void test1() {
        //初始化区块链
        ZJChain.ZJChain zjChain = new ZJChain.ZJChain();
        //向区块链中添加10个块
        for(int i = 0; i < 10; i++) {
            //创建新块
            ZJChain.Block block;
            if(zjChain.blockChain.size() == 0) {
                block = new ZJChain.Block("ZJChain.Block: " + i, "0");
            } else {
                block = new ZJChain.Block("ZJChain.Block: " + i, zjChain.blockChain.get(
                        zjChain.blockChain.size() - 1).hash);
            }
            zjChain.addBlock(block);
        }

        for(ZJChain.Block block : zjChain.blockChain) {
            System.out.println("hash: " + block.hash + " prevHash: " + block.prevHash);
        }
    }
```

# 3. 准备挖矿！！！

* 提起比特币，区块链，便离不开挖矿这个话题。那什么是挖矿？比特币挖矿就是找到一个随机数（Nonce）参与哈希运算Hash（ZJChain.Block Header），使得最后得到的哈希值符合难度要求（在很多种组合中试出满足要求的组合, 有一点运气成分），用公式表示就是Hash（ZJChain.Block Header）<= target。具体的说就是使生成的哈希值的开头至少有指定数目个0。实现如下：
```java
    public void mineBlock(int difficulty) {
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
```
* 简单来看挖矿难度的高低就是生成区块头的哈希值有多少0。difficulty每增加1，运算量都是呈几何增加，十分恐怖。
* 当难度为4:![SAWTcE](https://gitee.com/zhangjie0524/picgo/raw/master/uPic/SAWTcE.png)
* 当难度为5:![lVcfrW](https://gitee.com/zhangjie0524/picgo/raw/master/uPic/lVcfrW.png)
* 当难度为6:![4IZwd2](https://gitee.com/zhangjie0524/picgo/raw/master/uPic/4IZwd2.png)（好家伙，直接跑了8分多钟。。。）
* 对比可以看出，难度增加1，运算量（nonce可以表示运算的次数）直接增加一个量级，怪不得作为工作量衡量的标准（proof-of-work)。
* 这里还可以看出，挖矿之所以很耗算力，并不是因为这个哈希计算本身有多么复杂，而是它是需要重复这个哈希计算很多次（我这随便提一点难度，都到千万级了。。。）直到满足要求。这也是为什么GPU挖矿效率比CPU高的原因：其实不是GPU运算速度比CPU快，而是GPU运算的数据是单一的，是经过CPU运算往后分离出来的单一数据。CPU运算的所需求的东西许多，而且不是单一的某种数据。CPU可以运行更复杂的指令。如果是做一个简单的数学计算，一个最大16核的CPU最多只能同时跑16个线程，而一个普通的GPU就可以同时跑3000多个线程，所以做简单数学，GPU就比CPU要快几个数量级，而生成区块要做的哈希计算偏偏就是一个很简单的数学题。
* 另外，在检查区块链的有效性(isChainValid)的时候，还需要增加检查hash值是否满足难度要求这一点：
```java
    if(!curBlock.hash.substring(0, difficulty).equals(target)) {
        //如果不满足难度标准，也无效
        System.out.println("当前块未满足难度标准！");
        return false;
    }
```

# 4. 创建钱包

* 比特币是一种点对点的电子现金系统，没有实物形态，可以存储在比特币钱包里。日常生活中，钱包是用来放钱的，但比特币钱包里却没有比特币，而只是确立比特币所有权的工具：比特币被记录在比特币网络的区块链（前面实现的BlockChain）中，比特币的所有权是通过数字密钥、比特币地址和数字签名（接下来要实现的）来确立的。
* 数字密钥并不存储在网络中，而是由用户生成并存储在一个文件或简单的数据库中，称为钱包。比特币钱包里存储着你的比特币信息，包括比特币地址（类似于你的银行卡账号）和数字秘钥。
* 数字秘钥是用公钥加密创建一个密钥对，用于控制比特币的获取。密钥对包括一个私钥，和由其衍生出的唯一的公钥。公钥用于接收比特币，而私钥用于生成比特币支付时的交易签名（类似于你银行卡的密码）。支付比特币时，比特币的当前所有者需要在交易中提交其公钥和签名（每次交易的签名都不同，但均从同一个私钥生成）。比特币网络中的所有人都可以通过所提交的公钥和签名进行验证，并确认该交易是否有效，即确认支付者在该时刻对所交易的比特币拥有所有权。比特币私钥就用来保护你的钱包，如果私钥丢失，你将永远失去这笔比特币。
* Wallet实现：
```java
public class Wallet {
    public PublicKey publicKey;
    public PrivateKey privateKey;

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
}
```
* ECDSA:椭圆曲线数字签名算法（Elliptic Curve Digital Signature Algorithm，缩写ECDSA）是一种被广泛应用于数字签名的加密算法。

# 5. 实现交易（Transaction)

* 既然是一种货币，那么最重要的功能就是用来交易，作为最早出现的加密货币，比特币采用了 UTXO 模型作为其底层存储的数据结构，其全称为 Unspent Transaction output，也就是未被使用的交易输出。
*  UTXO 模型的加密货币中，某一个账户中的余额并不是由一个数字表示的，而是由当前区块链网络中所有跟当前账户有关的 UTXO 组成的。每一个UTXO就跟现实世界中的一张纸钞类似，一个UTXO只能用一次，如果数额超出则发给自己新的UTXO（自己给自己找零）。
*  比特币实质上没有存储货币，它有的不过是在一个个交易中记录的数字变化，而这个数字的源头来自矿工。我们挖矿产生的Block实质上是一个账本，其中记录一笔笔交易的记录。每挖出一个Block，就会从无到有生成可以用于交易的value（交易中的数字）给矿工，这便是比特币产生的地方。
* 交易中每个人用钱包来保管自己的UTXO，以及公私钥。公钥就类似与银行卡号，别人通过指定你的公钥来转账给你。秘钥类似于你的密码，但是又有不同：每次你发起转账，都需要通过你的秘钥结合交易的内容来生成一个签名，通过验证签名与公钥来确认身份。签名与交易是一一对应的，即使暴露了也无妨，但是秘钥是绝对不能泄漏的。
* UTXO 其实就是交易的一部分，基于 UTXO 模型的交易由输入和输出两个部分组成:UTXO 模型中的每一笔交易都是由多个交易输入组成的，这些输入其实就是 UTXO + 签名:属于某个人的UTXO加上这个人的签名（由秘钥生成）则可以表示这个人授权使用这个UTXO。每一个交易都可能会有多个输出，每一个输出都可以指向不同的地址，其中也有当前输出包含的值 value，这个value也就是比特币的计量数。
* 交易实现：
  * Transaction.java
```java
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
```
  * TransactionInput.java
```java
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
```
  * TransactionOutput.java
```java
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
```

# 6. 完善钱包（Wallet） 

* 一个钱包中保存着每个账户的公私钥，并且具备统计该账户拥有的UTXO并计算余额的能力，此外钱包还有创建交易发起支出的能力。
```java
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
```

# 7. 完善其它类

* 在StringUtil工具类中，增加生成和验证数字签名的功能。同时还有根据添加到区块中的交易生成merkleRoot的功能，merkelRoot用于标识区块的唯一性。
```java
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
```
* Block类的设置中，用merkleRoot来做数据生成哈希值，同时新增将交易记录添加到区块中的功能。
```java
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
```
* ZJchain类:加强验证区块链有效功能，在将一个块添加到区块中前进行挖矿计算。
```java
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
```                                                                                                        



# 8. 测试

* 在一个测试类中走一遍基本功能：
```java
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
```
* 结果：
![5N08eT](https://gitee.com/zhangjie0524/picgo/raw/master/uPic/5N08eT.png)
