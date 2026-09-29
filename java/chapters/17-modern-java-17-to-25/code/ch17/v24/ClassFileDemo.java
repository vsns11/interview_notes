package ch17.v24;

import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.classfile.instruction.InvokeInstruction;
import java.util.ArrayList;
import java.util.List;

/** JEP 484 (Java 24): a standard API to parse, generate and transform class files. */
public class ClassFileDemo {
    static int square(int x) { return x * x; }

    public static void main(String[] args) throws Exception {
        byte[] bytes;
        try (InputStream in = ClassFileDemo.class.getResourceAsStream("ClassFileDemo.class")) {
            bytes = in.readAllBytes();                                // our own compiled class
        }
        ClassModel cm = ClassFile.of().parse(bytes);
        System.out.println("class   : " + cm.thisClass().asInternalName()
                + "  (class-file major version " + cm.majorVersion() + ")");
        for (MethodModel m : cm.methods()) {
            int instructions = 0;
            List<String> calls = new ArrayList<>();
            if (m.code().isPresent()) {
                for (CodeElement e : m.code().get()) {                // walk the bytecode as typed objects
                    if (e instanceof Instruction) instructions++;
                    if (e instanceof InvokeInstruction inv) calls.add(inv.name().stringValue());
                }
            }
            System.out.printf("method  : %-8s %-24s %3d instructions, %d calls%s%n", m.methodName(), m.methodType(),
                    instructions, calls.size(), calls.size() <= 3 ? " " + calls : "");
        }
        System.out.println("square(7) = " + square(7));
    }
}
