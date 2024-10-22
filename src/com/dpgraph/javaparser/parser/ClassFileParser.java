package com.dpgraph.javaparser.parser;

import com.dpgraph.javaparser.core.JavaClass;
import com.dpgraph.javaparser.core.JavaPackage;
import com.dpgraph.javaparser.core.builder.FieldBuilder;
import com.dpgraph.javaparser.core.builder.MethodBuilder;
import com.dpgraph.javaparser.util.ClassFileConstants;
import com.dpgraph.javaparser.util.Modifier;
import com.dpgraph.javaparser.util.PackageFilter;

import java.io.*;

public class ClassFileParser extends Parser {


    private String fileName;
    private String className;
    private String superClassName;
    private String[] interfaceNames;
    private boolean isAbstract;
    private JavaClass javaClass;
    private Constant[] constantPool;
    private FieldOrMethodInfo[] fields;
    private FieldOrMethodInfo[] methods;
    private AttributeInfo[] attributes;
    private DataInputStream inputStream;


    public ClassFileParser(PackageFilter filter) {
        super(filter);
        reset();
    }

    public ClassFileParser() {
        this(new PackageFilter());
    }

    public JavaClass parse(File file) throws IOException {
        this.fileName = file.getCanonicalPath();

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            return parse(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JavaClass parse(InputStream inputStream) throws IOException {
        reset();

        javaClass = new JavaClass("Unknown");

        this.inputStream = new DataInputStream(inputStream);

        int magic = parseMagic();

        int minorVersion = parseMinorVersion();
        int majorVersion = parseMajorVersion();

        constantPool = parseConstantPool();

        parseAccessFlags();

        className = parseClassName();

        superClassName = parseSuperClassName();

        interfaceNames = parseInterfaces();

        fields = parseFields();

        methods = parseMethods();

        parseAttributes();

        addClassConstantReferences();

        addDependencies();

        addAnnotationsReferences();

        return javaClass;
    }

    private void addDependencies() {
        DependencyExtractor extractor = new DependencyExtractor();
        javaClass.setOutGoingDependencies(extractor.extract(javaClass));
    }

    private void addClassConstantReferences() throws IOException {
        for (int i = 1; i < constantPool.length; i++) {
            Constant constant = constantPool[i];

            if (constant.getTag() == ClassFileConstants.CONSTANT_CLASS) {
                String name = toUTF8(constant.getNameIndex());
                addImport(getPackageName(name));
            }

            if (constant.getTag() == ClassFileConstants.CONSTANT_DOUBLE || constant.getTag() == ClassFileConstants.CONSTANT_LONG) {
                i++;
            }
        }
    }

    private int addAnnotationElementValueReferences(byte[] data, int index) throws IOException {
        byte tag = data[index];
        index += 1;

        switch (tag) {
            case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 's' -> index += 2;
            case 'e' -> {
                int enumIndex = u2(data, index);
                addImport(getPackageName(toUTF8(enumIndex).substring(1)));
                index += 4;
            }
            case 'c' -> {
                int classInfoIndex = u2(data, index);
                addImport(getPackageName(toUTF8(classInfoIndex).substring(1)));
                index += 2;
            }
            case '@' -> index = addAnnotationReferences(data, index, 1);
            case '[' -> {
                int numValues = u2(data, index);
                index += 2;
                for(int i=0; i<numValues; i++) {
                    index = addAnnotationElementValueReferences(data, index);
                }
            }
        }

        return index;
    }

    private void addAnnotationReferences(AttributeInfo attributeInfo) throws IOException {
        byte[] data = attributeInfo.getValue();
        int numAnnotations = u2(data, 0);
        int annotationIndex = 2;

        addAnnotationReferences(data, annotationIndex, numAnnotations);
    }

    private void addAnnotationsReferences() throws IOException {
        for (int i = 1; i < attributes.length; i++) {
            AttributeInfo attribute = attributes[i];
            if (attribute.getName().equals("RuntimeVisibleAnnotations")) {
                addAnnotationReferences(attribute);
            }
        }

        for (int i = 1; i < fields.length; i++) {
            FieldOrMethodInfo field = fields[i];
            if (field.getRuntimeVisibleAnnotations() != null) {
                addAnnotationReferences(field.getRuntimeVisibleAnnotations());
            }
        }

        for (int i = 1; i < methods.length; i++) {
            FieldOrMethodInfo method = methods[i];
            if(method.getRuntimeVisibleAnnotations() != null) {
                addAnnotationReferences(method.getRuntimeVisibleAnnotations());
            }
        }
    }


    private static Modifier getModifier(int accessFlags) {
        boolean isPrivate = ((accessFlags & 0x0002) != 0);
        boolean isPublic = ((accessFlags & 0x0001) != 0);
        boolean isProtected = ((accessFlags & 0x0004) != 0);

        Modifier modifier;

        if(isPrivate) {
            modifier = Modifier.PRIVATE;
        } else if(isPublic) {
            modifier = Modifier.PUBLIC;
        } else if(isProtected){
            modifier = Modifier.PROTECTED;
        } else {
            modifier = Modifier.DEFAULT;
        }
        return modifier;
    }

    private int addAnnotationReferences(byte[] data, int index, int numAnnotations) throws IOException {
        int visitedAnnotations = 0;

        while (visitedAnnotations < numAnnotations) {
            int typeIndex = u2(data, index);
            int numElementValuePairs = u2(data, index=index+2);
            addImport(getPackageName(toUTF8(typeIndex).substring(1)));
            int visitedElementValuePairs = 0;
            index += 2;

            while (visitedElementValuePairs < numElementValuePairs) {
                index = addAnnotationElementValueReferences(data, index = index + 2);
                visitedElementValuePairs++;
            }
            visitedAnnotations++;
        }
        return index;
    }

    private int u2(byte[] data, int index) {
        return (data[index] << 8 & 0xFF0) | (data[index+1] & 0xFF);
    }

    private void parseAttributes() throws IOException {
        int attributesCount = inputStream.readUnsignedShort();
        attributes = new AttributeInfo[attributesCount];

        for(int i=0; i<attributesCount; i++) {
            attributes[i] = parseAttribute();

            if(attributes[i].getName() != null) {
                byte[] b = attributes[i].getValue();
                int b0 = b[0] < 0 ? b[0] + 256 : b[0];
                int b1 = b[1] < 0 ? b[1] + 256 : b[1];

                int pe = b0 * 256 + b1;

                if(attributes[i].getName().equals("SourceFile")) {
                    String descriptor = toUTF8(pe);
                    javaClass.setSourceFile(descriptor);
                } else if(attributes[i].getName().equals("Signature")) {
                    String descriptor = toUTF8(pe);
                    javaClass.setSignature(descriptor);
                }
            }
        }
    }

    private int parseMagic() throws IOException {
        int magic = inputStream.readInt();
        if(magic != ClassFileConstants.JAVA_MAGIC) {
            throw new IOException("Invalid class file" + fileName);
        }

        return magic;
    }

    private Constant[] parseConstantPool() throws IOException {
        int constantPoolSize = inputStream.readUnsignedShort();

        Constant[] pool = new Constant[constantPoolSize];

        for(int i=1; i<pool.length; i++) {
            Constant constant = parseNextConstant();

            pool[i] = constant;

            if(constant.getTag() == ClassFileConstants.CONSTANT_DOUBLE || constant.getTag() == ClassFileConstants.CONSTANT_LONG) {
                i++;
            }
        }

        return pool;
    }

    private void parseAccessFlags() throws IOException {
        int accessFlags = inputStream.readUnsignedShort();

        boolean isAbstract = ((accessFlags & ClassFileConstants.ACC_ABSTRACT) != 0);
        boolean isInterface = ((accessFlags & ClassFileConstants.ACC_INTERFACE) != 0);
        boolean isFinal = ((accessFlags & ClassFileConstants.ACC_FINAL) != 0);

        this.isAbstract = isAbstract || isInterface;
        javaClass.isAbstract(this.isAbstract);
        javaClass.setInterface(isInterface);
        javaClass.setFinal(isFinal);
    }

    private String parseClassName() throws IOException {
        int entryIndex = inputStream.readUnsignedShort();
        String className = getClassConstantName(entryIndex);
        javaClass.setName(className);
        javaClass.setPackageName(getPackageName(className));

        return className;
    }

    private String parseSuperClassName() throws IOException {
        int entryIndex = inputStream.readUnsignedShort();
        String superClassName = getClassConstantName(entryIndex);
        addImport(getPackageName(superClassName));

        return superClassName;
    }

    private void addImport(String packageName) {
        if(packageName != null && getFilter().accept(packageName)) {
            javaClass.addImportedPackage(new JavaPackage(packageName));
        }
    }

    private String[] parseInterfaces() throws IOException {
        int interfacesCount = inputStream.readUnsignedShort();
        String[] interfaceNames = new String[interfacesCount];

        for(int i=0; i<interfacesCount; i++) {
            int entryIndex = inputStream.readUnsignedShort();
            interfaceNames[i] = getClassConstantName(entryIndex);
            addImport(getPackageName(interfaceNames[i]));
        }

        return interfaceNames;
    }

    private FieldOrMethodInfo[] parseFields() throws IOException {
        int fieldsCount = inputStream.readUnsignedShort();
        FieldOrMethodInfo[] fields = new FieldOrMethodInfo[fieldsCount];
        FieldBuilder fieldBuilder = new FieldBuilder(constantPool);

        for(int i=0; i<fieldsCount; i++) {
            fields[i] = parseFieldOrMethodInfo();
            String descriptor = toUTF8(fields[i].getDescriptorIndex());

            String[] types = descriptorToTypes(descriptor);
            for (String type : types) {
                addImport(getPackageName(type));
            }

            javaClass.addField(fieldBuilder.parse(fields[i]));
        }



        return fields;
    }

    private FieldOrMethodInfo[] parseMethods() throws IOException {
        int methodsCount = inputStream.readUnsignedShort();
        MethodBuilder parser = new MethodBuilder(constantPool);

        FieldOrMethodInfo[] result = new FieldOrMethodInfo[methodsCount];

        for(int i=0; i<methodsCount; i++) {
            result[i] = parseFieldOrMethodInfo();
            String descriptor = toUTF8(result[i].getDescriptorIndex());


            javaClass.addMethod(parser.parse(result[i]));

            String[] types = descriptorToTypes(descriptor);
            for(String type : types) {
                if(!type.isEmpty()) {
                    addImport(getPackageName(type));
                }
            }

        }

        return result;
    }

    private FieldOrMethodInfo parseFieldOrMethodInfo() throws IOException {
        FieldOrMethodInfo result = new FieldOrMethodInfo(
                inputStream.readUnsignedShort(),
                inputStream.readUnsignedShort(),
                inputStream.readUnsignedShort()
        );

        int attributesCount = inputStream.readUnsignedShort();
        for(int a=0; a < attributesCount; a++) {
            AttributeInfo attribute = parseAttribute();
            if(attribute.getName().equals("RuntimeVisibleAnnotations")) {
                result.setRuntimeVisibleAnnotations(attribute);
            } else if(attribute.getName().equals("Signature")) {
                result.setSignature(attribute);
            }
        }

        return result;
    }

    private AttributeInfo parseAttribute() throws IOException{
        AttributeInfo result = new AttributeInfo();

        int nameIndex = inputStream.readUnsignedShort();
        if(nameIndex != -1) {
            result.setName(toUTF8(nameIndex));
        }

        int attributeLength = inputStream.readInt();
        byte[] value = new byte[attributeLength];

        for(int b = 0; b<attributeLength; b++) {
            value[b] = inputStream.readByte();
        }

        result.setValue(value);
        return result;
    }

    private String getPackageName(String s) {
        if(!s.isEmpty() && s.charAt(0) == '[') {
            String[] types = descriptorToTypes(s);
            if(types.length == 0) {
                return null;
            }

            s = types[0];
        }

        s = s.replace('/', '.');
        int index = s.lastIndexOf(".");
        if(index > 0) {
            return s.substring(0, index);
        }
        return "Default";
    }

    private String[] descriptorToTypes(String s) {
        int typesCount = 0;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == ';') {
                typesCount++;
            }
        }

        String[] types = new String[typesCount];

        int typeIndex = 0;
        for (int i = 0; i < s.length(); i++) {
            int startIndex = s.indexOf(ClassFileConstants.CLASS_DESCRIPTOR, i);
            if(startIndex < 0) {
                break;
            }

            i = s.indexOf(';', startIndex + 1);
            types[typeIndex++] = s.substring(startIndex + 1, i);
        }

        return types;
    }

