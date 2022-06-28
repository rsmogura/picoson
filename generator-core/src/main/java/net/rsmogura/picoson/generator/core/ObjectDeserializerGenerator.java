package net.rsmogura.picoson.generator.core;

import net.rsmogura.picoson.JsonToken;
import net.rsmogura.picoson.abi.JsonObjectDescriptor;
import net.rsmogura.picoson.abi.Names;
import net.rsmogura.picoson.generator.core.analyze.PropertiesCollector;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.util.Map;

import static net.rsmogura.picoson.abi.Names.DESCRIPTOR_HOLDER;
import static net.rsmogura.picoson.generator.core.BinaryNames.*;
import static org.objectweb.asm.Opcodes.*;

public class ObjectDeserializerGenerator extends AbstractMethodGenerator {
  public ObjectDeserializerGenerator(
      MethodVisitor mv,
      Type owner,
      Elements elements,
      Types typeUtils,
      PropertiesCollector propertiesCollector) {
    super(mv, owner, elements, typeUtils, propertiesCollector);
  }

  public void generate() {
    final Label propertyLoopStart = new Label();
    final Label propertyLoopEnd = new Label();

    final int jsonReaderSlot = 0;
    final int resultObjectSlot = 1;
    final int propertiesMapSlot = 2;

    generateNewObject(resultObjectSlot);
    generateLoadPropertiesMap(propertiesMapSlot);

    generateBeginObject(jsonReaderSlot);

    // while ((token = JsonReader.peek()) != JsonToken.END_OBJECT) {
    generateCheckIfEndOfObject(propertyLoopStart, propertyLoopEnd, jsonReaderSlot);

    mv.visitVarInsn(ALOAD, resultObjectSlot);

    //map.get(jsonReader.nextName()), if not found...
    generateFindPropertyDescriptor(jsonReaderSlot, propertiesMapSlot);

    //#jsonReadProp(propertyDescriptor, jsonReader)
    generateInvokePropertyReader(jsonReaderSlot);

    // } // goto while start
    mv.visitJumpInsn(GOTO, propertyLoopStart);
    mv.visitLabel(propertyLoopEnd);

    mv.visitVarInsn(ALOAD, jsonReaderSlot);
    mv.visitMethodInsn(INVOKEVIRTUAL,
      JSON_READER_NAME, "endObject", VOID_METHOD_DESCRIPTOR,
      false);
    mv.visitVarInsn(ALOAD, resultObjectSlot);
    mv.visitInsn(Opcodes.ARETURN);
  }

  protected void generateNewObject(final int resultObjectSlot) {
    mv.visitTypeInsn(NEW, owner.getInternalName());
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, owner.getInternalName(), "<init>",
      Type.getMethodDescriptor(Type.VOID_TYPE), false);
    mv.visitVarInsn(ASTORE, resultObjectSlot);
  }

  protected void generateLoadPropertiesMap(int propertiesMapSlot) {
    mv.visitFieldInsn(GETSTATIC, owner.getInternalName(), DESCRIPTOR_HOLDER, JSON_OBJECT_DESCRIPTOR);
    mv.visitMethodInsn(INVOKEVIRTUAL,
      Type.getInternalName(JsonObjectDescriptor.class), "getJsonProperties",
      Type.getMethodDescriptor(Type.getType(Map.class)),
      false);
    mv.visitVarInsn(ASTORE, propertiesMapSlot);
  }
  protected void generateBeginObject(final int jsonReaderSlot) {
    mv.visitVarInsn(ALOAD, jsonReaderSlot);
    mv.visitMethodInsn(INVOKEVIRTUAL,
      JSON_READER_NAME, "beginObject", VOID_METHOD_DESCRIPTOR,
      false);
  }

  protected void generateCheckIfEndOfObject(final Label propertyLoopStart,
                                            final Label propertyLoopEnd,
                                            final int jsonReaderSlot) {
    mv.visitLabel(propertyLoopStart);
    mv.visitVarInsn(ALOAD, jsonReaderSlot);
    mv.visitMethodInsn(INVOKEVIRTUAL,
      JSON_READER_NAME, "peek", JSON_TOKEN_RETURNING_METHOD,
      false);
    // On stack JsonToken

    // Load enum const
    mv.visitFieldInsn(GETSTATIC,
      JSON_TOKEN_INTERNAL_NAME, JsonToken.END_OBJECT.name(), JSON_TOKEN_DESCRIPTOR);
    //On stack peeked json token, begin object const
    mv.visitJumpInsn(IF_ACMPEQ, propertyLoopEnd);
  }

  protected void generateReadNextProperty(final int jsonReaderSlot) {
    mv.visitVarInsn(ALOAD, jsonReaderSlot);
    mv.visitMethodInsn(INVOKEVIRTUAL,
      JSON_READER_NAME, "nextName", STRING_RETURNING_METHOD,
      false);
    // On stack name
  }

  protected void generateFindPropertyDescriptor(int jsonReaderSlot, int propertiesMapSlot) {
    mv.visitVarInsn(ALOAD, propertiesMapSlot);
    generateReadNextProperty(jsonReaderSlot);
    mv.visitMethodInsn(INVOKEINTERFACE,
      MAP_INTERNAL_NAME, "get", OBJECT_OBJECT_METHOD_DESCRIPTOR,
      true);
    mv.visitTypeInsn(CHECKCAST, JSON_PROPERTY_DESCRIPTOR_NAME);
    // On stack PropertyDescriptor
  }

  protected void generateInvokePropertyReader(int jsonReaderSlot) {
    mv.visitVarInsn(ALOAD, jsonReaderSlot);
    mv.visitMethodInsn(INVOKEVIRTUAL,
      owner.getInternalName(), Names.READ_PROPERTY_NAME, READ_PROPERTY_DESCRIPTOR,
      false);
    mv.visitInsn(POP);
  }
}
