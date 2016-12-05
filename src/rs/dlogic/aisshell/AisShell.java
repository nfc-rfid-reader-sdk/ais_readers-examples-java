/**
 * 
 */
package rs.dlogic.aisshell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import rs.dlogic.aiswrapper.AisWrapper;
import rs.dlogic.aiswrapper.E_KNOWN_DEVICE_TYPES;
import rs.dlogic.aiswrapper.AisWrapper.RetValues;
import rs.dlogic.aiswrapper.AisWrapper.S_DEVICE;



/**
 * @author Vladan
 *
 */


public class AisShell extends AisWrapper {	
	
	S_PROGRESS progress = new S_PROGRESS();
    S_DEVICE dev = new S_DEVICE();
    RetValues rv = new RetValues();
    
    AisShell(){
    	libInstance = AisLibrary.aisReaders;
    }
  
	private Map<String, Boolean> Lights = new HashMap<>();
	private List<String>myFormats = new ArrayList<String>();
    private ArrayList<Pointer>HND_LIST = new ArrayList<Pointer>();
    
    private PointerByReference hnd = new PointerByReference();
	private PointerByReference devSerial = new PointerByReference();
	private IntByReference devType = new IntByReference();
	private IntByReference devID = new IntByReference();
	private IntByReference devFW_VER = new IntByReference();
	private IntByReference devCommSpeed = new IntByReference();
	private PointerByReference devFTDI_Serial = new PointerByReference();
	private IntByReference devOpened = new IntByReference();
	private IntByReference devStatus = new IntByReference();
	private IntByReference systemStatus = new IntByReference();
    
	
	private String PASS         = "1111";
	private int SECONDS         = 10;   	
	private int PULSE_DURATION  = 2000;
	private int RECORDS_TO_ACK  = 1;
	
	private static final String GREEN_MASTER = "green_master";
	private static final String RED_MASTER = "red_master";
	private static final String GREEN_SLAVE = "green_slave";
	private static final String RED_SLAVE = "red_slave";
   
    
   private void MyFormats(){	   
	   myFormats.add(0, "---------------------------------------------------------------------------------------------------------------------" );
	   myFormats.add(1, "| indx|  Reader HANDLE   | SerialNm | Type h/d | ID  | FW   | speed   | FTDI: sn   | opened | DevStatus | SysStatus |" );
	   myFormats.add(2,"| %3d | %016X | %s | %7d  | %2d  | %d  | %7d | %10s | %5d  | %8d  | %9d |\n");	   
   }
   
   private String rteFormat = "| %5d | %32s | %5d | %7d | %5d | %24s | %10d | %s | ";
	
   private String[] rteListHeader = {"---------------------------------------------------------------------------------------------------------------------------------------------",
  	           "| Idx   |              action              | RD ID | Card ID | JobNr |    NFC [length] : UID    | Time-stamp |       Date - Time            |"
  	};  

  
   
   String rteHead = rteListHeader[0] + "\n"
                  + rteListHeader[1] + "\n"
                  + rteListHeader[0] + "\n";
   
   
   
   private String ShowMeni(){
	   return        "\n--------------------------\n" 			   		 
			         + "q : List devices\t\t\to : Open device\t\t\t\tc : Close device "
			         + "\nd : Get devices count\t\t\tt : Get time\t\t\t\tT : Set time"
			         + "\nr : Real Time Events\t\t\tp : Set application password\t\tP : Change device password"
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
        
	  
   }
   
   private void ActiveDevice(AisWrapper.S_DEVICE dev, int index)  {
	try {
		dev.hnd = HND_LIST.get(index);		  		   		  
		dev.idx = HND_LIST.indexOf(dev.hnd) + 1;
		System.out.printf(" dev [%d] | hnd= 0x%X  %n" , dev.idx, dev.hnd.getInt(0));		
		
	} catch (IndexOutOfBoundsException | NullPointerException e) {		
		System.out.format("Exception: %s",e.toString());
	}
	   
   }
   