    private String getClassConstantName(int entryIndex) throws IOException{
        Constant entry = getConstantPoolEntry(entryIndex);

        if(entry == null) {
            return "";
        }
        return toUTF8(entry.getNameIndex()).replace('/', '.');
    }

    private String toUTF8(int entryIndex) throws IOException {
        Constant entry = getConstantPoolEntry(entryIndex);

        if(entry.getTag() == ClassFileConstants.CONSTANT_UTF8) {
            return (String) entry.getValue();
        }

        throw new IOException("com.dpgraph.parser.Constant pool entry is not a UTF8 type: " + entryIndex);
    }

    private Constant getConstantPoolEntry(int entryIndex) throws IOException{
        if(entryIndex < 0 || entryIndex >= constantPool.length) {
            return null;
            // throw new IOException("Illegal constant pool index" + entryIndex);
        }

        return constantPool[entryIndex];
    }

    private int parseMinorVersion() throws IOException {
        return inputStream.readUnsignedShort();
    }

    private int parseMajorVersion() throws IOException {
        return inputStream.readUnsignedShort();
    }

    private Constant parseNextConstant() throws IOException {

        byte tag = inputStream.readByte();

        return switch (tag) {
            case (ClassFileConstants.CONSTANT_CLASS), (ClassFileConstants.CONSTANT_STRING),
                 (ClassFileConstants.CONSTANT_METHOD_TYPE) -> new Constant(tag, inputStream.readUnsignedShort());
            case (ClassFileConstants.CONSTANT_FIELD), (ClassFileConstants.CONSTANT_METHOD),
                 (ClassFileConstants.CONSTANT_INTERFACE_METHOD), (ClassFileConstants.CONSTANT_NAME_AND_TYPE),
                 (ClassFileConstants.CONSTANT_INVOKEDYNAMIC) ->
                    new Constant(tag, inputStream.readUnsignedShort(), inputStream.readUnsignedShort());
            case (ClassFileConstants.CONSTANT_INTEGER) -> new Constant(tag, Integer.valueOf(inputStream.readInt()));
            case (ClassFileConstants.CONSTANT_FLOAT) ->
                // may cause some problems with boxing
                    new Constant(tag, inputStream.readFloat());
            case (ClassFileConstants.CONSTANT_LONG) ->
                // may cause some problems with boxing
                    new Constant(tag, inputStream.readLong());
            case (ClassFileConstants.CONSTANT_DOUBLE) ->
                // may cause some problems with boxing
                    new Constant(tag, inputStream.readDouble());
            case (ClassFileConstants.CONSTANT_UTF8) ->
                // may cause some problems with boxing
                    new Constant(tag, inputStream.readUTF());
            case (ClassFileConstants.CONSTANT_METHOD_HANDLE) ->
                // may cause some problems with boxing
                    new Constant(tag, inputStream.readByte(), inputStream.readUnsignedShort());
            case 0 -> new Constant(tag, inputStream.readByte(), inputStream.readUnsignedShort());
            default -> throw new IOException("Unknown com.dpgraph.parser.Constant: " + tag);
        };

    }

