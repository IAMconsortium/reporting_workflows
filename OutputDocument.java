package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * This class prepares the output document, based on the template Excelsheet. It
 * manages the layout and writing of data to the output sheet.
 * 
 * @author Vortech
 * 
 */
public class OutputDocument
{
   static final String  kIMAGE         = "IMAGE";
   static final String  kDATASHEET     = "data";
   static final String  kNA            = "N/A";
   static final String  kENOTATION     = "e";
   static final String  kCSV           = "csv";
   int                  _xoffset, _yoffset;

   String               _outfileName;
   String               _templateName;
   String               _outputFormat;
   String               _numberFormat;
   int                  _currentOutputRow;

   Workbook         _workbook      = null;

   ArrayList<Integer>   _relevantYears = new ArrayList<Integer>();

   static Logger        logger         = Logger.getLogger(OutputDocument.class.getName());

   ArrayList<OutputRow> _outrows       = new ArrayList<OutputRow>();

   CellStyle        _DecimalStyle  = null;
   CellStyle        _NormalStyle   = null;
   CellStyle        _EStyle   = null;


   public void close()
   {

      try
      {
         FileOutputStream fis = new FileOutputStream(_outfileName);
         _workbook.write(fis);
         fis.close();
         if (_outputFormat.equals(kCSV)== true) 
         {
            Sheet sh = _workbook.getSheetAt(_workbook.getActiveSheetIndex());
            //header

            
            String outname = _outfileName.replace(".xlsx", ".csv");
            final PrintWriter csvout = new PrintWriter (
                                          new BufferedWriter (
                                                new FileWriter(outname)));

            CSVPrinter printer = new CSVPrinter(csvout, CSVFormat.EXCEL.withHeader());
            
            for (int i=sh.getFirstRowNum();i <= sh.getLastRowNum();i++)
            {            
               Row r = sh.getRow(i);
               for (int j=r.getFirstCellNum() ; j < r.getLastCellNum();j++)
               { 
                  printer.print(r.getCell(j));
               }
               printer.println();                          
            }
            printer.close();
         }
      }
      catch (IOException e)
      {
         logger.error("Error writing report!");
      }
   }

   public OutputDocument(String iOutfileName, String iTemplateFileName, 
                         String iOutputFormat, String iNumberFormat)
   {
      _outfileName = iOutfileName;
      _templateName = iTemplateFileName;
      _outputFormat =  iOutputFormat;
      _numberFormat =  iNumberFormat;
   }

   public ArrayList<OutputRow> getOutrows()
   {
      return _outrows;
   }

   public void setOutrows(ArrayList<OutputRow> iList)
   {
      _outrows = iList;
   }

   public void setupDocumentFromTemplate() throws Exception
   {

      Runtime rt = Runtime.getRuntime();
      // Process pr = rt.exec("cmd /c dir");
      String command = "cmd /c copy " + _templateName + " " + _outfileName;
      logger.info("setupdocument " + command);
      Process pr = null;
      try
      {
         pr = rt.exec(command);
      }
      catch (IOException e)
      {
         logger.error("Error while attempting template copy.");
         e.printStackTrace();
      }

      int exitVal = 0;
      try
      {
         exitVal = pr.waitFor();
      }
      catch (InterruptedException e)
      {
         logger.error("Error while waiting for template copy to finish.");
         e.printStackTrace();
      }
      if (exitVal != 0)
      {
         logger.error("Error copy failed for: " + _templateName + " " + _outfileName);
      }

      try
      {
         FileInputStream fis = new FileInputStream(_outfileName);
         _workbook = WorkbookFactory.create(fis);
         _DecimalStyle = _workbook.createCellStyle();
         _DecimalStyle.setDataFormat((short) 2);
         _NormalStyle = _workbook.createCellStyle();
         _NormalStyle.setDataFormat((short) 1);
         _EStyle = _workbook.createCellStyle();
         _EStyle.setDataFormat(_workbook.getCreationHelper().createDataFormat().getFormat("0.00000E+00"));
         // http://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/BuiltinFormats.html
         // http://stackoverflow.com/questions/22743039/apache-poi-formatting-double-numbers-in-excel-files
      }
      catch (IOException e)
      {
         logger.error("Error could not open the copied file: " + _outfileName);
      }

      try
      {
         logger.info("Will now try to look for the sheet 'data' in the template file.");
         findVariableKeywordCell(_workbook.getSheet(kDATASHEET));
      }
      catch (Exception e)
      {
         logger.error("Could not locate 'UNIT' keyword in sheet " + kDATASHEET + " in " + _outfileName);
      }

      try
      {
         createRelevantYears(_workbook);
         createOutputRows(_workbook);
      }
      catch (IOException e)
      {
         logger.error("Error could not read information from the template file: " + _templateName);
      }

   }

