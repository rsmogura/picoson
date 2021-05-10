package net.rsmogura.picoson.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.CharArrayWriter;
import java.io.StringReader;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.tests.ReferencedTypes.InnerRef;
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

    InnerRef ir = new InnerRef();
    ir.setBaseTypes(new BaseTypes());
    ir.getBaseTypes().setL4(-99L);
    rt1.setInnerRef(ir);

    JsonSupport<ReferencedTypes> jsonSupport = ReferencedTypes.json();

    CharArrayWriter data = new CharArrayWriter();
    JsonWriter out = new JsonWriter(data);
    jsonSupport.write(rt1, out);
    out.close();

    final String s = data.toString();
    final JSONObject jsonObject = new JSONObject(s);
    //TODO Add intermediate tests with other parser

    final ReferencedTypes readType = jsonSupport.read(new JsonReader(new StringReader(s)));

    assertEquals(1, readType.getBaseTypes().getI1());
    assertEquals(-99L, readType.getInnerRef().getBaseTypes().getL4());
  }
}
