package org.glytoucan.admin.service;


import java.util.List;

import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glytoucan.admin.exception.UserException;
import org.glytoucan.admin.model.UserDetailsRequest;
import org.glytoucan.admin.model.UserDetailsResponse;
import org.glytoucan.admin.model.UserKeyRequest;
import org.glytoucan.admin.model.UserKeyResponse;

/**
 * @author developer
 *
 */
public interface UserProcedure {
	public static final String GIVEN_NAME = "givenName";
	public static final String ID = "id";
	public static final String FAMILY_NAME = "familyName";
	public static final String GENDER = "gender";
	public static final String PICTURE = "picture";
	public static final String EMAIL ="email", VERIFIED_EMAIL="verifiedEmail";
	public static final String CONTRIBUTOR_ID = "alternateName"; // shortcut to map contributor id into Person;
	public static final String PROGRAM_NAME = "programName";
	public static final String GLYTOUCAN_PROGRAM = "glytoucanPartnerProgram";
	public static final String GLYTOUCAN_PROGRAM_TITLE = "Glytoucan Partner";
	public static final String MEMBERSHIP_NUMBER = "membershipNumber";
	public static final String MEMBER = "member";
	public static final String MEMBER_OF = "memberOf";
	
	public void add(SparqlEntity userSparqlEntity) throws UserException;  
	
	/**
	 * 
	 * Retrieve the user information based on unique identifier.  If both unique identifiers are returned (both external and internal), 
	 * then it assumed that the user was already registered. 
	 * 
	 * @param primaryId
	 * @return
	 * @throws UserException
	 */
	public SparqlEntity getById(String primaryId) throws UserException;
	
  public UserKeyResponse getKey(UserKeyRequest req) throws UserException;

	public String getIdByEmail(String email) throws UserException;
	
	public String generateHash(String primaryId) throws UserException;

	public List<SparqlEntity> getAll() throws UserException;

	public List<SparqlEntity> getByContributorId(String username) throws UserException;

	boolean checkApiKey(String username, String hash) throws UserException;

  public UserDetailsResponse getDetails(UserDetailsRequest request) throws UserException;

}