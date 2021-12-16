package ch.unisg.ics.interactions.wot.td.bindings;

public interface Operation {

  void setPayload();

  Response execute();

}
