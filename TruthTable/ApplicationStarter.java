/**
 * Truth Table Constructor:
 *     generates truth tables for statements in propositional logic
 * Copyright (C) 2006, 2010, 2011  Brian S. Borowski
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Email: brian_borowski AT yahoo DOT com
 */

/**
 * Class that allows the application to run from an applet.
 */
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;

public class ApplicationStarter extends JApplet implements ActionListener {
    private static final long serialVersionUID = 1L;
    private GUI gui = null;

    public void init() {
        final JButton startButton = new JButton(
            "Start Truth Table Constructor");
        startButton.setMnemonic('S');
        final Container contentPane = getContentPane();
        contentPane.setBackground(
            getBackgroundColor(getParameter("background")));
        contentPane.setLayout(new FlowLayout());
        contentPane.add(startButton);
        startButton.addActionListener(this);
    }

    public void close() {
        gui.stop();
        gui.setVisible(false);
        gui.dispose();
        gui = null;
    }

    public void actionPerformed(final ActionEvent ae) {
        if (gui == null) {
            gui = new GUI(this);
        } else {
            gui.toFront();
        }
    }

    private Color getBackgroundColor(final String background) {
        if ((background == null) || (background.length() != 7) ||
            (background.charAt(0) != '#')) {
            return Color.white;
        } else {
            String hexcolor, red, green, blue;
            hexcolor = background.substring(1, background.length());
            red = hexcolor.substring(0, 2);
            green = hexcolor.substring(2, 4);
            blue = hexcolor.substring(4, 6);
            return new Color(Integer.parseInt(red, 16),
                             Integer.parseInt(green, 16),
                             Integer.parseInt(blue, 16));
        }
    }
}
