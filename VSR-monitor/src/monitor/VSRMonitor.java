/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package monitor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**IMPORT PER AUDIO**/
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

  /******************/
 /*** MONITORING ***/
/******************/


public class VSRMonitor{
	
	/** VARIABILI DI ISTANZA **/
	private static Socket socketMonitor;
	private static Socket socketDati;
	private static Socket socketAudio;
	private static int port = 176;
	private static int port2;
	/* fine variabili di istanza */
	
	
	
	/** COSTRUTTORE **/
	public VSRMonitor() {
		socketMonitor = null;
		socketDati = null;
		socketAudio = null;
    }
	/* fine costruttore */
	
	
	
	private static void chiusura(){
		JOptionPane.showMessageDialog(null, (String)"La webcam monitorata si è disconnessa. Chiusura..", "ATTENZIONE!", JOptionPane.WARNING_MESSAGE);
		System.exit(1);
	}
		
	
	
	/** MAIN **/
    public static void main(String[] args) {
    	
    	String[] mod = new String[2];
		String sel = "0";
		mod[0]="modalità video";
		mod[1]="modalità sorveglianza";
		String indirizzoServer = null;
		int numWebcamDisp;
		String[] elencoWebcam;
		
		InputStream is;
		DataInputStream dis;
		PrintWriter out;
		BufferedReader in;
		
		InputStream isAudio;
		DataInputStream disAudio;
		
		byte[] arrBytes;
		int numLetti = 0;
		
		byte[] arrBytesAudio;
		int numLettiAudio = 0;
		
		int id;
		int port2;
		
		//Inizio il metodo vero e proprio
    	try {
    		
    		//Chiedo a quale server connettersi
    		indirizzoServer = JOptionPane.showInputDialog(null, (String)"Inserisci l'indirizzo del server", "Client di monitoring", JOptionPane.QUESTION_MESSAGE);
    		if(indirizzoServer == null) System.exit(1);
    		if(indirizzoServer.equals("")) indirizzoServer = "localhost";
    		
    		//Creo la socket di scambio dati webcam
			socketMonitor = new Socket(indirizzoServer, port); 
			is = socketMonitor.getInputStream();
			dis = new DataInputStream(is);
			
			//SYNC1: Leggo l'id del thread
			in = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));
			id = Integer.parseInt(in.readLine());
			port2 = port+id;
			System.out.println("VSRMonitor "+id+": porta per video = "+port);
			
			//SYNC2: Creo la socket per lo scambio dati String
			socketDati = new Socket(indirizzoServer, port2);
			System.out.println("VSRMonitor "+id+": porta per dati = "+port2);
			
			//SYNC3: Creo la socket per lo scambio dati Audio
			socketAudio = new Socket(indirizzoServer, (port2+100));
			System.out.println("VSRMonitor "+id+": porta per audio = "+port2);
			
			//Chiedi modalità video o sorveglianza con JoptionPane
			sel = (String)JOptionPane.showInputDialog(null, "scegli l'opzione", "Client di monitoring", 1, null, mod, mod[0]);
			out = new PrintWriter(socketMonitor.getOutputStream(), true);
			out.println(sel); /*scrivo la scelta al MonitorThread*/
			
			if(sel==null){
				out.println("0");
				System.exit(1);
			}
			
			
			
