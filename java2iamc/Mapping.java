package data;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * 
 * This class is a container that represents the mapping.xls file
 * 
 * @author Vortech
 * 
 */
public class Mapping
{

   static Logger         logger = Logger.getLogger(Mapping.class.getName());

   ArrayList<MappingRow> _mappings;

   public Mapping()
   {
      _mappings = new ArrayList<MappingRow>();
   }

   /**
    * 
    * @param row
    *           Row from a mapping xls file
    * @param xoffset
    *           first cell (0-based that needs processing from left to right, is
    *           the variable name)
    */
   public void addMappingFromRowSheet(Row row, int xoffset)
   {
      MappingRow aRow = new MappingRow(row.getRowNum());
      
      aRow._variable = retrieveStringFromCell(row.getCell(xoffset));

      logger.info("Reading from mapping: " + aRow._variable);

      aRow._unitTemplate = retrieveStringFromCell(row.getCell(xoffset + 1));
      aRow._filename = retrieveStringFromCell(row.getCell(xoffset + 2));

      String aTypeString = retrieveStringFromCell(row.getCell(xoffset + 3));
      try
      {
         aRow._type = null;
         if (aTypeString != null)
         {
            aRow._type = WorldType.valueOf(aTypeString);
         }
      }
      catch (IllegalArgumentException e)
      {
         logger.error("The specified type in the mapping template at (Excel) line: " + aRow._lineno
               + "  is not recognized: " + retrieveStringFromCell(row.getCell(xoffset + 3)));
      }
      aRow._dimensions = parseRange(retrieveStringFromCell(row.getCell(xoffset + 4)));

      aRow._fileDimensions = null;
      ArrayList<ArrayList<Integer>> aFileDims = parseRange(retrieveStringFromCell(row.getCell(xoffset + 5)));
      if (aFileDims != null && aFileDims.size() > 0)
      {
         aRow._fileDimensions = aFileDims.get(0);
      }

      aRow._unitFile = retrieveStringFromCell(row.getCell(xoffset + 6));
      aRow._conversionFactor = retrieveDoubleFromCell(row.getCell(xoffset + 7));

      if (aRow.isValid())
      {
         _mappings.add(aRow);
      }
      else
      {
         logger.error("The mapping for "
               + aRow._variable
               + " at (Excel) line "
               + (aRow._lineno + 1)
               + " is not consistent, the conversion factor is missing or the requested dimensions exceed the file dimensions (row discarded in report generation)!");
      }

   }

   protected Double retrieveDoubleFromCell(Cell cell)
   {
      Double aResult = null;
      try
      {
         aResult = new Double(cell.getNumericCellValue());
      }
      catch (Exception e)
      {
         // in case of empty numeric cell etc.
         logger.info("Error with numeric cell.");
      }

      return aResult;
   }

   protected String retrieveStringFromCell(Cell cell)
   {
      String aResult = null;
      try
      {
         aResult = cell.getStringCellValue();
         if (aResult.length() == 0)
         {
            aResult = null; // discard empty string
         }
      }
      catch (Exception e)
      {
         // in case of empty string cell etc.
         logger.info("Error with string cell.");
      }

      return aResult;
   }

   protected ArrayList<ArrayList<Integer>> parseRange(String iDimensions)
   {

      ArrayList<ArrayList<Integer>> aResult = new ArrayList<ArrayList<Integer>>();
      int aCurrentIndex = 0;

      while (iDimensions != null && iDimensions.indexOf('[') != -1)
      {
         aCurrentIndex = iDimensions.indexOf('[');
         int aBlockEnd = iDimensions.indexOf(']');

         String s = iDimensions.substring(aCurrentIndex + 1, aBlockEnd);
         aResult.add(parseLine(s));

         iDimensions = iDimensions.substring(aBlockEnd + 1);
      }

      return aResult;
   }

   protected ArrayList<Integer> parseLine(String iNumbers)
   {
      ArrayList<Integer> aResult = new ArrayList<Integer>();

      // vgl. [r] ipv [1,2,r]
      String[] aNumberSet = iNumbers.split(",");

      for (String aNum : aNumberSet)
      {
         if (aNum.length() > 0)
         {
            if (aNum.equals("r"))
            {
               aResult.add(null);
            }
            else
            {
               aResult.add(new Integer(aNum));
            }
         }
      }

      return aResult;
   }

   public ArrayList<MappingRow> getRows()
   {
      return _mappings;
   }

   public String toString()
   {
      String s = "Mapping: \r\n";
      for (MappingRow r : _mappings)
      {
         s += r.toString() + "\r\n";
      }
      s += "\r\n";
      return s;
   }
}
