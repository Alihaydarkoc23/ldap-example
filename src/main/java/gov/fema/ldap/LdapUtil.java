package gov.fema.ldap;

import gov.fema.ldap.Person;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.unboundid.ldap.sdk.CompareRequest;
import com.unboundid.ldap.sdk.CompareResult;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.DNEntrySource;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.EntrySourceException;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;
 
/**
 *  gov.fema.ldap.LdapUtil
 *
 *  @author tariq ahsan
 */

public class LdapUtil {
 
    private Logger logger = Logger.getLogger(LdapUtil.class);
	//private Logger logger = LoggerFactory.getLogger(LDAPMain.class);
    private Hashtable<String, String> env = new Hashtable<String, String>();
    Properties config = new Properties();
 
    public LdapUtil() {
        try {
        	
        	// LDAP Connectivity
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
            env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
            env.put(Context.SECURITY_CREDENTIALS, "secret");     
            
            // Loading properties file
            InputStream inputStream = ResourceLoader.class.getResourceAsStream("/ldap.properties");
            config.load(inputStream);
            
        } catch (Exception e) {
            logger.error(e, e);
        }
 
    }
    
//    private LdapContext ldapAuthenticate(String password, String userdn) throws UserNotAuthorizedException, AuthorisationConnectionException
//	{
//		Hashtable<String, String> env = new Hashtable<String,String>();
//	env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
//		//set security credentials, note using simple cleartext authentication
//		env.put(Context.SECURITY_AUTHENTICATION, ldapSecurityAuthenicationType);
//		env.put(Context.SECURITY_PRINCIPAL, userdn);
//		env.put(Context.SECURITY_CREDENTIALS, password);
//		//connect to my domain controller
//		env.put(Context.PROVIDER_URL, activeServerLdapURL);
//		//Create the initial directory context
//		LdapContext ctx = null;
//		try {
//			ctx = new InitialLdapContext(env,null);
//		} catch (AuthenticationException e) {
//                        //You will get an exception here if the username/password is incorrect
//                        //handle it yourself!
//		} catch (Exception e) {
//                       //something went wrong
//                       ///handle in some way
//		}
//		return ctx;
//	}
 
