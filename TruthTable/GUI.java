/**
 * Truth Table Constructor:
 *     generates truth tables for statements in propositional logic
 * Copyright (C) 2006, 2010-2012  Brian S. Borowski
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
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import logic.Parser;
import logic.ParserException;
import logic.Scanner;
import logic.ScannerException;
import logic.TruthTable;
import logic.TruthValue;

public class GUI extends JFrame implements Runnable {
    public static final int COMPLETE_METHOD = 0,
                            ROW_METHOD = 1,
                            COLUMN_METHOD = 2;
    private static final long serialVersionUID = 1L;

    private final ApplicationStarter applicationStarter;
    private volatile Thread evaluateThread;
    private volatile FileSaveThread fileSaveThread;

    private final JButton constructButton, firstButton, previousButton, nextButton,
                  lastButton;
    private JLabel evaluationLabel, statusLabel, evaluationStatsLabel;
    private JMenu viewMenu, exportMenu;
    private JMenuItem exportTextMenuItem;
    private JRadioButtonMenuItem graphicItem, textItem;
    private JCheckBoxMenuItem highlightItem, numberRowsItem, numberColumnsItem,
                              removeParensItem, alternateColorsItem,
                              alphabetizePropositionsItem;
    private final JComboBox computationMethodComboBox;
    private final JPanel statusPanel;
    private final JProgressBar progressBar;
    private final JScrollPane scrollPane;
    private String lastStatement;
    private File currentDirectory;
    private final Cursor defaultCursor, waitCursor;
    private final Timer timer;
    private final ResourceBundle bundle;
    private final TruthTableTextArea truthTableTextArea;
    private final TruthTablePanel truthTablePanel;
    private TruthTable truthTable;
    private final QueueComboBox statementComboBox;
    private int outputMode, cachedEvaluation, computationMethod,
                currentRow, currentColumn, currentIteration;

    private final int smallTableLimit, maxStatementLength, maxRowsInTextTable;
    private final boolean hasFullPermission;
    private final boolean[] buttonStates;
    private final String[] methodNames, supportedImageTypes, supportedImageExtensions;
    private HelpFrame helpFrame;

    public GUI(final ApplicationStarter appStarter) {
        super();
        bundle = ResourceBundle.getBundle("application", Locale.US);
        setTitle(bundle.getString("APPLICATION_NAME"));
        this.applicationStarter = appStarter;
        outputMode = TruthValue.TRUE_FALSE;
        computationMethod = COMPLETE_METHOD;
        hasFullPermission = getHasFullPermission();
        smallTableLimit = Integer.parseInt(bundle.getString("SMALL_TABLE_LIMIT"));
        maxStatementLength = Integer.parseInt(bundle.getString("MAX_STATEMENT_LENGTH"));
        maxRowsInTextTable = Integer.parseInt(bundle.getString("MAX_ROWS_IN_TEXT_TABLE"));
        int arraySize = Integer.parseInt(bundle.getString("NUMBER_OF_METHODS"));
        methodNames = new String[arraySize];
        for (int i = 0; i < arraySize; i++)
            methodNames[i] = bundle.getString("METHOD_NAME_" + i);
        arraySize = Integer.parseInt(bundle.getString("NUMBER_OF_IMAGE_TYPES"));
        supportedImageTypes = new String[arraySize];
        supportedImageExtensions = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            supportedImageTypes[i] = bundle.getString("IMAGE_TYPE_" + i);
            supportedImageExtensions[i] = bundle.getString("IMAGE_EXTENSION_" + i);
        }
        buttonStates = new boolean[4];
        final GridBagConstraints gbc = new GridBagConstraints();
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        waitCursor = new Cursor(Cursor.WAIT_CURSOR);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new ClosingWindowListener(this));

        final AbstractAction escapeEvaluationAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent e) {
                if ((evaluateThread != null) && (evaluateThread.isAlive())) {
                    evaluateThread = null;
                    cachedEvaluation = TruthTable.UNDEFINED;
                }
                if ((fileSaveThread != null) && (fileSaveThread.isAlive())) {
                    fileSaveThread = null;
                }
            }
        };
        final InputMap inputMap =
            getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        getRootPane().getActionMap().put("escape", escapeEvaluationAction);

        setJMenuBar(getCreatedMenuBar());
        truthTableTextArea = new TruthTableTextArea(maxRowsInTextTable);
        truthTableTextArea.setEditable(false);

        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        final QueueComboBoxEditor basicEditor = new QueueComboBoxEditor(
            30, maxStatementLength, new StatementKeyListener());
        statementComboBox = new QueueComboBox(basicEditor, 25);
        statementComboBox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        ImagePanel imagePanel = new ImagePanel("images/construct.gif");

        constructButton = new JButton(imagePanel.getImageIcon());
        constructButton.addActionListener(new ConstructActionListener());
        constructButton.setFocusPainted(false);
        constructButton.setToolTipText("Construct truth table");

        imagePanel = new ImagePanel("images/first.gif");
        firstButton = new JButton(imagePanel.getImageIcon());
        firstButton.setFocusPainted(false);
        firstButton.setToolTipText("No computations");
        firstButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (computationMethod == ROW_METHOD) {
                    currentRow = -1;
                    truthTablePanel.setCurrentRow(currentRow);
                    truthTableTextArea.setCurrentRow(currentRow);
                    redrawTruthTableTextArea();
                    repaintViewport();
                } else if (computationMethod == COLUMN_METHOD) {
                    currentColumn = -1;
                    truthTablePanel.setCurrentColumn(currentColumn);
                    truthTableTextArea.setCurrentColumn(currentColumn);
                    redrawTruthTableTextArea();
                    repaintViewport();
                }
                setForwardButtonsEnabled(true);
                setBackwardButtonsEnabled(false);
            }
        });

        imagePanel = new ImagePanel("images/previous.gif");
        previousButton = new JButton(imagePanel.getImageIcon());
        previousButton.setFocusPainted(false);
        previousButton.setToolTipText("Previous computation");
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                if (computationMethod == ROW_METHOD) {
                    currentRow -= 1;
                    truthTablePanel.setCurrentRow(currentRow);
                    truthTableTextArea.setCurrentRow(currentRow);
                    redrawTruthTableTextArea();
                    repaintViewport();
                    if (currentRow == -1)
                        setBackwardButtonsEnabled(false);
                    if (currentRow == truthTable.getNumberOfLines() - 2)
                        setForwardButtonsEnabled(true);
                } else if (computationMethod == COLUMN_METHOD) {
                    currentColumn -= 1;
                    truthTablePanel.setCurrentColumn(currentColumn);
                    truthTableTextArea.setCurrentColumn(currentColumn);
                    redrawTruthTableTextArea();
                    repaintViewport();
                    if (currentColumn == -1)
                        setBackwardButtonsEnabled(false);
                    if (currentColumn == truthTable.getNumberOfColumns() - 2)
                        setForwardButtonsEnabled(true);
                }
            }
        });

        imagePanel = new ImagePanel("images/next.gif");
        nextButton = new JButton(imagePanel.getImageIcon());
        nextButton.setFocusPainted(false);
        nextButton.setToolTipText("Next computation");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                if (computationMethod == ROW_METHOD) {
                    currentRow += 1;
                    truthTablePanel.setCurrentRow(currentRow);
                    truthTableTextArea.setCurrentRow(currentRow);
                    redrawTruthTableTextArea();
                    repaintViewport();
                    if (currentRow == truthTable.getNumberOfLines() - 1)
                        setForwardButtonsEnabled(false);
                    if (currentRow == 0)
                        setBackwardButtonsEnabled(true);
                } else if (computationMethod == COLUMN_METHOD) {
                    currentColumn += 1;
                    truthTablePanel.setCurrentColumn(currentColumn);
                    truthTableTextArea.setCurrentColumn(currentColumn);
                    redrawTruthTableTextArea();
                    repaintViewport();
                    if (currentColumn == truthTable.getNumberOfColumns() - 1)
                        setForwardButtonsEnabled(false);
                    if (currentColumn == 0)
                        setBackwardButtonsEnabled(true);
                }
            }
        });

        imagePanel = new ImagePanel("images/last.gif");
        lastButton = new JButton(imagePanel.getImageIcon());
        lastButton.setFocusPainted(false);
        lastButton.setToolTipText("All computations");
        lastButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                if (computationMethod == ROW_METHOD) {
                    currentRow = truthTable.getNumberOfLines() - 1;
                    truthTablePanel.setCurrentRow(currentRow);
                    truthTableTextArea.setCurrentRow(currentRow);
                    redrawTruthTableTextArea();
                    repaintViewport();
                } else if (computationMethod == COLUMN_METHOD) {
                    currentColumn = truthTable.getNumberOfColumns() - 1;
                    truthTablePanel.setCurrentColumn(currentColumn);
                    truthTableTextArea.setCurrentColumn(currentColumn);
                    redrawTruthTableTextArea();
                    repaintViewport();
                }
                setForwardButtonsEnabled(false);
                setBackwardButtonsEnabled(true);
            }
        });

        computationMethodComboBox = new JComboBox();
        computationMethodComboBox.addItem(methodNames[COMPLETE_METHOD]);
        computationMethodComboBox.addItem(methodNames[ROW_METHOD]);
        computationMethodComboBox.addItem(methodNames[COLUMN_METHOD]);
        computationMethodComboBox.setEditable(false);
        computationMethodComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    computationMethod = computationMethodComboBox.getSelectedIndex();
                    if (truthTable != null) {
                        setComputationMethod(computationMethod);
                        redrawTruthTableTextArea();
                        repaintViewport();
                    }
                }
            }
        });

        final JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new GridBagLayout());
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        final JLabel statementLabel = new JLabel("Statement:");
        statementLabel.setDisplayedMnemonic('S');
        statementLabel.setLabelFor(statementComboBox);
        toolBar.add(statementLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 5, 0, 0);
        toolBar.add(statementComboBox, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        toolBar.add(constructButton, gbc);
        toolBar.addSeparator();
        gbc.insets = new Insets(0, 0, 0, 0);
        final JLabel methodLabel = new JLabel("Method:");
        methodLabel.setDisplayedMnemonic('M');
        methodLabel.setLabelFor(computationMethodComboBox);
        toolBar.add(methodLabel, gbc);
        gbc.insets = new Insets(0, 5, 0, 0);
        toolBar.add(computationMethodComboBox, gbc);
        toolBar.add(firstButton, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        toolBar.add(previousButton, gbc);
        toolBar.add(nextButton, gbc);
        toolBar.add(lastButton, gbc);
        contentPane.add(toolBar, BorderLayout.NORTH);

        truthTablePanel = new TruthTablePanel();
        scrollPane = new JScrollPane(truthTablePanel);
        scrollPane.setFocusable(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setPreferredSize(new Dimension(150, 17));

        timer = new Timer((int)javax.management.timer.Timer.ONE_SECOND / 8, new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                if (progressBar.isIndeterminate())
                    progressBar.setValue(progressBar.getValue() + 1);
                else
                    progressBar.setValue(currentIteration);
            }
        });

        statusPanel = new JPanel();
        initializeStatusPanel(false);
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(
                Integer.parseInt(bundle.getString("DEFAULT_WINDOW_WIDTH")),
                Integer.parseInt(bundle.getString("DEFAULT_WINDOW_HEIGHT"))));
        setMinimumSize(new Dimension(
                Integer.parseInt(bundle.getString("MIN_WINDOW_WIDTH")),
                Integer.parseInt(bundle.getString("MIN_WINDOW_HEIGHT"))));
        pack();
        setLocationRelativeTo(null);
        setComputationButtonsEnabled(false);
        setVisible(true);
        statementComboBox.requestFocusInWindow();
    }

    public void stop() {
        evaluateThread = null;
        fileSaveThread = null;
    }

    public void run() {
        if (cachedEvaluation == TruthTable.UNDEFINED) {
            final long startTime = System.currentTimeMillis();
            long endTime;
            final Thread thisThread = Thread.currentThread();
            final int numberOfLines = truthTable.getNumberOfLines(),
                pos = truthTable.getPositionOfMainColumn(),
                upperBound = numberOfLines / 2, oneLess = numberOfLines - 1;
            int result;
            char ch;
            boolean done = false;
            final String row = truthTable.computeRow(0);
            currentIteration = 0;

            ch = row.charAt(pos);
            if (ch == 'T')
                result = TruthTable.TAUTOLOGY;
            else if (ch == '1')
                result = TruthTable.IDENTITY;
            else
                result = TruthTable.CONTRADICTION;

            if (numberOfLines > smallTableLimit) {
                progressBar.setIndeterminate(false);
                progressBar.setString(null);
                progressBar.setMaximum(upperBound);
                initializeStatusPanel(true);
                statusLabel.setForeground(Color.BLACK);
                statusLabel.setText("In progress. Press Escape to abort.");
                evaluationStatsLabel.setText("");
                timer.start();
            }
            while ((evaluateThread == thisThread) && (currentIteration < upperBound)) {
                if ((truthTable.computeRow(currentIteration).charAt(pos) == ch) &&
                    (truthTable.computeRow(oneLess - currentIteration).charAt(pos) == ch))
                    currentIteration += 1;
                else {
                    currentIteration = numberOfLines;
                    done = true;
                    break;
                }
            }
            stopTimer();
            if (evaluateThread == thisThread) {
                if (currentIteration != numberOfLines) currentIteration <<= 1;

                if (!done)
                    cachedEvaluation = result;
                else
                    cachedEvaluation = TruthTable.CONDITIONAL;
                endTime = System.currentTimeMillis();

                if (currentIteration == 0)
                    evaluationStatsLabel.setText("1 row / " + (endTime - startTime)/1000f + " s");
                else
                    evaluationStatsLabel.setText(NumberFormat.getInstance().format(currentIteration) + " rows / " + (endTime - startTime)/1000f + " s");
                statusLabel.setForeground(Color.RED);
                statusLabel.setText(TruthTable.EVALUATION_DEFINITION[cachedEvaluation]);
            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Aborted by user");
            }
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(TruthTable.EVALUATION_DEFINITION[cachedEvaluation]);
        }
        evaluateThread = null;
    }

    private void initializeStatusPanel(final boolean useProgressBar) {
        statusPanel.removeAll();
        statusPanel.setLayout(new GridBagLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 2, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        evaluationLabel = new JLabel("Evaluation: ");
        statusPanel.add(evaluationLabel, gbc);
        statusLabel = new JLabel();
        statusLabel.setForeground(Color.RED);
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        statusPanel.add(statusLabel, gbc);
        final Dimension theSize = new Dimension(5, 27);
        statusPanel.add(new Box.Filler(theSize, theSize, theSize));
        gbc.anchor = GridBagConstraints.EAST;
        evaluationStatsLabel = new JLabel();
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 0, 5, 4);
        if (useProgressBar)
            statusPanel.add(progressBar, gbc);
        else
            statusPanel.add(evaluationStatsLabel, gbc);
        statusPanel.validate();
    }

    private void setComputationMethod(final int computationMethod) {
        switch (computationMethod) {
            case COMPLETE_METHOD:
                currentRow = currentColumn = Integer.MAX_VALUE;
                setComputationButtonsEnabled(false);
                break;
            case ROW_METHOD:
                currentRow = -1;
                currentColumn = Integer.MAX_VALUE;
                setBackwardButtonsEnabled(false);
                setForwardButtonsEnabled(true);
                break;
            case COLUMN_METHOD:
                currentColumn = -1;
                currentRow = Integer.MAX_VALUE;
                setBackwardButtonsEnabled(false);
                setForwardButtonsEnabled(true);
                break;
        }
        truthTablePanel.setCurrentRow(currentRow);
        truthTableTextArea.setCurrentRow(currentRow);
        truthTablePanel.setCurrentColumn(currentColumn);
        truthTableTextArea.setCurrentColumn(currentColumn);
    }

    private void setComputationButtonsEnabled(final boolean state) {
        setForwardButtonsEnabled(state);
        setBackwardButtonsEnabled(state);
    }

    private void setForwardButtonsEnabled(final boolean state) {
        nextButton.setEnabled(state);
        lastButton.setEnabled(state);
    }

    private void setBackwardButtonsEnabled(final boolean state) {
        firstButton.setEnabled(state);
        previousButton.setEnabled(state);
    }

    private JMenuBar getCreatedMenuBar() {
        /* boolean isMacOS = System.getProperty("mrj.version") != null;
        if (isMacOS)
            System.setProperty("com.apple.macos.useScreenMenuBar", "true"); */
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        exportMenu = new JMenu("Export");
        exportMenu.setEnabled(false);
        exportMenu.setMnemonic('E');

        final JMenu exportGraphicMenu = new JMenu("Graphic");
        exportGraphicMenu.setMnemonic('G');
        for (int i = 0; i < supportedImageTypes.length; i++) {
            final JMenuItem graphicItem = new JMenuItem(supportedImageTypes[i]);
            graphicItem.setMnemonic(supportedImageExtensions[i].charAt(0));
            graphicItem.addActionListener(new ExportGraphicActionListener(
                this, supportedImageTypes[i], supportedImageExtensions[i]));
            exportGraphicMenu.add(graphicItem);
        }
        exportTextMenuItem = new JMenuItem("Text");
        exportTextMenuItem.setMnemonic('T');
        exportTextMenuItem.addActionListener(new ExportTextActionListener(this));
        exportMenu.add(exportGraphicMenu);
        exportMenu.add(exportTextMenuItem);

        KeyStroke ks;
        fileMenu.add(exportMenu);
        // if (!isMacOS) {
        final JMenuItem exitItem = new JMenuItem("Exit");
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.ALT_MASK);
        exitItem.setAccelerator(ks);
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.addActionListener(new ExitActionListener(this));
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        // }

        viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        final ButtonGroup viewModeGroup = new ButtonGroup();
        graphicItem = new JRadioButtonMenuItem("Graphic", true);
        graphicItem.setMnemonic('G');
        graphicItem.addActionListener(new GraphicActionListener());
        viewModeGroup.add(graphicItem);

        textItem = new JRadioButtonMenuItem("Text");
        textItem.setMnemonic('T');
        textItem.addActionListener(new TextActionListener(this));
        viewModeGroup.add(textItem);

        final ButtonGroup outputModeGroup = new ButtonGroup();
        final JRadioButtonMenuItem trueFalseItem =
            new JRadioButtonMenuItem("True/False (T/F)", true);
        trueFalseItem.setMnemonic('F');
        trueFalseItem.addActionListener(new TrueFalseActionListener());
        outputModeGroup.add(trueFalseItem);

        final JRadioButtonMenuItem zeroOneItem =
            new JRadioButtonMenuItem("Zero/One (0/1)");
        zeroOneItem.setMnemonic('z');
        zeroOneItem.addActionListener(new ZeroOneActionListener());
        outputModeGroup.add(zeroOneItem);

        final JMenu textSizeMenu = new JMenu("Text Size");
        textSizeMenu.setMnemonic('S');

        final int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        final JMenuItem increaseItem = new JMenuItem("Increase");
        increaseItem.setMnemonic('I');
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, keyMask);
        increaseItem.setAccelerator(ks);
        increaseItem.addActionListener(new IncreaseFontActionListener());
        final JMenuItem decreaseItem = new JMenuItem("Decrease");
        decreaseItem.setMnemonic('D');
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, keyMask);
        decreaseItem.setAccelerator(ks);
        decreaseItem.addActionListener(new DecreaseFontActionListener());
        final JMenuItem normalItem = new JMenuItem("Normal");
        normalItem.setMnemonic('N');
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_0, keyMask);
        normalItem.setAccelerator(ks);
        normalItem.addActionListener(new NormalFontActionListener());

        textSizeMenu.add(increaseItem);
        textSizeMenu.add(decreaseItem);
        textSizeMenu.addSeparator();
        textSizeMenu.add(normalItem);

        highlightItem = new JCheckBoxMenuItem("Highlight Main Column");
        highlightItem.setMnemonic('H');
        highlightItem.setState(true);
        highlightItem.addActionListener(new HighlightActionListener());

        numberRowsItem = new JCheckBoxMenuItem("Show Row Numbers");
        numberRowsItem.setMnemonic('R');
        numberRowsItem.setState(true);
        numberRowsItem.addActionListener(new NumberRowsActionListener());

        numberColumnsItem = new JCheckBoxMenuItem("Show Column Numbers");
        numberColumnsItem.setMnemonic('C');
        numberColumnsItem.setState(false);
        numberColumnsItem.addActionListener(new NumberColumnsActionListener());

        alternateColorsItem = new JCheckBoxMenuItem("Alternate Row Colors");
        alternateColorsItem.setMnemonic('A');
        alternateColorsItem.setState(false);
        alternateColorsItem.addActionListener(new AlternateColorsActionListener());

        viewMenu.add(graphicItem);
        viewMenu.add(textItem);
        viewMenu.addSeparator();
        viewMenu.add(trueFalseItem);
        viewMenu.add(zeroOneItem);
        viewMenu.addSeparator();
        viewMenu.add(highlightItem);
        viewMenu.add(numberRowsItem);
        viewMenu.add(numberColumnsItem);
        viewMenu.add(alternateColorsItem);
        viewMenu.addSeparator();
        viewMenu.add(textSizeMenu);

        final JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic('O');

        removeParensItem = new JCheckBoxMenuItem("Remove Unnecessary Parentheses");
        removeParensItem.setMnemonic('R');
        removeParensItem.setState(true);

        alphabetizePropositionsItem = new JCheckBoxMenuItem("Alphabetize Propositions");
        alphabetizePropositionsItem.setMnemonic('A');
        alphabetizePropositionsItem.setState(true);

        optionsMenu.add(removeParensItem);
        optionsMenu.add(alphabetizePropositionsItem);

        final JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        final JMenuItem contentsItem = new JMenuItem("Contents");
        contentsItem.setMnemonic('C');
        contentsItem.addActionListener(new HelpActionListener(this));

        final JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(new AboutActionListener(this));

        helpMenu.add(contentsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void processCommand(String statement, final boolean updateFields) {
        statementComboBox.requestFocusInWindow();
        if (evaluateThread != null)
            stop();
        if (statement == null)
            return;
        if (statement.length() == 0) {
            AudioOptionPane.showMessageDialog(this,
                "Cannot construct table: no statement entered.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final Scanner scanner = new Scanner(statement);
        try {
            scanner.tokenize();
        } catch (final ScannerException se) {
            statementComboBox.select(se.getXValue() - 1, se.getYValue());
            AudioOptionPane.showMessageDialog(this,
                 se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        scanner.reformat();
        statement = scanner.getStatement();
        if (updateFields)
            statementComboBox.setText(scanner.getStatement());
        final Parser parser = new Parser(scanner.getTokenStream());
        try {
            parser.parse();
        } catch (final ParserException pe) {
            if (pe.selectAll())
                statementComboBox.selectAll();
            else
                statementComboBox.select(pe.getXValue() - 1, pe.getXValue());
            AudioOptionPane.showMessageDialog(this,
                pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (updateFields)
            statementComboBox.addItem(scanner.getStatement());
        if (removeParensItem.getState())
            parser.removeUnnecessaryParentheses();
        truthTable = new TruthTable(parser.getStatement(),
            parser.getPostfixStream(), outputMode, alphabetizePropositionsItem.getState());
        truthTableTextArea.setTruthTable(truthTable, updateFields);
        truthTableTextArea.setHighlightMainColumn(highlightItem.getState());
        truthTableTextArea.setShowRowNumbers(numberRowsItem.getState());
        truthTableTextArea.setShowColumnNumbers(numberColumnsItem.getState());
        if (textItem.isSelected()) {
            if (truthTable.getNumberOfLines() > maxRowsInTextTable) {
                AudioOptionPane.showMessageDialog(this,
                    "Cannot display tables with more than " +
                    maxRowsInTextTable + " rows in text.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                graphicItem.setSelected(true);
                scrollPane.setViewportView(truthTablePanel);
            } else {
                redrawTruthTableTextArea();
            }
        }
        truthTablePanel.setTruthTable(truthTable);
        truthTablePanel.setHighlightMainColumn(highlightItem.getState());
        truthTablePanel.setShowRowNumbers(numberRowsItem.getState());
        truthTablePanel.setShowColumnNumbers(numberColumnsItem.getState());
        truthTablePanel.setShowAlternateRowsInColor(alternateColorsItem.getState());
        if (updateFields)
            setComputationMethod(computationMethod);
        redrawTruthTableTextArea();
        repaintViewport();
        evaluateThread = new Thread(this);
        evaluateThread.start();
        lastStatement = statement;
        exportMenu.setEnabled(hasFullPermission);
        if (updateFields)
            statementComboBox.selectAll();
    }

    private void processCommand() {
        final String statement = statementComboBox.getText().trim();
        processCommand(statement, true);
    }

    private boolean getHasFullPermission() {
        boolean hasPermission = true;
        if (applicationStarter != null) {
            final SecurityManager manager = System.getSecurityManager();
            if (manager != null) {
                try {
                    manager.checkPermission(new java.security.AllPermission());
                } catch (final SecurityException se) {
                    hasPermission = false;
                } catch (final NullPointerException npe) {
                    hasPermission = false;
                }
            } else {
                hasPermission = false;
            }
        }
        return hasPermission;
    }

    private void redrawTruthTableTextArea() {
        final JScrollBar vScrollBar = scrollPane.getVerticalScrollBar(),
                         hScrollBar = scrollPane.getHorizontalScrollBar();
        final int vPosition = vScrollBar.getValue(),
                  hPosition = hScrollBar.getValue();
        truthTableTextArea.redraw();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                vScrollBar.setValue(vPosition);
                hScrollBar.setValue(hPosition);
            }
        });
    }

    private void repaintViewport() {
        final JViewport viewport = scrollPane.getViewport();
        viewport.revalidate();
        viewport.repaint();
    }

    private void stopTimer() {
        timer.stop();
        initializeStatusPanel(false);
        progressBar.setValue(0);
    }

    private void setMutableOptionsEnabled(final boolean enabled) {
        statementComboBox.setEnabled(enabled);
        constructButton.setEnabled(enabled);
        computationMethodComboBox.setEnabled(enabled);
        if (!enabled) {
            buttonStates[0] = firstButton.isEnabled();
            buttonStates[1] = previousButton.isEnabled();
            buttonStates[2] = nextButton.isEnabled();
            buttonStates[3] = lastButton.isEnabled();
            firstButton.setEnabled(false);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            lastButton.setEnabled(false);
        } else {
            firstButton.setEnabled(buttonStates[0]);
            previousButton.setEnabled(buttonStates[1]);
            nextButton.setEnabled(buttonStates[2]);
            lastButton.setEnabled(buttonStates[3]);
        }
        viewMenu.setEnabled(enabled);
        exportMenu.setEnabled(enabled);
    }

    private void saveTableToFile(final String filename) throws IOException {
        final Thread thisThread = Thread.currentThread();
        final FileWriter fileWriter = new FileWriter(filename);
        final BufferedWriter builderedWriter = new BufferedWriter(fileWriter);
        final PrintWriter printWriter = new PrintWriter(builderedWriter, true);
        StringBuilder builder = new StringBuilder();
        final int numberOfCharsInMaxLine = truthTableTextArea.getNumberOfCharsInMaxLine();

        if (fileSaveThread == thisThread) {
            if (truthTableTextArea.getIsMainColumnHighlighted()) {
                truthTableTextArea.getHighlightedLine(numberOfCharsInMaxLine, builder);
                printWriter.println(builder.toString());
            }

            builder = new StringBuilder();
            truthTableTextArea.padLeftMargin(builder, numberOfCharsInMaxLine);
            builder.append(truthTable.getHeader(true));
            printWriter.println(builder.toString());
            builder = new StringBuilder();
            truthTableTextArea.padLeftMargin(builder, numberOfCharsInMaxLine);
            builder.append(truthTable.getHeaderSeparator());
            printWriter.println(builder.toString());
        }

        final int numberOfLines = truthTable.getNumberOfLines(),
            displayMethod = truthTable.getDisplayMethod(),
            columnInfoHeight = truthTable.getColumnInfoHeight();
        currentIteration = 0;
        while ((fileSaveThread == thisThread) && (currentIteration < numberOfLines)) {
            builder = new StringBuilder();
            truthTableTextArea.getLine(currentIteration, displayMethod, numberOfLines, numberOfCharsInMaxLine,
                builder);
            if (currentIteration != numberOfLines - 1)
                printWriter.println(builder.toString());
            else
                printWriter.print(builder.toString());
            currentIteration++;
        }

        if ((fileSaveThread == thisThread) && (truthTableTextArea.getAreColumnNumbersShown())) {
            printWriter.println();
            int i = 0;
            builder = new StringBuilder();
            while (truthTableTextArea.getColumnOrderLine(i, numberOfCharsInMaxLine, builder)) {
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
        fileSaveThread = null;
    }

    private void doApplicationClosing(final JFrame parent) {
        int result = 0;
        if ((fileSaveThread != null) && (fileSaveThread.isAlive())) {
            final Object[] options = { "Continue saving", "Abort and close" };
            result = AudioOptionPane.showOptionDialog(parent,
                "The application is still saving the truth table to file.\n" +
                "You can return to the application to continue saving or\n" +
                "abort saving and close the application now.",
                bundle.getString("APPLICATION_NAME"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        } else {
            result = 1;
        }
        if (result == 1) {
            if (applicationStarter != null) {
                applicationStarter.close();
                if (helpFrame != null) {
                    helpFrame.setVisible(false);
                    helpFrame.dispose();
                    helpFrame = null;
                }
            } else
                System.exit(0);
        }
    }

    class ExportGraphicActionListener implements ActionListener {
        private final JFrame parent;
        private final String imageType, imageExtension;

        public ExportGraphicActionListener(final JFrame parent, final String imageType,
            final String imageExtension) {

            this.parent = parent;
            this.imageType = imageType;
            this.imageExtension = imageExtension;
        }

        public void actionPerformed(final ActionEvent e) {
            final BufferedImage bufferedImage;
            final int height = truthTablePanel.getCanvasHeight(),
                      width = truthTablePanel.getCanvasWidth();
            if (height * width <= 0) {
                AudioOptionPane.showMessageDialog(parent,
                     "Cannot allocate enough memory to export the truth table to image.\nYou will have to save this truth table as text.",
                     "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            } catch (final OutOfMemoryError oome) {
                AudioOptionPane.showMessageDialog(parent,
                     "Cannot allocate enough memory to export the truth table to image.\nYou will have to save this truth table as text.",
                     "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final SaveAsFileChooser chooser = new SaveAsFileChooser();
            chooser.setDialogTitle("Save As");
            chooser.setFileFilter(new ImageFilter(imageType, imageExtension));
            chooser.setAcceptAllFileFilterUsed(false);
            if (currentDirectory != null)
                chooser.setCurrentDirectory(currentDirectory);
            final int state = chooser.showSaveDialog(parent);
            final File file = chooser.getSelectedFile();
            if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
                new Thread() {
                    public void run() {
                        final int numberOfLines = truthTable.getNumberOfLines();
                        final String evaluationLabelStr = evaluationLabel.getText(),
                               statusLabelStr = statusLabel.getText(),
                               evaluationStatsLabelStr = evaluationStatsLabel.getText();
                        if (numberOfLines > 8) {
                            currentDirectory = chooser.getCurrentDirectory();
                            progressBar.setIndeterminate(true);
                            progressBar.setString("");
                            progressBar.setValue(0);
                            initializeStatusPanel(true);
                            evaluationLabel.setText("Exporting truth table to image file.");
                            statusLabel.setForeground(Color.BLACK);
                            statusLabel.setText("");
                            evaluationStatsLabel.setText("");
                            setMutableOptionsEnabled(false);
                            timer.start();
                            setCursor(waitCursor);
                        }
                        try {
                            truthTablePanel.saveContentsAsImage(file.getPath(), imageExtension, bufferedImage);
                            if (numberOfLines > 8) {
                                stopTimer();
                                evaluationLabel.setText(evaluationLabelStr);
                                statusLabel.setText(statusLabelStr);
                                evaluationStatsLabel.setText(evaluationStatsLabelStr);
                                setMutableOptionsEnabled(true);
                                setCursor(defaultCursor);
                            }
                        } catch (final Exception e) {
                            if (numberOfLines > 8) {
                                stopTimer();
                                evaluationLabel.setText(evaluationLabelStr);
                                statusLabel.setText(statusLabelStr);
                                evaluationStatsLabel.setText(evaluationStatsLabelStr);
                                setMutableOptionsEnabled(true);
                                setCursor(defaultCursor);
                            }
                            AudioOptionPane.showMessageDialog(parent,
                                 "An error occurred when saving to file '" + file.getPath() + "'.",
                                 "Error", JOptionPane.ERROR_MESSAGE);
                        } catch (final OutOfMemoryError oome) {
                            if (numberOfLines > 8) {
                                stopTimer();
                                evaluationLabel.setText(evaluationLabelStr);
                                statusLabel.setText(statusLabelStr);
                                evaluationStatsLabel.setText(evaluationStatsLabelStr);
                                setMutableOptionsEnabled(true);
                                setCursor(defaultCursor);
                            }
                            AudioOptionPane.showMessageDialog(parent,
                                 "Additional memory was requested to export the image, but none is available.\nYou can try exporting to a different type of image, or you can save this\ntruth table as text.",
                                 "Error", JOptionPane.ERROR_MESSAGE);
                        } finally {
                            progressBar.setIndeterminate(false);
                        }
                    }
                }.start();
            }
        }
    }

    class FileSaveThread extends Thread implements Runnable {
        private final JFrame parent;
        private final SaveAsFileChooser chooser;
        private final File file;

        public FileSaveThread(final JFrame parent, final SaveAsFileChooser chooser, final File file) {
            this.parent = parent;
            this.chooser = chooser;
            this.file = file;
        }

        public void run() {
            String evaluationLabelStr = evaluationLabel.getText(),
                   statusLabelStr = statusLabel.getText(),
                   evaluationStatsLabelStr = evaluationStatsLabel.getText();
            final int numberOfLines = truthTable.getNumberOfLines();
            if (numberOfLines > smallTableLimit) {
                evaluationLabelStr = evaluationLabel.getText();
                statusLabelStr = statusLabel.getText();
                evaluationStatsLabelStr = evaluationStatsLabel.getText();
                progressBar.setIndeterminate(false);
                progressBar.setString(null);
                progressBar.setMaximum(numberOfLines);
                initializeStatusPanel(true);
                evaluationLabel.setText("Saving truth table to file. ");
                statusLabel.setForeground(Color.BLACK);
                statusLabel.setText("Press Escape to abort.");
                evaluationStatsLabel.setText("");
                setMutableOptionsEnabled(false);
                setCursor(waitCursor);
                timer.start();
            }
            currentDirectory = chooser.getCurrentDirectory();
            try {
                saveTableToFile(file.getPath());
                if (numberOfLines > smallTableLimit) {
                    stopTimer();
                    evaluationLabel.setText(evaluationLabelStr);
                    statusLabel.setText(statusLabelStr);
                    evaluationStatsLabel.setText(evaluationStatsLabelStr);
                    setMutableOptionsEnabled(true);
                    setCursor(defaultCursor);
                }
            } catch (final IOException ioe) {
                if (numberOfLines > smallTableLimit) {
                    stopTimer();
                    evaluationLabel.setText(evaluationLabelStr);
                    statusLabel.setText(statusLabelStr);
                    evaluationStatsLabel.setText(evaluationStatsLabelStr);
                    setMutableOptionsEnabled(true);
                    setCursor(defaultCursor);
                }
                AudioOptionPane.showMessageDialog(parent,
                     "An error occurred when saving to file '" + file.getPath() + "'.",
                     "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class ExportTextActionListener implements ActionListener {
        private final JFrame parent;

        public ExportTextActionListener(final JFrame parent) {
            this.parent = parent;
        }

        public void actionPerformed(final ActionEvent e) {
            final SaveAsFileChooser chooser = new SaveAsFileChooser();
            chooser.setDialogTitle("Save As");
            chooser.setFileFilter(new TextFilter());
            chooser.setAcceptAllFileFilterUsed(false);
            if (currentDirectory != null)
                chooser.setCurrentDirectory(currentDirectory);
            final int state = chooser.showSaveDialog(parent);
            final File file = chooser.getSelectedFile();
            if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
                fileSaveThread = new FileSaveThread(parent, chooser, file);
                fileSaveThread.start();
            }
        }
    }

    class HelpActionListener implements ActionListener {
        private final JFrame parent;

        public HelpActionListener(final JFrame parent) {
            this.parent = parent;
        }

        public void actionPerformed(final ActionEvent e) {
            if ((helpFrame == null) || (!helpFrame.isVisible())) {
                helpFrame = new HelpFrame(parent,
                    bundle.getString("APPLICATION_NAME"), "index.html", applicationStarter,
                    Integer.parseInt(bundle.getString("HELP_FRAME_WIDTH")),
                    Integer.parseInt(bundle.getString("HELP_FRAME_HEIGHT")),
                    Integer.parseInt(bundle.getString("MIN_HELP_WIDTH")),
                    Integer.parseInt(bundle.getString("MIN_HELP_HEIGHT")));
            } else {
                helpFrame.toFront();
                helpFrame.requestFocus();
            }
        }
    }

    class AboutActionListener implements ActionListener {
        private final JFrame parent;

        public AboutActionListener(final JFrame parent) {
            this.parent = parent;
        }

        public void actionPerformed(final ActionEvent e) {
            final String[] data = {"Version " + bundle.getString("VERSION_NUMBER"),
                             "© " + bundle.getString("COPYRIGHT_YEAR") + " " + bundle.getString("AUTHOR"),
                             bundle.getString("RESERVATION_NOTICE"),
                             "Build: " + bundle.getString("BUILD_DATE")};
            new AboutDialog(parent, bundle.getString("APPLICATION_NAME"),
                data, bundle.getString("ICON"));
        }
    }

    class TextActionListener implements ActionListener {
        private final JFrame parent;

        public TextActionListener(final JFrame parent) {
            this.parent = parent;
        }

        public void actionPerformed(final ActionEvent e) {
            if ((truthTable == null) || (truthTable.getNumberOfLines() <= maxRowsInTextTable)) {
                scrollPane.setViewportView(truthTableTextArea);
                redrawTruthTableTextArea();
            } else {
                graphicItem.setSelected(true);
                AudioOptionPane.showMessageDialog(parent,
                    "Cannot display tables with more than " +
                    maxRowsInTextTable + " rows in text.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    class GraphicActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            scrollPane.setViewportView(truthTablePanel);
        }
    }

    class TrueFalseActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            outputMode = TruthValue.TRUE_FALSE;
            if (truthTable != null) {
                if (cachedEvaluation == TruthTable.IDENTITY)
                    cachedEvaluation = TruthTable.TAUTOLOGY;
                processCommand(lastStatement, false);
            }
        }
    }

    class ZeroOneActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            outputMode = TruthValue.ZERO_ONE;
            if (truthTable != null) {
                if (cachedEvaluation == TruthTable.TAUTOLOGY)
                    cachedEvaluation = TruthTable.IDENTITY;
                processCommand(lastStatement, false);
            }
        }
    }

    class IncreaseFontActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTableTextArea.increaseFontSize();
            truthTablePanel.increaseFontSize();
            repaintViewport();
        }
    }

    class DecreaseFontActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTableTextArea.decreaseFontSize();
            truthTablePanel.decreaseFontSize();
            repaintViewport();
        }
    }

    class NormalFontActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTableTextArea.resetDefaultFontSize();
            truthTablePanel.resetDefaultFontSize();
            repaintViewport();
        }
    }

    class StatementKeyListener implements KeyListener {
        public void keyReleased(final KeyEvent ke) { }

        public void keyPressed(final KeyEvent ke) {
            if (KeyEvent.getKeyText(ke.getKeyCode()).equals("Enter")) {
                cachedEvaluation = TruthTable.UNDEFINED;
                scrollPane.getVerticalScrollBar().setValue(0);
                processCommand();
            }
        }

        public void keyTyped(final KeyEvent ke) { }
    }

    class ConstructActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            cachedEvaluation = TruthTable.UNDEFINED;
            scrollPane.getVerticalScrollBar().setValue(0);
            processCommand();
        }
    }

    class HighlightActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTablePanel.setHighlightMainColumn(highlightItem.getState());
            truthTableTextArea.setHighlightMainColumn(highlightItem.getState());
            redrawTruthTableTextArea();
            repaintViewport();
        }
    }

    class NumberRowsActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTablePanel.setShowRowNumbers(numberRowsItem.getState());
            truthTableTextArea.setShowRowNumbers(numberRowsItem.getState());
            redrawTruthTableTextArea();
            repaintViewport();
        }
    }

    class NumberColumnsActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTablePanel.setShowColumnNumbers(numberColumnsItem.getState());
            truthTableTextArea.setShowColumnNumbers(numberColumnsItem.getState());
            redrawTruthTableTextArea();
            repaintViewport();
        }
    }

    class AlternateColorsActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            truthTablePanel.setShowAlternateRowsInColor(alternateColorsItem.getState());
            repaintViewport();
        }
    }

    class ExitActionListener implements ActionListener {
        private final JFrame parent;

        public ExitActionListener(final JFrame parent) {
            this.parent = parent;
        }

        public void actionPerformed(final ActionEvent e) {
            doApplicationClosing(parent);
        }
    }

    class ClosingWindowListener implements WindowListener {
        private final JFrame parent;

        public ClosingWindowListener(final JFrame parent) {
            this.parent = parent;
        }

        public void windowClosing(final WindowEvent e) {
            doApplicationClosing(parent);
        }

        public void windowDeactivated(final WindowEvent e) { }

        public void windowActivated(final WindowEvent e) { }

        public void windowDeiconified(final WindowEvent e) { }

        public void windowIconified(final WindowEvent e) { }

        public void windowClosed(final WindowEvent e) { }

        public void windowOpened(final WindowEvent e) { }
    }
}
