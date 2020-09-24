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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import logic.TruthTable;
import logic.TruthValue;

public class TruthTablePanel extends JComponent implements Configurable {
    private static final long serialVersionUID = 1L;
    private final int fontStyle = Font.BOLD;
    private String[] columnOrderString;
    private Font font;
    private int fontSize = DEFAULT_FONT_SIZE, height, width, charWidth,
            charHeight, tableWidth,  xOffset, yOffset, numberOfLines,
            numberOfPropositions, separatorYValue, evaluationXValue,
            numberOfCharsInMaxLine, maxCharDescent, reductionValue, currentRow,
            currentColumn;
    private boolean isMainColumnHighlighted, areRowNumbersShown,
            areColumnNumbersShown, areAlternateRowsInColor;
    private String tableHeader;
    private TruthTable truthTable;
    private Rectangle clipRect;
    private final Color rowColor = new Color(195, 225, 253);

    public TruthTablePanel() {
        font = FontManager.getFont(fontStyle, this.fontSize);
        clipRect = new Rectangle();
        reductionValue = 5;
    }

    public TruthTablePanel(final TruthTable truthTable) {
        setTruthTable(truthTable);
    }

    public void setTruthTable(final TruthTable truthTable) {
        this.truthTable = truthTable;
        computeValuesForPainting();
    }

    public int getCanvasHeight() {
        return height;
    }

