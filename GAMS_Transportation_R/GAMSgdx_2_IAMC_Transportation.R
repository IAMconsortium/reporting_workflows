# Template workflow from GAMS gdx results to the IAMC Excel template
#
# This example is based on:
#
# A Transportation Problem - a toy example for illustrative purposes
#
# Dantzig, G B, Chapter 3.3. In Linear Programming and Extensions.
# Princeton University Press, Princeton, New Jersey, 1963.
#
# see http://www.gams.com/mccarl/trnsport.gms

# load the libary
library(gdxrrw)

# assume that the year of the transportation problem is 2010
year <- 2010

# get the objective value from the gdx file
z <- rgdx("transportation_dump.gdx",list(name="z"))
report <- data.frame(region="World",period=year,value=z$val,variable="Total cost",unit="$")

# get the total quantity produced (sum of x)
x <- rgdx("transportation_dump.gdx",list(name="x"))
newRow <- data.frame(region="World",period=year,value=sum(x$val[,3]),variable="Production",unit="cases")
report <- rbind(report,newRow)

# loop over the origin nodes and compute the sum of shipments from a node
for (i in 1:length(x$uels[[1]])) {
    node <- x$uels[[1]][i]
    value <- sum(x$val[which(x$val[,1]==i),3])
    newRow <- data.frame(region=node,period=year,value=value,variable="Production",unit="cases")
    report <- rbind(report,newRow)
}

# loop over both origin and destination nodes and compute shipment values node-to-node
for (i in 1:length(x$uels[[1]])) {
    node <- x$uels[[1]][i]
    for (j in 1:length(x$uels[[2]])) {
        value <- sum(x$val[intersect(which(x$val[,1]==i),which(x$val[,2]==j)),3])
        key = paste("Shipment",x$uels[[2]][j],sep="|")
        newRow <- data.frame(region=node,period=year,value=value,variable=key,unit="cases")
        report <- rbind(report,newRow)
    }
}

# get the values of the demand parameter b
b <- rgdx("transportation_dump.gdx",list(name="b"))

# loop over demand nodes and write the demand value to the 
for (i in 1:length(b$uels[[1]])) {
    node <- b$uels[[1]][i]
    key = paste("Demand",b$uels[[1]][j],sep="|")
    newRow <- data.frame(region=node,period=year,value=sum(x$val[i,3]),variable=key,unit="cases")
    report <- rbind(report,newRow)
}

# assign the model and scenario name
report$model <- "Transportation"
report$scenario <- "Base"

# convert from long to wide format and reorder the columns 
report <- dcast(report,model+scenario+region+variable+unit~period)

# write the report to a csv file
write.csv(report,"IAMC_transportation.csv",quote=FALSE,row.names=TRUE) 


