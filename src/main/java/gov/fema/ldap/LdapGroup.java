package gov.fema.ldap;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
public class LdapGroup {

	public static void main(String[] args) {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389/ou=system");
        env.put(Context.URL_PKG_PREFIXES, "com.sun.jndi.url"); 
        env.put(Context.REFERRAL, "ignore"); 
        env.put(Context.SECURITY_AUTHENTICATION, "simple"); 
        //env.put(Context.SECURITY_PRINCIPAL, "cn=system,dc=*,dc=*"); 
        //env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_PRINCIPAL, "uid=awadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system");
        
        env.put(Context.SECURITY_CREDENTIALS, "secret");

        DirContext ctx;
        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        NamingEnumeration results = null;
        try {

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            //results = ctx.search("ou=path,dc=*,dc=*", "(objectClass=posixGroup)",controls);
//            results = ctx.search("ou=BusinessObjectsDisaster,ou=system,dc=*,dc=*", "(objectClass=posixGroup)",controls);
            results = ctx.search("ou=User,ou=BusinessObjectsDisaster,ou=system", "(objectClass=posixGroup)",controls);
            
            // Go through each item in list
            while (results.hasMore()) {
                SearchResult nc = (SearchResult)results.next();
                Attributes att=     nc.getAttributes();                           
                System.out.println("Group Name "+ att.get("cn").get(0));
                System.out.println("GID "+ att.get("GidNumber").get(0));
            }
        } catch (NameNotFoundException e) {
            System.out.println("Error : "+e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    System.out.println("Error : "+e);
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    System.out.println("Error : "+e);
                }
            }
        }      

    }


}
