package gov.fema.ldap;

import gov.fema.ldap.Person;
import java.security.MessageDigest;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;
 
/**
 *  gov.fema.ldap.LDAPMain
 *
 *  @author tariq ahsan
 */

public class LDAPMain {
 
    private Logger logger = Logger.getLogger(LDAPMain.class);
	//private Logger logger = LoggerFactory.getLogger(LDAPMain.class);
    private Hashtable<String, String> env = new Hashtable<String, String>();
 
    public LDAPMain() {
        try {
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
            env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
            env.put(Context.SECURITY_CREDENTIALS, "secret");
        } catch (Exception e) {
            logger.error(e, e);
        }
 
    }
 
    private boolean insert(Person person) {
        try {
 
            DirContext dctx = new InitialDirContext(env);
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute("uid", person.getName()));
            matchAttrs.put(new BasicAttribute("cn", person.getName()));
            matchAttrs.put(new BasicAttribute("street", person.getAddress()));
            matchAttrs.put(new BasicAttribute("sn", person.getSurName()));
            matchAttrs.put(new BasicAttribute("mail", person.getEmail()));
            matchAttrs.put(new BasicAttribute("userpassword", encryptLdapPassword("SHA", person.getPassword())));
            matchAttrs.put(new BasicAttribute("objectclass", "top"));
            matchAttrs.put(new BasicAttribute("objectclass", "person"));
            matchAttrs.put(new BasicAttribute("objectclass", "organizationalPerson"));
            matchAttrs.put(new BasicAttribute("objectclass", "inetorgperson"));
            //String name = "uid=" + person.getName() + ",ou=users,ou=system";
            String name = "uid=" + person.getName() + ",ou=User,ou=BusinessObjectsDisaster,ou=system";
            
            InitialDirContext iniDirContext = (InitialDirContext) dctx;
            iniDirContext.bind(name, dctx, matchAttrs);
 
            logger.debug("success inserting "+person.getName());
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
 
            logger.debug("success editing "+person.getName());
            return true;
        } catch (Exception e) {
            logger.error(e, e);
            return false;
        }
    }
 
    private boolean delete(Person person) {
        try {
 
            DirContext ctx = new InitialDirContext(env);
            ctx.destroySubcontext("uid=" + person.getName() + ",ou=User,ou=BusinessObjectsDisaster,ou=system");
            
            logger.debug("success deleting "+person.getName());
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
 
    public static void main(String[] args) {
    	
    	BasicConfigurator.configure();
        LDAPMain main = new LDAPMain();
 
        System.out.println("Now creating Person object...");
        Person person = new Person();
        person.setAddress("Reston");
        person.setName("tariqahsan");
        person.setSurName("Ahsan");
        person.setEmail("tahsan@newgentechnologies.com");
        person.setPassword("secret");
 
        // insert
        //main.insert(person);
         
        // edit
        //main.edit(person);
         
        // select
        main.search(person);
         
        // delete
        //main.delete(person);
    }
}
