package com.hazelcast2.spi;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.LinkedList;
import java.util.List;

public class SectorClassModel {

    public String name;
    public String superName;
    public String packageName;
    public final List<SectorMethod> methods = new LinkedList<>();
    public String cellName;

    public SectorClassModel(TypeElement classElement) {
        name = "Generated" + classElement.getSimpleName();
        superName = classElement.getSimpleName().toString();
        packageName = CodeGenerationUtils.getPackageNameFromQualifiedName(classElement.getQualifiedName().toString());

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (!enclosedElement.getKind().equals(ElementKind.METHOD)) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) enclosedElement;

            SectorMethod.Builder builder = new SectorMethod.Builder();
            builder.methodElement = methodElement;
            builder.classElement = classElement;
            builder.sectorClassModel = this;
            SectorMethod sectorMethod = builder.build();
            if (sectorMethod != null) {
                methods.add(sectorMethod);
            }

            if (methodElement.getSimpleName().toString().equals("loadCell")) {
                cellName = methodElement.getReturnType().toString();
            }
        }
    }

    public String getCellName() {
        return cellName;
    }

    public String getName() {
        return name;
    }

    public String getSuperName() {
        return superName;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<SectorMethod> getMethods() {
        return methods;
    }
}
