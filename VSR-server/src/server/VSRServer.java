/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

  /*************************/
 /*** CLASSE VSR SERVER ***/
/*************************/


public class VSRServer extends Thread{
	
	/** VARIABILI DI ISTANZA **/
	private static int idDisponibile = 1;
	
	private static ReentrantLock lockIdDisp = new ReentrantLock();
	private static ReentrantLock lockWebcam = new ReentrantLock();
	private static ReentrantLock lockMonitor = new ReentrantLock();
	private static ReentrantLock lockMonitorDati = new ReentrantLock();
	private static ReentrantLock lockMonitorAudio = new ReentrantLock();
	private static ReentrantLock lockCiclo = new ReentrantLock();
	
	private static ReentrantLock lockCoppie = new ReentrantLock();
	private static ReentrantLock lockAllarme = new ReentrantLock();
	private static ReentrantLock lockID = new ReentrantLock();
	private static ReentrantLock lockRif = new ReentrantLock();
	
	private static Hashtable<Long,Socket> listaWebcam;
	private static Hashtable<Long,Socket> listaMonitor;
	private static Hashtable<Long,Socket> listaMonitorDati;
	private static Hashtable<Long,Socket> listaMonitorAudio;
	
	private static Hashtable<Long, Hashtable<Long,Socket> > listaCoppie; /* threadId webcam - threadId monitor */
	private static Hashtable<Long, Hashtable<Long,Socket> > listaAllarme;
	private static Hashtable<Integer, Long> id_ThreadId;
	private static Hashtable<Long, Integer> threadId_Rif;
	/* fine varibili di istanza */
	
	
	
	/** COSTRUTTORE **/
	public VSRServer(){
		
	}
	/* fine costruttore */
	
	
	
	/** METODI GET **/
	public static int getIdDisponibile(){
		return idDisponibile;
	}
	
	public static Hashtable<Long,Socket> getListaWebcam(){
		return listaWebcam;
	}

	public static Hashtable<Long,Socket> getListaMonitor(){
		return listaMonitor;
	}
	
	public static Hashtable<Long,Socket> getListaMonitorDati(){
		return listaMonitorDati;
	}
	
	public static Hashtable<Long,Socket> getListaMonitorAudio(){
		return listaMonitorAudio;
	}
	
	public static Hashtable<Long, Hashtable<Long,Socket> > getListaCoppie(){
		return listaCoppie;
	}
	
	public static Hashtable<Long, Hashtable<Long,Socket> > getListaAllarme(){
		return listaAllarme;
	}
	
	public static Hashtable<Integer, Long> getId_ThreadId(){
		return id_ThreadId;
	}
	
	public static Hashtable<Long, Integer> getThreadId_Rif(){
		return threadId_Rif;
	}
	
	public static ReentrantLock getLockIdDisp(){
		return lockIdDisp;
	}
	
	public static ReentrantLock getLockWebcam(){
		return lockWebcam;
	}
	
	public static ReentrantLock getLockMonitor(){
		return lockMonitor;
	}
	
	public static ReentrantLock getLockMonitorDati(){
		return lockMonitorDati;
	}
	
	public static ReentrantLock getLockMonitorAudio(){
		return lockMonitorAudio;
	}
	
	public static ReentrantLock getLockCoppie(){
		return lockCoppie;
	}
	
	public static ReentrantLock getLockAllarme(){
		return lockAllarme;
	}
	
	public static ReentrantLock getLockID(){
		return lockID;
	}
	
	public static ReentrantLock getLockRif(){
		return lockRif;
	}
	
	public static ReentrantLock getLockCiclo(){
		return lockCiclo;
	}
	/* fine metodi GET */

	
	
	/** METODO SET_ID_DISPONIBILE **/
	public static void setIdDisponibile(){
		idDisponibile++;
	}
	/* fine metodo SET */
	
	
	
  /************/
 /*** MAIN ***/
/************/
	
	public static void main(String[] args) throws IOException {
		
		//Inizializzo le variabili
		listaWebcam = new Hashtable<Long,Socket>();
		listaMonitor = new Hashtable<Long,Socket>();
		listaMonitorDati = new Hashtable<Long,Socket>();
		listaMonitorAudio = new Hashtable<Long,Socket>();
		listaCoppie = new Hashtable<Long, Hashtable<Long,Socket> >(); //WebcamId - <MonitorId, Socket>
		listaAllarme = new Hashtable<Long, Hashtable<Long,Socket> >();
		id_ThreadId = new Hashtable<Integer, Long>();
		threadId_Rif = new Hashtable<Long, Integer>();
        ServerSocket serverMonitorSocket = null;
        ServerSocket serverAcquisSocket = null;
                
        //Provo a creare la ServerSocket di acquisizione
        try {
        	serverAcquisSocket = new ServerSocket(65);
        } catch (IOException e) {
            System.err.println("impossibile ascoltare la porta: 65.");
            System.exit(1);
        }
        
        //Provo a creare la ServerSocket di monitoraggio
        try {
        	serverMonitorSocket = new ServerSocket(176);
        } catch (IOException e) {
            System.err.println("impossibile ascoltare la porta: 176.");
            System.exit(1);
        }
      
        //Avvio il thread di accept() di monitoring
        MonitorAcceptThread monitorAcceptT = new MonitorAcceptThread(serverMonitorSocket);
        monitorAcceptT.start();
        
        //Mi metto in accept per i client di acquisizione
        Socket clientAcquisSocket = null;
        try {
        	while (true) {
        		clientAcquisSocket = serverAcquisSocket.accept();
        		new AcquisThread(clientAcquisSocket).start();
        		Thread.sleep(MIN_PRIORITY);
            }
        } catch (IOException e) {
            System.err.println("Accept Acquisizione fallita");
            System.exit(1);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
}