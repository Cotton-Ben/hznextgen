package com.hazelcast2.spi;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@SupportedAnnotationTypes("com.hazelcast2.spi.SectorClass")
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
            template = cfg.getTemplate("SectorTemplate.ftl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(SectorClass.class)) {
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
        SectorClassModel clazz = new SectorClassModel();
        clazz.name = "Generated" + classElement.getSimpleName();
        clazz.superName = classElement.getSimpleName().toString();
        clazz.packageName = getPackageNameFromQualifiedName(classElement.getQualifiedName().toString());

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (!enclosedElement.getKind().equals(ElementKind.METHOD)) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) enclosedElement;
            SectorOperation operationAnnotation = methodElement.getAnnotation(SectorOperation.class);

            if (operationAnnotation != null) {
                String methodName = methodElement.getSimpleName().toString();

                int argCount = methodElement.getParameters().size();

                SectorMethod method = new SectorMethod();
                clazz.methods.add(method);

                boolean cellbased = operationAnnotation.cellbased();
                method.cellbased = cellbased;
                method.name = "hz_" + methodName;

                AsyncMethod asyncMethod = new AsyncMethod();
                method.asyncMethod = asyncMethod;
                method.originalMethod = buildOriginalMethod(methodElement);
                asyncMethod.name = "hz_async" + capitalizeFirstLetter(methodName);
                asyncMethod.returnType = "InvocationFuture";


                method.returnType = methodElement.getReturnType().toString();
                method.invocationClassName = capitalizeFirstLetter(methodName) + argCount + "Invocation";
                method.targetMethod = methodName;
                method.readonly = operationAnnotation.readonly();
                method.functionId = clazz.methods.size();

                List<FormalArgument> args = new LinkedList<>();

                for (VariableElement variableElement : methodElement.getParameters()) {
                    FormalArgument formalArgument = new FormalArgument();
                    if (cellbased && args.isEmpty()) {
                        formalArgument.name = "id";
                        formalArgument.type = "long";
                    } else {
                        formalArgument.name = "arg" + (args.size() + 1);
                        formalArgument.type = variableElement.asType().toString();
                    }
                    args.add(formalArgument);
                }


                method.formalArguments = args;


                asyncMethod.formalArguments = args;
            } else if (methodElement.getSimpleName().toString().equals("loadCell")) {
                clazz.cellName = methodElement.getReturnType().toString();
            }
        }
        return clazz;
    }

    public OriginalMethod buildOriginalMethod(ExecutableElement methodElement) {
        OriginalMethod method = new OriginalMethod();
        method.returnType = methodElement.getReturnType().toString();
        method.name = methodElement.getSimpleName().toString();

        List<FormalArgument> args = method.formalArguments;
        SectorOperation operationAnnotation = methodElement.getAnnotation(SectorOperation.class);

        for (VariableElement variableElement : methodElement.getParameters()) {
            FormalArgument formalArgument = new FormalArgument();
            if (operationAnnotation.cellbased() && args.isEmpty()) {
                formalArgument.name = "cell";
            } else {
                formalArgument.name = "arg" + (args.size() + 1);
            }
            formalArgument.type = variableElement.asType().toString();
            args.add(formalArgument);
        }
        return method;
    }

    public void generate(TypeElement classElement) {
        SectorClassModel clazz = generateClassModel(classElement);

        String content = null;
        try {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("class", clazz);

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
