/**
 * 
 */
package rs.dlogic.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

import rs.dlogic.wrapper.AisWrapper;



/**
 * @author Vladan
 *
 */




public class MyAisWrapper extends AisWrapper {	
	
	String formatOut;
	int DL_STATUS;	
    static AisLibrary libInstance;
    
    PointerByReference hnd = new PointerByReference();
	PointerByReference devSerial = new PointerByReference();
	IntByReference devType = new IntByReference();
	IntByReference devID = new IntByReference();
	IntByReference devFW_VER = new IntByReference();
	IntByReference devCommSpeed = new IntByReference();
	byte[]devFTDI_Serial = new byte[9];
	IntByReference devOpened = new IntByReference();
	IntByReference devStatus = new IntByReference();
	IntByReference systemStatus = new IntByReference();
    
    
    
    MyAisWrapper(){
    	libInstance = AisLibrary.aisReaders;
    }

    S_DEVICE dev_hnd;
    
     
    private List<String>myFormats = new ArrayList<String>();
	private ArrayList<Pointer>HND_LIST = new ArrayList<Pointer>();
    
   
    //***************************************************************************
   
    
   void MyFormats(){
	   //format_grid
	   myFormats.add(0, "---------------------------------------------------------------------------------------------------------------------" );
	   myFormats.add(1, "| indx|  Reader HANDLE   | SerialNm | Type h/d | ID  | FW   | speed   | FTDI: sn   | opened | DevStatus | SysStatus |" );
	   myFormats.add(2,"| %3d | %016X | %d | %7d  | %2d  | %d  | %7d | %10s | %5d  | %8d  | %9d |\n");
	   
   }
    
   String AisOpen(){	  
	   for (Pointer hnd : HND_LIST){
		   DL_STATUS = libInstance.AIS_Open(hnd);		   
		   formatOut += String.format("AIS_Open(0x%X):{ %d(%s):%s}\n", hnd.getInt(0), DL_STATUS,
				                     Integer.toHexString(DL_STATUS), E_ERROR_CODES.DL_OK.getValue());		   
	   }
	   return formatOut; 
   }
   
   
    void listDevices(){
    	prepareListForCheck();
    	System.out.println("checking...please wait...");
    	int devCount = AISListUpdateAndGetCount();    	    
    	formatOut = String.format("AIS_List_UpdateAndGetCount()= [%d]\n", devCount);
    	System.out.print(formatOut);
    	
    }
    
    private int addDevice(int deviceType, int deviceId){
    	DL_STATUS = AISListAddDeviceForCheck(deviceType, deviceId);
    	formatOut = String.format("AIS_List_AddDeviceForCheck(type: %d, id: %d)> { %s }", 
    			                 deviceType, deviceId, libInstance.dl_status2str(DL_STATUS).getString(0));
    	System.out.println(formatOut);
    	return DL_STATUS;
    }
    
