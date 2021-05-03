package net.rsmogura.picoson.tests;

import lombok.Data;
import net.rsmogura.picoson.annotations.Json;

@Json
@Data
public class ReferencedTypes {
  private BaseTypes baseTypes;

  private ReferencedTypes referencedTypes;

  private BaseTypes baseTypes2;

  private String name;
}
