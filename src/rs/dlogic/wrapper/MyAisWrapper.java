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
    
    MyAisWrapper(){
    	libInstance = AisLibrary.aisReaders;
    }

    
    //***************************************************************************
    
    void listDevices(){
    	prepareListForCheck();
    	System.out.println("checking...please wait...");
    	
    	
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
//				if (!line.startsWith("#")) // && !line.startsWith("\n") && !line.startsWith("\r\n"))
//				{
															
					String []linePart = line.split("\\#\\: ");
					
					System.out.println(linePart[1]);																
					//devId = Integer.parseInt(llineSplit[1]);
					
					//System.out.println("DevType=" + lineSplit[0]);					
					//System.out.println("devID= " + devId);
					
//					devId = Integer.parseInt(lineSplit[1]);																										
//					DL_STATUS = libInstance.device_type_str2enum(devType, devTypeEnum);
//					if (addDevice(devTypeEnum.getValue(), devId) == 0){
//						addedDevType ++;
//					}
				//}
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
    
    
    void init(){
    	listDevices();
    }
    
    
	public static void main(String[] args) {			   	  	 
       MyAisWrapper init = new MyAisWrapper();       
       System.out.println(init.AISGetLibraryVersionStr());
       init.init();
	}

}