   @SuppressWarnings("resource")
   private Boolean MeniLoop(){
	   Scanner terminal;
	   terminal = new Scanner(System.in);	   
	   String mChar = terminal.nextLine();	 	 
	   int index;
	   if (Character.isDigit(mChar.trim().charAt(0))){		  
		   index = Integer.parseInt(mChar)-1;
		   ActiveDevice(dev, index);
	   }else{
		   ActiveDevice(dev, 0); 
	   }
	   	   
	   if (mChar.contains("x")){
		   System.out.println("\nAPPLICATION EXIT NOW !\n");
		   terminal.close();
		   return false;
	   }	   
	   switch (mChar) 
	   {
		case "o":
			System.out.println(Open());
			break;
		case "q":
			GetListInformation();
			break;
		case "i":
		     GetVersion(dev);
		     GetTime(dev);
		     System.out.println(libInstance.sys_get_timezone_info().getString(0));
		     break;
		case "c": 
			System.out.println(Close());
			break;
		case "m":
			System.out.print(ShowMeni());
			break;
		case "d": //Get devices count
			System.out.printf("DEVICE COUNT: %d%n" ,AisWrappListUpdateAndGetCount());
			break;
		case "t":
			GetTime(dev);						
			break;
		case "T":
			SetTime(dev);
			break;
		case "b": //Black_list read
			BlackListRead(dev);
			break;
		case "B":
			BlackListWrite(dev);
			break;
		case "w": //White list read
			WhiteListRead(dev);
			break;
		case "W":
			WhiteListWrite(dev);
			break;
		case "L":
			TestLights(dev);
			break;
		case "Q": 
			EditDeviceListForChecking();
			break;
		case "s" :
			ConfigFileRead(dev);
			break;
		case "S":
			ConfigFileWrite(dev);
			break;
		case "g":
			GetIOState(dev);
			break;
		case "y":
			RelayToogle(dev);
			break;
		case "G":
			LockOpen(dev);
			break;
		case "r":
			RTEListen(dev, SECONDS);
			break;
		case "l":
			System.out.println(LogGet(dev));
			break;
		case "n":
		    System.out.println(LogByIndex(dev));
		    break;
		case "N":
			System.out.println(LogByTime(dev));
			break;
		case "u":			
			UnreadLog();
			break;
		case "E":
			rv = EELock(dev);
			System.out.println(rv.ret_string);
			break;
		case "e":
			rv = EEUnlock(dev);
			System.out.println(rv.ret_string);			
			break;
		case "P":
			ChangeDevicePass(dev);
			break;
		case "p":
			SetApplicationPass(dev);
			break;
		default:
			System.out.print(ShowMeni());
			break;				
		}		  
	  return true;
   }
   
    @SuppressWarnings("resource")
    public void ConfigFileRead(S_DEVICE dev){    	
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
    	String localTime = formatter.format(new Date());     	
    	String fileName = String.format("BaseHD-%s-ID%d-%s", dev.devSerial, dev.devID, localTime);    	
    	String fName;
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
    	fileName = fName + ".config";    	
    	System.out.printf("AIS_Config_Read(file: %s)%n", fileName);    	
    	rv = AisWrappConfigFileRead(dev, fileName, PASS);
    	System.out.printf("AIS_Config_Read():%s", libInstance.dl_status2str(rv.dl_status).getString(0));
        
    }
    
    @SuppressWarnings("resource")
    public void ConfigFileWrite(S_DEVICE dev){
    	String fileName = "BaseHD-xxx.config";
    	String fName;
    	System.out.printf("%nStore configuration from file to the device%nConfig file - enter for default [%s] : ", fileName);
    	Scanner scan = new Scanner(System.in);
    	String sscan = scan.nextLine();    	
    	if (sscan.length()!= 0){    		
    		fName = sscan.trim();
    	}else {
    		fName = fileName;
    	}
    	fileName = fName;
    	System.out.printf("AIS_Config_Send(file: %s)%n", fileName);
    	rv = AisWrappConfigFileWrite(dev, fileName);
    	System.out.printf("AIS_Config_Send():%s", libInstance.dl_status2str(rv.dl_status).getString(0));
    }
    
    
    
    
    public void GetIOState(S_DEVICE dev){    	
    	rv = AisWrappGetIOState(dev);
    	System.out.printf("IO STATE= intercom= %d, door= %d, relay_state= %d : %s%n",
    			         rv.intercom, rv.door, rv.relay_state, libInstance.dl_status2str(rv.dl_status).getString(0));
    	
    }
    
