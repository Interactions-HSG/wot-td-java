package ch.unisg.ics.interactions.wot.td.io.json;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.io.AbstractTDWriter;

/**
 * A writer to serialize TDs in the JSON-LD 1.1 format.
 */
public class TDJsonWriter extends AbstractTDWriter {

  public TDJsonWriter(ThingDescription td) {
    super(td);
  }

  @Override
  public String write() {
    return null;
  }

  @Override
  public AbstractTDWriter setNamespace(String prefix, String namespace) {
    return null;
  }

  @Override
  protected AbstractTDWriter addTypes() {
    return null;
  }

  @Override
  protected AbstractTDWriter addTitle() {
    return null;
  }

  @Override
  protected AbstractTDWriter addSecurity() {
    return null;
  }

  @Override
  protected AbstractTDWriter addBaseURI() {
    return null;
  }

  @Override
  protected AbstractTDWriter addProperties() {
    return null;
  }

  @Override
  protected AbstractTDWriter addActions() {
    return null;
  }

  @Override
  protected AbstractTDWriter addGraph() {
    return null;
  }
}
