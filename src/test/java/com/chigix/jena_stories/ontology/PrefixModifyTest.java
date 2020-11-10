package com.chigix.jena_stories.ontology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

public class PrefixModifyTest {

  private Model targetModel;
  private String expectedPrefixSettingResult;

  @Before
  public void setUp() {
    this.targetModel = ModelFactory.createDefaultModel();
    String nsChigix = "http://chigix.com/else#";
    String nsKanban = "http://kanban.run/else#";
    Resource root = targetModel.createResource(nsChigix + "root");
    Property propP = targetModel.createProperty(nsChigix, "P");
    Property propQ = targetModel.createProperty(nsKanban, "Q");
    Resource x = targetModel.createResource(nsChigix + "x");
    Resource y = targetModel.createResource(nsChigix + "y");
    Resource z = targetModel.createResource(nsChigix + "z");
    targetModel.add(root, propP, x).add(root, propP, y);
    targetModel.add(y, propQ, z);

    try {
      this.expectedPrefixSettingResult = IOUtils.toString(
          getClass().getClassLoader().getResourceAsStream("snapshot-prefix-setting.rdf"),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPrefixSet() {
    targetModel.setNsPrefix("chigix", "http://chigix.com/else#");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    RDFDataMgr.write(bos, targetModel, Lang.RDFXML);
    assertEquals(expectedPrefixSettingResult, bos.toString(StandardCharsets.UTF_8));
  }

  @Test
  public void testPrefixRemove() {
    targetModel.setNsPrefix("chigix", "http://chigix.com/else#");
    Map<String, String> expected = new HashMap<>();
    expected.put("chigix", "http://chigix.com/else#");
    assertTrue(Maps.difference(targetModel.getNsPrefixMap(), expected).areEqual());
    targetModel.removeNsPrefix("chigix");
    assertTrue(Maps.difference(targetModel.getNsPrefixMap(),
        Collections.<String, String>emptyMap()).areEqual());
  }
}
