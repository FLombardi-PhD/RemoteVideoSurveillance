/*
 * tesi plm di PROGETTO DI RETI E SISTEMI INFORMATICI
 * titolo: Videosorveglianza Remota
 * autore: Federico Lombardi
 * 
 */

package acquisizione;

import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.net.Socket;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.Robot;

//import java.nio.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.media.*;
import javax.media.datasink.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.util.*;
import javax.media.control.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import com.sun.image.codec.jpeg.*;
import com.sun.media.protocol.vfw.VFWCapture;

  /**********************/
 /*** VSRAcquisizone ***/
/**********************/


public class VSRAcquisizione extends JFrame implements WindowListener, ComponentListener {
	
	/** VARIABILI DI ISTANZA **/
	private static Socket socketWebcam;
	protected static Socket socketDati;
	private static int port = 65;
	private static int port2;
	private static int id;
	protected static ReentrantLock lockAudio = new ReentrantLock();
	protected static ReentrantLock lockAudioInizio = new ReentrantLock();
	protected static boolean semaforoVideoFatto;
	protected static boolean semaforoAudioFatto;
	
	/* variabili di formato*/
	protected final static int MIN_WIDTH = 160;
	protected final static int MIN_HEIGHT = 100;
	protected static int shotCounter = 1;
	/* fine variabili di formato*/
		
	/* variabili swing */
	protected JLabel statusBar = null;
	protected JPanel visualContainer = null;
	protected Component visualComponent = null;
	protected JToolBar toolbar = null;
	protected MyToolBarAction formatButton = null;
	protected MyToolBarAction captureButton = null;
	/* fine variabili swing */
	
	/* variabili del player */
	protected Player player = null;
	protected CaptureDeviceInfo webCamDeviceInfo = null;
	protected MediaLocator ml = null;
	protected Dimension imageSize = null;
	protected FormatControl formatControl = null;
	protected VideoFormat currentFormat = null;
	protected Format[] videoFormats = null;
	protected MyVideoFormat[] myFormatList = null;
	protected boolean initialised = false;
	/* fine variabili del player */
	

	
	/** COSTRUTTORE **/
	public VSRAcquisizione(String frameTitle) {
		super(frameTitle);
		
		socketWebcam = null;
		socketDati = null;
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Exception cnfe) {
			System.out.println(cnfe.getMessage());
		}
		
		// dimensione della finestra
		setSize(320, 260);
		addWindowListener(this);
		addComponentListener(this);
		getContentPane().setLayout(new BorderLayout());
		visualContainer = new JPanel();
		visualContainer.setLayout(new BorderLayout());
		getContentPane().add(visualContainer, BorderLayout.CENTER);
		statusBar = new JLabel("");
		statusBar.setBorder(new EtchedBorder());
		getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

/*
* ----------------------------------------------------------------
* @restituisce true se viene trovata una webcam *
* ----------------------------------------------------------------
*/
	public boolean initialise() throws Exception {
	return (initialise(autoDetect()));
	}

/*
* -------------------------------------------------------------------
* @params _deviceInfo, informazioni sulla webcam rilevata * @restituisce
* true se la webcam viene correttamente rilevata*
* -------------------------------------------------------------------
*/
	public boolean initialise(CaptureDeviceInfo _deviceInfo) throws Exception {
		statusBar.setText("Avvio in corso...");
		webCamDeviceInfo = _deviceInfo;
		if (webCamDeviceInfo != null) {
		statusBar.setText("Connessione alla webcam : "
		+ webCamDeviceInfo.getName());
		try {
			setUpToolBar();
			getContentPane().add(toolbar, BorderLayout.NORTH);
			ml = webCamDeviceInfo.getLocator();
			
			if (ml != null) {
				player = Manager.createRealizedPlayer(ml);
				
				if (player != null) {
					player.start();
					formatControl = (FormatControl) player.getControl("javax.media.control.FormatControl");
					videoFormats = webCamDeviceInfo.getFormats();
					visualComponent = player.getVisualComponent();

					if (visualComponent != null) {
						visualContainer.add(visualComponent, BorderLayout.CENTER);
						myFormatList = new MyVideoFormat[videoFormats.length];
			
						for (int i = 0; i < videoFormats.length; i++) {
							myFormatList[i] = new MyVideoFormat(
							(VideoFormat) videoFormats[i]);
						}

						Format currFormat = formatControl.getFormat();
					
						if (currFormat instanceof VideoFormat) {
							currentFormat = (VideoFormat) currFormat;
							imageSize = currentFormat.getSize();
							visualContainer.setPreferredSize(imageSize);
							setSize(imageSize.width, imageSize.height + statusBar.getHeight() + toolbar.getHeight());
						} else {System.err.println("Errore nella rilevazione del formato video.");}
						
						invalidate();
						pack();
						return (true);
					} else {
						System.err.println("Errore nella creazione della componente visiva.");
						return (false);
					}
				} else {
					System.err.println("Errore nella creazione del player.");
					statusBar.setText("Errore nella creazione del player.");
					return (false);
				}
			} else {
				System.err.println("Nessun MediaLocator per: " + webCamDeviceInfo.getName());
				statusBar.setText("Nessun MediaLocator per: " + webCamDeviceInfo.getName());
				return (false);
			}
		} catch (IOException ioEx) {
			statusBar.setText("Connessione a: " + webCamDeviceInfo.getName());
			return (false);
		} catch (NoPlayerException npex) {
			statusBar.setText("Errore nella creazione del player.");
			return (false);
		} catch (CannotRealizeException nre) {
			statusBar.setText("Errore nella realizzazione del player.");
			return (false);
		}

		} else return (false);
	}


