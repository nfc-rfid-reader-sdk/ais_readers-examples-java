package rs.dlogic.aiswrapper;

public interface MObservable {
	public void addObserver(MObserver o);
	public void removeObserver(MObserver o);
	public void notifyObserver(boolean event);
}
