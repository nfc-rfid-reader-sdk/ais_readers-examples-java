/**
 * 
 */
package rs.dlogic.aiswrapper;


/**
 * @author Vladan S
 * @version 1.0.0
 * {@link}
 */

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Date;



public class AisWrapper {			
	
	public class S_PROGRESS{
		public boolean printHdr;
		public int percentOld;
	}

	public class S_LOG{
		public int index;
		public int action;
		public int readerID;
		public int cardID;
		public int systemID;
		public byte[]nfc_uid;
		public int nfcUidLen;
		public long timestamp;
	}
	
	
	
	
   public class RetValues{	
	   public long currentTime;	
	   public long timezone;
	   public int DST;
	   public long offset;
	   public int dl_status;	
	   public int listSize = 0;
	   public String strList = null;	
	   public int intercom;
	   public int door;
	   public int relay_state;
	   public int hardwareType;
	   public int firmwareVersion;
	   public Boolean ret_state;
	   public String ret_string;
	}
	
	
	public class S_DEVICE{	
		public int idx;
		public Pointer hnd;
		public String devSerial;
		public int devType;
		public int devID;
		public int devFW_VER;
		public int devCommSpeed;	
		public String devFTDI_Serial;
		public int devOpened;
		public int devStatus;
		public int statusLast;
		public int systemStatus;
		public int relayState;
		public int realTimeEvents;
		public int logAvailable;
		public int logUnread;
		public int logUnreadLast;
		public int cmdResponses;
		public int cmdPercent;
		public Boolean cmdFinish;
		public int timeOutOccured;
		public int status;
		public int Status;	
		public S_LOG log = new S_LOG();
	}
	
	
	
	public static AisLibrary libInstance;
	RetValues rv = new RetValues();
	public AisWrapper(){
		libInstance = AisLibrary.aisReaders;		 
	}
	
		
	public int NFC_UID_MAX_LEN = 10;

	protected String AisWrappGetLibraryVersionStr(){
    	return libInstance.AIS_GetLibraryVersionStr().getString(0);
    }
	
	protected void AisWrappListEraseAllDeviceForCheck(){
		libInstance.AIS_List_EraseAllDevicesForCheck();
	}
	
	protected int AisWrappListAddDeviceForCheck(int deviceType,int deviceId){
		return libInstance.AIS_List_AddDeviceForCheck(deviceType, deviceId);
	}
	
	protected String AisWrappGetDevicesForCheck(){
		return libInstance.AIS_List_GetDevicesForCheck().getString(0);
	}
	
	protected int AisWrappListUpdateAndGetCount(){
    	IntByReference deviceCount = new IntByReference();
    	libInstance.AIS_List_UpdateAndGetCount(deviceCount);
    	return deviceCount.getValue();
    }
	
    protected RetValues AisWrappGetTime(Pointer devHnd){
    	LongByReference currentTime = new LongByReference(0);    	
    	IntByReference timezone = new IntByReference();
    	IntByReference DST = new IntByReference();
    	IntByReference offset = new IntByReference();
    	int DL_STATUS = libInstance.AIS_GetTime(devHnd, 
    			                            currentTime, 
    			                            timezone, 
    			                            DST, 
    			                            offset);    	    			    	    	   	
	   rv.currentTime = currentTime.getValue() * 1000;
	   rv.DST = DST.getValue();
	   rv.offset = offset.getValue();
	   rv.timezone = timezone.getValue();
	   rv.dl_status = DL_STATUS;	  	  
       return rv;    			    		    
    }
    
    protected RetValues AisWrappSetTime(Pointer devHnd, String pass){    	    	     	    	
    	long currentTime = new Date().getTime();     	
    	long timezone = libInstance.sys_get_timezone();
    	int DST = libInstance.sys_get_daylight();
    	long offset = libInstance.sys_get_dstbias();
    	byte[]PASS = pass.getBytes(); 
    	int DL_STATUS = libInstance.AIS_SetTime(devHnd, 
    			                            PASS,
    			                            new NativeLong(currentTime / 1000),    			                           
    			                            timezone, 
    			                            DST, 
    			                            offset);    	    			    	    	   	
	   rv.currentTime = currentTime;      
	   rv.DST = DST;
	   rv.offset = offset;
	   rv.timezone = timezone;
	   rv.dl_status = DL_STATUS;
       return rv;    	
    }
    
