package blockchain;

import java.util.ArrayList;
import org.bouncycastle.*;
import com.google.gson.GsonBuilder;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;

import blockchain.*;

public class Blockchain {
    
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    public static float minimumTransaction = 0.1f;
    public static Transaction genesisTransaction;
    public static int difficulty = 3;
    public static Wallet walletA;
    public static Wallet walletB;

    public static Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        
        //Temporary working list of unspent transactions at a given block state
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        
        //Looping Blockchain
        for(int i=1; i< blockchain.size(); i++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            
            //compare current hash in the block and calculated current hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("current hashes are different");
                return false;
            }
            
            //compare previous hash in the block and calculated previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("previous hashes are different");
                return false;
            }
            
            //check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
            	System.out.println("The block has not been mined");
            	return false;
            }
            
            //Loop through blockchain transactions
            TransactionOutput tempOutput;
            for(int t=0; t<currentBlock.transactions.size(); t++) {
            	Transaction currentTransaction = currentBlock.transactions.get(t);
            	
            	if(!currentTransaction.verifySignature()) {
            		System.out.println("Signature on Transaction(" + t + ") is Invalid");
            		return false;
            	}
            	
            	if(currentTransaction.getInputsValue()!=currentTransaction.getOutputsValue()) {
            		System.out.println("Inputs are not equal to outputs on Transaction(" +t+ ")");
            	    return false;
            	}
            	
            	for(TransactionInput input: currentTransaction.inputs) {
            		tempOutput = tempUTXOs.get(input.transactionOutputId);
            		
            		if(tempOutput == null) {
            			System.out.println("Reference input on transaction(" + t + ") is Missing");
            			return false;
            		}
            		
            		if(input.UTXO.value != tempOutput.value) {
            			System.out.println("Reference Input Transaction(" + t + ") is Invalid");
            		    return false;
            		}
            		
            		tempUTXOs.remove(input.transactionOutputId);
            	}
            	
            	for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
            }
            
        }
        System.out.println("Blockchain is valid");
        return true;
    }
    
    public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
    
    public static void main(String[] args) {	
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		
		//Create wallets:
		walletA = new Wallet();
		walletB = new Wallet();		
		Wallet coinbase = new Wallet();
		
		//create genesis transaction, which sends 100 NoobCoin to walletA: 
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
		genesisTransaction.transactionId = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining Genesis block... ");
//		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(genesisTransaction));
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		
		String blockchainJson1 = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nThe block chain: ");
     	System.out.println(blockchainJson1);
		
		//testing
		Block block1 = new Block(genesis.hash);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 110f));
    	addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		String blockchainJson2 = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson2);
//		
//		Block block2 = new Block(block1.hash);
//		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
//		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
//		addBlock(block2);
//		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//		System.out.println("WalletB's balance is: " + walletB.getBalance());
//		
//		String blockchainJson3 = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//		System.out.println("\nThe block chain: ");
//		System.out.println(blockchainJson3);
//		
//		Block block3 = new Block(block2.hash);
//		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
//		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
//		addBlock(block3);
//		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//		System.out.println("WalletB's balance is: " + walletB.getBalance());
//		
//		String blockchainJson4 = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//		System.out.println("\nThe block chain: ");
//		System.out.println(blockchainJson4);
		
		isChainValid();
		
	}
    
//    public static void main(String[] args) {
//        
//        //security provider
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//        
//        //create new wallets
//        walletA = new Wallet();
//        walletB = new Wallet();
//        Wallet coinbase = new Wallet();
//        
//        //Testing public and private keys
//        System.out.println("Public and Private Keys :");
//        System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
//        System.out.println(StringUtil.getStringFromKey(walletB.publicKey));
//        
//        //Creating a Test Transaction
//        Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
//        transaction.generateSignature(walletA.privateKey);
//        
//        //verify the signature
//        System.out.println("Is Signature verified :");
//        System.out.println(transaction.verifySignature());
//        
////        // Adding blocks to ArrayList
////        blockchain.add(new Block("This is first block","0"));
////        System.out.println("Trying to Mine bock1...");
////        blockchain.get(0).mineBlock(difficulty);
////        
////        blockchain.add(new Block("This is second block",blockchain.get(blockchain.size()-1).hash));
////        System.out.println("Trying to Mine bock2...");
////        blockchain.get(1).mineBlock(difficulty);
////        
////        blockchain.add(new Block("This is third block",blockchain.get(blockchain.size()-1).hash));
////        System.out.println("Trying to Mine bock3...");
////        blockchain.get(2).mineBlock(difficulty);
////        
////        System.out.println("\nBlockchain is valid : " + isChainValid());
////        
////        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
////        System.out.println("\nBlockchain is");
////        System.out.println(blockchainJson);
//        
//    }
    
}

