package main.java.com.byl.yuce;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import main.java.com.byl.yuce.dantuo.Data2Db;
import main.java.com.byl.yuce.samenumber.Data2DbSameNumber;
import main.java.com.byl.yuce.sima.Data2DbSima;

public class App
{
  static Connection conn = null;
  static String maxIssueId = "";
  public static String lineCount = "0";
  public static String srcNumberTbName = null;
  public static String danMaTbName = null;
  public static String simaTbName = null;
  public static String sameNumTbName = null;
  public static String province = null;
  
  private static void initParam()
  {
    Properties p = new Properties();
    InputStream is = App.class.getClassLoader().getResourceAsStream("db.properties");
    try
    {
      p.load(is);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    lineCount = p.getProperty("lineCount", "79");
    srcNumberTbName = p.getProperty("srcNumberTbName");
    danMaTbName = p.getProperty("danMaTbName");
    simaTbName = p.getProperty("simaTbName");
    sameNumTbName = p.getProperty("sameNumTbName");
    province = p.getProperty("province");
  }
  
  public static void main(String[] args)
  {
    initParam();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}, new Date(), 20000L);
  }
  
  private static void execData()
  {
    Data2Db data2Db = new Data2Db();
    String maxIssueNumber = null;
    try
    {
      maxIssueNumber = data2Db.findMaxIssueIdFromSrcDb();
      if (!maxIssueId.equals(maxIssueNumber))
      {
        maxIssueId = maxIssueNumber;
        startDanMa(maxIssueId);
        
        startSiMa(maxIssueId);
        
        startSameNumber(maxIssueId);
      }
    }
    catch (Exception e)
    {
      String info = getErrorInfoFromException(e);
      LogUtil.error(maxIssueNumber + "‘§≤‚ ß∞‹£°" + info, province + "/danma");
    }
  }
  
  private static void startDanMa(String issueNumber)
  {
    try
    {
      Data2Db data2Db = new Data2Db();
      

      data2Db.execDrawnPrize(issueNumber);
      
      String nextIssueNumber = getNextIssueByCurrentIssue(issueNumber);
      data2Db.execDanMa(nextIssueNumber);
    }
    catch (SQLException sqlEx)
    {
      sqlEx.printStackTrace();
      LogUtil.error(issueNumber + "‘§≤‚ ß∞‹£°" + sqlEx.getMessage(), province + "/danma");
    }
  }
  
  public static String getErrorInfoFromException(Exception e)
  {
    try
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return "\r\n" + sw.toString() + "\r\n";
    }
    catch (Exception e2) {}
    return "bad getErrorInfoFromException";
  }
  
  public static String getNextIssueByCurrentIssue(String issueNumber)
  {
    String issueCode = issueNumber.substring(issueNumber.length() - 2, issueNumber.length());
    int issue = Integer.parseInt(issueCode);
    int nextIssue = (issue + 1) % Integer.parseInt(lineCount);
    if (nextIssue > 9) {
      return issueNumber.substring(0, issueNumber.length() - 2) + nextIssue;
    }
    if (nextIssue == 0) {
      return issueNumber.substring(0, issueNumber.length() - 2) + lineCount;
    }
    if (nextIssue == 1) {
      return DateUtil.getNextDay(issueNumber.substring(0, issueNumber.length() - 3)) + "001";
    }
    return issueNumber.substring(0, issueNumber.length() - 2) + "0" + nextIssue;
  }
  
  public static void startSiMa(String issueNumber)
  {
    try
    {
      Data2DbSima data2DbSima = new Data2DbSima();
      data2DbSima.execDrawnSima(issueNumber);
    }
    catch (SQLException sqlEx)
    {
      LogUtil.error(issueNumber + "‘§≤‚ ß∞‹£°", province + "/sima");
    }
  }
  
  public static void startSameNumber(String issueNumber)
  {
    try
    {
      Data2DbSameNumber data2DbSameNumber = new Data2DbSameNumber();
      data2DbSameNumber.execSameNum(issueNumber);
    }
    catch (SQLException sqlEx)
    {
      sqlEx.printStackTrace();
      LogUtil.error(issueNumber + "‘§≤‚ ß∞‹£°", province + "/same");
    }
  }
}