    private Boolean loadListFromFile(){
    	String fileName = "readers.ini";
    	String devTypeStr;
    	File f = new File(fileName);
    	int addedDevType = 0;
    	int devId;
    	
    	IntByReference devTypeEnum = new IntByReference();
        if (!f.exists()){
        	System.out.printf("File <%s> not found. \n",fileName);
        	return false;
        }
        try {        	
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) !=null)
			{				
				if (line.startsWith("#")) continue;							    
					String []linePart = line.trim().split(":");
					if (linePart.length <= 1) continue;
					   devTypeStr = linePart[0];
					   devId = Integer.parseInt(linePart[1]);					 
					   DL_STATUS = libInstance.device_type_str2enum(devTypeStr, devTypeEnum);
					   if (addDevice(devTypeEnum.getValue(), devId) == 0){
						  addedDevType ++;
			}
		  }			
		} catch (IOException e) {			
			e.printStackTrace();
		}		
        if (addedDevType > 0){
			return true;
		}else{
			System.out.println("Error. No device is added in the list...");
			return false;
		}
        
    }
               
    void listForCheckPrint(){
    	String getDevices;    	
    	PointerByReference dev_type_str = new PointerByReference();
    	int deviceType;
    	int deviceId;    	
    	getDevices = AISGetDevicesForCheck();
    	if (getDevices.length() == 0)
    	   {return;}        
    	for (String item : getDevices.split("\n"))
    	{
    	  String[] t = item.split(":");
    	  deviceType = Integer.parseInt(t[0]);
    	  deviceId = Integer.parseInt(t[1]);    	 
    	  DL_STATUS = libInstance.device_type_enum2str(deviceType, dev_type_str);    	  
    	  System.out.printf("     %20s (enum= %d) on ID %d\n", dev_type_str.getValue().getString(0), deviceType, deviceId);     	 
    	}    	    	    
    }
    
    
    
    void prepareListForCheck(){    	
    	System.out.println("AIS_List_GetDevicesForCheck() BEFORE / DLL STARTUP");
    	listForCheckPrint();
    	AISListEraseAllDeviceForCheck();
    	if (!loadListFromFile()){
    		System.out.println("Tester try to connect with a Base HD device on any/unkown ID");
    	    addDevice(E_KNOWN_DEVICE_TYPES.DL_AIS_BASE_HD_SDK.getDeviceTypes(),0);
    	
    	}
    	System.out.println("AIS_List_GetDevicesForCheck() AFTER LIST UPDATE");
    	listForCheckPrint();
    }
    
    
    void ListEraseAllDevicesForCheck(){
    	System.out.println("AIS_List_EraseAllDevicesForCheck()");
    	AISListEraseAllDeviceForCheck();
    }
    
    void GetDevicesForCheck(){
    	System.out.format("AISGetDevicesForCheck() :%s",AISGetDevicesForCheck());
    }
    
    void ListAddDeviceForCheck(int deviceType, int deviceId){
    	DL_STATUS =  AISListAddDeviceForCheck(deviceType, deviceId);
    	formatOut = String.format("AIS_List_AddDeviceForCheck()>> deviceType: %d : deviceId: %d |DL_STATUS: %d", deviceType, deviceId, DL_STATUS);
    	System.out.println(formatOut);
    }
    
   
    
	void GetListInformation(){    	    	
    	int devCount = AISListUpdateAndGetCount();
    	if (devCount < 0) return;
    	HND_LIST.clear();
    	S_DEVICE dev = new S_DEVICE();    	    	
    	for (int i = 0;i<devCount;i++)
    	{
    		DL_STATUS = libInstance.AIS_List_GetInformation(hnd, 
    				                                        devSerial, 
    				                                        devType, 
    				                                        devID,
    				                                        devFW_VER,
    				                                        devCommSpeed,
    				                                        devFTDI_Serial,    				                                        
    				                                        devOpened, 
    				                                        devStatus, 
    				                                        systemStatus);
    		if (DL_STATUS !=0) return;    		
    		HND_LIST.add(hnd.getValue());    		
    		AisOpen();
    		dev.idx = 1;
    		dev.hnd = hnd.getValue().getInt(0); 
            dev.devSerial = devSerial.getValue().getInt(0); 
            dev.devType = devType.getValue();
            dev.devID = devID.getValue();
            dev.devFW_VER = devFW_VER.getValue();
            dev.devCommSpeed = devCommSpeed.getValue();
            dev.devFTDI_Serial = devFTDI_Serial;                                 
            dev.devOpened = devOpened.getValue();
            dev.devStatus = devStatus.getValue();
            dev.systemStatus = devStatus.getValue();
            
            System.out.println(myFormats.get(0) + "\n" + myFormats.get(1) + "\n");
            //formatOut = String.format(myFormats.get(3),dev.idx,)
    		System.out.format("| %3d | %016X | %d | %7d  | %2d  | %d  | %7d | %10s | %5d  | %8d  | %9d |\n",
    				         dev.idx,
    				         dev.hnd,
    				         dev.devSerial,
    				         dev.devType,
    				         dev.devID,
    				         dev.devFW_VER,
    				         dev.devCommSpeed,
    				         dev.devFTDI_Serial,
    				         dev.devOpened,
    				         dev.devStatus,
    				         dev.systemStatus
    				         );
    	
    	}
    	
    }
    
    
    
    
    void init(){
    	listDevices();
    	MyFormats();
    	GetListInformation();
    }
    
    
	public static void main(String[] args) {			   	  	 
       MyAisWrapper init = new MyAisWrapper();       
       System.out.println(init.AISGetLibraryVersionStr());
       init.init();
	}

}
