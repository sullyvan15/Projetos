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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ImageIcon imageIcon;

    public ImagePanel(final String filename) {
        this(getImage(filename));
    }

    public ImagePanel(final Image img) {
        this.imageIcon = new ImageIcon(img);
        final Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    public void paintComponent(final Graphics g) {
        g.drawImage(imageIcon.getImage(), 0, 0, null);
    }

    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    public static Image getImage(final String filename) {
        ImageIcon icon;
        final URL url = ImagePanel.class.getResource(filename);
        if (url != null) {
            icon = new ImageIcon(url);
        } else {
            // Read from file.
            icon = new ImageIcon(filename);

            // Try to read from URL.
            if ((icon == null) ||
                (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
                try {
                    icon = new ImageIcon(new URL(filename));
                } catch (final MalformedURLException murle) {
                    // Not a URL.
                    return null;
                }
            }
        }
        return icon.getImage();
    }
}
