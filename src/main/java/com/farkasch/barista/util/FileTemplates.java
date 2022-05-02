package com.farkasch.barista.util;

import com.farkasch.barista.services.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileTemplates {

  @Autowired
  PersistenceService persistenceService;

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

  public String interfaceTemplate(String className, String classPath){
    String content = createPackage(classPath)
      + "public interface " + className + " {\n"
      + "}";

    return content;
  }

  public String annotationTemplate(String className, String classPath){
    String content = createPackage(classPath)
      + "public @interface " + className + " {\n"
      + "}";

    return content;
  }

  public String recordTemplate(String className, String classPath){
    String content = createPackage(classPath)
      + "public record " + className + "() {}";

    return content;
  }

  private String createPackage(String classPath) {
    String packageName = classPath.substring(persistenceService.getOpenProject().getSourceRoot().length());
    packageName = packageName.replace("\\", ".");

    return packageName.length() == 0 ? "" : "package " + packageName.substring(1) + ";\n\n";
  }
}