			/**MODALITA' VIDEO**/
			if(sel.equals("modalità video")){
				
				//Leggo elenco webcam disponibili
				in = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));
				String input = in.readLine();
				System.out.println("VSRMonitor "+id+": ho letto da MonitorThread : "+input);
				numWebcamDisp = Integer.parseInt(input);
				System.out.println("VSRMonitor "+id+": webcam disponibili : "+numWebcamDisp);
				
				//Se non ci sono webcam disponibili chiudi
				if(numWebcamDisp == 0){
					JOptionPane.showMessageDialog(null, (String)"Non sono connesse webcam. Chiusura..", "ATTENZIONE!", JOptionPane.WARNING_MESSAGE);
					System.exit(1);
				}
				
				elencoWebcam = new String[numWebcamDisp];
				
				//Leggo gli id delle webcam
				for(int i=0; i<numWebcamDisp; i++){
					elencoWebcam[i] = in.readLine();
				}
				
				
				//Chiedo all'utente quale webcam vedere
				sel = (String)JOptionPane.showInputDialog(null, "scegli la webcam da monitorare", "Client di monitoring", 1, null, elencoWebcam, elencoWebcam[0]);
				
				//Se ho premuto annulla esco
				if(sel == null){
					out.println("0");
					System.exit(1);
				}
				
				//Se ho scelto qualcosa la printo
				out.println(sel);
				
				//Costruisco la frame
				GUI_VIDEO grafica = new GUI_VIDEO("MONITOR "+id+":  visualizza webcam "+Integer.parseInt(sel));
			    grafica.setVisible(true);
			    grafica.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			    grafica.setResizable(false);
				
				//Setto gli stream per l'audio
			    isAudio = socketAudio.getInputStream();
				disAudio = new DataInputStream(isAudio);
				
				
				/**Da qui in poi comunico direttamente con AcquisThread**/
				//ridefinisco lo stream di lettura dati String
			    in = new BufferedReader(new InputStreamReader(socketDati.getInputStream()));
			    
			    
			    /**VARIABILI PER AUDIO**/
			    AudioFormat m_audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
			    		(float)11025.0, 16, 2, 4, (float)11025.0, false);
			    
			    DataLine.Info	targetInfo = new DataLine.Info(SourceDataLine.class,
			    	  	  m_audioFormat);
			    
			    SourceDataLine m_line;
			    m_line = (SourceDataLine) AudioSystem.getLine( targetInfo );
			    
			    arrBytesAudio = new byte[11024];
			    
			    //Eventuali timer alle socket
			    //socketDati.setSoTimeout(3000);
				//socketMonitor.setSoTimeout(3000);
				//socketAudio.setSoTimeout(3000);
			    
			    //Entro nel ciclo di lettura
				while(true){
					try{
						
						//Apro e avvio la linea audio
						m_line.open( m_audioFormat );
						m_line.start();
					
						//Leggo il numerdo di byte da leggere
						numLetti = Integer.parseInt(in.readLine());
						if(numLetti<0){
							chiusura();
						}
						arrBytes = new byte[numLetti];
						//System.out.println("MONITOR: dati letti "+numLetti);
						
						
						//Leggo l'immagine
						dis.readFully(arrBytes, 0, numLetti);
						
						
						//Setto l'icona
						ImageIcon imm = new ImageIcon(arrBytes); 
						grafica.label.setIcon(imm);
						
								
						//Leggo l'audio e lo salvo nell'array
						numLettiAudio = disAudio.read(arrBytesAudio, 0, 11024);
						if(numLettiAudio<1){
							chiusura();
						}
						//System.out.println("MONITOR: audio letto "+numLettiAudio);
						m_line.write(arrBytesAudio, 0, numLettiAudio);
						
						
						//Se audio è selezionato riproducilo
						if(grafica.audioSiNo) {
							m_line.drain();
						}
						m_line.close();		
							
							
					} catch(IOException eIOE){
						chiusura();

					}
				}				
			}
			/**fine modalità video**/
			
			
			
			/**MODALITA' SORVEGLIANZA**/
			else if(sel.equals("modalità sorveglianza")){
				
				int webcamAttive = 0;
				
				//Avviso MonitorThread della scelta "modalità sorveglianza"
				in = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));
				numWebcamDisp = Integer.parseInt(in.readLine());
				
				//Se non ci sono webcam disponibili chiudi
				if(numWebcamDisp == 0){
					JOptionPane.showMessageDialog(null, (String)"Non sono connesse webcam. Chiusura..", "ATTENZIONE!", JOptionPane.WARNING_MESSAGE);
					System.exit(1);
				}
				
				//Leggo gli indici delle webcam presenti
				elencoWebcam = new String[numWebcamDisp];
				for(int i=0; i<numWebcamDisp; i++){
					elencoWebcam[i] = in.readLine();
				}
				
				//Imposto la grafica con i checkbox
				GUI_SORV grafica = new GUI_SORV(numWebcamDisp, elencoWebcam);
				grafica.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				grafica.setSize(160, 100+30*numWebcamDisp);
				grafica.setVisible(true);
				while(!GUI_SORV.completo){};
				
				//Invio i flag selezione\deselezione al monitorThread
				for(int i=0; i<numWebcamDisp; i++){
					System.out.println("VSRMonitor "+id+": indice mandato "+i+" = "+GUI_SORV.getValore(i));
					if(GUI_SORV.getValore(i) == 1) webcamAttive++; 
					out.println(GUI_SORV.getValore(i));					
				}
				
				
				/**Da qui in poi comunico direttamente con AcquisThread**/
				
				
			    //Costruisco la frame con l'elenco degli allarmi e il fotogramma
				int webcamMonitorate = webcamAttive;
				JFrame frame = new JFrame("MONITOR "+id+":    "+webcamMonitorate+"/"+webcamAttive+" webcam sorvegliate attive");
				Container contentPane = new Container();
				JPanel panelWebcam = new JPanel();
				JPanel panelText = new JPanel();
					
			    JTextArea text = new JTextArea();
			    JLabel label = new JLabel();
			    JScrollPane scroll = new JScrollPane(text);
			    			    
			    text.setLineWrap(true);
			    text.setWrapStyleWord(false);
			    text.setColumns(35);
			    text.setRows(15);
			    text.setAutoscrolls(true);
			    text.setEditable(false);
			    			    
			    contentPane.setLayout(new FlowLayout());
			    frame.setLayout(new BorderLayout());
			    
			    frame.setSize(780, 290);
			    contentPane.setSize(780, 290);
			    panelWebcam.setSize(350, 290);
			    
			    panelWebcam.add(label);
			    panelText.add(scroll);
			    
			    contentPane.add(panelText);
			    contentPane.add(panelWebcam);
			    
			    frame.add(contentPane);
			    panelText.setVisible(true);
			    panelWebcam.setVisible(true);
			    contentPane.setVisible(true);
			    frame.setResizable(false);
			    frame.setVisible(true);
			    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			    scroll.setVisible(true);
			    
			    //Costruisco i file di log e ultimo fotogramma
			    PrintStream logFile = new PrintStream(new FileOutputStream("LOG_allarmi_utente_"+id+".txt"));
			    logFile.println("CRONOLOGIA ALLARMI: \n\n");
			    File ultimaFoto = new File("ultima_foto_utente_"+id+".jpg");
			    ultimaFoto.setWritable(true, true);
			    FileOutputStream fos = new FileOutputStream(ultimaFoto);
			    
			    //Stringhe per dettagli allarme e allarme precedente
				in = new BufferedReader(new InputStreamReader(socketDati.getInputStream()));
				String stringAllarme = new String();
				String allarmePrecedente = new String();
				arrBytes = new byte[1];
				
				while(true){	
				
					while(true){
					
						try{
							
							//Chiudo lo stream del file per aggiornarlo
							fos.close();
							//System.out.println("VSRMonitor "+id+": 1");
					
							
							//Leggo il num di byte da leggere
							numLetti = Integer.parseInt(in.readLine());
							stringAllarme = in.readLine();
							
							if(numLetti>0){
								arrBytes = new byte[numLetti];
								//System.out.println("VSRMonitor "+id+": 2");
					
								//Leggo da dove viene l'allarme
								if(!stringAllarme.equals(allarmePrecedente)){
									text.insert(stringAllarme+"\n", 0);
									logFile.println(stringAllarme+"\n");
								}
								//System.out.println("VSRMonitor "+id+": 3");
							
								
								//Leggo l'immagine
								dis.readFully(arrBytes, 0, numLetti);
								//System.out.println("VSRMonitor "+id+": 4");
							
								
								//Stampo l'icona
								ImageIcon imm = new ImageIcon(arrBytes); 
								label.setIcon(imm);
							
							
								//Scrivo su file jpg
								fos = new FileOutputStream(ultimaFoto);
								fos.write(arrBytes);
								//System.out.println("VSRMonitor "+id+": 5");
							
								
								//Aggiorno la stringa dell'allarme precedente
								allarmePrecedente = new String();
								allarmePrecedente = stringAllarme;
								//System.out.println("VSRMonitor "+id+": 6");
								
							}
							
							else{
								
								//Se ho ricevuto un allarme di disconnessione webcam
								webcamAttive--;
								frame.setTitle("MONITOR "+id+":    "+webcamAttive+"/"+webcamMonitorate+" webcam sorvegliate attive");
								text.insert(stringAllarme+"\n", 0);
								logFile.println(stringAllarme+"\n");
								if(webcamAttive == 0) chiusura();
							}
							
						} catch(NullPointerException eNPE){ break;
						} catch(NumberFormatException eIFE) { break;
						} catch(IOException eIOE){ chiusura(); }
					}		
				}
			}
			/**fine modalità sorveglianza**/
			
			else{
				
				//avvisa MonitorThread della chiusura della socket: da implementare
				out.println("0");
				System.exit(1);
			}
			
    	} catch (UnknownHostException e) {e.printStackTrace();
    	} catch (IOException e) { 
    		JOptionPane.showMessageDialog(null, (String)"Errore di connessione. Chiusura..", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
    		System.exit(1);
    	} catch (LineUnavailableException e) { e.printStackTrace(); }
    	
    }
    /** fine main **/
  
}