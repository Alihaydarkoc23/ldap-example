package gov.fema.ldap;

import java.util.ArrayList;
import java.util.List;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

public class AddUser {

	public static void main(String[] args) throws LDAPException {

		insertTestUsers();

	}
	
	private static void insertTestUsers() throws LDAPException {
		  LDAPConnection connection = null;
		  try {
		    connection = new LDAPConnection("localhost", 10389);

		    // entry tim/sausages
		    List<Attribute> addRequest = new ArrayList<Attribute>();
		    addRequest.add(new Attribute("objectClass", "top"));
		    addRequest.add(new Attribute("objectClass", "person"));
		    addRequest.add(new Attribute("objectClass", "organizationalPerson"));
		    addRequest.add(new Attribute("objectClass", "inetOrgPerson"));
		    addRequest.add(new Attribute("cn", "Tim Fox"));
		    addRequest.add(new Attribute("sn", "Fox"));
		    addRequest.add(new Attribute("mail", "tim@example.com"));
		    addRequest.add(new Attribute("uid", "tim"));
		    addRequest.add(new Attribute("userPassword", "{ssha}d0M5Z2qjOOCSCQInvZHgVAleCqU5I+ag9ZHXMw=="));

		    connection.add("uid=tim,ou=User,dc=example,dc=com", addRequest);
		  } finally {
		    if (connection != null) {
		      connection.close();
		    }
		  }
		}

}
