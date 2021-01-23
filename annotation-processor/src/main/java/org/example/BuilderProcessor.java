package org.example;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * @author Japhy
 * @since 2021/1/17 01:54
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Kind.NOTE, "processing ----------");
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elementsAnnotatedWith =
                roundEnv.getElementsAnnotatedWith(annotation);
            Map<Boolean, ? extends List<? extends Element>> annotatedMethods =
                elementsAnnotatedWith.stream().collect(Collectors.partitioningBy(
                    element -> ((ExecutableType) element.asType()).getParameterTypes().size() == 1
                        && element.getSimpleName().toString().startsWith("set")));

            List<? extends Element> setters = annotatedMethods.get(true);
            List<? extends Element> others = annotatedMethods.get(false);
            others.forEach(element -> {
                messager.printMessage(Kind.ERROR,
                    "@org.example.DemoBuilder must be applied to SetXxx method with single arg", element);
            });
            if (setters.isEmpty()) {
               continue;
            }
            String className =
                ((TypeElement) setters.get(0).getEnclosingElement()).getQualifiedName().toString();

            Map<String, String> setterMap = setters.stream()
                .collect(Collectors.toMap(element -> element.getSimpleName().toString(),
                    element -> ((ExecutableType) element.asType()).getParameterTypes().get(0)
                        .toString()));

            try {
                writeBuilderFile(className, setterMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void writeBuilderFile(
        String className, Map<String, String> setterMap)
        throws IOException {

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + "Builder";
        String builderSimpleClassName = builderClassName
            .substring(lastDot + 1);

        JavaFileObject builderFile = processingEnv.getFiler()
            .createSourceFile(builderClassName);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.print("public class ");
            out.print(builderSimpleClassName);
            out.println(" {");
            out.println();

            out.print("    private ");
            out.print(simpleClassName);
            out.print(" object = new ");
            out.print(simpleClassName);
            out.println("();");
            out.println();

            out.print("    public ");
            out.print(simpleClassName);
            out.println(" build() {");
            out.println("        return object;");
            out.println("    }");
            out.println();

            setterMap.entrySet().forEach(setter -> {
                String methodName = setter.getKey();
                String argumentType = setter.getValue();

                out.print("    public ");
                out.print(builderSimpleClassName);
                out.print(" ");
                out.print(methodName);

                out.print("(");

                out.print(argumentType);
                out.println(" value) {");
                out.print("        object.");
                out.print(methodName);
                out.println("(value);");
                out.println("        return this;");
                out.println("    }");
                out.println();
            });

            out.println("}");
        }
    }
}
