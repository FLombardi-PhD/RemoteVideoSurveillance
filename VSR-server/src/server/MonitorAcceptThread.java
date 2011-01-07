/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

  /********************************/
 /*** THREAD DI MONITOR ACCEPT ***/
/********************************/


class MonitorAcceptThread extends Thread {
	
	/** VARIABILI DI ISTANZA **/
	protected ServerSocket serverMonitorSocket = null;
	protected Socket clientMonitorSocket = new Socket();
	/* fine variabili di istanza */
	
	
	
	/** COSTRUTTORE **/
	public MonitorAcceptThread(ServerSocket in) {
		serverMonitorSocket = in;
    }
	/*fine costruttore */
	
	
	
	/** METODO RUN **/
    public void run() {
    	
    	System.out.println("MonitorThreadAccept creato con successo");
    	try {
        	while (true) {
        		clientMonitorSocket = serverMonitorSocket.accept();
        		new MonitorThread(clientMonitorSocket).start();
        		Thread.sleep(MIN_PRIORITY);
            }
        } catch (IOException e) {
            System.err.println("Accept Monitor fallita");
            System.exit(1);
        } catch (InterruptedException e) { e.printStackTrace();	}
      
    }
    /* fine metodo run */
    
}