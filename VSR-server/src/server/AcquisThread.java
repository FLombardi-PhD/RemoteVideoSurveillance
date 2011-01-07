/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


  /******************************/
 /*** THREAD DI ACQUISIZIONE ***/
/******************************/


class AcquisThread extends Thread {
	
	/** VARIABILI DI ISTANZA **/
	protected Socket socketWebcam;
	protected Socket socketDati;
	protected ServerSocket serverSocket;
	protected ServerSocket serverSocketAudio;
	protected Socket socketAudio;
	private boolean webcamConnessa = true;
	private Long threadId;
	private Long idMonitor = new Long(0);
	private Integer id = 0;
	private int port = 65;
	/* fine variabili di istanza */
	
	/** VARIABILI DI CALENDARIO **/
	Calendar calendario;
	String giorno;
	String mese;
	String anno;
	String ora;
	String minuti;
	String secondi;
	String dataOra;
	String String_am_pm;
	int am_pm;
	/* fine variabili di calendario */
	
	
	/** COSTRUTTORE **/
    public AcquisThread(Socket socketIn) {
    	threadId = this.getId();
    	socketDati = new Socket();
    	socketWebcam = socketIn;
    	socketAudio = new Socket();
    }
    /* fine costruttore */
    
    
    
    /** COPIA FILE **/
    void copiaFile(File src, File dst, int byteUsati) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Trasferisce i bytes da in a out
        byte[] buf = new byte[byteUsati];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    /* fine copia file */
    
    
    
    /** CONTROLLO FILE UGUALI **/
    public boolean controlloFileUguali(int arr1, int arr2){
    	//algoritmo: daimplementare
    	if((arr1 < 15000 && arr1 > 10000) || (arr2 < 15000 && arr2 > 10000)){
    		if (arr1 > arr2){
        		if(arr1 - arr2 > 400) {
        			//System.out.println("dimensione 1="+arr1+"; dimensione 2="+arr2);
        			return false;
        		}
        	}
        	else if (arr2 > arr1){
        		if(arr2 - arr1 > 400) {
        			//System.out.println("dimensione 1="+arr1+"; dimensione 2="+arr2);
        			return false;
        		}
        	}
    	}
    	else if (arr1 < 10000 || arr2 < 10000){
    		if (arr1 > arr2){
        		if(arr1 - arr2 > 250) {
        			//System.out.println("dimensione 1="+arr1+"; dimensione 2="+arr2);
        			return false;
        		}
        	}
        	else if (arr2 > arr1){
        		if(arr2 - arr1 > 250) {
        			//System.out.println("dimensione 1="+arr1+"; dimensione 2="+arr2);
        			return false;
        		}
        	}
    	}
    	else{
    		if (arr1 > arr2){
        		if(arr1 - arr2 > 100) {
        			//System.out.println("dimensione 1="+arr1+"; dimensione 2="+arr2);
        			return false;
        		}
        	}
        	else if (arr2 > arr1){
        		if(arr2 - arr1 > 100) {
        			//System.out.println("dimensione 1="+arr1+"; dimensione 2="+arr2);
        			return false;
        		}
        	}
    	}
    	return true;
    }
    /* fine controllo file uguali */
 
    
    
    /** CALENDARIO **/
    private String calendario(){
    	calendario = new GregorianCalendar();
    	
    	giorno = Integer.toString(calendario.get(Calendar.DAY_OF_MONTH));
    	mese = Integer.toString(calendario.get(Calendar.MONTH));
    	anno = Integer.toString(calendario.get(Calendar.YEAR));
    	
    	ora = Integer.toString(calendario.get(Calendar.HOUR));
    	minuti = Integer.toString(calendario.get(Calendar.MINUTE));
    	secondi = Integer.toString(calendario.get(Calendar.SECOND));
    	am_pm = calendario.get(Calendar.AM_PM);
    	if(am_pm == 1) String_am_pm = "PM";
    	else String_am_pm = "AM";
    	
    	return dataOra = giorno.concat("/").concat(mese).concat("/").concat(anno).concat
    		("; ore: ").concat(ora).concat(":").concat(minuti).concat(".").concat(secondi)
    		.concat(" "+String_am_pm);
    }
    
    
    
