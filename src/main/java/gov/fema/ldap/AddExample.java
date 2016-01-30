package gov.fema.ldap;


import com.unboundid.ldap.sdk.*;
import com.unboundid.ldif.LDIFException;

/**
 * demonstrate the {@link AddRequest}; this example uses a hard coded hostname of {@code
 * localhost} and port {@code 389} and attempts to add {@code uid=user,dc=example,dc=com}.
 */
public final class AddExample
{

  /**
   * demonstrate the {@link AddRequest}
   */
  public static void main(final String... args)
  {
    /*
    * Use connection options to specify that the connection attempt
    * should be 1 second and if the ADD request times out, the request
    * should be abandoned
    */
    final LDAPConnectionOptions connectionOptions = new LDAPConnectionOptions();
    connectionOptions.setAbandonOnTimeout(true);
    connectionOptions.setConnectTimeoutMillis(1000);

    /*
    * LDIF lines for the add request
    * 
    * String url = "ldap://ldap.forumsys.com:389";
	  String base = "dc=example, dc=com";
	  String userDn = "cn=read-only-admin, dc=example, dc=com";
	  String password = "password";
		
    */
//    final String[] ldifLines =
//            {"dn: uid=user,dc=example,dc=com", "changetype: add", "cn: Joe User", "sn: User",
//                    "uid: user", "userPassword: password"};
    final String[] ldifLines =
        {"dn: uid=user,dc=maxcrc,dc=com", "changetype: add", "cn: Joe User", "sn: User",
                "uid: user", "userPassword: secret"};

    //final String host = "ldap.example.com";
    
//    final String host = "ldap.forumsys.com";
    final String host = "DESKTOP-7KP2STH";
    final int port = 389;
    try
    {
      final LDAPConnection ldapConnection = new LDAPConnection(connectionOptions, host, port);
      try
      {
        final LDAPResult ldapResult = ldapConnection.add(new AddRequest(ldifLines));
        System.out.println(ldapResult);
      }
      catch(LDIFException e)
      {
        System.err.println(e);
      }
      finally
      {
        ldapConnection.close();
      }
    }
    catch(LDAPException e)
    {
      System.err.println(e);
    }

  }

}

