package com.chigix.jena_stories.ontology;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.junit.Before;
import org.junit.Test;

/**
 * https://jena.apache.org/documentation/ontology/#ontology-meta-data
 */
public class MetaHeaderTest {

  private String expectedDublinCoreMetaResult;
  private String expectedOntologyImportResult;

  @Before
  public void setUP() {
    try {
      this.expectedDublinCoreMetaResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-dublin-core-metadata.owl"),
          StandardCharsets.UTF_8);
      this.expectedOntologyImportResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-ontology-import.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testBaseDeclaration() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("african-wildlife.owl"), null, "RDF/XML");
    Ontology baseOntology = model.getOntology("xml:base");
    assertThat(baseOntology, notNullValue(Ontology.class));
    assertThat(baseOntology.getURI(), equalTo("xml:base"));

    model.read(RDFDataMgr.open("african-wildlife.owl"), "http://ontology.chigix.com/some#", "RDF/XML");
    assertThat(model.getOntology("xml:base"), notNullValue(Ontology.class));
    assertThat(model.getOntology("xml:base").getURI(), equalTo("xml:base"));
    assertThat(model.getOntology("http://ontology.chigix.com/some"), nullValue());
  }

  @Test
  public void testBaseHeader() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("printer-ontology.owl"), null, "RDF/XML");
    Ontology baseOntology = model.getOntology("xml:base");
    assertThat(baseOntology, nullValue());

    model.read(RDFDataMgr.open("printer-ontology.owl"), "http://ontology.chigix.com/some#", "RDF/XML");
    assertThat(model.getOntology("xml:base"), nullValue());
    // https://jena.apache.org/documentation/ontology/#ontology-meta-data
    // rdf:about="" is a shorthand way of referencing the document's base URI
    // The base URI can be specified from read() method.
    assertThat(model.getOntology("http://ontology.chigix.com/some"), notNullValue(Ontology.class));

    // https://jena.apache.org/documentation/ontology/#the-generic-ontology-type-ontresource
    assertThat(model.getOntology("http://ontology.chigix.com/some").getComment(null),
        equalTo("An example that shows an ontology that describes African wildlife."));
  }

  /**
   * An extended usage of xmlbase in Semantic Web: https://www.w3.org/TR/xmlbase/
   */
  @Test
  public void testBasePreamble() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
        + " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
        + " xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
        // https://jena.apache.org/documentation/ontology/#ontology-meta-data
        // Use `xml:base` to declare document's base URI
        + " xml:base=\"http://ontology.chigix.com/some#\""
        + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" >"
        + "<owl:Ontology rdf:about=\"\" />"
        + "</rdf:RDF>").getBytes()),
        null, "RDF/XML");
    assertThat(model.getOntology("http://ontology.chigix.com/some"), notNullValue(Ontology.class));
    model.read(new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
        + " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
        + " xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
        + " xml:base=\"http://ontology.chigix.com/some#\""
        + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" >"
        // Declaration of `xml:base` will be overrided by `rdf:about`
        // Therefore, an empty string is expected and practicle on the `rdf:about` here.
        + "<owl:Ontology rdf:about=\"xml:base\" />"
        + "</rdf:RDF>").getBytes()),
        null, "RDF/XML");
    assertThat(model.getOntology("xml:base"), notNullValue(Ontology.class));
  }

  /**
   * https://jena.apache.org/documentation/ontology/#ontology-meta-data
   */
  @Test
  public void testDublinCoreMetadata() {
    OntModel model = ModelFactory.createOntologyModel();
    // Create an ontology named `xml:base`
    Ontology ont = model.createOntology("xml:base");

    // Attach Dublin Core Vocabulary
    ont.addProperty(DCTerms.creator, "John Smith");

    // https://jena.apache.org/tutorials/rdf_api.html#ch-Prefixes
    model.setNsPrefix("dc", "http://purl.org/dc/terms/");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    model.write(baos);
    assertThat(baos.toString(), equalTo(expectedDublinCoreMetaResult));
  }

  /**
   * https://jena.apache.org/documentation/ontology/#ontology-meta-data
   * `owl:imports` statements do not cause the corresponding document to be
   * imported by default.
   */
  @Test
  public void testOntologyImport() {
    OntModel model = ModelFactory.createOntologyModel();
    // Create an ontology
    Ontology ont = model.createOntology(null);
    ont.addImport(model.createResource("http://ontology.chigix.com/import1"));
    ont.addImport(model.createResource("http://ontology.chigix.com/import2"));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    model.write(baos);
    assertThat(baos.toString(), equalTo(expectedOntologyImportResult));
  }

  @Test
  public void testPrinterVersionGet() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("printer-ontology.owl"), "xml:base", "RDF/XML");
    Ontology baseOntology = model.getOntology("xml:base");

    // https://jena.apache.org/documentation/ontology/#the-generic-ontology-type-ontresource
    assertThat(baseOntology.getVersionInfo(),
        equalTo("\n      My example version 1.2, 17 October 2002\n    "));

    assertThat(baseOntology.getPriorVersion(), nullValue());
  }
}
