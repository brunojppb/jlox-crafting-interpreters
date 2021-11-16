package com.bpaulino.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output_directory>");
      System.exit(64);
    }
    String outputDir = args[0];
    defineAst(outputDir, "Expr", Arrays.asList(
       "Binary:   Expr left, Token operator, Expr right",
       "Grouping: Expr expression",
       "Literal:  Object value",
       "Unary:    Token operator, Expr right"
    ));
  }

  private static void defineAst(String outputDir, String basename, List<String> types) throws IOException {
    String path = outputDir + "/" + basename + ".java";
    PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

    writer.println("package com.bpaulino.parser;");
    writer.println();
    writer.println("import com.bpaulino.scanner.Token;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + basename + " {");

    for (String type: types) {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, basename, className, fields);
    }

    writer.println("}");
    writer.close();
  }

  private static void defineType(PrintWriter writer, String basename, String className, String fieldList) {
    // Class definition
    writer.println("  static class " + className + " extends " + basename + " {");
    // Class constructor
    writer.println("    " + className + "(" + fieldList + ") {");

    // Store class field values within the constructor
    String[] fields = fieldList.split(", ");
    for (String field: fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";");
    }
    // wrapping up the constructor
    writer.println("    }");
    writer.println();

    // class fields
    for(String field: fields) {
      writer.println("    final " + field + ";");
    }

    // Wrapping up the class
    writer.println("  }");
  }

}




























