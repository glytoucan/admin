package org.glytoucan.admin.service;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlDAO;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.Contributor;
import org.glycoinfo.rdf.scint.ClassHandler;
import org.glycoinfo.rdf.scint.DeleteScint;
import org.glycoinfo.rdf.scint.InsertScint;
import org.glycoinfo.rdf.scint.Scintillate;
import org.glycoinfo.rdf.scint.SelectScint;
import org.glycoinfo.rdf.service.ContributorProcedure;
import org.glycoinfo.rdf.service.exception.ContributorException;
import org.glycoinfo.rdf.service.impl.MailService;
import org.glycoinfo.rdf.utils.NumberGenerator;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.User;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author developer
 *
 */
@Service
public class UserProcedureRdf implements UserProcedure {

  Log logger = LogFactory.getLog(UserProcedureRdf.class);

  String[] requiredFields = { "email", "givenName", "familyName", "verifiedEmail", UserProcedure.CONTRIBUTOR_ID };

  @Autowired
  SparqlDAO sparqlDAO;

  @Autowired
  @Qualifier(value = "selectscintperson")
  SelectScint selectScintPerson;

  @Autowired
  @Qualifier(value = "insertscintperson")
  InsertScint insertScintPerson;

  @Autowired
  @Qualifier(value = "deletescintperson")
  DeleteScint deleteScintPerson;

  @Autowired
  @Qualifier(value = "selectscintregisteraction")
  SelectScint selectScintRegisterAction;

  @Autowired
  @Qualifier(value = "insertscintregisteraction")
  InsertScint insertScintRegisterAction;

  @Autowired
  @Qualifier(value = "insertScintProgramMembership")
  InsertScint insertScintProgramMembership;

  @Autowired
  @Qualifier(value = "deleteScintProgramMembership")
  DeleteScint deleteScintProgramMembership;

  @Autowired
  @Qualifier(value = "selectScintProgramMembership")
  SelectScint selectScintProgramMembership;

  @Autowired
  @Qualifier(value = "selectScintOrganization")
  SelectScint selectScintOrganization;

  @Autowired
  @Qualifier(value = "selectScintDateTime")
  SelectScint selectScintDateTime;

  // @Autowired
  // @Qualifier(value = "contributorProcedure")
  // ContributorProcedure contributorProcedure;

  @Autowired
  MailService mailService;

  ClassHandler getPersonClassHandler() throws UserException {
    ClassHandler scint = new ClassHandler("schema", "http://schema.org/", "Person");
    scint.setSparqlDAO(sparqlDAO);
    return scint;
  }

  // ClassHandler getRegisterActionClassHandler() throws UserException {
  // ClassHandler scint = new ClassHandler("schema", "http://schema.org/",
  // "RegisterAction");
  // scint.setSparqlDAO(sparqlDAO);
  // return scint;
  // }


  ClassHandler getOrganizationClassHandler() throws UserException {
    ClassHandler ch = new ClassHandler("schema", "http://schema.org/", "Organization");
    ch.setSparqlDAO(sparqlDAO);
    return ch;
  }

