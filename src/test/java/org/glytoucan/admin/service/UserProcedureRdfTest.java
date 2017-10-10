package org.glytoucan.admin.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.DeleteSparqlBean;
import org.glycoinfo.rdf.InsertSparql;
import org.glycoinfo.rdf.InsertSparqlBean;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SelectSparqlBean;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlDAO;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.glycan.Contributor;
import org.glycoinfo.rdf.scint.DeleteScint;
import org.glycoinfo.rdf.scint.InsertScint;
import org.glycoinfo.rdf.scint.Scintillate;
import org.glycoinfo.rdf.scint.SelectScint;
import org.glycoinfo.rdf.utils.NumberGenerator;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.glytoucan.admin.service.UserProcedure;
import org.glytoucan.admin.service.UserProcedureConfig;
import org.glytoucan.admin.service.UserProcedureRdf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { UserProcedureRdfTest.class, VirtSesameTransactionConfig.class,
    UserProcedureConfig.class })
@ComponentScan(basePackages = { "org.glycoinfo.rdf.scint" })
@Configuration
@EnableAutoConfiguration
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT, classes = { UserProcedureRdfTest.class, VirtSesameTransactionConfig.class,
//    UserProcedureConfig.class })
public class UserProcedureRdfTest {

  private static final Log logger = LogFactory.getLog(UserProcedureRdfTest.class);

  @Autowired
  SparqlDAO sparqlDAO;

  @Autowired
  @Qualifier(value = "insertscintperson")
  InsertScint insertScintPerson;

  @Autowired
  @Qualifier(value = "insertScintProgramMembership")
  InsertScint insertScintProgramMembership;

  @Autowired
  @Qualifier(value = "selectScintProgramMembership")
  SelectScint selectScintProgramMembership;

  @Autowired
  @Qualifier(value = "deleteScintProgramMembership")
  DeleteScint deleteScintProgramMembership;
  @Autowired
  UserProcedure userProcedure;

  @Autowired
  @Qualifier(value = "selectscintperson")
  SelectScint selectScintPerson;

  @Test
  @Transactional
  public void testDeleteProgramMembership() throws SparqlException {

    SparqlEntity sparqlEntityPerson = new SparqlEntity("TestID123");
    sparqlEntityPerson.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);

    insertScintPerson.update(sparqlEntityPerson);

    sparqlDAO.insert((InsertSparql) insertScintPerson.getSparqlBean());

    // ProgramMembership entity
    SparqlEntity sparqlentityProgramMembership = new SparqlEntity(
        UserProcedureRdf.GLYTOUCAN_PROGRAM + sparqlEntityPerson.getValue(SelectSparql.PRIMARY_KEY));
    sparqlentityProgramMembership.setValue(UserProcedureRdf.PROGRAM_NAME, UserProcedureRdf.GLYTOUCAN_PROGRAM_TITLE);

    sparqlentityProgramMembership.setValue(UserProcedureRdf.MEMBERSHIP_NUMBER, "123");
    sparqlentityProgramMembership.setValue(UserProcedureRdf.MEMBER, insertScintPerson);

    insertScintProgramMembership.update(sparqlentityProgramMembership);

    sparqlDAO.insert((InsertSparql) insertScintProgramMembership.getSparqlBean());

    sparqlentityProgramMembership.setValue(UserProcedureRdf.MEMBERSHIP_NUMBER, null);
    selectScintProgramMembership.update(sparqlentityProgramMembership);
    List<SparqlEntity> list = sparqlDAO.query(selectScintProgramMembership.getSparqlBean());

    SparqlEntity se = list.iterator().next();
    se.setValue(SelectSparql.PRIMARY_KEY,
        UserProcedureRdf.GLYTOUCAN_PROGRAM + sparqlEntityPerson.getValue(SelectSparql.PRIMARY_KEY));
    // delete the previous Program Membership
    deleteScintProgramMembership.update(se);
    sparqlDAO.delete(deleteScintProgramMembership.getSparqlBean());
  }

  @Test(expected = UserException.class)
  public void testInsufficientUser() throws SparqlException, UserException {
    SparqlEntity se = new SparqlEntity();
    se.setValue("id", "person456");
    userProcedure.add(se);
  }

  @Test
  @Transactional
  public void testUser() throws SparqlException, UserException {
    SparqlEntity se = new SparqlEntity();
    // se.setValue(SelectSparql.PRIMARY_KEY, "person789@person.com");
    se.setValue("email", "person789@person.com");
    se.setValue("givenName", "person");
    se.setValue("familyName", "789");
    se.setValue("verifiedEmail", "true");
    se.setValue(UserProcedure.CONTRIBUTOR_ID, "789");
    userProcedure.add(se);

    se.remove(SelectSparql.PRIMARY_KEY);
    se.setValue("member", "");
    se.setValue("contributor", "");
    se.remove("verifiedEmail");
    se.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);

    SelectScint personScint = selectScintPerson;
    personScint.update(se);
    List<SparqlEntity> results = sparqlDAO.query(personScint.getSparqlBean());

    Assert.assertFalse(results.size() == 0);
    for (SparqlEntity sparqlEntity : results) {
      logger.debug(sparqlEntity.toString());
      Assert.assertNotNull(sparqlEntity.getValue("contributor"));
      Assert.assertNotNull(sparqlEntity.getValue("member"));
    }
  }

  @Test
  @Transactional
  public void testUserNotVerified() throws SparqlException, UserException {
    SparqlEntity se = new SparqlEntity();
    se.setValue("email", "person456@person.com");
    se.setValue("givenName", "person");
    se.setValue("familyName", "456");
    se.setValue("verifiedEmail", "false");
    se.setValue(UserProcedure.CONTRIBUTOR_ID, "456");
    userProcedure.add(se);
    se.setValue("member", null);
    se.remove("verifiedEmail");
    se.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);

    selectScintPerson.update(se);
    List<SparqlEntity> results = sparqlDAO.query(selectScintPerson.getSparqlBean());

    Assert.assertFalse(results.size() == 0);
    for (SparqlEntity sparqlEntity : results) {
      logger.debug(sparqlEntity.toString());
      Assert.assertNull(sparqlEntity.getValue("contributor"));
      Assert.assertNotNull(sparqlEntity.getValue("member"));
    }
  }

  // @Test see joinmembership
  // public void testGetUser() throws SparqlException {
  // SparqlEntity results = userProcedure.getUser("aokinobu@gmail.com");
  // Assert.assertNotNull(results);
  // Assert.assertNotNull(results.getValue("familyName"));
  // Assert.assertNotNull(results.getValue("Name"));
  // Assert.assertNotNull(results.getValue("givenName"));
  // Assert.assertNotNull(results.getValue("alternateName"));
  // }

  @Test(expected = UserException.class)
  @Transactional
  public void testJoinBadMembership() throws SparqlException, UserException {
    String results = userProcedure.generateHash("person123@test.com");
    Assert.assertNotNull(results);
  }

  @Test
  @Transactional
  public void testJoinMembership() throws SparqlException, UserException {
    SparqlEntity se = new SparqlEntity();
    // se.setValue(SelectSparql.PRIMARY_KEY, "person789");
    se.setValue(UserProcedure.EMAIL, "person789@person.com");
    se.setValue(UserProcedure.GIVEN_NAME, "testperson789given");
    se.setValue(UserProcedure.FAMILY_NAME, "testperson789family");
    se.setValue(UserProcedure.VERIFIED_EMAIL, "true");
    se.setValue(UserProcedure.CONTRIBUTOR_ID, "789");
    userProcedure.add(se);

    // String results = userProcedure.generateHash("person789");
    String results = userProcedure.generateHash(se.getValue(UserProcedure.EMAIL));
    Assert.assertNotNull(results);

    se = userProcedure.getById(se.getValue(UserProcedure.EMAIL));
    logger.debug(se.getData().toString());
    Assert.assertNotNull(se.getValue(UserProcedure.MEMBER_OF));
    Assert.assertNotNull(se.getValue(UserProcedure.MEMBERSHIP_NUMBER));
  }

  @Test
  @Transactional
  public void testJoinMembershipTwice() throws SparqlException, UserException {
    SparqlEntity se = new SparqlEntity();
    // se.setValue(SelectSparql.PRIMARY_KEY, "person789");
    se.setValue(UserProcedure.EMAIL, "person789@person.com");
    se.setValue(UserProcedure.GIVEN_NAME, "testperson789given");
    se.setValue(UserProcedure.FAMILY_NAME, "testperson789family");
    se.setValue(UserProcedure.VERIFIED_EMAIL, "true");
    se.setValue(UserProcedure.CONTRIBUTOR_ID, "789");
    userProcedure.add(se);

    String results = userProcedure.generateHash(se.getValue(UserProcedure.EMAIL));
    Assert.assertNotNull(results);

    se = userProcedure.getById(se.getValue(UserProcedure.EMAIL));
    logger.debug(se.getData().toString());
    Assert.assertNotNull(se.getValue(UserProcedure.MEMBER_OF));
    Assert.assertNotNull(se.getValue(UserProcedure.MEMBERSHIP_NUMBER));
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String results2 = userProcedure.generateHash(se.getValue(UserProcedure.EMAIL));
    Assert.assertNotNull(results2);
    Assert.assertNotEquals(results, results2);
    se = userProcedure.getById(se.getValue(UserProcedure.EMAIL));
    logger.debug(se.getData().toString());
    Assert.assertNotNull(se.getValue(UserProcedure.MEMBER_OF));
    Assert.assertNotNull(se.getValue(UserProcedure.MEMBERSHIP_NUMBER));
  }

  @Test
  @Transactional
  public void testCheck() throws SparqlException, UserException {
    SparqlEntity se = new SparqlEntity();
    // se.setValue(SelectSparql.PRIMARY_KEY, "person789");
    se.setValue(UserProcedure.EMAIL, "person789@person.com");
    se.setValue(UserProcedure.GIVEN_NAME, "testperson789given");
    se.setValue(UserProcedure.FAMILY_NAME, "testperson789family");
    se.setValue(UserProcedure.VERIFIED_EMAIL, "true");
    se.setValue(UserProcedure.CONTRIBUTOR_ID, "789");
    userProcedure.add(se);

    String hash = userProcedure.generateHash(se.getValue(UserProcedure.EMAIL));

    SparqlEntity sePerson = userProcedure.getById(se.getValue(UserProcedure.EMAIL));

    Assert.assertTrue(userProcedure.checkApiKey(sePerson.getValue(UserProcedure.EMAIL), hash));
  }


  @Test
  @Transactional
  public void testConvertToHash() throws SparqlException, UserException {
//    prefix glytoucan: <http://www.glytoucan.org/glyco/owl/glytoucan#>
//      SELECT distinct ?email
//       WHERE {
//      ?o <http://schema.org/email> ?email
//  }
    
    // delete all older alternatename
    DeleteSparqlBean dsb = new DeleteSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "DELETE WHERE { "
        + "GRAPH <http://rdf.glytoucan.org/schema/users> {"
            + "?users <http://schema.org/alternateName> ?old . "
            + "}"
            + "}"
        );

    sparqlDAO.delete(dsb);

    SelectSparqlBean ssb = new SelectSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "SELECT ?email FROM\n"
        + "<http://rdf.glytoucan.org/schema/users>\n"
        + "WHERE { "
            + "?users <http://schema.org/email> ?email . "
            + "}"
        );
    
    List<SparqlEntity> results = sparqlDAO.query(ssb);
    
    for (Iterator iterator = results.iterator(); iterator.hasNext();) {
      SparqlEntity sparqlEntity = (SparqlEntity) iterator.next();
      String email = sparqlEntity.getValue("email");
      String hash = NumberGenerator.generateSHA256Hash(email);
      sparqlDAO.insert(new InsertSparqlBean("insert data { \n"
          + "graph <http://rdf.glytoucan.org/schema/users> {\n"
          + "<http://schema.org/Person#" + hash + "> <http://schema.org/alternateName> \"" + hash + "\" . \n"
          + "}\n"
          + "}"));
    }
    
    ssb = new SelectSparqlBean(" prefix schema: <http://schema.org/> SELECT distinct count(?o) ?email  WHERE {\n" + 
        "  ?uri schema:alternateName ?o .\n" + 
        "  ?uri schema:email ?email .\n" + 
        "  }    group by ?email"
        );
    sparqlDAO.query(ssb);
 

