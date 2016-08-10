package program;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import data.Mapping;
import data.MappingRow;
import data.OutputDocument;
import data.OutputRow;
import data.RegionControl;
import data.RegionType;
import data.WorldType;
import data.RegionType.ModelType;
import data.RegionType.RegionName;

import mdio.CConnectorException;
import mdio.MDataDomainStep;
import mdio.MDataReader;

/**
 * 
 * 
 * 
 * This class does the actual creation of the output rows for the report.
 * Preceding steps: processing of template and mapping Task: to take proper
 * values from the MDIO values and construct output for the OutputDocument.
 * 
 * Precondition : The command line arguments were validated Postcondition: The
 * Excel file is ready
 * 
 * @author Vortech
 */
public class Engine
{
   static Logger                    logger = Logger.getLogger(Engine.class.getName());
   Mapping                          _mapping;

   String                           _templateFile;
   ArrayList<String>                _scenario;
   String                           _outputFile;
   ArrayList<RegionType.RegionName> _reportRegions;
   String                           _scenarioBasePath;
   OutputDocument                   _output;
   String                           _outputFormat;
   String                           _numberFormat;

   /**
    * 
    * @param iTemplateFile
    *           filename + path of the template file.
    * @param iScenarios
    *           scenario names, to be appeded to iScenarioBasePath in order to
    *           get a valid path.
    * @param iScenarioBasePath
    *           common path name for the scenario's.
    * @param iOutputFile
    *           filename + path of the xls report that should be generated.
    * @param iMapping
    *           The processed mapping file.
    * @param iOutputFormat
    *           csv or excel
    * @param iNumberFormat
    *           decimal or E notation
    * @param iRegionsToReport
    *           Regions that should be reported, if the list is empty it means
    *           all regions will be reported.
    */
   public Engine(String iTemplateFile, ArrayList<String> iScenarios, String iScenarioBasePath, String iOutputFile,
         Mapping iMapping, String iOutputFormat, String iNumberFormat, 
         ArrayList<RegionType.RegionName> iRegionsToReport)
   {
      _templateFile = iTemplateFile;
      _scenario = iScenarios;
      _mapping = iMapping;
      _outputFile = iOutputFile;
      _reportRegions = iRegionsToReport;
      _scenarioBasePath = iScenarioBasePath;
      _outputFormat = iOutputFormat;
      _numberFormat = iNumberFormat;
   }

   public void doWork() throws Exception
   {
      _output = new OutputDocument(_outputFile, _templateFile, _outputFormat, _numberFormat);
      _output.setupDocumentFromTemplate();

      ArrayList<OutputRow> elementsToFill = _output.getOutrows();

      for (String aScenario : _scenario)
      {
         for (OutputRow r : elementsToFill)
         {
            createOutputEntry(r, aScenario);

         }
      }

      _output.close();

   }

   /**
    * Create rapport items for a given templateRow and scenario A typical
    * rapport contains multiple templateRows and scenario's giving a
    * T(emplate)xS(cenario) order of output rows.
    * 
    * Logging and nothing else will occur when a template row cannot be matched
    * against a mapping row.
    * 
    * @param iTemplateRow
    * @param iScenario
    */
   protected void createOutputEntry(OutputRow iTemplateRow, String iScenario) throws Exception
   {

      MappingRow aMatch = null;
      for (MappingRow m : _mapping.getRows())
      {
         if (m._variable.equals(iTemplateRow._variable) && m._unitTemplate.equals(iTemplateRow._unit))
         {
            aMatch = m;
         }
      }

      if (aMatch == null)
      {
         logger.warn("Mapping definition missing for template (Excel) line " + (iTemplateRow._templateSourceLine + 1) + ", variable: " + iTemplateRow._variable + "\t" + iTemplateRow._unit + " (variable is discarded in report generation)");
      }
      else
      {
         if (aMatch._filename == null)
         {
            logger.warn("data file missing : M file name missing in mapping on (Excel) line " + (aMatch._lineno + 1) + " for variable " + aMatch._variable + " (variable is discarded in report generation)");
         }
         else
         {
            fillRows(iTemplateRow, aMatch, iScenario);
         }
      }

   }

