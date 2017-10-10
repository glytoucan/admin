package org.glytoucan.admin.service;

import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.scint.DeleteScint;
import org.glycoinfo.rdf.scint.InsertScint;
import org.glycoinfo.rdf.scint.SelectScint;
import org.glycoinfo.rdf.service.impl.GraphConfig;
import org.glycoinfo.rdf.service.impl.MailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserProcedureConfig implements GraphConfig {
	
	public static final String graphUser = graph + "/schema/users";
	
	 @Bean(name = "selectclasslist")
	  SelectScint getSelectClassList() throws SparqlException {
	    SelectScint select = new SelectScint("schema", "http://schema.org/", "Person");
	    return select;
	  }
	
	@Bean(name = "selectscintperson")
	SelectScint getSelectPersonScint() throws SparqlException {
		SelectScint select = new SelectScint("schema", "http://schema.org/", "Person");
		return select;
	}

	@Bean(name = "insertscintperson")
	InsertScint getInsertPersonScint() throws SparqlException {
		InsertScint insert = new InsertScint(graphUser, "schema", "http://schema.org/", "Person");
//		insert.setClassHandler(getPersonClassHandler());
		return insert;
	}
//
	@Bean(name = "deletescintperson")
	DeleteScint getDeletePersonScint() throws SparqlException {
		DeleteScint delete = new DeleteScint(graphUser, "schema", "http://schema.org/", "Person");
//		delete.setClassHandler(getPersonClassHandler());
		return delete;
	}
	
	@Bean(name = "selectscintregisteraction")
	SelectScint getSelectRegisterActionScint() throws SparqlException {
		return new SelectScint("schema", "http://schema.org/", "RegisterAction");
	}

	@Bean(name = "selectScintOrganization")
	SelectScint getSelectScintOrganization() throws SparqlException {
		return new SelectScint("schema", "http://schema.org/", "Organization");
	}
	
	@Bean(name = "selectScintDateTime")
	SelectScint getSelectScintDateTime() throws SparqlException {
		return new SelectScint("schema", "http://schema.org/", "DateTime");
	}

	@Bean(name = "insertscintregisteraction")
	InsertScint getInsertRegisterActionScint() throws SparqlException {
		InsertScint insert = new InsertScint(graphUser, "schema", "http://schema.org/", "RegisterAction");
//		insert.getSparql().setGraph(graphUser);
//		insert.setClassHandler(new SelectScint());
		return insert;
	}
	
	@Bean(name = "insertScintProgramMembership")
	InsertScint getInsertScintProgramMembership() throws SparqlException {
		InsertScint insertScintProgramMembership = new InsertScint(graphUser, "schema", "http://schema.org/", "ProgramMembership");
		return insertScintProgramMembership;
	}
//
	@Bean(name = "deleteScintProgramMembership")
	DeleteScint getDeleteScintProgramMembership() throws SparqlException {
		DeleteScint deleteScintProgramMembership = new DeleteScint(graphUser, "schema", "http://schema.org/", "ProgramMembership");
		return deleteScintProgramMembership;
	}
//	
	@Bean(name = "selectScintProgramMembership")
	SelectScint getSelectProgramMembership() throws SparqlException {
		SelectScint select = new SelectScint("schema", "http://schema.org/", "ProgramMembership");
		select.getSparqlBean().setFrom("FROM <" + graphUser + ">");
		return select;
	}

	//	
//	ClassHandler getPersonClassHandler() throws SparqlException {
//		ClassHandler ch = new ClassHandler("schema", "http://schema.org/", "Person");
//		ch.setSparqlDAO(sparqlDAO);
//		return ch;
//	}
//	
////	ClassHandler getRegisterActionClassHandler() throws SparqlException {
////		ClassHandler ch = new ClassHandler("schema", "http://schema.org/", "RegisterAction");
////		ch.setSparqlDAO(sparqlDAO);
////		return ch; 
////	}
//	
//	ClassHandler getDateTimeClassHandler() throws SparqlException {
//		ClassHandler ch = new ClassHandler("schema", "http://schema.org/", "DateTime");
//		ch.setSparqlDAO(sparqlDAO);
//		return ch; 
//	}
//	
//	ClassHandler getProgramMembershipClassHandler() throws SparqlException {
//		ClassHandler classHandler = new ClassHandler("schema", "http://schema.org/", "ProgramMembership");
//		classHandler.setSparqlDAO(sparqlDAO);
//		return classHandler; 
//	}
//	
	@Bean(name = "userProcedure")
	UserProcedureRdf getUserProcedure() throws SparqlException {
		UserProcedureRdf user = new UserProcedureRdf();
		return user;
	}
//	
//	@Bean(name = "contributorProcedure")
//	ContributorProcedure getContributorProcedure() throws SparqlException {
//		ContributorProcedure cp = new ContributorProcedureRdf();
//		return cp;
//	}
//	
	@Bean
	MailService mailService() {
		return new MailService();
	}
}