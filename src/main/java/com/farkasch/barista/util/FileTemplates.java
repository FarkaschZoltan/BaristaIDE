package com.farkasch.barista.util;

import com.farkasch.barista.services.PersistenceService;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileTemplates {

  @Autowired
  private PersistenceService persistenceService;

  public String mainTemplate() {
    String content =
      "public class Main {\n"
        + "  public static void main(String args[]) {\n"
        + "    System.out.println(\"Hello World!\");\n"
        + "  }\n"
        + "}";
    return content;
  }

  public String classTemplate(String className, String classPath) {
    String content = createPackage(classPath)
      + "public class " + className + " {\n"
      + "}";

    return content;
  }

  public String enumTemplate(String className, String classPath) {
    String content = createPackage(classPath)
      + "public enum " + className + " {\n"
      + "}";

    return content;
  }

  public String interfaceTemplate(String className, String classPath) {
    String content = createPackage(classPath)
      + "public interface " + className + " {\n"
      + "}";

    return content;
  }

  public String annotationTemplate(String className, String classPath) {
    String content = createPackage(classPath)
      + "public @interface " + className + " {\n"
      + "}";

    return content;
  }

  public String recordTemplate(String className, String classPath) {
    String content = createPackage(classPath)
      + "public record " + className + "() {}";

    return content;
  }

  public String createSetter(String variableName, String variableClass) {
    String content =
      "  public void set" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1) + "(" + variableClass + " " + variableName
        + ") {\n"
        + "    this." + variableName + " = " + variableName + ";\n"
        + "  }\n\n";

    return content;
  }

  public String createGetter(String variableName, String variableClass) {
    String content = "  public " + variableClass + " get" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1) + "() {\n"
      + "    return " + variableName  + ";\n"
      + "  }\n\n";

    return content;
  }

  public String createConstructor(HashMap<String, String> variables, String className){
    StringBuilder arguments = new StringBuilder();
    StringBuilder content = new StringBuilder();
    String argumentString;
    String constructor;
    for(String name : variables.keySet()){
      arguments.append(variables.get(name));
      arguments.append(" ");
      arguments.append(name);
      arguments.append(", ");

      content.append("  this.");
      content.append(variables.get(name));
      content.append(" = ");
      content.append(variables.get(name));
      content.append(";\n");
    }

    if(arguments.length() > 0){
      argumentString = "(" + arguments.substring(0, arguments.length()-2) + ")";
    } else {
      argumentString = "()";
    }
     constructor = "  public " + className + argumentString + " {\n"
       + content.toString()
       + "  }\n\n";

    return constructor;
  }

  public String createPackage(String classPath) {
    String packageName = classPath.substring(persistenceService.getOpenProject().getSourceRoot().length());
    packageName = packageName.replace("\\", ".");

    return packageName.length() == 0 ? "" : "package " + packageName.substring(1) + ";\n\n";
  }

  public String createImport(String filePath) {
    String importString = filePath.substring(persistenceService.getOpenProject().getSourceRoot().length());
    importString = importString.replaceAll("(\\.java)", "");
    importString = importString.replace("\\", ".");

    return importString.length() == 0 ? "" : "import " + importString.substring(1) + ";\n";
  }
}
