package com.chigix.jena_stories;

import static org.apache.jena.rdf.model.ModelFactory.createOntologyModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.instanceOf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * https://jena.apache.org/documentation/ontology/#ontology-classes-and-basic-class-expressions
 */
public class BasicClassExpressionTest {

  private final static String SOURCE = "http://wwww.eswc2006.org/technologies/ontology";

  private final static String NS = SOURCE + "#";

  private String expectedEnumeration;

  @Before
  public void setUp() {
    try {
      expectedEnumeration = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-enumeration.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testBasicClassExpression() {
    // Use a recipe object to create an ontology model with none reasoner
    // https://jena.apache.org/documentation/ontology/#creating-ontology-models
    OntModel model = createOntologyModel(OntModelSpec.OWL_MEM);

    model.read(RDFDataMgr.open("african-wildlife.owl"), NS, "RDF/XML");

    Resource r = model.getResource(NS + "herbivore");
    assertFalse(r instanceof OntClass);

    // Convert a plaint RDF resource into its class facet
    // <owl:Class rdf:ID="herbivore">
    // https://jena.apache.org/documentation/ontology/#rdf-level-polymorphism-and-java
    OntClass herbivore = r.as(OntClass.class);
    assertTrue(herbivore instanceof OntClass);

    // A shorten style of calling
    OntClass plant = model.getOntClass(NS + "plant");
    for (Iterator<OntClass> iterSubClasses = plant.listSubClasses(); iterSubClasses.hasNext();) {
      OntClass children = iterSubClasses.next();
      assertTrue(ArrayUtils.contains(new String[] {
          "http://wwww.eswc2006.org/technologies/ontology#tasty_plant",
          "http://wwww.eswc2006.org/technologies/ontology#tree",
      }, children.getURI()));
    }
  }

  @Test
  public void createNewClassFromExistingResource() {
    OntModel model = createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("african-wildlife.owl"), NS, "RDF/XML");
    OntClass suspectHerbivore = model.createClass(NS + "super_herbivore");
    assertEquals(NS + "super_herbivore", suspectHerbivore.getURI());
  }

  @Test
  public void createAnonymousClass() {
    OntModel model = createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("african-wildlife.owl"), NS, "RDF/XML");
    OntClass suspectHerbivore = model.createClass();
    assertNull(suspectHerbivore.getURI());
  }

  @Test
  public void testRootClass() {
    OntModel model = createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("african-wildlife.owl"), NS, "RDF/XML");
    assertTrue(model.createClass(NS + "animal").isHierarchyRoot());
    assertFalse(model.createClass(NS + "tasty_plant").isHierarchyRoot());
  }

  /**
   * https://jena.apache.org/documentation/ontology/#enumerated-classes
   *
   * Create the class whose members are the given individuals.
   *
   * The example in Ch.4.2.8 from _A Semantic Web Primer_ is used here.
   */
  @Test
  public void testEnumeration() {
    OntModel model = createOntologyModel(OntModelSpec.OWL_MEM);
    OntClass thing = model.createClass("http://www.w3.org/2002/07/owl#Thing");
    EnumeratedClass weekdays = model.createEnumeratedClass(NS + "weekdays", null);
    weekdays.addOneOf(thing.createIndividual(NS + "Monday"));
    weekdays.addOneOf(thing.createIndividual(NS + "Tuesday"));
    weekdays.addOneOf(thing.createIndividual(NS + "Wednesday"));
    weekdays.addOneOf(thing.createIndividual(NS + "Thursday"));
    weekdays.addOneOf(thing.createIndividual(NS + "Friday"));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    model.write(baos);
    assertThat(baos.toString(), Matchers.equalTo(expectedEnumeration));

    Iterator<?> i = weekdays.listOneOf();
    assertThat(((OntResource) i.next()).getURI(), Matchers.equalTo(NS + "Monday"));
    assertThat(i.next(), not(instanceOf(Individual.class)));
    assertThat(((OntResource) i.next()).getURI(), Matchers.equalTo(NS + "Wednesday"));
    assertThat(((OntResource) i.next()).getURI(), Matchers.equalTo(NS + "Thursday"));
    assertThat(((OntResource) i.next()).getURI(), Matchers.equalTo(NS + "Friday"));
  }
}
