/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package acquisizione;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

  /*************/
 /*** AUDIO ***/
/*************/


class AUDIO extends Thread{
		TargetDataLine targetDataline = null;
		byte[] data;
		private Socket sAudio;
		//private Socket sDati;
		
		public AUDIO(Socket audio){
			sAudio = audio;
			//sDati = dati;
		}

		public synchronized void run(){
			
			/* codice per la creazione e apertura della targetDataLine*/
			//1000,8,1,1,1000,false
			AudioFormat tmpAf = new AudioFormat(/*AudioFormat.Encoding*/ AudioFormat.Encoding.PCM_SIGNED ,
					/*float sampleRate*/ (float)11025.0,
					/*int sampleSizeInBits*/ 16,
					/*int channels*/ 2,
					/*int frameSize*/ 4,
					/*float frameRate*/ (float)11025.0,
					/*boolean bigEndian*/ false);
			
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, tmpAf);
			if (!AudioSystem.isLineSupported(info)){
				System.out.println("La linea " +info+ " non e'supportata.");} else
				try {
					targetDataline = (TargetDataLine) AudioSystem.getLine(info);
					targetDataline.open(tmpAf);
				} catch (LineUnavailableException e) { e.printStackTrace();	}
				
			/*fine codice targetDataLine*/
				
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int frameSizeInBytes = tmpAf.getFrameSize();
			int bufferSubLength = (int)(targetDataline.getBufferSize()/8); //frazione buffer
			int bufferLengthInBytes = bufferSubLength* frameSizeInBytes;
			data = new byte[bufferLengthInBytes];
			int numBytesRead;
			
			/***AGGIUNTI STREAM PER AUDIO***/
			OutputStream osAAudio = null;
			DataOutputStream dosAAudio = null;
			//PrintWriter outAAudio = null;
			try {
				osAAudio = sAudio.getOutputStream();
				dosAAudio = new DataOutputStream(osAAudio);
				//outAAudio = new PrintWriter(sDati.getOutputStream());
			} catch (IOException e) { e.printStackTrace(); }
			
			/**FINE AGGIUNTA STREAM PER AUDIO**/
			
			
			targetDataline.start();
			VSRAcquisizione.lockAudioInizio.lock();		
			while (this!=null) {
				
				//stava sotto l'attesa del token
				if((numBytesRead = targetDataline.read(data, 0, bufferLengthInBytes)) == -1) { break; }
				//targetDataline.read(data, 0, bufferLengthInBytes);
				out.write(data, 0, numBytesRead);
				
				while(!VSRAcquisizione.semaforoVideoFatto){}
				VSRAcquisizione.lockAudio.lock();
				VSRAcquisizione.semaforoAudioFatto = false;
				
				//qui ho tolto il pezzo sopra l'attesa del semaforo
				
				//SYNC5: invio il num di dati da leggere da microfono
				//outAAudio.println(numBytesRead);
				//outAAudio.flush();
				//System.out.println(numBytesRead);
				
				//SYNC6: invio i dati del microfono
				try {
					dosAAudio.write(data, 0, numBytesRead);
					dosAAudio.flush();
					//System.out.println("5");
					
					VSRAcquisizione.lockAudio.unlock();
					VSRAcquisizione.semaforoAudioFatto = true;
					VSRAcquisizione.semaforoVideoFatto = false;
					//Thread.sleep(250);
				//} catch (InterruptedException e) { e.printStackTrace();
				} catch (IOException e) { System.exit(1); 
				}
				
			}
			
			targetDataline.stop();
			targetDataline.close();
			
		}
}
