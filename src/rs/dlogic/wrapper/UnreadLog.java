/**
 * 
 */
package rs.dlogic.wrapper;

import java.util.Date;
import java.util.Scanner;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

/**
 * @author Vladan
 *
 */
public class UnreadLog extends Rte{
	
	private S_DEVICE dev;
	
	UnreadLog(S_DEVICE device){
		this.dev = device;		
	}

    /**
     * UnreadLogInfo()
     * @return read log count and rte count
     */
    protected RetValues AisUnreadLogInfo(){
     String sLogCount = "";
     String sRteCount = "";         
     int logCount = libInstance.AIS_ReadLog_Count(dev.hnd);
     if (logCount == 0){
        sLogCount = String.format("\nAIS_ReadLog_Count() %d\n" , logCount);
     }
     int rteCount = libInstance.AIS_ReadRTE_Count(dev.hnd);
     if (rteCount == 0){
    	 sRteCount = String.format("\nAIS_RTELog_Count() %d\n" , rteCount);
     }
     rv.ret_string = sLogCount + sRteCount;
     return rv;
   }
        
    protected RetValues AisUnreadLogCount(S_DEVICE dev){     	
    	AISMainLoop(dev);
    	rv.ret_string = printLogUnread(dev);
    	return rv;
    }
    
    protected RetValues AisUnreadLogGet(S_DEVICE dev){
    	IntByReference logIndex = new IntByReference();
		IntByReference logAction = new IntByReference();
		IntByReference logReaderID = new IntByReference();
		IntByReference logCardID = new IntByReference();
		IntByReference logSystemID = new IntByReference();
		byte[] nfcUid = new byte[CONSTANTS.Numeric.NFC_UID_MAX_LEN.value()];
		IntByReference nfcUidLen = new IntByReference();
		LongByReference timeStamp = new LongByReference();
		String nfcuid = "";
		String res = "";	
		dev.status = libInstance.AIS_UnreadLOG_Get(dev.hnd, logIndex, logAction, logReaderID, logCardID, logSystemID, nfcUid, nfcUidLen, timeStamp);
		
		if (dev.status !=0){
			rv.ret_string = libInstance.dl_status2str(dev.status).getString(0);
			return rv;			
		}		
		for (int i=0;i<nfcUidLen.getValue();i++)
			nfcuid += String.format(":%02X", nfcUid[i]);
		
		dev.log.index = logIndex.getValue();
		dev.log.action = logAction.getValue();
		dev.log.readerID = logReaderID.getValue();
		dev.log.cardID = logCardID.getValue();
		dev.log.systemID = logSystemID.getValue();
		dev.log.nfc_uid = nfcUid;
		dev.log.nfcUidLen = nfcUidLen.getValue();
		dev.log.timestamp = timeStamp.getValue();
		
		String rteHead = rteListHeader[0] + "\n"
		           + rteListHeader[1] + "\n"
		           + rteListHeader[0] + "\n";
		
		
		res +=String.format(rteFormat, dev.log.index, 
	               libInstance.dbg_action2str(dev.log.action).getString(0),
	               dev.log.readerID, dev.log.cardID, dev.log.systemID,		               
	               "[" + Integer.toString(dev.log.nfcUidLen) + "] " + nfcuid,
	               dev.log.timestamp, new Date(dev.log.timestamp * 1000).toString()
	              );
	    rv.ret_string = rteHead
	    		      + res + "\n"
	    		      + rteListHeader[0] + "\n"
	    		      + String.format("\nAIS_UnreadLOG_Get() %s\n", libInstance.dl_status2str(dev.status).getString(0));
	    return rv;			
    }
   
   
  protected RetValues AisUnreadLogAck(S_DEVICE dev){
	  int recToAck = CONSTANTS.Numeric.RECORDS_TO_ACK.value();
	  dev.status = libInstance.AIS_UnreadLOG_Ack(dev.hnd, recToAck);
	  rv.ret_string = String.format("\nAIS_UnreadLOG_Ack() %s\n",  libInstance.dl_status2str(dev.status).getString(0));
	  return rv;
  }
    
    
    
    
   protected String ShowMeni(){
	   String meni = "\n 1 : Count | 2 : Get | 3 : Ack | x : Exit "
			        + "\n--------------------------\n"
		            + "Press key to select action\n\n";
	   return  meni;
	   
   }
   
   @SuppressWarnings("resource")
   protected boolean MeniLoop(){	   
	Scanner scan = new Scanner(System.in);
	String input = scan.nextLine();
	   if (input.contains("1"))
	     {
		   rv = AisUnreadLogCount(dev);		   
		   System.out.print(rv.ret_string);  
	     }
	   if (input.contains("2")){
		   rv = AisUnreadLogGet(dev);
		   System.out.print(rv.ret_string);
	   }
	   if (input.contains("3")){
		   rv = AisUnreadLogAck(dev);
		   System.out.print(rv.ret_string);
	   }
	   if (input.contains("x")){		  		  
		   return false; 
	   }	   
	   return true;
   }
   
   public void AisUnreadLog(){
	   System.out.println(ShowMeni());
	   while (true){
		   if (MeniLoop() !=true) {break;}
	   }
   }
}
