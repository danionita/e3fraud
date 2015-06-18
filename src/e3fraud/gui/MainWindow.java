/*
 * Copyright (C) 2015 Dan Ionita 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package e3fraud.gui;

import com.hp.hpl.jena.rdf.model.Resource;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.jfree.chart.JFreeChart;
import e3fraud.model.E3Model;
import e3fraud.model.ModelRanker;
import e3fraud.model.SubIdealModelGenerator;
import e3fraud.parser.FileParser;
import e3fraud.vocabulary.E3value;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartFrame;

/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
public class MainWindow extends JPanel
        implements ActionListener {

    int COMPONENT_WIDTH = 50;

    static private final String newline = "\n";
    JButton openButton, saveButton, generateButton, graphButton;
    JTextArea log;
    JLabel label;
    JList list;
    DefaultListModel<E3Model> subIdealModelList = new DefaultListModel<>();
    JFileChooser fc;
    JFileChooser sfc;
    Frame listFrame;
    JScrollPane listScrollPane, logScrollPane;
    JFreeChart graph1 = null;
    JFreeChart graph2 = null;
    E3Model baseModel = null;
    Resource selectedNeed;
    Resource selectedActor;
    int startValue = 0, endValue = 0;
    

    public MainWindow() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(15, 50);
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);
        logScrollPane = new JScrollPane(log);

        list = new JList<>(subIdealModelList);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(5);
        list.setCellRenderer(new CustomListCellRenderer(list));
        listScrollPane = new JScrollPane(list);

        //Create a file chooser for saving
        sfc = new JFileChooser();
        FileFilter jpegFilter = new FileNameExtensionFilter("JPEG image", new String[]{"jpg", "jpeg"});
        sfc.addChoosableFileFilter(jpegFilter);
        sfc.setFileFilter(jpegFilter);

        //Create a file chooser for loading
        fc = new JFileChooser();
        FileFilter rdfFilter = new FileNameExtensionFilter("RDF file", "RDF");
        fc.addChoosableFileFilter(rdfFilter);
        fc.setFileFilter(rdfFilter);
        
        label=new JLabel();
        label.setText("Renked list of generated models:");

        //Create the open button.  
        openButton = new JButton("Load model",
                createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);

        //Create the graph button. 
        graphButton = new JButton("Show graph",
                createImageIcon("images/Plot.png"));
        graphButton.addActionListener(this);
        
        //Create the generation button. 
        generateButton = new JButton("Generate sub-ideal models",
                createImageIcon("images/generate.png"));
        generateButton.addActionListener(this);

        //Create the save button. 
        saveButton = new JButton("Save graph",
                createImageIcon("images/Save16.gif"));
        saveButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(graphButton);
        buttonPanel.add(saveButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        add(listScrollPane, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(MainWindow.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                log.append("Opening: " + file.getName() + "." + newline);
                //parse file
                this.baseModel = FileParser.parseFile(file);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

            //handle Graph button
        } else if (e.getSource() == generateButton) {
            if (this.baseModel != null) {

                //have the user indicate the ToA via pop-up
                JFrame frame1 = new JFrame("Select Target of Assessment");
                Map<String, Resource> actorsMap = this.baseModel.getActorsMap();
                String selectedActorString = (String) JOptionPane.showInputDialog(frame1,
                        "Which actor's perspective are you taking?",
                        "Choose main actor",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        actorsMap.keySet().toArray(),
                        actorsMap.keySet().toArray()[0]);

                //have the user select a need via pop-up
                JFrame frame2 = new JFrame("Select graph parameter");
                Map<String, Resource> needsMap = this.baseModel.getNeedsMap();
                String selectedNeedString = (String) JOptionPane.showInputDialog(frame2,
                        "What do you want to use as parameter?",
                        "Choose need to parametrize",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        needsMap.keySet().toArray(),
                        needsMap.keySet().toArray()[0]);

                //have the user select occurence interval via pop-up
                JTextField xField = new JTextField("1", 4);
                JTextField yField = new JTextField("500", 4);
                JPanel myPanel = new JPanel();
                myPanel.add(new JLabel("Mininum occurences:"));
                myPanel.add(xField);
                myPanel.add(Box.createHorizontalStrut(15)); // a spacer
                myPanel.add(new JLabel("Maximum occurences:"));
                myPanel.add(yField);
                int result = JOptionPane.showConfirmDialog(null, myPanel,
                        "Please Enter occurence rate interval", JOptionPane.OK_CANCEL_OPTION);
                //temporary occurence interval
                
                if (result == JOptionPane.OK_OPTION) {
                    startValue = Integer.parseInt(xField.getText());
                    endValue = Integer.parseInt(yField.getText());
                } else if (result == JOptionPane.CANCEL_OPTION) {
                    selectedNeedString = null;
                }

                //generate sub-ideal models
                if (selectedNeedString != null && selectedActorString != null) {
                    selectedNeed = needsMap.get(selectedNeedString);
                    selectedActor = actorsMap.get(selectedActorString);
                    log.append("Generating sub-ideal models...." + newline);
                    revalidate();
                    repaint();
                    super.validate();
                    super.repaint();
                    SubIdealModelGenerator subIdealModelGenerator = new SubIdealModelGenerator();
                    Set<E3Model> subIdealModels = subIdealModelGenerator.generateAll(baseModel, selectedActor);
                    //Set<E3Model> subIdealModels = subIdealModelGenerator.generateHiddenTransactions(baseModel, mainActor);
                    log.append("Generated : " + subIdealModels.size() + " sub-ideal models!" + newline);
                    log.append("Ranking sub-ideal models " + newline + "\tbased on average loss for \"" + selectedActorString + "\"" + newline + "\t and on average \u0394gain of the other actors in the model " + newline + "\twhen \"" + selectedNeedString + "\" " + "\toccurs " + startValue + " to " + endValue + " times..." + newline);
                    java.util.List<E3Model> sortedSubIdealModels = ModelRanker.sortByLossandGain(baseModel, subIdealModels, selectedActor, selectedNeed, startValue, endValue, false);
                    log.append("Ranking complete! Results displayed below" + newline);
                    int i = 0;
                    System.out.println("RANKED LIST OF MODELS BASED ON LOSS FOR " + selectedActorString + " and on \u0394gain of the other actors in the model:\n");
                    subIdealModelList.clear();
                    for (E3Model model : sortedSubIdealModels) {
                        i++;
                        System.out.println(i + ":" + model.getDescription() + "");
                        subIdealModelList.addElement(model);                        
                        for (Resource actor : model.getActors()) {                            
                            System.out.println("\t\tAverage for " + actor.getProperty(E3value.e3_has_name).getString() + "\t = \t" + model.getLastKnownAverages().get(actor) + "( \u0394 " + (model.getLastKnownAverages().get(actor) - baseModel.getLastKnownAverages().get(actor)) + " )");
                        }
                    }

                    list.setSelectedIndex(0);
                    list.setVisibleRowCount(3);
                    revalidate();
                    repaint();
                    super.validate();
                    list.ensureIndexIsVisible(0);
                } else {
                    log.append("Attack generation cancelled!" + newline);
                }

//                //generate graph
//                if (selectedNeedString != null) {
//                    Resource selectedNeed = needsMap.get(selectedNeedString);
//                    log.append("Generating graph where : \"" + selectedNeedString + "\" "+ newline+ "\toccurs " + startValue + " to " + endValue + " times."+ newline);
//                    graph1 = GraphingTool.generateGraph(model, selectedNeed, startValue, endValue, true);//expected graph
//                    graph2 = GraphingTool.generateGraph(model, selectedNeed, startValue, endValue, false);//real graph                    
//                    ChartFrame chartframe1 = new ChartFrame("Ideal results", graph1);
//                    chartframe1.pack();
//                    chartframe1.setLocationByPlatform(true);
//                    chartframe1.setVisible(true);
//                    ChartFrame chartframe2 = new ChartFrame("Non-ideal Results", graph2);
//                    chartframe2.pack();                    
//                    chartframe2.setLocationByPlatform(true);
//                    chartframe2.setVisible(true);
//                } else {
//                    log.append("Graph generation cancelled!" + newline);
//                }
            } else {
                log.append("Load a model file first!" + newline);
            }

        } //Handle the graph generation button
        else if (e.getSource() == graphButton) {
            log.append("Displaying graph..." + newline);
            graph1 = GraphingTool.generateGraph(baseModel, selectedNeed, startValue, endValue, true);//expected graph
            graph2 = GraphingTool.generateGraph((E3Model) list.getSelectedValue(), selectedNeed, startValue, endValue, false);//real graph                    
            ChartFrame chartframe1 = new ChartFrame("Ideal results", graph1);
            chartframe1.pack();
            chartframe1.setLocationByPlatform(true);
            chartframe1.setVisible(true);
            ChartFrame chartframe2 = new ChartFrame("Non-ideal Results", graph2);
            chartframe2.pack();
            chartframe2.setLocationByPlatform(true);
            chartframe2.setVisible(true);
        } 


        //Handle save button action.
        else if (e.getSource()
                == saveButton) {
            JFrame frame = new JFrame("Select graph to save");
            String[] options = {"Expected scenario", "Real Scenario"};
            String selectedGraph = (String) JOptionPane.showInputDialog(frame,
                    "Which graph do you want to save?",
                    "Choose graph to save",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (selectedGraph != null) {
                int returnVal = sfc.showSaveDialog(MainWindow.this);
                if (this.graph1 != null) {
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = sfc.getSelectedFile();
                        if (selectedGraph.equals("Expected scenario")) {
                            log.append("Saving: " + file.getName() + "." + newline);
                            try {
                                GraphingTool.saveToFile(file, graph1);
                            } catch (IOException ex) {
                                log.append("Could not write to file: " + file.getName() + "." + newline);
                            }
                        } else if (selectedGraph.equals("Real Scenario")) {
                            log.append("Saving: " + file.getName() + "." + newline);
                            try {
                                GraphingTool.saveToFile(file, graph1);
                            } catch (IOException ex) {
                                log.append("Could not write to file: " + file.getName() + "." + newline);
                            }
                        }
                    } else {
                        log.append("Save command cancelled by user." + newline);
                    }
                    log.setCaretPosition(log.getDocument().getLength());
                } else {
                    log.append("Create a graph first!" + newline);
                }
            }
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon
            createImageIcon(String path) {
        java.net.URL imgURL = MainWindow.class
                .getResource(path);
        if (imgURL
                != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event dispatch thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("e3fraud");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new MainWindow());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