  ClassHandler getProgramMembershipClassHandler() throws UserException {
    ClassHandler classHandler = new ClassHandler("schema", "http://schema.org/", "ProgramMembership");
    classHandler.setSparqlDAO(sparqlDAO);
    return classHandler;
  }

//  @Override
  @Transactional
  public void add(SparqlEntity userSparqlEntity) throws UserException {
    Set<String> columns = userSparqlEntity.getColumns();
    if (!columns.containsAll(Arrays.asList(requiredFields))) {
      throw new UserException("not all required fields are supplied");
    }

    String id = determinePrimaryKey(userSparqlEntity.getValue(UserProcedureRdf.EMAIL));

    SparqlEntity userdetails = getById(userSparqlEntity.getValue(UserProcedureRdf.EMAIL));

    // get primary key from parameter (security)
    // String personUID = userSparqlEntity.getValue(SelectSparql.PRIMARY_KEY);
    // check for primary key of user. (should be at least at UUID)
    if (id == null || id.equals("")) {
      throw new UserException("PRIMARY_KEY is >" + id + "<");
    }

    final SparqlEntity sparqlentityPerson = new SparqlEntity(id);

    for (String field : requiredFields) {
      if (field.equals("verifiedEmail"))
        continue;

      sparqlentityPerson.setValue(field, userSparqlEntity.getValue(field));
    }

    // if you have a verified email, you are a contributor of the glytoucan
    // organization.

    // new Organization Class
    // SelectScint organizationSelect = new SelectScint();
    // organizationSelect.setClassHandler(getOrganizationClassHandler());

    // Organization entity
    SparqlEntity sparqlentityOrganization = new SparqlEntity("glytoucan");
    try {
      selectScintOrganization.update(sparqlentityOrganization);
    } catch (SparqlException e) {
      throw new UserException(e);
    }

    String verified = userSparqlEntity.getValue(VERIFIED_EMAIL);
    if (verified != null && verified.equals("true")) {
      sparqlentityPerson.setValue("contributor", selectScintOrganization);
    }

    // otherwise just a member.
    sparqlentityPerson.setValue("member", selectScintOrganization);

    if (StringUtils.isBlank(userSparqlEntity.getValue(org.glytoucan.admin.service.UserProcedureRdf.EMAIL)))
      throw new UserException("email cannot be blank.  Please fix account information.");

    // find external contributor id
    String contributorId = userSparqlEntity.getValue(UserProcedure.CONTRIBUTOR_ID);

    if (StringUtils.isBlank(nameMap(sparqlentityPerson)) && !StringUtils.equals(contributorId, "815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d"))
      throw new UserException("given name cannot be blank.  Please fix account information or login with google+.");


    // try {
    // contributorId =
    // contributorProcedure.addContributor(nameMap(sparqlentityPerson));
    // } catch (ContributorException e) {
    // throw new UserException(e);
    // }

    sparqlentityPerson.setValue(CONTRIBUTOR_ID, contributorId);

    try {
      // before insert, hash the id.
      sparqlentityPerson.setValue(SelectSparql.PRIMARY_KEY,
          id);
      insertScintPerson.update(sparqlentityPerson);

      sparqlDAO.insert(insertScintPerson.getSparqlBean());
    } catch (SparqlException e) {
      throw new UserException(e);
    }

    // RegisterAction entityrequiredFields = {SelectSparql.PRIMARY_KEY, "email",
    // "givenName", "familyName", "verifiedEmail"};
    // UID is userid + register class
    SparqlEntity sparqlentityRegisterAction = new SparqlEntity(id);

    // new DateTime Class
    // SelectScint dateTimeSelect = new SelectScint();
    // dateTimeSelect.setClassHandler(getDateTimeClassHandler());

    // check for date time

    // no date time so use current

    // DateTime entity
    SparqlEntity sparqlentityDateTime = new SparqlEntity(new Date());
    try {
      selectScintDateTime.update(sparqlentityDateTime);

      // set the datetime class to be the startTime range for the registeraction
      // domain.
      sparqlentityRegisterAction.setValue("startTime", selectScintDateTime);

      SparqlEntity sparqlEntityPerson = new SparqlEntity(id);
      // SelectScint personSelect = new SelectScint();
      // personSelect.setClassHandler(getPersonClassHandler());
      selectScintPerson.update(sparqlEntityPerson);

      sparqlentityRegisterAction.setValue("participant", selectScintPerson);

      // set the sparqlentity for the registeraction.
      insertScintRegisterAction.update(sparqlentityRegisterAction);

      sparqlDAO.insert(insertScintRegisterAction.getSparqlBean());

      String name = userSparqlEntity.getValue(GIVEN_NAME);

      String sendEmail = userSparqlEntity.getValue("sendEmail");
      if (StringUtils.isNotBlank(sendEmail)) {
        mailService.newRegistration(userdetails.getValue(UserProcedureRdf.EMAIL), name);
        mailService.newRegistrationAdmin(sparqlEntityPerson);
      }
    } catch (SparqlException e) {
      throw new UserException(e);
    }
  }

