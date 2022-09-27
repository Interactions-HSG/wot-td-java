package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.io.json.TDJsonWriter;
import org.eclipse.rdf4j.rio.RDFFormat;

public interface TDWriter {

  /**
   * Writes out as a String the Thing Description associated with this writer.
   * @return a string representing the Thing Description in the correct format.
   */
  String write();

  //TODO this should not be dependent from RDFFormat
  static String write(ThingDescription td, RDFFormat format) {
    if(format.equals(RDFFormat.JSONLD) || format.equals(RDFFormat.NDJSONLD)) {
      return new TDJsonWriter(td).write();
    }
    return new TDGraphWriter(td).write();
  }
}
