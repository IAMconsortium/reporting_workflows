library(gdxrrw)

#define scaling factors
#bn kwh to ej: 
pwh_2_ej <- 1/1000*60*60 
quad_2_ej <- 1.055 #https://www.aps.org/policy/reports/popa-reports/energy/units.cfm 

### get time set from gdx as vector of numerics : 
### due to strange data format coming out of rgdx, this rather cryptic form is required
### you can first look at rgdx.set("gdx.gdx","yoursetname") to see how the data looks like
time <- as.numeric(as.character(rgdx.set("fulldata.gdx","t")$i))

### the same would be done for the regional dimension in a multiregional model, so also for national models, if they feature subnational regional disaggregation
#reg <- as.character(gdx.set("fulldata.gdx","r")$i)

### read in a variable, here consumption, called c in the model
cons <- rgdx("fulldata.gdx",list(name="c"))
### again, rather cryptic output, so we construct a dataframe with right temporal dimension
### plus add variable and unit text and convert to right unit
cons <- data.frame(period=time,value=cons$val[,2]*1000,variable="Consumption",unit="billion US$2010")

### The same can be done in a simple fashion for a variety of variables
gdp <- rgdx("fulldata.gdx",list(name="y"))
gdp <- data.frame(period=time,value=gdp$val[,2]*1000,variable="GDP|MER",unit="billion US$2010")

inv <- rgdx("fulldata.gdx",list(name="i"))
inv <- data.frame(period=time,value=inv$val[,2]*1000,variable="Investments",unit="billion US$2010")

inv_en <- rgdx("fulldata.gdx",list(name="ec"))
### not correct, but just to illustrate how certain calculations could be done: 
### here assuming that 2/3 of the energy cost variable ec represents energy supply investments and 1/3 energy demand
inv_es <- data.frame(period=time,value=inv_en$val[,2]*1000*2/3,variable="Investments|Energy Supply",unit="billion US$2010")
inv_ed <- data.frame(period=time,value=inv_en$val[,2]*1000*1/3,variable="Investments|Energy Demand",unit="billion US$2010")

elec <- rgdx("fulldata.gdx",list(name="e"))
elec <- data.frame(period=time,value=elec$val[,2]*pwh_2_ej,variable="Final Energy|Electricity",unit="EJ/yr")

### the non-electric energy seems to refer to primary energy (judging from the numbers)
fe <- rgdx("fulldata.gdx",list(name="n"))
fe <- data.frame(period=time,value=fe$val[,2]*quad_2_ej+elec$value,variable="Final Energy",unit="EJ/yr")


### once you have the different items, they can be bound together:
dat<- rbind(cons,gdp,inv,inv_es,inv_ed,elec,fe)
### alternatively, you could have used one generic dataframe "dat" that 'grows' over time:
# cons <- rgdx("fulldata.gdx",list(name="c"))
# dat <- data.frame(period=time,value=cons$val[,2]*1000,variable="Consumption",unit="billion US$2010")
# gdp <- rgdx("fulldata.gdx",list(name="y"))
# dat <- rbind(dat, data.frame(period=time,value=gdp$val[,2]*1000,variable="GDP|MER",unit="billion US$2010"))
# ...


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
write.csv(dat,"ETAMAC_Baseline.csv",quote=FALSE,row.names=FALSE) 

### alternative call for having the separation of columns by semicolons
# write.csv2(dat,"ETAMAC_Baseline.csv",quote=FALSE,row.names=FALSE) 

