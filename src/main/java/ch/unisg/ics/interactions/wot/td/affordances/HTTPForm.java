package ch.unisg.ics.interactions.wot.td.affordances;

import java.util.List;

public class HTTPForm {
  private String methodName;
  private String href;
  private String mediaType;
  private List<String> rel;
  
  public HTTPForm(String methodName, String href, String mediaType, List<String> rel) {
    this.methodName = methodName;
    this.href = href;
    this.mediaType = mediaType;
    this.rel = rel;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getHref() {
    return href;
  }

  public String getMediaType() {
    return mediaType;
  }

  public List<String> getRel() {
    return rel;
  }
}
