package org.glytoucan.admin.endpoint;

import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.convert.error.ConvertException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.DerivatizedMass;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.ResourceEntry;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.exception.InvalidException;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.Authentication;
import org.glytoucan.admin.model.ClassListRequest;
import org.glytoucan.admin.model.ClassListResponse;
import org.glytoucan.admin.model.ErrorCode;
import org.glytoucan.admin.model.ResponseMessage;
import org.glytoucan.admin.model.User;
import org.glytoucan.admin.model.UserCoreRequest;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserGenerateKeyRequest;
import org.glytoucan.admin.model.UserGenerateKeyResponse;
import org.glytoucan.admin.model.UserKeyCheckRequest;
import org.glytoucan.admin.model.UserKeyCheckResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.glytoucan.admin.model.UserRegisterCoreRequest;
import org.glytoucan.admin.model.UserRegisterRequest;
import org.glytoucan.admin.model.UserRegisterResponse;
import org.glytoucan.admin.service.AuthService;
import org.glytoucan.admin.service.UserProcedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * 
 * @author aoki
 * 
 *         The specification for this can be found at
 *         http://code.glytoucan.org/system/api_list/.
 *
 *         This work is licensed under the Creative Commons Attribution 4.0
 *         International License. To view a copy of this license, visit
 *         http://creativecommons.org/licenses/by/4.0/.
 *
 */
@Endpoint
public class UserEndpoint {
  private static final Log logger = LogFactory.getLog(UserEndpoint.class);
  private static final String NAMESPACE_URI = "http://model.admin.glytoucan.org/";

  @Autowired
  private UserProcedure userProcedure;

  @Autowired
  public UserEndpoint(UserProcedure userProcedure) {
    this.userProcedure = userProcedure;
  }

  @Autowired
  AuthService authService;