    public void RelayToogle(S_DEVICE dev){    	
    	rv = AisWrappRelayToogle(dev);
    	System.out.printf("AIS_RelayStateSet(RELAY= %d) : %s%n", rv.relay_state, 
    			         libInstance.dl_status2str(rv.dl_status).getString(0));
    }
    
    
    
    public void LockOpen(S_DEVICE dev){    	    	
    	int pulseDuration = PULSE_DURATION; 
    	rv = AisWrappLockOpen(dev, pulseDuration);
    	System.out.printf("AIS_LockOpen(pulse_duration= %d ms) : %s%n", 
    			         pulseDuration, libInstance.dl_status2str(rv.dl_status).getString(0));
    }
    
    
    
    public String Open(){
       int dlstatus; 
 	   StringBuilder out = new StringBuilder();  	    	  
 	   for (Pointer hnd : HND_LIST){		   		   		  
 		   dlstatus = libInstance.AIS_Open(hnd); 		  
 		   out.append(String.format("AIS_Open(0x%X):%s%n", hnd.getInt(0), libInstance.dl_status2str(dlstatus).getString(0)));		   
 	   } 	   
 	   return out.toString(); 
    }
    
    String Close(){
       int dlstatus; 	
       StringBuilder out = new StringBuilder(); 
 	   for (Pointer hnd : HND_LIST){
 		   dlstatus = libInstance.AIS_Close(hnd);		   
 		   out.append(String.format("AIS_Close(0x%X):%s%n", hnd.getInt(0), libInstance.dl_status2str(dlstatus).getString(0))); 				                       	  
 	   }
 	   return out.toString();
    }
   

    public void GetTime(S_DEVICE dev){
    	String fOut;    	    	    	    	
    	rv = AisWrappGetTime(dev.hnd);    	    	    
    	fOut = String.format("%nAIS_GetTime(dev[%d] hnd=0x%X):%s > (currentTime= %d | tz= %d | dst= %d | offset= %d): %s%n",
    			              dev.idx, dev.hnd.getInt(0), libInstance.dl_status2str(rv.dl_status).getString(0), rv.currentTime, rv.timezone, rv.DST, rv.offset,
    			               new Date(rv.currentTime).toString() 
    			               );    	    	   
    	System.out.println(fOut);    	    
    }
    
  
    void SetTime(S_DEVICE dev){
    	String fOut;    	    	    	    	
    	rv = AisWrappSetTime(dev.hnd, PASS);    	    	    
    	fOut = String.format("%nAIS_SetTime(dev[%d] hnd=0x%X) %s > (currentTime= %d | tz= %d | dst= %d | offset= %d): %s%n",
    			               dev.idx, dev.hnd.getInt(0), libInstance.dl_status2str(rv.dl_status).getString(0), rv.currentTime, rv.timezone, rv.DST, rv.offset,
    			               new Date(rv.currentTime).toString()
    			               );    	    	   
    	System.out.println(fOut); 
    }
   
    
    