    private synchronized void erroreSocketMonitor(){
    	   	
		//Elimino la entry del monitor dalla listaCoppie
		VSRServer.getLockCoppie().lock();
		try{
			VSRServer.getListaCoppie().get(threadId).remove(idMonitor);
		} finally { VSRServer.getLockCoppie().unlock(); }
		
		//Elimino la entry del monitor dalla listaAllarme
		VSRServer.getLockAllarme().lock();
		try{
			VSRServer.getListaAllarme().get(threadId).remove(idMonitor);	
		} finally { VSRServer.getLockAllarme().unlock(); }
		
		VSRServer.getLockRif().lock();
		int numSelezionate = 0;
		try{
			numSelezionate = VSRServer.getThreadId_Rif().get(idMonitor);
		} finally { VSRServer.getLockRif().unlock(); }
		
		if(numSelezionate == 1){
			
			//SOLO SE TUTTE LE ENTRY NON CI SONO PIU ELIMINA IL MONITOR DEFINITIVAMENTE
			//Elimino la entry del monitor da monitorWebcam
			VSRServer.getLockMonitor().lock();
			try{
				//VSRServer.getListaMonitor().get(idMonitor).close();
				VSRServer.getListaMonitor().remove(idMonitor);
			//} catch (IOException e) { e.printStackTrace();
			} finally { VSRServer.getLockMonitor().unlock(); }
			
			//Elimino la entry del monitor da monitorDati
			VSRServer.getLockMonitorDati().lock();
			try{
				//VSRServer.getListaMonitorDati().get(idMonitor).close();
				VSRServer.getListaMonitorDati().remove(idMonitor);
			//} catch (IOException e) { e.printStackTrace();	
			} finally { VSRServer.getLockMonitorDati().unlock(); }
			
			//Elimino la entry del monitor da monitorAudio
			VSRServer.getLockMonitorAudio().lock();
			try{
				//VSRServer.getListaMonitorAudio().get(idMonitor).close();
				VSRServer.getListaMonitorAudio().remove(idMonitor);
			//} catch (IOException e) { e.printStackTrace();	
			} finally { VSRServer.getLockMonitorAudio().unlock(); }
			
		}
		else{
			VSRServer.getLockRif().lock();
			try{
				VSRServer.getThreadId_Rif().remove(idMonitor);
				VSRServer.getThreadId_Rif().put(idMonitor, numSelezionate-1);
			} finally { VSRServer.getLockRif().unlock(); }
		}
		
    }
    
    
    
