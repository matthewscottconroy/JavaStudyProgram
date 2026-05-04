package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class SerializationQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.SERIALIZATION; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ser-mc-01", Topic.SERIALIZATION, 2,
                "What interface must a class implement to be serializable using ObjectOutputStream?",
                "Storable", "Cloneable", "Serializable", "Encodable",
                "c",
                "java.io.Serializable is a marker interface (no methods) that opts a class into Java's built-in "
                + "serialization mechanism. ObjectOutputStream can then write an instance to a byte stream, "
                + "and ObjectInputStream can reconstruct it. All non-transient, non-static fields must also be serializable."),

            mc("ser-mc-02", Topic.SERIALIZATION, 2,
                "What does the 'transient' keyword do to a field during serialization?",
                "Makes the field serialize first",
                "Prevents the field from being serialized",
                "Makes the field read-only after deserialization",
                "Encrypts the field during serialization",
                "b",
                "A transient field is skipped during serialization — its value is not written to the stream. "
                + "After deserialization it receives the default value (null for objects, 0 for numbers, false for boolean). "
                + "Use transient for passwords, cached computed values, or non-serializable references."),

            mc("ser-mc-03", Topic.SERIALIZATION, 3,
                "What is serialVersionUID used for?",
                "To set the version of Java used to compile the class",
                "To uniquely identify the class version so deserialization can detect incompatible changes",
                "To control the order in which fields are serialized",
                "To specify how many instances can be deserialized",
                "b",
                "serialVersionUID is a version stamp. If you deserialize a stream with a different "
                + "serialVersionUID than the current class definition, an InvalidClassException is thrown. "
                + "Declare it as 'private static final long serialVersionUID = 1L;' to control compatibility explicitly."),

            mc("ser-mc-04", Topic.SERIALIZATION, 3,
                "Which class writes a serializable object to a file?",
                "FileWriter", "ObjectOutputStream", "DataOutputStream", "PrintWriter",
                "b",
                "ObjectOutputStream wraps another OutputStream (typically FileOutputStream) "
                + "and provides writeObject(). "
                + "To read back: ObjectInputStream wraps FileInputStream and provides readObject(). "
                + "The file is binary — do not try to open it in a text editor."),

            trace("ser-tr-01", Topic.SERIALIZATION, 3,
                "What is printed after serializing and deserializing?",
                "import java.io.*;\n"
                + "class Box implements Serializable {\n"
                + "    int size = 10;\n"
                + "    transient String label = \"fragile\";\n"
                + "}\n"
                + "// serialize Box to bytes, then deserialize\n"
                + "Box b = /* deserialized Box */;\n"
                + "System.out.println(b.size + \" \" + b.label);",
                "10 null",
                "size (non-transient) is preserved. label (transient) is not serialized, "
                + "so after deserialization it reverts to the default value for String: null."),

            debug("ser-db-01", Topic.SERIALIZATION, 3,
                "This code throws NotSerializableException. Why?",
                "class Engine {  // does NOT implement Serializable\n"
                + "    int horsepower = 300;\n"
                + "}\n"
                + "class Car implements Serializable {\n"
                + "    Engine engine = new Engine();\n"
                + "}\n"
                + "// ObjectOutputStream.writeObject(new Car()) throws",
                "Car must have a no-arg constructor to be serializable",
                "All non-transient fields must also be serializable — Engine is not",
                "Engine must be a static nested class",
                "writeObject() can only serialize primitive fields",
                "b",
                "Java's serialization recursively serializes every non-transient field. "
                + "Engine doesn't implement Serializable, so the runtime throws NotSerializableException. "
                + "Fix: make Engine implement Serializable, or declare engine as transient."),

            codegen("ser-cg-01", Topic.SERIALIZATION, 3,
                "Which snippet correctly serializes an object 'dog' to a file?",
                "FileWriter fw = new FileWriter(\"dog.dat\"); fw.write(dog.toString());",
                "try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(\"dog.dat\"))) { oos.writeObject(dog); }",
                "Files.writeString(Path.of(\"dog.dat\"), dog.toString());",
                "ObjectOutputStream.serialize(dog, \"dog.dat\");",
                "b",
                "ObjectOutputStream.writeObject() serializes the object graph to binary format. "
                + "Wrapping in try-with-resources ensures the stream is closed (and the object header flushed) "
                + "even if an exception occurs. "
                + "Option A writes a String representation, not binary serialization."),

            mc("ser-mc-05", Topic.SERIALIZATION, 2,
                "What happens to static fields during Java serialization?",
                "Static fields are serialized before instance fields",
                "Static fields are not serialized — they belong to the class, not the instance",
                "Static fields are serialized only if they are final",
                "Static fields cause a NotSerializableException",
                "b",
                "Serialization saves the state of a specific object instance. "
                + "Static fields are part of the class, not an instance, so they are not written to the stream. "
                + "After deserialization, static fields retain whatever value the class has in the running JVM."),

            mc("ser-mc-06", Topic.SERIALIZATION, 3,
                "What is the purpose of custom readObject() and writeObject() methods?",
                "To replace the Serializable interface",
                "To control exactly how the object's state is written and read during serialization",
                "To define which fields are transient",
                "To enable serialization of final classes",
                "b",
                "Declaring private void writeObject(ObjectOutputStream oos) and readObject(ObjectInputStream ois) "
                + "lets you customize serialization logic — encrypt sensitive fields, validate state on deserialization, "
                + "or serialize computed values. The JVM calls these methods instead of the default mechanism."),

            mc("ser-mc-07", Topic.SERIALIZATION, 3,
                "What exception is thrown when deserializing an object whose class is not found on the classpath?",
                "NotSerializableException", "ClassCastException", "ClassNotFoundException", "InvalidClassException",
                "c",
                "ObjectInputStream.readObject() throws ClassNotFoundException if the class definition "
                + "is not available in the current JVM's classpath. "
                + "InvalidClassException is thrown when the serialVersionUID doesn't match. "
                + "NotSerializableException is thrown during serialization (write side)."),

            trace("ser-tr-02", Topic.SERIALIZATION, 3,
                "What is the value of 'count' after deserialization?",
                "class Counter implements Serializable {\n"
                + "    int count = 5;\n"
                + "    static int instances = 10;\n"
                + "    transient int cache = 99;\n"
                + "}\n"
                + "// Counter c serialized then deserialized in fresh JVM where instances=0\n"
                + "// What is c.count after deserialization?",
                "5",
                "count is a non-transient instance field — it is serialized and restored to 5. "
                + "cache (transient) would be 0. instances (static) stays at whatever the class has in the new JVM."),

            trace("ser-tr-03", Topic.SERIALIZATION, 3,
                "What is printed?",
                "class Box implements Serializable {\n"
                + "    private static final long serialVersionUID = 1L;\n"
                + "    int size;\n"
                + "    Box(int size) { this.size = size; }\n"
                + "}\n"
                + "// serialize Box(42) then deserialize into Box b\n"
                + "System.out.println(b.size);",
                "42",
                "The non-transient field 'size' is serialized and restored. "
                + "The explicit serialVersionUID ensures compatibility if the class is recompiled without structural changes."),

            debug("ser-db-02", Topic.SERIALIZATION, 3,
                "Deserialization throws InvalidClassException. What is the most likely cause?",
                "// Original class had: private static final long serialVersionUID = 1L;\n"
                + "// New version of the class has: private static final long serialVersionUID = 2L;\n"
                + "// Trying to deserialize a stream written with the original class",
                "The class must be final to be deserializable",
                "The stream was written with serialVersionUID=1 but the current class has serialVersionUID=2 — version mismatch",
                "The class name changed",
                "ObjectInputStream cannot read files larger than 1 MB",
                "b",
                "Java checks serialVersionUID during deserialization. If the stored value differs from the class's current value, "
                + "it throws InvalidClassException. "
                + "If you add a field and keep the same serialVersionUID, Java tolerates it; the new field gets its default value. "
                + "Changing the UID intentionally breaks backward compatibility."),

            codegen("ser-cg-02", Topic.SERIALIZATION, 3,
                "Which snippet correctly reads a serialized object from a file?",
                "FileReader fr = new FileReader(\"data.ser\"); Object o = fr.read();",
                "try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(\"data.ser\"))) { MyClass obj = (MyClass) ois.readObject(); }",
                "MyClass obj = Serializable.read(\"data.ser\", MyClass.class);",
                "byte[] bytes = Files.readAllBytes(Path.of(\"data.ser\")); MyClass obj = (MyClass) bytes;",
                "b",
                "ObjectInputStream wraps a FileInputStream and readObject() returns Object — cast to the expected type. "
                + "Try-with-resources closes the stream. "
                + "readObject() throws both IOException and ClassNotFoundException, so both must be handled.")
        );
    }
}
