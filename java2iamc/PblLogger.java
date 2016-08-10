package program;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class PblLogger
{

   static Logger baselogger = Logger.getRootLogger();

   static void changeLogLevel(Level iNewLogLevel)
   {
      baselogger.setLevel(iNewLogLevel);
      baselogger.info("the Loglevel was changed to " + iNewLogLevel.toString());
   }
   
   static void setupLogging(Level iLogLevel)
   {
      try
      {
         String layout = "%5p\t%5r\t%15.15c:\t%m\t%n";
         BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(layout)));
                  
         Date aNow = new Date();
         SimpleDateFormat aFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

         File  logFile = new File("log/PblDelivery_" + aFormat.format(aNow) + ".log");

         logFile.mkdirs();
         if (logFile.exists())
         {
            logFile.delete();
         }
         baselogger.addAppender(new FileAppender(new PatternLayout(layout), logFile.getAbsolutePath()));

         if (iLogLevel == null)
         {
            baselogger.setLevel(Level.DEBUG);   
         }
         else
         {
            baselogger.setLevel(iLogLevel);
         }
         
         
      }
      catch (Exception e)
      {
         System.err.println("Error creating logger " + e.getMessage());
         System.out.println("Error creating logger " + e.getMessage());
         System.exit(1);
      }
   }

}
