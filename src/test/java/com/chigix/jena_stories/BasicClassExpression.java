package com.chigix.jena_stories;

import static org.apache.jena.rdf.model.ModelFactory.createOntologyModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

/**
 * https://jena.apache.org/documentation/ontology/#ontology-classes-and-basic-class-expressions
 */
public class BasicClassExpression {

  private final static String SOURCE = "http://wwww.eswc2006.org/technologies/ontology";

  private final static String NS = SOURCE + "#";

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
}
