package gui;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SntGUI extends JFrame {

    private static JButton startBtn;
    private static JTextField intervalField;
    private static JLabel infoLabel;

    /**
     * @param args
     */
    public static void main(String[] args) {
	new SntGUI();
    }

    public SntGUI() {
	this.setLayout(new GridLayout(0, 1));
	infoLabel = new JLabel("Hvor ofte vil du se etter endringer (sekund)?");
	intervalField = new JTextField("30");
	startBtn = new JButton("Start");
	add(infoLabel);
	add(intervalField);
	add(startBtn);
	startBtn.addActionListener(new TaskAL());
	setSize(350, 80);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setVisible(true);

    }

    public static long getInterval() {
	long interval;
	try {
	    interval = Long.parseLong(intervalField.getText()) * 1000; // millisec
	} catch (Exception e) {
	    interval = 30000;
	    JOptionPane.showMessageDialog(null, "Ikke et tall, prøv igjen.");
	    return -1;
	}
	return interval;
    }

    protected static void updateGUIafterLaunch() {
	infoLabel.setText("Sjekker VG.no hvert " + intervalField.getText() + " sekund.");
	startBtn.setVisible(false);
	intervalField.setVisible(false);
    }

}