    private void reset() {
        className = null;
        superClassName = null;
        interfaceNames = new String[0];
        isAbstract = false;

        javaClass = null;
        constantPool = new Constant[0];
        fields = new FieldOrMethodInfo[0];
        methods = new FieldOrMethodInfo[0];
        attributes = new AttributeInfo[0];
    }

    public String toString() {

        StringBuilder s = new StringBuilder();

        try {

            s.append("\n").append(className).append(":\n");

            s.append("\nConstants:\n");
            for (int i = 1; i < constantPool.length; i++) {
                Constant entry = getConstantPoolEntry(i);
                s.append("    ").append(i).append(". ").append(entry.toString()).append("\n");
                if (entry.getTag() == ClassFileConstants.CONSTANT_DOUBLE
                        || entry.getTag() == ClassFileConstants.CONSTANT_LONG) {
                    i++;
                }
            }

            s.append("\nClass Name: ").append(className).append("\n");
            s.append("Super Name: ").append(superClassName).append("\n\n");

            s.append("Signature: \n").append(javaClass.getSignature()).append("\n\n");

            s.append(interfaceNames.length).append(" interfaces\n");
            for (String interfaceName : interfaceNames) {
                s.append("    ").append(interfaceName).append("\n");
            }

            s.append("\n").append(fields.length).append(" fields\n");
            for (FieldOrMethodInfo field : fields) {
                s.append(field.toString()).append("\n");
            }

            s.append("\n").append(methods.length).append(" methods\n");
            for (FieldOrMethodInfo method : methods) {
                s.append(method.toString()).append("\n");
            }

            s.append("\nDependencies:\n");
            for(JavaPackage javaPackage : javaClass.getImportedPackages()) {
                s.append("    ").append(javaPackage.getName()).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return s.toString();
    }

    public static void main(String[] args) {
        try {
            if(args.length == 0) {
                System.err.println("usage: com.dpgraph.parser.parser.ClassFileParser <class-file>");
                System.exit(0);
            }

            ClassFileParser parser = new ClassFileParser();

           JavaClass clazz = parser.parse(new File(args[0]));

            System.out.println(clazz.toString());

            System.err.println(parser.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }



}
