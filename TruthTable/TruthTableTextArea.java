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

import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JTextArea;

import logic.TruthTable;
import logic.TruthValue;

public class TruthTableTextArea extends JTextArea implements Configurable {
    private static final long serialVersionUID = 1L;
    private int fontSize = DEFAULT_FONT_SIZE;
    private final int fontStyle = Font.PLAIN, maxLines;
    private int currentRow, currentColumn;
    private String[] columnOrderString;
    private boolean isMainColumnHighlighted, areRowNumbersShown,
                    areColumnNumbersShown, resetCaretToTop;
    private TruthTable truthTable;

    public TruthTableTextArea(final int maxLines) {
        this.maxLines = maxLines;
        this.columnOrderString = null;
        setFont(new Font(Font.MONOSPACED, fontStyle, this.fontSize));
    }

    public TruthTableTextArea(final TruthTable truthTable, final int maxLines) {
        this(maxLines);
        setTruthTable(truthTable, true);
    }

    public void setTruthTable(final TruthTable truthTable, final boolean resetCaretToTop) {
        this.columnOrderString = null;
        this.truthTable = truthTable;
        this.resetCaretToTop = resetCaretToTop;
    }

    public boolean setCurrentRow(final int currentRow) {
        if ((truthTable == null) && (currentRow >= 0)) {
            this.currentRow = -1;
            return false;
        } else {
            final int numberOfLines = truthTable.getNumberOfLines();
            if (currentRow >= numberOfLines) {
                this.currentRow = numberOfLines - 1;
                return false;
            } else {
                this.currentRow = currentRow;
                return true;
            }
        }
    }

    public boolean setCurrentColumn(final int currentColumn) {
        if ((truthTable == null) && (currentColumn >= 0)) {
            this.currentColumn = -1;
            return false;
        } else {
            final int numberOfColumns = truthTable.getNumberOfColumns();
            if (currentColumn >= numberOfColumns) {
                this.currentColumn = numberOfColumns - 1;
                return false;
            } else {
                this.currentColumn = currentColumn;
                return true;
            }
        }
    }

    public void redraw() {
        if ((truthTable == null) || (truthTable.getNumberOfLines() > maxLines)) {
            setText("");
            return;
        }
        final StringBuilder builder = new StringBuilder();
        final int numberOfCharsInMaxLine = getNumberOfCharsInMaxLine();

        if (isMainColumnHighlighted) {
            getHighlightedLine(numberOfCharsInMaxLine, builder);
            builder.append("\n");
        }

        padLeftMargin(builder, numberOfCharsInMaxLine);
        builder.append(truthTable.getHeader(true));
        builder.append("\n");
        padLeftMargin(builder, numberOfCharsInMaxLine);
        builder.append(truthTable.getHeaderSeparator());
        builder.append("\n");

        final int numberOfLines = truthTable.getNumberOfLines(),
            displayMethod = truthTable.getDisplayMethod(),
            columnInfoHeight = truthTable.getColumnInfoHeight();
        for (int i = 0; i < numberOfLines; i++) {
            getLine(i, displayMethod, numberOfLines, numberOfCharsInMaxLine,
                builder);
            if (i != numberOfLines - 1)
                builder.append("\n");
        }
        if (areColumnNumbersShown) {
            builder.append("\n");
            int i = 0;
            while (getColumnOrderLine(i, numberOfCharsInMaxLine, builder)) {
                if (i != columnInfoHeight - 1)
                    builder.append("\n");
                i++;
            }
        }

        setText(builder.toString());
        if (resetCaretToTop) {
            setCaretPosition(0);
            resetCaretToTop = false;
        }
    }

    public void saveTableToFile(final String filename) throws IOException {
        final FileWriter fileWriter = new FileWriter(filename);
        final BufferedWriter builderedWriter = new BufferedWriter(fileWriter);
        final PrintWriter printWriter = new PrintWriter(builderedWriter, true);
        StringBuilder builder = new StringBuilder();
        final int numberOfCharsInMaxLine = getNumberOfCharsInMaxLine();

        if (isMainColumnHighlighted) {
            getHighlightedLine(numberOfCharsInMaxLine, builder);
            printWriter.println(builder.toString());
        }

        builder = new StringBuilder();
        padLeftMargin(builder, numberOfCharsInMaxLine);
        builder.append(truthTable.getHeader(true));
        printWriter.println(builder.toString());
        builder = new StringBuilder();
        padLeftMargin(builder, numberOfCharsInMaxLine);
        builder.append(truthTable.getHeaderSeparator());
        printWriter.println(builder.toString());

        final int numberOfLines = truthTable.getNumberOfLines(),
            displayMethod = truthTable.getDisplayMethod(),
            columnInfoHeight = truthTable.getColumnInfoHeight();
        for (int i = 0; i < numberOfLines; i++) {
            builder = new StringBuilder();
            getLine(i, displayMethod, numberOfLines, numberOfCharsInMaxLine,
                builder);
            if (i != numberOfLines - 1)
                printWriter.println(builder.toString());
            else
                printWriter.print(builder.toString());

        }
        if (areColumnNumbersShown) {
            printWriter.println();
            int i = 0;
            builder = new StringBuilder();
            while (getColumnOrderLine(i, numberOfCharsInMaxLine, builder)) {
                if (i != columnInfoHeight - 1)
                    printWriter.println(builder.toString());
                else
                    printWriter.print(builder.toString());
                i++;
                builder = new StringBuilder();
            }
        }
        printWriter.close();
        builderedWriter.close();
        fileWriter.close();
    }

