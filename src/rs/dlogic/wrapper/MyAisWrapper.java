/**
 * 
 */
package rs.dlogic.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import rs.dlogic.wrapper.AisWrapper;



/**
 * @author Vladan
 *
 */




public class MyAisWrapper extends AisWrapper {	
	
		
	int DL_STATUS;	
    //static AisLibrary libInstance;
    
    
    PointerByReference hnd = new PointerByReference();
	PointerByReference devSerial = new PointerByReference();
	IntByReference devType = new IntByReference();
	IntByReference devID = new IntByReference();
	IntByReference devFW_VER = new IntByReference();
	IntByReference devCommSpeed = new IntByReference();
	PointerByReference devFTDI_Serial = new PointerByReference();
	IntByReference devOpened = new IntByReference();
	IntByReference devStatus = new IntByReference();
	IntByReference systemStatus = new IntByReference();
        
        
    MyAisWrapper(){
    	libInstance = AisLibrary.aisReaders;
    }

        
    private static String PASS = CONSTANTS.AlfaNumeric.PASS.strValue();
    private List<String>myFormats = new ArrayList<String>();
    private ArrayList<Pointer>HND_LIST = new ArrayList<Pointer>();
	
	private Map<String, Boolean> Lights = new HashMap<>();
	S_DEVICE dev = new S_DEVICE();	
	Rte rte = new Rte();
	
	
	
    //***************************************************************************
   
    
   void MyFormats(){
	   //format_grid
	   myFormats.add(0, "---------------------------------------------------------------------------------------------------------------------" );
	   myFormats.add(1, "| indx|  Reader HANDLE   | SerialNm | Type h/d | ID  | FW   | speed   | FTDI: sn   | opened | DevStatus | SysStatus |" );
	   myFormats.add(2,"| %3d | %016X | %s | %7d  | %2d  | %d  | %7d | %10s | %5d  | %8d  | %9d |\n");
	   
   }
   
