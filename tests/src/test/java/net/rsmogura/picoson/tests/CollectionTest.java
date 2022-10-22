package net.rsmogura.picoson.tests;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.rsmogura.picoson.JsonReader;
import net.rsmogura.picoson.JsonSupport;
import net.rsmogura.picoson.JsonWriter;
import net.rsmogura.picoson.annotations.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.CharArrayReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CollectionTest {
  @Test
  public void testRead() {
    JsonSupport<UserAccount> jsonSupport = net.rsmogura.picoson.Json.jsonSupport(UserAccount.class);
    LinkedList<UserAccount> result = jsonSupport.readCollection(new JsonReader(new StringReader("[" +
      "{\"userId\":\"a\", \"id\": 1},"+
      "{\"userId\":\"b\", \"id\": 2}" +
      "]")), new LinkedList<>());
    assertSame(LinkedList.class, result.getClass());
    assertEquals(2, result.size());
    assertEquals("a", result.get(0).userId);
    assertEquals(1, result.get(0).id);
    assertEquals("b", result.get(1).userId);
    assertEquals(2, result.get(1).id);
  }

  @Test
  public void testWrite() {
    JsonSupport<UserAccount> jsonSupport = net.rsmogura.picoson.Json.jsonSupport(UserAccount.class);
    List<UserAccount> accounts = new ArrayList<>();
    accounts.add(new UserAccount("a", 1));
    accounts.add(new UserAccount("b", 2));

    StringWriter sw = new StringWriter();
    jsonSupport.writeCollection(new JsonWriter(sw), accounts);

    assertEquals("[" +
      "{\"id\":1,\"userId\":\"a\"},"+
      "{\"id\":2,\"userId\":\"b\"}" +
      "]", sw.toString());
  }

  @Json
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserAccount {
    private String userId;
    private long id;
  }

  @Json
  public static class MultiObject {
    private List<UserAccount> userAccountsList;

    //private AccountsList accountsList;
  }

  public static class AccountsList extends LinkedList<UserAccount> {

  }
}
