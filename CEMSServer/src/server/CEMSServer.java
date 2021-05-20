package server;

import ocsf.server.ObservableServer;

public class CEMSServer extends ObservableServer{

	public CEMSServer(int port) {
		super(port);
		
	}
	
	@Override
	protected void serverStarted(){
		String serverStartedString = SERVER_STARTED.substring(0,SERVER_STARTED.length()-1);
		sendToLog(serverStartedString + " on port: " + getPort());
	}
	
	/**
	 * this function 
	 * @param msg - the string for sending to the server log
	 */
	private void sendToLog(String msg) {
		setChanged();
		notifyObservers(msg);
	}
	
	@Override
	protected void serverStopped() {
		
	}
	
	

}
