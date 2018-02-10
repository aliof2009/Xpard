/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gepard;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import org.gepard.client.Config;
import org.gepard.common.InvalidFASTAFileException;
import org.gepard.common.InvalidSubMatFileException;
import org.gepard.common.Sequence;
import org.gepard.common.SubstitutionMatrix;

public class MyMethods {

    /*
SubstitutionMatrix.java
    if (true) {
        return MyMethods.myDoLoad(br, file);
    }
    
    public int getScore(byte a, byte b) {
        return scoreMatrix[(int)a&0xff][(int)b&0xff];
    }
    public byte map(byte input) {
        return charMapping[(int)input&0xff];
    }
    public char reverseMap(byte input) {
        return revMapping[(int)input&0xff];
    }
     */
    public static SubstitutionMatrix myDoLoad(BufferedReader br, String file) throws IOException, InvalidSubMatFileException {
        String curLine;
        int[][] matrix = null;
        char[] backMapping = new char[256];
        byte[] revMapping = new byte[256];
        revMapping[13] = -1;
        revMapping[10] = -1;
        int mapPos = 1, linenum = 0, reallinenum = 0;
        boolean firstLine = true;
        while ((curLine = br.readLine()) != null) {
            reallinenum++;
            if ((curLine.length() > 0)/* && (curLine.charAt(0) != '#')*/) {
                if (firstLine) {
                    curLine = UTFANSIMapper.UTFtoANSIMatrixCreator(curLine, br);
                }
                byte[] UTFtoANSIConverter2 = UTFANSIMapper.UTFtoANSIConverterMatrix(curLine);
//                System.out.println((byte) curLine.charAt(0));
                if (firstLine) {
                    for (int i = 0; i < UTFtoANSIConverter2.length; i++) {
                        if (UTFtoANSIConverter2[i] != '\t') {
                            // throw exception if character already defined
                            if (revMapping[(int) (UTFtoANSIConverter2[i] & 0xff)] != 0) {
                                throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - character defined twice: " + UTFtoANSIConverter2[i]);
                            } else {
                                revMapping[(int) (UTFtoANSIConverter2[i] & 0xff)] = (byte) mapPos++;
                                // if this is not the last character a whitespace must follow
                                if ((i < (UTFtoANSIConverter2.length - 1)) && (UTFtoANSIConverter2[i + 1] != '\t')) {
                                    throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid expression: " + UTFtoANSIConverter2[i] + UTFtoANSIConverter2[i + 1] + UTFtoANSIConverter2[i + 2]);
                                }
                            }
                        }
                    }
                    // create backward mapping 
                    for (int i = 0; i < 256; i++) {
                        if (revMapping[i] >= 0) {
                            backMapping[revMapping[i]] = (char) i;
                        }
                    }
                    // create scorematrix array
                    matrix = new int[mapPos][mapPos];
                    // set flag
                    firstLine = false;

                } else {
                    // check if we already have all needed lines
                    if (linenum == (mapPos - 1)) {
                        throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - too many lines");
                    }
                    int toknum = 0;
                    int poslast = 0;
                    boolean lastchar = false;
                    boolean foundnonspace = false;
                    for (int i = 0; i < curLine.length(); i++) {
                        // last character ?
                        if (i == curLine.length() - 1) {
                            if (curLine.charAt(i) != '\t') {
                                lastchar = true;
                                i++;
                            }
                        }

                        if (lastchar || ((curLine.charAt(i) == '\t') && foundnonspace)) {
                            if (toknum > 0) {
                                // check if there are too many tokens
                                if (toknum >= mapPos) {
                                    throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - too many tokens");
                                }
                                // extract integer value and insert into array
                                try {
                                    matrix[toknum][linenum + 1] = Integer.parseInt(curLine.substring(poslast, i).trim());
                                } catch (NumberFormatException nfe) {
                                    throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid integer value: " + curLine.substring(poslast, i).trim());
                                }

                            } else {
                                // first token: check if correct character is given (matrix must be symmetric)
                                if (revMapping[(int) UTFtoANSIConverter2[0] & 0xff] != (linenum + 1)) {
                                    throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid character: " + curLine.trim().charAt(0) + ", exptected: " + backMapping[linenum + 1]);
                                }
                                // a white space must follow now!
                                if (curLine.trim().charAt(1) != '\t') {
                                    throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - invalid expression: " + curLine.substring(0, 2));
                                }

                            }

                            toknum++;
                            poslast = i;
                            foundnonspace = false;
                        } else if (curLine.charAt(i) != '\t') {
                            foundnonspace = true;
                        }
                    }
                    // decrease toknum by one if last characters where whitespaces
                    if (!foundnonspace) {
                        toknum--;
                    }
                    if (toknum > 0) {  // any tokens found or empty line?
                        if (toknum < (mapPos - 1)) // incomplete line
                        {
                            throw new InvalidSubMatFileException("Error in " + file + ":" + reallinenum + " - incomplete line");
                        } else {
                            linenum++;
                        }
                    }
                }
            }
        }
        // check if we are missing lines
        if (linenum < (mapPos - 1)) {
            throw new InvalidSubMatFileException("Error in " + file + " - there are lines missing");
        }

        return new SubstitutionMatrix(matrix, revMapping, backMapping);
    }

