package com.timcritt.tfg.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final String BASE = "com.timcritt.tfg";

    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String PORTS = "..application.port..";
    private static final String SERVICES = "..application.service..";
    private static final String INFRASTRUCTURE = "..infrastructure..";

    @Test
    void hexagonal_architecture_should_be_followed() {
        JavaClasses classes = new ClassFileImporter().importPackages(BASE);

        ArchRule domainMustBeIndependent = ArchRuleDefinition.noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, INFRASTRUCTURE)
                .allowEmptyShould(true);

        ArchRule applicationMustNotDependOnInfrastructure = ArchRuleDefinition.noClasses()
                .that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                .allowEmptyShould(true);

        ArchRule portsMustNotDependOnInfrastructure = ArchRuleDefinition.noClasses()
                .that().resideInAPackage(PORTS)
                .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                .allowEmptyShould(true);

        // Application services should only depend on application/domain/core Java packages — no Spring framework types allowed.
        ArchRule servicesShouldOnlyDependOnCore = ArchRuleDefinition.classes()
                .that().resideInAPackage(SERVICES)
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        SERVICES,
                        APPLICATION,
                        PORTS,
                        DOMAIN,
                        "java..",
                        "javax..",
                        "jakarta..",
                        "lombok.."
                )
                .allowEmptyShould(true);

        ArchRule coreMustNotKnowInfrastructure = ArchRuleDefinition.noClasses()
                .that().resideInAnyPackage(DOMAIN, APPLICATION)
                .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                .allowEmptyShould(true);

        domainMustBeIndependent.check(classes);
        applicationMustNotDependOnInfrastructure.check(classes);
        portsMustNotDependOnInfrastructure.check(classes);
        servicesShouldOnlyDependOnCore.check(classes);
        coreMustNotKnowInfrastructure.check(classes);
    }
}
