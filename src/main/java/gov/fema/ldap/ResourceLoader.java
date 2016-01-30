package gov.fema.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Java Program to demonstrate how to load resources e.g. properties file from
 * classpath. There are two ways to load resources in Java, one by using
 * getResourceAsStream() and getResource() method from java.lang.Class. Main
 * difference between these two methods are that one returns an InputStream
 * while other returns a URL object.
 *
 * @author Javin Paul
 */
public class ResourceLoader{

    public static void main(String args[]) {

        // loading resource using getResourceAsStream() method
        InputStream in = ResourceLoader.class.getResourceAsStream("/app.properties");

        Properties config = new Properties();
        try {
            config.load(in);
            System.out.println(config.getProperty("name"));
            System.out.println(config.getProperty("version"));

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // loading resource using getResource() method
//        URL resourceURL = Test.class.getResource("app.properties");
//        Properties appConfig = new Properties();
//        try {
//            appConfig.load(resourceURL.openStream());
//            System.out.println(appConfig.getProperty("name"));
//            System.out.println(appConfig.getProperty("version"));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

}

