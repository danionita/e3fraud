/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test
;


import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import e3fraud.gui.MainWindow;

/**
 *
 * @author IonitaD
 */
public class GUI {

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                MainWindow.createAndShowGUI();
            }
        });
    }
}
