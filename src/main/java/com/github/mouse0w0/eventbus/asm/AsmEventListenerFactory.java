package com.github.mouse0w0.eventbus.asm;

import com.github.mouse0w0.eventbus.Event;
import com.github.mouse0w0.eventbus.misc.EventListener;
import com.github.mouse0w0.eventbus.misc.EventListenerFactory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public final class AsmEventListenerFactory implements EventListenerFactory {

    public static AsmEventListenerFactory create() {
        return new AsmEventListenerFactory();
    }

    private static final String[] INTERFACES = {Type.getInternalName(EventListener.class)};
    private static final String[] THROWS = {Type.getInternalName(Exception.class)};
    private static final String EVENT_TYPE_DESC = Type.getDescriptor(Event.class);
    private static final String METHOD_POST_DESC = "(" + EVENT_TYPE_DESC + ")V";

    private final ClassDefiner classDefiner = new ClassDefiner();
    private final AtomicInteger uniqueId = new AtomicInteger(1);
    private final Map<Method, Class<?>> cachedWrappers = new HashMap<>();

    @Override
    public EventListener create(Class<?> eventType, Object owner, Method method, boolean isStatic) throws Exception {
        Constructor<?> constructor = createWrapper(eventType, owner, method, isStatic).getConstructors()[0];
        return (EventListener) (isStatic ? constructor.newInstance() : constructor.newInstance(owner));
    }

    private Class<?> createWrapper(Class<?> eventType, Object owner, Method method, boolean isStatic) {
        if (cachedWrappers.containsKey(method)) {
            return cachedWrappers.get(method);
        }

        Class<?> ownerType = isStatic ? (Class<?>) owner : owner.getClass();
        String declClassName = Type.getInternalName(method.getDeclaringClass());
        String declClassDesc = Type.getDescriptor(method.getDeclaringClass());
        String eventName = Type.getInternalName(eventType);
        String methodName = method.getName();
        String methodDesc = Type.getMethodDescriptor(method);
        String wrapperName = getUniqueName(ownerType.getSimpleName(), methodName, eventType.getSimpleName());

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, wrapperName, null, "java/lang/Object", INTERFACES);

        cw.visitSource(".dynamic", null);

        if (!isStatic) {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "owner", declClassDesc, null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                    isStatic ? "()V" : "(" + declClassDesc + ")V",
                    isStatic ? "()V" : "(" + declClassDesc + ")V", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, wrapperName, "owner", declClassDesc);
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "post", METHOD_POST_DESC, null, THROWS);
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, wrapperName, "owner", declClassDesc);
            }
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventName);
            mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, declClassName, methodName, methodDesc, false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        Class<?> wrapper = classDefiner.defineClass(ownerType.getClassLoader(), wrapperName, cw.toByteArray());
        cachedWrappers.put(method, wrapper);
        return wrapper;
    }

    private String getUniqueName(String ownerName, String handlerName, String eventName) {
        return "AsmDynamicListener_" + Integer.toHexString(System.identityHashCode(this)) + "_" + ownerName + "_" + handlerName + "_" + eventName + "_" + uniqueId.getAndIncrement();
    }
}
