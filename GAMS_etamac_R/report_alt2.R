library(gdxrrw)

### for the whole IAMC reporting, it might make sense to split up the reporing code into various functions and files
### this file demonstrates the general workflow

gdx <- "fulldata.gdx"
source("repMacro.R")
source("repEnergy.R")

dat <- rbind(repMacro(gdx),repEnergy(gdx))


### the lines 17 and 26 could also be included in each of the reporting functions
### this duplicates some code (-), but has the advantage, that parts of the reporting can be quickly done and the resulting dataframe is always in IAMC format
### this could be helpful if you plan to do more and more of your model analysis with R anyway

### include the remaining dimensions needed for the IAMC template
dat$model <- "ETAMAC"
dat$scenario <- "Baseline"
### only if your model is not regionally disaggregated, otherwise the regional disaggregation should ideally be taken care of earlier on, so that the dataframes has a regional dimension from the beginning on (analogous to the temporal dimension)
dat$region <- "USA"
### in case you have various sub-national regions and the national total in the dataframe, but only want to report the total, you could select this:
# dat <- dat[dat$region=="USA",]

### convert from long to wide format and reorder the columns: 
dat <- dcast(dat,model+scenario+region+variable+unit~period)

### write out to csv file
write.csv(dat,"ETAMAC_Baseline2.csv",quote=FALSE,row.names=FALSE) 

### alternative call for having the separation of columns by semicolons
# write.csv2(dat,"ETAMAC_Baseline.csv",quote=FALSE,row.names=FALSE) 
