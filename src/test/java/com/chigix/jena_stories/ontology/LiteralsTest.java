package com.chigix.jena_stories.ontology;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;

/**
 * https://jena.apache.org/tutorials/rdf_api.html#ch-More-about-Literals-and-Datatypes
 * https://github.com/apache/jena/blob/master/jena-core/src-examples/jena/examples/rdf/Tutorial11.java
 */
public class LiteralsTest {

  private String expectedLanguageTagResult;
  private Model expectedDuplicateLiteralResult;

  @Before
  public void testUp() {
    try {
      this.expectedLanguageTagResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-literals-language-tag.rdf"),
          StandardCharsets.UTF_8);
      expectedDuplicateLiteralResult = ModelFactory.createDefaultModel();
      expectedDuplicateLiteralResult.read(RDFDataMgr.open("snapshot-literals-duplicate.nt"), "",
          Lang.NTRIPLES.getName());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testLanguageTag() {
    Model model = ModelFactory.createDefaultModel();
    Resource res = model.createResource();

    // Add Property
    res.addProperty(RDFS.label, model.createLiteral("chat", "en"))
        .addProperty(RDFS.label, model.createLiteral("チャット", "jp"))
        .addProperty(RDFS.label, model.createLiteral("<em>chat</em>", true));

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFDataMgr.write(baos, model, Lang.RDFXML);
    assertEquals(expectedLanguageTagResult, baos.toString(StandardCharsets.UTF_8));
  }

  @Test
  public void testDuplicateLiterals() {
    Model model = ModelFactory.createDefaultModel();
    Resource res = model.createResource();

    // Add Property with a simple literal
    res.addProperty(RDFS.label, "11")
        .addProperty(RDFS.label, "11"); // whether duplicate value would be merged

    final ByteArrayOutputStream baosActual = new ByteArrayOutputStream();
    RDFDataMgr.write(baosActual, model, Lang.RDFXML);
    final ByteArrayOutputStream baosExpected = new ByteArrayOutputStream();
    RDFDataMgr.write(baosExpected, expectedDuplicateLiteralResult, Lang.RDFXML);
    assertEquals(baosExpected.toString(), baosActual.toString());
  }

}
