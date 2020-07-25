package net.rsmogura.picoson.research;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class JsonClassFinder {
  static volatile Object o;

  ConcurrentHashMap<Class, Consumer<Object>> map = new ConcurrentHashMap<>();

  JsonClassFinder() {
    map.put(JsonObject1.class, (o) -> ((JsonObject1) o).serializeJsonTest());
    map.put(JsonObject2.class, (o) -> ((JsonObject2) o).serializeJsonTest());
  }

  public void serialize(Object o) throws Exception {
    map.get(o.getClass()).accept(o);
  }

  public void deserialize(Class c) throws Exception {
    if (c == JsonObject1.class) {
      JsonObject1.deserializeJsonTest();
    } else if (c == JsonObject2.class) {
      JsonObject2.deserializeJsonTest();
    }
    System.out.println(c.getCanonicalName());
  }
  /** Check if class is loaded during using .class. usefull for loading during initialization. */
  public static void testClassLoading() {
    new JsonClassFinder();
  }

  public static void main(String[] args) throws Exception {
    JsonClassFinder jsonClassFinder = new JsonClassFinder();
    double d = new Random(System.currentTimeMillis()).nextDouble();
    System.out.println(d);
    if (d > 0.5) {
      o = new JsonObject1();
    } else {
      o = new JsonObject2();
    }

    jsonClassFinder.serialize(o);
    jsonClassFinder.deserialize(o.getClass());
  }
}