    public void BlackListRead(S_DEVICE dev){
    	String fOut;    	    	    	    	
    	rv = AisWrappBlackListRead(dev, PASS);    	
    	fOut = String.format("%nAIS_Blacklist_Read() > black_list(size= %d | %s) > %s%n" ,
    			             rv.listSize, rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0) );
    	System.out.println(fOut);
    }
   
    @SuppressWarnings("resource")
    public void BlackListWrite(S_DEVICE dev){
    	String fOut;    	    	    	    
		Scanner inputBL = new Scanner(System.in);
    	String sBuffer =  "=- Write Black List -=\n"
    					+ "Try to write black-list decimal numbers (delimited with anything)\n"
    			        + "eg. 2, 102 250;11\n";
    	System.out.print(sBuffer + "\nEnter black list:");    	    	   
    	rv = AisWrappBlackListWrite(dev, PASS, inputBL.nextLine());
    	fOut = String.format("%nAIS_Blacklist_Write():black_list= %s > %s%n" , rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0));
        System.out.println(fOut); 
        
    }
    
  
    public void WhiteListRead(S_DEVICE dev) 
    {
    	String fOut;    	    	    	   
    	rv = AisWrappWhiteListRead(dev, PASS);    	
    	fOut = String.format("%nAIS_Whitelist_Read() > white_list(size= %d | %s) > %s%n" ,
    			             rv.listSize, rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0) );
    	System.out.println(fOut); 	
    }
    
    @SuppressWarnings ("resource")
    void WhiteListWrite(S_DEVICE dev){
    	String fOut;    	    	    	
    	Scanner inputBL = new Scanner(System.in);
    	String sBuffer =  "=- Write White List -=\n"
    					+ "Enter white-list UIDs (in HEX format delimited with '.' or ':' or not)\n"
    			        + "Each UID separate by ',' or space eg. 37:0C:96:69,C2.66.EF.95 01234567\n";
    	System.out.print(sBuffer + "\nEnter white list:");    	    	   
    	rv = AisWrappWhiteListWrite(dev, PASS, inputBL.nextLine());
    	fOut = String.format("%nAIS_Whitelist_Write():white_list= %s > %s%n" , rv.strList, libInstance.dl_status2str(rv.dl_status).getString(0));
        System.out.println(fOut);        
    }
     
    
    private void CleanMapLights(){    	
   	    Lights.put(GREEN_MASTER, false);
   	    Lights.put(RED_MASTER, false);
   	    Lights.put(GREEN_SLAVE, false);
   	    Lights.put(RED_SLAVE, false);     	  
    }
    
    
    
	@SuppressWarnings("resource")
	public void TestLights(S_DEVICE dev){
    	String lightMeni = "\tg : green master | r : red master | G : green slave | R : red slave  || x : exit \n"
    			         + "\t-----------------";
    	System.out.println(lightMeni);     	
    	    	
    	Scanner s;
    	s = new Scanner(System.in);
   	    Boolean print = false;   	   
   	    CleanMapLights();   	    
    	while (true){
    		     		 
    		 String choise = s.nextLine();    		     		     		 
    		 if (choise.contains("g")){    			 
    			 Lights.replace(GREEN_MASTER, true);
    			 print = true;    			 
    			}
    		 if (choise.contains("r")){    			 
    			 Lights.replace(RED_MASTER, true);
    			 print = true;   			 
    		 }
    		 if (choise.contains("G")){    			 
    			 Lights.replace(GREEN_SLAVE, true);
    			 print = true;     			 }
    		 if (choise.contains("R")){
    			 Lights.replace(RED_SLAVE, true);
    			 print = true;     			 }
    		 if (choise.contains("x")) {
    			 break;    			    			    		
    			 } 
    		
    		 if (print){    			 
    			int greenMaster = Lights.get(GREEN_MASTER) ? 1:0;
    			int redMaster = Lights.get(RED_MASTER) ? 1:0;
    			int greenSlave = Lights.get(GREEN_SLAVE) ? 1:0;
    			int redSlave = Lights.get(RED_SLAVE) ? 1:0;
    			rv = AisWrappTestLights(dev, greenMaster, redMaster, greenSlave, redSlave);
    			String fOut = String.format("%nAIS_LightControl(master:green= %d | master:red= %d || slave:green= %d | slave:sred= %d) > %s", 
    					              greenMaster, redMaster, greenSlave, redSlave, libInstance.dl_status2str(rv.dl_status).getString(0));
    			System.out.println(fOut);
    			CleanMapLights();
    			print = false;
    		}    			
    	}     	   	   
   }

	
	

	public String PrintAvailableDevices(){		
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
	                   + String.format("Known devices ( supported by %s )%n", AisWrappGetLibraryVersionStr())
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
    
	public String ShowActualList(){
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
	   
		
		String getDev = AisWrappGetDevicesForCheck();
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
	public int [] DevInput(){		
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
		return devInput;		
	}
	
	public String ShowResult(String funName,int devType, int devId){
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
				+ String.format("AFTER UPDATE CYCLE %n%s", AisWrappGetDevicesForCheck());
		
	}
	
	@SuppressWarnings("resource")
	public void EditDeviceListForChecking(){
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
				AisWrappListEraseAllDeviceForCheck();
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

 
	public void RTEListen(S_DEVICE dev, int maxSec){				
		long stopTime = new Date(System.currentTimeMillis() + ((maxSec*90)*10)).getTime();	
		System.out.printf("Wait for RTE for %ds...%n", maxSec);
		while (new Date(System.currentTimeMillis()).getTime()<= stopTime){			
			for (Pointer hnd : HND_LIST){
				dev.hnd = hnd;
				MainLoop(dev);
			}
		}
		System.out.println("END RTE listen...");
	}
	
	public String LogGet(S_DEVICE dev){				
		byte[] pass = PASS.getBytes();
		dev.status = libInstance.AIS_GetLog(dev.hnd, pass);
		if (dev.status !=0){
			return libInstance.dl_status2str(dev.status).getString(0);			
		}
		DoCmd(dev);
		rv = PrintLog(dev);	
		return rteHead
			  + rv.ret_string 
			  + rteListHeader[0]
			  + String.format("%nAIS_GetLog() %s%n",  libInstance.dl_status2str(dev.status).getString(0));		
	}
	
	public void GetVersion(S_DEVICE dev){		
		rv = AisWrappGetVersion(dev);
		System.out.format("AIS_GetVersion() |>>hw = %d | fw = %d%n", rv.hardwareType, rv.firmwareVersion);		
	}
	
	
	@SuppressWarnings("resource")
	public String LogByIndex(S_DEVICE dev){	
		byte[] pass = PASS.getBytes();
		Scanner inIndex = new Scanner(System.in);
		System.out.println("#=- Print log by index -=#");
		System.out.print("Enter index start: ");				
		int startIndex = inIndex.nextInt();
		System.out.print("Enter index end  : ");
		int endIndex = inIndex.nextInt();						
		dev.status = libInstance.AIS_GetLogByIndex(dev.hnd, pass, startIndex, endIndex);
		String fOut = String.format("%nAIS_GetLogByIndex:(pass: %s [ %d - %d ] >> %s)%n", PASS, startIndex, endIndex, libInstance.dl_status2str(dev.status).getString(0));
		
		if (dev.status !=0){
			return fOut;
		}
		DoCmd(dev);
		rv = PrintLog(dev);		
		return  rteHead
				+ rv.ret_string
				+ rteListHeader[0]
				+ fOut;		
	}
	
	@SuppressWarnings("resource")
	public String LogByTime(S_DEVICE dev){
		Scanner inIndex = new Scanner(System.in);
		System.out.println("#=- Read LOG by Time (time-stamp) range -=#");
		System.out.print("Enter time-stamp start: ");				
		int startTime = inIndex.nextInt();
		System.out.print("Enter time-stamp end  : ");
		int endTime = inIndex.nextInt();				
		byte[] pass = PASS.getBytes();
		dev.status = libInstance.AIS_GetLogByTime(dev.hnd, pass, startTime, endTime);
		String fOut = String.format("%nAIS_GetLogByTime:(pass: %s [ %d - %d ] >> %s)%n", PASS, startTime, endTime, libInstance.dl_status2str(dev.status).getString(0));
		
		if (dev.status !=0){
			return fOut;
		}
		DoCmd(dev);
		rv = PrintLog(dev);		
		return  rteHead
				+ rv.ret_string
				+ rteListHeader[0]
				+ fOut;			  			
	}
	
	/**
     * UnreadLogInfo()
     * @return read log count and rte count
     */
    private RetValues UnreadLogInfo(){
     String sLogCount = "";
     String sRteCount = "";         
     int logCount = libInstance.AIS_ReadLog_Count(dev.hnd);
     if (logCount == 0){
        sLogCount = String.format("%nAIS_ReadLog_Count() %d%n" , logCount);
     }
     int rteCount = libInstance.AIS_ReadRTE_Count(dev.hnd);
     if (rteCount == 0){
    	 sRteCount = String.format("%nAIS_RTELog_Count() %d%n" , rteCount);
     }
     rv.ret_string = sLogCount + sRteCount;
     return rv;
   }
    
    public RetValues PrintLog(S_DEVICE dev){				
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
				nfcuid.append(String.format("%02X:", nfcUid[i]));
			
			result.append(String.format(rteFormat, dev.log.index, 
		               libInstance.dbg_action2str(dev.log.action).getString(0),
		               dev.log.readerID, dev.log.cardID, dev.log.systemID,		               
		               "[" + Integer.toString(dev.log.nfcUidLen) + "] " + nfcuid,
		               dev.log.timestamp, new Date(dev.log.timestamp * 1000).toString())
					   + "\n"
					   );
			nfcuid.setLength(0);
		}
		rv.ret_string = result.toString();
		return rv;
	}
	
    public RetValues MainLoop(S_DEVICE dev){		
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
			rv = PrintRTE(dev);
			System.out.println(rv.ret_string);
			
		}
		
		if (dev.logAvailable != 0){
			rv = PrintLog(dev);
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
   
    public RetValues PrintRTE(S_DEVICE dev){
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
			
		String rteHead = String.format("AIS_ReadRTE_Count = %d%n", rteCount)
				       + "= RTE Real Time Events = \n"
				       + rteListHeader[0] + "\n"
				       + rteListHeader[1] + "\n"
				       + rteListHeader[0] + "\n";
		
		while (true){			
			dev.status = libInstance.AIS_ReadRTE(dev.hnd, logIndex, logAction, logReaderID, logCardID, logSystemID, nfcUid, nfcUidLen, timeStamp);			
			dev.log.index = logIndex.getValue();
			dev.log.action = logAction.getValue();
			dev.log.readerID = logReaderID.getValue();
			dev.log.cardID = logCardID.getValue();
			dev.log.systemID = logSystemID.getValue();
			dev.log.nfc_uid = nfcUid;
			dev.log.nfcUidLen = nfcUidLen.getValue();
			dev.log.timestamp = timeStamp.getValue();											
			
			if (dev.status !=0){
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
		rv.ret_string = rteHead + res + String.format("LOG unread (incremental) = %d %s\n", dev.logUnread, libInstance.dl_status2str(dev.status).getString(0));
		return rv;
	}
    
    private void printPercent(int Percent){
  	  
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
    
    
    
    
    public void DoCmd(S_DEVICE dev){
  	  if (dev.status !=0){
  		  return;
  		 }
  	  dev.cmdFinish = false;
  	  progress.printHdr = true;
  	  while(!dev.cmdFinish){
  		  rv = MainLoop(dev);		  		  		  		  
  		  if (!rv.ret_state){
  			  break;
  		  }
  		  
  	  }
  	  
    }   
    
	
  /**
   * 
   * @param dev - S_DEVICE class reference
   * @return unreaded log count
   */
  protected String printLogUnread(S_DEVICE dev){
	  return String.format("LOG unread (incremental) = %d", dev.logUnread);
  }
 
     
    public RetValues UnreadLogCount(S_DEVICE dev){     	
    	MainLoop(dev);
    	rv.ret_string = printLogUnread(dev);
    	return rv;
    }
    
    public RetValues UnreadLogGet(S_DEVICE dev){
    	StringBuilder nfcuid = new StringBuilder();  
    	StringBuilder res = new StringBuilder(); 
    	IntByReference logIndex = new IntByReference();
		IntByReference logAction = new IntByReference();
		IntByReference logReaderID = new IntByReference();
		IntByReference logCardID = new IntByReference();
		IntByReference logSystemID = new IntByReference();
		byte[] nfcUid = new byte[NFC_UID_MAX_LEN];
		IntByReference nfcUidLen = new IntByReference();
		LongByReference timeStamp = new LongByReference();					
		dev.status = libInstance.AIS_UnreadLOG_Get(dev.hnd, logIndex, logAction, logReaderID, logCardID, logSystemID, nfcUid, nfcUidLen, timeStamp);
		
		if (dev.status !=0){
			rv.ret_string = libInstance.dl_status2str(dev.status).getString(0);
			return rv;			
		}		
		for (int i=0;i<nfcUidLen.getValue();i++)
			nfcuid.append(String.format(":%02X", nfcUid[i]));
		
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
		
		
		res.append(String.format(rteFormat, dev.log.index, 
	               libInstance.dbg_action2str(dev.log.action).getString(0),
	               dev.log.readerID, dev.log.cardID, dev.log.systemID,		               
	               "[" + Integer.toString(dev.log.nfcUidLen) + "] " + nfcuid,
	               dev.log.timestamp, new Date(dev.log.timestamp * 1000).toString()
	              ));
	    rv.ret_string = rteHead
	    		      + res + "\n"
	    		      + rteListHeader[0] + "\n"
	    		      + String.format("%nAIS_UnreadLOG_Get() %s%n", libInstance.dl_status2str(dev.status).getString(0));
	    return rv;			
    }
   
   
  public RetValues UnreadLogAck(S_DEVICE dev){
	  int recToAck = RECORDS_TO_ACK;
	  dev.status = libInstance.AIS_UnreadLOG_Ack(dev.hnd, recToAck);
	  rv.ret_string = String.format("%nAIS_UnreadLOG_Ack() %s%n",  libInstance.dl_status2str(dev.status).getString(0));
	  return rv;
  }
    
    
    
    
   public String UnreadShowMeni(){
	   return "\n 1 : Count | 2 : Get | 3 : Ack | x : Exit "
			        + "\n--------------------------\n"
		            + "Press key to select action\n\n";
	  	  
   }
   
   @SuppressWarnings("resource")
   public boolean UnreadMeniLoop(){	   
	Scanner scan = new Scanner(System.in);
	String input = scan.nextLine();
	   if (input.contains("1"))
	     {
		   rv = UnreadLogCount(dev);		   
		   System.out.print(rv.ret_string);  
	     }
	   if (input.contains("2")){
		   rv = UnreadLogGet(dev);
		   System.out.print(rv.ret_string);
	   }
	   if (input.contains("3")){
		   rv = UnreadLogAck(dev);
		   System.out.print(rv.ret_string);
	   }
	   if (input.contains("x")){		  		  
		   return false; 
	   }	   
	   return true;
   }
   
   public void UnreadLog(){
	   System.out.println(UnreadShowMeni());
	   while (true){
		   if (UnreadMeniLoop() !=true) {
			   break;
			}
	   }
   }	
	
	
	
	
  public RetValues EELock(S_DEVICE dev){	  
	  byte[]pass = PASS.getBytes();	  
	  dev.status = libInstance.AIS_EE_WriteProtect(dev.hnd, pass);
	  rv.ret_string = String.format("EEPROM Lock - AIS_EE_WriteProtect() %s", libInstance.dl_status2str(dev.status).getString(0));
	  return rv;
  }
	
  public RetValues EEUnlock(S_DEVICE dev){
	  byte[]pass = PASS.getBytes();	 
	  dev.status = libInstance.AIS_EE_WriteUnProtect(dev.hnd, pass);
	  rv.ret_string = String.format("EEPROM Lock - AIS_EE_WriteUnProtect() %s", libInstance.dl_status2str(dev.status).getString(0));
	  return rv;
	  
  }
  
  @SuppressWarnings("resource")
  public void SetApplicationPass(S_DEVICE dev){
	  Scanner scan = new Scanner(System.in);
	  System.out.printf("%nOld password is actual application password: %s%n", PASS);
	  System.out.print("Enter new password for units ( and application ): ");
	  String newPass = scan.nextLine();
	  if (newPass.length() == 0){
		  System.out.println("Patch - new pass = default pass");
		  newPass = PASS;
	  }
	  System.out.printf("Try set new password for units= %s%n", newPass);
	  dev.status = libInstance.AIS_ChangePassword(dev.hnd, PASS.getBytes(), newPass.getBytes());
	  if (dev.status == 0){
		  PASS = newPass;
		  System.out.printf("New default application password = %s%n", PASS);		  
	  }	  
	  System.out.printf("AIS_ChangePassword (old pass= %s new pass= %s |%s%n" , PASS,newPass,libInstance.dl_status2str(dev.status).getString(0));	  	  
  }
  
  @SuppressWarnings("resource")
  public void ChangeDevicePass(S_DEVICE dev){
	  Scanner scan = new Scanner(System.in);
	  System.out.printf("%nActual application password is :%s%n", PASS);
	  System.out.print("Enter new default application password : ");
	  String newPass = scan.nextLine();
	  if (newPass.length() == 0){
		  System.out.println("Patch - new pass = default pass");
		  newPass = PASS;
	  }
	  PASS = newPass;
	  System.out.printf("%nNew default application password = %s%n", PASS);
  }
  
  
  
  
 //************************************************************************
	void listDevices(){    	
    	prepareListForCheck();
    	System.out.println("checking...please wait...");
    	int devCount = AisWrappListUpdateAndGetCount();    	    
    	System.out.printf("AIS_List_UpdateAndGetCount()= [%d]%n", devCount); 
    	if (devCount >0){
    		GetListInformation();
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
				String []linePart = line.trim().split(":");
				if (line.startsWith("#") || (linePart.length <= 1)){ 
					continue;				
				}
			   devTypeStr = linePart[0];
			   devId = Integer.parseInt(linePart[1]);					 
			   libInstance.device_type_str2enum(devTypeStr, devTypeEnum);
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
    	int dlstatus;
    	getDevices = AisWrappGetDevicesForCheck();
    	if (getDevices.length() == 0)
    	   {return;}        
    	for (String item : getDevices.split("\n"))
    	{
    	  String[] t = item.split(":");
    	  deviceType = Integer.parseInt(t[0]);
    	  deviceId = Integer.parseInt(t[1]);    	 
    	  dlstatus = libInstance.device_type_enum2str(deviceType, dev_type_str);    	  
    	  System.out.printf("     %20s (enum= %d) on ID %d%n", dev_type_str.getValue().getString(0), deviceType, deviceId);     	 
    	}    	    	    
    }
    
    
    
    void prepareListForCheck(){    	
    	System.out.println("AIS_List_GetDevicesForCheck() BEFORE / DLL STARTUP");
    	listForCheckPrint();
    	AisWrappListEraseAllDeviceForCheck();
    	if (!loadListFromFile()){
    		System.out.println("Tester try to connect with a Base HD device on any/unkown ID");
    	    addDevice(E_KNOWN_DEVICE_TYPES.DL_AIS_BASE_HD_SDK.value(),0);
    	
    	}
    	System.out.println("AIS_List_GetDevicesForCheck() AFTER LIST UPDATE");
    	listForCheckPrint();
    }
   
    int addDevice(int deviceType, int deviceId){
    	String fOut;
    	int dlstatus;
    	dlstatus = AisWrappListAddDeviceForCheck(deviceType, deviceId);
    	fOut = String.format("AIS_List_AddDeviceForCheck(type: %d, id: %d)> { %s }", 
    			                 deviceType, deviceId, libInstance.dl_status2str(dlstatus).getString(0));
    	System.out.println(fOut);
    	return dlstatus;
    }
    
    void ListEraseAllDevicesForCheck(){
    	System.out.println("AIS_List_EraseAllDevicesForCheck()");
    	AisWrappListEraseAllDeviceForCheck();
    }
    
    void GetDevicesForCheck(){
    	System.out.format("AISGetDevicesForCheck() :%s",AisWrappGetDevicesForCheck());
    }
    
    void ListAddDeviceForCheck(int deviceType, int deviceId){
    	int dlstatus;
    	String fOut;
    	dlstatus =  AisWrappListAddDeviceForCheck(deviceType, deviceId);
    	fOut = String.format("AIS_List_AddDeviceForCheck()>> deviceType: %d : deviceId: %d |DL_STATUS: %d", deviceType, deviceId, dlstatus);
    	System.out.println(fOut);
    }
    
   
    
	void GetListInformation(){    	    			
		StringBuilder result = new StringBuilder();
		StringBuilder res = new StringBuilder();
		int dlstatus;
		int devCount = AisWrappListUpdateAndGetCount();
    	if (devCount <= 0) {
    		return;
    		}
    	else
    	{    		
	    	HND_LIST.clear();	    
	    	for (int i = 0;i<devCount;i++)
	    	{	    		    		
	    		dlstatus = libInstance.AIS_List_GetInformation(hnd, 
	    				                                        devSerial, 
	    				                                        devType, 
	    				                                        devID,
	    				                                        devFW_VER,
	    				                                        devCommSpeed,
	    				                                        devFTDI_Serial,    				                                        
	    				                                        devOpened, 
	    				                                        devStatus, 
	    				                                        systemStatus);
	    		if (dlstatus !=0){ 
	    			return;    		
	    		}
	    		HND_LIST.add(hnd.getValue());    		
	    		Open();
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
				   res.append(result.toString() + myFormats.get(0));			         
			       System.out.println(res.toString());
					
					
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
       AisShell shell = new AisShell();       
       System.out.println(shell.AisWrappGetLibraryVersionStr());
       shell.init();          	  
   	   while (true) 
       {
    	   if (!shell.MeniLoop()){
    		   break;    		   
    	   }
       }
       System.exit(0);      
       return;
	}

}