    public int getCanvasWidth() {
        return width;
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

    public void increaseFontSize() {
        if (fontSize < MAX_FONT_SIZE) {
            this.fontSize += 2;
            font = FontManager.getFont(fontStyle, this.fontSize);
            computeValuesForPainting();
        }
    }

    public void decreaseFontSize() {
        if (fontSize > MIN_FONT_SIZE) {
            this.fontSize -= 2;
            font = FontManager.getFont(fontStyle, this.fontSize);
            computeValuesForPainting();
        }
    }

    public void resetDefaultFontSize() {
        this.fontSize = DEFAULT_FONT_SIZE;
        font = FontManager.getFont(fontStyle, this.fontSize);
        computeValuesForPainting();
    }

    public void setHighlightMainColumn(final boolean isMainColumnHighlighted) {
        this.isMainColumnHighlighted = isMainColumnHighlighted;
    }

    public void setShowRowNumbers(final boolean areRowNumbersShown) {
        this.areRowNumbersShown = areRowNumbersShown;
        computeValuesForPainting();
    }

    public void setShowColumnNumbers(final boolean areColumnNumbersShown) {
        this.areColumnNumbersShown = areColumnNumbersShown;
        computeValuesForPainting();
    }

    public void setShowAlternateRowsInColor(final boolean areAlternateRowsInColor) {
        this.areAlternateRowsInColor = areAlternateRowsInColor;
    }

    public Dimension getPreferredSize() {
        if (truthTable != null) {
            final FontMetrics fontMetrics = getFontMetrics(font);
            tableWidth = fontMetrics.stringWidth(tableHeader);
            charWidth = fontMetrics.charWidth(' ');
            charHeight = fontMetrics.getHeight() + 1;
            maxCharDescent = fontMetrics.getMaxDescent();
            xOffset = charWidth/2;
            yOffset = charHeight/2;
            int rowDimension = tableHeader.length() * charWidth + 2 * xOffset,
                colDimension = (numberOfLines + 1) * charHeight + yOffset;
            if (areRowNumbersShown) {
                rowDimension = (tableHeader.length() + getNumberOfCharsInMaxLine() + 1) *
                               charWidth + 2 * xOffset;
            }
            if (areColumnNumbersShown) {
                columnOrderString = truthTable.getColumnOrderStrings(Integer.MAX_VALUE);
                colDimension = (numberOfLines + 1) * charHeight + yOffset +
                               columnOrderString.length * (charHeight - reductionValue);
            }
            return new Dimension(rowDimension, colDimension);
        } else {
            return new Dimension(0, 0);
        }
    }

    public void paintComponent(final Graphics graphics) {
        StringBuilder builder;
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        if (truthTable != null) {
            int xPosition = 3,
                xValue,
                mainColumnPosition = -1;
            final int displayMethod = truthTable.getDisplayMethod();
            int leftOffset = xOffset, interpretedPosition;

            if (areRowNumbersShown) {
                leftOffset += (charWidth * (numberOfCharsInMaxLine + 1));
            }

            graphics.setColor(Color.black);
            graphics.setFont(font);
            graphics.drawString(tableHeader, leftOffset, charHeight);
            graphics.getClipBounds(clipRect);

            // Determine which rows needs to be painted.
            int startRow = (clipRect.y - maxCharDescent) / charHeight - 1,
                endRow = (clipRect.y + clipRect.height) / charHeight - 1;
            if (startRow < 0) startRow = 0;
            if (endRow >= numberOfLines) endRow = numberOfLines - 1;

            int i = startRow;
            for (i = startRow; i <= endRow; i++) {
                if (displayMethod == TruthValue.TRUE_FALSE) {
                    interpretedPosition = numberOfLines - i - 1;
                } else {
                    interpretedPosition = i;
                }
                final boolean[] binaryPropositionValues = truthTable.getBinaryFormat(interpretedPosition);
                final int binaryLength = binaryPropositionValues.length,
                    iPlus2 = i + 2;

                builder = new StringBuilder();
                if (areRowNumbersShown) {
                    final String str = String.valueOf(i);
                    final int strLength = str.length();
                    for (int j = 0; j < numberOfCharsInMaxLine - strLength; j++) {
                        builder.append(" ");
                    }
                    builder.append(i);
                    builder.append(")");
                    graphics.setColor(Color.GRAY);
                    graphics.drawString(builder.toString(), xOffset, charHeight * iPlus2);
                    graphics.setColor(Color.black);
                }

                // Alternate row color for ease of viewing.
                if ((areAlternateRowsInColor) && (i % 2 != 0)) {
                    graphics.setColor(rowColor);
                    graphics.fillRect(leftOffset,
                        charHeight * i + separatorYValue + 1, tableWidth + 1,
                        charHeight);
                    graphics.setColor(Color.black);
                }

                builder = new StringBuilder();
                for (int j = 0; j < binaryLength; j++) {
                    builder.append(" ");
                    builder.append(TruthValue.getTruthValueString(binaryPropositionValues[j], displayMethod));
                    builder.append(" ");
                }
                graphics.drawString(builder.toString(), leftOffset, charHeight * iPlus2);

                if (i <= currentRow) {
                    String rowString;
                    if (currentColumn == Integer.MAX_VALUE) {
                        rowString = truthTable.computeRow(interpretedPosition);
                    } else {
                        rowString = truthTable.computeRow(interpretedPosition, currentColumn);
                    }
                    xValue = evaluationXValue;
                    if (isMainColumnHighlighted) {
                        String substring;
                        if (mainColumnPosition == -1) {
                            mainColumnPosition = truthTable.getPositionOfMainColumn();
                        }
                        if (mainColumnPosition != 0) {
                            substring = rowString.substring(0, mainColumnPosition);
                            graphics.drawString(substring, xValue, charHeight * iPlus2);
                            xValue += charWidth * substring.length();
                        }

                        substring = rowString.substring(mainColumnPosition, mainColumnPosition + 1);
                        graphics.setColor(Color.red);
                        graphics.drawString(substring, xValue, charHeight * iPlus2);

                        xValue += charWidth * substring.length();
                        substring = rowString.substring(mainColumnPosition + 1);
                        graphics.setColor(Color.black);
                        graphics.drawString(substring, xValue, charHeight * iPlus2);
                    } else {
                        graphics.drawString(rowString, xValue, charHeight * iPlus2);
                    }
                }
            }
            if (areColumnNumbersShown) {
                graphics.setColor(Color.gray);
                xValue = evaluationXValue;
                final int stopOffPoint = charHeight * (i + 2),
                    reducedCharHeight = charHeight - reductionValue;
                columnOrderString = truthTable.getColumnOrderStrings(currentColumn);
                for (int k = 0; k < columnOrderString.length; k++) {
                    graphics.drawString(columnOrderString[k], xValue, stopOffPoint + reducedCharHeight * k);
                }
            }

            graphics.setColor(Color.blue);
            graphics.drawLine(leftOffset, separatorYValue, tableWidth + leftOffset, separatorYValue);
            int extension = charHeight;
            if (areAlternateRowsInColor) extension = separatorYValue;
            for (i = 0; i < numberOfPropositions; i++) {
                xValue = charWidth * xPosition + (leftOffset - 1);
                graphics.drawLine(xValue, yOffset, xValue, charHeight * (numberOfLines) + extension);
                xPosition += 3;
            }
        }
    }

    public void saveContentsAsImage(final String filename, final String imageType, final BufferedImage image)
        throws IOException, OutOfMemoryError {
        final Graphics2D g2 = image.createGraphics();
        this.paint(g2);
        g2.dispose();
        ImageIO.write(image, imageType, new File(filename));
    }

    private void computeValuesForPainting() {
        if (truthTable != null) {
            numberOfLines = truthTable.getNumberOfLines();
            numberOfPropositions = truthTable.getNumberOfPropositions();
            tableHeader = truthTable.getHeader(false);
            final Dimension canvasSize = getPreferredSize();
            height = canvasSize.height;
            width = canvasSize.width;
            setPreferredSize(canvasSize);
            numberOfCharsInMaxLine = getNumberOfCharsInMaxLine();
            separatorYValue = (2 * charHeight + yOffset) / 2;
            evaluationXValue = (numberOfPropositions * 3 + 1) * charWidth + xOffset;
            if (areRowNumbersShown) {
                evaluationXValue += (charWidth * (numberOfCharsInMaxLine + 1));
            }
        }
    }

    private int getNumberOfCharsInMaxLine() {
        final String str = String.valueOf(truthTable.getNumberOfLines() - 1);
        return str.length();
    }
}
