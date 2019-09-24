package blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;
    
    //used to store UTXOs owned by this wallet
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    
    public Wallet(){
        generateKeyPair();
    }
    
    public void generateKeyPair(){
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            
            //Initialising key generator
            keyGen.initialize(ecSpec, random);
            
            //Generating key pair
            KeyPair keyPair = keyGen.generateKeyPair();
            
            //setting public and private keys
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    //returns balance and stores UTXOs owned by this wallet in this.UTXOs
    public float getBalance() {
    	float total = 0;
    	for(Map.Entry<String, TransactionOutput> item : Blockchain.UTXOs.entrySet()) {
    		TransactionOutput UTXO = item.getValue();
    		
    		//if transaction output belongs to me
    		if(UTXO.isMine(publicKey)) { 
    			UTXOs.put(UTXO.id, UTXO); //add it to our list of unspent transactions
    			total += UTXO.value;
    		}
    	}
    	
    	return total;
    }
    
    //Generates and returns a new Transaction from this wallet
    public Transaction sendFunds(PublicKey reciepient, float value) {
    	if(getBalance()<value) {
    		System.out.println("Not Enough funds to send to reciepient");
    		return null;
    	}
    	
    	ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    	
    	float total = 0;
    	for(Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
    		TransactionOutput UTXO = item.getValue();
    		total += UTXO.value;
    		inputs.add(new TransactionInput(UTXO.id));
    		if(total>value) break;
    	}
    	
    	Transaction newTransaction = new Transaction(publicKey, reciepient, value, inputs);
    	newTransaction.generateSignature(privateKey);
    	
    	for(TransactionInput input : inputs) {
    		UTXOs.remove(input.transactionOutputId);
    	}
    	
    	return newTransaction;
    }
}

