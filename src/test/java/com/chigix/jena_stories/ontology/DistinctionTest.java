package com.chigix.jena_stories.ontology;

import static org.apache.jena.rdf.model.ModelFactory.createOntologyModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.in;

import java.util.Iterator;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.junit.Test;

public class DistinctionTest {

  /**
   * https://jena.apache.org/documentation/ontology/#the-generic-ontology-type-ontresource
   */
  @Test
  public void testDistinction() {
    final String SOURCE = "http://wwww.eswc2006.org/technologies/ontology";
    final String NS = SOURCE + "#";

    // Use a recipe object to create an ontology model with none reasoner
    // https://jena.apache.org/documentation/ontology/#creating-ontology-models
    OntModel base = createOntologyModel(OntModelSpec.OWL_MEM);

    // Load an ontology Document
    // Document refers to an ontology serialized in some transport syntax
    // https://jena.apache.org/documentation/ontology/#compound-ontology-documents-and-imports-processing
    base.read(RDFDataMgr.open("african-wildlife.owl"), NS, "RDF/XML");

    // create a dummy animal for this example
    OntClass carnivore = base.getOntClass(NS + "carnivore");
    Individual carnivore1 = base.createIndividual(NS + "carnivore1", carnivore);

    // list the asserted types
    Iterator<Resource> iterCarnivoreAssertedTypes = carnivore1.listRDFTypes(false);
    assertEquals(NS + "carnivore", iterCarnivoreAssertedTypes.next().getURI());

    // Use a recipe object to create an ontology model with `optimised rule-based
    // reasoner`
    // copy the existing base model.
    // The Reasoner attached to an ontology model is specified through the
    // `OntModelSpec`.
    // https://jena.apache.org/documentation/ontology/#ontology-inference-overview
    // Other inference models are available as well:
    // https://jena.apache.org/documentation/inference/index.html#generalExamples
    OntModel inf = createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);

    // list the inferred types
    Individual carnivore1Infered = inf.getIndividual(NS + "carnivore1");
    Iterator<Resource> iterCarnivoreInferredTypes = carnivore1Infered.listRDFTypes(false);
    // <owl:Class rdf:ID="carnivore">
    assertEquals(NS + "carnivore", iterCarnivoreInferredTypes.next().getURI());

    Resource intersection = iterCarnivoreInferredTypes.next();
    assertNull(intersection.getURI());

    StmtIterator intersectionStmts = intersection
        .listProperties(inf.getProperty(OWL.someValuesFrom.getURI()));
    // <owl:Class rdf:about="#animal" />
    assertEquals(NS + "animal", intersectionStmts.nextStatement().getObject().toString());
    // <owl:Restriction>
    intersectionStmts = intersection.listProperties(inf.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
    assertThat(intersectionStmts.nextStatement().getObject().toString(), in(new String[] {
        "http://www.w3.org/2002/07/owl#Restriction",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2002/07/owl#Class",
        "http://www.w3.org/2000/01/rdf-schema#Class"
    }));
    assertThat(intersectionStmts.nextStatement().getObject().toString(), in(new String[] {
        "http://www.w3.org/2002/07/owl#Restriction",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2002/07/owl#Class",
        "http://www.w3.org/2000/01/rdf-schema#Class"
    }));
    assertThat(intersectionStmts.nextStatement().getObject().toString(), in(new String[] {
        "http://www.w3.org/2002/07/owl#Restriction",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2002/07/owl#Class",
        "http://www.w3.org/2000/01/rdf-schema#Class"
    }));
    assertThat(intersectionStmts.nextStatement().getObject().toString(), in(new String[] {
        "http://www.w3.org/2002/07/owl#Restriction",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2000/01/rdf-schema#Resource",
        "http://www.w3.org/2002/07/owl#Class",
        "http://www.w3.org/2000/01/rdf-schema#Class"
    }));
    assertEquals(NS + "animal", iterCarnivoreInferredTypes.next().getURI());
    assertThat(iterCarnivoreInferredTypes.next().getURI(), in(new String[] {
        "http://www.w3.org/2002/07/owl#Thing",
        "http://www.w3.org/2000/01/rdf-schema#Resource"
    }));
    assertThat(iterCarnivoreInferredTypes.next().getURI(), in(new String[] {
        "http://www.w3.org/2002/07/owl#Thing",
        "http://www.w3.org/2000/01/rdf-schema#Resource"
    }));
  }
}