    protected RetValues AisWrappConfigFileRead(S_DEVICE dev, String fileName, String pass){    	
    	byte[]passw = pass.getBytes();
    	byte[]configFile = fileName.getBytes();
    	dev.status = libInstance.AIS_Config_Read(dev.hnd, passw, configFile);    	   	    
    	rv.dl_status = dev.status;
    	return rv;
    }
    
    public RetValues AisWrappConfigFileWrite(S_DEVICE dev, String fileName){
    	byte[]configFile = fileName.getBytes();
    	dev.status = libInstance.AIS_Config_Send(dev.hnd, configFile);
    	rv.dl_status = dev.status;
    	return rv;
    }
    
    
    protected RetValues AisWrappBlackListWrite(S_DEVICE dev, String pass, String blackList){    	    	
    	
    	byte[] PASS = pass.getBytes();
    	byte[] bBlackList = blackList.getBytes();
    	dev.devStatus = libInstance.AIS_Blacklist_Write(dev.hnd, PASS, bBlackList);      	
    	rv.strList = blackList;
    	rv.dl_status = dev.devStatus;
    	return rv;
    }
    
    protected RetValues AisWrappWhiteListWrite(S_DEVICE dev, String pass, String whiteList){    	   	
    	
    	byte[] PASS = pass.getBytes();
    	byte[] bWhiteList = whiteList.getBytes();
    	dev.devStatus = libInstance.AIS_Whitelist_Write(dev.hnd, PASS, bWhiteList);      	
    	rv.strList = whiteList;
    	rv.dl_status = dev.devStatus;
    	return rv;
    }
    
    protected RetValues AisWrappBlackListRead(S_DEVICE dev, String pass){
    	PointerByReference blackList = new PointerByReference();    	  	
    	String listSize;
    	byte[] PASS = pass.getBytes();
    	dev.devStatus = libInstance.AIS_Blacklist_Read(dev.hnd, PASS, blackList);
    	if (dev.devStatus == E_ERROR_CODES.DL_OK.value()){
    		listSize = blackList.getValue().getString(0);    		
    	}else {listSize = "";}
    	rv.listSize = listSize.length();
    	if (blackList.getValue() == null){
    		rv.strList="No Black list";
    		}
    	else
    	   rv.strList =  blackList.getValue().getString(0);
  	    rv.dl_status = dev.devStatus;
        return rv;   
    }
    
    protected RetValues AisWrappWhiteListRead(S_DEVICE dev, String pass){
    	PointerByReference whiteList = new PointerByReference();    	    	
    	String listSize;
    	byte[] PASS = pass.getBytes();
    	dev.devStatus = libInstance.AIS_Whitelist_Read(dev.hnd, PASS, whiteList);    
    	if (dev.devStatus == E_ERROR_CODES.DL_OK.value()){
    		listSize = whiteList.getValue().getString(0);    		
    	}else {listSize = "";}
    	rv.listSize = listSize.length();
    	if (whiteList.getValue() == null){
    		rv.strList="No White list";
    	 }
    	else
    	   rv.strList =  whiteList.getValue().getString(0);
  	    rv.dl_status = dev.devStatus;
        return rv;   
    }
    
    protected RetValues AisWrappTestLights(S_DEVICE dev, int greenMaster, int redMaster, int greenSlave, int redSlave){    	     	  	
   	    dev.devStatus = libInstance.AIS_LightControl(dev.hnd, greenMaster, redMaster, greenSlave, redSlave);   	    		                        	   
   	    rv.dl_status = dev.devStatus;    	    	
   	    return rv;
    }
    
    protected RetValues AisWrappGetIOState(S_DEVICE dev){    	
    	IntByReference intercom = new IntByReference();
    	IntByReference door = new IntByReference();
    	IntByReference relay_state = new IntByReference();
    	dev.devStatus = libInstance.AIS_GetIoState(dev.hnd, intercom, door, relay_state);    	    	
    	rv.dl_status = dev.devStatus;
    	rv.door = door.getValue();
    	rv.intercom = intercom.getValue();
    	rv.relay_state = relay_state.getValue();
    	return rv;    	
    }
    
    protected RetValues AisWrappRelayToogle(S_DEVICE dev){    	
    	AisWrappGetIOState(dev);
    	if (dev.relayState == 0) {
    		dev.relayState = 1; 		   
    	}
    	else dev.relayState = 0;    	    
    	dev.devStatus = libInstance.AIS_RelayStateSet(dev.hnd, dev.relayState);
    	rv.dl_status = dev.devStatus;
    	rv.relay_state = dev.relayState;
    	return rv;    
    }
    
    
    