    public void increaseFontSize() {
        if (fontSize < MAX_FONT_SIZE) {
            this.fontSize += 2;
            setFont(new Font(Font.MONOSPACED, fontStyle, this.fontSize));
        }
    }

    public void decreaseFontSize() {
        if (fontSize > MIN_FONT_SIZE) {
            this.fontSize -= 2;
            setFont(new Font(Font.MONOSPACED, fontStyle, this.fontSize));
        }
    }

    public void resetDefaultFontSize() {
        this.fontSize = DEFAULT_FONT_SIZE;
        setFont(new Font(Font.MONOSPACED, fontStyle, this.fontSize));
    }

    public void setHighlightMainColumn(final boolean isMainColumnHighlighted) {
        this.isMainColumnHighlighted = isMainColumnHighlighted;
    }

    public boolean getIsMainColumnHighlighted() {
        return isMainColumnHighlighted;
    }

    public void setShowRowNumbers(final boolean areRowNumbersShown) {
        this.areRowNumbersShown = areRowNumbersShown;
    }

    public void setShowColumnNumbers(final boolean areColumnNumbersShown) {
        this.areColumnNumbersShown = areColumnNumbersShown;
    }

    public boolean getAreColumnNumbersShown() {
        return areColumnNumbersShown;
    }

    public int getNumberOfCharsInMaxLine() {
        final String str = String.valueOf(truthTable.getNumberOfLines() - 1);
        return str.length();
    }

    public void padLeftMargin(final StringBuilder builder, final int numberOfChars) {
        if (areRowNumbersShown) {
            for (int i = 0; i < numberOfChars + 1; i++)
                builder.append(" ");
        }
    }

    public void getHighlightedLine(final int numberOfCharsInMaxLine, final StringBuilder builder) {
        padLeftMargin(builder, numberOfCharsInMaxLine);
        final int numberOfPropositions = truthTable.getNumberOfPropositions();
        for (int i = 0; i < numberOfPropositions; i++)
            builder.append("    ");
        builder.append(" ");
        final int mainColumnPosition =
            truthTable.getPositionOfMainColumn();
        for (int i = 0; i < mainColumnPosition; i++)
            builder.append(" ");
        builder.append("*");
        for (int i = mainColumnPosition;
             i < truthTable.getStatement().length(); i++)
            builder.append(" ");
    }

    public void getLine(final int i, final int displayMethod, final int numberOfLines,
        final int numberOfCharsInMaxLine, final StringBuilder builder) {
        int interpretedPosition;
        if (displayMethod == TruthValue.TRUE_FALSE)
            interpretedPosition = numberOfLines - i - 1;
        else
            interpretedPosition = i;
        if (areRowNumbersShown) {
            final String str = String.valueOf(i);
            final int strLength = str.length();
            for (int j = 0; j < numberOfCharsInMaxLine - strLength; j++)
                builder.append(" ");
            builder.append(i);
            builder.append(")");
        }
        final boolean[] binaryPropositionValues =
            truthTable.getBinaryFormat(interpretedPosition);
        final int binaryLength = binaryPropositionValues.length;
        for (int j = 0; j < binaryLength; j++) {
            builder.append(" ");
            builder.append(
                TruthValue.getTruthValueString(binaryPropositionValues[j],
                displayMethod));
            builder.append(" |");
        }
        if (i <= currentRow) {
            builder.append(" ");
            String rowString;
            if (currentColumn == Integer.MAX_VALUE)
                rowString = truthTable.computeRow(interpretedPosition);
            else
                rowString = truthTable.computeRow(interpretedPosition, currentColumn);
            builder.append(rowString);
            builder.append(" ");
        }
    }

    public boolean getColumnOrderLine(final int lineNumber, final int numberOfCharsInMaxLine, final StringBuilder builder) {
        columnOrderString = truthTable.getColumnOrderStrings(currentColumn);
        if ((columnOrderString == null) || (lineNumber >= columnOrderString.length))
            return false;
        padLeftMargin(builder, numberOfCharsInMaxLine);
        final int numberOfPropositions = truthTable.getNumberOfPropositions();
        for (int i = 0; i < numberOfPropositions; i++)
            builder.append("    ");
        builder.append(" ");
        builder.append(columnOrderString[lineNumber]);
        return true;
    }
}
