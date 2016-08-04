package data;

import java.util.ArrayList;

/**
 * This class represents a single row in the Excel mapping sheet. The line
 * number is also added to provide more useful error messages.
 * 
 * @author Vortech
 * 
 */
public class MappingRow
{

   public String                        _variable;
   public String                        _unitTemplate;
   public String                        _filename;
   public WorldType                     _type;
   public ArrayList<ArrayList<Integer>> _dimensions;
   public ArrayList<Integer> _fileDimensions;
   // dimensions in M file
   public String                        _unitFile;
   public Double                        _conversionFactor;

   public int                           _lineno;

   public MappingRow(int iLine)
   {
      _variable = null;
      _unitTemplate = null;
      _filename = null;
      _type = null;
      _dimensions = null;
      _fileDimensions = null;
      _unitFile = null;
      _conversionFactor = null;
      _lineno = iLine;
   }

   public String toString()
   {
      String s = "MappingRow: ";
      s += _variable + "\t" + _unitTemplate + " ....";

      return s;
   }

   /**
    * 
    * @return false if the (requested) _dimensions are outside the _fileDimensions, also a _conversionFactor is required when _dimensions are set.
    */
   public boolean isValid()
   {
      boolean aResult = true;
      
      if (_dimensions.size() > 0 && _conversionFactor == null)
      {
         aResult = false;
      }
      
      for (ArrayList<Integer> singleDimSet : _dimensions)
      {
         for (int i=0;i<singleDimSet.size();++i)
         {
            if (singleDimSet.get(i) != null && singleDimSet.get(i) > _fileDimensions.get(i)) //ignore special case for the 'r' template value
            {
               aResult = false;
            }
         }
      }
      return aResult;
      
   }
   
}
