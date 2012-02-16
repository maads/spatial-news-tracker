package crawl;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sok.Search;

public class AvisCrawl extends Thread {

	public static ArrayList<String> vgForside = new ArrayList<String>();

	public static ArrayList<ArrayList<String>> allArticle = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> allArticleOld = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> allArticleOldBT = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> allArticleBT = new ArrayList<ArrayList<String>>();
	public static ArrayList<String> articlebt = new ArrayList<String>();

	static JLabel label;
	JButton startbtn;
	JTextField textField;

	protected boolean isRunning = false;

	private JButton sokBtn;

	// Needed for making it OS independent. This is "/" on UNIX and "\" on
	// Windows.
	static String fileSeperator = System.getProperty("file.separator");
	
	public static int taskCounter = 0; 

	public AvisCrawl() {
		System.out.println("started " + new Date(System.currentTimeMillis()).toString());
		initGUI();
	}

	private void initGUI() {
		JFrame frame = new JFrame("Avis");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 200);

		label = new JLabel();
		sokBtn = new JButton("Søk i artikler");
		startbtn = new JButton("Start");
		startbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!isRunning) {
					Timer timer = new Timer();
					String secondsInput = textField.getText();
					int timerRateInMilliseconds = 10000; // 10 sec as default
					if (Util.isNumeric(secondsInput)) {
						timerRateInMilliseconds = Integer.parseInt(textField
								.getText()) * 1000; // to get it to milisec
						isRunning = true;
						timer.scheduleAtFixedRate(new CrawlTask(), 0,
								timerRateInMilliseconds);
						setTextFieldText("Sjekker VG.no hvert " + secondsInput
								+ " sekund");
					} else {
						JOptionPane.showMessageDialog(null,
								"Ikke et tall, prøv igjen");
					}
				} else
					JOptionPane.showMessageDialog(null, "Kjører allerede :)");
			}
		});
		textField = new JTextField(
				"Skriv inn hvor ofte vg.no skal sjekkes (i sekund)");
		frame.setLayout(new GridLayout(0, 1));
		frame.add(label);
		frame.add(textField);
		frame.add(startbtn);
		frame.add(sokBtn);
		sokBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Search();
			}
		});
		setLabelText(""); // bare for å vise noe tekst..
		frame.setVisible(true);
	}

	private void setTextFieldText(String string) {
		textField.setText(string);
	}

	public static void setLabelText(String text) {
		if (text.length() > 10)
			label.setText("Sist oppdatert kl: " + text.substring(0, 8));
		else
			label.setText("Enda ikke oppdatert");
	}

	public static void main(String[] args) {
		AvisCrawl av = new AvisCrawl();
	}
}