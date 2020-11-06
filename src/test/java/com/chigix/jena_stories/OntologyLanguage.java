package com.chigix.jena_stories;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Before;
import org.junit.Test;

/**
 * https://jena.apache.org/documentation/ontology/#creating-ontology-models
 *
 * This case is intended to confirm difference on format from Jena Model
 * Serializer (XMl Generator).
 *
 * Because Language selection is covered by OntModelSpec as well, it is
 * primarily the choice of reasoner, rather than the choice of language profile.
 */
public class OntologyLanguage {

  private static final String SOURCE = "http://chigix.com/ontology";

  private String expectedAnimalOntologyInRDFS;
  private String expectedAnimalOntologyInOwl;

  @Before
  public void setUp() {
    try {
      this.expectedAnimalOntologyInRDFS = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-ontology-animal-rdfs.rdf"),
          StandardCharsets.UTF_8);
      this.expectedAnimalOntologyInOwl = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-ontology-animal-owl.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void createOntologyDefaultLang() throws Exception {
    OntModel ontology = ModelFactory.createOntologyModel();
    OntClass animal = ontology.createClass(SOURCE + "#animal");
    ontology.createIndividual(SOURCE + "#uma", animal);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ontology.write(baos, Lang.RDFXML.getName());
    // Therefore Jena create Ontology in OWL defaultly
    assertEquals(expectedAnimalOntologyInOwl, baos.toString(StandardCharsets.UTF_8));
  }

  @Test
  public void createOntologyInRdfs() throws Exception {
    OntModel ontology = ModelFactory.createOntologyModel(ProfileRegistry.RDFS_LANG);
    // https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/ontology/ProfileRegistry.html
    assertEquals("http://www.w3.org/2000/01/rdf-schema#", ProfileRegistry.RDFS_LANG);
    OntClass animal = ontology.createClass(SOURCE + "#animal");
    ontology.createIndividual(SOURCE + "#uma", animal);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ontology.write(baos, Lang.RDFXML.getName());
    assertEquals(expectedAnimalOntologyInRDFS, baos.toString(StandardCharsets.UTF_8));
  }

  @Test
  public void createOntologyInOwlDl() throws Exception {
    OntModel ontology = ModelFactory.createOntologyModel(ProfileRegistry.OWL_DL_LANG);
    assertEquals("http://www.w3.org/TR/owl-features/#term_OWLDL", ProfileRegistry.OWL_DL_LANG);
    OntClass animal = ontology.createClass(SOURCE + "#animal");
    ontology.createIndividual(SOURCE + "#uma", animal);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ontology.write(baos, Lang.RDFXML.getName());
    assertEquals(expectedAnimalOntologyInOwl, baos.toString(StandardCharsets.UTF_8));
  }

  /**
   * https://jena.apache.org/documentation/ontology/#creating-ontology-models
   *
   * It is primarily the choice of reasoner, rather than the choice of language
   * profile.
   *
   * @throws Exception
   */
  @Test
  public void createOntologyInOwlDlSpec() throws Exception {
    OntModel ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    assertEquals("http://www.w3.org/TR/owl-features/#term_OWLDL", ProfileRegistry.OWL_DL_LANG);
    OntClass animal = ontology.createClass(SOURCE + "#animal");
    ontology.createIndividual(SOURCE + "#uma", animal);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ontology.write(baos, Lang.RDFXML.getName());
    assertEquals(expectedAnimalOntologyInOwl, baos.toString(StandardCharsets.UTF_8));
  }

}
