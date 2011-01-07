/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package monitor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

  /****************/
 /*** GUI_SORV ***/
/****************/
 

public class GUI_SORV extends JFrame{
	
	/** VARIABILI DI ISTANZA **/
	private static JButton okJButton;
	protected static JCheckBox[] webcamJCheckBox;
	protected static boolean completo;
	/* fine variabili di istanza*/
		
		
	
	/** METODO GETVALORE **/
	public static int getValore(int i){
		return CheckBoxHandler.getValWebcam(i);
	}
		
		
	
	/** COSTRUTTORE **/
	public GUI_SORV(int n, String[] arr){
			
		super("Scegli le webcam da controllare");
		setLayout( new FlowLayout() );
		completo = false;
		
		okJButton = new JButton( "OK" );
		add(okJButton);
		ButtonHandler handlerButton = new ButtonHandler();
		okJButton.addActionListener(handlerButton);
		
		
		
		webcamJCheckBox = new JCheckBox[n];
		for(int i=0; i<n; i++){
			webcamJCheckBox[i] = new JCheckBox("webcam "+arr[i]);
			add(webcamJCheckBox[i]);
		}
			
		CheckBoxHandler handlerCheck = new CheckBoxHandler();
		for(int i=0; i<n; i++){
			webcamJCheckBox[i].addItemListener(handlerCheck);
		}
			
	}
	/* fine costruttore */
		
		
	
	/** CHIUSURA **/
	public int chiusura(){
		this.setVisible(false);
		return 0;
	}
	/* fine chiusura */
	
	
	
	/** CLASSI PRIVATE **/
	private class ButtonHandler implements ActionListener{
		
		public void actionPerformed(ActionEvent event){
			JOptionPane.showMessageDialog( GUI_SORV.this, String.format("Verranno salvati un file di log e l'ultimo fotogramma" ) ); //ok = event.getActionCommand()
			setVisible(false);
			completo = true;
		}
	}
		
	private static class CheckBoxHandler implements ItemListener{
		
		/** VARIABILI DI ISTANZA **/
		private static int[] valWebcam = new int[webcamJCheckBox.length];
		/* fine variabili di istanza */
		
		/** METODO GETVALWEBCAM **/
		public static int getValWebcam(int i){
			return valWebcam[i];
		}
		public void itemStateChanged( ItemEvent event ){
			for(int i=0; i<webcamJCheckBox.length; i++){
				if(event.getSource() == webcamJCheckBox[i]){
					if(webcamJCheckBox[i].isSelected()) valWebcam[i] = 1;
					else valWebcam[i] = 0;
				}	
			}
		}
	}
	/* fine classi private*/
	
}
