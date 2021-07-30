package ZJChain;

import org.junit.jupiter.api.Test;

public class BlockTest {

    @Test
    public void test1() {
        Block block1 = new Block("0");
        Block block2 = new Block(block1.hash);
        Block block3 = new Block(block2.hash);
        System.out.println("1: " + "hash: " + block1.hash + " prevHash: " + block1.prevHash);
        System.out.println("2: " + "hash: " + block2.hash + " prevHash: " + block2.prevHash);
        System.out.println("3: " + "hash: " + block3.hash + " prevHash: " + block3.prevHash);
    }
}
