package com.chigix.jena_stories;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class Utils {

  private Utils() {
  }

  /**
   * https://jena.apache.org/tutorials/rdf_api.html#ch-Writing-RDF
   *
   * @param m
   * @return
   */
  public static String modelToRDFXMLMessage(final Model m) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
      RDFDataMgr.write(ps, m, Lang.RDFXML);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return baos.toString(StandardCharsets.UTF_8);
  }
}
