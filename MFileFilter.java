package program;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import data.MappingRow;
import mdio.MDataDomainStep;

/**
 * 
 * This class takes MDIO input and uses the filter constraints from a mapping to
 * return only the relevant values/slices for the rapport
 * 
 * @author Vortech
 * 
 */
public class MFileFilter
{

   static Logger logger = Logger.getLogger(MFileFilter.class.getName());

   MappingRow    _mapping;

   public MFileFilter(MappingRow iMapping)
   {
      _mapping = iMapping;
   }

   /**
    * This function takes the relevant data from a timestep, where the relevant
    * data is defined in the mapping, it is usually a slice of a timestep
    * matrix.
    * 
    * @param iStep
    * @param iTimeStep
    * @return double[] array, the size is the same as the size of the dimenion
    *         that is iterated (can be a single value).
    */
   public double[] filterValues(MDataDomainStep iStep, int iTimeStep)
   {
      double aResult[] = null;

      ArrayList<ArrayList<Integer>> aMappingDims = _mapping._dimensions;
      ArrayList<Integer> aFileDims = null;
      if (_mapping._fileDimensions != null) // when a single value is supplied
                                            // for each timestep no dimensions
                                            // are provided
      {
         aFileDims = _mapping._fileDimensions;
      }

      if (aFileDims == null)
      {
         aResult = new double[] { 0 };
         aResult[0] = iStep.getData(iTimeStep)[0].doubleValue();

      }
      else if (aFileDims.size() == 1)
      {
         aResult = singleDimension(iStep.getData(iTimeStep), aMappingDims);
      }
      else if (aFileDims.size() == 2)
      {
         aResult = twoDimensions(iStep.getData(iTimeStep), aMappingDims);
      }
      else if (aFileDims.size() == 3)
      {
         aResult = threeDimensions(iStep.getData(iTimeStep), aMappingDims);
      }

      return aResult;
   }

   /**
    * Mappings are specified in the Excel template as 1, 2 or 3 dimensional:
    * [2,2,2] [2,3] [4] Mappings can contain an 'r' to specify that that
    * dimensions needs to be iterated and contains a value for each region
    * Multiple mappings may be summed if the dimensions are equal: [r, 4] [r, 5]
    * means iterate over the first dimension so that you create the sum of cell
    * 4 and 5 for each region.
    * 
    * @param iMapping
    *           the 'wanted dimensions' mapping from the template file.
    * @return The required output set: a single value, or a value per region.
    */
   public double[] singleDimension(Number[] iData, ArrayList<ArrayList<Integer>> iMapping)
   {
      double aResult[] = null;

      boolean aSingleValue = true; // do we need to provide separate values per
                                   // region or a total (=true)?
      Number[] aSet = iData;

      for (Integer aDimToCheck : iMapping.get(0))
      {
         if (aDimToCheck == null)
         { // null represents the 'r' character from the Excel template file
            aSingleValue = false;
         }

      }

      if (aSingleValue)
      {
         aResult = new double[] { 0 };

         for (ArrayList<Integer> n : iMapping)
         {
            for (Integer m : n)
            {
               aResult[0] += aSet[m - 1].doubleValue();
            }
         }
      }
      else
      {
         aResult = new double[aSet.length];

         for (int i = 0; i < aSet.length; ++i)
         {
            aResult[i] = aSet[i].doubleValue();
         }
      }

      return aResult;
   }

   public static int getColumnToIterate(ArrayList<Integer> iDimToCheck)
   {
      int aResult = -1;

      if (iDimToCheck != null)
      {
         int i = 0;
         for (Integer aIndex : iDimToCheck)
         {
            if (aIndex == null)
            {
               aResult = i;
            }
            i++;
         }
      }

      return aResult;
   }

