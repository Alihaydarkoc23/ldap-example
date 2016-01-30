package gov.fema.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author srccodes.com
 * @version 1.0
 */
public class SLF4JHello {
    private final static Logger slf4jLogger = LoggerFactory.getLogger(SLF4JHello.class);

    /**
     * Print hello in log.
     * 
     * @param name
     */
    public void sayHello(String name) {
        slf4jLogger.info("Hi, {}", name);
        slf4jLogger.info("Welcome to the HelloWorld example of SLF4J");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
//        SLF4JHello slf4jHello = new SLF4JHello();
//        slf4jHello.sayHello("srccodes.com");
    	String name = "Tariq Ahsan";
        slf4jLogger.info("Hi, {}", name);
        slf4jLogger.info("Welcome to the HelloWorld example of SLF4J");
    }
}