  /**
   * 
   * Query entry using accession number.
   * 
   * @param accessionNumber
   * @return
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userKeyRequest")
  @ResponsePayload
  @Transactional
  public UserKeyResponse getKey(@RequestPayload UserKeyRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getAuthentication().getId());
    Assert.notNull(request.getAuthentication().getApiKey());
    Assert.notNull(request.getPrimaryId());

    UserKeyResponse ukr = new UserKeyResponse();

    ResponseMessage rm = new ResponseMessage();

    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
        ukr.setPrimaryId(request.getPrimaryId());
        ukr.setResponseMessage(rm);
        return ukr;
    }

    SparqlEntity se = null;
    try {
      ukr = userProcedure.getKey(request);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("Invalid Request" + e.getMessage());
      rm.setErrorCode("-100");
      rm.setTime((new Date()).toString());
      ukr.setResponseMessage(rm);
      return ukr;
    }

    rm.setMessage("query result for:>" + request.getPrimaryId());
    rm.setErrorCode("0");
    rm.setTime((new Date()).toString());

    ukr.setResponseMessage(rm);
    return ukr;
  }

  /**
   * 
   * Query User Details using primaryId.
   * 
   * @param accessionNumber
   * @return
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userDetailsRequest")
  @ResponsePayload
  @Transactional
  public UserDetailsResponse userDetailsRequest(@RequestPayload UserDetailsRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getPrimaryId());

    UserDetailsResponse res = new UserDetailsResponse();

    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());

    SparqlEntity se = null;
    try {
      res = userProcedure.getDetails(request);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("Invalid Accession Number");
      rm.setErrorCode("-100");
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }

    rm.setMessage("query result for:>" + request.getPrimaryId());
    rm.setErrorCode("0");

    res.setResponseMessage(rm);
    return res;
  }
  
  /**
   * 
   * Query User Core using primaryId.
   * 
   * @param accessionNumber
   * @return
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userCoreRequest")
  @ResponsePayload
  @Transactional
  public UserDetailsResponse userCoreRequest(@RequestPayload UserCoreRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getEmail());

    UserDetailsResponse res = new UserDetailsResponse();

    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());

    SparqlEntity se = null;
    try {
      res = userProcedure.getCore(request);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("Invalid Accession Number");
      rm.setErrorCode("-100");
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }

    rm.setMessage("query result for:>" + request.getEmail());
    rm.setErrorCode("0");

    res.setResponseMessage(rm);
    return res;
  }
 
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userKeyCheckRequest")
  @ResponsePayload
  @Transactional
  public UserKeyCheckResponse userKeyCheckRequest(@RequestPayload UserKeyCheckRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getAuthentication().getId());
    Assert.notNull(request.getAuthentication().getApiKey());
    Assert.notNull(request.getContributorId());
    Assert.notNull(request.getApiKey());

    UserKeyCheckResponse res = new UserKeyCheckResponse();

//    UserDetailsResponse userRes = new UserDetailsResponse();
    
    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());
//    UserDetailsRequest userRequest = new UserDetailsRequest();
    
//    userRequest.setPrimaryId(request.getPrimaryId());
    
//    SparqlEntity se = null;
//    try {
//      userRes = userProcedure.getDetails(userRequest);
//    } catch (UserException e) {
//      // invalid data in se, return with errorcode.
//      rm.setMessage("Invalid Accession Number");
//      rm.setErrorCode("-100");
//      rm.setTime((new Date()).toString());
//      res.setResponseMessage(rm);
//      return res;
//    }
//    
//    User user = userRes.getUser();
//    
//    if (null == user) {
//      // invalid data in se, return with errorcode.
//      rm.setMessage("User " + request.getPrimaryId() + " does not exist");
//      rm.setErrorCode("-50");
//      rm.setTime((new Date()).toString());
//      res.setResponseMessage(rm);
//      return res;
//     }
    
//    try {
      Authentication auth = new Authentication();
      auth.setApiKey(request.getApiKey());
      auth.setId(request.getContributorId());
      rm = authService.authenticate(auth);
      boolean result = rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_SUCCESS.toString());
//      boolean result = userProcedure.checkApiKey(request.getContributorId(), request.getApiKey());
      
      res.setResult(result);
//    } catch (UserException e) {
//      // invalid data in se, return with errorcode.
//      rm.setMessage("Invalid API Key");
//      rm.setErrorCode("-100");
//      rm.setTime((new Date()).toString());
//      res.setResponseMessage(rm);
//      return res;
//    }
    


    rm.setMessage("query result for:>" + request.getContributorId());
    rm.setErrorCode("0");

    res.setResponseMessage(rm);
    return res;
  }
  
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userGenerateKeyRequest")
  @ResponsePayload
  @Transactional
  public UserGenerateKeyResponse generateKey(@RequestPayload UserGenerateKeyRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getPrimaryId());

    UserGenerateKeyResponse res = new UserGenerateKeyResponse();

    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());

    SparqlEntity se = null;
    try {
      res.setKey(userProcedure.generateHash(request.getPrimaryId()));
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("User Exception:" + e.getMessage());
      rm.setErrorCode("-100");
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }

    rm.setMessage("generated hash for " + request.getPrimaryId());
    rm.setErrorCode("0");

    res.setResponseMessage(rm);
    return res;
  }
  
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userRegisterRequest")
  @Transactional
  @ResponsePayload
  public UserRegisterResponse userRegisterRequest(@RequestPayload UserRegisterRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getUser());

    User user = request.getUser();
    UserRegisterResponse res = new UserRegisterResponse();

    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());

    SparqlEntity se = new SparqlEntity();
    se.setValue(UserProcedure.EMAIL, user.getEmail());
    se.setValue(UserProcedure.GIVEN_NAME, user.getGivenName());
    se.setValue(UserProcedure.FAMILY_NAME, user.getFamilyName());
    se.setValue(UserProcedure.VERIFIED_EMAIL, user.getEmailVerified());
    se.setValue(UserProcedure.CONTRIBUTOR_ID, user.getExternalId());
    
    try {
      userProcedure.add(se);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("User Exception:" + e.getMessage());
      rm.setErrorCode(ErrorCode.INVALID_PARAMETERS.toString());
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    
    UserDetailsRequest udr = new UserDetailsRequest();
    udr.setAuthentication(request.getAuthentication());
    udr.setPrimaryId(user.getEmail());
    UserDetailsResponse response = userDetailsRequest(udr);
    user = response.getUser();
    
    rm.setMessage("User generated for " + user.getGivenName());
    rm.setErrorCode("0");
    res.setResponseMessage(rm);
    res.setUser(user);
    
    return res;
  }
  
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userRegisterCoreRequest")
  @Transactional
  @ResponsePayload
  public UserRegisterResponse userRegisterRequest(@RequestPayload UserRegisterCoreRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getUser());

    User user = request.getUser();
    UserRegisterResponse res = new UserRegisterResponse();

    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());

    SparqlEntity se = new SparqlEntity();
    se.setValue(UserProcedure.EMAIL, user.getEmail());
    
    try {
      userProcedure.addCore(se);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("User Exception:" + e.getMessage());
      rm.setErrorCode(ErrorCode.INVALID_PARAMETERS.toString());
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    
    UserCoreRequest udr = new UserCoreRequest();
    udr.setAuthentication(request.getAuthentication());
    udr.setEmail(user.getEmail());
    UserDetailsResponse response = userCoreRequest(udr);
    user = response.getUser();
    
    rm.setMessage("User generated for " + user.getGivenName());
    rm.setErrorCode("0");
    res.setResponseMessage(rm);
    res.setUser(user);
    
    return res;
  }
  
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "classListRequest")
  @Transactional
  @ResponsePayload
  public ClassListResponse classListRequest(@RequestPayload ClassListRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getClassname());
    Assert.notNull(request.getPrefix());
    Assert.notNull(request.getPrefixUri());
    Assert.notNull(request.getPredicate());

    ClassListResponse res = new ClassListResponse();

    ResponseMessage rm = new ResponseMessage();
    rm = authService.authenticate(request.getAuthentication());
    if (rm.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILURE.toString())) {
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    rm.setTime((new Date()).toString());

    try {
      String result = userProcedure.getAllClass(request.getGraph(), request.getPrefix(), request.getPrefixUri(),
          request.getClassname(), request.getPredicate(), request.getLimit(), request.getOffset(),
          request.getDelimiter());
      rm.setMessage("result for " + request.getClassname());
      rm.setErrorCode("0");
      res.setResponseMessage(rm);
      res.setResults(result);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("User Exception:" + e.getMessage());
      rm.setErrorCode(ErrorCode.INVALID_PARAMETERS.toString());
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
    }

    return res;
  }
}