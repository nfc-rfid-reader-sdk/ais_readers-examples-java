/**
 * 
 */
package rs.dlogic.wrapper;

import java.util.Date;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;


/**
 * @author Vladan
 *
 */
public class Rte extends AisWrapper {
	
	S_PROGRESS progress = new S_PROGRESS();
	private int DL_STATUS;	
	public int NFC_UID_MAX_LEN = 10;
	
	protected String[] rteListHeader = {"---------------------------------------------------------------------------------------------------------------------------------------------",
			                            "| Idx   |              action              | RD ID | Card ID | JobNr |    NFC [length] : UID    | Time-stamp |       Date - Time            |"
			};  
	
	protected String rteFormat = "| %5d | %32s | %5d | %7d | %5d | %24s | %10d | %s | ";
	
	protected RetValues AISPrintRTE(S_DEVICE dev){
		String res; 
		StringBuilder result = new StringBuilder();
		StringBuilder nfcuid = new StringBuilder();		
		IntByReference logIndex = new IntByReference();
		IntByReference logAction = new IntByReference();
		IntByReference logReaderID = new IntByReference();
		IntByReference logCardID = new IntByReference();
		IntByReference logSystemID = new IntByReference();
		byte[] nfcUid = new byte[NFC_UID_MAX_LEN];
		IntByReference nfcUidLen = new IntByReference();
		LongByReference timeStamp = new LongByReference();		
		int rteCount = libInstance.AIS_ReadLog_Count(dev.hnd);
			
		String rteHead = String.format("AIS_ReadRTE_Count = %d\n", rteCount)
				       + "= RTE Real Time Events = \n"
				       + rteListHeader[0] + "\n"
				       + rteListHeader[1] + "\n"
				       + rteListHeader[0] + "\n";
		
		while (true){			
			DL_STATUS = libInstance.AIS_ReadRTE(dev.hnd, logIndex, logAction, logReaderID, logCardID, logSystemID, nfcUid, nfcUidLen, timeStamp);			
			dev.log.index = logIndex.getValue();
			dev.log.action = logAction.getValue();
			dev.log.readerID = logReaderID.getValue();
			dev.log.cardID = logCardID.getValue();
			dev.log.systemID = logSystemID.getValue();
			dev.log.nfc_uid = nfcUid;
			dev.log.nfcUidLen = nfcUidLen.getValue();
			dev.log.timestamp = timeStamp.getValue();											
			if (DL_STATUS !=0){
				break;
			}
			for (int i=0;i<nfcUidLen.getValue();i++)
				nfcuid.append(String.format(":%02X", nfcUid[i]));
			
			result.append(String.format(rteFormat, dev.log.index, 
					               AisWrapper.libInstance.dbg_action2str(dev.log.action).getString(0),
					               dev.log.readerID, dev.log.cardID, dev.log.systemID,
					               "[" + Integer.toString(dev.log.nfcUidLen) + "] " + nfcuid,
					               dev.log.timestamp, new Date(dev.log.timestamp * 1000).toString())
					              );
								
		}		
		res = result + "\n"			  
			  + rteListHeader[0] 
			  + "\n";				
		rv.ret_string = rteHead + res + String.format("LOG unread (incremental) = %d %s\n", dev.logUnread, libInstance.dl_status2str(DL_STATUS).getString(0));
		return rv;
	}
	
	
	protected RetValues AISPrintLog(S_DEVICE dev){				
		String res;
		StringBuilder result = new StringBuilder();
		StringBuilder nfcuid = new StringBuilder();
		IntByReference logIndex = new IntByReference();
		IntByReference logAction =  new IntByReference();
		IntByReference logReaderID = new IntByReference();
		IntByReference logCardID = new IntByReference();
		IntByReference logSystemID = new IntByReference();
		byte[] nfcUid = new byte[NFC_UID_MAX_LEN];
		IntByReference nfcUidLen = new IntByReference();
		LongByReference timeStamp = new LongByReference();		
		
		String rteHead = rteListHeader[0] + "\n"
			           + rteListHeader[1] + "\n"
			           + rteListHeader[0] + "\n";
		
		while (true){
			dev.status = libInstance.AIS_ReadLog(dev.hnd,
					                             logIndex,
					                             logAction, 
					                             logReaderID, 
					                             logCardID, 
					                             logSystemID, 
					                             nfcUid, 
					                             nfcUidLen, 
					                             timeStamp);
			
			dev.log.index = logIndex.getValue();
			dev.log.action = logAction.getValue();
			dev.log.readerID = logReaderID.getValue();
			dev.log.cardID = logCardID.getValue();
			dev.log.systemID = logSystemID.getValue();
			dev.log.nfc_uid = nfcUid;
			dev.log.nfcUidLen = nfcUidLen.getValue();
			dev.log.timestamp = timeStamp.getValue();
			
			if (dev.status != 0){				
				break;
			}
			
			
			for (int i=0;i<nfcUidLen.getValue();i++)
				nfcuid.append(String.format(":%02X", nfcUid[i]));
			
			result.append(String.format(rteFormat, dev.log.index, 
		               libInstance.dbg_action2str(dev.log.action).getString(0),
		               dev.log.readerID, dev.log.cardID, dev.log.systemID,		               
		               "[" + Integer.toString(dev.log.nfcUidLen) + "] |" + nfcuid,
		               dev.log.timestamp, new Date(dev.log.timestamp * 1000).toString())
					   + "\n"
					);			
		}
		res = result 
				 // + "\n" 
				  + rteListHeader[0]
				  + "\n";
				  
		rv.ret_string = rteHead + res + String.format("AIS_GetLog() %s\n",  libInstance.dl_status2str(dev.status).getString(0));
				
		return rv;
	}
	
	
	
