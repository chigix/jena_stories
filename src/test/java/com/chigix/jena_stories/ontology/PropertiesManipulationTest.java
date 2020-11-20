package com.chigix.jena_stories.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.FunctionalProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.XSD;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * https://jena.apache.org/documentation/ontology/#ontology-properties
 *
 * A simple test on the set of convenient Java classes that helps manipulate
 * properties represented in an ontology model.
 */
public class PropertiesManipulationTest {

  private static final String NS = "http://chigix.com/else#";
  private String expectedOntologyPropertyResult;
  private String expectedConvertedFunctionalPropertyResult;
  private String expectedSpecialProperties;

  @Before
  public void setUp() {
    try {
      this.expectedOntologyPropertyResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-ontology-properties.owl"),
          StandardCharsets.UTF_8);
      this.expectedConvertedFunctionalPropertyResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-functional-property-convert.owl"),
          StandardCharsets.UTF_8);
      this.expectedSpecialProperties = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-special-properties.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * `owl:ObjectProperty` is a special case of `rdf:Property`.
   */
  @Test
  public void testOntProperties() {
    // A property in an ontology model is an extension of the core Jena API class
    // Property
    // allows access to the additional information that can be asserted about
    // properties in an ontology language
    // the pattern of add, set, get, list, has, and remove methods
    // Attribute | Meaning subProperty | a sub property of this property
    // Multiple domain values are interpreted as a conjunciton
    // subPropertyOf
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    OntClass programme = m.createClass(NS + "hasProgramme");
    OntClass orgEvent = m.createClass(NS + "OrganizedEvent");

    ObjectProperty hasProgramme = m.createObjectProperty(NS + "hasProgramme");
    // The domain denotes the class of value of property maps from
    hasProgramme.addDomain(orgEvent);

    // OntProperty is the common API super-class
    OntProperty body = m.createOntProperty(NS + "body");
    // range | Denotes the class or classes that form the range of this property
    body.addRange(programme);
    body.addLabel("has programme", "en");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos, Lang.RDFXML.getName());
    assertEquals(this.expectedOntologyPropertyResult, baos.toString());
  }

  @Test
  public void testSuperProperties() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    m.read(getClass().getClassLoader().getResourceAsStream("printer-ontology.owl"), NS);
    // NOTE: xmlns defined in owl file would be overridden here
    assertNull(m.getDatatypeProperty("http://ontology.chigix.com/printer.owl#manufactured_by"));
    DatatypeProperty printingTech = m.getDatatypeProperty("http://chigix.com/else#printingTechnology");
    assertTrue(printingTech instanceof DatatypeProperty);

    DatatypeProperty printingRes = m.getDatatypeProperty("http://chigix.com/else#printingResolution");

    // Add a super-property
    DatatypeProperty spec = m.createDatatypeProperty(NS + "printingSpec");
    spec.addDomain(m.getOntClass(NS + "printer"));
    spec.addRange(XSD.normalizedString);

    // Generalize the separate properties
    spec.addSubProperty(printingTech);
    spec.addSubProperty(printingRes);

    assertEquals(NS + "printingSpec", printingTech.getSuperProperty().getURI());
  }

  @Test
  @Ignore
  public void testFunctionalProperty() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    m.read(getClass().getClassLoader().getResourceAsStream("african-wildlife.owl"), NS);
    ObjectProperty property = m.getObjectProperty(NS + "eats");
    assertFalse(property.isFunctionalProperty());
    FunctionalProperty newFuncProp = property.convertToFunctionalProperty();
    assertTrue(newFuncProp.isFunctionalProperty());

    // @TODO: Use a smaller test case ?
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertEquals(baos.toString(), expectedConvertedFunctionalPropertyResult);
  }

  @Test
  public void testTransitiveProperty() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
    m.read(getClass().getClassLoader().getResourceAsStream("african-wildlife.owl"), NS);
    ObjectProperty isPartOf = m.getObjectProperty(NS + "is_part_of");

    // Special Properties are all ObjectProperties
    assertTrue(isPartOf.isTransitiveProperty());
  }

  /**
   * A simple example revised form A Semantic Web Primer
   */
  @Test
  public void testSpecialProperties() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
    ObjectProperty p = m.createObjectProperty(NS + "hasSameGradeAs");
    p.addRDFType(m.getResource("http://www.w3.org/2002/07/owl#TransitiveProperty"));
    p.addRDFType(m.getResource("http://www.w3.org/2002/07/owl#SymmetricProperty"));
    p.addDomain(m.getResource(NS + "student"));
    p.addRange(m.getResource(NS + "student"));
    m.createInverseFunctionalProperty(NS + "isTheSocialSecurityNumberfor");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertThat(baos.toString(), Matchers.equalTo(expectedSpecialProperties));
  }

}