    private boolean insert(Person person) {
        try {
 
            DirContext dctx = new InitialDirContext(env);
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute("uid", person.getName()));
            matchAttrs.put(new BasicAttribute("cn", person.getName()));
            matchAttrs.put(new BasicAttribute("street", person.getAddress()));
            matchAttrs.put(new BasicAttribute("sn", person.getSurName()));
            matchAttrs.put(new BasicAttribute("mail", person.getEmail()));
//            matchAttrs.put(new BasicAttribute("createTimestamp", person.getCreateTimestamp()));
            matchAttrs.put(new BasicAttribute("createTimestamp", "20160126214618.067Z"));
            
            matchAttrs.put(new BasicAttribute("userpassword", encryptLdapPassword("SHA", person.getPassword())));
            matchAttrs.put(new BasicAttribute("objectclass", "top"));
            matchAttrs.put(new BasicAttribute("objectclass", "person"));
            matchAttrs.put(new BasicAttribute("objectclass", "organizationalPerson"));
            matchAttrs.put(new BasicAttribute("objectclass", "inetorgperson"));
            //String name = "uid=" + person.getName() + ",ou=users,ou=system";
            String name = "uid=" + person.getName() + config.getProperty("BoUser");
            
            InitialDirContext iniDirContext = (InitialDirContext) dctx;
            iniDirContext.bind(name, dctx, matchAttrs);
 
            logger.debug("success inserting "+person.getName());
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
    
    private boolean createGroup(Group group) {
        try {
 
            DirContext dctx = new InitialDirContext(env);
            Attributes matchAttrs = new BasicAttributes(true);
            
            // Construct the group name
            String groupName = group.getName() + group.getDisasterNumber() + group.getReportType();
            
            matchAttrs.put(new BasicAttribute("cn", groupName));         
            //matchAttrs.put(new BasicAttribute("member", "uid=awadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system"));
            matchAttrs.put(new BasicAttribute("member", ""));
            matchAttrs.put(new BasicAttribute("objectclass", "top"));
            matchAttrs.put(new BasicAttribute("objectclass", "groupOfNames"));
            String name = "cn=" + groupName + "," + config.getProperty("BoGroup");
            
            InitialDirContext iniDirContext = (InitialDirContext) dctx;
            iniDirContext.bind(name, dctx, matchAttrs);
 
            logger.debug("success inserting " + group.getName());
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
 
    private boolean edit(Person person) {
        try {
 
            DirContext ctx = new InitialDirContext(env);
            ModificationItem[] mods = new ModificationItem[4];
            Attribute mod0 = new BasicAttribute("street", person.getAddress());
            Attribute mod1 = new BasicAttribute("userpassword", encryptLdapPassword("SHA", person.getPassword()));
            Attribute mod2 = new BasicAttribute("sn", person.getSurName());
            Attribute mod3 = new BasicAttribute("mail", person.getEmail());
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod1);
            mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod2);
            mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod3);
            ctx.modifyAttributes("uid=" + person.getName() + ",ou=User,ou=BusinessObjectsDisaster,ou=system", mods);
 
            logger.debug("success editing " + person.getName());
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
 
    private boolean delete(Person person) {
        try {
 
            DirContext ctx = new InitialDirContext(env);
            ctx.destroySubcontext("uid=" + person.getName() + config.getProperty("BoUser"));
            
            logger.debug("success deleting "+person.getName());
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
    
    private boolean deleteGroup(Group group) {
        try {
 
            // Construct the group name -- May not have to do this later - Tariq
            String groupName = group.getName() + group.getDisasterNumber() + group.getReportType();
            
            DirContext ctx = new InitialDirContext(env);
            ctx.destroySubcontext("cn=" + groupName + "," + config.getProperty("BoGroup"));
            
            logger.debug("success deleting " + groupName);
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
     
    private boolean search(Person person) {
        try {
 
            DirContext ctx = new InitialDirContext(env);
            //String base = "ou=users,ou=system";
            String base = "ou=User,ou=BusinessObjectsDisaster,ou=system";
 
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

//            results = ctx.search("ou=system,dc=example,dc=com", "(objectClass=posixGroup)",controls);
 
            String filter = "(&(objectclass=person)(uid="+person.getName()+"))";
 
            NamingEnumeration results = ctx.search(base, filter, sc);
 
            // Go through each item in list
//            while (results.hasMore()) {
//                SearchResult nc = (SearchResult)results.next();
//                Attributes att=     nc.getAttributes();                           
//                System.out.println("Group Name "+ att.get("cn").get(0));
//                System.out.println("GID "+ att.get("GidNumber").get(0));
//            }
            while (results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes attrs = sr.getAttributes();
 
                Attribute attr = attrs.get("uid");
                if(attr != null)
                    logger.debug("Record found : " + attr.get());
	                logger.debug("Group        : " + attrs.get("cn").get(0));
	                logger.debug("GID          : " + attrs.get("ou").get(0));
            }
            ctx.close();
                         
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
    
    
    private void searchUserInGroup() throws NamingException {	
    	
    	LDAPConnection connection = new LDAPConnection();
        try
        {
           connection.connect("localhost", 10389);
        }
        catch (LDAPException e)
        {
        // TODO Auto-generated catch block
           e.printStackTrace();
        }
    	
    	
    	String attributeName = "member";
        String assertionValue = "uid=awadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system";
        String entryDN = "cn=Business_Objects_Disaster_Users_1450da,ou=Group,ou=BusinessObjectsDisaster,ou=system";
        CompareRequest compareRequest = new CompareRequest(entryDN, attributeName, assertionValue);
        CompareResult compareResult = null;
        try
        {
           compareResult = connection.compare(compareRequest);
           if (compareResult.compareMatched())
           {
               System.out.println("The user: " + assertionValue + " is a member of the group: " + entryDN);
           }
           else
           {
               System.out.println("The user: " + assertionValue + " is NOT a member of the group: " + entryDN);
           }
        }
        catch (LDAPException e)
        {
        // TODO Auto-generated catch block
           e.printStackTrace();
        }
    }
    
    private void searchUserGroup() throws NamingException {

    	//Note the userdn here does not have the username - we have already binded to active directory.
    	//You'll need to change this string for your domain/structure.
//    	String userdn = "OU=Admin,OU=Groups,DC=uk,DC=BLAH,DC=com";
    	String userdn = config.getProperty("BoGroup"); // ou=Group,ou=BusinessObjectsDisaster,ou=system
    	
    	SearchControls searchCtrls = new SearchControls();
    	searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	String[] attributes = {"member","memberof"};
    	searchCtrls.setReturningAttributes(attributes);
    	
    	DirContext context = new InitialDirContext(env);

    	//Change the NameOfGroup for the group name you would like to retrieve the members of.
    	String filter = "(&(objectCategory=group)(cn=Business_Objects_Disaster_Users_1450ha))";

    	//use the context we created above and the filter to return all members of a group.
    	NamingEnumeration values = context.search( userdn, filter, searchCtrls);
    	
    	//Loop through the search results
    	while (values.hasMoreElements()) {
    		SearchResult sr = (SearchResult)values.next();
    		System.out.println(">>>" + sr.getName());
    		Attributes attrs = sr.getAttributes();

    		if (null != attrs)
    		{
    			for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements();)
    			{
    				Attribute atr = (Attribute) ae.next();
    				String attributeID = atr.getID();
    				Enumeration vals = atr.getAll();					

    				if (vals.hasMoreElements()) {
    					String username = (String) vals.nextElement();
    					logger.debug("Username: " +  username);

    				}
    			}
    		}
    		else {
    			logger.debug("No members for groups found");
    		}
    	}
    }
    
    private boolean isGroupContainUser(LDAPConnection ldapConnection, String groupDn, String userDn) throws LDAPException {
        boolean ret = false;
//        userDn = "uid=awadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system";
//        groupDn = "ou=Group,ou=BusinessObjectsDisaster,ou=system";
//        ldapConnection = new LDAPConnection();
//        try
//        {
//        	ldapConnection.connect("localhost", 10389);
//        }
//        catch (LDAPException e)
//        {
//        // TODO Auto-generated catch block
//           e.printStackTrace();
//        }
        Entry groupEntry = ldapConnection.getEntry(groupDn);

        //String[] memberValues = groupEntry.getAttributeValues("uniquemember");
        String[] memberValues = groupEntry.getAttributeValues("member");
        if (memberValues != null) {
            DN ldapUserDn = new DN(userDn);
            System.out.println("ldapUserDn : " + ldapUserDn.toString());
            for (String memberEntryDnString : memberValues) {
            	System.out.println("memberEntryDnString : " + memberEntryDnString);
                DN memberEntryDn = new DN(memberEntryDnString);
                System.out.println("memberEntryDn : " + memberEntryDn.toString());
                if (memberEntryDn.equals(ldapUserDn)) {
                	System.out.println("memberEntryDn.equals(ldapUserDn)");
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    
//    public SearchResultEntry getUserDetails(LDAPConnection connection, String username) {
//        try {
//  
//            SearchResult searchResults;
//            searchResults = connection.search(searchDn, SearchScope.SUB,
//                    "(uid=" + username + ")", "+");
//
//            if (searchResults.getEntryCount() == 1) {
//                SearchResultEntry entry = searchResults.getSearchEntries().get(
//                        0);
//                connection.close();
//                return entry;
//            } else {
//            	logger.error("NOT FOUND!");
//                connection.close();
//                return null;
//            }
//        } catch (LDAPException e) {
//        	logger.error("Exception");
//            return null;
//        }
//    }
    
    public void getUserGroup1() throws LDAPException {
    	LDAPConnection ldapConnection = new LDAPConnection();
    	try
    	{
    		ldapConnection.connect("localhost", 10389);
    	}
    	catch (LDAPException e)
    	{
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	//Entry groupEntry = ldapConnection.getEntry("ou=Group,ou=BusinessObjectsDisaster,ou=system");
    	Entry groupEntry = ldapConnection.getEntry("cn=Business_Objects_Disaster_Users_1450da,ou=Group,ou=BusinessObjectsDisaster,ou=system");
    	//Entry groupEntry = ldapConnection.getEntry("cn=My Group,ou=Groups,dc=example,dc=com");
    	String[] memberValues = groupEntry.getAttributeValues("member");
    	int entriesReturned = 0;
    	int exceptionsCaught = 0;
    	System.out.println("Here!!!");
    	if (memberValues != null)
    	{
    		System.out.println("Here!!!");
    		DNEntrySource entrySource = new DNEntrySource(ldapConnection, memberValues, "cn");
    		try
    		{
    			while (true)
    			{
    				Entry memberEntry;
    				try
    				{
    					memberEntry = entrySource.nextEntry();
    				}
    				catch (EntrySourceException ese)
    				{
    					// A problem was encountered while attempting to obtain an entry.
    					// We may be able to continue reading entries (e.g., if the problem
    					// was that the group referenced an entry that doesn't exist), or
    					// we may not (e.g., if the problem was a significant search error
    					// or problem with the connection).
    					exceptionsCaught++;
    					if (ese.mayContinueReading())
    					{
    						continue;
    					}
    					else
    					{
    						break;
    					}
    				}

    				if (memberEntry == null)
    				{
    					// We've retrieved all of the entries for the given set of DNs.
    					break;
    				}
    				else
    				{
    					entriesReturned++;
    				}
    			}
    		}
    		finally
    		{
    			entrySource.close();
    		}
    	}
    }
    
    public List<Entry> getUserGroup(String userDN) throws LDAPException, EntrySourceException {
    	
    	LDAPConnection ldapConnection = new LDAPConnection();
        try
        {
        	ldapConnection.connect("localhost", 10389);
        }
        catch (LDAPException e)
        {
        // TODO Auto-generated catch block
           e.printStackTrace();
        }
    	Entry userEntry = ldapConnection.getEntry(userDN);

        List<Entry> entryList = new ArrayList();
//        String[] memberValues = userEntry.getAttributeValues("memberOf"); For AD
        String[] memberValues = userEntry.getAttributeValues("member");
        if (memberValues != null) {
            DNEntrySource entrySource = new DNEntrySource(ldapConnection, memberValues);
            while (true) {
                Entry memberEntry = entrySource.nextEntry();
                if (memberEntry == null) {
                    break;
                }

                entryList.add(memberEntry);
            }
        }

        return entryList;
    }
    
    private boolean searchGroup(Person person) {
        try {
 
            DirContext ctx = new InitialDirContext(env);
            //String base = "ou=users,ou=system";
            String base = config.getProperty("BoGroup");
 
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

//            results = ctx.search("ou=system,dc=example,dc=com", "(objectClass=posixGroup)",controls);
            //String filter = "(&(objectclass=person)(uid="+person.getName()+"))";
//            String filter = "(&(objectCategory=group)(cn={0}))";
            String filter = "(&(objectCategory=group)(cn={0}))";
 
            NamingEnumeration results = ctx.search(base, filter, sc);
 
            // Go through each item in list
//            while (results.hasMore()) {
//                SearchResult nc = (SearchResult)results.next();
//                Attributes att=     nc.getAttributes();                           
//                System.out.println("Group Name "+ att.get("cn").get(0));
//                System.out.println("GID "+ att.get("GidNumber").get(0));
//            }
            while (results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes attrs = sr.getAttributes();
 
                Attribute attr = attrs.get("cn");
                if(attr != null)
                    logger.debug("Record found : " + attr.get());
	                logger.debug("Group        : " + attrs.get("cn").get(0));
	                logger.debug("street          : " + attrs.get("street").get(0));
	                logger.debug("objectClass          : " + attrs.get("objectclass").get(0));
	                logger.debug("objectClass          : " + attrs.get("objectclass").get(1));
	                logger.debug("objectClass          : " + attrs.get("objectclass").get(2));
	                logger.debug("objectClass          : " + attrs.get("objectclass").get(3));
            }
            ctx.close();
                         
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
 
    private String encryptLdapPassword(String algorithm, String _password) {
        String sEncrypted = _password;
        if ((_password != null) && (_password.length() > 0)) {
            boolean bMD5 = algorithm.equalsIgnoreCase("MD5");
            boolean bSHA = algorithm.equalsIgnoreCase("SHA")
                    || algorithm.equalsIgnoreCase("SHA1")
                    || algorithm.equalsIgnoreCase("SHA-1");
            if (bSHA || bMD5) {
                String sAlgorithm = "MD5";
                if (bSHA) {
                    sAlgorithm = "SHA";
                }
                try {
                    MessageDigest md = MessageDigest.getInstance(sAlgorithm);
                    md.update(_password.getBytes("UTF-8"));
                    sEncrypted = "{" + sAlgorithm + "}" + (new BASE64Encoder()).encode(md.digest());
                } catch (Exception e) {
                    sEncrypted = null;
                    logger.error(e, e);
                }
            }
        }
        return sEncrypted;
    }
 
    public static void main(String[] args) throws NamingException {
    	
    	BasicConfigurator.configure();
        LdapUtil main = new LdapUtil();

        Person person = new Person();
        person.setAddress("Chantilly");
        person.setName("tariqahsan");
        person.setSurName("Ahsan");
        person.setEmail("tahsan@newgentechnologies.com");
        person.setPassword("secret");
        person.setCreateTimestamp(new Date());
        
        // Group
        Group group = new Group();
        group.setName("Business_Objects_Disaster_Users_");
        group.setReportType("da");
        group.setDisasterNumber("1452");
 
        // insert
        //main.insert(person);
         
        // edit
        //main.edit(person);
         
        // select
        //main.search(person);
        
//        // Search Group
//        System.out.println("Now searching user groups ...");
//        main.searchUserGroup();
        
        // Search in Group
//        System.out.println("Now searching user in groups ...");
//        main.searchUserInGroup();
         
        // delete
        //main.delete(person);
        
        // Create Group
//        System.out.println("Creating a group ...");
//        main.createGroup(group);
        
        // Create Group
//        System.out.println("Removing a group ...");
//        main.deleteGroup(group);
        try {
            String userDn = "uid=awadsworth,ou=User,ou=BusinessObjectsDisaster,ou=system";
            String groupDn = "cn=Business_Objects_Disaster_Users_1450ha,ou=Group,ou=BusinessObjectsDisaster,ou=system";
            LDAPConnection ldapConnection = new LDAPConnection();
        	ldapConnection.connect("localhost", 10389);
        	System.out.println("Running isGroupContainUser ...");
			//main.isGroupContainUser(ldapConnection, groupDn, userDn);
        	main.getUserGroup1();
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    }
}
