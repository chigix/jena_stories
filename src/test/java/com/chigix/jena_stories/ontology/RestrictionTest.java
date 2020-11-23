package com.chigix.jena_stories.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.equalTo;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

/**
 * https://jena.apache.org/documentation/ontology/#restriction-class-expressions
 */
public class RestrictionTest {

  private static final String NS = "http://chigix.com/else#";
  private String expectedTwoRestrictionCreateResult;
  private String expectedHasValueExpression;
  private String expectedHasValueDebug;

  @Before
  public void setUp() {
    try {
      expectedTwoRestrictionCreateResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-two-restriction.owl"),
          StandardCharsets.UTF_8);
      expectedHasValueExpression = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-hasvalue-expression.owl"),
          StandardCharsets.UTF_8);
      expectedHasValueDebug = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-hasvalue-debug.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testTwoRestriction() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    // anonymous restriction on property p
    OntProperty simpleProperty = m.createOntProperty(NS + "simpleProperty");
    Restriction anonR = m.createRestriction(simpleProperty);
    // A restriction is typically not assigned a URI in an ontology
    assertNull(anonR.getURI());
    assertTrue(anonR.isRestriction());
    assertFalse(anonR.isAllValuesFromRestriction());
    assertThat(anonR, is(not(instanceOf(AllValuesFromRestriction.class))));

    OntClass newClass = m.createClass(NS + "NewClass");
    ObjectProperty restrictedProperty = m.createObjectProperty(NS + "restrictedProperty");

    // null denotes the URI in an anonymous restriction
    m.createAllValuesFromRestriction(null, restrictedProperty, newClass);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertThat(baos.toString(StandardCharsets.UTF_8),
        isSimilarTo(expectedTwoRestrictionCreateResult)
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)));
  }

  @Test
  public void testRestrictionRetrieve() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    // get restriction with a given URI
    Restriction r = m.getRestriction(NS + "price");
    assertNull(r);

    OntClass newClass = m.createClass(NS + "NewClass");
    ObjectProperty restrictedProperty = m.createObjectProperty(NS + "restrictedProperty");

    AllValuesFromRestriction avfR = m.createAllValuesFromRestriction(NS + "newRestriction", restrictedProperty,
        newClass);
    Restriction newRest = m.getRestriction(NS + "newRestriction");
    assertNotSame(avfR, newRest);
    assertEquals(avfR, newRest);
    assertFalse(newRest instanceof AllValuesFromRestriction);
    assertTrue(newRest.isAllValuesFromRestriction());
  }

  @Test
  public void testRestrictionList() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    m.read(getClass().getClassLoader().getResourceAsStream("african-wildlife.owl"), NS);

    ObjectProperty eatenBy = m.getObjectProperty(NS + "eats");

    Iterator<Restriction> i = eatenBy.listReferringRestrictions();

    while (i.hasNext()) {
      Restriction r = i.next();
      if (r.isSomeValuesFromRestriction()) {
        String uri = r.asSomeValuesFromRestriction().getSomeValuesFrom().getURI();
        assertEquals(NS + "eats", r.getOnProperty().getURI());
        assertEquals(NS + "animal", uri);
        continue;
      }
      if (r.isAllValuesFromRestriction()) {
        String uri = r.asAllValuesFromRestriction().getAllValuesFrom().getURI();
        assertEquals(NS + "eats", r.getOnProperty().getURI());
        assertThat(uri, in(new String[] { NS + "leaf", NS + "herbivore", null }));
        continue;
      }
      fail();
    }
  }

  /**
   * Example from A Semantic Primer
   */
  @Test
  @Ignore("Similarity calculation might be more suitalble instead of XML compare here")
  public void testHasValueRestriction() {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
    OntClass MathCourse = m.createClass(NS + "mathCourse");
    ObjectProperty isTaughtBy = m.createObjectProperty(NS + "isTaughtBy");
    Resource davidBillington = m.createResource(NS + "949352");
    Restriction r = m.createHasValueRestriction(null, isTaughtBy, davidBillington);
    // Restriction is a Class Expression:
    // a local anonymous class that satisfy certain restriction conditions
    assertThat(r, isA(OntClass.class));
    MathCourse.setSuperClass(r);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    m.write(baos);
    assertThat(baos.toString(), equalTo(expectedHasValueExpression));

    // https://jena.apache.org/documentation/ontology/#additional-notes
    // snapshot the contents of ont model for debug
    Model snapshot = ModelFactory.createDefaultModel();
    snapshot.add(m);

    ByteArrayOutputStream snapshotResult = new ByteArrayOutputStream();
    snapshot.write(snapshotResult);
    assertThat(snapshotResult.toString(),
        isSimilarTo(expectedHasValueDebug)
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName)));
  }

}
