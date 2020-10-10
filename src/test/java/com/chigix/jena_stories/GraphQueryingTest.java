package com.chigix.jena_stories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.VCARD;
import org.junit.Before;
import org.junit.Test;

public class GraphQueryingTest {

  private Model targetModel;

  @Before
  public void setUp() {
    this.targetModel = ModelFactory.createDefaultModel();
    final Resource johnSmith = targetModel.createResource("http://somewhere/JohnSmith");
    johnSmith.addProperty(VCARD.FN, "John Smith").addProperty(
        VCARD.N,
        targetModel.createResource()
            .addProperty(VCARD.Given, "John")
            .addProperty(VCARD.Family, "Smith")
            .addProperty(VCARD.NICKNAME, "Adaman"));
  }

  @Test
  public void listVCards() {
    ResIterator iterResources = targetModel.listSubjectsWithProperty(VCARD.FN);
    assertEquals("http://somewhere/JohnSmith", iterResources.nextResource().getURI());
  }

  @Test
  public void selectAllStatements() {
    StmtIterator iterStatements = targetModel.listStatements(
        new SimpleSelector((Resource) null, (Property) null, (RDFNode) null));
    Statement vcardName = iterStatements.nextStatement();
    assertEquals("http://somewhere/JohnSmith", vcardName.getSubject().getURI());
    assertEquals(VCARD.N, vcardName.getPredicate());
    assertTrue(vcardName.getObject().isResource());

    Statement vcardFullName = iterStatements.nextStatement();
    assertEquals("http://somewhere/JohnSmith", vcardFullName.getSubject().getURI());
    assertEquals(VCARD.FN, vcardFullName.getPredicate());
    assertTrue(vcardFullName.getObject().isLiteral());

    Statement vcardNickName = iterStatements.nextStatement();
    assertEquals("Adaman", vcardNickName.getString());

    Statement vcardFamily = iterStatements.nextStatement();
    assertEquals("Smith", vcardFamily.getString());

    Statement vcardGiven = iterStatements.nextStatement();
    assertEquals("John", vcardGiven.getString());

    try {
      System.out.println(iterStatements.nextStatement());
    } catch (NoSuchElementException e) {
      return;
    }
    fail("NoSuchElementException should be catched");
  }
}
