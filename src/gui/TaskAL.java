package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import snt.Snt;

public class TaskAL implements ActionListener {

    private static long intervall;

    public TaskAL() {
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
intervall = SntGUI.getInterval();
if(intervall > 0) {
SntGUI.updateGUIafterLaunch();
new Snt(intervall);
}
    }

}