//    select *
//    where {
//    graph ?g{
//    #?o glytoucan:contributor ?contributor .
//    ?contributor a foaf:Person .
//    ?contributor foaf:name ?name
//    }}group by ?g limit 1000
    
  }
  
  @Test
  @Transactional
  public void testCleanupData() throws SparqlException, UserException {
    
//    prefix schema: <http://schema.org/> SELECT distinct count(?o) ?email  WHERE {
//      ?uri schema:alternateName ?o .
//      ?uri schema:email ?email .
//      }    group by ?email

//    prefix glytoucan: <http://www.glytoucan.org/glyco/owl/glytoucan#>
//      SELECT *
//       WHERE {
//      GRAPH ?g {?o glytoucan:contributor <http://rdf.glycoinfo.org/glytoucan/contributor/userId/105>}} GROUP BY ?g
//      limit 100
    
    // 5858, 5860, 5855
    
    DeleteSparqlBean dsb = new DeleteSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "DELETE WHERE { "
        + "GRAPH <http://rdf.glytoucan.org/schema/users> {"
            + "?users <http://schema.org/alternateName> \"5880\" . "
            + "}"
            + "}"
        );

    sparqlDAO.delete(dsb);

    SelectSparqlBean ssb = new SelectSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "SELECT * FROM\n"
        + "<http://rdf.glytoucan.org/schema/users>\n"
        + "WHERE { "
            + "?users <http://schema.org/alternateName> \"5880\" . "
            + "}"
        );

    List<SparqlEntity> list = sparqlDAO.query(ssb);
    Assert.assertEquals(0, list.size());

    dsb = new DeleteSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "DELETE WHERE { "
        + "GRAPH <http://rdf.glytoucan.org/schema/users> {"
            + "?users <http://schema.org/alternateName> \"5882\" . "
            + "}"
            + "}"
        );

    sparqlDAO.delete(dsb);

    ssb = new SelectSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "SELECT * FROM\n"
        + "<http://rdf.glytoucan.org/schema/users>\n"
        + "WHERE { "
            + "?users <http://schema.org/alternateName> \"5882\" . "
            + "}"
        );

    list = sparqlDAO.query(ssb);
    Assert.assertEquals(0, list.size());
    
    dsb = new DeleteSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "DELETE WHERE { "
        + "GRAPH <http://rdf.glytoucan.org/schema/users> {"
            + "?users <http://schema.org/alternateName> \"5863\" . "
            + "}"
            + "}"
        );

    sparqlDAO.delete(dsb);

    ssb = new SelectSparqlBean("prefix schema: <http://schema.org/>\n" + 
        "SELECT * FROM\n"
        + "<http://rdf.glytoucan.org/schema/users>\n"
        + "WHERE { "
            + "?users <http://schema.org/alternateName> \"5863\" . "
            + "}"
        );

    list = sparqlDAO.query(ssb);
    Assert.assertEquals(0, list.size());
    
