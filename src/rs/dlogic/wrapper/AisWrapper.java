/**
 * 
 */
package rs.dlogic.wrapper;


/**
 * @author Vladan S
 * @version 1.0.0
 * {@link}
 */

import com.sun.jna.*;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import rs.dlogic.wrapper.AisWrapper.AisLibrary;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


//interface AisFunctions{
//	     String AISGetLibraryVersionStr();
//    }

class S_DEVICE{
	int idx;
	Pointer hnd;
	int devSerial;
	int devType;
	int devID;
	int devFW_VER;
	int devCommSpeed;
	byte[]devFTDI_Serial = new byte[9];
	int devOpened;
	int devStatus;
	int systemStatus;	
}



public class AisWrapper {			
	static  AisLibrary libInstance;
	AisWrapper(){
		libInstance = AisLibrary.aisReaders;
	}
	

	/**
	 * functions implement
	 * @author Vladan
	 *
	 */
	public String AISGetLibraryVersionStr(){
    	return libInstance.AIS_GetLibraryVersionStr().getString(0);
    }
	
	public void AISListEraseAllDeviceForCheck(){
		libInstance.AIS_List_EraseAllDevicesForCheck();
	}
	
	public int AISListAddDeviceForCheck(int deviceType,int deviceId){
		return libInstance.AIS_List_AddDeviceForCheck(deviceType, deviceId);
	}
	
	public String AISGetDevicesForCheck(){
		return libInstance.AIS_List_GetDevicesForCheck().getString(0);
	}
	
    public int AISListUpdateAndGetCount(){
    	IntByReference deviceCount = new IntByReference();
    	libInstance.AIS_List_UpdateAndGetCount(deviceCount);
    	return deviceCount.getValue();
    }
	
	
		
static String sPlatform;
static public String GetLibFullPath() {

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
		
	AisLibrary aisReaders = (AisLibrary)Native.loadLibrary(GetLibFullPath(),AisLibrary.class);
			
	Pointer AIS_GetLibraryVersionStr();
	
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
						    byte[] pDevice_FTDI_Serial,						   
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
			NativeLong time_to_set,
			int timezone,
			int DST,
			int offset);
   
   int AIS_GetTime(Pointer device,
   		LongByReference current_time,
   		IntByReference timezone,
   		IntByReference DST,
   		IntByReference offset);
   		
   int AIS_GetLog(Pointer device, 
   		      byte[] str_password);
  
   int AIS_ReadLog_Count(Pointer device);
   
   int AIS_ReadLog_File_Size(Pointer device);
	
   int AIS_ReadLog_File_Get(Pointer device, byte[] file);
   
   int AIS_ReadLog(Pointer device, 
			IntByReference log_action, 
			IntByReference log_reader_id, 
			IntByReference log_card_id, 
			IntByReference log_system_id,
			byte[]nfc_uid,
			IntByReference nfc_uid_len,
			LongByReference timestamp);
   
   int AIS_Close(Pointer device);
   
   int AIS_Restart(Pointer device);
   
   int AIS_Blacklist_Read(Pointer device,   		             
   		             byte[] password,   		             
   		             ByteByReference csv_blacklist 
   		             );
   
   int AIS_Blacklist_Write(Pointer device,
   		              byte[] password, 
   		              byte[] csv_blacklist);
   
   int AIS_BatteryGetInfo(Pointer device, 
   		               IntByReference battery_status, 
   		               IntByReference percent);
   
   int AIS_List_AddDeviceForCheck( int device_type, 
   		                        int device_id);

   int AIS_List_EraseDeviceForCheck( int device_type, 
   		                          int device_id);
   
   void AIS_List_EraseAllDevicesForCheck();
   
   Pointer AIS_List_GetDevicesForCheck();	
   
   
   int AIS_OpenLock(Pointer p);
   
   
   int device_type_enum2str(int devType, PointerByReference dev_type_str);       
   
   int device_type_str2enum(String devTypeStr,IntByReference devType);
   
   Pointer dl_status2str(int status);
   
}
   	   	   
}
