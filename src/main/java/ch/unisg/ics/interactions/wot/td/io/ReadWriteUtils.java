package ch.unisg.ics.interactions.wot.td.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

final class ReadWriteUtils {
  private final static Logger LOGGER = Logger.getLogger(ReadWriteUtils.class.getCanonicalName());

  static Model readModelFromString(RDFFormat format, String description, String baseURI)
      throws RDFParseException, RDFHandlerException, IOException {
    StringReader stringReader = new StringReader(description);

    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    Model model = new LinkedHashModel();
    rdfParser.setRDFHandler(new StatementCollector(model));

    rdfParser.parse(stringReader, baseURI);

    return model;
  }

  static String writeToString(RDFFormat format, Model model) {
    OutputStream out = new ByteArrayOutputStream();

    try {
      Rio.write(model, out, format,
          new WriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true));
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, e.getMessage());
      }
    }

    return out.toString();
  }

  static StringReader getStringReaderFromRDF(String str, RDFFormat format){
    String newStr = "";
    if (format.equals(RDFFormat.TURTLE)) {
      newStr = conversion(str);
    } else {
      newStr = str;
    }
    return new StringReader(newStr);
  }

  static String conversion(String str){
    StringBuilder newStr = new StringBuilder();
    for (int i = 0; i<str.length();i++){
      char c = str.charAt(i);
      if (c == '{'){
        newStr.append("%7B");
      }
      else if (c == '}'){
        newStr.append("%7D");
      }
      else {
        newStr.append(c);
      }
    }
    return newStr.toString();
  }

  private ReadWriteUtils() { }
}