   private String ShowMeni(){
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
   
   public void ActiveDevice(S_DEVICE dev, int index)  {
	try {
		dev.hnd = HND_LIST.get(index);		  		   		  
		dev.idx = HND_LIST.indexOf(dev.hnd) + 1;
		System.out.printf(" dev [%d] | hnd= 0x%X  \n" , dev.idx, dev.hnd.getInt(0));		
		
	} catch (IndexOutOfBoundsException | NullPointerException e) {		
		System.out.format("Exception: %s",e.toString());
	}
	   
   }
   
   @SuppressWarnings("resource")
   private Boolean MeniLoop(){
	   Scanner terminal;
	   terminal = new Scanner(System.in);	   
	   String mChar = terminal.nextLine();	 	 
	   int index = 0;
	   if (Character.isDigit(mChar.trim().charAt(0))){		  
		   index = Integer.parseInt(mChar)-1;
		   ActiveDevice(dev, index);
	   }else{
		   ActiveDevice(dev, 0); 
	   }
	   	   
	   if (mChar.contains("x")){
		   System.out.println("APPLICATION EXIT NOW !\n");
		   terminal.close();
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
		case "i":
		     AisGetVersion(dev);
		     AisGetTime(dev);
		     System.out.println(libInstance.sys_get_timezone_info().getString(0));
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
		case "T":
			AisSetTime(dev);
			break;
		case "b": //Black_list read
			AisBlackListRead(dev);
			break;
		case "B":
			AisBlackListWrite(dev);
			break;
		case "w": //White list read
			AisWhiteListRead(dev);
			break;
		case "W":
			AisWhiteListWrite(dev);
			break;
		case "L":
			AisTestLights(dev);
			break;
		case "Q": //Edit device list for checking
			AisEditDeviceListForChecking();
			break;
		case "s" :
			AisConfigFileRead(dev);
			break;
		case "g":
			AisGetIOState(dev);
			break;
		case "y":
			AisRelayToogle(dev);
			break;
		case "G":
			AisLockOpen(dev);
			break;
		case "r":
			AisRTEListen(dev, CONSTANTS.Numeric.SECONDS.value());
			break;
		case "l":
			System.out.println(AisLogGet(dev));
			break;
		case "n":
		    System.out.println(AisLogByIndex(dev));
		    break;
		case "N":
			System.out.println(AisLogByTime(dev));
			break;
		case "u":
			UnreadLog ulog = new UnreadLog(dev);
			ulog.AisUnreadLog();
			break;
		case "E":
			rv = EELock(dev);
			System.out.println(rv.ret_string);
			break;
		case "e":
			rv = EEUnlock(dev);
			System.out.println(rv.ret_string);
			break;
		}		  
	  return true;
   }
   
    @SuppressWarnings("resource")
    void AisConfigFileRead(S_DEVICE dev){    	
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
    	String localTime = formatter.format(new Date());     	
    	String fileName = String.format("BaseHD-%s-ID%d-%s", dev.devSerial, dev.devID, localTime);    	
    	String fName="";
    	String print = "Read configuration from the device - to the file"
    			     + String.format("%nConfig file - enter for default [%s] : ",fileName);
    	System.out.println(print);    	
		Scanner scan = new Scanner(System.in);
    	String sscan = scan.nextLine();    	
    	if (sscan.length()!= 0){    		
    		fName = sscan.trim();
    	}else {
    		fName = fileName;
    	}
    	fileName = "";
    	fileName = fName + ".config";    	
    	System.out.printf("AIS_Config_Read(file: %s)%n", fileName);    	
    	rv = AISConfigFileRead(dev, fileName, PASS);
    	System.out.printf("AIS_Config_Read():%s", libInstance.dl_status2str(rv.dl_status).getString(0));
        
    }
    
   
    
    void AisGetIOState(S_DEVICE dev){    	
    	rv = AISGetIOState(dev);
    	System.out.printf("IO STATE= intercom= %d, door= %d, relay_state= %d : %s%n",
    			         rv.intercom, rv.door, rv.relay_state, libInstance.dl_status2str(rv.dl_status).getString(0));
    	
    }
    
   
    
    void AisRelayToogle(S_DEVICE dev){    	
    	rv = AISRelayToogle(dev);
    	System.out.printf("AIS_RelayStateSet(RELAY= %d) : %s%n", rv.relay_state, 
    			         libInstance.dl_status2str(rv.dl_status).getString(0));
    }
    
    
    
    void AisLockOpen(S_DEVICE dev){    	    	
    	int pulseDuration = CONSTANTS.Numeric.PULSE_DURATION.value(); 
    	rv = AISLockOpen(dev, pulseDuration);
    	System.out.printf("AIS_LockOpen(pulse_duration= %d ms) : %s%n", 
    			         pulseDuration, libInstance.dl_status2str(rv.dl_status).getString(0));
    }
    
    
    
    String AisOpen(){       
 	   StringBuilder out = new StringBuilder();  	    	  
 	   for (Pointer hnd : HND_LIST){		   		   		  
 		   DL_STATUS = libInstance.AIS_Open(hnd); 		  
 		   out.append(String.format("AIS_Open(0x%X):%s%n", hnd.getInt(0), libInstance.dl_status2str(DL_STATUS).getString(0)));		   
 	   } 	   
 	   return out.toString(); 
    }
    
    String AisClose(){
       StringBuilder out = new StringBuilder(); 
 	   for (Pointer hnd : HND_LIST){
 		   DL_STATUS = libInstance.AIS_Close(hnd);		   
 		   out.append(String.format("AIS_Close(0x%X):%s%n", hnd.getInt(0), libInstance.dl_status2str(DL_STATUS).getString(0))); 				                       	  
 	   }
 	   return out.toString();
    }
   

    void AisGetTime(S_DEVICE dev){
    	String fOut;    	    	    	    	
    	rv = AISGetTime(dev.hnd);    	    	    
    	fOut = String.format("AIS_GetTime(dev[%d] hnd=0x%X):%s > (currentTime= %d | tz= %d | dst= %d | offset= %d): %s%n",
    			              dev.idx, dev.hnd.getInt(0), libInstance.dl_status2str(rv.dl_status).getString(0), rv.currentTime, rv.timezone, rv.DST, rv.offset,
    			               new Date(rv.currentTime).toString() 
    			               );    	    	   
    	System.out.println(fOut);    	    
    }
    
  
    void AisSetTime(S_DEVICE dev){
    	String fOut;    	    	    	    	
    	rv = AISSetTime(dev.hnd, PASS);    	    	    
    	fOut = String.format("%nAIS_SetTime(dev[%d] hnd=0x%X) %s > (currentTime= %d | tz= %d | dst= %d | offset= %d): %s%n",
    			               dev.idx, dev.hnd.getInt(0), libInstance.dl_status2str(rv.dl_status).getString(0), rv.currentTime, rv.timezone, rv.DST, rv.offset,
    			               new Date(rv.currentTime).toString()
    			               );    	    	   
    	System.out.println(fOut); 
    }
   
    
    
    void AisBlackListRead(S_DEVICE dev){
    	String fOut;    	    	    	    	
    	rv = AISBlackListRead(dev, PASS);    	
    	fOut = String.format("%nAIS_Blacklist_Read() > black_list(size= %d | %s) > %s%n" ,
    			             rv.listSize, rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0) );
    	System.out.println(fOut);
    }
   
