/**
 * 
 */
package rs.dlogic.wrapper;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

import rs.dlogic.wrapper.AisWrapper;



/**
 * @author Vladan
 *
 */




public class MyAisWrapper extends AisWrapper {	
	
	
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
	private Scanner terminal;
    
   
    //***************************************************************************
   
    
   void MyFormats(){
	   //format_grid
	   myFormats.add(0, "---------------------------------------------------------------------------------------------------------------------" );
	   myFormats.add(1, "| indx|  Reader HANDLE   | SerialNm | Type h/d | ID  | FW   | speed   | FTDI: sn   | opened | DevStatus | SysStatus |" );
	   myFormats.add(2,"| %3d | %016X | %08d | %7d  | %2d  | %d  | %7d | %10s | %5d  | %8d  | %9d |\n");
	   
   }
   
   String ShowMeni(){
	   String myMeni = "\n--------------------------\n" 
			   		 
			         + "q : List devices\t\t\to : Open device\t\t\t\tc : Close device "
			         + "\nd : Get devices count\t\t\tt : Get time\t\t\t\tT : Set time"
			         + "\nr : Real Time Events\t\t\tP : Set application password\t\tp : Change device password"
			         + "\nl : Get log\t\t\t\tn : Get log by Index\t\t\tN : Get log by Time"
			         + "\nu : Get unread log\t\t\tw : White-list Read\t\t\tW : White-list Write"
			         + "\nb : Black-list Read\t\t\tB : Black-list Write\t\t\tL : Test Lights"
			         + "\ng : Get IO state\t\t\tG : Open gate/lock\t\t\ty : Relay toggle state"
			         + "\nE : EERPOM LOCK\t\t\t\te : EERPOM UNLOCK\t\t\tF : Firmware update"
			         + "\ns : Settings read to file\t\tS : Settings write from file\t\ti : Device Information"
			         + "\nm : Meni\t\t\t\tQ : Edit device list for checking"
			         + "\nx : EXIT "
			         + "\n--------------------------\n"
			         + "Press key to select action\n\n";      
        
	  return myMeni;
   }
   
   public void ActiveDevice(S_DEVICE dev, int index){
	   dev.hnd = HND_LIST.get(index);		  		   		  
	   dev.idx = HND_LIST.indexOf(dev.hnd) + 1;
	   System.out.printf(" dev [%d] | hnd= 0x%X   " , dev.idx, dev.hnd.getInt(0));
   }
   
   public Boolean MeniLoop(){
	   terminal = new Scanner(System.in);	   
	   String mChar = terminal.nextLine();
	   S_DEVICE dev = new S_DEVICE();	  
	   int index = 0;
	   if (Character.isDigit(mChar.trim().charAt(0))){		  
		   index = Integer.parseInt(mChar)-1; 		  
	   }	   
	   ActiveDevice(dev, index);
	   
	   if (mChar.contains("x")){
		   System.out.println("EXIT\n");
		   return false;
	   }	   
	   switch (mChar) 
	   {
		case "o":
			System.out.println(AisOpen());
			break;
		case "q":
			AISGetListInformation();
			break;
		case "c": 
			System.out.println(AisClose());
			break;
		case "m": 
			System.out.print(ShowMeni());
			break;
		case "d": //Get devices count
			System.out.printf("DEVICE COUNT: %d\n" ,AISListUpdateAndGetCount());
			break;
		case "t":
			AisGetTime(dev);						
			break;
		}	   	  
	  return true;
   }
   
   
   String AisOpen(){
 	   String out = "";
 	   for (Pointer hnd : HND_LIST){		   		   		  
 		   DL_STATUS = libInstance.AIS_Open(hnd);		   
 		   out += String.format("AIS_Open(0x%X):{ %d(%s):%s}\n", hnd.getInt(0), DL_STATUS,
 				                     Integer.toHexString(DL_STATUS), E_ERROR_CODES.DL_OK.getValue()); //TODO display the numerical value of DL_STATUS		   
 	   }
 	   return out; 
    }
    
    String AisClose(){
 	   String out = "";
 	   for (Pointer hnd : HND_LIST){
 		   DL_STATUS = libInstance.AIS_Close(hnd);		   
 		   out += String.format("AIS_Close(0x%X):{ %d(%s):%s}\n", hnd.getInt(0), DL_STATUS,
 				                     Integer.toHexString(DL_STATUS), E_ERROR_CODES.DL_OK.getValue()); //TODO display the numerical value of DL_STATUS		   
 	   }
 	   return out;
    }
   

