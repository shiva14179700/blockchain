package blockchain;

import java.security.*;
import java.util.ArrayList;

public class Transaction {
    public String transactionId;
    public PublicKey sender;
    public PublicKey reciepient;
    public float value;
    public byte[] signature;
    
    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
    
    private static int sequence = 0;
    
    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }
    
    public String calculateHash(){
        sequence++;
        return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + 
                StringUtil.getStringFromKey(reciepient) + 
                Float.toString(value) + sequence );
    }
    
    //signs the data to avoid tampering
    public void generateSignature(PrivateKey privateKey){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }   
    
    //verifies the signature
    public boolean verifySignature(){
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }
    
    public boolean processTransaction() {
    	if(verifySignature() == false) {
    		System.out.println("Signature for Transaction failed to verify");
    		return false;
    	}
    	
    	//check the transaction inputs for unspent money
    	for(TransactionInput i : inputs) {
    		i.UTXO = Blockchain.UTXOs.get(i.transactionOutputId);
    	}
    	
    	//checking if the transaction is valid
    	if(getInputsValue()<Blockchain.minimumTransaction) {
    		System.out.println("Transaction inputs are not enough : " + getInputsValue());
    		return false;
    	}
    	
    	//generate transaction outputs
    	float leftOver = getInputsValue() - value;
    	transactionId = calculateHash();
    	outputs.add(new TransactionOutput(this.reciepient, value, transactionId)); //send value to reciepient
    	outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
    	
    	//add transaction outputs to Unspent List
    	for(TransactionOutput o : outputs) {
    		Blockchain.UTXOs.put(o.id, o);
    	}
    	
    	//remove transaction inputs from UTXOs List
    	for(TransactionInput i : inputs) {
    		if(i.UTXO == null) continue;
    		Blockchain.UTXOs.remove(i.UTXO.id);
    	}
    	
    	return true;
    }
    
    //calculate sum of inputs(UTXOs) values
    public float getInputsValue() {
    	float total = 0;
    	for(TransactionInput i : inputs) {
    		if(i.UTXO == null) continue; //if transaction is not found => skipping it
    		total += i.UTXO.value;
    	}
    	return total;
    }
    
    //calculate sum of outputs
    public float getOutputsValue() {
    	float total = 0;
    	for(TransactionOutput o : outputs) {
    		total += o.value;
    	}
    	return total;
    }
}