   protected MDataReader canOpenMFile(String iDataSourceName)
   {
      MDataReader aReader = null;

      File aExistanceTest = new File(iDataSourceName);
      if (aExistanceTest.exists())
      {
         try
         {
            aReader = new MDataReader(iDataSourceName);
         }
         catch (CConnectorException e)
         {
            // reader is returned as null, logging will be done by calling
            // function.
         }
      }

      return aReader;
   }

   /**
    * 
    * 
    * @param iMapping
    * @param iReader
    * @return type of the model, which is SingleValue for Global models, and
    *         depends on the dimensions for other Regional and Noglobal.
    */
   protected ModelType determineModelType(MappingRow iMapping, MDataReader iReader)
   {
      ModelType aDocType = null;

      if (iMapping._type == WorldType.regional || iMapping._type == WorldType.noglobal)
      {
         int aRelevantRegioDimension = MFileFilter.getColumnToIterate(iMapping._dimensions.get(0));
         aDocType = RegionType.typeForSize(iReader.getDimensionSizes(aRelevantRegioDimension));
      }
      else
      {
         aDocType = ModelType.SingleValue;
      }

      return aDocType;
   }

   /**
    * When a template is associated with a mapping then we can create output
    * rows for a given scenario and append them to the document. For this we use
    * the M file.
    * 
    * 
    * @param iTemplateRow
    * @param iMapping
    * @param iScenario
    * @throws Exception
    *            when not all requested years are encountered in the M file
    */
   protected void fillRows(OutputRow iTemplateRow, MappingRow iMapping, String iScenario) throws Exception
   {

      String aDataSourceName = _scenarioBasePath + iScenario + "\\" + iMapping._filename;
      
      logger.info("template (Excel) " + (iTemplateRow._templateSourceLine + 1) + ":" + iTemplateRow._variable
            + " will be filled with mapping (Excel) " + (iMapping._lineno + 1) + ":" + iMapping._variable
            + " using scenario: " + iScenario);

      MDataReader aReader = canOpenMFile(aDataSourceName);

      if (aReader == null)
      {
         logger.error("Unable to open " + aDataSourceName + " with a mapping from line " + iMapping._lineno
               + " and a template from line " + iTemplateRow._templateSourceLine + " (variable is discarded in report generation)");
         
         return;
      }

      MDataDomainStep aStep = aReader.getMDataDomainStep();
      ModelType aDocType = determineModelType(iMapping, aReader);
      
      Map<RegionName, OutputRow> aRegionRow = null;
      try
      {
         aRegionRow = RegionControl.createOutputList(aDocType, iTemplateRow, iScenario, iMapping._type);
         stepThroughFile(iMapping, aStep, iTemplateRow, aReader, aRegionRow);
         postProcess(aRegionRow);         
      }
      catch (Exception e)
      {
         logger.error("Unable to create the output rows for this template/mapping!");
      }



   }

   public void stepThroughFile(MappingRow iMapping, MDataDomainStep iStep, OutputRow iTemplateRow, MDataReader iReader,  Map<RegionName, OutputRow> ioRegionRow)
   {
      int i = 0;
      ModelType aDocType = determineModelType(iMapping, iReader);      
      MFileFilter aFilter = new MFileFilter(iMapping);

      ArrayList<Integer> aRelevantSet = _output.getRelevantYears();
      ArrayList<Integer> aMatchedSet = new ArrayList<Integer>();      
      
      int aCurrentRelevantIndex = 0;
      while (iStep.getTimestep(i) > 0.01 && aCurrentRelevantIndex < aRelevantSet.size())
      { // double comparison, and 0
        // means no entry
         
         int aYearToCompare = (int) iStep.getTimestep(i);
         double aValueSet[];

         if (aRelevantSet.get(aCurrentRelevantIndex) == aYearToCompare)
         {
            aValueSet = aFilter.filterValues(iStep, i);
            Double d[] = new Double[aValueSet.length];

            for (int k = 0; k < aValueSet.length; k++)
            {
               d[k] = aValueSet[k];
            }
            processFilteredData(iTemplateRow, iMapping, d, ioRegionRow, aDocType, iReader);
            aMatchedSet.add(aYearToCompare);
            aCurrentRelevantIndex++;
         }
         if (aCurrentRelevantIndex < aRelevantSet.size() && aYearToCompare > aRelevantSet.get(aCurrentRelevantIndex))
         {
            // insert empty column as we missed our expected year (in template,
            // but not in M-file)
            int aSize = aFilter.filterValues(iStep, 0).length;
            Double aDummy[] = new Double[aSize];
            for (int k = 0; k < aSize; k++)
            {
               aDummy[k] = null;
            }
            processFilteredData(iTemplateRow, iMapping, aDummy, ioRegionRow, aDocType, iReader);
            aCurrentRelevantIndex++;
         }
         i++;
      }

      if (aMatchedSet.size() != aRelevantSet.size())
      {
         String aMsg = "Not all years in the template were available in M file, requested : " + aRelevantSet
               + " and found was: " + aMatchedSet;
         logger.warn(aMsg);
      }      
   }
   
