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

import java.applet.AudioClip;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JApplet;
import javax.swing.JOptionPane;

public class AudioOptionPane {

    public static void showMessageDialog(
            final Component parentComponent,
            final Object message,
            final String title,
            final int messageType) {

        play("audio/notification.wav");
        JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
    }

    public static int showOptionDialog(
            final Component parentComponent,
            final Object message,
            final String title,
            final int optionType,
            final int messageType,
            final Icon icon,
            final Object[] options,
            final Object initialValue) {

        play("audio/notification.wav");
        return JOptionPane.showOptionDialog(parentComponent, message, title, messageType, messageType, icon, options, initialValue);
    }

    public static int showConfirmDialog(
            final Component parentComponent,
            final Object message,
            final String title,
            final int optionType) {

        play("audio/notification.wav");
        return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
    }

    private static void play(final String filename) {
        URL url = null;

        url = AudioOptionPane.class.getResource(filename);
        if (url == null) {
            try {
                final File file = new File(filename);
                if (file.canRead()) url = file.toURI().toURL();
            } catch (final IllegalArgumentException iae) {
                // URL is not absolute.
            } catch (final MalformedURLException murle) {
                // Not a URL.
            }
        }

        if (url == null) {
            System.err.println("Audio file " + filename + " not found.");
            return;
        }
        final AudioClip clip = JApplet.newAudioClip(url);
        clip.play();
    }
}
