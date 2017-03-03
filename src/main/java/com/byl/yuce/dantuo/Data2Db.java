package main.java.com.byl.yuce.dantuo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import main.java.com.byl.yuce.App;
import main.java.com.byl.yuce.ConnectSrcDb;
import main.java.com.byl.yuce.LogUtil;
import main.java.com.byl.yuce.SrcDataBean;

import com.mysql.jdbc.PreparedStatement;

public class Data2Db
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
      LogUtil.error(e.getMessage(), "danma");
    }
    return flag;
  }
  
  public String findMaxIssueIdFromSrcDb()
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    String issueId = null;
    PreparedStatement pstmt = null;
    String sql = "SELECT max(issue_number) FROM " + App.srcNumberTbName;
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        issueId = rs.getString(1);
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      LogUtil.error(e.getMessage(), "danma");
    }
    return issueId;
  }
  
  public SrcDataBean getRecordByIssueCode(String issueCode)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    PreparedStatement pstmt = null;
    SrcDataBean srcDataBean = null;
    String sql = "SELECT issue_number,no1,no2,no3 FROM " + App.srcNumberTbName + " WHERE ISSUE_NUMBER = '" + issueCode + "'";
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
      LogUtil.error(e.getMessage(), "danma");
    }
    return srcDataBean;
  }
  
  public Fast3DanMa getYuceRecordByIssueCode(String issueCode)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    PreparedStatement pstmt = null;
    Fast3DanMa data = null;
    String sql = "SELECT ISSUE_NUMBER,DANMA_ONE,DANMA_TWO,CREATE_TIME FROM " + App.danMaTbName + " WHERE ISSUE_NUMBER = '" + issueCode + "'";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
      {
        data = new Fast3DanMa();
        data.setIssueNumber(rs.getString(1));
        data.setDanmaOne(Integer.valueOf(rs.getInt(2)));
        data.setDanmaTwo(Integer.valueOf(rs.getInt(3)));
        data.setCreateTime(rs.getDate(4));
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      LogUtil.error(e.getMessage(), "danma");
    }
    return data;
  }
  
  public List<SrcDataBean> getLast20Record(String issueCode)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    List<SrcDataBean> srcList = new ArrayList();
    PreparedStatement pstmt = null;
    String sql = "SELECT issue_number,no1,no2,no3 FROM " + App.srcNumberTbName + "  where issue_number like '%" + issueCode + "' order by issue_number desc limit 20 ";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
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
      LogUtil.error(e.getMessage(), "danma");
    }
    return srcList;
  }
  
  public List<Fast3Count> getTimesForNumber(List<SrcDataBean> noList)
  {
    List<Fast3Count> fast3CountList = new ArrayList();
    int[] arr7 = new int[6];
    int[] arr14 = new int[6];
    int[] arr20 = new int[6];
    int i = 0;
    for (SrcDataBean no : noList)
    {
      int[] temp = { no.getNo1(), no.getNo2(), no.getNo3() };
      Integer[] numIntArr = getUniqueArr(temp);
      if (i < 7) {
        for (int j = 0; j < numIntArr.length; j++) {
          arr7[(numIntArr[j].intValue() - 1)] += 1;
        }
      }
      if (i < 14) {
        for (int j = 0; j < numIntArr.length; j++) {
          arr14[(numIntArr[j].intValue() - 1)] += 1;
        }
      }
      if (i < 20) {
        for (int j = 0; j < numIntArr.length; j++) {
          arr20[(numIntArr[j].intValue() - 1)] += 1;
        }
      }
      i++;
    }
    for (int j = 0; j < 6; j++)
    {
      Fast3Count fast3Count = new Fast3Count();
      fast3Count.setNumber(Integer.valueOf(j + 1));
      fast3Count.setCount7(Integer.valueOf(arr7[j]));
      fast3Count.setCount14(Integer.valueOf(arr14[j]));
      fast3Count.setCount20(Integer.valueOf(arr20[j]));
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
  
  public void execDanMa(String issueNumber)
    throws SQLException
  {
    String issueCode = issueNumber.substring(issueNumber.length() - 2, issueNumber.length());
    List<SrcDataBean> noList = getLast20Record(issueCode);
    
    List<Fast3Count> fast3CountList = getTimesForNumber(noList);
    Collections.sort(fast3CountList);
    if (!hasRecordByIssueNumber(issueNumber, App.danMaTbName)) {
      insertData2Db(issueNumber, fast3CountList);
    }
  }
  
  private void insertData2Db(String issueNumber, List<Fast3Count> fast3CountList)
    throws SQLException
  {
    Connection conn = ConnectSrcDb.getSrcConnection();
    String sql = "insert into " + App.danMaTbName + " (issue_number,DANMA_ONE,DANMA_TWO,CREATE_TIME) values(?,?,?,?)";
    PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
    pstmt.setString(1, issueNumber);
    pstmt.setInt(2, ((Fast3Count)fast3CountList.get(0)).getNumber().intValue());
    pstmt.setInt(3, ((Fast3Count)fast3CountList.get(1)).getNumber().intValue());
    pstmt.setTimestamp(4, new Timestamp(new Date().getTime()));
    pstmt.executeUpdate();
  }
  
  public void execDrawnPrize(String issueCode)
    throws SQLException
  {
    SrcDataBean number = getRecordByIssueCode(issueCode);
    

    Fast3DanMa fast3DanMa = getYuceRecordByIssueCode(issueCode);
    if (fast3DanMa != null)
    {
      String status = judgeDownStatus(number, fast3DanMa);
      updateDanMaStatus(status, number, issueCode);
    }
  }
  
  private String judgeDownStatus(SrcDataBean number, Fast3DanMa fast3DanMa)
  {
    String status = null;
    int[] numArr = { number.getNo1(), number.getNo2(), number.getNo3() };
    int danmaOne = fast3DanMa.getDanmaOne().intValue();
    int danmaTwo = fast3DanMa.getDanmaTwo().intValue();
    String numStr = Arrays.toString(numArr);
    if (numStr.indexOf(Integer.toString(danmaOne)) > 0)
    {
      if (numStr.indexOf(Integer.toString(danmaTwo)) > 0) {
        status = "3";
      } else {
        status = "1";
      }
    }
    else if (numStr.indexOf(Integer.toString(danmaTwo)) > 0) {
      status = "2";
    } else {
      status = "0";
    }
    return status;
  }
  
  private void updateDanMaStatus(String status, SrcDataBean srcDataBean, String issueNumber)
    throws SQLException
  {
    Connection conn = ConnectSrcDb.getSrcConnection();
    String sql = "UPDATE " + App.danMaTbName + " SET status = ?,drown_number=?  where issue_number = ?";
    PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
    pstmt.setString(1, status);
    pstmt.setString(2, Integer.toString(srcDataBean.getNo1()) + Integer.toString(srcDataBean.getNo2()) + Integer.toString(srcDataBean.getNo3()));
    pstmt.setString(3, issueNumber);
    pstmt.executeUpdate();
  }
}
