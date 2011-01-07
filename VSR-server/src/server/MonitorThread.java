/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.io.*;

import javax.swing.JOptionPane;

  /*************************/
 /*** THREAD DI MONITOR ***/
/*************************/


class MonitorThread extends Thread {
	
	/** VARIABILI DI ISTANZA **/
	protected Socket socketMonitor;
	protected Socket socketDati;
	protected Socket socketAudio;
	protected ServerSocket serverSocketDati;
	protected ServerSocket serverSocketAudio;
	private Long threadId;
	private String sceltaModalità;
	private int port = 176;
	/* fine variabili di istanza */
	
	
	
	/** COSTRUTTORE **/
	public MonitorThread(Socket socketIn) {
		socketMonitor = socketIn;
		socketDati = null;
		threadId = this.getId();
		long Tid = threadId;
		port += (int)Tid;
		
		//Istanzio la serverSocketDati
		try {
			serverSocketDati = new ServerSocket(port);
		} catch (IOException e) { e.printStackTrace(); }
		
		//Istanzio la serverSocketDati
		try {
			serverSocketAudio = new ServerSocket(port+100);
		} catch (IOException e) { e.printStackTrace(); }
    }
	/*fine costruttore */
	
	
	
	/** METODO RUN **/
    public synchronized void run() {
    	
    	//Aggiungo all'hashtable <threadId monitor, Socket webcam> il monitor corrente
		VSRServer.getLockMonitor().lock();
		try{
			VSRServer.getListaMonitor().put(threadId, socketMonitor);
		} finally { VSRServer.getLockMonitor().unlock(); }
		System.out.println("MonitorThread "+threadId+" creato con successo; ThreadID="+getId());
		
		//SYNC1: Invio l'id del monitor
		PrintWriter out;
		try {
			out = new PrintWriter(socketMonitor.getOutputStream(), true);
			out.println(threadId);
		} catch (IOException e2) { e2.printStackTrace(); }
		
		//SYNC2: Accetto la connessione con la socket per i dati String
		try {
			socketDati = serverSocketDati.accept();
		} catch (IOException e1) { e1.printStackTrace(); }
		
		//Aggiungo all'hashtable <threadId monitor, Socket dati> il monitor corrente
		VSRServer.getLockMonitorDati().lock();
		try{
			VSRServer.getListaMonitorDati().put(threadId, socketDati);
		} finally { VSRServer.getLockMonitorDati().unlock(); }
    	
		//SYNC3: Accetto la connessione con la socket per i dati Audio
		try {
			socketAudio = serverSocketAudio.accept();
		} catch (IOException e1) { e1.printStackTrace(); }
		
		//Aggiungo all'hashtable <threadId monitor, Socket audio> il monitor corrente
		VSRServer.getLockMonitorAudio().lock();
		try{
			VSRServer.getListaMonitorAudio().put(threadId, socketAudio);
		} finally { VSRServer.getLockMonitorAudio().unlock(); }
		
    	//Leggo la scelta (video o sorveglianza) dall'utente
    		
    	try{
    		
    		//scelta modalità video o sorveglianza
    		out = new PrintWriter(socketMonitor.getOutputStream(), true);
    		BufferedReader in = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));
    		sceltaModalità = in.readLine(); /*leggo scelta modalità dal VSRMonitor*/
    		System.out.println("MonitorThread "+threadId+": scelta la "+sceltaModalità);
    		