    void AisGetTime(S_DEVICE dev){
    	String fOut;    	    	
    	RetValues rv;     	    	
    	rv = AISGetTime(dev.hnd);    	    	    
    	fOut = String.format("AIS_GetTime(dev[%d] hnd=0x%X)> (currentTime= %d | tz= %d | dst= %d | offset= %d): %s\n",
    			               dev.idx, dev.hnd.getInt(0), rv.currentTime, rv.timezone, rv.DST, rv.offset,
    			               new Date(new Date(rv.currentTime).getTime()) );    	    	   
    	System.out.println(fOut);    	    
    }
    
    int AISSetTime(){
    	Pointer device;
		byte[] str_password = new byte[9]; 
		NativeLong time_to_set;
		long timezone = libInstance.sys_get_timezone();
		int DST = libInstance.sys_get_daylight();
		long offset	= libInstance.sys_get_dstbias();
		return 0;
    }
   
   
    void listDevices(){    	
    	prepareListForCheck();
    	System.out.println("checking...please wait...");
    	int devCount = AISListUpdateAndGetCount();    	    
    	System.out.printf("AIS_List_UpdateAndGetCount()= [%d]\n", devCount);    	    	
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
   
    int addDevice(int deviceType, int deviceId){
    	String fOut = "";
    	DL_STATUS = AISListAddDeviceForCheck(deviceType, deviceId);
    	fOut = String.format("AIS_List_AddDeviceForCheck(type: %d, id: %d)> { %s }", 
    			                 deviceType, deviceId, libInstance.dl_status2str(DL_STATUS).getString(0));
    	System.out.println(fOut);
    	return DL_STATUS;
    }
    
    void ListEraseAllDevicesForCheck(){
    	System.out.println("AIS_List_EraseAllDevicesForCheck()");
    	AISListEraseAllDeviceForCheck();
    }
    
    void GetDevicesForCheck(){
    	System.out.format("AISGetDevicesForCheck() :%s",AISGetDevicesForCheck());
    }
    
    void ListAddDeviceForCheck(int deviceType, int deviceId){
    	String fOut ="";
    	DL_STATUS =  AISListAddDeviceForCheck(deviceType, deviceId);
    	fOut = String.format("AIS_List_AddDeviceForCheck()>> deviceType: %d : deviceId: %d |DL_STATUS: %d", deviceType, deviceId, DL_STATUS);
    	System.out.println(fOut);
    }
    
   
    
	void AISGetListInformation(){    	    			
		String s = "";
		int devCount = AISListUpdateAndGetCount();
    	if (devCount <= 0) {return;}
    	else
    	{    		
	    	HND_LIST.clear();
	    	S_DEVICE dev = new S_DEVICE();    	    	
	    	for (int i = 0;i<devCount;i++)
	    	{
	    		/*
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
	    		*/
	    		
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
	    		dev.hnd = hnd.getValue(); 
	            dev.devSerial = Integer.parseInt(devSerial.getValue().getString(0)); 
	            dev.devType = devType.getValue();
	            dev.devID = devID.getValue();
	            dev.devFW_VER = devFW_VER.getValue();
	            dev.devCommSpeed = devCommSpeed.getValue();
	            dev.devFTDI_Serial = devFTDI_Serial;                                 
	            dev.devOpened = devOpened.getValue();
	            dev.devStatus = devStatus.getValue();
	            dev.systemStatus = devStatus.getValue();
	           
	            System.out.println(myFormats.get(0) + "\n" + myFormats.get(1) + "\n" + myFormats.get(0));                                          
	    		try {  
	    			    			
					s += String.format(myFormats.get(2),
							         dev.idx,
							         dev.hnd.getInt(0),
							         dev.devSerial,
							         dev.devType,
							         dev.devID,
							         dev.devFW_VER,
							         dev.devCommSpeed,
							         new String(devFTDI_Serial,"UTF-8"),
							         dev.devOpened,
							         dev.devStatus,
							         dev.systemStatus);
				   			         
			       System.out.println(s += myFormats.get(0));
					
					
				} catch (UnsupportedEncodingException e) {				
					e.printStackTrace();
				}
	    	}    		    	
    	}
    	
}
               
    void init(){
    	MyFormats();
    	listDevices();    	
    	AISGetListInformation();
    	System.out.println(ShowMeni());
    }
    
    
	public static void main(String[] args) {			   	  	 
       MyAisWrapper init = new MyAisWrapper();       
       System.out.println(init.AISGetLibraryVersionStr());
       init.init();
       while (true) 
       {
    	   if (!init.MeniLoop()){
    		   break;    		   
    	   }
       }
       System.exit(0);      
       return;
	}

}
