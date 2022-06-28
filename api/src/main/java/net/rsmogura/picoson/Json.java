package net.rsmogura.picoson;

import net.rsmogura.picoson.abi.Names;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class Json {
  //XXX Can create memory leak, as both class and json support do not use weak references
  private static final ConcurrentHashMap<Class<?>, JsonSupport<?>> classToSupport
    = new ConcurrentHashMap<>();

  public static <T> JsonSupport<T> jsonSupport(Class<T> clazz) {
    return (JsonSupport<T>) classToSupport.computeIfAbsent(clazz, Json::findJsonSupportOrThrow);
  }

  private static <T> JsonSupport<T> findJsonSupportOrThrow(Class<T> clazz) {
    try {
      Field jsonSupportHolder = clazz.getDeclaredField(Names.SUPPORT_CLASS_HOLDER);
      jsonSupportHolder.setAccessible(true);
      return (JsonSupport<T>) jsonSupportHolder.get(null);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
