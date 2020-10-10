package com.chigix.jena_stories;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.VCARD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {

  static String personURI = "http://somewhere/JohnSmith";
  static String fullName = "John Smith";
  private static final Logger LOG = LoggerFactory.getLogger(App.class.getName());

  /**
   * A getting started graph initialized to confirm this maven project works for
   * Jena.
   *
   * https://github.com/apache/jena/blob/master/jena-core/src-examples/jena/examples/rdf/Tutorial04.java
   * https://jena.apache.org/tutorials/rdf_api.html#ch-Introduction
   *
   * @param args
   */
  public static void main(String[] args) {
    final Model model = ModelFactory.createDefaultModel();
    final Resource johnSmith = model.createResource(personURI)
        // A property from VCARD schema
        .addProperty(VCARD.FN, fullName);
    final String givenName = "John";
    final String familyName = "Smith";
    johnSmith
        .addProperty(
            VCARD.N,
            model.createResource()
                .addProperty(VCARD.Given, givenName).addProperty(VCARD.Family, familyName))
        .addProperty(VCARD.NICKNAME, "Smithy")
        .addProperty(VCARD.NICKNAME, "Adman");
    LOG.info("The newly generated resource: {}", johnSmith);
  }

}
