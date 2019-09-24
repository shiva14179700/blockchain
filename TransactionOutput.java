package blockchain;

import java.security.PublicKey;

import blockchain.StringUtil;

public class TransactionOutput {
     public String id;
     public PublicKey reciepient;
     public float value;
     public String parentTransactionId;
     
     public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
    	 this.reciepient = reciepient;
    	 this.value = value;
    	 this.parentTransactionId = parentTransactionId;
 		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
     }
     
     //check if coin is mine
     public boolean isMine(PublicKey publicKey) {
    	 return (publicKey == reciepient);
     }
}
