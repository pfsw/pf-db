// ===========================================================================
// CONTENT  : CLASS UnitTestHelper
// AUTHOR   : Manfred Duchrow
// VERSION  : 1.0 - 13/04/2020
// HISTORY  :
//  13/04/2020  mdu  CREATED
//
// Copyright (c) 2020, by MDCS. All rights reserved.
// ===========================================================================
package org.pfsw.db.util.testhelper;

import java.io.File;

import javax.sql.DataSource;

import org.pfsw.db.util.DataSourceProxy;

public class UnitTestHelper
{
  public static final boolean DEBUG = "true".equals(System.getProperty("unittest.debug", "false"));
  
  public static final String DB_FILE_PATH = getTempFolder() + "/database/h2/unittest/pf-db";

  public static DataSource createNewDatabase1()
  {
    deleteH2Database(DB_FILE_PATH);
    return createDataSource1();
  }
  
  public static DataSource createDataSource1()
  {
    return new DataSourceProxy("jdbc:h2:file:" + DB_FILE_PATH, "sa", "");
  }
  
  public static void deleteH2Database(String databasePath)
  {
    deleteFile(databasePath + ".trace.db");
    deleteFile(databasePath + ".mv.db");
  }

  public static void deleteFile(String filePath)
  {
    File file;

    file = new File(filePath);
    if (file.exists())
    {
      debug("Delete file '%s' -> %s", filePath, file.delete());
    }
  }

  public static void sleep(long millis)
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public static String getTempFolder()
  {
    File file = new File("./build/tmp");
    file.mkdirs();
    return file.getAbsolutePath().replace('\\', '/');
  }
  
  public static void debug(String message, Object... args)
  {
    if (DEBUG)
    {      
      System.out.println(String.format(message, args));
    }
  }
}
