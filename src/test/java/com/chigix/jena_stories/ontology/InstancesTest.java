package com.chigix.jena_stories.ontology;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.AllDifferent;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

/**
 * https://jena.apache.org/documentation/ontology/#instances-or-individuals
 */
public class InstancesTest {

  private final static String NS = "http://ontology.chigix.com/some#";
  private String expectedUniversityInstances;

  @Before
  public void setUp() {
    try {
      this.expectedUniversityInstances = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-university-instances.owl"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Individual is an alias for `OntResource`
   * https://jena.apache.org/documentation/ontology/#the-generic-ontology-type-ontresource
   */
  @Test
  public void testIndividuals() {
    OntModel model = ModelFactory.createOntologyModel();

    OntClass c = model.createClass(NS + "SomeClass");

    // first way: use a call on OntModel
    Individual ind0 = model.createIndividual(NS + "ind0", c);

    // Second way: use a call on OntClass
    Individual ind1 = c.createIndividual(NS + "ind1");

    assertThat(ind0.getRDFType().getURI(), equalTo(NS + "SomeClass"));
    assertThat(ind1.getRDFType().getURI(), equalTo(NS + "SomeClass"));
  }

  /**
   * Revised from an example in _A Semantic Web Primer_
   */
  // @Test
  public void testUniversityInstances() {
    OntModel model = ModelFactory.createOntologyModel();
    String uniPrefix = "http://ontology.chigix.com/uni#";
    model.setNsPrefix("uni", uniPrefix);
    OntClass academicStaffMember = model.createClass(NS + "academicStaffMember");
    OntClass course = model.createClass(NS + "course");

    // https://jena.apache.org/documentation/ontology/#functional-properties
    Property isTaughtBy = model.createObjectProperty(NS + "isTaughtBy", true);

    Individual member = academicStaffMember.createIndividual(NS + "id949352");
    Individual memberAlias = academicStaffMember.createIndividual(NS + "id949318");
    Property propAge = model.createProperty(uniPrefix + "age");
    // https://jena.apache.org/documentation/notes/typed-literals.html
    member.addProperty(propAge, model.createTypedLiteral(39));

    Individual course1111 = course.createIndividual(NS + "CIT1111");
    course1111.addProperty(isTaughtBy, member.asResource());
    course1111.addProperty(isTaughtBy, memberAlias);

    AllDifferent allDiff = model.createAllDifferent();
    allDiff.addDistinctMember(member);
    allDiff.addDistinctMember(memberAlias);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    model.write(baos);

    assertThat(baos.toString(),
        isSimilarTo(expectedUniversityInstances)
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)));
  }

}
