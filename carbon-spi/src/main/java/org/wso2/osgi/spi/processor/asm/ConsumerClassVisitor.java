package org.wso2.osgi.spi.processor.asm;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ConsumerClassVisitor extends ClassVisitor implements Opcodes {

    private final Type consumerClassType;
    private boolean isModified = false;

    public ConsumerClassVisitor(ClassVisitor classVisitor, String consumerClassName) {
        super(ASM5, classVisitor);
        this.consumerClassType = Type.getType("L" + consumerClassName.replace('.', '/') + ";");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new ConsumerMethodAdapter(this, methodVisitor, access, name, desc);
    }

    public Type getConsumerClassType() {
        return consumerClassType;
    }

    public void setModified() {
        isModified = true;
    }

    public boolean isModified() {
        return isModified;
    }
}