	public void setFormat(VideoFormat selectedFormat) {
		if (formatControl != null) {
		player.stop();
		imageSize = selectedFormat.getSize();
		formatControl.setFormat(selectedFormat);
		player.start();
		statusBar.setText("Formato: " + selectedFormat);
		currentFormat = selectedFormat;
		visualContainer.setPreferredSize(currentFormat.getSize());
		setSize(imageSize.width, imageSize.height + statusBar.getHeight()+ toolbar.getHeight());
		} else {
			System.out.println("Visual component non è un'istanza di FormatControl");
			statusBar.setText("Visual component non può cambiare formato");
		}
	}


	public VideoFormat getFormat() {
	return (currentFormat);
	}


	protected void setUpToolBar() {
		toolbar = new JToolBar();
		formatButton = new MyToolBarAction("Risoluzione", null);
		captureButton = new MyToolBarAction("Cattura immagine", null);
		toolbar.add(formatButton);
		toolbar.add(captureButton);
		getContentPane().add(toolbar, BorderLayout.NORTH);
	}


	protected void toolbarHandler(MyToolBarAction actionBtn) {
		if (actionBtn == formatButton) {
		Object selected = JOptionPane.showInputDialog(this,
		"Selezionare il formato video", "Selezionare il formato",
		JOptionPane.INFORMATION_MESSAGE, null, myFormatList, currentFormat);
		
		if (selected != null) {
			setFormat(((MyVideoFormat) selected).format);
		}
		
		} else if (actionBtn == captureButton) {
			Image photo = grabFrameImage();
			if (photo != null) {
				MySnapshot snapshot = new MySnapshot(photo, new Dimension(imageSize));
			} else {
				System.err.println("Errore : Impossibile grabbare il frame");
			}
		}
	}


/*-------------------------------------------------------------------
* Cerca una webcam installata per Windows (vfw)* 
* @restituisce le informazioni sul device trovato
*-------------------------------------------------------------------*/

	public CaptureDeviceInfo autoDetect() {
	Vector list = CaptureDeviceManager.getDeviceList(null);
	CaptureDeviceInfo devInfo = null;

	if (list != null) {
		String name;
		for (int i = 0; i < list.size(); i++) {
			devInfo = (CaptureDeviceInfo) list.elementAt(i);
			name = devInfo.getName();
			if (name.startsWith("vfw:")) {
				break;
			}
		}
	
		if (devInfo != null && devInfo.getName().startsWith("vfw:")) {
		return (devInfo);
		} else {
			for (int i = 0; i < 10; i++) {
				try {
					name = VFWCapture.capGetDriverDescriptionName(i);
					if (name != null && name.length() > 1) {
						devInfo = com.sun.media.protocol.vfw.VFWSourceStream.autoDetect(i);
						if (devInfo != null) {
						return (devInfo);
						}
					}
				} catch (Exception ioEx) {
					statusBar.setText("Errore nella ricerca della webcam : " + ioEx.getMessage());
				}
			}
		return (null);
		}
	} else {
	return (null);
	}
	}

