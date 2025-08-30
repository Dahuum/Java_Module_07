package fr.school.annotations;

import com.google.auto.service.AutoService;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("*") // Process ALL annotations for debugging
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class HtmlProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("=== PROCESSOR RUNNING ===");
        
        for (Element element : roundEnv.getElementsAnnotatedWith(HtmlForm.class)) {
            System.out.println("Found element with @HtmlForm: " + element.getSimpleName());
            if (element.getKind() == ElementKind.CLASS) {
                processHtmlForm((TypeElement) element);
            }
        }
        return true;
    }

    private void processHtmlForm(TypeElement classElement) {
        System.out.println("Processing class: " + classElement.getSimpleName());
        HtmlForm htmlForm = classElement.getAnnotation(HtmlForm.class);
        
        if (htmlForm == null) {
            System.out.println("ERROR: @HtmlForm annotation is null!");
            return;
        }
        
        System.out.println("Creating file: " + htmlForm.fileName());
        
        try {
            FileObject file = processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT, "", htmlForm.fileName());
            
            System.out.println("File created successfully");
            
            PrintWriter writer = new PrintWriter(file.openOutputStream());
            
            writer.println("<form action = \"" + htmlForm.action() + "\" method = \"" + htmlForm.method() + "\">");
            
            for (Element field : classElement.getEnclosedElements()) {
                if (field.getKind() == ElementKind.FIELD) {
                    HtmlInput htmlInput = field.getAnnotation(HtmlInput.class);
                    if (htmlInput != null) {
                        System.out.println("Processing field: " + field.getSimpleName());
                        writer.println("\t<input type = \"" + htmlInput.type() + 
                                     "\" name = \"" + htmlInput.name() + 
                                     "\" placeholder = \"" + htmlInput.placeholder() + "\">");
                    }
                }
            }
            
            writer.println("\t<input type = \"submit\" value = \"Send\">");
            writer.println("</form>");
            
            writer.close();
            System.out.println("HTML file generation completed!");
            
        } catch (IOException e) {
            System.out.println("ERROR creating file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}