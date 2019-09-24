package blockchain;

import java.util.ArrayList;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;
    public int nonce;
    
    //Constructor
    public Block(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }
    
    public String calculateHash(){
        String calculatedhash =  StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
        return calculatedhash;
    }
    
    public void mineBlock(int difficulty){
    	merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        while(!hash.substring(0,difficulty).equals(target)){
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block is Mined! : " + hash);
    }
    
    //Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
    	 if(transaction == null) return false;
    	 if((!"0".equals(previousHash))) {
    		 if(transaction.processTransaction() != true) {
    			 System.out.println("Transaction failed to process");
    			 return false;
    		 }
    	 }
    	 transactions.add(transaction);
    	 System.out.println("Transaction successfully added to block");
    	 return true;
    }
}