//
//    dsb = new DeleteSparqlBean("prefix schema: <http://schema.org/>\n" + 
//        "DELETE WHERE { "
//        + "GRAPH <http://rdf.glytoucan.org/schema/users> {"
//            + "?users <http://schema.org/alternateName> \"5855\" . "
//            + "}"
//            + "}"
//        );
//
//    sparqlDAO.delete(dsb);
//
//    ssb = new SelectSparqlBean("prefix schema: <http://schema.org/>\n" + 
//        "SELECT * FROM\n"
//        + "<http://rdf.glytoucan.org/schema/users>\n"
//        + "WHERE { "
//            + "?users <http://schema.org/alternateName> \"5855\" . "
//            + "}"
//        );
//
//    sparqlDAO.query(ssb);
    
    ssb = new SelectSparqlBean(" prefix schema: <http://schema.org/> SELECT distinct count(?o) ?email  WHERE {\n" + 
        "  ?uri schema:alternateName ?o .\n" + 
        "  ?uri schema:email ?email .\n" + 
        "  }    group by ?email"
        );
    sparqlDAO.query(ssb);
 

//    select *
//    where {
//    graph ?g{
//    #?o glytoucan:contributor ?contributor .
//    ?contributor a foaf:Person .
//    ?contributor foaf:name ?name
//    }}group by ?g limit 1000
    
  }
      
  @Test
  @Transactional
  public void testConversionToEmail() throws SparqlException, UserException {
    
    // retrieve all persons emails
    SparqlEntity sparqlEntityPerson = new SparqlEntity();
    sparqlEntityPerson.setValue(UserProcedure.EMAIL, null);
//    sparqlEntityPerson.setValue(UserProcedure.GIVEN_NAME, null);
//    sparqlEntityPerson.setValue(UserProcedure.FAMILY_NAME, null);
//    sparqlEntityPerson.setValue(UserProcedure.VERIFIED_EMAIL, null);
    sparqlEntityPerson.setValue(UserProcedure.CONTRIBUTOR_ID, null);
//    sparqlEntityPerson.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);

    selectScintPerson.update(sparqlEntityPerson);

    List<SparqlEntity> list = sparqlDAO.query((SelectSparql) selectScintPerson.getSparqlBean());
    
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      SparqlEntity sparqlEntity = (SparqlEntity) iterator.next();
      logger.debug("email:>" + sparqlEntity.getValue(UserProcedure.EMAIL));
      logger.debug("UserProcedure.CONTRIBUTOR_ID:>" + sparqlEntity.getValue(UserProcedure.CONTRIBUTOR_ID));
      UserDetailsRequest request = new UserDetailsRequest();
      request.setPrimaryId(sparqlEntity.getValue(UserProcedure.EMAIL));
      UserDetailsResponse response = userProcedure.getDetails(request);
      
      sparqlEntity.setValue(UserProcedure.VERIFIED_EMAIL, response.getUser().getEmailVerified());

      // convert the unique id
      userProcedure.add(sparqlEntity);

      // confirm the contributor id is the same
      sparqlEntity.setValue(UserProcedure.CONTRIBUTOR_ID, null);
      sparqlEntity.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);
      List<SparqlEntity> listConfirm = sparqlDAO.query((SelectSparql) selectScintPerson.getSparqlBean());
      
      for (SparqlEntity sparqlEntity2 : listConfirm) {
        logger.debug("result email:>" + sparqlEntity2.getValue(UserProcedure.EMAIL));
        logger.debug("result contributor:>" + sparqlEntity2.getValue(UserProcedure.CONTRIBUTOR_ID));
      }
    }
    // passing old contributorid will retrieve latest hash
  }
  
  @Test
  @Transactional
  public void testGetKey() throws SparqlException, UserException {
    
    UserKeyRequest req = new UserKeyRequest();
    req.setPrimaryId("aokinobu@gmail.com");
    UserKeyResponse hash = userProcedure.getKey(req);
    logger.debug(hash);
    logger.debug(hash.getKey());
    Assert.assertNotNull(hash.getKey());
    Assert.assertEquals(hash.getPrimaryId(), "aokinobu@gmail.com");

    req.setPrimaryId("glytoucan@gmail.com");
    hash = userProcedure.getKey(req);
    logger.debug(hash);
    logger.debug(hash.getKey());
    Assert.assertNotNull(hash.getKey());
    Assert.assertEquals(hash.getPrimaryId(), "glytoucan@gmail.com");

    
  }
}
