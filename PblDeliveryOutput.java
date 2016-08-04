package program;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import mdio.CConnector;
import mdio.CConnectorException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import data.Mapping;
import data.RegionType;

/**
 * This is the main class. It has a responsibility to set up the logger, process
 * the command line and initiate the processing.
 * 
 * @author Vortech
 * 
 */
public class PblDeliveryOutput
{

   int           xoffset = 0;
   // column of the cell with 'VARIABLE' in it
   int           yoffset = 0;
   // row of the cell with 'VARIBLE' in it

   static Logger logger  = Logger.getLogger(PblDeliveryOutput.class.getName());

   Mapping       _mapping;

   public PblDeliveryOutput()
   {
      _mapping = new Mapping();
   }

   public static final String                 kMAPPINGSHEETNAME = "data";

   protected String                           _scenarioBasePath = "";
   protected String                           _templateFile     = "";
   protected String                           _mappingFile      = "";
   protected String                           _outputName       = "";
   protected String                           _outputFormat     = "excel";
   protected String                           _numberFormat     = "decimal";
   protected ArrayList<String>                _scenario         = new ArrayList<String>();
   protected ArrayList<RegionType.RegionName> _reportRegions    = new ArrayList<RegionType.RegionName>(); ;

   private void ProcessRegios(String iRegio) throws IllegalArgumentException
   {

      _reportRegions.add(RegionType.RegionName.valueOf(iRegio));
   }

   private void processDelivery() throws Exception
   {
      try
      {
         readMapping();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         logger.error("Unable to process mapping file (is the filename correct?) ");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         logger.error("Unable to process mapping file (the document probably contains an error)");
      }

      Engine aReportCreator = new Engine(_templateFile, _scenario, _scenarioBasePath, _outputName, _mapping,
            _outputFormat, _numberFormat, _reportRegions);
      aReportCreator.doWork();
   }

   private void readMapping() throws FileNotFoundException, IOException, Exception
   {

      FileInputStream fis = new FileInputStream(_mappingFile);
      Workbook aWorkBook = WorkbookFactory.create(fis);
      Sheet aSheet=null;
      aSheet = aWorkBook.getSheet(kMAPPINGSHEETNAME);

      logger.info("looking for VARIABLE keyword in mapping file");
      findVariableKeywordCell(aSheet);
      logger.info("got VARIABLE keyword in mapping file");

      int aRowVariable = 1;

      while (aRowVariable + yoffset != aSheet.getLastRowNum())
      {
         try
         {
            aSheet.getRow(aRowVariable + yoffset).getCell(xoffset).getStringCellValue();
         }
         catch (NullPointerException e)
         {
            break;
         }
         logger.info("Adding row (Excel) " + (aRowVariable + yoffset + 1) + " from mapping sheet");
         _mapping.addMappingFromRowSheet(aSheet.getRow(aRowVariable + yoffset), xoffset);
         aRowVariable++;

      }
      logger.debug(_mapping.toString());
   }

   /*
	 * 
	 */
   private void findVariableKeywordCell(Sheet iSheet) throws Exception
   {
      for (int i = 0; i < 15; i++)
      {
         Row aRow = iSheet.getRow(i);
         for (int j = 0; j < 15 && j < aRow.getLastCellNum(); j++)
         {
            if (aRow.getCell(j).getStringCellValue().equals("VARIABLE"))
            {
               xoffset = j;
               yoffset = i;
               return;
            }
         }
      }

      throw new Exception("Could not find cell 'VARIABLE' in the mapping file: " + _mappingFile);
   }

