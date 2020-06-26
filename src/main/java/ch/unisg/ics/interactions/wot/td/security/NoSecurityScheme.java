package ch.unisg.ics.interactions.wot.td.security;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public class NoSecurityScheme extends SecurityScheme {

  @Override
  public String getSchemaType() {
    return WoTSec.NoSecurityScheme;
  }
  
}