    private void erroreSocketWebcam(){
    	
    	/*DEVO MANDARE A VIDEO E SORVEGLIANZA CARATTERI DIVERSI*/
    	
    	try {
    		serverSocket.close();
			serverSocketAudio.close();
	    	socketDati.close();
	    	socketWebcam.close();
	    	socketAudio.close();
		} catch (IOException e) { e.printStackTrace(); }
    	
    	
    	
    	//Rimuovo tutte le entry dalle hastable relative alla webcam corrente
    	VSRServer.getLockID().lock();
		try{
			VSRServer.getId_ThreadId().remove(id);
		} finally { VSRServer.getLockID().unlock(); }
    	
    	VSRServer.getLockCoppie().lock();
		try{
			VSRServer.getListaCoppie().remove(threadId);
		} finally { VSRServer.getLockCoppie().unlock(); }
    	
		VSRServer.getLockAllarme().lock();
		try{
			VSRServer.getListaAllarme().remove(threadId);
		} finally { VSRServer.getLockAllarme().unlock(); }
		
    	
    	VSRServer.getLockWebcam().lock();
		try{
			VSRServer.getListaWebcam().remove(threadId);
		} finally { VSRServer.getLockWebcam().unlock(); }
		
		
		this.stop();
    }
    
    
    private synchronized void cicloAllarme(byte[] arrBytes,
    									   int numLetti,
    									   int dimArrPrec,
    									   int contPrimaFoto){
    	VSRServer.getLockAllarme().lock();
		Hashtable a = new Hashtable<Long,Socket>();
		try{
			Set<Long> setCoppie = VSRServer.getListaAllarme().keySet();
			Iterator it = setCoppie.iterator();
			while(it.hasNext()){
				Long longWebcamCorr = (Long) it.next();
				if(longWebcamCorr == threadId){

					//hastable contenente threadId e socket dei monitor
					a = VSRServer.getListaAllarme().get(threadId);
					
					//interrompo la ricerca quando trovo l'hashtable desiderata
					break;
				}
			}	
		} finally { VSRServer.getLockAllarme().unlock(); }
		
		//Ora devo cercare la socket giusta dalla hastable trovata sopra
		Set<Long> setIdMonitor_Sock = a.keySet();
		Iterator it = setIdMonitor_Sock.iterator();
		while(it.hasNext()){
			VSRServer.getLockMonitor().lock();
			Socket sockOutWebcam = null;
			Socket sockOutDati = null;
			try{
				idMonitor = (Long) it.next();
				sockOutWebcam = (Socket) a.get(idMonitor);
				sockOutDati = VSRServer.getListaMonitorDati().get(idMonitor);
				VSRServer.getLockMonitor().unlock();
			} catch(ConcurrentModificationException eCM) { 
				VSRServer.getLockMonitor().unlock();
				break;
			}	
				
			if(contPrimaFoto!=0){
				if(!controlloFileUguali(numLetti, dimArrPrec) || !webcamConnessa){
				
					//PRIMA LE SOCKET ERANO DEFINITE QUI E NON FUORI L'IF
				
				
					//Srivo il num di byte da leggere
					try{
						PrintWriter out = new PrintWriter(sockOutDati.getOutputStream(), true);
						if(webcamConnessa) out.println(numLetti);
						else out.println(-2);
					} catch(IOException e3) { erroreSocketMonitor(); break;
					} catch(NullPointerException eNPE){ break; }
				
				
					//Scrivo il messaggio d'allarme
					try{
						//System.out.println("lancio msg d'allarme");
						PrintWriter out = new PrintWriter(sockOutDati.getOutputStream(), true);
						if(webcamConnessa) out.println("ALLARME da Webcam "+id+"; "+calendario());
						else out.println("WEBCAM "+id+" DISCONNESSA!  "+calendario());
					} catch(IOException e) { erroreSocketMonitor(); break;
					} catch(NullPointerException eNPE){ break; }
				
				
					//Scrivo il fotogramma dell'allarme
					try{
						//System.out.println("msg lanciato, invio il file");
						OutputStream os = sockOutWebcam.getOutputStream();
						DataOutputStream dos = new DataOutputStream(os);
						if(webcamConnessa){
							dos.write(arrBytes, 0, numLetti);
							dos.flush();
						}
						
					} catch(IOException e) { erroreSocketMonitor(); break;
					} catch(NullPointerException eNPE){ break; }
				
				}	
			}	   							
			    					
		}
    }
    
    
    
