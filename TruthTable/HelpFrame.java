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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Class that implements a non-modal help dialog box.
 */
public class HelpFrame extends CenterableFrame {
    private static final long serialVersionUID = 1L;
    private static final String helpDirectory = "help/";

    public HelpFrame(final JFrame parent, final String title, final String indexFile,
        final JApplet applet, final int width, final int height, final int minWidth, final int minHeight) {

        super(title + " Help");
        setBackground(parent.getBackground());
        addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent e) {
                int width = getWidth(),
                    height = getHeight();
                if (width < minWidth)
                    width = minWidth;
                if (height < minHeight)
                    height = minHeight;
                setSize(width, height);
            }
        });

        final JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        editorPane.setPage(e.getURL());
                    } catch (final IOException ioe) {
                        AudioOptionPane.showMessageDialog(parent,
                            "An error occurred while loading '" + e.getURL() + "'.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        try {
            final URL url = getClass().getResource(helpDirectory + indexFile);
            // Workaround for bug "HTMLEditorKit throws NullPointerException when reloaded" in 1.6.0_22
            ParserDelegator workaround = new ParserDelegator();
            // End workaround
            editorPane.setPage(url);
        } catch (final IOException ioe) {
            AudioOptionPane.showMessageDialog(parent,
                "An error occurred while loading '" + helpDirectory + indexFile + "'.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        final JScrollPane scrollPane = new JScrollPane(editorPane,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add("Center", scrollPane);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                e.getWindow().setVisible(false);
                e.getWindow().dispose();
            }
        });

        setSize(width, height);
        setCenter(this, parent);
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        dispose();
    }
}
