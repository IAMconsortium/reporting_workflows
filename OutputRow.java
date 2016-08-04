package data;

import java.util.ArrayList;

import data.RegionType.ModelType;
import data.RegionType.RegionName;

/**
 * 
 * 
 * 
 * These rows have a double function: they can be used to represent rows in the
 * template.xls file but are also useful as output rows for the final document
 * (at this stage all fields are set).
 * 
 * @author Vortech
 */
public class OutputRow implements Cloneable
{

   //public String                   _model;   // we can distinguish between TIMER and IMAGE but PBL always wants IMAGE in the report (business logic)
   public String                   _scenario;
   private RegionType.RegionName _region;
   public String                   _variable;
   public String                   _unit;
   public ArrayList<Double>        _values             = new ArrayList<Double>();

   public int                      _templateSourceLine = 0;

   public String getRegionString()
   {
      return _region.toString();
   }
   
   public void changeToWorldNaEntry()
   {
      _region = RegionName.World;

      ArrayList<Double> aList = new ArrayList<Double>();
      for (@SuppressWarnings("unused") Double d : _values) //need to iterate to get the correct number of items..
      {
         aList.add(null);
      }

      _values = aList;
   }

   public OutputRow clone()
   {
      OutputRow aResult = new OutputRow(_variable, _unit, _templateSourceLine, _region);
      //aResult._model = _model;
      aResult._scenario = _scenario;

      for (Double d : _values)
      {
         if (d != null)
         {
            aResult._values.add(new Double(d));
         }
         else
         {
            aResult._values.add(null);
         }
      }

      return aResult;
   }

   public OutputRow(String iVariable, String iUnit, int iLine, RegionType.RegionName iRegion)
   {
      _variable = iVariable;
      _unit = iUnit;
      _templateSourceLine = iLine;
      _region = iRegion;
   }

   public String toString()
   {
      String aResult = "OutputRow: [" + _region + "|" + _variable + " | " + _unit + " | " + _templateSourceLine + " "
            + _values + " ]";

      return aResult;
   }

   protected static OutputRow createOutputRow(ModelType iModel, OutputRow iTemplateRow, String iScenario,
         RegionName iRegion)
   {
      OutputRow aRow = new OutputRow(iTemplateRow._variable, iTemplateRow._unit, iTemplateRow._templateSourceLine,
            iRegion);
      aRow._scenario = iScenario;
      //aRow._model = iModel.getExcelString();

      return aRow;
   }

   public RegionType.RegionName getRegion()
   {
      return _region;
   }

   public void setRegion(RegionType.RegionName _region)
   {
      this._region = _region;
   }

}