    /** METODO RUN **/
    public synchronized void run() {
    	
    	System.out.println("AcquisThread creato con successo!");
    	
    	//Setto l'id
    	VSRServer.getLockIdDisp().lock();
    	try{
    		id = VSRServer.getIdDisponibile();
    		VSRServer.setIdDisponibile();
    	} finally {	VSRServer.getLockIdDisp().unlock(); }
    	
        System.out.println("id impostato "+id);
        
        
        //Creo la server socket di accept per dati String
        try {
        	System.out.println(port);
        	port += id;
        	System.out.println(port);
			serverSocket = new ServerSocket(port);
			System.out.println("attesa su porta "+port);
		} catch (IOException e) { e.printStackTrace(); }
		
        //SYNC1: Invio l'id al client usando, solo questa volta, la socket dedicata alla webcam
    	PrintWriter out;
		try {
			out = new PrintWriter(socketWebcam.getOutputStream(), true);
			out.println(id);
		} catch (IOException e2) { e2.printStackTrace(); }
    	
    	//SYNC2: Accetto la connessione con la socket per i dati String
    	try {
    			socketDati = serverSocket.accept();
    	} catch (IOException e1) { e1.printStackTrace(); }
    	
    	
    	/*****AGGIUNTO PER AUDIO1*****/ //NON MODIFICARE SENZA AUDIO
    	//SYNC2.1: audio
    	try {
        	serverSocketAudio = new ServerSocket(port+100);
			System.out.println("attesa su porta "+(port+100));
		} catch (IOException e) { e.printStackTrace(); }
		try {
			socketAudio = serverSocketAudio.accept();
			System.out.println("audio accettato");
		} catch (IOException e1) { e1.printStackTrace(); }
		System.out.println(socketWebcam.toString());
		System.out.println(socketDati.toString());
		System.out.println(socketAudio.toString());
    	/**FINE AGGIUNTA PER AUDIO**/
		
		//Popolo le hastable con id-threadId e threadId-socket
		VSRServer.getLockWebcam().lock();
		try{
			VSRServer.getListaWebcam().put(threadId, socketWebcam);
		} finally { VSRServer.getLockWebcam().unlock(); }    			
		
		//Assegno id-threadId
		VSRServer.getLockID().lock();
		try{
			VSRServer.getId_ThreadId().put(id, threadId);
		} finally { VSRServer.getLockID().unlock(); }
		
        //Popolo la listaCoppie aggiungendo  la entry relativa alla webcam corrente
        VSRServer.getLockCoppie().lock();
        try{
        	VSRServer.getListaCoppie().put(threadId, new Hashtable<Long,Socket>());
        } finally { VSRServer.getLockCoppie().unlock(); }
        
        
        //Popolo la listaAllarme aggiungendo la entry relativa alla webcam corrente
        VSRServer.getLockAllarme().lock();
        try{
        	VSRServer.getListaAllarme().put(threadId, new Hashtable<Long,Socket>());
        } finally { VSRServer.getLockAllarme().unlock(); }
        
        
		System.out.println("AcquisThread "+threadId+": hastable popolate");
		
        //Entro nel metodo vero e proprio
    	try {
    			//Variabili
    			int contPrimaFoto = 0;;
    			int numLetti;
    			
    			InputStream is = null;
    			DataInputStream dis = null;
    			OutputStream os = null;
    			DataOutputStream dos = null;
    			BufferedReader in = null;
    			InputStream isAudio = null;
    			DataInputStream disAudio = null;
    			OutputStream osAudio = null;
    			DataOutputStream dosAudio = null;
    			
    						
    			try{
    				//inputStream della socket (tra client e thread corrente) da cui leggere
    				is = socketWebcam.getInputStream();
    			
    				//DataInputStream: qui salvo i dati che leggo dallo stream
    				dis = new DataInputStream(is);    			
    			
    				//OutputStream: della socket (tra client di monitor e thread)
    				os = null;
    				
    				//DataOutputStream: da qui inoltro i dati ai monitor
    				dos = new DataOutputStream(os);
    				
    				//BufferedReader per scambio dati String:
    				in = new BufferedReader(new InputStreamReader(socketDati.getInputStream()));
    			
    			    			
    				/*****AGGIUNTO PER AUDIO2*****/ //NON MODIFICARE SENZA AUDIO
    				//Creo gli strem per l'audio
    				isAudio = socketAudio.getInputStream();
    				disAudio = new DataInputStream(isAudio);
    				osAudio = null;
    				dosAudio = new DataOutputStream(osAudio);
    				/**FINE AGGIUNTA PER AUDIO**/
    			
    			} catch(IOException e5){ erroreSocketWebcam(); }
    			
    		    //Array in cui salvo i byte letti
    		    byte[] arrBytes = null;
    			int dimArrPrec = 0;
    			numLetti = 0;
    			
    			/*****VARIABILI AUDIO3*****/ //NON MODIFICARE SENZA AUDIO
    			byte[] arrBytesAudio = null;
    			int numLettiAudio = 0;
    			arrBytesAudio = new byte[11024];
    			/**FINE VARIABILI AUDIO**/
    			
    			
    			
    			 /*************************/
    			/**Inizio ciclo infinito**/
    		   /*************************/
    			
    			System.out.println("AcquisThread "+threadId+": PRONTO!");
    			while(true){
    					
    				try{
    					    					
    					//SYNC3: Leggo dal client il numero di dati da leggere della webcam
    					numLetti = Integer.parseInt(in.readLine());
    					arrBytes = new byte[numLetti];
    					//System.out.println("video byte: "+numLetti);
    				
    					
    					//SYNC4: Leggo i bytes dell'immagine
    					dis.readFully(arrBytes, 0, numLetti);
    				    					
    					
    					/*****AGGIUNTO PER AUDIO4*****/
    					//SYNC5: Leggo dal client il numero di dati da leggere dal microfono
    					//numLettiAudio = Integer.parseInt(inAudio.readLine()); //62
    					//arrBytesAudio = new byte[numLettiAudio];
    					//System.out.println("audio byte: "+numLettiAudio);
    					
    					
    					//SYNC6: Leggo i bytes dell'audio
    					numLettiAudio = disAudio.read(arrBytesAudio, 0, 11024); 
    					//System.out.println("audio letto"+numLettiAudio);
    					/**FINE AGGIUNTA PER AUDIO**/
    					
    					
    				} catch (IOException eRicevo){ webcamConnessa = false; }
    				
    				
    				/**FASE VIDEO**/
    				
    				
    				//Cerco i client monitor associati alla webcam corrente
    				VSRServer.getLockCoppie().lock();
    				Hashtable<Long,Socket> a = new Hashtable<Long,Socket>();
    				Iterator<Long> it;
    				try{
    					Set<Long> setCoppie = VSRServer.getListaCoppie().keySet();
    					it = setCoppie.iterator();
    					while(it.hasNext()){
    						Long longWebcamCorr = it.next();
    						if(longWebcamCorr == threadId){
    							//hastable contenente threadId e socket dei monitor
    							a = VSRServer.getListaCoppie().get(threadId);
    							break;
    						}
    					}	
    				} finally { VSRServer.getLockCoppie().unlock(); }    				
    				
    				
    				//Scandisco la hashtable con threadId e socket dei monitor
    				Set<Long> setIdMonitor_Sock = a.keySet();
    				it = setIdMonitor_Sock.iterator();
    				while(it.hasNext()){
						VSRServer.getLockMonitor().lock();
    					Socket sockOutWebcam = null;
    					try{
    						idMonitor = it.next();
    						sockOutWebcam = a.get(idMonitor);
    					} catch(ConcurrentModificationException eCM) { 
    						VSRServer.getLockMonitor().unlock();
    						break;
    					} 
    					VSRServer.getLockMonitor().unlock(); 
    					
    					VSRServer.getLockMonitorDati().lock();
    					Socket sockOutDati;
    					try{
    						sockOutDati = VSRServer.getListaMonitorDati().get(idMonitor);
    					} finally { VSRServer.getLockMonitorDati().unlock(); }
    					
    					/**AUDIO1**/
    					VSRServer.getLockMonitorAudio().lock();
    					Socket sockOutAudio;
    					try{
    						sockOutAudio = VSRServer.getListaMonitorAudio().get(idMonitor);
    					} finally { VSRServer.getLockMonitorAudio().unlock(); }
    					//ho spostato gli stream audio nell'invio dell'audio
    					/**FINE AUDIO1**/
    					
 
    					//Invio il num di dati Video da leggere
    					try{
    						out = new PrintWriter(sockOutDati.getOutputStream(), true);
    						
    						//Se la webcam è ancora connessa scrivi il num di byte da leggere
    						if(webcamConnessa) out.println(numLetti);
    						
    						//Altrimenti scrivi -2 come messaggio d'errore
    						else out.println(-2);
    					} catch(IOException e) { erroreSocketMonitor();
    					} catch(NullPointerException eNPE) {}
    					
    					
    					//Invio l'immagine
    					try{
    						os = sockOutWebcam.getOutputStream();
    						dos = new DataOutputStream(os);
    						
    						if(webcamConnessa) {
    							dos.write(arrBytes, 0, numLetti);
    							dos.flush();
    						}
    						else dos.write(null);
    					} catch(IOException e) { erroreSocketMonitor();
    					} catch(NullPointerException eNPE) {}
    					
    					
    					//Invio l'audio
    					try{
    						osAudio = sockOutAudio.getOutputStream();
        					dosAudio = new DataOutputStream(osAudio);
        					if(webcamConnessa){
        						dosAudio.write(arrBytesAudio, 0, 11024);
        						dosAudio.flush();
        					}
        					else dosAudio.write(null);
    					} catch(IOException e) { erroreSocketMonitor();
    					} catch(NullPointerException eNPE) {}
    				}    				
    				
    				
    				/**Fase di allarme**/
    				//VSRServer.getLockCiclo().lock();
    				//try{
    					cicloAllarme(arrBytes, numLetti, dimArrPrec, contPrimaFoto);
    				//} finally{ VSRServer.getLockCiclo().unlock(); }
    				//Fine fase di allarme
    				
    				dimArrPrec = numLetti;
    				contPrimaFoto++;
    				Thread.sleep(100);
    				if(!webcamConnessa) erroreSocketWebcam();
    				
    			}	
    					
    				
    	} catch (InterruptedException e1) {	e1.printStackTrace(); } 
    	
    	
    }
    /* fine metodo run */    
    
}