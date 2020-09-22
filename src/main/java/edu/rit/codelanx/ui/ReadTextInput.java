package edu.rit.codelanx.ui;

import java.io.*;

public class ReadTextInput implements IReadInput {
    private final InputStream input;
    private final PrintStream output;
    private final BufferedReader buffer;
    private final InputStreamReader reader;

    ReadTextInput(InputStream input, PrintStream output){
        this.input = input;
        this.output = output;
        this.reader = new InputStreamReader(input);
        this.buffer = new BufferedReader(this.reader);
    }

    /**
     * reads buffered input
     * @param buffer
     * @return
     * @throws IOException
     */
    @Override
    public String readInput(BufferedReader buffer) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = buffer.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }
}