    protected RetValues AisWrappLockOpen(S_DEVICE dev,int pulseDuration){    	    
    	dev.devStatus =libInstance.AIS_LockOpen(dev.hnd, pulseDuration);
    	rv.dl_status = dev.devStatus;
    	return rv;
    }
    
    protected RetValues AisWrappGetVersion(S_DEVICE dev){
    	IntByReference hardwareType = new IntByReference();
    	IntByReference firmwareVersion = new IntByReference();
    	rv.dl_status = libInstance.AIS_GetVersion(dev.hnd, hardwareType, firmwareVersion);
    	rv.hardwareType = hardwareType.getValue();
    	rv.firmwareVersion = firmwareVersion.getValue();
    	return rv;
    }
 
    
    
    
    
    
    
    
    
    
 //*********************************************************************************   
	
static String sPlatform;
public static String GetLibFullPath() {

    String lib_path = System.getProperty("user.dir");
    switch(Platform.getOSType())
    {
        case Platform.UNSPECIFIED:
            // throw exception            
            break;            
        case Platform.WINDOWS:
            if (Platform.is64Bit()){
               lib_path += "\\lib\\aisreaders\\windows\\x86_64\\"; 
            }else{
               lib_path += "\\lib\\aisreaders\\windows\\x86\\"; 
            }                                
            break;
        case Platform.MAC:
            if (Platform.is64Bit()){
               lib_path += "\\lib\\aisreaders\\osx\\x86_64\\"; 
            }
            break;
        case Platform.LINUX:
            if (Platform.is64Bit()){
                lib_path += "\\lib\\aisreaders\\linux\\x86_64\\";
            }else
            {
               lib_path += "\\lib\\aisreaders\\linux\\x86\\"; 
            }
        default:            
            lib_path += "/lib/aisreaders/";            
            break;
    }   
    
    lib_path += GetPlatformType();  
    return lib_path;
}

/**
 * The type of platform 1: isLinux() 2: isWindows()
 * @return .so or .dll name based on the platform as String type
 */
private static String GetPlatformType()
{  
  int osType = Platform.getOSType();	  
  String libName ="ais_readers";
  String prefix = "";
  String postfix = "";
  String extension = "";
  String platformType = "";
  
	  switch (osType) 
	  {
		  case Platform.LINUX:
		      prefix = "lib";
		      extension = ".so";
		      break;
		
		  case Platform.WINDOWS:
		      prefix = "";
		      extension = ".dll";		      
		      break;
	  }
	  
	  if (Platform.is64Bit()){
		  postfix += "-x86_64";					  
	  } else{ 
		  postfix +="x86";	  	 		 
	  }	   	 
   
	  platformType = prefix + libName + "-" + postfix + extension;	 
   return platformType;
    }
 
public interface AisLibrary extends Library{
	
	AisLibrary aisReaders = (AisLibrary)Native.loadLibrary(AisWrapper.GetLibFullPath(),AisLibrary.class);
			
    Pointer AIS_GetLibraryVersionStr();
	
	int AIS_GetVersion(Pointer device,
			          IntByReference hardware_type,
			          IntByReference firmware_version
			          );
	
	int AIS_MainLoop(Pointer pDevice_HND, 
             IntByReference RealTimeEvents,
			 IntByReference LogAvailable,
			 IntByReference LogUnread,
			 IntByReference cmdResponses,
			 IntByReference cmdPercent,
			 IntByReference DeviceStatus,
			 IntByReference TimeoutOccurred,
		     IntByReference Status);

   int AIS_List_UpdateAndGetCount(IntByReference device_count);

   int AIS_List_GetInformation(PointerByReference pDevice_HND,
						    PointerByReference pDevice_Serial,						    						    
   		                    IntByReference pDevice_type,
						    IntByReference pDevice_ID,
						    IntByReference pDevice_FW_VER,
						    IntByReference pDevice_CommSpeed,						    						  
						    PointerByReference pDevice_FTDI_Serial,						   
						    IntByReference pDevice_isOpened,
						    IntByReference pDevice_Status,
						    IntByReference pSystem_Status								
							);

   int AIS_Open(Pointer device); 
   
   int AIS_ReadRTE_Count(Pointer device);
      
   int AIS_ReadRTE(Pointer device,
   				IntByReference log_index,
   				IntByReference log_action,
   				IntByReference log_reader_id, 
   				IntByReference log_card_id,
   				IntByReference log_system_id,
   				byte[] nfc_uid,
   				IntByReference nfc_uid_len,
   				LongByReference timestamp
   				);
   