   public double[] twoDimensions(Number[] iData, ArrayList<ArrayList<Integer>> iMapping)
   {
      double aResult[] = null;

      boolean aSingleValue = true; // do we need to provide separate values per
                                   // region or a total (=true)?
      int aColumn = 0;
      int aColumnToIterate = -1;
      for (Integer aDimToCheck : iMapping.get(0))
      {
         if (aDimToCheck == null)
         { // null represents the 'r' character from the Excel template file
            aSingleValue = false;
            aColumnToIterate = aColumn;

         }
         aColumn++;
      }

      if (aSingleValue)
      {

         aResult = new double[] { 0 };
      }
      else
      {
         aResult = new double[_mapping._fileDimensions.get(aColumnToIterate)];
      }

      for (ArrayList<Integer> aDimToPick : iMapping)
      {
         if (aSingleValue)
         {
            int xoffset = (aDimToPick.get(0) - 1) * _mapping._fileDimensions.get(1);
            int yoffset = aDimToPick.get(1) - 1;
            aResult[0] += iData[xoffset + yoffset].doubleValue();
         }
         else
         {
            if (aColumnToIterate == 0)
            {
               // int yoffset = (aDimToPick.get(1) -1) *
               // _mapping._fileDimensions.get(0).get(1);
               int yoffset = aDimToPick.get(1) - 1;

               for (int i = 0; i < _mapping._fileDimensions.get(0); i++)
               {
                  aResult[i] += iData[i * _mapping._fileDimensions.get(1) + yoffset].doubleValue();
               }
            }
            else
            {
               int xoffset = (aDimToPick.get(0) - 1) * _mapping._fileDimensions.get(1);
               for (int i = 0; i < _mapping._fileDimensions.get(1); i++)
               {

                  aResult[i] += iData[xoffset + i].doubleValue();
               }
            }

         }
      }

      return aResult;
   }

   public double[] threeDimensions(Number[] iData, ArrayList<ArrayList<Integer>> iMapping)
   {
      double aResult[] = null;

      boolean aSingleValue = true; // do we need to provide separate values per
                                   // region or a total (=true)?
      int aColumn = 0;
      int aColumnToIterate = -1;
      for (Integer aDimToCheck : iMapping.get(0))
      {
         if (aDimToCheck == null)
         { // null represents the 'r' character from the Excel template file
            aSingleValue = false;
            aColumnToIterate = aColumn;

         }
         aColumn++;
      }

      if (aSingleValue)
      {

         aResult = new double[] { 0 };
      }
      else
      {
         aResult = new double[_mapping._fileDimensions.get(aColumnToIterate)];
      }

      /**
       * The offsets from the template are one-based: the dimensions [2,3,4] can be read as [1..2,1..3,1..4]
       * However, we need to convert to a zero-based single address. [0,6,10] in [28,6,10] gives 0 + (6-1)*10 + (10-9) is 59 as output.
       * When we iterate over a column we start already with a zero-based number, hence for [r, 6, 10] we get r*6*10 + (6-1)*10 + (10-9)
       * and not (r-1)*6*10+..... This discrepancy can create confusion!
       * 
       * 
       */
      for (ArrayList<Integer> aDimToPick : iMapping)
      {
         if (aSingleValue)
         {
            int xoffset = (aDimToPick.get(0) -1) * _mapping._fileDimensions.get(1) * _mapping._fileDimensions.get(2);
            int yoffset = (aDimToPick.get(1) -1)  * _mapping._fileDimensions.get(2);
            int zoffset = aDimToPick.get(2) - 1;
            aResult[0] += iData[xoffset + yoffset + zoffset].doubleValue();
         }
         else
         {
            if (aColumnToIterate == 0)
            {
               int zoffset = (aDimToPick.get(2) - 1);
               int yoffset = (aDimToPick.get(1) - 1) * _mapping._fileDimensions.get(2) ;

               for (int i = 0; i < _mapping._fileDimensions.get(0); i++)
               {
                  int xoffset = i * _mapping._fileDimensions.get(1) * _mapping._fileDimensions.get(2); //The i variable is already zero-based so do NOT subtract 1
                  aResult[i] += iData[yoffset + zoffset + xoffset].doubleValue();
               }
            }
            else if (aColumnToIterate == 1)
            {
               int zoffset = (aDimToPick.get(2) - 1);
               int xoffset = (aDimToPick.get(0) - 1) * _mapping._fileDimensions.get(1) * _mapping._fileDimensions.get(2);

               for (int i = 0; i < _mapping._fileDimensions.get(1); i++)
               {
                  int yoffset = i * _mapping._fileDimensions.get(2);
                  aResult[i] += iData[yoffset + zoffset + xoffset].doubleValue();
               }
            }
            else
            {
               // int zoffset = (aDimToPick.get(2) - 1);
               int yoffset = (aDimToPick.get(1) - 1) * _mapping._fileDimensions.get(2);
               int xoffset = (aDimToPick.get(0) - 1) * _mapping._fileDimensions.get(1) * _mapping._fileDimensions.get(2);

               for (int i = 0; i < _mapping._fileDimensions.get(2); i++)
               {
                  int zoffset = i;
                  aResult[i] += iData[yoffset + zoffset + xoffset].doubleValue();
               }

            }

         }
      }

      return aResult;
   }

}