    @SuppressWarnings("resource")
    void AisBlackListWrite(S_DEVICE dev){
    	String fOut;    	    	    	    
		Scanner inputBL = new Scanner(System.in);
    	String sBuffer =  "=- Write Black List -=\n"
    					+ "Try to write black-list decimal numbers (delimited with anything)\n"
    			        + "eg. 2, 102 250;11\n";
    	System.out.print(sBuffer + "\nEnter black list:");    	    	   
    	rv = AISBlackListWrite(dev, PASS, inputBL.nextLine());
    	fOut = String.format("%nAIS_Blacklist_Write():black_list= %s > %s%n" , rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0));
        System.out.println(fOut); 
        inputBL.close();
    }
    
  
    void AisWhiteListRead(S_DEVICE dev) 
    {
    	String fOut;    	    	    	   
    	rv = AISWhiteListRead(dev, PASS);    	
    	fOut = String.format("%nAIS_Whitelist_Read() > white_list(size= %d | %s) > %s%n" ,
    			             rv.listSize, rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0) );
    	System.out.println(fOut); 	
    }
    
    @SuppressWarnings ("resource")
    void AisWhiteListWrite(S_DEVICE dev){
    	String fOut;    	    	    	
    	Scanner inputBL = new Scanner(System.in);
    	String sBuffer =  "=- Write White List -=\n"
    					+ "Enter white-list UIDs (in HEX format delimited with '.' or ':' or not)\n"
    			        + "Each UID separate by ',' or space eg. 37:0C:96:69,C2.66.EF.95 01234567\n";
    	System.out.print(sBuffer + "\nEnter white list:");    	    	   
    	rv = AISWhiteListWrite(dev, PASS, inputBL.nextLine());
    	fOut = String.format("%nAIS_Whitelist_Write():white_list= %s > %s%n" , rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0));
        System.out.println(fOut);        
    }
     
    
    private void CleanMapLights(){    	
   	    Lights.put("green_master", false);
   	    Lights.put("red_master", false);
   	    Lights.put("green_slave", false);
   	    Lights.put("red_slave", false);     	  
    }
    
    
    
	@SuppressWarnings("resource")
	void AisTestLights(S_DEVICE dev){
    	String lightMeni = "\tg : green master | r : red master | G : green slave | R : red slave  || x : exit \n"
    			         + "\t-----------------";
    	System.out.println(lightMeni);     	
    	String fOut;    	
    	Scanner s;
    	s = new Scanner(System.in);
   	    Boolean print = false;   	   
   	    CleanMapLights();   	    
    	while (true){
    		     		 
    		 String choise = s.nextLine();    		     		     		 
    		 if (choise.contains("g")){    			 
    			 Lights.replace("green_master", true);
    			 print = true;    			 
    			}
    		 if (choise.contains("r")){    			 
    			 Lights.replace("red_master", true);
    			 print = true;   			 
    		 }
    		 if (choise.contains("G")){    			 
    			 Lights.replace("green_slave", true);
    			 print = true;     			 }
    		 if (choise.contains("R")){
    			 Lights.replace("red_slave", true);
    			 print = true;     			 }
    		 if (choise.contains("x")) {
    			 break;    			    			    		
    			 } 
    		
    		 if (print){    			 
    			int greenMaster = Lights.get("green_master") ? 1:0;
    			int redMaster = Lights.get("red_master") ? 1:0;
    			int greenSlave = Lights.get("green_slave") ? 1:0;
    			int redSlave = Lights.get("red_slave") ? 1:0;
    			rv = AISTestLights(dev, greenMaster, redMaster, greenSlave, redSlave);
    			fOut = String.format("%nAIS_LightControl(master:green= %d | master:red= %d || slave:green= %d | slave:sred= %d) > %s", 
    					              greenMaster, redMaster, greenSlave, redSlave, libInstance.dl_status2str(rv.dl_status).getString(0));
    			System.out.println(fOut);
    			CleanMapLights();
    			print = false;
    		}    			
    	}     	   	   
   }

	
	