    /*
FASTAReader.java
    if (true) {
        return MyMethods.myReadFile(file, substmat);
    }
     */
    public static Sequence myReadFile(String file, SubstitutionMatrix substmat)
            throws FileNotFoundException, IOException, InvalidFASTAFileException {
        boolean invalidchars = false;
        int charcount = 0;

        // get file length
        File f = new File(file);
        int filelen = (int) f.length();
        if (filelen < 0) {
            throw new InvalidFASTAFileException("Files larger than 2.1GB are not supported");
        }
        if (filelen == 0) {
            throw new InvalidFASTAFileException("Empty file");
        }

        // get input stream
        byte[] contentsUTF = new byte[filelen];
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            in.readFully(contentsUTF, 0, filelen);
        }
        // UTF to ANSI
        String[] stringUTF = new String(contentsUTF).split(System.lineSeparator(), 2);
        String name = stringUTF[0];
        byte[] contents = UTFANSIMapper.UTFtoANSIConverterFASTA(stringUTF[1].replace("\n", " "));

        // now parse rest of file
        // first run: count valid characters, overall characters and ACGT amount
        int validcount = 0;
        for (int j = 0; j < contents.length; j++) {

            if (j >= contents.length) {
                break;
            }

            // valid character for current marix?
            if (substmat.map(contents[j]) > 0) {
                validcount++;
            }
            // any character except 13 and 10?
            if (substmat.map(contents[j]) >= 0) {
                charcount++;
            }
        }

        // second run: create and fill array
        byte[] sequence = new byte[validcount];
        int k = 0;
        for (int j = 0; j < contents.length; j++) {

            if (j >= contents.length) {
                break;
            }

            // add to sequence or output error
            byte mapval = substmat.map(contents[j]);

            if (mapval > 0) {
                sequence[k++] = mapval;
            } else if (mapval == 0) {
                invalidchars = true;
            }
        }
        // create and return LocalSequence object
        Sequence seq = new Sequence(sequence, name, false, invalidchars);

        return seq;
    }

    /*
FASTAReader.java
    if (true) {
        return MyMethods.myIsNucleotideFile();
    }
     */
    public static boolean myIsNucleotideFile() {
        return false;
    }

    /*
ControlPanel.java    
     */
    static String CustomMatrixName;

    public static String getCustomMatrixName() {
        return CustomMatrixName;
    }

    public static void setCustomMatrixName(String CustomMatrixName) {
        MyMethods.CustomMatrixName = Paths.get(CustomMatrixName).getFileName().toString().replaceFirst("[.][^.]+$", "");
    }
    /*
ControlPanel.java
    chkAutoMatrix.setSelected(Config.getInstance().getIntVal("automatrix", [1->0] ) == 1 ? true : false);
    
InfoPanel.java
    bufferGraphics.setFont(new Font(["Courier"->"Courier New"], Font.PLAIN, 12));
    insertNumIntoCharArray(strpos1, 0, [seq1pos-backward -> seq1pos + forward - 1]);
    insertNumIntoCharArray(strpos1, oddadd + forward + backward - numLength(seq1pos + forward - 1), [seq1pos+forward-1 -> seq1pos - backward]);
    insertNumIntoCharArray(strpos2, 0, [seq2pos-backward -> seq2pos + forward - 1]);
    insertNumIntoCharArray(strpos2, oddadd + forward + backward - numLength(seq2pos + forward - 1), [seq2pos+forward-1 -> seq2pos - backward]);
    insertNumIntoCharArray(strpos2, 0, [seq2pos + backward -> seq2pos - forward + 1]);
    insertNumIntoCharArray(strpos2, oddadd + forward + backward - numLength(seq2pos - forward + 1), [seq2pos - forward + 1 -> seq2pos + backward]);
    bufferGraphics.drawString([String.valueOf -> UTFANSIMapper.ANSItoUTFConverter](strseq1, 0, alignlen), 10, ALIGNMENT_Y + (charheight * 2) + ascent);
    bufferGraphics.drawString("ا" + String.valueOf(strsim, 0, alignlen), 10, ALIGNMENT_Y + (charheight * 3) + ascent);
    bufferGraphics.drawString([String.valueOf -> UTFANSIMapper.ANSItoUTFConverter](strseq2, 0, alignlen), 10, ALIGNMENT_Y + (charheight * 4) + ascent);
    alignlen = [Math.min(]((dim.width - (ALIGNMENT_MARGIN_LEFT + ALIGNMENT_MARGIN_HOR_ADD)) / charwidth)[,101)];
     */
}
