package main.java.com.byl.yuce.dantuo;

import java.util.Date;

public class Fast3DanMa
{
  private String issueNumber;
  private Integer danmaOne;
  private Integer danmaTwo;
  private Date createTime;
  private char status;
  
  public String getIssueNumber()
  {
    return this.issueNumber;
  }
  
  public void setIssueNumber(String issueNumber)
  {
    this.issueNumber = issueNumber;
  }
  
  public Integer getDanmaOne()
  {
    return this.danmaOne;
  }
  
  public void setDanmaOne(Integer danmaOne)
  {
    this.danmaOne = danmaOne;
  }
  
  public Integer getDanmaTwo()
  {
    return this.danmaTwo;
  }
  
  public void setDanmaTwo(Integer danmaTwo)
  {
    this.danmaTwo = danmaTwo;
  }
  
  public Date getCreateTime()
  {
    return this.createTime;
  }
  
  public void setCreateTime(Date createTime)
  {
    this.createTime = createTime;
  }
  
  public char getStatus()
  {
    return this.status;
  }
  
  public void setStatus(char status)
  {
    this.status = status;
  }
}
