package ch.unisg.ics.interactions.wot.td.bindings;

import ch.unisg.ics.interactions.wot.td.affordances.Link;

import java.util.Collection;

public interface Response {

  enum ResponseStatus {
    OK,
    CONSUMER_ERROR,
    THING_ERROR,
    UNKNOWN_ERROR
  }

  ResponseStatus getStatus();

  Object getPayload();

  Collection<Link> getLinks();

}
