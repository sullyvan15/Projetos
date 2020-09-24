/**
 * Truth Table Constructor:
 *     generates truth tables for statements in propositional logic
 * Copyright (C) 2006  Brian S. Borowski
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

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

public class QueueComboBox extends JComboBox {
    private static final long serialVersionUID = 1L;
    private JTextField textEditor;
    private LinkedList<String> queue;
    private int maxLength, caretPosition, keyCode, maxQueueLength;
    private String currentText;

    public QueueComboBox(final QueueComboBoxEditor basicEditor, final int maxQueueLength) {
        super();
        this.maxQueueLength = maxQueueLength;
        currentText = "";
        queue = new LinkedList<String>();
        setEditor(basicEditor);
        setRenderer(new StatementRenderer(this));
        setEditable(true);
        setMaximumRowCount(10);

        addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED)
                    if (textEditor.getSelectedText() == null)
                        textEditor.selectAll();
            }
        });
        textEditor = (JTextField)getEditor().getEditorComponent();
        textEditor.addFocusListener(new FocusAdapter() {
            public void focusGained(final FocusEvent fe) {
                if (textEditor.getSelectedText() == null)
                    textEditor.selectAll();
            }
        });
        textEditor.addKeyListener(new KeyAdapter() {
            public void keyReleased(final KeyEvent e) {
                currentText = getText();
                keyCode = e.getKeyCode();
                caretPosition = textEditor.getCaretPosition();
                if ((keyCode == KeyEvent.VK_LEFT)  || (keyCode == KeyEvent.VK_KP_LEFT)  ||
                    (keyCode == KeyEvent.VK_RIGHT) || (keyCode == KeyEvent.VK_KP_RIGHT) ||
                    (keyCode == KeyEvent.VK_HOME)  || (keyCode == KeyEvent.VK_END)      ||
                    (keyCode == KeyEvent.VK_SHIFT) || (keyCode == KeyEvent.VK_CONTROL)  ||
                    (keyCode == KeyEvent.VK_DOWN)  || (keyCode == KeyEvent.VK_KP_DOWN)  ||
                    (keyCode == KeyEvent.VK_UP)    || (keyCode == KeyEvent.VK_KP_UP)    ||
                    (keyCode == KeyEvent.VK_ENTER)) {
                    return;
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    textEditor.selectAll();
                    return;
                }
                final LinkedList<String> matchings = getMatchingItems(currentText);
                if (matchings.size() > 0) {
                    setModel(new DefaultComboBoxModel(matchings.toArray()));
                    setSelectedIndex(-1);
                    matchings.getFirst();
                    textEditor.setText(currentText);
                    if (caretPosition < currentText.length())
                        textEditor.setCaretPosition(caretPosition);
                    if (currentText.length() < maxLength)
                        showPopup();
                    else
                        hidePopup();
                } else {
                    hidePopup();
                }
            }
        });
    }

    public void hidePopup() {
        if (!isPopupVisible()) return;
        super.hidePopup();
        setModel(new DefaultComboBoxModel(queue.toArray()));
        setText(currentText);
        if (queue.size() > 0)
            setSelectedIndex(0);
        if (caretPosition < currentText.length())
            textEditor.setCaretPosition(caretPosition);
    }

    public void setText(final String text) {
        textEditor.setText(text);
    }

    public void addItem(final String item) {
        queue.remove(item);
        queue.add(0, item);
        final int size = queue.size();
        if (size > maxQueueLength)
            remove(size - 1);
        setModel(new DefaultComboBoxModel(queue.toArray()));
    }

    public String getText() {
        return textEditor.getText();
    }

    public void selectAll() {
        textEditor.selectAll();
    }

    public void select(final int selectionStart, final int selectionEnd) {
        textEditor.select(selectionStart, selectionEnd);
    }

    private LinkedList<String> getMatchingItems(String match) {
        final LinkedList<String> list = new LinkedList<String>();
        if (match.length() == 0)
            return list;
        final ListIterator<String> iterator = queue.listIterator(0);
        String currentText, currentLowerCase;
        match = match.toLowerCase().replaceAll("\\s", "");
        maxLength = 0;
        int currentLength;
        while (iterator.hasNext()) {
            currentText = iterator.next();
            currentLowerCase = currentText.toLowerCase().replaceAll("\\s", "");
            if ((currentLowerCase.startsWith(match)) && (!currentLowerCase.equals(match))) {
                list.add(currentText);
                currentLength = currentText.length();
                if (currentLength > maxLength)
                    maxLength = currentLength;
            }
        }
        return list;
    }
}

class QueueComboBoxEditor extends MetalComboBoxEditor {

    public QueueComboBoxEditor(final int columns, final int maxLength, final KeyListener listener) {
        super();
        editor.setColumns(columns);
        editor.addKeyListener(listener);

        // Limit the length of statements that can be entered
        final Document document = editor.getDocument();
        if (document instanceof AbstractDocument) {
            final AbstractDocument abstractDocument = (AbstractDocument)document;
            abstractDocument.setDocumentFilter(new DocumentSizeFilter(maxLength));
        }
    }

    public String getText() {
        return editor.getText();
    }

    public void setText(final String text) {
        editor.setText(text);
    }

    public void select(final int selectionStart, final int selectionEnd) {
        editor.select(selectionStart, selectionEnd);
    }
}

class StatementRenderer extends JLabel implements ListCellRenderer {
    private static final long serialVersionUID = 1L;
    private final Border
        selectedBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.white, 1),
            BorderFactory.createLineBorder(Color.black, 1)),
        emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private final Color selectedColor = new Color(255, 229, 179);
    private final BasicComboPopup popup;

    public StatementRenderer(final JComboBox comboBox) {
        // Don't paint behind the component
        setOpaque(true);
        popup = (BasicComboPopup)comboBox.getUI().getAccessibleChild(this, 0);
    }

    public Component getListCellRendererComponent(final JList list, final Object value,
            final int index, final boolean isSelected, final boolean cellHasFocus) {
        String statement = (String)value;
        final FontMetrics fontMetrics = getFontMetrics(list.getFont());
        final int statementLength = statement.length(),
            maxCharsInStatement = popup.getWidth() / fontMetrics.charWidth(' ') - 1;
        if (statementLength > maxCharsInStatement) {
            final int difference = statementLength - maxCharsInStatement,
                maxIndex = statementLength - difference - 3;
            if (maxIndex >= 0) {
                statement = statement.substring(0, statementLength - difference - 3);
                statement = statement + "...";
            }
        }
        setText(statement);
        if (isSelected) {
            setBackground(selectedColor);
            setBorder(selectedBorder);
        } else {
            setBackground(Color.white);
            setBorder(emptyBorder);
        }
        return this;
    }
}
