package org.cds.cql.fhir.flink.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Nullable;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.reader.SimpleStreamFormat;
import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.core.fs.FSDataInputStream;

public class FHIRInputTextFormat extends SimpleStreamFormat<String> {

private static final long serialVersionUID = 1L;

public static final String DEFAULT_CHARSET_NAME = "UTF-8";

private final String charsetName;

public FHIRInputTextFormat() {
    this(DEFAULT_CHARSET_NAME);
}

public FHIRInputTextFormat(String charsetName) {
    this.charsetName = charsetName;
}

@Override
public Reader createReader(Configuration config, FSDataInputStream stream) throws IOException {
    final BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream, charsetName));
    return new Reader(reader);
}

@Override
public TypeInformation<String> getProducedType() {
    return Types.STRING;
}

// ------------------------------------------------------------------------

/** The actual reader for the {@code TextLineInputFormat}. */
@PublicEvolving
public static final class Reader implements StreamFormat.Reader<String> {

	
    private final BufferedReader reader;

    Reader(final BufferedReader reader) {
        this.reader = reader;
    }

    @Nullable
    @Override
    public String read() throws IOException {
		
		StringBuilder buf = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			buf.append(line);
		}
		//this.close();
		return buf.toString();
		 
    	//return reader.readLine()+"56776";
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
}
