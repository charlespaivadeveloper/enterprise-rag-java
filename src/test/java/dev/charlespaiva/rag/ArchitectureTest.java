package dev.charlespaiva.rag;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("dev.charlespaiva.rag");
    }

    @Test
    void entitiesMustNotDependOnOuterLayers() {
        noClasses().that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "..usecase..",
                "..interfaceadapter..",
                "..framework..")
            .check(classes);
    }

    @Test
    void useCasesMustNotDependOnInterfaceAdaptersOrFrameworks() {
        noClasses().that().resideInAPackage("..usecase..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..interfaceadapter..",
                "..framework..",
                "org.springframework..",
                "jakarta.persistence..")
            .check(classes);
    }

    @Test
    void interfaceAdaptersMustNotDependOnFramework() {
        noClasses().that().resideInAPackage("..interfaceadapter..")
            .should().dependOnClassesThat().resideInAPackage("..framework..")
            .check(classes);
    }
}
