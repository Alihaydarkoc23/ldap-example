package gov.fema.ldap;

import java.util.List;
import java.util.Map;

import javax.naming.directory.DirContext;

public interface LdapDao {

	DirContext getLdapContext();

	List<UserDetails> attributeUserSearch(Map<String, String> searchAttributes);

	List<UserDetails> wildCardUserSearch(Map<String, String> searchAttributes);

}