   private void createOutputRows(Workbook _workbook2)
   {

      for (int y = _yoffset + 1; y <= _workbook2.getSheet(kDATASHEET).getLastRowNum(); y++)
      {
         Row aRow = _workbook2.getSheet(kDATASHEET).getRow(y);

         Cell aCell = aRow.getCell(_xoffset - 1);
         if (aCell == null)
         {
            break;
         }

         String aVariable = aRow.getCell(_xoffset - 1).getStringCellValue();
         String aUnit = aRow.getCell(_xoffset).getStringCellValue();
         // String aRegion = aRow.getCell(_xoffset-2).getStringCellValue();
         // -relevant regions supplied from command line
         _outrows.add(new OutputRow(aVariable, aUnit, y, null));
      }
      logger.debug("outrows to fill per scenario: " + _outrows.size());
   }

   public ArrayList<Integer> getRelevantYears()
   {
      return _relevantYears;
   }

   private void createRelevantYears(Workbook _workbook2) throws Exception
   {
      Row aRow = _workbook2.getSheet(kDATASHEET).getRow(_yoffset);
      boolean aHasPrevious = false;
      int aPreviousYear = -1;

      for (int i = _xoffset + 1; i < aRow.getLastCellNum(); i++)
      {
         int d = (int) aRow.getCell(i).getNumericCellValue();
         _workbook2.getSheet(kDATASHEET).setColumnWidth(i, 256 * 12); // 256
                                                                         // is 1
                                                                         // character
                                                                         // wide
         if (d == 0)
         {
            // cell is empty
            break;
         }
         if (aHasPrevious)
         {
            if (d <= aPreviousYear)
            {
               String aMsg = "The years in the template have to be in rising order from left to right, this is not the case as "
                     + aPreviousYear + " was followed by " + d;
               logger.fatal(aMsg);
               throw new Exception(aMsg);
            }
         }
         _relevantYears.add(new Integer(d));
         aPreviousYear = d;
         aHasPrevious = true;
      }
      logger.debug("relevant years: " + _relevantYears);

   }

   public void addRow(OutputRow iRow)
   {
      Sheet aSheet = _workbook.getSheet(kDATASHEET);

      aSheet.createRow(_currentOutputRow);
      Row aRow = aSheet.getRow(_currentOutputRow);

      // set fields
      for (int i = 0; i < 32; i++)
      {
         aRow.createCell(i);
      }

      aRow.getCell(0).setCellValue(kIMAGE);
      aRow.getCell(1).setCellValue(iRow._scenario);
      aRow.getCell(2).setCellValue(iRow.getRegionString());
      aRow.getCell(3).setCellValue(iRow._variable);
      aRow.getCell(4).setCellValue(iRow._unit);

      int i = 5;
      for (Double s : iRow._values)
      {
         if (s != null)
         {
            aRow.getCell(i).setCellValue(s);
            if (this._numberFormat.equals(kENOTATION))
            {
               aRow.getCell(i).setCellStyle(_EStyle);
            }
            else
            {
               if (Math.abs(s.doubleValue()) >= 10)
               {
                  aRow.getCell(i).setCellStyle(_NormalStyle);
               }
               else
               {
                  aRow.getCell(i).setCellStyle(_DecimalStyle);
               }
            }
         }
         else
         {
            aRow.getCell(i).setCellValue(kNA);
         }
         i++;
      }

      _currentOutputRow++;
   }

   private void findVariableKeywordCell(Sheet sheet) throws Exception
   {
      for (int i = 0; i < 15; i++)
      {
         Row aRow = sheet.getRow(i);
         for (int j = 0; j < 15 && j < aRow.getLastCellNum(); j++)
         {
            if (aRow.getCell(j).getStringCellValue().equals("Unit"))
            {
               _xoffset = j;
               _yoffset = i;
               _currentOutputRow = i + 1;
               return;
            }
         }
      }

      throw new Exception("Could not find cell 'Unit' in the template file");
   }

}
