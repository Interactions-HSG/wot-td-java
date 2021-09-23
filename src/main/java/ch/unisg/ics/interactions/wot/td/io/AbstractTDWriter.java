package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;

/**
 * A writer for serializing TDs.
 * Provides a fluent API for adding prefix bindings to be used for serialization.
 */
public abstract class AbstractTDWriter implements TDWriter {

  protected final ThingDescription td;

  protected AbstractTDWriter(ThingDescription td) {
    this.td = td;
  }

  protected abstract AbstractTDWriter setNamespace(String prefix, String namespace);

  protected abstract AbstractTDWriter addTypes();

  protected abstract AbstractTDWriter addTitle();

  protected abstract AbstractTDWriter addSecurity();

  protected abstract AbstractTDWriter addBaseURI();

  protected abstract AbstractTDWriter addProperties();

  protected abstract AbstractTDWriter addActions();

  protected abstract AbstractTDWriter addGraph();

}
