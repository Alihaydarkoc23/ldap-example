//package gov.fema.feim.dao.daoImpl;
package gov.fema.ldap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import gov.fema.feim.dao.LdapDao;
import gov.fema.feim.ws.dto.jaxb.UserBase;
import gov.fema.feim.ws.dto.jaxb.UserDetails;

public class LdapDaoImpl implements LdapDao {
	private Map<String, String> configuration;
	
	private void printAttributes(Attributes attributes) throws NamingException {
		for(NamingEnumeration ae = attributes.getAll(); ae.hasMore();) {
			Attribute attr = (Attribute)ae.next();
			System.out.println("Attr: " + attr.getID());
			for(NamingEnumeration e = attr.getAll(); e.hasMore();) {
				System.out.println("Value: " + e.next());
			}
		}
	}

	private Attributes buildSearchCriteria(Map<String, String> searchAttributes) {
		if(searchAttributes != null && searchAttributes.size() > 0) {
			BasicAttributes criteria = new BasicAttributes(true);
			Iterator<Entry<String, String>> it = searchAttributes.entrySet().iterator();
			Entry<String, String> searchKeyValue = null;
			while(it.hasNext()) {
				searchKeyValue = it.next();
				criteria.put(new BasicAttribute(searchKeyValue.getKey(), searchKeyValue.getValue()));
			}
			return criteria;
		}
		
		return null;
	}

	private String buildWildCardSearchCriteria(Map<String, String> searchAttributes) {
		StringBuilder criteria = new StringBuilder("(&(objectclass=femaPerson)");
		if(searchAttributes != null && searchAttributes.size() > 0) {
			Iterator<Entry<String, String>> it = searchAttributes.entrySet().iterator();
			Entry<String, String> searchKeyValue = null;
			while(it.hasNext()) {
				searchKeyValue = it.next();
				criteria.append("(").append(searchKeyValue.getKey()).append("=").append(searchKeyValue.getValue()).append(")");
			}
			criteria.append(")");
			
			System.out.println("*****criteria"+ criteria.toString());
			return criteria.toString();
		}
		return null;
	}

	
	@Override
	public DirContext getLdapContext() {
		  String ldapUsername = "uid=isim,ou=demo,ou=Users,DC=FEMA,DC=DHS,DC=NET";
	      String ldapPassword = "";
	         
	      Hashtable env = new Hashtable();
	      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	      //env.put(Context.PROVIDER_URL, "ldaps://hostname.femaeadis.com:636/");
	      //env.put(Context.SECURITY_PROTOCOL, "ssl");
		   
	      env.put(Context.PROVIDER_URL, "ldap://hostname.femaeadis.com:389/");
	      
	      env.put(Context.SECURITY_AUTHENTICATION, "simple");
	      env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
	      env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
	 	    
	      DirContext ctx = null;
	      try {
	    	  System.out.println("*****LDAP URL:"+ env.get(Context.PROVIDER_URL));
	         ctx = new InitialDirContext(env);
	      } catch (NamingException e) {
	         throw new RuntimeException(e);
	      }
	      
		return ctx;
	}

	@Override
	public List<UserDetails> attributeUserSearch(Map<String, String> searchAttributes) {
	      DirContext ctx = getLdapContext();

		NamingEnumeration results = null;
	      try {
	         SearchControls controls = new SearchControls();
	         Attributes criteria = buildSearchCriteria(searchAttributes);
	         controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	         results = ctx.search( "DC=FEMA,DC=DHS,DC=NET", criteria);

	         while (results.hasMore()) {
	            SearchResult searchResult = (SearchResult) results.next();
	            System.out.println(">>>" + searchResult.getName() );
	            printAttributes(searchResult.getAttributes());
	         }
	      } catch (NameNotFoundException e) {
	         e.printStackTrace();
	      } catch (NamingException e) {
		         e.printStackTrace();
	      } finally {
	         if (results != null) {
	            try {
	               results.close();
	            } catch (Exception e) {
	               // Never mind this.
	            }
	         }
	         if (ctx != null) {
	            try {
	               ctx.close();
	            } catch (Exception e) {
	               // Never mind this.
	            }
	         }
	      }
		return null;
	}

	@Override
	public List<UserDetails> wildCardUserSearch(Map<String, String> searchAttributes) {
		DirContext ctx = getLdapContext();

		NamingEnumeration results = null;
	      try {
	    	 List<UserDetails> userSearchResults = new ArrayList<UserDetails>();
	         SearchControls controls = new SearchControls();
	         String criteria = buildWildCardSearchCriteria(searchAttributes);
	         controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	         results = ctx.search( "DC=FEMA,DC=DHS,DC=NET", criteria, controls);

	         while (results.hasMore()) {
	            SearchResult searchResult = (SearchResult) results.next();
	            System.out.println(">>>" + searchResult.getName() );
	            printAttributes(searchResult.getAttributes());
	            buildUserDetail(searchResult.getAttributes(), userSearchResults);
	         }
	         return userSearchResults;
	      } catch (NameNotFoundException e) {
	         e.printStackTrace();
	      } catch (NamingException e) {
		         e.printStackTrace();
	      } finally {
	         if (results != null) {
	            try {
	               results.close();
	            } catch (Exception e) {
	               // Never mind this.
	            }
	         }
	         if (ctx != null) {
	            try {
	               ctx.close();
	            } catch (Exception e) {
	               // Never mind this.
	            }
	         }
	      }
		return null;
	}

	private void buildUserDetail(Attributes attributes, List<UserDetails> userSearchResults) throws NamingException {
		UserDetails user = new UserDetails();
		StringBuilder sb = new StringBuilder();
		
		for(NamingEnumeration ae = attributes.getAll(); ae.hasMore();) {
			Attribute attr = (Attribute)ae.next();
			System.out.println("Attr: " + attr.getID());
			String attrValue = getAttributeValue(attr);
			
			if(attr.getID().equalsIgnoreCase("telephonenumber")) {
				user.setBusinessPhone(attrValue);
			}
			else if(attr.getID().equalsIgnoreCase("uid")) {
				user.setUserName(attrValue);
			}
			else if(attr.getID().equalsIgnoreCase("givenname")) {
				user.setFirstName(attrValue);
			}
			else if(attr.getID().equalsIgnoreCase("sn")) {
				user.setLastName(attrValue);
			}
			else if(attr.getID().equalsIgnoreCase("initials")) {
				user.setMiddleName(attrValue);
			}
			else if(attr.getID().equalsIgnoreCase("mail")) {
				user.setEmail(attrValue);
			}
		}
		userSearchResults.add(user);
	}	

	private String getAttributeValue(Attribute attr) throws NamingException {
		StringBuilder sb = new StringBuilder();

		for(NamingEnumeration e = attr.getAll(); e.hasMore();) {
			if(sb.length() == 0) {
				sb.append(e.next());
			}
			else {
				sb.append("|").append(e.next());
			}
		}
		return sb.toString();
	}
    

	@PostConstruct
    private void loadLdapMapping() {
    	Properties configProps = new Properties();
    	InputStream in = null;
    	try {
    		in = this.getClass().getResourceAsStream("/properties/LdapAttributeMapping.properties"); 
			if( in != null) {
				System.out.println("Loaded properties");
				configProps.load(in);
				
				configuration = new Hashtable<String, String>(configProps.size());
				return ;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    		if(in != null) {
    			try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    }

    
}
