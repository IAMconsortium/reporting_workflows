repEnergy <- function(gdx){

  #define scaling factors
  #bn kwh to ej: 
  pwh_2_ej <- 1/1000*60*60 
  quad_2_ej <- 1.055 #https://www.aps.org/policy/reports/popa-reports/energy/units.cfm 
  
  ### get time set from gdx as vector of numerics : 
  ### due to strange data format coming out of rgdx, this rather cryptic form is required
  ### you can first look at rgdx.set("gdx.gdx","yoursetname") to see how the data looks like
  time <- as.numeric(as.character(rgdx.set(gdx,"t")$i))
  
  
  elec <- rgdx(gdx,list(name="e"))
  elec <- data.frame(period=time,value=elec$val[,2]*pwh_2_ej,variable="Final Energy|Electricity",unit="EJ/yr")

  ### the non-electric energy seems to refer to primary energy (judging from the numbers)
  fe <- rgdx(gdx,list(name="n"))
  fe <- data.frame(period=time,value=fe$val[,2]*quad_2_ej+elec$value,variable="Final Energy",unit="EJ/yr")
  
  dat <- rbind(elec,fe)
  return(dat)

}