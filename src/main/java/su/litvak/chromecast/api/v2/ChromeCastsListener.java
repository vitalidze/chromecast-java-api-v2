package su.litvak.chromecast.api.v2;

public interface ChromeCastsListener {
	
	public void newChromeCastDiscovered(ChromeCast chromeCast);
	
	public void chromeCastRemoved(ChromeCast chromecast);

}
