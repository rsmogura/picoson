package net.rsmogura.picoson.tests;

import lombok.Data;
import net.rsmogura.picoson.annotations.Json;

@Json
@Data
public class ReferencedTypes {
  private BaseTypes baseTypes;

  private InnerRef innerRef;

  private String name;

  @Json
  @Data
  public static class InnerRef {
    BaseTypes baseTypes;
  }
}
