package com.hazelcast2.spi.foo2.codegenerator;

import com.hazelcast2.spi.foo2.Foo2OperationMethod;
import com.hazelcast2.spi.foo2.Foo2SectorAnnotation;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("com.hazelcast2.spi.foo2.Foo2SectorAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SectorCodeGenerator extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Template template;

    @Override
    public void init(ProcessingEnvironment env) {
        filer = env.getFiler();
        messager = env.getMessager();

        Configuration cfg = new Configuration();
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));
        try {
            template = cfg.getTemplate("Foo2SectorTemplate.ftl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Foo2SectorAnnotation.class)) {
            generate((TypeElement) element);
        }

        return true;
    }

    public static String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String getPackageNameFromQualifiedName(String qualifiedClassName) {
        return qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("."));
    }

    private SectorClassModel generateClassModel(TypeElement classElement) {
        final SectorClassModel clazz = new SectorClassModel();
        clazz.name = "Generated" + classElement.getSimpleName();
        clazz.superName = classElement.getSimpleName().toString();
        clazz.packageName = getPackageNameFromQualifiedName(classElement.getQualifiedName().toString());

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement methodElement = (ExecutableElement) enclosedElement;

                Foo2OperationMethod operationAnnotation = methodElement.getAnnotation(Foo2OperationMethod.class);

                if (operationAnnotation != null) {
                    String methodName = methodElement.getSimpleName().toString();

                    int argCount = methodElement.getParameters().size();

                    SectorMethodModel method = new SectorMethodModel();
                    clazz.methods.add(method);

                    method.name = "do" + capitalizeFirstLetter(methodName);
                    method.returnType = methodElement.getReturnType().toString();
                    method.invocationClassName = capitalizeFirstLetter(methodName) + argCount + "Invocation";
                    method.targetMethod = methodName;
                    method.readonly = operationAnnotation.readonly();
                    int functionId = clazz.methods.size();
                    method.functionId = method.readonly ? -functionId : functionId;

                    for (VariableElement variableElement : methodElement.getParameters()) {
                        method.args.add(variableElement.asType().toString());
                    }


                    this.messager.printMessage(Diagnostic.Kind.WARNING, methodName + " " + method.args);

                } else if (methodElement.getSimpleName().toString().equals("loadCell")) {
                    clazz.cellName = methodElement.getReturnType().toString();
                }
            }
        }
        return clazz;
    }

    public void generate(TypeElement classElement) {
        SectorClassModel clazz = generateClassModel(classElement);

        String content = null;
        try {
            // Build the data-model
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("class", clazz);

            // // Console output
            //Writer out = new OutputStreamWriter(System.out);
            //template.process(data, out);
            //out.flush();

            StringWriter writer = new StringWriter();
            template.process(data, writer);
            content = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JavaFileObject file;
        try {
            file = filer.createSourceFile(
                    clazz.packageName + "." + clazz.name,
                    classElement);
            file.openWriter()
                    .append(content)
                    .close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