   /**
    * Since the M file always returns a row for every dimension we want to keep
    * only the rows we requested in the report.
    * 
    * @param iRegionRow
    */
   protected void postProcess(Map<RegionName, OutputRow> iRegionRow)
   {
      if (_reportRegions.size() == 0) // no filtering on region
      {
         for (OutputRow aRow : iRegionRow.values())
         {
            _output.addRow(aRow);
         }
      }
      else
      {
         for (OutputRow aRow : iRegionRow.values())
         {
            for (RegionType.RegionName aRegion : _reportRegions)
            {
               if (aRegion == aRow.getRegion())
               {
                  _output.addRow(aRow);
               }
            }
         }
      }

   }

   /**
    * When data comes in from the M file not all rows are relevant and some
    * manipulation is required, like skipping record 27 in a file with 28
    * dimenions. This is done here and the result has values from the M file
    * properly mapped to regions.
    * 
    * @param iTemplateRow
    * @param iMapping
    * @param iValueSet
    * @param iRegionRow
    * @param iDocType
    */
   protected void processFilteredData(OutputRow iTemplateRow, MappingRow iMapping, Double iValueSet[],
         Map<RegionName, OutputRow> iRegionRow, ModelType iDocType, MDataReader iReader)
   {

      if (iMapping._type == WorldType.global)
      {
         OutputRow aRow = iRegionRow.get(RegionName.World);
         addSafeValue(aRow,iMapping._conversionFactor, iValueSet[0]);

      }
      else if (iMapping._type == WorldType.regional || iMapping._type == WorldType.noglobal)
      {
         for (int aRegionIndex = 0; aRegionIndex < iValueSet.length; aRegionIndex++) // should
         // depend
         // on
         // ModelType
         {
            int aRowIndex = aRegionIndex;
            OutputRow aRow = null;
            if (aRegionIndex == 26 && iDocType == ModelType.TimerFairEmptyWorld)
            {
               aRegionIndex++; // skip empty record
               aRow = iRegionRow.get(RegionName.World);
               addSafeValue(aRow,iMapping._conversionFactor, iValueSet[aRegionIndex]);
               
               if(iMapping._type == WorldType.noglobal)
               {
                     aRow.changeToWorldNaEntry();
               }
            }
            else if (aRegionIndex == 26 && iDocType == ModelType.TimerFairWorld)
            {
               aRow = iRegionRow.get(RegionName.values()[aRowIndex]);
               addSafeValue(aRow,iMapping._conversionFactor, iValueSet[aRegionIndex]);
               
               if(iMapping._type == WorldType.noglobal)
               {
                     aRow.changeToWorldNaEntry();
               }
            }
            else if (aRegionIndex == 24 && iDocType == ModelType.ImageWorld)
            {
               //take world key, instead of index
               aRow = iRegionRow.get(RegionName.World);
               addSafeValue(aRow,iMapping._conversionFactor, iValueSet[aRegionIndex]);               
            }
            else
            {
               aRow = iRegionRow.get(RegionName.values()[aRowIndex]);
               addSafeValue(aRow,iMapping._conversionFactor, iValueSet[aRegionIndex]);
            }

         }

      }

   }

   private void addSafeValue(OutputRow iRow, double iConversionFactor, Double iValue)
   {
      if (iValue != null)
      {
         iRow._values.add(new Double(iConversionFactor * iValue));
      }
      else
      {
         iRow._values.add(null);
      }
   }
}
