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


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Dan
 */
class CustomListCellRenderer extends JPanel implements ListCellRenderer {

    private Color selectionBackground;
    private Color background;

    public CustomListCellRenderer(JList list) {
        selectionBackground = list.getSelectionBackground();
        background = list.getBackground();

        //setPreferredSize(new Dimension(15, 50));
    }

    public Component getListCellRendererComponent(JList list, Object object,
        int index, boolean isSelected, boolean cellHasFocus) {
        //setText(object.toString());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        JLabel left = new JLabel(Integer.toString(index+1));
        left.setFont(new Font("Arial", Font.BOLD, 14));
        JTextArea right = new JTextArea(object.toString());     
        right.setLineWrap(true);
        right.setWrapStyleWord(true);
        
        left.setBackground(isSelected ? selectionBackground : background);
        right.setBackground(isSelected ? selectionBackground : background);
        panel.add(left);
        panel.add(right);    
        return panel; 

       
    }
}
