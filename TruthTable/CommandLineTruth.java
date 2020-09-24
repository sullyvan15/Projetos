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

import java.util.LinkedList;
import java.util.ListIterator;

import logic.Parser;
import logic.ParserException;
import logic.Scanner;
import logic.ScannerException;
import logic.Token;
import logic.TruthTable;
import logic.TruthValue;

/**
 * Implements a command-line truth table constructor.
 * The statement argument should be enclosed in quotes " " on UNIX/Linux.
 */
public class CommandLineTruth {

    public static void main(final String[] args) {
        final int numberOfArguments = args.length;
        if (numberOfArguments == 0) {
            System.out.println("Usage: java CommandLineTruth <statement>");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numberOfArguments; i++) {
            builder.append(args[i]);
        }
        final String statement = builder.toString();
        System.out.print("Input: " + statement + "\n");
        final Scanner scanner = new Scanner(statement);
        try {
            scanner.tokenize();
        } catch (final ScannerException se) {
            System.err.println("Error: " + se.getMessage());
            System.exit(1);
        }
        scanner.reformat();
        if (scanner.getStatement().length() == 0) {
            System.err.println(
                "Error: Cannot construct table, no statement entered.");
            System.exit(1);
        }
        System.out.print("Reformatted: " + scanner.getStatement() + "\n");
        final Parser parser = new Parser(scanner.getTokenStream());
        LinkedList<Token> postfixStream = null;
        try {
            parser.parse();
            parser.removeUnnecessaryParentheses();
            postfixStream = parser.getPostfixStream();
            final ListIterator<Token> iterator = postfixStream.listIterator(0);
            System.out.print("Postfix: ");
            while (iterator.hasNext()) {
                final Token token = iterator.next();
                System.out.print(token.getSymbol());
            }
            System.out.print("\n\n");
        } catch (final ParserException pe) {
            System.err.println("Error: " + pe.getMessage());
            System.exit(1);
        }
        final TruthTable truthTable = new TruthTable(parser.getStatement(), postfixStream, TruthValue.TRUE_FALSE, true);
        builder = new StringBuilder();
        builder.append(truthTable.getHeader(true));
        builder.append("\n");
        builder.append(truthTable.getHeaderSeparator());
        builder.append("\n");
        System.out.print(builder.toString());
        final int numberOfLines = truthTable.getNumberOfLines();
        for (int i = numberOfLines - 1; i >= 0; --i) {
            builder = new StringBuilder();
            final boolean[] binaryPropositionValues = truthTable.getBinaryFormat(i);
            final int binaryLength = binaryPropositionValues.length;
            for (int j = 0; j < binaryLength; j++) {
                builder.append(" ");
                builder.append(TruthValue.getTruthValueString(binaryPropositionValues[j], TruthValue.TRUE_FALSE));
                builder.append(" |");
            }
            builder.append(" ");
            builder.append(truthTable.computeRow(i));
            builder.append(" ");
            if (i != 0) {
                builder.append("\n");
            }
            System.out.print(builder.toString());
        }
    }
}
