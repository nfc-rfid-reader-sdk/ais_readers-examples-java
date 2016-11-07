/**
 * 
 */
package rs.dlogic.wrapper;

/**
 * @author Vladan
 *
 */
import java.util.ArrayList;

interface AisLibrary{	
	void AISGetLibraryVersion();
	void List_EraseAllDevicesForCheck();
	void AddDeviceForCheck();
	void List_UpdateAndGetCount();
	void List_GetInformation();
}


class AisInit {
	private ArrayList<AisLibrary> listeners = new ArrayList<AisLibrary>();
	public void addListener(AisLibrary toAdd){
		listeners.add(toAdd);
	}
	CLibrary.AisReaders libInstance;
	AisInit(){
		libInstance = CLibrary.AisReaders.aisReaders;
	}
	
	public void GetLibraryVersion(){
		System.out.println(libInstance.AIS_GetLibraryVersionStr().getString(0));
		for (AisLibrary h1:listeners){
			h1.AISGetLibraryVersion();
		}
	}
	
}



public class AisWrapper {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
       AisInit initiater = new AisInit();
       initiater.GetLibraryVersion();
	}

}
