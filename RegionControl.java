package data;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import data.RegionType.ModelType;
import data.RegionType.RegionName;

/**
 * This class has the enumerations for the various dimensions in the M-files
 * that are encountered. It also has an enumeration with the region names in it.
 * Note that correct behaviour depends on the order of regions in the RegionName
 * enumeration! This will also not work with the aggegrated regions in files
 * with 33 dimensions!
 * 
 * @author Vortech
 * 
 */
public class RegionControl
{
   static Logger logger = Logger.getLogger(RegionControl.class.getName());


   /**
    * <pre>
    *    In onze huidige datafiles kan je verschillende configuraties tegenkomen
    *    24    24 Image regios
    *    25    24 image regios+wereld
    *    26    26 TIMER/FAIR regios
    *    27    26 TIMER/FAIR regios+wereld
    *    28    26 TIMER/FAIR regios+lege regio+wereld
    *    33    26 TIMER/FAIR regios+aggregaties+wereld
    * 
    *    Dus je kan wel een bestand tegenkomen met 25 + 1 regios.
    * 
    *    De beslisboom wordt dan:
    * 
    *   IF 24,
    *      Report [24 regios, 0 for region 25 en 26]
    *      LOG: missing regions 25 and 26, LOG missing global 
    *   If 25,
    *      Report [24 regios, 0 for region 25 en 26, Global(dim25 van data)]
    *      LOG: missing regions 25 and 26 
    *   IF 26,
    *      report [26 regions]
    *      LOG: missing global
    *    IF 27
    *           report [26 regions, global(dim27 van data)]
    *           LOG: missing global
    *    IF 28
    *           report [26 regions, global(dim28 van data)]
    *           LOG: missing global
    *    IF 33
    *           report [26 regions, global(dim33 van data)]
    *           LOG: missing global
    * 
    *    Voor wereldtotaal geld ook
    *    IFNOT 24 of 26 THEN laatste van de array altijd het wereldtotaal.
    *    IF 24 of 25 THEN report N/A, LOG: missing global
    * </pre>
    */
   protected static void createRegions(int numRegions, HashMap<RegionName, OutputRow> ioRegions, ModelType iModel,
         OutputRow iTemplateRow, String iScenario)
   {
      int i = 0;
      for (RegionName r : RegionName.values())
      {
         OutputRow aRow = OutputRow.createOutputRow(iModel, iTemplateRow, iScenario, r);
         ioRegions.put(r, aRow);
         i++;

         if (i == numRegions)
         {
            break;
         }
      }

   }

   /**
    * This function uses a combination of the global/noglobal/regions and
    * ModelType to determine how many outputrows need to be created for the
    * current mapping.
    * 
    * @param iModel The modeltype determines the amount of regions when worldtype is not 'global'
    * @param iTemplateRow The template row is used to copy some relevant information to the output row, like the variable name and unit.
    * @param iScenario The scenario is also part of the result row and is already copied into place here.
    * @param iType If the WorldType is set to global then only a single output row is created.
    * @return A map that maps regions to output rows, it can contain 1, 24, 25, 26 or 27 entries.
    * @throws Exception If the ModelType is not recognized and the WorldType is not global.
    * 
    */
   public static Map<RegionName, OutputRow> createOutputList(ModelType iModel, OutputRow iTemplateRow,
         String iScenario, WorldType iType) throws Exception
   {
      HashMap<RegionName, OutputRow> aResult = new HashMap<RegionName, OutputRow>();

      if (iType == WorldType.global)
      {
         OutputRow aRow = OutputRow.createOutputRow(iModel, iTemplateRow, iScenario, RegionName.World);

         aResult.put(RegionName.World, aRow);
      }
      else
      {
         switch (iModel)
         {
            case Image:
               createRegions(24, aResult, iModel, iTemplateRow, iScenario);
               break;
            case ImageWorld:
               createRegions(24, aResult, iModel, iTemplateRow, iScenario);
               aResult.put(RegionName.World,
                     OutputRow.createOutputRow(iModel, iTemplateRow, iScenario, RegionName.World));
               break;
            case TimerFair:
               createRegions(26, aResult, iModel, iTemplateRow, iScenario);
               break;
            case TimerFairWorld:
               createRegions(26, aResult, iModel, iTemplateRow, iScenario);
               aResult.put(RegionName.World,
                     OutputRow.createOutputRow(iModel, iTemplateRow, iScenario, RegionName.World));
               break;
            case TimerFairEmptyWorld:
               createRegions(26, aResult, iModel, iTemplateRow, iScenario);
               aResult.put(RegionName.World,
                     OutputRow.createOutputRow(iModel, iTemplateRow, iScenario, RegionName.World));
               break;
            case TimerFairAggWorld:
               createRegions(33, aResult, iModel, iTemplateRow, iScenario);
               break;
            case SingleValue:
               aResult.put(RegionName.World,
                     OutputRow.createOutputRow(iModel, iTemplateRow, iScenario, RegionName.World));
               break;
            default:
               throw new Exception("Model not supported");
         }

      }

      return aResult;
   }

}
