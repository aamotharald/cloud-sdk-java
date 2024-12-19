/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.sdk.datamodel.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sap.cloud.sdk.datamodel.openapi.generator.exception.OpenApiGeneratorException;
import com.sap.cloud.sdk.datamodel.openapi.generator.model.ApiMaturity;
import com.sap.cloud.sdk.datamodel.openapi.generator.model.GenerationConfiguration;

import io.vavr.control.Try;

@MojoTest
class DataModelGeneratorMojoUnitTest
{

    static final String POM_XML = "/pom.xml";

    static final String PLUGIN_TEST_DIR = "src/test/resources/DataModelGeneratorMojoUnitTest/";

    static final String POM_testAdditionalPropertiesAndEnablingAnyOfOneOf =
        PLUGIN_TEST_DIR + "testAdditionalPropertiesAndEnablingAnyOfOneOf" + POM_XML;
    static final String POM_testEmptyRequiredParameter = PLUGIN_TEST_DIR + "testEmptyRequiredParameter" + POM_XML;
    static final String POM_testInvocationWithAllParameters =
        PLUGIN_TEST_DIR + "testInvocationWithAllParameters" + POM_XML;
    static final String POM_testInvocationWithMandatoryParameters =
        PLUGIN_TEST_DIR + "testInvocationWithMandatoryParameters" + POM_XML;
    static final String POM_testSkipExecution = PLUGIN_TEST_DIR + "testSkipExecution" + POM_XML;
    static final String POM_testInvocationWithUnexpectedApiMaturity =
        PLUGIN_TEST_DIR + "testInvocationWithUnexpectedApiMaturity" + POM_XML;

    @TempDir
    File outputDirectory;

    private DataModelGeneratorMojo sut;

    @Test
    @InjectMojo( goal = "generate", pom = POM_testInvocationWithAllParameters )
    void testInvocationWithAllParameters( DataModelGeneratorMojo mojo )
        throws MojoExecutionException
    {
        final GenerationConfiguration configuration = mojo.retrieveGenerationConfiguration().get();

        assertThat(configuration.getApiMaturity()).isEqualTo(ApiMaturity.RELEASED);
        assertThat(configuration.isVerbose()).isTrue();
        assertThat(configuration.getOutputDirectory()).isEqualTo("output-directory");
        assertThat(configuration.getInputSpec())
            .isEqualTo("DataModelGeneratorMojoUnitTest/testInvocationWithAllParameters/input/sodastore.yaml");
        assertThat(configuration.getModelPackage()).isEqualTo("com.sap.cloud.sdk.datamodel.rest.test.model");
        assertThat(configuration.getApiPackage()).isEqualTo("com.sap.cloud.sdk.datamodel.rest.test.api");
        assertThat(configuration.deleteOutputDirectory()).isTrue();
        assertThat(configuration.isOneOfAnyOfGenerationEnabled()).isFalse();

        mojo.setOutputDirectory(outputDirectory.getAbsolutePath());

        mojo.execute();
    }

    @Test
    @InjectMojo( goal = "generate", pom = POM_testInvocationWithMandatoryParameters )
    void testInvocationWithMandatoryParameters( DataModelGeneratorMojo mojo )
        throws MojoExecutionException
    {

        final GenerationConfiguration configuration = mojo.retrieveGenerationConfiguration().get();

        assertThat(configuration.getApiMaturity()).isEqualTo(ApiMaturity.RELEASED);
        assertThat(configuration.isVerbose()).isFalse();
        assertThat(configuration.getOutputDirectory()).isEqualTo("output-directory");
        assertThat(configuration.getInputSpec())
            .isEqualTo("DataModelGeneratorMojoUnitTest/testInvocationWithMandatoryParameters/input/sodastore.yaml");
        assertThat(configuration.getModelPackage()).isEqualTo("com.sap.cloud.sdk.datamodel.rest.test.model");
        assertThat(configuration.getApiPackage()).isEqualTo("com.sap.cloud.sdk.datamodel.rest.test.api");
        assertThat(configuration.deleteOutputDirectory()).isFalse();

        mojo.setOutputDirectory(outputDirectory.getAbsolutePath());

        mojo.execute();
    }

    @Test
    @InjectMojo( goal = "generate", pom = POM_testEmptyRequiredParameter )
    void testEmptyRequiredParameter( DataModelGeneratorMojo mojo )
        throws MojoExecutionException
    {

        final Try<Void> mojoExecutionTry = Try.run(mojo::execute);

        assertThat(mojoExecutionTry.isFailure()).isTrue();

        assertThat(mojoExecutionTry.getCause())
            .isInstanceOf(MojoExecutionException.class)
            .hasCauseInstanceOf(OpenApiGeneratorException.class);
        assertThat(mojoExecutionTry.getCause().getCause().getSuppressed())
            .satisfiesExactly(
                e -> assertThat(e).isInstanceOf(IllegalArgumentException.class),
                e -> assertThat(e).isInstanceOf(IllegalArgumentException.class));
    }

    @Test
    @InjectMojo( goal = "generate", pom = POM_testSkipExecution )
    void testSkipExecution( DataModelGeneratorMojo mojo )
        throws MojoExecutionException
    {
        mojo.execute();
        //no reasonable assertion possible
    }

    @Test
    @InjectMojo( goal = "generate", pom = POM_testInvocationWithUnexpectedApiMaturity )
    void testInvocationWithUnexpectedApiMaturity( DataModelGeneratorMojo mojo )
        throws MojoExecutionException
    {

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(mojo::execute)
            .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @InjectMojo( goal = "generate", pom = POM_testAdditionalPropertiesAndEnablingAnyOfOneOf )
    void testAdditionalPropertiesAndEnablingAnyOfOneOf( DataModelGeneratorMojo mojo )
        throws MojoExecutionException
    {

        assertThat(mojo.retrieveGenerationConfiguration().get().getAdditionalProperties())
            .containsEntry("param1", "val1")
            .containsEntry("param2", "val2")
            .containsEntry("useAbstractionForFiles", "true");

        assertThat(mojo.retrieveGenerationConfiguration().get().isOneOfAnyOfGenerationEnabled()).isTrue();
    }
}
