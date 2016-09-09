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
import org.glytoucan.admin.model.ResponseMessage;
import org.glytoucan.admin.model.User;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserGenerateKeyRequest;
import org.glytoucan.admin.model.UserGenerateKeyResponse;
import org.glytoucan.admin.model.UserKeyCheckRequest;
import org.glytoucan.admin.model.UserKeyCheckResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;
import org.glytoucan.admin.service.AuthService;
import org.glytoucan.admin.service.UserProcedure;
import org.springframework.beans.factory.annotation.Autowired;
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
  public UserKeyResponse getKey(@RequestPayload UserKeyRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getAuthentication().getId());
    Assert.notNull(request.getAuthentication().getApiKey());
    Assert.notNull(request.getPrimaryId());

    ResponseMessage rm = new org.glytoucan.admin.model.ResponseMessage();
    rm.setTime((new Date()).toString());
    UserKeyResponse ukr = new UserKeyResponse();

    try {
      if (!authService.authenticate(request.getAuthentication())) {
        rm.setErrorCode("-401");
        rm.setMessage("unauthorized");
        ukr.setPrimaryId(request.getPrimaryId());
        ukr.setResponseMessage(rm);
        return ukr;
      }
    } catch (UserException e1) {
      rm.setErrorCode("-401");
      rm.setMessage("failed to authenticate");
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
  public UserDetailsResponse userDetailsRequest(@RequestPayload UserDetailsRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getPrimaryId());

    ResponseMessage rm = new org.glytoucan.admin.model.ResponseMessage();
    rm.setTime((new Date()).toString());
    UserDetailsResponse res = new UserDetailsResponse();

    try {
      if (!authService.authenticate(request.getAuthentication())) {
        rm.setErrorCode("-403");
        rm.setMessage("unauthorized");
        res.setResponseMessage(rm);
        return res;
      }
    } catch (UserException e1) {
      rm.setErrorCode("-403");
      rm.setMessage("failed to authenticate");
      res.setResponseMessage(rm);
      return res;
    }

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
 
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userKeyCheckRequest")
  @ResponsePayload
  public UserKeyCheckResponse userKeyCheckRequest(@RequestPayload UserKeyCheckRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getAuthentication().getId());
    Assert.notNull(request.getAuthentication().getApiKey());
    Assert.notNull(request.getPrimaryId());
    Assert.notNull(request.getApiKey());

    ResponseMessage rm = new org.glytoucan.admin.model.ResponseMessage();
    rm.setTime((new Date()).toString());
    UserKeyCheckResponse res = new UserKeyCheckResponse();

    UserDetailsResponse userRes = new UserDetailsResponse();
    
    try {
      if (!authService.authenticate(request.getAuthentication())) {
        rm.setErrorCode("-403");
        rm.setMessage("unauthorized");
        res.setResponseMessage(rm);
        return res;
      }
    } catch (UserException e1) {
      rm.setErrorCode("-403");
      rm.setMessage("failed to authenticate");
      res.setResponseMessage(rm);
      return res;
    }
    UserDetailsRequest userRequest = new UserDetailsRequest();
    
    userRequest.setPrimaryId(request.getPrimaryId());
    
    SparqlEntity se = null;
    try {
      userRes = userProcedure.getDetails(userRequest);
    } catch (UserException e) {
      // invalid data in se, return with errorcode.
      rm.setMessage("Invalid Accession Number");
      rm.setErrorCode("-100");
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
    }
    
    User user = userRes.getUser();
    
    if (null == user) {
      // invalid data in se, return with errorcode.
      rm.setMessage("User " + request.getPrimaryId() + " does not exist");
      rm.setErrorCode("-50");
      rm.setTime((new Date()).toString());
      res.setResponseMessage(rm);
      return res;
     }
    
    se = null;
    try {
      boolean result = userProcedure.checkApiKey(user.getExternalId(), request.getApiKey());
      res.setResult(result);
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

  public UserGenerateKeyResponse generateKey(UserGenerateKeyRequest request) {
    Assert.notNull(request);
    Assert.notNull(request.getAuthentication());
    Assert.notNull(request.getPrimaryId());

    ResponseMessage rm = new org.glytoucan.admin.model.ResponseMessage();
    rm.setTime((new Date()).toString());
    UserGenerateKeyResponse res = new UserGenerateKeyResponse();

    try {
      if (!authService.authenticate(request.getAuthentication())) {
        rm.setErrorCode("-403");
        rm.setMessage("unauthorized");
        res.setResponseMessage(rm);
        return res;
      }
    } catch (UserException e1) {
      rm.setErrorCode("-403");
      rm.setMessage("failed to authenticate");
      res.setResponseMessage(rm);
      return res;
    }

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
  
}