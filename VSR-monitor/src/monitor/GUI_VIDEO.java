/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package monitor;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

  /*****************/
 /*** GUI_VIDEO ***/
/*****************/

public class GUI_VIDEO extends JFrame{

	/** VARIABILI DI ISTANZA **/
	protected JLabel label;
	protected JCheckBox tastoAudio;
	protected boolean audioSiNo;
	/* fine variabili di istanza */
	
	
	
	/** COSTRUTTORE **/
	public GUI_VIDEO(String s){
		super(s);
		setLayout(new FlowLayout());
		label = new JLabel();
		setSize(350, 310);
		add(label);
		
		audioSiNo = false;
		tastoAudio = new JCheckBox("audio");
		add(tastoAudio);
		CBoxHandler handlerCheck = new CBoxHandler();
		tastoAudio.addItemListener(handlerCheck);
		
	}
	/* fine costruttore */
	
	
	
	/** CLASSE PRIVATA **/
	private class CBoxHandler implements ItemListener{
    	
    	public void itemStateChanged( ItemEvent event ){
    		if(event.getSource() == tastoAudio){
    			if(tastoAudio.isSelected()) audioSiNo = true;
    			else audioSiNo = false;
    		}	
    	}
    }
	/* fine classe privata*/
	
}