	public void deviceInfo() {
		if (webCamDeviceInfo != null) {
			Format[] formats = webCamDeviceInfo.getFormats();
			if ((formats != null) && (formats.length > 0)) {
			}
			for (int i = 0; i < formats.length; i++) {
				Format aFormat = formats[i];
				if (aFormat instanceof VideoFormat) {
					Dimension dim = ((VideoFormat) aFormat).getSize();
					System.out.println("Video Format " + i + ": " + formats[i].getEncoding() + ", " + dim.width + " x " + dim.height);
				}
			}
		} else {
			System.out.println("Errore : Nessuna webcam trovata!");
		}
	}

/*
* -------------------------------------------------------------------
* Grabba un frame dalla webcam @restituisce il frame in un buffer
* -------------------------------------------------------------------
*/

	public Buffer grabFrameBuffer() {
		if (player != null) {
			FrameGrabbingControl fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
			if (fgc != null) {
			return (fgc.grabFrame());
			} else {
			System.err.println("Errore : FrameGrabbingControl non disponibile");
			return (null);
			}
		} else {
		System.err.println("Errore nel Player");
		return (null);
		}
	}

/*
* -------------------------------------------------------------------
* Converte il buffer frame in un'immagine
* -------------------------------------------------------------------
*/

	public Image grabFrameImage() {
		Buffer buffer = grabFrameBuffer();
		if (buffer != null) {
			BufferToImage btoi = new BufferToImage((VideoFormat) buffer.getFormat());
			
			if (btoi != null) {
			Image image = btoi.createImage(buffer);
			
				if (image != null) {
					return (image);
				} else {
					System.err.println("Errore di conversione Buffer - BufferToImage");
					return (null);
				}
			} else {
				System.err.println("Errore nella creazione di BufferToImage");
				return (null);
			}
		} else {
			System.out.println("Errore: buffer vuoto");
			return (null);
		}
	}


	public void playerClose() {
		if (player != null) {
			player.close();
			player.deallocate();
			player = null;
		}
	}


	public void windowClosing(WindowEvent e) {
		playerClose();
		System.exit(1);
	}


	public void componentResized(ComponentEvent e) {
		Dimension dim = getSize();
		boolean mustResize = false;
		if (dim.width < MIN_WIDTH) {
			dim.width = MIN_WIDTH;
			mustResize = true;
		}
		
		if (dim.height < MIN_HEIGHT) {
			dim.height = MIN_HEIGHT;
			mustResize = true;
		}
		
		if (mustResize) setSize(dim);
	}


	public void windowActivated(WindowEvent e) {
	}


	public void windowClosed(WindowEvent e) {
	}


	public void windowDeactivated(WindowEvent e) {
	}


	public void windowDeiconified(WindowEvent e) {
	}


	public void windowIconified(WindowEvent e) {
	}


	public void windowOpened(WindowEvent e) {
	}


	public void componentHidden(ComponentEvent e) {
	}


	public void componentMoved(ComponentEvent e) {
	}


	public void componentShown(ComponentEvent e) {
	}


	protected void finalize() throws Throwable {
		playerClose();
		super.finalize();
	}


class MyToolBarAction extends AbstractAction {

	public MyToolBarAction(String name, String imagefile) {
		super(name);
	}

	public void actionPerformed(ActionEvent event) {
		toolbarHandler(captureButton);
	}

};

class MyVideoFormat {

	public VideoFormat format;

	public MyVideoFormat(VideoFormat _format) {
		format = _format;
	}

	public String toString() {
		Dimension dim = format.getSize();
		return (format.getEncoding() + " [ " + dim.width + " x " + dim.height + " ]");
	}

};


class MySnapshot extends JFrame {
	protected Image photo = null;

