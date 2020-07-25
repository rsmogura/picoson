package net.rsmogura.picoson.research;

import net.rsmogura.picoson.gson.JsonReader;
import net.rsmogura.picoson.gson.JsonToken;

import java.io.IOException;

public class SampleUserAlgo {
  private String userName;
  private String email;

  private void $readJson(JsonReader reader) {
    try {
      reader.beginObject();
      while (reader.peek() != JsonToken.END_OBJECT) {
        String propertyName = reader.nextName();
        if ("userName".equals(propertyName)) {
          this.userName = reader.nextString();
        } else if ("email".equals(propertyName)) {
          this.email = reader.nextString();
        }
      }
    }catch(IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static SampleUserAlgo readJson(JsonReader reader) {
    SampleUserAlgo sampleUserAlgo = new SampleUserAlgo();
    sampleUserAlgo.$readJson(reader);
    return sampleUserAlgo;
  }
}