  @Transactional
  public String generateHash(String id) throws UserException {
    // SparqlEntity userSE = getUser(email);
    // String userid = userSE.getValue(ID);

    try {
      // check if it exists
      SparqlEntity sparqlEntityPerson = new SparqlEntity(determinePrimaryKey(id));
      sparqlEntityPerson.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);
      selectScintPerson.update(sparqlEntityPerson);
      List<SparqlEntity> person = sparqlDAO.query(selectScintPerson.getSparqlBean());
      if (null == person || !person.iterator().hasNext())
        throw new UserException("id >" + id + "< doesnt exist");

      // insertScintPerson.setClassHandler(getPersonClassHandler());
      insertScintPerson.getSparqlBean().setSparqlEntity(sparqlEntityPerson);

      // ProgramMembership entity
      SparqlEntity sparqlentityProgramMembership = new SparqlEntity(
          GLYTOUCAN_PROGRAM + sparqlEntityPerson.getValue(SelectSparql.PRIMARY_KEY));
      sparqlentityProgramMembership.setValue(PROGRAM_NAME, GLYTOUCAN_PROGRAM_TITLE);

      sparqlentityProgramMembership.setValue(MEMBER, insertScintPerson);

      // delete the previous Program Membership
      sparqlentityProgramMembership.setValue(UserProcedureRdf.MEMBERSHIP_NUMBER, null);
      selectScintProgramMembership.update(sparqlentityProgramMembership);
      List<SparqlEntity> list = sparqlDAO.query(selectScintProgramMembership.getSparqlBean());

      // if it does exist
      if (list.iterator().hasNext()) {
        SparqlEntity se = list.iterator().next();
        se.setValue(SelectSparql.PRIMARY_KEY,
            UserProcedureRdf.GLYTOUCAN_PROGRAM + sparqlEntityPerson.getValue(SelectSparql.PRIMARY_KEY));

        deleteScintProgramMembership.update(se);
        sparqlDAO.delete(deleteScintProgramMembership.getSparqlBean());

        sparqlEntityPerson.setValue(MEMBER_OF, deleteScintProgramMembership);
        deleteScintPerson.update(sparqlEntityPerson);
        sparqlDAO.delete(deleteScintPerson.getSparqlBean());
      }

      Date dateVal = new Date();

      sparqlentityProgramMembership.setValue(MEMBERSHIP_NUMBER,
          NumberGenerator.generateHash(sparqlEntityPerson.getValue(SelectSparql.PRIMARY_KEY)
              + sparqlentityProgramMembership.getValue("programName")
              + sparqlentityProgramMembership.getValue(SelectSparql.PRIMARY_KEY) + "SEED", dateVal));

      // set the sparqlentity for the registeraction.
      // insertScintProgramMembership.setClassHandler(getProgramMembershipClassHandler());
      insertScintProgramMembership.update(sparqlentityProgramMembership);
      logger.debug(insertScintProgramMembership.getSparqlBean().getSparql());
      sparqlDAO.insert(insertScintProgramMembership.getSparqlBean());

      sparqlEntityPerson.setValue(MEMBER_OF, insertScintProgramMembership);
      insertScintPerson.update(sparqlEntityPerson);
      sparqlDAO.insert(insertScintPerson.getSparqlBean());

      return sparqlentityProgramMembership.getValue(MEMBERSHIP_NUMBER);
    } catch (SparqlException e) {
      throw new UserException(e);
    }

  }

  private String nameMap(SparqlEntity sparqlEntity) {
    // the name map is the mapping of gmail address to contributor ID.
    return sparqlEntity.getValue(org.glytoucan.admin.service.UserProcedureRdf.GIVEN_NAME) + " " + sparqlEntity.getValue(FAMILY_NAME);
  }

  
  @Transactional
  public SparqlEntity getById(String primaryId) throws UserException {
    try {

      // set the main unique identifier
      String id = determinePrimaryKey(primaryId);
      SparqlEntity userSE = new SparqlEntity(id);

      // Select Person
      // SelectScint personScint = new SelectScint();
      // personScint.setClassHandler(getPersonClassHandler());
      selectScintPerson.update(userSE);

      List<SparqlEntity> userData = sparqlDAO.query(selectScintPerson.getSparqlBean());

      if (userData.size() > 1)
        logger.warn("more than one userData for :>" + userSE);
      if (userData.iterator().hasNext()) {
        SparqlEntity first = userData.iterator().next();

        // set contributor name.
        first.setValue(Contributor.NAME, nameMap(first));

        SparqlEntity programMembership = new SparqlEntity(GLYTOUCAN_PROGRAM + id);
        // selectScintProgramMembership.setClassHandler(getProgramMembershipClassHandler());
        programMembership.setValue("member", selectScintPerson);

        selectScintProgramMembership.update(programMembership);
        List<SparqlEntity> membershipData = sparqlDAO.query(selectScintProgramMembership.getSparqlBean());

        // if memberOf program, find id.
        if (!membershipData.isEmpty()) {
          // check for Hash
          for (Iterator iterator = membershipData.iterator(); iterator.hasNext();) {
            SparqlEntity sparqlEntity = (SparqlEntity) iterator.next();
            String programName = sparqlEntity.getValue(UserProcedureRdf.PROGRAM_NAME);
            String hash = null;
            if (StringUtils.isNotBlank(programName) && programName.equals(UserProcedureRdf.GLYTOUCAN_PROGRAM_TITLE))
              hash = sparqlEntity.getValue(MEMBERSHIP_NUMBER);
            first.setValue(MEMBERSHIP_NUMBER, hash);
          }
        }

        return first;
      } else
        return null;
    } catch (SparqlException e) {
      throw new UserException(e);
    }
  }

  public String[] getRequiredFields() {
    return requiredFields;
  }

  public void setRequiredFields(String[] requiredFields) {
    this.requiredFields = requiredFields;
  }

  // @Override
  // public String getMembershipInfo(String email) throws UserException {
  // SparqlEntity emailSE = new SparqlEntity();
  // emailSE.setValue(EMAIL, email);
  // SparqlEntity contributor = null;
  // contributor = getMembershipI(nameMap(emailSE));
  // if (StringUtils.isNotBlank(contributor.getValue(Contributor.ID))) {
  // emailSE.setValue(CONTRIBUTOR_ID, contributor.getValue(Contributor.ID));
  // }
  // return emailSE.getValue(CONTRIBUTOR_ID);
  // }

  @Transactional
  public String getIdByEmail(String email) throws UserException {
    try {
      SparqlEntity emailSE = new SparqlEntity();
      emailSE.setValue(EMAIL, email);

      SparqlEntity sparqlEntityPerson = new SparqlEntity();
      sparqlEntityPerson.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);
      sparqlEntityPerson.setValue(CONTRIBUTOR_ID, null);
      sparqlEntityPerson.setValue(EMAIL, email);

      selectScintPerson.update(sparqlEntityPerson);
      List<SparqlEntity> person = sparqlDAO.query(selectScintPerson.getSparqlBean());
      if (person.iterator().hasNext()) {
        SparqlEntity sparqlEntity = (SparqlEntity) person.iterator().next();
        return sparqlEntity.getValue(CONTRIBUTOR_ID);
      }
      throw new UserException("cannot identify user via email");
    } catch (SparqlException e) {
      throw new UserException(e);
    }

  }

  @Transactional
  public List<SparqlEntity> getAll() throws UserException {
    try {
      return sparqlDAO.query(selectScintPerson.getSparqlBean());
    } catch (SparqlException e) {
      throw new UserException(e);
    }
  }


  @Transactional
  public List<SparqlEntity> getByContributorId(String username) throws UserException {
    try {
      SparqlEntity sparqlEntityPerson = new SparqlEntity();
      sparqlEntityPerson.setValue("alternateName", username);
      sparqlEntityPerson.setValue("givenName", null);
      sparqlEntityPerson.setValue("email", null);
      sparqlEntityPerson.setValue(Scintillate.NO_DOMAINS, SelectSparql.TRUE);
      selectScintPerson.update(sparqlEntityPerson);

      return sparqlDAO.query(selectScintPerson.getSparqlBean());
    } catch (SparqlException e) {
      throw new UserException(e);
    }

  }

  @Transactional
  public boolean checkApiKey(String contributorId, String hash) throws UserException {
    try {
      if (StringUtils.isBlank(contributorId) || StringUtils.isBlank(hash)) {
        throw new UserException("username or key cannot be blank");
      }

      hash = hash.trim();
      String username = contributorId.trim();
      
      // if not email, be a nice guy and convert from contributor id
      UserDetailsRequest udRequest = new UserDetailsRequest();
      if (!username.contains("@")) {
        List<SparqlEntity> list = getByContributorId(username);
        for (SparqlEntity sparqlEntity : list) {
          username = sparqlEntity.getValue("email");
          if (StringUtils.isBlank(username))
            continue;
        }
        if (StringUtils.isBlank(username))
          throw new UserException("cannot retrieve primaryId from contributor" + contributorId);
      }

      SparqlEntity pmSE = new SparqlEntity(GLYTOUCAN_PROGRAM + determinePrimaryKey(username));
      pmSE.setValue(UserProcedureRdf.MEMBER, selectScintPerson);
      pmSE.setValue(UserProcedureRdf.MEMBERSHIP_NUMBER, null);
      pmSE.setValue(Scintillate.NO_DOMAINS, true);
      selectScintProgramMembership.update(pmSE);

      List<SparqlEntity> resultsPM = sparqlDAO.query(selectScintProgramMembership.getSparqlBean());
      if (resultsPM.iterator().hasNext()) {
        SparqlEntity pmResultsSE = resultsPM.iterator().next();

        String storedHash = pmResultsSE.getValue(UserProcedureRdf.MEMBERSHIP_NUMBER);
        logger.debug("input hash:>" + hash + "<");
        logger.debug("storedhash:>" + storedHash + "<");
        return hash.equals(storedHash);
      }
      return false;
    } catch (SparqlException e) {
      throw new UserException(e);
    }
  }

  /**
   * 
   * The email address is used as the primary key. Based on that id, there
   * should be only one membership with the same member.
   * 
   * @see org.glytoucan.admin.service.UserProcedure#getKey(org.glytoucan.client.soap.UserKeyRequest)
   */
  @Transactional
  public UserKeyResponse getKey(UserKeyRequest req) throws UserException {
    String email = req.getPrimaryId();
    String primaryKey = determinePrimaryKey(email);
    SparqlEntity sparqlEntityPerson = new SparqlEntity(primaryKey);
    UserKeyResponse ukr = new UserKeyResponse();
    try {
      selectScintPerson.update(sparqlEntityPerson);

      SparqlEntity pmSE = new SparqlEntity(GLYTOUCAN_PROGRAM + primaryKey);
      pmSE.setValue(UserProcedureRdf.MEMBER, selectScintPerson);
      pmSE.setValue(UserProcedureRdf.MEMBERSHIP_NUMBER, null);
      pmSE.setValue(Scintillate.NO_DOMAINS, true);
      selectScintProgramMembership.update(pmSE);

      ukr.setPrimaryId(email);

      List<SparqlEntity> resultsPM = sparqlDAO.query(selectScintProgramMembership.getSparqlBean());
      if (resultsPM.iterator().hasNext()) {
        SparqlEntity pmResultsSE = resultsPM.iterator().next();

        String storedHash = pmResultsSE.getValue(UserProcedureRdf.MEMBERSHIP_NUMBER);
        ukr.setKey(storedHash);
      }
    } catch (SparqlException e) {
      throw new UserException(e);
    }

    return ukr;
  }

  @Override
  @Transactional
  public UserDetailsResponse getDetails(UserDetailsRequest req) throws UserException {
    String id = req.getPrimaryId();
    String primaryKey = determinePrimaryKey(id);
    SparqlEntity sparqlEntityPerson = new SparqlEntity(primaryKey);
    UserDetailsResponse res = new UserDetailsResponse();
    try {
//      sparqlEntityPerson.setValue(Scintillate.NO_DOMAINS, true);
      selectScintPerson.update(sparqlEntityPerson);

      List<SparqlEntity> sRes = sparqlDAO.query(selectScintPerson.getSparqlBean());
      User user = new User();
      if (sRes.iterator().hasNext()) {
        SparqlEntity sResultsSE = sRes.iterator().next();

        user.setEmail(sResultsSE.getValue(EMAIL));
//        verified or not is determined by member or contributor
//        user.setEmailVerified(sResultsSE.getValue(VERIFIED_EMAIL)); 
        user.setExternalId(sResultsSE.getValue(CONTRIBUTOR_ID));
        user.setFamilyName(sResultsSE.getValue(FAMILY_NAME));
        user.setGivenName(sResultsSE.getValue(GIVEN_NAME));
        user.setPrimaryId(sResultsSE.getValue(ID));
      }
      
      String primaryId = determinePrimaryKey(id);
      final SparqlEntity sparqlentityPerson = new SparqlEntity(primaryId);

      // Organization entity
      SparqlEntity sparqlentityOrganization = new SparqlEntity(UserProcedure.GLYTOUCAN_ORG);
      try {
        selectScintOrganization.update(sparqlentityOrganization);
      } catch (SparqlException e) {
        throw new UserException(e);
      }

      sparqlentityPerson.setValue("contributor", selectScintOrganization);
      
      selectScintPerson.update(sparqlentityPerson);
      List<SparqlEntity> results = sparqlDAO.query(selectScintPerson.getSparqlBean());
      if (results.size() > 0)
        user.setEmailVerified(VERIFIED_EMAIL_TRUE);
      else
        user.setEmailVerified(VERIFIED_EMAIL_FALSE);
      
      res.setUser(user);

    } catch (SparqlException e) {
      throw new UserException(e);
    }
    
    return res;
  }
  
  private String determinePrimaryKey(String email) {
    return NumberGenerator.generateSHA256Hash(email);
  }
}