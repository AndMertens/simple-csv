package be.bornput.csv.processor;

import be.bornput.csv.annotation.CsvField;
import be.bornput.csv.annotation.CsvRecord;
import com.squareup.javapoet.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("be.bornput.csv.compiler.annotations.CsvRecord")
@SupportedSourceVersion(SourceVersion.RELEASE_17) // or your java version
public class CsvMapperProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = env.getMessager();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(CsvRecord.class)) {
            if (el.getKind() != ElementKind.CLASS) continue;
            TypeElement type = (TypeElement) el;
            try {
                generateMapperFor(type);
            } catch (Exception ex) {
                messager.printMessage(Diagnostic.Kind.ERROR, "CsvMapper generation failed: " + ex.getMessage());
            }
        }
        return true;
    }

    private void generateMapperFor(TypeElement type) throws IOException {
        String pkg = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
        String className = type.getSimpleName().toString();
        String mapperName = className + "CsvMapper";

        // Collect fields, while respecting @CsvFieldOrder and the @CsvIgnore option
        List<VariableElement> fields = type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .collect(Collectors.toList());

        // Build header list, writer lambdas and reader function code (string-based)
        // For brevity: we will create simple methods writeRow(T) and readRow(List<String>)
        // and a static getHeaders().

        // Create TypeName for the POJO
        ClassName pojoType = ClassName.get(type);

        // Generate getHeaders() method
        List<String> headers = new ArrayList<>();
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        // sort by @CsvField.order
        fields.sort(Comparator.comparingInt(f -> {
            CsvField ann = f.getAnnotation(CsvField.class);
            return ann == null ? Integer.MAX_VALUE : ann.order();
        }));

        for (VariableElement f : fields) {
            CsvField ann = f.getAnnotation(CsvField.class);
            if (ann != null && ann.ignore()) continue;
            String header = ann != null && !ann.name().isEmpty() ? ann.name() : f.getSimpleName().toString();
            headers.add(header);
        }

        TypeSpec.Builder mapper = TypeSpec.classBuilder(mapperName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // getHeaders method
        CodeBlock.Builder headersBlock = CodeBlock.builder()
                .add("java.util.List<String> headers = new java.util.ArrayList<>();\n");
        for (String h : headers) {
            headersBlock.add("headers.add($S);\n", h);
        }
        headersBlock.add("return headers;\n");

        MethodSpec getHeaders = MethodSpec.methodBuilder("getHeaders")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)))
                .addCode(headersBlock.build())
                .build();

        mapper.addMethod(getHeaders);

        // writeRow method (very simple: calls getters)
        CodeBlock.Builder writeRowCode = CodeBlock.builder()
                .add("java.util.List<String> row = new java.util.ArrayList<>();\n");

        for (VariableElement f : fields) {
            CsvField ann = f.getAnnotation(CsvField.class);
            if (ann != null && ann.ignore()) continue;

            String fname = f.getSimpleName().toString();
            // attempt getter name: getX or isX (for boolean) - basic convention
            String getter = "get" + capitalize(fname) + "()";
            // you can improve by checking methods present
            writeRowCode.add("row.add(String.valueOf(obj.$L));\n", getter); // naive: requires getter presence
        }
        writeRowCode.add("return row;\n");

        MethodSpec writeRow = MethodSpec.methodBuilder("writeRow")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, String.class))
                .addParameter(pojoType, "obj")
                .addCode(writeRowCode.build())
                .build();

        mapper.addMethod(writeRow);

        // Create a Pojo from a list of values: you must match constructor signature
        // For simplicity generate code that uses `new Xxx(values.get(0), Integer.parseInt(values.get(1)), ...)`
        CodeBlock.Builder readRowCode = CodeBlock.builder();
        readRowCode.add("return new $T(", pojoType);
        boolean first = true;
        for (VariableElement f : fields) {
            CsvField ann = f.getAnnotation(CsvField.class);
            if (ann != null && ann.ignore()) continue;

            TypeMirror t = f.asType();
            String parseExpr = getParseExpr(f, t, first, headers, ann);
            if (!first) readRowCode.add(", ");
            readRowCode.add(parseExpr);
            first = false;
        }
        readRowCode.add(");\n");

        MethodSpec readRow = MethodSpec.methodBuilder("readRow")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterizedTypeName.get(List.class, String.class), "values")
                .returns(pojoType)
                .addCode(readRowCode.build())
                .build();

        mapper.addMethod(readRow);

        JavaFile javaFile = JavaFile.builder(pkg + ".csvgen", mapper.build()).build();
        javaFile.writeTo(filer);
    }

    private String getParseExpr(VariableElement f, TypeMirror t, boolean first, List<String> headers, CsvField ann) {
       return getValueOf(f, t, first, headers, ann);
    }

    private String getValueOf(VariableElement f, TypeMirror t, boolean first, List<String> headers, CsvField ann) {
        return String.valueOf(parseExpressionForType(t, "values.get(" + (first ? "0" : Integer.toString(getIndexOf(f, headers, ann))) + ")"));
    }
    private static int getIndexOf(VariableElement f, List<String> headers, CsvField ann) {
        return headers.indexOf(
                ann != null && !ann.name().isEmpty() ? ann.name() : f.getSimpleName().toString());
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private CodeBlock parseExpressionForType(TypeMirror t, String valueExpr) {
        // Very small helper — expand for all your supported types
        String typeMirror = t.toString();
        return switch (typeMirror) {
            case "int", "java.lang.Integer" -> CodeBlock.of("Integer.parseInt($L)", valueExpr);
            case "long", "java.lang.Long" -> CodeBlock.of("Long.parseLong($L)", valueExpr);
            case "java.lang.String" -> CodeBlock.of("$L", valueExpr);
            case "double", "java.lang.Double" -> CodeBlock.of("Double.parseDouble($L)", valueExpr);
            default ->
                // fallback - raw string
                    CodeBlock.of("$L", valueExpr);
        };
    }
}