   int AIS_SetTime(Pointer device, 
			byte[] str_password, 			
			NativeLong timeToSet,
			long timezone,
			int DST,
			long offset);
   
   int AIS_GetTime(Pointer device,
   		LongByReference current_time,		
   		IntByReference timezone,
   		IntByReference DST,
   		IntByReference offset);
   		
   int AIS_GetLog(Pointer device, 
   		      byte[] str_password);
  
   int AIS_GetLogByTime(Pointer device,
		                byte[] str_password,
		                long time_from,
		                long time_to);
   
   int AIS_GetLogByIndex(Pointer device, 
		                byte[] password,
		                int start_index,
		                int end_index);
   
   
   
   int AIS_ReadLog_Count(Pointer device);
   
   int AIS_ReadLog_File_Size(Pointer device);
	
   int AIS_ReadLog_File_Get(Pointer device, byte[] file);
   
   int AIS_ReadLog(Pointer device, 
		    IntByReference log_index,
			IntByReference log_action, 
			IntByReference log_reader_id, 
			IntByReference log_card_id, 
			IntByReference log_system_id,
			byte[]nfc_uid,
			IntByReference nfc_uid_len,
			LongByReference timestamp);
   
   int AIS_UnreadLOG_Get(Pointer device,
		    IntByReference log_index,
			IntByReference log_action, 
			IntByReference log_reader_id, 
			IntByReference log_card_id, 
			IntByReference log_system_id,
			byte[]nfc_uid,
			IntByReference nfc_uid_len,
			LongByReference timestamp
		   );
   
   int AIS_UnreadLOG_Ack(Pointer device,
		   int records_to_ack);
   
   int AIS_Close(Pointer device);
   
   int AIS_Restart(Pointer device);
   
   int AIS_Blacklist_Read(Pointer device,   		             
   		             byte[] password,   		             
   		             PointerByReference str_blacklist 
   		             );
   
   int AIS_Whitelist_Read(Pointer device,
		                  byte[] password,   		             
	                      PointerByReference str_whitelist
	                     );
   
      
   int AIS_Blacklist_Write(Pointer device,
   		              byte[] password, 
   		              byte[] csv_blacklist);
   
   int AIS_Whitelist_Write(Pointer device,
		   				   byte[] password, 
		   				   byte[] csv_whitelist);
   
   int AIS_BatteryGetInfo(Pointer device, 
   		               IntByReference battery_status, 
   		               IntByReference percent);
   
   int AIS_List_AddDeviceForCheck( int device_type, 
   		                        int device_id);

   int AIS_List_EraseDeviceForCheck( int device_type, 
   		                          int device_id);
   
   void AIS_List_EraseAllDevicesForCheck();
   
   Pointer AIS_List_GetDevicesForCheck();	
   
   int AIS_LightControl(Pointer device, 
		   				int green_master,
		   				int red_master,
		   				int green_slave,
		   				int red_slave);
   
   int AIS_LockOpen(Pointer device, int pulse_duration);
   
   int AIS_Config_Read(Pointer device,
		              byte[] password,
		              byte[] config_bin_filename);
   
   int AIS_Config_Send(Pointer device,
                       byte[] config_bin_filename);
   
   
   
   int AIS_GetIoState(Pointer device,
		              IntByReference intercom,
		              IntByReference door,
		              IntByReference relay_state);
       
   int AIS_RelayStateSet(Pointer device, int state);
   
   int AIS_EE_WriteProtect(Pointer device, 
		                   byte[] password);
   
   int AIS_EE_WriteUnProtect(Pointer device,
		                  byte[] password);
   
   int AIS_ChangePassword(Pointer device,
		                 byte[]PASS,
		                 byte[]newPass);
   
   
   int AIS_FW_Update(Pointer device,
		            String firmware_bin_file,
		            int percent);
   
   int device_type_enum2str(int devType, PointerByReference dev_type_str);          
   int device_type_str2enum(String devTypeStr,IntByReference devType);   
   Pointer dl_status2str(int status);   
   Pointer dbg_action2str(int action);
  
   Pointer sys_get_timezone_info();
   long sys_get_timezone();
   int sys_get_daylight();
   long sys_get_dstbias();
   
   int dbg_device_type (int devType, PointerByReference devName,
		                PointerByReference devDescription, 
		                IntByReference hwType, IntByReference speed, 
		                IntByReference rteTest, IntByReference isHalfDuplex,
		                IntByReference isAloneOnTheBus);
  
  }
}
