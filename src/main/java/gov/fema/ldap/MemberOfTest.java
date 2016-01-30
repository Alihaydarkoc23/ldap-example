package gov.fema.ldap;

import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class MemberOfTest{
    private static final String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String connectionURL = "ldap://localhost:10389";
    private static final String connectionName = "uid=admin,ou=system";
    private static final String connectionPassword = "secret";

    // Optioanl
    private static final String authentication = null;
    private static final String protocol = null;

    private static String username = "Anthony Wardsworth";

    //private static final String MEMBER_OF = "memberOf";
    private static final String MEMBER_OF = "member";
    private static final String[] attrIdsToSearch = new String[] { MEMBER_OF };
    public static final String SEARCH_BY_SAM_ACCOUNT_NAME = "(sAMAccountName=%s)";
    public static final String SEARCH_GROUP_BY_GROUP_CN = "(&(objectCategory=group)(cn={0}))";
    private static String userBase = "ou=User,ou=BusinessObjectsDisaster,ou=system";

    public static void main(String[] args) throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();

        // Configure our directory context environment.

        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        env.put(Context.PROVIDER_URL, connectionURL);
        env.put(Context.SECURITY_PRINCIPAL, connectionName);
        env.put(Context.SECURITY_CREDENTIALS, connectionPassword);
        if (authentication != null)
            env.put(Context.SECURITY_AUTHENTICATION, authentication);
        if (protocol != null)
            env.put(Context.SECURITY_PROTOCOL, protocol);

        InitialDirContext   context = new InitialDirContext(env);
        String              filter  = String.format(SEARCH_BY_SAM_ACCOUNT_NAME, username);
        SearchControls      constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIdsToSearch);
        NamingEnumeration results = context.search(userBase, filter,constraints);
        // Fail if no entries found
        if (results == null || !results.hasMore()) {
            System.out.println("No result found");
            return;
        }

        // Get result for the first entry found
        SearchResult result = (SearchResult) results.next();

        // Get the entry's distinguished name
        NameParser parser = context.getNameParser("");
        Name contextName = parser.parse(context.getNameInNamespace());
        Name baseName = parser.parse(userBase);

        Name entryName = parser.parse(new CompositeName(result.getName())
                .get(0));

        // Get the entry's attributes
        Attributes attrs = result.getAttributes();
        Attribute attr = attrs.get(attrIdsToSearch[0]);

        NamingEnumeration e = attr.getAll();
        System.out.println("Member of");
        while (e.hasMore()) {
            String value = (String) e.next();
            System.out.println(value);
        }
    }
}
