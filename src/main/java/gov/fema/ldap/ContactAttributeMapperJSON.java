package gov.fema.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.ldap.core.AttributesMapper;


public class ContactAttributeMapperJSON implements AttributesMapper {

	public Object mapFromAttributes(Attributes attribute) throws NamingException {

		NamingEnumeration<String> ids = attribute.getIDs();
		JSONObject jo = new JSONObject();
		while(ids.hasMore()){
			String id = ids.next();
			try {
				jo.put(id, attribute.get(id).get());
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
		return jo.toString();
	
	}

}
