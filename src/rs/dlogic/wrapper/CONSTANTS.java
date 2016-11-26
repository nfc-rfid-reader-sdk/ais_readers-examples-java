/**
 * 
 */
package rs.dlogic.wrapper;

/**
 * @author Vladan
 *
 */

public class CONSTANTS{
	

public enum AlfaNumeric{
	PASS            ("1111");
	
	private String strvalue;
	private AlfaNumeric(String value){
		this.strvalue = value;
	}
	public String strValue(){
		return strvalue;
	}
}

public enum Numeric {  
  
   SECONDS         (10),
   RECORDS_TO_ACK  (1),
   NFC_UID_MAX_LEN (10),
   PULSE_DURATION  (2000);
   
   
   
   private int value;
   private  Numeric(int value){
	    this.value = value;
   }
   public int value(){
       return value;
   }  	
}
}