package main.java.com.byl.yuce.samenumber;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import main.java.com.byl.yuce.App;
import main.java.com.byl.yuce.ConnectSrcDb;
import main.java.com.byl.yuce.DateUtil;
import main.java.com.byl.yuce.LogUtil;
import main.java.com.byl.yuce.SrcDataBean;

import com.mysql.jdbc.PreparedStatement;

public class Data2DbSameNumber
{
  public boolean hasRecordByIssueNumber(String issueNumber, String tbName)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    boolean flag = false;
    int count = 0;
    PreparedStatement pstmt = null;
    String sql = "SELECT count(*) count FROM " + tbName + " where issue_number = '" + issueNumber + "'";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        count = rs.getInt(1);
      }
      if (count > 0) {
        flag = true;
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      LogUtil.error(e.getMessage(), "sima");
    }
    return flag;
  }
  
  public SrcDataBean getRecordByIssueNumber(String issueNumber)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    PreparedStatement pstmt = null;
    SrcDataBean srcDataBean = null;
    String sql = "SELECT issue_number,no1,no2,no3 FROM " + App.srcNumberTbName + " WHERE ISSUE_NUMBER = '" + issueNumber + "'";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
      {
        srcDataBean = new SrcDataBean();
        srcDataBean.setIssueId(rs.getString(1));
        srcDataBean.setNo1(rs.getInt(2));
        srcDataBean.setNo2(rs.getInt(3));
        srcDataBean.setNo3(rs.getInt(4));
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      LogUtil.error(e.getMessage(), "same/");
    }
    return srcDataBean;
  }
  
  private List<String> getSameNumIssue(SrcDataBean srcDataBean)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    List<String> srcList = new ArrayList();
    PreparedStatement pstmt = null;
    
    String getIssueList = "SELECT ISSUE_NUMBER FROM " + App.srcNumberTbName + " WHERE no1=? and no2=? and no3=? ORDER BY ISSUE_NUMBER DESC LIMIT 10 ";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(getIssueList);
      pstmt.setInt(1, srcDataBean.getNo1());
      pstmt.setInt(2, srcDataBean.getNo2());
      pstmt.setInt(3, srcDataBean.getNo3());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
      {
        String srcBean = rs.getString(1);
        srcList.add(srcBean);
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      LogUtil.error(e.getMessage(), "same/");
    }
    return srcList;
  }
  
  private List<SrcDataBean> getNextIssueRecordList(List<String> nextIssue)
  {
    List<SrcDataBean> srcList = new ArrayList();
    srcList = new ArrayList();
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    PreparedStatement pstmt = null;
    
    String getIssueList = "SELECT ISSUE_NUMBER,NO1,NO2,NO3 FROM  " + App.srcNumberTbName + " WHERE issue_number IN (" + DateUtil.listToString(nextIssue) + ") ORDER BY ISSUE_NUMBER DESC";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(getIssueList);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
      {
        SrcDataBean srcDataBean = new SrcDataBean();
        srcDataBean.setIssueId(rs.getString(1));
        srcDataBean.setNo1(rs.getInt(2));
        srcDataBean.setNo2(rs.getInt(3));
        srcDataBean.setNo3(rs.getInt(4));
        srcList.add(srcDataBean);
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      LogUtil.error(e.getMessage(), "same/");
    }
    return srcList;
  }
  
  public List<Fast3SameNumber> getTimesForNumber(List<SrcDataBean> noList)
  {
    List<Fast3SameNumber> fast3CountList = new ArrayList();
    int[] arr10 = new int[6];
    /*for (Iterator localIterator = noList.iterator(); localIterator.hasNext(); j < numIntArr.length)
    {
      SrcDataBean no = (SrcDataBean)localIterator.next();
      int[] temp = { no.getNo1(), no.getNo2(), no.getNo3() };
      numIntArr = getUniqueArr(temp);
      j = 0; continue;
      arr10[(numIntArr[j].intValue() - 1)] += 1;j++;
    }
    for (int j = 0; j < 6; j++)
    {
      Fast3SameNumber fast3Count = new Fast3SameNumber();
      fast3Count.setNumber(Integer.valueOf(j + 1));
      fast3Count.setCount10(Integer.valueOf(arr10[j]));
      fast3CountList.add(fast3Count);
    }*/
    for (SrcDataBean no : noList)
    {
      int[] temp = { no.getNo1(), no.getNo2(), no.getNo3() };
      for (int j = 0; j < temp.length; j++) //循环开奖号码
      {
        arr10[(temp[j] - 1)] += 1;//计算每个开奖号码出现的次数
      }
    }
    for (int j = 0; j < 6; j++)
    {
      Fast3SameNumber fast3Count = new Fast3SameNumber();
      fast3Count.setNumber(Integer.valueOf(j + 1));
      fast3Count.setCount10(Integer.valueOf(arr10[j]));
      fast3CountList.add(fast3Count);
    }
    return fast3CountList;
  }
  
  private Integer[] getUniqueArr(int[] a)
  {
    List<Integer> list = new LinkedList();
    for (int i = 0; i < a.length; i++) {
      if (!list.contains(Integer.valueOf(a[i]))) {
        list.add(Integer.valueOf(a[i]));
      }
    }
    return (Integer[])list.toArray(new Integer[list.size()]);
  }
  
  public void execSameNum(String issueNumber)
    throws SQLException
  {
    SrcDataBean srcDataBean = getRecordByIssueNumber(issueNumber);
    
    List<String> issueList = getSameNumIssue(srcDataBean);
    if ((issueList != null) && (issueList.size() > 0))
    {
      List<String> nextIssueList = new ArrayList();
      for (String issue : issueList)
      {
        String nextIssue = App.getNextIssueByCurrentIssue(issue);
        nextIssueList.add(nextIssue);
      }
      List<SrcDataBean> noList = getNextIssueRecordList(nextIssueList);
      
      Object fast3CountList = getTimesForNumber(noList);
      
      Collections.sort((List)fast3CountList);
      SrcDataBean param = new SrcDataBean();
      param.setIssueId((String)nextIssueList.get(0));
      noList.add(0, param);
      if (issueList.size() == 10) {
        insertData2Db(issueList, noList, (List)fast3CountList);
      }
    }
  }
  
  private void insertData2Db(List<String> issueList, List<SrcDataBean> noList, List<Fast3SameNumber> fast3CountList)
    throws SQLException
  {
    Connection conn = ConnectSrcDb.getSrcConnection();
    
    String truncateTb = "TRUNCATE TABLE " + App.sameNumTbName;
    String sql = "insert into " + App.sameNumTbName + " (CURRENT_ISSUE,LOTTORY_NUMBER,NEXT_ISSUE,NEXT_LOTTORY_NUMBER,CREATE_TIME) values(?,?,?,?,?)";
    
    SrcDataBean currentRecord = getRecordByIssueNumber((String)issueList.get(0));
    conn.setAutoCommit(false);
    PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
    pstmt.addBatch(truncateTb);
    for (int i = noList.size() - 1; i >= 0; i--)
    {
      if (i == 0) {
        pstmt.setString(4, Integer.toString(((Fast3SameNumber)fast3CountList.get(0)).getNumber().intValue()) + Integer.toString(((Fast3SameNumber)fast3CountList.get(1)).getNumber().intValue()) + Integer.toString(((Fast3SameNumber)fast3CountList.get(4)).getNumber().intValue()) + Integer.toString(((Fast3SameNumber)fast3CountList.get(5)).getNumber().intValue()));
      } else {
        pstmt.setString(4, Integer.toString(((SrcDataBean)noList.get(i)).getNo1()) + Integer.toString(((SrcDataBean)noList.get(i)).getNo2()) + Integer.toString(((SrcDataBean)noList.get(i)).getNo3()));
      }
      pstmt.setString(1, (String)issueList.get(i));
      pstmt.setString(2, Integer.toString(currentRecord.getNo1()) + Integer.toString(currentRecord.getNo2()) + Integer.toString(currentRecord.getNo3()));
      pstmt.setString(3, ((SrcDataBean)noList.get(i)).getIssueId());
      pstmt.setTimestamp(5, new Timestamp(new Date().getTime()));
      pstmt.addBatch();
    }
    pstmt.executeBatch();
    conn.commit();
    pstmt.clearBatch();
    conn.setAutoCommit(true);
  }
}
