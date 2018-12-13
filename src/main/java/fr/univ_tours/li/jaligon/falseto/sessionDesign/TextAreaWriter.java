/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import java.io.IOException;
import java.io.Writer;
import javax.swing.JTextArea;

/**
 *
 * @author julien
 */
public class TextAreaWriter extends Writer {

    private JTextArea textArea;

    public TextAreaWriter(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void write(char buf[], int off, int len) throws IOException {
        if (textArea == null) {
            throw new IOException("already closed");
        }
        textArea.append(new String(buf, off, len));
        textArea.setCaretPosition(textArea.getText().length());
    }

    public void flush() {
    }

    public void close() {
        textArea = null;
    }
}