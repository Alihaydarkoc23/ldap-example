package gov.fema.ldap;

import java.util.List;

import javax.naming.ldap.LdapContext;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

public class Ldapview {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		String url = "ldap://ldap.forumsys.com:389";
		String base = "dc=example, dc=com";
		String userDn = "cn=read-only-admin, dc=example, dc=com";
		String password = "password";
//		final String[] ldifLines =
//	        {"dn: uid=user,dc=maxcrc,dc=com", "changetype: add", "cn: Joe User", "sn: User",
//	                "uid: user", "userPassword: secret"};
//		String url = "ldap://DESKTOP-7KP2STH:389";
//		String url = "ldap://localhost:389";
//		String base = "dc=maxcrc, dc=com";
//		String userDn = "cn=Manager, dc=maxcrc, dc=com";
//		String password = "secret";
		
		try {
			LdapContextSource ctxSrc = new LdapContextSource();
			ctxSrc.setUrl(url);
			ctxSrc.setBase(base);
			ctxSrc.setUserDn(userDn);
			ctxSrc.setPassword(password);
			ctxSrc.afterPropertiesSet();
			LdapTemplate lt = new LdapTemplate(ctxSrc);
			
			/** Get User **/
			AndFilter filter = new AndFilter();
			filter.and(new EqualsFilter("objectClass", "Person"));
			@SuppressWarnings("unchecked")
			List<String> list = lt.search("", filter.encode(), new ContactAttributeMapperJSON());
			//System.out.println(list.toString());
			
			/** Get Group **/
			list = lt.search("", "(&(objectClass=posixGroup, mapper)(objectClass=groupOfNames)(member=uid=" + "newton" + ",ou=People,dc=example,dc=com))",
								new GroupContextMapperJSON());
			System.out.println(list.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
