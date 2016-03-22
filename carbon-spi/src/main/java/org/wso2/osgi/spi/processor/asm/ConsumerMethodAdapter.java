package org.wso2.osgi.spi.processor.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.wso2.osgi.spi.processor.DynamicInject;

public class ConsumerMethodAdapter extends GeneratorAdapter implements Opcodes {

    private final Type DYNAMIC_INJECT_TYPE = Type.getType(DynamicInject.class);
    private final Type CLASS_TYPE = Type.getType(Class.class);
    private final Type CLASSLOADER_TYPE = Type.getType(ClassLoader.class);
    private final ConsumerClassVisitor consumerClassVisitor;
    private final String JAVA_SPI_CLASSPATH = "java/util/ServiceLoader";
    private final String JAVA_SPI_METHOD = "load";
    private final String JAVA_SPI_METHOD_SIGNATURE_DEFAULT = "(Ljava/lang/Class;)Ljava/util/ServiceLoader;";
    private final String JAVA_SPI_METHOD_SIGNATURE_CLASSLOADER = "(Ljava/lang/Class;Ljava/lang/ClassLoader;)Ljava/util/ServiceLoader;";

    private boolean isLdcTracked = false;
    private boolean hasClassLoaderParameter = false;
    private Type lastLdcType;

    public ConsumerMethodAdapter(ConsumerClassVisitor cv, MethodVisitor mv, int access, String name, String desc) {
        super(ASM5, mv, access, name, desc);
        this.consumerClassVisitor = cv;
    }


    @Override
    public void visitLdcInsn(Object cst) {
        if (!isLdcTracked && cst instanceof Type) {
            lastLdcType = ((Type) cst);
        }
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

        if (isSPICall(opcode, owner, name, desc, itf)) {

            isLdcTracked = true;

            Label startTry = newLabel();
            Label endTry = newLabel();

            //start try block
            visitTryCatchBlock(startTry, endTry, endTry, null);
            mark(startTry);

            // Add: DynamicInject.storeContextClassloader();
            invokeStatic(DYNAMIC_INJECT_TYPE, new Method("storeContextClassloader", Type.VOID_TYPE, new Type[0]));


            //Load the strings, method parameter and target
            visitLdcInsn(lastLdcType);
            visitLdcInsn(consumerClassVisitor.getConsumerClassType());

            //Change the class on the stack into a classloader
            invokeVirtual(CLASS_TYPE, new Method("getClassLoader",
                    CLASSLOADER_TYPE, new Type[0]));

            //Call our util method
            invokeStatic(DYNAMIC_INJECT_TYPE, new Method("fixContextClassloader", Type.VOID_TYPE,
                    new Type[]{CLASS_TYPE, CLASSLOADER_TYPE}));

            if (hasClassLoaderParameter) {

                visitInsn(POP);
                visitInsn(POP);
                visitLdcInsn(lastLdcType);

                invokeStatic(Type.getType(Thread.class), new Method("currentThread", Type.getType(Thread.class),
                        new Type[0]));
                invokeVirtual(Type.getType(Thread.class), new Method("getContextClassLoader",
                        CLASSLOADER_TYPE, new Type[0]));
            }

            //Call the original instruction
            super.visitMethodInsn(opcode, owner, name, desc, itf);

            //If no exception then go to the finally (finally blocks are a catch block with a jump)
            Label afterCatch = newLabel();
            goTo(afterCatch);


            //start the catch
            mark(endTry);
            //Run the restore method then throw on the exception
            invokeStatic(DYNAMIC_INJECT_TYPE, new Method("restoreContextClassloader", Type.VOID_TYPE, new Type[0]));
            throwException();

            //start the finally
            mark(afterCatch);
            //Run the restore and continue
            invokeStatic(DYNAMIC_INJECT_TYPE, new Method("restoreContextClassloader", Type.VOID_TYPE, new Type[0]));
            consumerClassVisitor.setModified();
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    private boolean isSPICall(int opcode, String owner, String name, String desc, boolean itf) {

        boolean isStatic = (opcode == INVOKESTATIC);
        boolean isJavaServiceLoader = owner.equals(JAVA_SPI_CLASSPATH);
        boolean isLoadMethod = name.equals(JAVA_SPI_METHOD);
        boolean isValidMethodSignature;

        switch (desc) {
            case JAVA_SPI_METHOD_SIGNATURE_DEFAULT:
                isValidMethodSignature = true;
                hasClassLoaderParameter = false;
                break;
            case JAVA_SPI_METHOD_SIGNATURE_CLASSLOADER:
                isValidMethodSignature = true;
                hasClassLoaderParameter = true;
                break;
            default:
                isValidMethodSignature = false;
                break;
        }

        return isStatic && isJavaServiceLoader && isLoadMethod && isValidMethodSignature && !itf;
    }
}