	protected int shotNumber;

	public MySnapshot(Image grabbedFrame, Dimension imageSize) {
		super();
		shotNumber = shotCounter++;
		setTitle("Immagine" + shotNumber);
		photo = grabbedFrame;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		int imageHeight = photo.getWidth(this);
		int imageWidth = photo.getHeight(this);
		setSize(imageSize.width, imageSize.height);
		final FileDialog saveDialog = new FileDialog(this, "Salva immagine", FileDialog.SAVE);
		final JFrame thisCopy = this;
		saveDialog.setFile("Immagine" + shotNumber);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveDialog.show();
				String filename = saveDialog.getFile();
				if (filename != null) {
					if (saveJPEG(filename)) {
						JOptionPane.showMessageDialog(thisCopy, "Salvata immagine " + filename);
						setVisible(false);
						dispose();
					} else {
					JOptionPane.showMessageDialog(thisCopy, "Errore nel salvataggio di " + filename);
					}
				} else {
				setVisible(false);
				dispose();
				}
			}
		});

		setVisible(true);
	}


	public void paint(Graphics g) {
		g.drawImage(photo, 0, 0, getWidth(), getHeight(), this);
	}

/*
* -------------------------------------------------------------------
* Salva un'immagine come JPEG * @params immagine da salvare * @params
* nome del file dove salvare l'immagine *
* -------------------------------------------------------------------
*/

	public boolean saveJPEG(String filename) {
		boolean saved = false;
		BufferedImage bi = new BufferedImage(photo.getWidth(null), photo.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bi.createGraphics();
		g2.drawImage(photo, null, null);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
			param.setQuality(1.0f, false);
			encoder.setJPEGEncodeParam(param);
			encoder.encode(bi);
			out.close();
			saved = true;
		} catch (Exception ex) {
		System.out.println("Errore salvataggio JPEG: " + ex.getMessage());
		}
		return (saved);
	}
}


//Ritorna un BufferedImage con il contenuto dell'immagine passata
public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
        return (BufferedImage)image;
    }

    // Creo l'oggetto Image
    image = new ImageIcon(image).getImage();

    // Creo un BufferedImage con un formato compatibile con lo schermo
    BufferedImage bimage = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
        // Determino la trasparenza
        int transparency = Transparency.OPAQUE;
        
        // Creao il BufferedImage
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        bimage = gc.createCompatibleImage(
            image.getWidth(null), image.getHeight(null), transparency);
    } catch (HeadlessException e) {
        //viene lancaita solo se non � presente uno schermo
    }

    if (bimage == null) {
        // Creo a BufferedImage usando il color model di default
        int type = BufferedImage.TYPE_INT_RGB;
        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
    }

    // Copio l'Image nel BufferedImage
    Graphics g = bimage.createGraphics();

    //Dipingo l'immagine nel BufferedImage
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
}

public static Image toImage(BufferedImage bufferedImage) {
    return Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
}


