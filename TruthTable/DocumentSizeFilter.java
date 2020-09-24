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

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DocumentSizeFilter extends DocumentFilter {
    private final int maxCharacters;

    public DocumentSizeFilter(final int maxChars) {
        maxCharacters = maxChars;
    }

    public void insertString(final FilterBypass fb, final int offs, String str,
        final AttributeSet a) throws BadLocationException {

        str = getSingleLineString(str);
        // Rejects the entire insertion if it would make the contents too long.
        if ((fb.getDocument().getLength() + str.length()) <= maxCharacters)
            super.insertString(fb, offs, str, a);
        else
            Toolkit.getDefaultToolkit().beep();
    }

    public void replace(final FilterBypass fb, final int offs, final int length, String str,
        final AttributeSet a) throws BadLocationException {

        str = getSingleLineString(str);
        // Rejects the entire replacement if it would make the contents too long.
        if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters)
            super.replace(fb, offs, length, str, a);
        else
            Toolkit.getDefaultToolkit().beep();
    }

    private String getSingleLineString(String str) {
        // Replace carriage returns and line feeds with regular spaces to
        // prevent the combobox from expanding vertically.
        if (str.indexOf('\n') >= 0 || str.indexOf('\r') >= 0) {
            final StringBuilder builder = new StringBuilder(str);
            char c;
            for (int i = builder.length() - 1; i >= 0; i--) {
                c = builder.charAt(i);
                if ((c == '\n') || (c == '\r'))
                    builder.setCharAt(i, ' ');
            }
            str = builder.toString();
        }
        return str;
    }
}