//Q - EditDeviceListForChecking
	String PrintAvailableDevices(){		
		PointerByReference devName =  new PointerByReference();
		PointerByReference devDescript = new PointerByReference();
		IntByReference hwType = new IntByReference(0);
		IntByReference speed = new IntByReference(0); 
        IntByReference rteTest = new IntByReference(0);
        IntByReference isHalfDuplex = new IntByReference(0);
        IntByReference isAloneOnTheBus = new IntByReference(0);
                
		int status;
		int maxDev = E_KNOWN_DEVICE_TYPES.DL_AIS_SYSTEM_TYPES_COUNT.value();
		String grid_0 = "-------------------------------------------------------------------\n",
			   grid_1 = "------------+-----------------+------------------------------------\n";	
		
	    String header = grid_0 + "Look at ais_readers_list.h for Device enumeration\n"
	                   + String.format("Known devices ( supported by %s )%n", AISGetLibraryVersionStr())
	                   + grid_0 + " Dev.type   |   Short  name   | Long name\n"
	                   + grid_1;
	    
	    String print = "";
	    System.out.println(header);
	    
	    for (int i = 1;i<maxDev;i++){
	    	status = libInstance.dbg_device_type(i, devName, devDescript,
	    			  hwType, speed, rteTest, isHalfDuplex, isAloneOnTheBus);
	    
	        if (status != 0){
	        	System.out.println("NOT SUPORTED!");
	        	break;
	        }else {	        	
	        	print += String.format("\t %2d | %15s | %s%n", i, devName.getValue().getString(0),
	        													devDescript.getValue().getString(0)); 	        		  		        	
	        }	        	  
	    }
	    return 
	    		print + grid_0;
	
	}
    
	String ShowActualList(){
		PointerByReference devName =  new PointerByReference();
		PointerByReference devDescript = new PointerByReference();
		IntByReference hwType = new IntByReference(0);
		IntByReference speed = new IntByReference(0); 
        IntByReference rteTest = new IntByReference(0);
        IntByReference isHalfDuplex = new IntByReference(0);
        IntByReference isAloneOnTheBus = new IntByReference(0);
        StringBuilder print = new StringBuilder();
		
        String grid_0 = "-------------------------------------------------------------------\n",
			   grid_1 = "------------+------+--------------------+--------------------------\n";
				
		String header = "Show actual list for checking:\n"
				      + grid_0
				      + " Dev.type   |  ID  |      Short  name   | Long name\n"
				      + grid_1;
	   
		
		String getDev = AISGetDevicesForCheck();
	    System.out.println(header);
	    
		for (String item : getDev.split("\n")) {
			String[] devTypes = item.split(":");
			int deviceType = Integer.parseInt(devTypes[0]);
			libInstance.dbg_device_type(deviceType, devName, devDescript, hwType, speed, rteTest, isHalfDuplex, isAloneOnTheBus);
			print.append(String.format("\t%2s  | \t%2s | %18s | %s%n", devTypes[0], devTypes[1],
					devName.getValue().getString(0), devDescript.getValue().getString(0)));

		}

		return  
				print.toString() + grid_1;     
	}

	@SuppressWarnings ("resource")
	int [] DevInput(){		
		int maxDev = E_KNOWN_DEVICE_TYPES.DL_AIS_SYSTEM_TYPES_COUNT.value();
		System.out.println("Enter device type and then enter device BUS ID for check");
		int[]devInput = new int[2];
		Scanner in;	
		in = new Scanner(System.in);
		while (true){			
			System.out.printf("Enter device type (1,2, ... , %d)('x' for exit !)   : " , maxDev-1);
			String scan = in.next();
			if (scan.contains("x")){
				devInput[0] = 0; //deviceType				
				break;
			}			
			System.out.print("Enter device bus ID (if full duplex then enter 0)   :  ");
			devInput[0] = Integer.parseInt(scan);
			devInput[1] = in.nextInt(); //deviceID	
			
			System.out.print("\nAgain (Y/N) ?");			
			scan = in.next();
			if (scan.contains("N") || scan.contains("n")){
				break;
				}
		}
		//in.close();
		return devInput;		
	}
	
	String ShowResult(String funName,int devType, int devId){
		int status = 0;
		switch (funName){
		case "AIS_List_AddDeviceForCheck()":
			status = libInstance.AIS_List_AddDeviceForCheck(devType, devId);
			break;
		case "AIS_List_EraseDeviceForCheck()":
			status = libInstance.AIS_List_EraseDeviceForCheck(devType, devId);
			break;
		}
		return String.format("%s(type: %d, id: %d)> %s %n", funName, devType, devId,
				libInstance.dl_status2str(status).getString(0)) + "Finish list edit.\n"
				+ String.format("AFTER UPDATE CYCLE %n%s", AISGetDevicesForCheck());
		
	}
	
	@SuppressWarnings("resource")
	void AisEditDeviceListForChecking(){
		Scanner scan;		
		int deviceType,deviceID;
		String meni = "\n\t1 : show known device types"
                    + "\n\t2 : show actual list for checking" 
                    + "\n\t3 : clear list for checking"
                    + "\n\t4 : add device for check"
                    + "\n\t5 : erase device from checking list"
                    + "\n\t------------------------------------"
                    + "\n\tm : print meni"
                    + "\n\tx : Exit";
		
		System.out.println(meni);
		scan = new Scanner(System.in);
		while (true){			
			String choise = scan.nextLine();
			if (choise.contains("x")){
				break;
			   }
			if (choise.contains("m")){
				System.out.println(meni);
				}
			if (choise.contains("1")){
				System.out.println(PrintAvailableDevices());
				};				
			if (choise.contains("2")){
				System.out.println(ShowActualList());
				}
			if (choise.contains("3")){				
				AISListEraseAllDeviceForCheck();
				System.out.println("Clear list for checking !");
			}
			if (choise.contains("4")){					
				deviceType = DevInput()[0];
				deviceID = DevInput()[1];
				System.out.println(ShowResult("AIS_List_AddDeviceForCheck()", deviceType, deviceID));				
			}
			if (choise.contains("5")){					
				deviceType = DevInput()[0];
				deviceID = DevInput()[1];
				System.out.println(ShowResult("AIS_List_EraseDeviceForCheck()", deviceType, deviceID));				
			}	
		}				
	}

 
	public void AisRTEListen(S_DEVICE dev, int maxSec){				
		long stopTime = new Date(System.currentTimeMillis() + ((maxSec*90)*10)).getTime();			
		while (new Date(System.currentTimeMillis()).getTime()<= stopTime){			
			for (Pointer hnd : HND_LIST){
				dev.hnd = hnd;
				rte.AISMainLoop(dev);
			}
		}
		System.out.println("END RTE listen...");
	}
	
	public String AisLogGet(S_DEVICE dev){				
		byte[] pass = CONSTANTS.AlfaNumeric.PASS.strValue().getBytes();
		dev.status = libInstance.AIS_GetLog(dev.hnd, pass);
		
		if (dev.status !=0){
			return libInstance.dl_status2str(dev.status).getString(0);
			
		}
		rte.DoCmd(dev);
		rv = rte.AISPrintLog(dev);	
		return rv.ret_string;		
	}
	
	public void AisGetVersion(S_DEVICE dev){		
		rv = AISGetVersion(dev);
		System.out.format("AIS_GetVersion() |>>hw = %d | fw = %d%n", rv.hardwareType, rv.firmwareVersion);		
	}
	
	
	@SuppressWarnings("resource")
	public String AisLogByIndex(S_DEVICE dev){				
		Scanner inIndex = new Scanner(System.in);
		System.out.println("#=- Print log by index -=#");
		System.out.print("Enter index start: ");				
		int startIndex = inIndex.nextInt();
		System.out.print("Enter index end  : ");
		int endIndex = inIndex.nextInt();				
		byte[] pass = CONSTANTS.AlfaNumeric.PASS.strValue().getBytes();
		dev.status = libInstance.AIS_GetLogByIndex(dev.hnd, pass, startIndex, endIndex);
		String fOut = String.format("AIS_GetLogByIndex:(pass: %s [ %d - %d ] >> %s)%n", new String(pass), startIndex, endIndex, libInstance.dl_status2str(dev.status).getString(0));
		
		if (dev.status !=0){
			return fOut;
		}
		rte.DoCmd(dev);
		rv = rte.AISPrintLog(dev);		
		return  fOut + rv ;			  			
	}
	
	@SuppressWarnings("resource")
	public String AisLogByTime(S_DEVICE dev){
		Scanner inIndex = new Scanner(System.in);
		System.out.println("#=- Read LOG by Time (time-stamp) range -=#");
		System.out.print("Enter time-stamp start: ");				
		int startTime = inIndex.nextInt();
		System.out.print("Enter time-stamp end  : ");
		int endTime = inIndex.nextInt();				
		byte[] pass = CONSTANTS.AlfaNumeric.PASS.strValue().getBytes();
		dev.status = libInstance.AIS_GetLogByTime(dev.hnd, pass, startTime, endTime);
		String fOut = String.format("AIS_GetLogByTime:(pass: %s [ %d - %d ] >> %s)%n", new String(pass), startTime, endTime, libInstance.dl_status2str(dev.status).getString(0));
		
		if (dev.status !=0){
			return fOut;
		}
		rte.DoCmd(dev);
		rv = rte.AISPrintLog(dev);		
		return  fOut + rv ;			  			
	}
	
  public RetValues EELock(S_DEVICE dev){
	  byte[]PASS = CONSTANTS.AlfaNumeric.PASS.strValue().getBytes();
	  //String PASS = CONSTANTS.AlfaNumeric.PASS.strValue();
	  dev.status = libInstance.AIS_EE_WriteProtect(dev.hnd, PASS);
	  rv.ret_string = String.format("EEPROM Lock - AIS_EE_WriteProtect() %s", libInstance.dl_status2str(dev.status).getString(0));
	  return rv;
  }
	
  public RetValues EEUnlock(S_DEVICE dev){
	  byte[]PASS = CONSTANTS.AlfaNumeric.PASS.strValue().getBytes();
	  //String PASS = CONSTANTS.AlfaNumeric.PASS.strValue();
	  dev.status = libInstance.AIS_EE_WriteUnProtect(dev.hnd, PASS);
	  rv.ret_string = String.format("EEPROM Lock - AIS_EE_WriteUnProtect() %s", libInstance.dl_status2str(dev.status).getString(0));
	  return rv;
	  
  }
  

 //************************************************************************
	void listDevices(){    	
    	prepareListForCheck();
    	System.out.println("checking...please wait...");
    	int devCount = AISListUpdateAndGetCount();    	    
    	System.out.printf("AIS_List_UpdateAndGetCount()= [%d]\n", devCount); 
    	if (devCount >0){
    		AISGetListInformation();
    	}else
    		System.out.println("NO DEVICE FOUND!");
    }
    
    
    
    private Boolean loadListFromFile(){
    	String fileName = "readers.ini";
    	String devTypeStr;
    	File f = new File(fileName);
    	int addedDevType = 0;
    	int devId;
    	
    	IntByReference devTypeEnum = new IntByReference();
        if (!f.exists()){
        	System.out.printf("File <%s> not found. %n",fileName);
        	return false;
        }
        try {        	
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) !=null)
			{				
				if (line.startsWith("#")) 
					continue;							    
					String []linePart = line.trim().split(":");
					if (linePart.length <= 1) 
						continue;
					   devTypeStr = linePart[0];
					   devId = Integer.parseInt(linePart[1]);					 
					   DL_STATUS = libInstance.device_type_str2enum(devTypeStr, devTypeEnum);
					   if (addDevice(devTypeEnum.getValue(), devId) == 0){
						  addedDevType ++;
			}
		  }	
		 reader.close();
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
    	  System.out.printf("     %20s (enum= %d) on ID %d%n", dev_type_str.getValue().getString(0), deviceType, deviceId);     	 
    	}    	    	    
    }
    
    
    
    void prepareListForCheck(){    	
    	System.out.println("AIS_List_GetDevicesForCheck() BEFORE / DLL STARTUP");
    	listForCheckPrint();
    	AISListEraseAllDeviceForCheck();
    	if (!loadListFromFile()){
    		System.out.println("Tester try to connect with a Base HD device on any/unkown ID");
    	    addDevice(E_KNOWN_DEVICE_TYPES.DL_AIS_BASE_HD_SDK.value(),0);
    	
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
		StringBuilder result = new StringBuilder();
		String res;
		int devCount = AISListUpdateAndGetCount();
    	if (devCount <= 0) {
    		return;
    		}
    	else
    	{    		
	    	HND_LIST.clear();	    
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
	    		if (DL_STATUS !=0) 
	    			return;    		
	    		HND_LIST.add(hnd.getValue());    		
	    		AisOpen();
	    		dev.idx = 1;
	    		dev.hnd = hnd.getValue(); 	            
	            dev.devSerial = devSerial.getValue().getString(0); 	            	            	            	            	            	            	            
	            dev.devType = devType.getValue();
	            dev.devID = devID.getValue();
	            dev.devFW_VER = devFW_VER.getValue();
	            dev.devCommSpeed = devCommSpeed.getValue();
	            dev.devFTDI_Serial = devFTDI_Serial.getValue().getString(0);                                 
	            dev.devOpened = devOpened.getValue();
	            dev.devStatus = devStatus.getValue();
	            dev.systemStatus = devStatus.getValue();
	           
	            System.out.println(myFormats.get(0) + "\n" + myFormats.get(1) + "\n" + myFormats.get(0));                                          
	    		try {  
	    			    			
					result.append(String.format(myFormats.get(2),
							         dev.idx,
							         dev.hnd.getInt(0),
							         dev.devSerial,
							         dev.devType,
							         dev.devID,
							         dev.devFW_VER,
							         dev.devCommSpeed,							      
							         dev.devFTDI_Serial,
							         dev.devOpened,
							         dev.devStatus,
							         dev.systemStatus)
							    );
				   res = result.toString();			         
			       System.out.println(res += myFormats.get(0));
					
					
				} catch (Exception e) {				
					e.printStackTrace();
				}
	    	}    		    	
    	}
    	
}
               
    void init(){
    	MyFormats();
    	listDevices();    	    	
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
