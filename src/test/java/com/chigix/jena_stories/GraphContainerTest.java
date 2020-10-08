package com.chigix.jena_stories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.VCARD;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

/**
 * https://jena.apache.org/tutorials/rdf_api.html#ch-Containers
 * https://github.com/apache/jena/blob/master/jena-core/src-examples/jena/examples/rdf/Tutorial10.java
 */
public class GraphContainerTest {

  private String expectedOutput;

  @Before
  public void setUp() {
    try {
      this.expectedOutput = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-bag-demo.rdf"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testBagModel() {
    Model model = ModelFactory.createDefaultModel();
    model.read(RDFDataMgr.open("vc-db-1.rdf"), "");
    // Create a bag
    Bag smiths = model.createBag();

    // select all the resources with a VCARD.FN property
    StmtIterator stmts = model.listStatements(
        new SimpleSelector(null, VCARD.FN, (RDFNode) null) {
          @Override
          public boolean selects(Statement s) {
            return s.getString().endsWith("Smith");
          }
        });
    // Add the Smith's to the bag
    while (stmts.hasNext()) {
      smiths.add(stmts.nextStatement().getSubject());
    }

    // Validate the graph
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFDataMgr.write(baos, model, Lang.RDFXML);
    assertThat(baos.toString(StandardCharsets.UTF_8), isSimilarTo(expectedOutput)
        .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)));

    // Get the members of the bag
    NodeIterator nodes = smiths.iterator();
    // each node is represented by ordinal properties onto this bag
    assertEquals("resource from ordinal property", "Becky Smith",
        ((Resource) nodes.next()).getRequiredProperty(VCARD.FN).getString());
    assertEquals("resource from ordinal property", "John Smith",
        ((Resource) nodes.next()).getRequiredProperty(VCARD.FN).getString());
  }
}
