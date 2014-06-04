package org.mortbay.ijetty.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import android.util.Log;

public class PropertiesUtils {
	
        private String mVersion = "1";//属性文件的版本,默认1
        private String mConsoleVersion = "0";
	private String mPlayUrl = "";  //
	private String mPlayListVersion = "";
	private String mApkPushVersion = "";
	
	
	private String POP3Host = ""; // POP3服务器
	private int POP3SelectedIndex = 0;
	private String SMTPHost = ""; // SMTP服务器
	private int SMTPSelectedIndex = 0;
	private String userName = ""; // 登录服务器的帐号
	
	public String getVersion()
	{
	    return this.mVersion;
	}
	       
	public String getConsoleVersion()
	{
	    return this.mConsoleVersion;
	}
	
	public String setConsoleVersion()
	{
	    return this.mConsoleVersion;
	}
	
	public void setPlayUrl(String playUrl)
	{
		this.mPlayUrl = playUrl;
	}
	
	public String getPlayUrl()
	{
		return this.mPlayUrl;
	}
	
	public void setPlayListVersion(String playListVersion)
	{
	    this.mPlayListVersion = playListVersion;
	}
	
	public String getPlayListVersion()
	{
	    return this.mPlayListVersion;
	}
	
	public void setApkPushVersion(String apkPushVersion)
	{
	    this.mApkPushVersion = apkPushVersion;
	}
	
	public String getApkPushVersion()
	{
	    return this.mApkPushVersion;
	}
	
	public void setPop3Host(String pop3Host)
	{
		POP3Host = pop3Host;
	}
	
	public void setPop3SelectedIndex(int pop3SelectedIndex)
	{
		POP3SelectedIndex = pop3SelectedIndex;
	}
	
	public String getPop3Host()
	{
		return POP3Host;
	}
	
	public int getPop3SelectedIndex()
	{
		return POP3SelectedIndex;
	}
	
	public void setSmtpHost(String smtpHost)
	{
		SMTPHost = smtpHost;
	}
	
	public void setSmtpSelectedIndex(int smtpSelectedIndex)
	{
		SMTPSelectedIndex = smtpSelectedIndex;
	}
	
	public String getSmtpHost()
	{
		return SMTPHost;
	}
	
	public int getSmtpSelectedIndex()
	{
		return SMTPSelectedIndex;
	}
	
	public void setUserName(String user)
	{
		userName = user;
	}
	
	public String getUserName()
	{
		return userName;
	}
	
    //读取资源文件,并处理中文乱码
    public static void readPropertiesFile(String filename)
    {
        Properties properties = new Properties();
        try
        {
            InputStream inputStream = new FileInputStream(filename);
            properties.load(inputStream);
            inputStream.close(); //关闭流
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String username = properties.getProperty("username");
        String passsword = properties.getProperty("password");
        String chinese = properties.getProperty("chinese");
        try
        {
            chinese = new String(chinese.getBytes("ISO-8859-1"), "GBK"); // 处理中文乱码
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        System.out.println(username);
        System.out.println(passsword);
        System.out.println(chinese);
    }

    //读取XML文件,并处理中文乱码
    public void readPropertiesFileFromXML(String filename)
    {
        Properties properties = new Properties();
        try
        {
            InputStream inputStream = new FileInputStream(filename);
            properties.loadFromXML(inputStream);
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        mVersion = properties.getProperty("Version");
        mConsoleVersion = properties.getProperty("ConsoleVersion");
        mPlayUrl = properties.getProperty("PlayUrl");
        mPlayListVersion = properties.getProperty("PlayListVersion");        
    }

    //读取XML文件,并处理中文乱码
    public void readPropertiesFileFromXML(InputStream inputStream)
    {
        Properties properties = new Properties();
        try
        {
            //InputStream inputStream = new FileInputStream(filename);
            properties.loadFromXML(inputStream);
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        mVersion = properties.getProperty("Version");
        mConsoleVersion = properties.getProperty("ConsoleVersion");
        mPlayUrl = properties.getProperty("PlayUrl");
        mPlayListVersion = properties.getProperty("PlayListVersion");   
        mApkPushVersion = properties.getProperty("apkPushVersion"); 
    }
    
    //写资源文件，含中文
    public static void writePropertiesFile(String filename)
    {
        Properties properties = new Properties();
        try
        {
            OutputStream outputStream = new FileOutputStream(filename);
            properties.setProperty("username", "myname");
            properties.setProperty("password", "mypassword");
            properties.setProperty("chinese", "中文");
            properties.store(outputStream, "author: pengyixing@sina.com");
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //写资源文件到XML文件，含中文
    public void writePropertiesFileToXML(String filename)
    {
        Properties properties = new Properties();
        try
        {
            OutputStream outputStream = new FileOutputStream(filename);
            properties.setProperty("Version", mVersion);
            properties.setProperty("ConsoleVersion", mConsoleVersion);
            properties.setProperty("PlayUrl", mPlayUrl);
            properties.setProperty("PlayListVersion", mPlayListVersion);
            properties.setProperty("apkPushVersion", mApkPushVersion);
            properties.storeToXML(outputStream, "author: pengyixing@sina.com");
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
