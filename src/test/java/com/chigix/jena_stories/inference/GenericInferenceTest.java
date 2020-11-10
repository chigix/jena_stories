package com.chigix.jena_stories.inference;

import static org.junit.Assert.assertThat;

import java.util.Iterator;

import static org.hamcrest.Matchers.equalTo;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.junit.Before;
import org.junit.Test;

public class GenericInferenceTest {

  private static final String NS = "urn:x-hp-jena:eg/";

  private Model trivialSetup;
  private Property p;
  private Property q;

  @Before
  public void setUp() {
    // Build a trivial example data set
    trivialSetup = ModelFactory.createDefaultModel();
    p = trivialSetup.createProperty(NS, "p");
    q = trivialSetup.createProperty(NS, "q");
    trivialSetup.add(p, RDFS.subPropertyOf, q);
    trivialSetup.createResource(NS + "a").addProperty(p, "foo");
  }

  /**
   * https://jena.apache.org/documentation/inference/index.html#generalExamples
   */
  @Test
  public void smallSetup() {
    // Create an underlying inference model
    InfModel inf = ModelFactory.createRDFSModel(trivialSetup);
    Resource a = inf.getResource(NS + "a");

    // p(X, Y) -> q(X, Y)
    // p(a, foo)
    // => q(a, foo)
    assertThat(a.getProperty(q).getString(), equalTo("foo"));
  }

  /**
   * https://jena.apache.org/documentation/inference/index.html#generalExamples
   */
  @Test
  public void registrySetup() {
    // Create the inference model manually from reasoner registry
    Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
    InfModel inf = ModelFactory.createInfModel(reasoner, trivialSetup);
    Resource a = inf.getResource(NS + "a");

    // p(X, Y) -> q(X, Y)
    // p(a, foo)
    // => q(a, foo)
    assertThat(a.getProperty(q).getString(), equalTo("foo"));
  }

  /**
   * https://jena.apache.org/documentation/inference/index.html#generalExamples
   */
  @Test
  public void reasonerFactorySetup() {
    // Create the inference model manually from reasoner factory
    Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(null);
    InfModel inf = ModelFactory.createInfModel(reasoner, trivialSetup);
    Resource a = inf.getResource(NS + "a");

    // p(X, Y) -> q(X, Y)
    // p(a, foo)
    // => q(a, foo)
    assertThat(a.getProperty(q).getString(), equalTo("foo"));
  }

  /**
   * https://jena.apache.org/documentation/inference/index.html#generalExamples
   */
  @Test
  public void suppressBasicAxioms() {
    Resource config = ModelFactory.createDefaultModel().createResource()
        .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_SIMPLE);
    Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(config);
    InfModel inf = ModelFactory.createInfModel(reasoner, trivialSetup);
    Iterator<Statement> iterStat = inf.listStatements();
    assertThat(iterStat.next().toString(), equalTo("[urn:x-hp-jena:eg/a, urn:x-hp-jena:eg/p, \"foo\"]"));
    assertThat(iterStat.next().toString(),
        equalTo("[urn:x-hp-jena:eg/p, http://www.w3.org/2000/01/rdf-schema#subPropertyOf, urn:x-hp-jena:eg/q]"));
    assertThat(iterStat.next().toString(),
        equalTo("[urn:x-hp-jena:eg/p, http://www.w3.org/2000/01/rdf-schema#subPropertyOf, urn:x-hp-jena:eg/p]"));
    assertThat(iterStat.next().toString(),
        equalTo("[urn:x-hp-jena:eg/q, http://www.w3.org/2000/01/rdf-schema#subPropertyOf, urn:x-hp-jena:eg/q]"));
    assertThat(iterStat.next().toString(),
        equalTo("[urn:x-hp-jena:eg/a, urn:x-hp-jena:eg/q, \"foo\"]"));
  }

}
