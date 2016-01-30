package gov.fema.ldap;

import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
 
public class LdapSearch {
   public static void main(String[] args) throws NamingException {
      InitialLdapContext ctx = constructInitialLdapContext();
      // the name of the context to search
      //String contextName = "ou=groups,o=sevenSeas";
      String contextName = "ou=Group,ou=BusinessObjectsDisaster,ou=system";
      // Filter expression
      String filterExpr = "(uniquemember={0})"; // selects the groups a user belongs to.
 
      // Filter parameters (name of the user)
      //String userDN = "cn=Fletcher Christian,ou=people,o=sevenSeas";
      String userDN = "uid=awadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system";
      //String userDN = "cn=Anthony Wadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system";
      Object[] filterArgs = { userDN };
 
      SearchControls constraints = new javax.naming.directory.SearchControls();
      constraints.setSearchScope(SearchControls.SUBTREE_SCOPE); // SUBTREE_SCOPE means recursive search
 
      NamingEnumeration<SearchResult> search = ctx.search(contextName, filterExpr, filterArgs, constraints);
      System.out.println("Count of search :" + search.toString());
      while (search.hasMoreElements()) {
         System.out.println("Name " + search.next().getName());
      }
   }
 
   private static InitialLdapContext constructInitialLdapContext()
         throws NamingException {
      Properties env = new Properties();
      env.put("java.naming.factory.initial",
            "com.sun.jndi.ldap.LdapCtxFactory");
      // LDAP url
      env.put("java.naming.provider.url", "ldap://localhost:10389");
      // ldap login
      env.put("java.naming.security.principal", "uid=admin,ou=system");
      env.put("java.naming.security.credentials", "secret");
 
      return new InitialLdapContext(env, null);
   }
 
}
