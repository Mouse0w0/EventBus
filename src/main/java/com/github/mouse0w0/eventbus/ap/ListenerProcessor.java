package com.github.mouse0w0.eventbus.ap;


import com.github.mouse0w0.eventbus.Event;
import com.github.mouse0w0.eventbus.Listener;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.mouse0w0.eventbus.ap.ProcessingUtils.getQualifiedName;
import static com.github.mouse0w0.eventbus.ap.ProcessingUtils.hasModifier;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ListenerProcessor extends AbstractProcessor {

    private static final String CLASS_NAME = Listener.class.getName();

    private final static String EVENT_CLASS_NAME = Event.class.getName();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(CLASS_NAME);
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for (Element element : roundEnv.getElementsAnnotatedWith(Listener.class)) {
                ExecutableElement method = (ExecutableElement) element;

                TypeElement owner = (TypeElement) method.getEnclosingElement();
                List<? extends VariableElement> parameters = method.getParameters();

                if (parameters.size() != 1) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("The count of listener method parameter must be 1. Listener: %s.%s(?)", owner.getQualifiedName(), method.getSimpleName()));
                }

                VariableElement event = parameters.get(0);

                if (!processingEnv.getTypeUtils().isAssignable(event.asType(), processingEnv.getElementUtils().getTypeElement(EVENT_CLASS_NAME).asType())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("The parameter of listener method must be Event or it's child class. Listener: %s.%s(%s)", owner.getQualifiedName(), method.getSimpleName(), getQualifiedName(event.asType())));
                }

                if (!hasModifier(method, Modifier.PUBLIC)) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Listener method must be public. Listener: %s.%s(%s)", owner.getQualifiedName(), method.getSimpleName(), getQualifiedName(event.asType())));
                }

                if (method.getReturnType().getKind() != TypeKind.VOID) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("The return type of listener method must be void. Listener: %s.%s(%s)", owner.getQualifiedName(), method.getSimpleName(), getQualifiedName(event.asType())));
                }
            }
        }
        return false;
    }
}
