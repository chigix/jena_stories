package com.chigix.jena_stories;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.stream.StreamManager;
import org.junit.Before;
import org.junit.Test;

public class GraphMergeTest {

  private String expectedOutput;

  @Before
  public void beforeEach() {
    try {
      this.expectedOutput = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-merged-graph.rdf"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testMergePerson() throws IOException {
    // Use Jena Reader
    Model modelPart1 = ModelFactory.createDefaultModel();
    Model modelPart2 = ModelFactory.createDefaultModel();
    modelPart1.read(RDFDataMgr.open("vc-db-3.rdf"), "");
    modelPart2.read(RDFDataMgr.open("vc-db-4.rdf"), "");
    StreamManager.get().open("snapshot-merged-graph.rdf");

    // Merge the graphs
    Model mergedModel = modelPart1.union(modelPart2);

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
      RDFDataMgr.write(ps, mergedModel, Lang.RDFXML);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    assertEquals("Assert Merged Model", expectedOutput,
        baos.toString(StandardCharsets.UTF_8));
  }
}