	public RetValues AISMainLoop(S_DEVICE dev){		
		IntByReference realTimeEvents = new IntByReference();
		IntByReference logAvailable = new IntByReference();
		IntByReference unreadLog = new IntByReference();
		IntByReference cmdResponses = new IntByReference();    
		IntByReference cmdPercent = new IntByReference();
		IntByReference devStatus = new IntByReference();
		IntByReference timeOutOccured = new IntByReference();
		IntByReference _status = new IntByReference();
						
		dev.status = libInstance.AIS_MainLoop(dev.hnd, 
				      realTimeEvents, 
				      logAvailable, 
				      unreadLog, 
				      cmdResponses, 
				      cmdPercent, 
				      devStatus, 
				      timeOutOccured, 
				      _status);
		
		dev.realTimeEvents = realTimeEvents.getValue();
		dev.logAvailable = logAvailable.getValue();
		dev.logUnread = unreadLog.getValue();
		dev.cmdResponses = cmdResponses.getValue();
		dev.cmdPercent = cmdPercent.getValue();
		dev.devStatus = devStatus.getValue();
		dev.timeOutOccured = timeOutOccured.getValue();
		dev.Status = _status.getValue();
		
		if (dev.status != 0){
			if (dev.statusLast != dev.status){
			   System.out.println(libInstance.dl_status2str(dev.status).getString(0));			   
			}
			dev.statusLast = dev.status;
			rv.ret_state = false;
			return rv;
		}
		
		if (dev.realTimeEvents != 0){
			rv = AISPrintRTE(dev);
			System.out.println(rv.ret_string);
			
		}
		
		if (dev.logAvailable != 0){
			rv = AISPrintLog(dev);
			//System.out.print(rv.ret_string);
		}
		
		if(dev.logUnread != 0){
			if (dev.logUnreadLast != dev.logUnread){
				dev.logUnreadLast = dev.logUnread;
				//System.out.print(printLogUnread(dev));
			}
		}
		
		if (dev.timeOutOccured != 0){
			System.out.printf("TimeoutOccurred= %d\n" , dev.timeOutOccured);
		}
		 
		if (dev.Status != 0){
			System.out.printf("[%d] local_status= %s\n", dev.idx, libInstance. dl_status2str(dev.Status).getString(0));
		}
		
		if (dev.cmdPercent != 0){
			printPercent(dev.cmdPercent);
		}
		
		if (dev.cmdResponses != 0){
			dev.cmdFinish = true;
			System.out.print("\n-- COMMAND FINISH !\n");
		}
		rv.ret_state = true;
		return rv;
	}
	
  /**
   * 
   * @param dev - S_DEVICE class reference
   * @return unreaded log count
   */
  protected String printLogUnread(S_DEVICE dev){
	  return String.format("LOG unread (incremental) = %d", dev.logUnread);
  }
 
  protected void printPercent(int Percent){
	  
	  if (progress.printHdr == true){
		  progress.printHdr = false;
		  progress.percentOld = -1;		  
	  }
	  while (progress.percentOld != Percent){
		 if (progress.percentOld < 100){
			 System.out.print(".");
		 }
		 progress.percentOld ++;
	  }
  }
  
  protected void DoCmd(S_DEVICE dev){
	  RetValues rv;
	  if (dev.status !=0){return;}
	  dev.cmdFinish = false;
	  progress.printHdr = true;
	  while(!dev.cmdFinish){
		  rv = AISMainLoop(dev);		  		  		  		  
		  if (!rv.ret_state){
			  break;
		  }
		  
	  }
	  
  }
   
  





}
