package org.exoplatform.commons.utils;

import org.jgroups.util.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by exo on 6/30/17.
 */
public class RDBMSMigrationUtils {
  private static ExecutorService executorService = Executors.newSingleThreadExecutor(new DefaultThreadFactory("COMMONS-RDBMS-MIGRATION", false, false));;

  public static ExecutorService getExecutorService() {
    return executorService;
  }
}
