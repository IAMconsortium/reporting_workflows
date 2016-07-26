repMacro <- function(gdx){
  
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
dat <- data.frame(period=time,value=cons$val[,2]*1000,variable="Consumption",unit="billion US$2010")

### The same can be done in a simple fashion for a variety of variables
gdp <- rgdx("fulldata.gdx",list(name="y"))
dat <- rbind(dat, data.frame(period=time,value=gdp$val[,2]*1000,variable="GDP|MER",unit="billion US$2010"))

inv <- rgdx("fulldata.gdx",list(name="i"))
dat <- rbind(dat, data.frame(period=time,value=inv$val[,2]*1000,variable="Investments",unit="billion US$2010"))

inv_en <- rgdx("fulldata.gdx",list(name="ec"))
### not correct, but just to illustrate how certain calculations could be done: 
### here assuming that 2/3 of the energy cost variable ec represents energy supply investments and 1/3 energy demand
dat <- rbind(dat, data.frame(period=time,value=inv_en$val[,2]*1000*2/3,variable="Investments|Energy Supply",unit="billion US$2010"))
dat <- rbind(dat, data.frame(period=time,value=inv_en$val[,2]*1000*1/3,variable="Investments|Energy Demand",unit="billion US$2010"))

return(dat)

}
