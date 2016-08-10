package data;

import org.apache.log4j.Logger;

public class RegionType
{
   static Logger logger = Logger.getLogger(RegionType.class.getName());
   
   public static ModelType typeForSize(int iSize)
   {
      switch (iSize)
      {
         case 24:
            return ModelType.Image;
         case 25:
            return ModelType.ImageWorld;
         case 26:
            return ModelType.TimerFair;
         case 27:
            return ModelType.TimerFairWorld;
         case 28:
            return ModelType.TimerFairEmptyWorld;
         case 33:
            // return ModelType.TimerFairAggWorld;
            logger.warn("33 dimensions in file, selected type will be Timer/Fair/World (27 dimensions) with global result set to N/A");
            return ModelType.TimerFairWorld;
         default:
            return null;
      }
   }
   
   
   public enum RegionName
   {
      CAN, USA, MEX, RCAM, BRA, RSAM, NAF, WAF, EAF, SAF, WEU, CEU, TUR, UKR, STAN, RUS, ME, INDIA, KOR, CHN, SEAS, INDO, JAP, OCE, RSAS, RSAF, World, Regio1, Regio2, Regio3, Regio4, Regio5, Empty

   }
   
   public enum ModelType
   {
      Image, ImageWorld, TimerFair, TimerFairWorld, TimerFairEmptyWorld, TimerFairAggWorld, SingleValue;

      public String getExcelString()
      {
         String base = this.toString();
         if (base.startsWith("Image"))
         {
            return "IMAGE";
         }
         else if (base.startsWith("Timer"))
         {
            return "TIMER";
         }
         else
         {
            return "UNKNOWN";
         }
      }

   }   
}
