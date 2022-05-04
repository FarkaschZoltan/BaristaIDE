package com.farkasch.barista.util;

import com.farkasch.barista.util.enums.ResultTypeEnum;
import java.io.File;

public class Result {
  private String message;
  private ResultTypeEnum result;
  private File logFile;

  private Result(String message, ResultTypeEnum result, File logFile){
    this.message = message;
    this.result = result;
    this.logFile = logFile;
  }

  public String getMessage() {
    return message;
  }


  public ResultTypeEnum getResult() {
    return result;
  }


  public File getLogFile() {
    return logFile;
  }


  public static Result OK(){
    return OK("All correct!");
  }

  public static Result OK(String message){
    return new Result(message, ResultTypeEnum.OK, null);
  }

  public static Result FAIL(){
    return FAIL("Something went wrong!");
  }

  public static Result FAIL(String message){
    return new Result(message, ResultTypeEnum.FAIL, null);
  }

  public static Result ERROR(){
    return ERROR("An error has occurred!");
  }

  public static Result ERROR(String message){
    return ERROR(message, null);
  }

  public static Result ERROR(String message, File errorLog){
    return new Result(message, ResultTypeEnum.ERROR, errorLog);
  }
}
