package ch.unisg.ics.interactions.wot.td.schemas;

import java.util.Set;

public class StringSchema extends DataSchema {

  private StringSchema(Set<String> semanticTypes) {
    super(DataSchema.STRING, semanticTypes);
  }
  
  public static class Builder extends DataSchema.Builder<StringSchema, StringSchema.Builder> {

    @Override
    public StringSchema build() {
      return new StringSchema(this.semanticTypes);
    }
  }
}
