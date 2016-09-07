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
    Assert.notNull(request.getEmail());

    ResponseMessage rm = new org.glytoucan.admin.model.ResponseMessage();
    UserKeyResponse ukr = new UserKeyResponse();

    try {
      if (!authService.authenticate(request.getAuthentication())) {
        rm.setErrorCode("-403");
        rm.setMessage("unauthorized");
        ukr.setEmail(request.getEmail());
        ukr.setResponseMessage(rm);
        return ukr; 
      }
    } catch (UserException e1) {
      rm.setErrorCode("-403");
      rm.setMessage("failed to authenticate");
      ukr.setEmail(request.getEmail());
      ukr.setResponseMessage(rm);
      return ukr; 
    }
    
    SparqlEntity se = null;
	try {
		ukr = userProcedure.getKey(request);
	} catch (UserException e) {
		// invalid data in se, return with errorcode.
	    rm.setMessage("Invalid Accession Number");
	    rm.setErrorCode("-100");
      rm.setTime((new Date()).toString());
	    ukr.setResponseMessage(rm);
	    return ukr;
	}

    rm.setMessage("query result for:>" + request.getEmail());
    rm.setErrorCode("0");

    ukr.setResponseMessage(rm);
    return ukr;
  }

//  /**
//   * 
//   * Search for entry using sequence text.
//   * 
//   * @param sequence
//   *          text
//   * @return glycosequencesearchresponse
//   * 
//   */
//  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "glycoSequenceTextSearchRequest")
//  @ResponsePayload
//  public GlycoSequenceSearchResponse searchSequence(@RequestPayload GlycoSequenceTextSearchRequest request) {
//    Assert.notNull(request);
//    Assert.notNull(request.getSequence());
//    GlycoSequenceSearchResponse gssr = new GlycoSequenceSearchResponse();
//
//    SparqlEntity se;
//    try {
//      se = glycanProcedure.searchBySequence(request.getSequence());
//    } catch (SparqlException | ConvertException e) {
//      ResponseMessage rm = ResponseMessageGenerator.extractException(e);
//      rm.setErrorCode(new BigInteger(GlycanProcedure.CouldNotConvertErrorCode));
//      gssr.setResponseMessage(rm);
//      return gssr;
//    }
//
//    ResponseMessage rm = new ResponseMessage();
//    rm.setMessage(se.getValue(GlycanProcedure.FromSequence));
//    rm.setErrorCode(new BigInteger("0"));
//
//    gssr.setAccessionNumber(se.getValue(GlycanProcedure.AccessionNumber));
//    gssr.setSequence(se.getValue(GlycanProcedure.Sequence));
//    gssr.setImage(se.getValue(GlycanProcedure.Image));
//    gssr.setResponseMessage(rm);
//    return gssr;
//  }
//  
//  /**
//   * 
//   * Query for total count.
//   * 
//   * @return glycosequencecountresponse
//   * 
//   */
//  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "glycoSequenceCountRequest")
//  @ResponsePayload
//  public GlycoSequenceCountResponse countSequence(@RequestPayload GlycoSequenceCountRequest request) {
//    Assert.notNull(request);
//    GlycoSequenceCountResponse gscr = new GlycoSequenceCountResponse();
//
//    SparqlEntity se = glycanProcedure.getCount();
//
//    ResponseMessage rm = new ResponseMessage();
//    rm.setMessage(se.getValue("total"));
//    rm.setErrorCode(new BigInteger("0"));
//
//    gscr.setCount(se.getValue("total"));
//    gscr.setResponseMessage(rm);
//    return gscr;
//  }
}