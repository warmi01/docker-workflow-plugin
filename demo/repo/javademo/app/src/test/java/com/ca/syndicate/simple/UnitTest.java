package com.ca.syndicate.servlet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

import com.ca.syndicate.example.DeviceSensor;


/**
 * Unit test for simple App.
 */
public class UnitTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UnitTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UnitTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

   
    public void testAlert() {

        Assert.assertEquals(SimpleServlet.getAlerts(), "alerts: 1");

    }

    public void testStatus() {

      SimpleServlet example = new SimpleServlet();

      Assert.assertEquals(example.getStatus(), "OK");

    }

    public void testDeviceSensor() {

      DeviceSensor example = new DeviceSensor();

      Assert.assertEquals(example.scan("d1", "floor1"), "d1 at floor1 is running. ");

    }


}