    		//Elenco webcam disponibili
    		VSRServer.getLockWebcam().lock();
    		int numWebcamDisp = VSRServer.getListaWebcam().size();
    		VSRServer.getLockWebcam().unlock();
    		Set<Integer> setElencoIdWebcam = null;
    		Iterator<Integer> it = null;
    		Integer[] webcamIdArray = new Integer[numWebcamDisp];
    		int indiceArray = 0;
    		VSRServer.getLockID().lock();
    		try{
    			
    			//Inserisco nell'array gli indici
    			setElencoIdWebcam = VSRServer.getId_ThreadId().keySet();
    			it = setElencoIdWebcam.iterator();
    			while(it.hasNext()){
    				webcamIdArray[indiceArray] = it.next();
    				indiceArray++;
    			}
    			
    			//Ordino l'array con selection sort
    			for (int i=0; i<webcamIdArray.length-1; i++) {
    				for (int j=i+1; j<webcamIdArray.length; j++) {
    					if (webcamIdArray[i] > webcamIdArray[j]) {
    						int temp = webcamIdArray[i];
    						webcamIdArray[i] = webcamIdArray[j];
    						webcamIdArray[j] = temp;
    					}
    				}
    			}
    			
    		} finally { VSRServer.getLockID().unlock(); }
    		
    		
    		
    		
    		/**MODALITA' VIDEO**/
    		if(sceltaModalità.equals("modalità video")){
    			
    			int idWebcamDaVedere;
    			String stringLetta;
    		
    			//Dico al monitor quante webcam ci sono
    			System.out.println("MonitorThread "+threadId+": invio a VSRMonitor il num di webcam disponibili("+numWebcamDisp+")");
    			out.println(numWebcamDisp);
    			 
    			//Se non ci sono webcam disponibili chiudi
				if(numWebcamDisp == 0){
					this.stop();
				}
				
    			//Invio id delle webcam disponibili
    			for(int i=0; i<numWebcamDisp; i++){
    				out.println(webcamIdArray[i]);
    			}
    			
    			//Chiedo all'utente quale webcam vuole vedere
    			in = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));
    			System.out.println("MonitorThread "+threadId+": chiedo a VSRMonitor quale webcam vuole vedere");
    			stringLetta = in.readLine();
    			if(stringLetta.equals("0")){
    				System.out.println("il monitor ha premuto 'annulla'");
        			VSRServer.getLockMonitor().lock();
        			try{
        				VSRServer.getListaMonitor().remove(threadId); /*elimino la entry del monitor dalla matrice*/
        			} finally { VSRServer.getLockMonitor().unlock(); }
        			this.stop();
    			}
    			
    			
    			//Leggo la webcam da visualizzare
    			idWebcamDaVedere = Integer.parseInt(stringLetta);
    			System.out.println("VSRMonitor ha scelto la webcam "+idWebcamDaVedere);
    			
    			
    			//Cerco il ThreadId relativo alla webcam chiesta dall'utente tramite l'id
    			Long threadIdWebcam = new Long(0);
        		VSRServer.getLockID().lock();
        		try{
        			threadIdWebcam = VSRServer.getId_ThreadId().get(idWebcamDaVedere);
        		} finally { VSRServer.getLockID().unlock();	}
        		System.out.println("VSRMonitor ha scelto la webcam "+idWebcamDaVedere+"; ThreadId="+threadIdWebcam);
        		
        		
    			//Setto la coppia monitor-webcam scelta
    			VSRServer.getLockCoppie().lock();
    			try{
    				VSRServer.getListaCoppie().get(threadIdWebcam).put(threadId, socketMonitor);
    				System.out.println("MonitorThread "+threadId+": aggiorno la matrice con la coppia VSRMonitor-Webcam");
    			} finally { VSRServer.getLockCoppie().unlock(); }
    			
    			
    			//Qui ho finito con MonitorThread. AcquisThread comunica con VSRMonitor
    			System.out.println("MonitorThread: Monitor "+threadId+" Webcam "+idWebcamDaVedere);
    			System.out.println("MonitorThread "+threadId+": da qui non servo più");
    		
    			VSRServer.getLockRif().lock();
    			try{
    				VSRServer.getThreadId_Rif().put(threadId, 1);
    			} finally { VSRServer.getLockRif().unlock(); }
    			
    		}
    	
    		
    		
    		/**MODALITA' SORVEGLIANZA**/
    		else if(sceltaModalità.equals("modalità sorveglianza")){
    			
    			//Dico al monitor quante webcam ci sono
    			out.println(numWebcamDisp);
    			int webcamSelTemp;
    			System.out.println("entro nel ciclo");
    		
    			
    			//Invio gli indici al monitor
    			for(int i=0; i<numWebcamDisp; i++){
    				out.println(webcamIdArray[i]);
    			}
    			
    			
    			//Chiedo all'utente quali webcam vuole sorvegliare
    			Long threadIdWebcam;
    			it = setElencoIdWebcam.iterator();
    			VSRServer.getLockID().lock();
    			int numSelezionate = 0;
    			
    			try{
    				int cont = 0;
    				while(cont<numWebcamDisp){
    					webcamSelTemp = Integer.parseInt(in.readLine());
    					threadIdWebcam = VSRServer.getId_ThreadId().get(webcamIdArray[cont]);
      					if(webcamSelTemp == 1){
    						numSelezionate++;
    						VSRServer.getLockAllarme().lock();
    						try{ 
    							VSRServer.getListaAllarme().get(threadIdWebcam).put(threadId, socketMonitor); /*threadId web - threadId monitor */
    						} finally { VSRServer.getLockAllarme().unlock(); }
    					}
    					System.out.println("MonitorThread "+threadId+": flag "+webcamSelTemp+" per webcam "+(cont)+"; ThreadIdWebcam="+threadIdWebcam);
    					cont++;
    				}
    			} catch(IOException eIOE){ 
    				if(VSRServer.getLockAllarme().isLocked()) VSRServer.getLockAllarme().unlock();
    			} finally { VSRServer.getLockID().unlock(); }
    			
    			
    			//Condizione che non ho selezionato nessuna webcam
    			if(numSelezionate == 0){
    				VSRServer.getLockMonitor().lock();
        			try{
        				VSRServer.getListaMonitor().remove(threadId); /*elimino la entry del monitor dalla matrice*/
        			} finally { VSRServer.getLockMonitor().unlock(); }
        			this.stop();
    			}
    			
    			
    			//Setto il numero di riferimenti di quante webcam sorveglia il monitor corrente
    			VSRServer.getLockRif().lock();
    			try{
    				VSRServer.getThreadId_Rif().put(threadId, numSelezionate);
    			} finally { VSRServer.getLockRif().unlock(); }
    			
    			
    			//Se ho almeno una webcam selezionata vado avanti
    			System.out.println("MonitorThread "+threadId+": da qui non servo più");
   
    		}
    	
    		
    		
    		/**PREMUTO TASTO ANNULLA**/
    		else{
    			System.out.println("il monitor ha premuto 'annulla'");
    			VSRServer.getLockMonitor().lock();
    			try{
    				VSRServer.getListaMonitor().remove(threadId); /*elimino la entry del monitor dalla matrice*/
    			} finally { VSRServer.getLockMonitor().unlock(); }
    			this.stop();
    		}
    		
    		
    	} catch(IOException e) {
    		VSRServer.getLockMonitor().lock();
    		try{
    			VSRServer.getListaMonitor().remove(threadId);
    			} finally { VSRServer.getLockMonitor().unlock(); }
    		e.printStackTrace();
    	}	
 
    }
    /* fine metodo run */
    
}