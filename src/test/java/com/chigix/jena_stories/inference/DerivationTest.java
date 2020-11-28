package com.chigix.jena_stories.inference;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.junit.Before;
import org.junit.Test;

/**
 * https://jena.apache.org/documentation/inference/index.html#derivations
 */
public class DerivationTest {

  private static String NS = "urn:x-hp:eg/";

  @Before
  public void setUp() {
  }

  public void getEntailmentResult() {
    Model trivial = ModelFactory.createDefaultModel();
    Property p = trivial.createProperty(NS + "p");
    Resource rcA = trivial.createResource(NS + "A");
    Resource rcB = trivial.createResource(NS + "B");
    Resource rcC = trivial.createResource(NS + "C");
    Resource rcD = trivial.createResource(NS + "D");
    rcA.addProperty(p, rcB);
    rcB.addProperty(p, rcC);
    rcC.addProperty(p, rcD);

    String rules = "[rule1: (?a urn:x-hp:eg/p ?b) (?b urn:x-hp:eg/p ?c) -> (?a urn:x-hp:eg/p ?c)]";
    Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
    reasoner.setDerivationLogging(true);
    InfModel inf = ModelFactory.createInfModel(reasoner, trivial);

    // Entailment to A -> D is unavailable until the reasoner is injected
    assertThat(trivial.getProperty(rcA, p).getObject(), equalTo(rcB));
    assertThat(inf.getProperty(rcA, p).getObject(), equalTo(rcD));
  }

  @Test
  public void traceFacts() {
    Model trivial = ModelFactory.createDefaultModel();
    Property p = trivial.createProperty(NS + "p");
    Resource rcA = trivial.createResource(NS + "A");
    Resource rcB = trivial.createResource(NS + "B");
    Resource rcC = trivial.createResource(NS + "C");
    Resource rcD = trivial.createResource(NS + "D");
    rcA.addProperty(p, rcB);
    rcB.addProperty(p, rcC);
    rcC.addProperty(p, rcD);

    String rules = "[rule1: (?a urn:x-hp:eg/p ?b) (?b urn:x-hp:eg/p ?c) -> (?a urn:x-hp:eg/p ?c)]";
    Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
    reasoner.setDerivationLogging(true);
    InfModel inf = ModelFactory.createInfModel(reasoner, trivial);

    StmtIterator i = inf.listStatements(rcA, p, rcD);
    Statement s = i.nextStatement();
    assertThat(new RDFNode[] {
        s.getSubject(), s.getPredicate(), s.getObject(),
    }, equalTo(
        new Resource[] { rcA, p, rcD }));
    Iterator<Derivation> id = inf.getDerivation(s);
    Derivation deriv = id.next();
    assertThat(deriv.toString(), equalTo("Rule rule1"));
    StringWriter sw = new StringWriter();
    deriv.printTrace(new PrintWriter(sw), true);
    sw.flush();
    assertThat(sw.toString(), equalTo("Rule rule1 concluded (eg:A eg:p eg:D) <-\n"
        + "    Rule rule1 concluded (eg:A eg:p eg:C) <-\n"
        + "        Fact (eg:A eg:p eg:B)\n"
        + "        Fact (eg:B eg:p eg:C)\n"
        + "    Fact (eg:C eg:p eg:D)\n"));

    assertThat(id.hasNext(), is(false));
    assertThat(i.hasNext(), is(false));
  }

  /**
   * https://jena.apache.org/documentation/inference/index.html#processingControl
   */
  @Test
  public void processingControl() {
    Model trivial = ModelFactory.createDefaultModel();
    Property p = trivial.createProperty(NS + "p");
    Resource rcA = trivial.createResource(NS + "A");
    Resource rcB = trivial.createResource(NS + "B");
    Resource rcC = trivial.createResource(NS + "C");
    Resource rcD = trivial.createResource(NS + "D");
    rcA.addProperty(p, rcB);
    rcB.addProperty(p, rcC);
    rcC.addProperty(p, rcD);

    String rules = "[rule1: (?a urn:x-hp:eg/p ?b) (?b urn:x-hp:eg/p ?c) -> (?a urn:x-hp:eg/p ?c)]";
    Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
    reasoner.setDerivationLogging(true);
    InfModel inf1 = ModelFactory.createInfModel(reasoner, trivial);

    assertThat(trivial.getProperty(rcA, p).getObject(), equalTo(rcB));
    assertThat(inf1.getProperty(rcA, p).getObject(), equalTo(rcD));

    inf1.remove(rcA, p, rcB);

    // Base model is still changed by the normal `add` and `remove`
    // calls to the `InfModel`.
    assertThat(trivial.getProperty(rcA, p), nullValue());
    assertThat(inf1.getProperty(rcA, p), nullValue());

    // Current deduction (A -> B) and temporary rules are discarded
    inf1.add(rcA, p, rcC);
    // Inference start again from scratch at the following query.
    assertThat(trivial.getProperty(rcA, p).getObject(), equalTo(rcC));
    assertThat(inf1.getProperty(rcA, p).getObject(), equalTo(rcD));
  }

}
