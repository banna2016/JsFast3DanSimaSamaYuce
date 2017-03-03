package main.java.com.byl.yuce.sima;

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
import main.java.com.byl.yuce.DateUtil;
import main.java.com.byl.yuce.LogUtil;
import main.java.com.byl.yuce.SrcDataBean;
import main.java.com.byl.yuce.dantuo.Fast3Count;

import com.mysql.jdbc.PreparedStatement;

public class Data2DbSima
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
  
  private String getIssueCodeByIssueNumber(String issueNumber)
  {
    if ((issueNumber != null) && (issueNumber.length() > 2)) {
      return issueNumber.substring(issueNumber.length() - 2, issueNumber.length());
    }
    LogUtil.error("»ñÈ¡ÆÚºÅ´íÎó£¡", "sima");
    return null;
  }
  
  public void execDrawnSima(String issueNumber)
    throws SQLException
  {
    SrcDataBean srcDataBean = getRecordByIssueNumber(issueNumber);
    
    Fast3SiMa fast3SiMa = getSiMaYuceRecordByIssueCode(issueNumber);
    if (fast3SiMa != null)
    {
      if (!issueNumber.equals(fast3SiMa.getDrownIssueNumber()))
      {
        String status = judgeDownStatus(srcDataBean, fast3SiMa);
        if (status == "1")
        {
          fast3SiMa.setDrownCycle(fast3SiMa.getDrownCycle() + 1);
          fast3SiMa.setDrownIssueNumber(issueNumber);
          fast3SiMa.setDrownNumber(Integer.toString(srcDataBean.getNo1()) + Integer.toString(srcDataBean.getNo2()) + Integer.toString(srcDataBean.getNo3()));
          fast3SiMa.setStatus(status);
          
          updateDanMaStatus(fast3SiMa);
          
          execSima(issueNumber, fast3SiMa);
        }
        else
        {
          fast3SiMa.setDrownCycle(fast3SiMa.getDrownCycle() + 1);
          fast3SiMa.setDrownIssueNumber(issueNumber);
          fast3SiMa.setDrownNumber(Integer.toString(srcDataBean.getNo1()) + Integer.toString(srcDataBean.getNo2()) + Integer.toString(srcDataBean.getNo3()));
          fast3SiMa.setStatus(status);
          updateDanMaStatus(fast3SiMa);
          if (fast3SiMa.getDrownCycle() == 3) {
            execSima(issueNumber, fast3SiMa);
          }
        }
      }
    }
    else {
      execSima(issueNumber, fast3SiMa);
    }
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
      LogUtil.error(e.getMessage(), "sima");
    }
    return srcDataBean;
  }
  
  public Fast3SiMa getSiMaYuceRecordByIssueCode(String issueNumber)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    PreparedStatement pstmt = null;
    Fast3SiMa data = null;
    String sql = "SELECT ID,YUCE_ISSUE_START,YUCE_ISSUE_STOP,DROWN_PLAN,DROWN_CYCLE,DROWN_ISSUE_NUMBER  FROM " + App.simaTbName + " WHERE " + issueNumber + " BETWEEN YUCE_ISSUE_START AND YUCE_ISSUE_STOP   ORDER BY ID DESC LIMIT 1";
    try
    {
      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
      {
        data = new Fast3SiMa();
        data.setId(rs.getInt(1));
        data.setYuceIssueStart(rs.getString(2));
        data.setYuceIssueStop(rs.getString(3));
        data.setDrownPlan(rs.getString(4));
        data.setDrownCycle(rs.getInt(5));
        data.setDrownIssueNumber(rs.getString(6));
      }
      if ((rs != null) && (!rs.isClosed())) {
        rs.close();
      }
    }
    catch (SQLException e)
    {
      LogUtil.error(e.getMessage(), "sima");
    }
    return data;
  }
  
  public List<SrcDataBean> getYucePool(String issueCode)
  {
    Connection srcConn = ConnectSrcDb.getSrcConnection();
    List<SrcDataBean> srcList = new ArrayList();
    PreparedStatement pstmt = null;
    String startDay = DateUtil.getNextNDay(-7);
    
    String endDay = DateUtil.getNextNDay(-1);
    
    String code1 = DateUtil.getNextIssueCodeByCurrentIssue(issueCode);
    String code2 = DateUtil.getNextIssueCodeByCurrentIssue(code1);
    String code3 = DateUtil.getNextIssueCodeByCurrentIssue(code2);
    String sql = "SELECT issue_number,no1,no2,no3 FROM " + App.srcNumberTbName + " WHERE substr(ISSUE_NUMBER,1,6) between '" + startDay + "' and '" + endDay + "' AND substr(ISSUE_NUMBER,8) IN ('" + code1 + "','" + code2 + "','" + code3 + "') ORDER BY ISSUE_NUMBER DESC";
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
      e.printStackTrace();
      LogUtil.error(e.getMessage(), "sima/");
    }
    return srcList;
  }
  
  public List<Fast3Count> getTimesForNumber(List<SrcDataBean> noList)
  {
    List<Fast3Count> fast3CountList = new ArrayList();
    int[] arr3 = new int[6];
    int[] arr5 = new int[6];
    int[] arr7 = new int[6];
    int i = 0;
    for (SrcDataBean no : noList)
    {
      int[] temp = { no.getNo1(), no.getNo2(), no.getNo3() };
      Integer[] numIntArr = getUniqueArr(temp);
      if (i < 9) {
        for (int j = 0; j < numIntArr.length; j++) {
          arr3[(numIntArr[j].intValue() - 1)] += 1;
        }
      }
      if (i < 15) {
        for (int j = 0; j < numIntArr.length; j++) {
          arr5[(numIntArr[j].intValue() - 1)] += 1;
        }
      }
      if (i < 21) {
        for (int j = 0; j < numIntArr.length; j++) {
          arr7[(numIntArr[j].intValue() - 1)] += 1;
        }
      }
      i++;
    }
    for (int j = 0; j < 6; j++)
    {
      Fast3Count fast3Count = new Fast3Count();
      fast3Count.setNumber(Integer.valueOf(j + 1));
      fast3Count.setCount7(Integer.valueOf(arr3[j]));
      fast3Count.setCount14(Integer.valueOf(arr5[j]));
      fast3Count.setCount20(Integer.valueOf(arr7[j]));
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
  
  public void execSima(String issueNumber, Fast3SiMa fast3SiMa)
    throws SQLException
  {
    List<SrcDataBean> noList = getYucePool(issueNumber.substring(issueNumber.length() - 2, issueNumber.length()));
    
    List<Fast3Count> fast3CountList = getTimesForNumber(noList);
    Collections.sort(fast3CountList);
    if ((fast3SiMa == null) || (fast3SiMa.getDrownCycle() > 0)) {
      insertData2Db(issueNumber, fast3CountList);
    }
  }
  
  private void insertData2Db(String issueNumber, List<Fast3Count> fast3CountList)
    throws SQLException
  {
    Connection conn = ConnectSrcDb.getSrcConnection();
    String sql = "insert into " + App.simaTbName + " (YUCE_ISSUE_START,YUCE_ISSUE_STOP,DROWN_PLAN,CREATE_TIME) values(?,?,?,?)";
    String code1 = App.getNextIssueByCurrentIssue(issueNumber);
    String code2 = App.getNextIssueByCurrentIssue(code1);
    String code3 = App.getNextIssueByCurrentIssue(code2);
    int[] numArr = { ((Fast3Count)fast3CountList.get(0)).getNumber().intValue(), ((Fast3Count)fast3CountList.get(1)).getNumber().intValue(), ((Fast3Count)fast3CountList.get(2)).getNumber().intValue(), ((Fast3Count)fast3CountList.get(3)).getNumber().intValue() };
    Arrays.sort(numArr);
    PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
    pstmt.setString(1, code1);
    pstmt.setString(2, code3);
    pstmt.setString(3, Integer.toString(numArr[0]) + Integer.toString(numArr[1]) + Integer.toString(numArr[2]) + Integer.toString(numArr[3]));
    pstmt.setTimestamp(4, new Timestamp(new Date().getTime()));
    pstmt.executeUpdate();
  }
  
  private String judgeDownStatus(SrcDataBean number, Fast3SiMa fast3SiMa)
  {
    String status = "1";
    int[] numArr = { number.getNo1(), number.getNo2(), number.getNo3() };
    String drownPlan = fast3SiMa.getDrownPlan();
    for (int i = 0; i < numArr.length; i++) {
      if (drownPlan.indexOf(Integer.toString(numArr[i])) < 0)
      {
        status = "0";
        break;
      }
    }
    return status;
  }
  
  private void updateDanMaStatus(Fast3SiMa fast3SiMa)
    throws SQLException
  {
    Connection conn = ConnectSrcDb.getSrcConnection();
    String sql = "UPDATE " + App.simaTbName + " SET DROWN_ISSUE_NUMBER=?,DROWN_NUMBER=?,status = ?,DROWN_CYCLE=?  where ID = ?";
    
    PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
    pstmt.setString(1, fast3SiMa.getDrownIssueNumber());
    pstmt.setString(2, fast3SiMa.getDrownNumber());
    pstmt.setString(3, fast3SiMa.getStatus());
    pstmt.setInt(4, fast3SiMa.getDrownCycle());
    pstmt.setInt(5, fast3SiMa.getId());
    pstmt.executeUpdate();
  }
}
