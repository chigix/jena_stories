package com.chigix.jena_stories.ontology;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.in;

import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.Test;

public class OntToolsTest {

  private final static String NS = "http://ontology.chigix.com/some#";

  @Test
  public void testNamedHierarchyRoots() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    model.read(RDFDataMgr.open("african-wildlife.owl"), NS, "RDF/XML");

    // identifies the classes that are uppermost in the class hierarchy
    // https://jena.apache.org/documentation/ontology/#listing-classes
    ExtendedIterator<OntClass> classes = model.listHierarchyRootClasses();
    Integer count = 0;
    while (classes.hasNext()) {
      if (classes.next().isAnon()) {
        // anonymous classes are contained as well
        count++;
      }
    }
    assertThat(count, is(10));
    // It is important to close the iterators
    classes.close();

    // The named classes lie closest to the top of the hierarchy
    // https://jena.apache.org/documentation/ontology/#listing-classes
    List<OntClass> roots = OntTools.namedHierarchyRoots(model);
    for (OntClass root : roots) {
      assertThat(root.getURI(), is(in(new String[] { NS + "animal",
          NS + "plant", NS + "carnivore", NS + "herbivore", NS + "leaf", NS + "branch" })));
    }
  }

}
