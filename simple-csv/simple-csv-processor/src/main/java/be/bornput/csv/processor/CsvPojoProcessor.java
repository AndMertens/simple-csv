package be.bornput.csv.processor;

import be.bornput.csv.annotation.CsvPojo;
import be.bornput.csv.annotation.CsvColumn;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes("be.bornput.csv.annotations.CsvPojo")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class CsvPojoProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(CsvPojo.class)) {
            if (!(element instanceof TypeElement clazz)) continue;

            try {
                generateMapperFor(clazz);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(
                        javax.tools.Diagnostic.Kind.ERROR,
                        "Failed to generate CSV mapper: " + e.getMessage()
                );
            }
        }
        return true;
    }

    private void generateMapperFor(TypeElement clazz) throws IOException {
        String packageName = processingEnv.getElementUtils()
                .getPackageOf(clazz)
                .getQualifiedName()
                .toString();

        String className = clazz.getSimpleName().toString();
        String mapperName = className + "CsvMapper";

        List<Column> columns = extractColumns(clazz);

        JavaFileObject file = processingEnv.getFiler()
                .createSourceFile(packageName + "." + mapperName);

        try (Writer w = file.openWriter()) {
            w.write(generateSource(packageName, className, mapperName, columns));
        }
    }

    private static class Column {
        String fieldName;
        String header;
        int order;

        Column(String fieldName, String header, int order) {
            this.fieldName = fieldName;
            this.header = header;
            this.order = order;
        }
    }

    private List<Column> extractColumns(TypeElement clazz) {
        List<Column> cols = new ArrayList<>();

        for (Element e : clazz.getEnclosedElements()) {
            if (e.getKind() == ElementKind.FIELD && e.getAnnotation(CsvColumn.class) != null) {
                CsvColumn ann = e.getAnnotation(CsvColumn.class);
                cols.add(new Column(
                        e.getSimpleName().toString(),
                        ann.value(),
                        ann.order()
                ));
            }
        }

        cols.sort(Comparator.comparingInt(c -> c.order));
        return cols;
    }

    private String generateSource(
            String pkg, String pojo, String mapper, List<Column> columns
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import java.util.*;\n");
        sb.append("import java.io.*;\n\n");

        sb.append("public final class ").append(mapper).append(" {\n\n");

        // HEADER array
        sb.append("    private static final String[] HEADERS = {");
        sb.append(columns.stream().map(c -> "\"" + c.header + "\"").reduce((a,b)->a+","+b).orElse(""));
        sb.append("};\n\n");

        // writeCsv()
        sb.append("    public static void writeCsv(Writer out, List<")
                .append(pojo).append("> list) throws IOException {\n");
        sb.append("        out.write(String.join(\",\", HEADERS));\n");
        sb.append("        out.write(\"\\n\");\n\n");
        sb.append("        for (").append(pojo).append(" obj : list) {\n");
        sb.append("            List<String> vals = new ArrayList<>();\n");

        for (Column c : columns) {
            sb.append("            vals.add(String.valueOf(obj.")
                    .append(c.fieldName).append("));\n");
        }

        sb.append("            out.write(String.join(\",\", vals));\n");
        sb.append("            out.write(\"\\n\");\n");
        sb.append("        }\n");
        sb.append("    }\n\n");

        // readCsv()
        sb.append("    public static List<").append(pojo)
                .append("> readCsv(BufferedReader br) throws IOException {\n");
        sb.append("        List<").append(pojo).append("> out = new ArrayList<>();\n");
        sb.append("        String header = br.readLine();\n");
        sb.append("        if (header == null) return out;\n");
        sb.append("        String line;\n");
        sb.append("        while ((line = br.readLine()) != null) {\n");
        sb.append("            String[] parts = line.split(\",\");\n");
        sb.append("            ").append(pojo).append(" obj = new ").append(pojo).append("();\n");

        for (int i = 0; i < columns.size(); i++) {
            Column c = columns.get(i);
            sb.append("            obj.").append(c.fieldName)
                    .append(" = parseValue(parts[").append(i).append("]);\n");
        }

        sb.append("            out.add(obj);\n");
        sb.append("        }\n");
        sb.append("        return out;\n");
        sb.append("    }\n\n");

        // parseValue (very simple string for now)
        sb.append("    private static String parseValue(String s) { return s; }\n");

        sb.append("}\n");

        return sb.toString();
    }
}