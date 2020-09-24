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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class CenterableFrame extends JFrame implements Centerable {
    private static final long serialVersionUID = 1L;

    public CenterableFrame(final String title) {
        super(title);
    }

    public void setCenter(final Container me, final JFrame parent) {
        final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        final int screenWidth = d.getSize().width,
            screenHeight = d.getSize().height - 27,
            parentWidth = parent.getSize().width,
            parentHeight = parent.getSize().height,
            w = me.getSize().width,
            h = me.getSize().height;
        final Point p = parent.getLocation(), setp = parent.getLocation();
        if (p.x < 0)
            setp.x = (parentWidth + p.x - w) / 2;
        else if (p.x + parentWidth > screenWidth)
            setp.x = p.x + (screenWidth - p.x - w) / 2;
        else
            setp.x = p.x + (parentWidth - w) / 2;

        if (p.y < 0)
            setp.y = (parentHeight + p.y - h) / 2;
        else if (p.y + parentHeight > screenHeight)
            setp.y = p.y + (screenHeight - p.y - h) / 2;
        else
            setp.y = p.y + (parentHeight - h) / 2;

        if (setp.x < 0)
            setp.x = 0;
        else if (setp.x + w > screenWidth)
            setp.x = screenWidth - w;

        if (setp.y < 0)
            setp.y = 0;
        else if (p.y + h > screenHeight)
            setp.y = screenHeight - h;
        setLocation(setp);
    }
}
