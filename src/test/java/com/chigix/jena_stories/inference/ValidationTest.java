package com.chigix.jena_stories.inference;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Iterator;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

public class ValidationTest {

  /**
   * If an object value for a property lies outside the given value space (owl:Range),
   * that RDF statement raises an logical inconsistency.
   *
   * https://jena.apache.org/documentation/inference/index.html#validation
   */
  @Test
  public void validatePrinterOntology() {
    Model data = RDFDataMgr.loadModel("printer-ontology.owl");
    InfModel infmodel = ModelFactory.createRDFSModel(data);
    // The validation result comprises
    //   * a simple pass/fail flag
    //   * a list of specific reports detailing detected inconsistencies
    ValidityReport validity = infmodel.validate();
    assertThat(validity.isValid(), is(true));
  }

  /**
   * The underlying ontology is continued from:
   * https://github.com/chigix/jena_stories/blob/master/src/test/java/com/chigix/jena_stories/ontology/PropertiesManipulationTest.java
   */
  @Test
  public void validateAnnotationProperty() {
    InfModel infmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF,
        RDFDataMgr.loadModel("snapshot-annotation-usage.owl"));
    ValidityReport validity = infmodel.validate();
    assertThat(validity.isValid(), is(false));
    // xml:lang is only available for xsd:langString
    Iterator<Report> iterReports = validity.getReports();
    assertThat(iterReports.next().getType(), equalTo("\"range check\""));
    assertThat(iterReports.next().getType(), equalTo("dtRange"));
  }
}
