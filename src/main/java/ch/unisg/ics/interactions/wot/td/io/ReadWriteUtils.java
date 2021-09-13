package ch.unisg.ics.interactions.wot.td.io;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;

import com.apicatalog.jsonld.document.RdfDocument;
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
    //TODO: remove this when rdf4j starts supporting JSON-LD 1.1
    if(format.getName().contains("JSON-LD")){
      return writeToJsonLD(model);
    }

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

  //This is a hack - purposefully ugly just to make it work for the time being
  static String writeToJsonLD(Model model) {
    InputStream rdfStream = new ByteArrayInputStream(writeToString(RDFFormat.NTRIPLES, model).getBytes());
    try {
      Document document = RdfDocument.of(rdfStream);
      return JsonLd.fromRdf(document).get().toString();
    } catch (JsonLdError e) {
      e.printStackTrace();
    }
    return "";
  }

  private ReadWriteUtils() { }
}
