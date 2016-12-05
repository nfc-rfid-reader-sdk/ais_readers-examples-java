package rs.dlogic.aiswrapper;

public enum E_KNOWN_DEVICE_TYPES {
	DL_UNKNOWN_DEVICE  (0),				
	DL_AIS_100 (1),
	DL_AIS_20  (2),
	DL_AIS_30  (3),
	DL_AIS_35  (4),
	DL_AIS_50  (5),
	DL_AIS_110 (6),
	DL_AIS_LOYALITY  (7),
	DL_AIS_37 (8),		
	DL_AIS_BMR (9),
	DL_AIS_BASE_HD (10),
	DL_AIS_BASE_HD_SDK (11),
	DL_AIS_SYSTEM_TYPES_COUNT (12);
	
	private int value;
	private E_KNOWN_DEVICE_TYPES(int value){
		this.value = value;
	}		
	public int value(){
		return value;
	}	
}

