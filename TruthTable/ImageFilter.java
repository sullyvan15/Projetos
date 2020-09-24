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

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImageFilter extends FileFilter implements Filterable {

    private final String imageType, imageExtension;

    public ImageFilter(final String imageType, final String imageExtension) {
        super();
        this.imageType = imageType;
        this.imageExtension = imageExtension;
    }

    public boolean accept(final File f) {
        boolean accept = f.isDirectory();

        if (!accept) {
            final String suffix = getSuffix(f);
            if (suffix != null)
                accept = suffix.equals(imageExtension);
        }
        return accept;
    }

    public String getDescription() {
        return imageType + " Files (*." + imageExtension + ")";
    }

    public String getExtension() {
        return imageExtension;
    }

    private String getSuffix(final File f) {
        final String s = f.getPath();
        String suffix = null;
        final int i = s.lastIndexOf(".");
        if ((i > 0) && (i < s.length()-1))
            suffix = s.substring(i+1).toLowerCase();
        return suffix;
    }
}
