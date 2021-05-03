package net.rsmogura.picoson.tests;

import java.io.CharArrayWriter;
import java.util.Map;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonWriter;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class ReferenceTypesTest {

  @Test
  public void testReadWrite() throws Exception {
    ReferencedTypes rt1 = new ReferencedTypes();
    BaseTypes bt1_1 = new BaseTypes();
    bt1_1.setI1(1);
    rt1.setBaseTypes(bt1_1);
    rt1.setName("rt1");

    JsonSupport<ReferencedTypes> jsonSupport = ReferencedTypes.json();

    CharArrayWriter data = new CharArrayWriter();
    JsonWriter out = new JsonWriter(data);
    jsonSupport.write(rt1, out);
    out.close();

    final String s = data.toString();
    final Map<String, JSONObject> vals = (Map<String, JSONObject>) JSONObject.stringToValue(s);

  }
}