private static int chiusura(){
	try {
		socketDati.close();
		socketWebcam.close();
	} catch (IOException e) {}	
	return JFrame.EXIT_ON_CLOSE;
}



	/** MAIN **/
	public static synchronized void main(String[] args) {
		try {
			VSRAcquisizione myWebcam = new VSRAcquisizione("WebCam Capture");
			myWebcam.setVisible(false);
			
			if (!myWebcam.initialise()) {
				JOptionPane.showMessageDialog(null, (String)"WebCam non trovata / inizializzata. Chiusura..", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			String indirizzoServer; // = null;
			indirizzoServer = JOptionPane.showInputDialog(null, (String)"Inserisci l'indirizzo del server", "Client di acquisizione", JOptionPane.QUESTION_MESSAGE);
			System.out.println("indirizzo: "+indirizzoServer);
			if(indirizzoServer == null) System.exit(1);
			System.out.println("indirizzo: "+indirizzoServer);
			if(indirizzoServer.equals("")) indirizzoServer = "localhost";
			
			//Creo la socket per lo scambio dati Webcam
			socketWebcam = new Socket(indirizzoServer, port);
			
			//SYNC1: Leggo un attimo la porta su cui creare la socket per scambio dati String
			BufferedReader in = new BufferedReader(new InputStreamReader(socketWebcam.getInputStream()));
			id = Integer.parseInt(in.readLine());
			port2 = port+id;
			System.out.println("VSRAcquisizione "+id+": porta per webcam = "+port2);
			
	
			//SYNC2: Creo la socket per lo scambio dati String
			socketDati = new Socket(indirizzoServer, port2);
			System.out.println("VSRAcquisizione "+id+": porta per dati = "+port2);			
			
			/*****AGGIUNTA PER AUDIO1*****/ 
			Socket socketAudio = new Socket(indirizzoServer, (port2+100)); //LASCIARE SENZA AUDIO!
			System.out.println("VSRAcquisizione "+id+": porta per audio = "+(port2+100));
			/**FINE AGGIUNTA PER AUDIO**/
			
			
			//Creo gli stream per i dati String
			PrintWriter out = new PrintWriter(socketDati.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socketDati.getInputStream()));
			
			//Creo gli stream per i dati Webcam
			OutputStream os = socketWebcam.getOutputStream();
		    DataOutputStream dos = new DataOutputStream(os);
		    
		    /*****AGGIUNTA PER AUDIO2*****/
		    //Creo e avvio l'audio
		    lockAudioInizio.lock();
			AUDIO audio = new AUDIO(socketAudio);
			audio.start();
			
			//Stampo il resoconto delle socket create
			System.out.println("VSRAcquisizione "+id+": socketWebcam = "+socketWebcam.toString());
			System.out.println("VSRAcquisizione "+id+": socketDati = "+socketDati.toString());
			System.out.println("VSRAcquisizione "+id+": socketAudio = "+socketAudio.toString());
			/**FINE AGGIUNTA PER AUDIO**/
			
			//Grabbo frame dalla webcam
			JFrame frame = new JFrame();
		    frame.setLayout(new FlowLayout());
		    frame.setTitle("WEBCAM "+id);
		    JLabel label = new JLabel("Webcam "+id+" connessa al server..");
		    frame.setSize(210, 70);
		    frame.setResizable(false);
		    frame.add(label);
		    Thread.sleep(1000);
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setVisible(true);
		    
		    /*****AGGIUNTA PER AUDIO3*****/
		    semaforoVideoFatto = false;
		    semaforoAudioFatto = true;
			lockAudioInizio.unlock();
			/**FINE AGGIUNTA PER AUDIO**/
		    
			System.out.println("VSRAcquisizione: PRONTO!");
	
			//Imposto un timeout: se non ricevo niente per n secondi chiudo
			socketDati.setSoTimeout(5000);
			
		    //Entro nel while(true)
			while(true){
				
				try{
					
					/*****AGGIUNTA PER AUDIO4*****/
					while(!semaforoAudioFatto){} lockAudio.lock();
					/**FINE AGGIUNTA PER AUDIO**/
				
					//Creo l'immagine
					Image foto = myWebcam.grabFrameImage();
					BufferedImage buffImage = toBufferedImage(foto);
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					RenderedImage im = buffImage;
					ImageIO.write(im, "jpeg", bout);
					byte[] bytes = bout.toByteArray();
					

					//SYNC3: Invio la dimensione dell'array di bytes dell'immagine
					//System.out.println("1");
					out.println(bytes.length);
					//System.out.println("2");
					
					//SYNC4: Invio l'array con i byte dell'immagine
					//System.out.println("3");
					dos.write(bytes, 0, bytes.length);			    
					dos.flush();
					//System.out.println("4");
					
					//Setto l'icona della label con l'immagine
					//ImageIcon imm = new ImageIcon(bytes); 
					//label.setIcon(imm);
					
					/*****AGGIUNTA PER AUDIO5*****/
					lockAudio.unlock();
					semaforoVideoFatto = true;
					semaforoAudioFatto = false;
					/**FINE AGGIUNTA PER AUDIO**/
					
					Thread.sleep(250); //4 immagini al secondo
					
				} catch(NullPointerException eNPE){ continue;
				} catch (IOException ex) {	audio.stop(); System.exit(1); }
				
			}
		} catch (Exception ex) {}
	}
}
