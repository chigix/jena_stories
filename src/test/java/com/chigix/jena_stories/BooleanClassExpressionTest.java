package com.chigix.jena_stories;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.ComplementClass;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.IntersectionClass;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

public class BooleanClassExpressionTest {

  private static final String NS = "http://chigix.com/else#";
  private String expectedListConstruction;
  private String expectedCollectionBuilding;
  private String expectedIntersectionBuilding;
  private String expectedComplementOf;

  @Before
  public void setUp() {
    try {
      expectedListConstruction = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-list-construction.owl"),
          StandardCharsets.UTF_8);
      expectedCollectionBuilding = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-collection-building.owl"),
          StandardCharsets.UTF_8);
      expectedIntersectionBuilding = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-intersection-building.owl"),
          StandardCharsets.UTF_8);
      expectedComplementOf = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-complement-of.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * https://jena.apache.org/documentation/ontology/#list-expressions
   */
  @Test
  public void testListConstruction() {
    OntModel m = ModelFactory.createOntologyModel();
    OntClass c0 = m.createClass(NS + "c0");
    OntClass c1 = m.createClass(NS + "c1");
    OntClass c2 = m.createClass(NS + "c2");
    RDFList classes = m.createList(new RDFNode[] { c0, c1, c2 });
    assertTrue(classes.isResource());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertThat(baos.toString(), Matchers.equalTo(expectedListConstruction));

    assertThat(classes.size(), Matchers.is(3));

    Iterator<RDFNode> i = classes.iterator();
    assertThat(i.next().asResource().getURI(), Matchers.equalTo(NS + "c0"));
    assertThat(i.next().asResource().getURI(), Matchers.equalTo(NS + "c1"));
    assertThat(i.next().asResource().getURI(), Matchers.equalTo(NS + "c2"));
  }

  @Test
  public void testCollectionBuilding() {
    OntModel m = ModelFactory.createOntologyModel();
    OntClass c0 = m.createClass(NS + "c0");
    OntClass c1 = m.createClass(NS + "c1");
    OntClass c2 = m.createClass(NS + "c2");
    RDFList classes = m.createList(); // An empty list
    // Build a list one element at time
    // `cons` adds cell to the front of the list
    classes.cons(c0);
    classes.cons(c1);
    classes.cons(c2);
    assertTrue(classes.isResource());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    Diff d = DiffBuilder.compare(Input.fromString(baos.toString()))
        .withTest(Input.fromString(expectedCollectionBuilding))
        .withNodeFilter(x -> !x.getNodeName().equals("rdf:Description")).build();
    assertFalse(d.hasDifferences());
  }

  /**
   * https://jena.apache.org/documentation/ontology/#intersection-union-and-complement-class-expressions
   */
  @Test
  public void testIntersectionBuilding() {
    OntModel m = ModelFactory.createOntologyModel();

    // Create the class references
    OntClass place = m.createClass(NS + "place");
    OntClass indTrack = m.createClass(NS + "IndustryTrack");

    // Get the property reference
    ObjectProperty hasPart = m.createObjectProperty(NS + "hasPart");
    ObjectProperty hasLoc = m.createObjectProperty(NS + "hasLocation");

    // Create the UK instance
    Individual uk = place.createIndividual(NS + "united_kingdom");

    // Now the anonymous restrictions
    HasValueRestriction ukLocation = m.createHasValueRestriction(null, hasLoc, uk);
    SomeValuesFromRestriction hasIndTrack = m.createSomeValuesFromRestriction(null, hasPart, indTrack);

    // Finally create the intersection class
    IntersectionClass ukIndustrialConf = m.createIntersectionClass(NS + "UKIndustrialConference",
        m.createList(new RDFNode[] { ukLocation, hasIndTrack }));
    assertThat(ukIndustrialConf, Matchers.instanceOf(BooleanClassDescription.class));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertThat(baos.toString(), Matchers.equalTo(expectedIntersectionBuilding));
  }

  /**
   * Example in ch.4.2.7 in _A Semantic Web Primer_
   */
  @Test
  public void testComplementOf() {
    OntModel m = ModelFactory.createOntologyModel();

    // Create the class references
    OntClass course = m.createClass(NS + "course");

    // Finally create the complement class
    ComplementClass staffMember = m.createComplementClass(NS + "staffMember", course);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertThat(baos.toString(), Matchers.equalTo(expectedComplementOf));
  }

}
