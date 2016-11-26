/**
 * 
 */
package rs.dlogic.wrapper;

import java.util.Date;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import rs.dlogic.wrapper.AisWrapper.AisLibrary;

/**
 * @author Vladan
 *
 */
public class Rte {
	
	private String rte = "";
	private int DL_STATUS;
	
			
	String[] rteListHeader = {"-----------------------------------------------------------------------------------------------------------------------------------------",
			"| Idx   |              action              | RD ID | Card ID | JobNr |    NFC [length] : UID    | Time-stamp |       Date - Time        |"
			};  
	
	String rteFormat = "| {0:5d} | {1:28s} | {2:5d} | {3:7d} | {4:5d} | {5:24s} | {6:10d} | {7:s} | ";
	
	public RetValues AisPrintRTE(S_DEVICE dev){
		RetValues rv = null;
		String resRte = "", res = "";
		IntByReference logIndex = new IntByReference();
		IntByReference logAction = new IntByReference();
		IntByReference logReaderID = new IntByReference();
		IntByReference logCardID = new IntByReference();
		IntByReference logSystemID = new IntByReference();
		byte[] nfcUid = new byte[CONSTANTS.Numeric.NFC_UID_MAX_LEN.value()];
		IntByReference nfcUidLen = new IntByReference();
		LongByReference timeStamp = new LongByReference();
		String nfcuid = "";
		int rteCount = AisWrapper.libInstance.AIS_ReadLog_Count(dev.hnd);
		String rteHead = String.format("AIS_ReadRTE_Count = %d\n", rteCount)
				       + "= RTE Real Time Events = \n"
				       + rteListHeader[0] + "\n"
				       + rteListHeader[1] + "\n"
				       + rteListHeader[0] + "\n";
		
		while (true){
			DL_STATUS = AisWrapper.libInstance.AIS_ReadRTE(dev.hnd, logIndex, logAction, logReaderID, logCardID, logSystemID, nfcUid, nfcUidLen, timeStamp);
			
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
			nfcuid = new String(dev.log.nfc_uid);
			resRte += String.format(rteFormat, dev.log.index, 
					               AisWrapper.libInstance.dbg_action2str(dev.log.action),
					               dev.log.readerID, dev.log.cardID, dev.log.systemID,
					               "[" + Integer.toString(dev.log.nfcUidLen) + "] |",
					               dev.log.timestamp, new Date(dev.log.timestamp).toString()
					              );
		
		}
		
		res = resRte + "\n" + rteListHeader[0] + "\n";
		rv.ret_string = rteHead + res + String.format("LOG unread (incremental) = %d\n", dev.logUnread + AisWrapper.libInstance.dl_status2str(DL_STATUS).getString(0));
		return rv;
	}
	
		
	public RetValues AisMainLoop(S_DEVICE dev){
		RetValues rv = null;
		IntByReference realTimeEvents = new IntByReference();
		IntByReference logAvailable = new IntByReference();
		IntByReference unreadLog = new IntByReference();
		IntByReference cmdResponses = new IntByReference();    
		IntByReference cmdPercent = new IntByReference();
		IntByReference devStatus = new IntByReference();
		IntByReference timeOutOccured = new IntByReference();
		IntByReference _status = new IntByReference();
		
		DL_STATUS = AisWrapper.libInstance.AIS_MainLoop(dev.hnd, 
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
		dev.status = _status.getValue();
		
		if (DL_STATUS != 0){
			if (dev.statusLast != dev.status){
			   System.out.println(AisWrapper.libInstance.dl_status2str(DL_STATUS).getInt(0));			   
			}
			rv.ret_state = false;
			return rv;
		}
		
		if (dev.realTimeEvents != 0){
			rv = AisPrintRTE(dev);
			System.out.println(rv.ret_string);
		}
		
		
		
		return rv;
	}
	
	

}