   public static void readManifest() throws IOException
   {
      Enumeration<URL> resources = PblDeliveryOutput.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements())
      {
         try
         {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            // check that this is your manifest and do what you need or get the
            // next one
            Attributes b = manifest.getMainAttributes();
            if (b.getValue("Build-time") != null)
            {
               String aMsg = "Your build is: " + b.getValue("Build-time");
               logger.info(aMsg);
               System.out.println(aMsg);
            }
         }
         catch (Exception e)
         {
            // handle
         }
      }
   }

   /**
    * 
    * @param args
    *           -t template.xls -m mapping.xls -b scenarioBasedir -s scenario1
    *           -s scenario2 -r Regio1 -r Regio2
    */
   public static void main(String[] args)
   {
      java.util.Date aDate = new java.util.Date();
      long aStart = aDate.getTime();
      PblLogger.setupLogging(Level.ALL);

      try
      {
         readManifest();
      }
      catch (IOException e)
      {

      }

      PblDeliveryOutput aDelivery = new PblDeliveryOutput();
      boolean aCommandLineOk = true;

      // test if the native MDIO dll will function properly
      CConnector a = new CConnector();
      try
      {
         a.connect();
      }
      catch (CConnectorException e)
      {
         String aMsg = "There is no connection with the native dll (for M-file reading).";
         System.err.println(aMsg);
         logger.fatal(aMsg);
      }

      if (args.length >= 8)
      {
         aCommandLineOk = aDelivery.processArguments(args);
         if (aCommandLineOk)
         {
            try
            {
               aDelivery.processDelivery();
               logger.info("Reached the end of the program.");
            }
            catch (Exception e)
            {
               e.printStackTrace();
               logger.fatal(e.getMessage());
               logger.fatal("An exception prevented the program from finishing normally.");
            }
         }
      }
      else
      {
         aCommandLineOk = false;
      }

      if (!aCommandLineOk)
      {
         String aUsage = "PblDeliveryTool creates an Excel report from MyM data and Excel formatting sheets\r\n"
               + "\r\n"
               + "Usage: PblDeliveryTool <outputfile> <loglevel> <template> <mapping> <scenario base path> <scenarios> [regions]\r\n"
               + "with:\t<outputfile>\t '-f myfile.xls' Name and path where the generated Excel output file should be created\r\n"
               + "\t<loglevel>\t '-l WARN' One of ALL, FATAL, ERROR, WARN, INFO. All messages that have less importance than the specified level are nog logged.\r\n"
               + "\t<template>\t '-t myTemplate.xls' The template file contains the subset of the mapping to report and the years to report on (Excel file).\r\n"
               + "\t<mapping>\t '-m myMapping.xls' The mapping file defines a variable that can be reported and provides information its dimensions (Excel file).\r\n"
               + "\t<scenario base path> '-b C:\\path\\to\\scenario_' The path that is put in front of the scenario's, note that no additional slashes are added when appending the scenario to it.\r\n"
               + "\t<scenarios>\t'-s scenario [-s scenario2]' The scenario's (collection of MyM files) that should be part of the report, multiple scenario's can be provided.\r\n"
               + "\t[regions]\t '-r World -r CAN' Filter to put only the selected regions in the report, provide no regions to include all regions.\r\n"
               + "\t <outputformat>\t '-of csv|excel Format of outputfile. Excel is the default.\r\n"
               + "\t <numberformat>\t '-nf decimal|e numberformat of data. decimal is the default.\r\n"               + "\r\n" 
               + "\tThe base path with a scenario appended should be a valid path that is the root dir of the MyM collection, i.e. should have subdirs F2RT, I2RT, T2RT\r\n"
               + "\r\nExample: java -jar PblDeliveryTool.jar -l INFO -t test.xls -t templateTest.xls -m mapping.xls -b C:\\data\\scenario_ -s R2G23 -r World -r CAN\r\n\r\n" 
               + "ERROR: check your commandline arguments";

         logger.fatal(aUsage);

         System.out.flush();
         
         
         System.err.print("\r\n\r\n");
         System.err.print(aUsage);
         // System.err.print(aExample);

         System.exit(1);
      }

      aDate = new java.util.Date();
      long aStop = aDate.getTime();
      long aRuntime = aStop - aStart;

      logger.info("Run took " + aRuntime + " milliseconds");
   }

   private boolean processArguments(String[] args)
   {
      boolean aCommandLineOk = true;
      for (int i = 0; i < args.length; i += 2)
      {
         if (args[i].equals("-f")) // output file
         {
            _outputName = args[i + 1];
         }
         else if (args[i].equals("-t")) // template file
         {
            _templateFile = args[i + 1];
         }
         else if (args[i].equals("-m")) // mappping file
         {
            _mappingFile = args[i + 1];
         }
         else if (args[i].equals("-b")) // scenario common path
         {
            _scenarioBasePath = args[i + 1];
         }
         else if (args[i].equals("-s")) // scenarios
         {
            _scenario.add(args[i + 1]);
         }
         else if (args[i].equals("-l")) // loglevel
         {

            PblLogger.changeLogLevel(Level.toLevel(args[i + 1]));
         }
         else if (args[i].equals("-of")) // outputformat
         {
            if ((args[i + 1].equals("csv"))||(args[i + 1].equals("excel")))
            {
               this._outputFormat= args[i + 1];
            }
            else 
            {
               logger.fatal("outputformat should be: excel or csv, supplied was: "
                     + args[i + 1]);
               aCommandLineOk = false;
            }
         }         
         else if (args[i].equals("-nf")) // numberformat
         {
            if ((args[i + 1].equals("e"))||(args[i + 1].equals("decimal")))
            {
               this._numberFormat= args[i + 1];
            }
            else 
            {
               logger.fatal("numberformat should be: decimal or e, supplied was: "
                     + args[i + 1]);
               aCommandLineOk = false;
            }
         }
         else if (args[i].equals("-r")) // regions
         {
            try
            {
               ProcessRegios(args[i + 1]);
            }
            catch (IllegalArgumentException e)
            {
               logger.fatal("The regions should be reported separated: -r USA -r WEU is valid, supplied was: "
                     + args[i + 1]);
               for (RegionType.RegionName aName : RegionType.RegionName.values())
               {
                  logger.info(aName + " is a valid region");
               }
               aCommandLineOk = false;
            }
         }
         else
         {
            logger.error("Command switch not recognized.  |" + args[i] + "|");
            aCommandLineOk = false;
         }
      }

      return aCommandLineOk;
   }

}
