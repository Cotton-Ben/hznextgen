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

    public void generate(TypeElement classElement) {
        SectorClassModel clazz = new SectorClassModel(classElement);

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
