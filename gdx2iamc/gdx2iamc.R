warning("Last version of script and package is now at https://github.com/lolow/gdx2iamc")

# gdx2iamc.R
# Usage: Rscript gdx2iamc.R, or from Rstudio 
#
# Convert a serie of report gdx files into xls files for submission to iamc DB
# The xlsx files will be locates in the same directory than the template
# Author: laurent.drouet@feem.it
# Version: 07/2016
#
# TODO
# - Use the model registration form for information if available
# - Convert into a package
#
# NOTE:
# If gdxtools does not find your gams installation, 
# add the following lines in the package section 
# library(gdxrrw)
# igdx('/opt/gams24.2/') # with your gams path

# Configuration
MODEL         = "ETAMAC"   # as defined in the registration form
REGIONS       = c('World') # as defined in the registration form
YEARS         = NULL       # by default look into the template
XLSX_TEMPLATE = 'iamc_template/Diagnostics_template_2015-08-12.xlsx'
GDX_GLOB      = '../GAMS_etamac_R/report*.gdx'

# Check if working directory is source directory
if (!is.null(parent.frame(2)$ofile)) {
  if (getwd() != dirname(parent.frame(2)$ofile))
    stop("please set working directory to source directory")
}

source('get_libraries.R')
source('iamc.R')

# Packages
pkgs <- c('data.table', 'stringr', 'foreach', 'openxlsx', 'tools')
res <- lapply(pkgs, require_package)
require_gdxtools()

# Load iamc xls template
template = iamc.template(XLSX_TEMPLATE)

# Load variable definition
iamc.vars = loadvar(template)

# Define the list of gdx to processed
gdxfiles = Sys.glob(GDX_GLOB)
stopifnot(length(gdxfiles)>0)

# Define the scenario name using the gdx name
scenario_name <- function(gdxfile){
  name = basename(gdxfile)
  name = str_sub(name,1,-5)
  return(name)
}

# Get gdx report variables
gdxlist = lapply(gdxfiles, gdx)
gdxlist.items = lapply(gdxlist,all_items)
gdx.vars = unique(unlist(lapply(gdxlist.items, function(x) x$parameters)))
gdx.vrep = gdx.vars[str_detect(gdx.vars,"^rep_")]

# subset gdx report variables
gdx.vrep.to.load = intersect(gdx.vrep,unique(iamc.vars$vrep))

# DEBUG: variable not found
gdx.vrep.not.found = setdiff(gdx.vrep,unique(iamc.vars$vrep))
if(length(gdx.vrep.not.found)>0) print(paste("gdx param not associated:",paste(gdx.vrep.not.found,collapse=", ")))
iamc.vars.not.found = setdiff(unique(iamc.vars$vrep),gdx.vrep)
if(length(iamc.vars.not.found)>0) print(paste("gdx param not found:",paste(iamc.vars.not.found,collapse=", ")))

# Region and year subsets
nrep.keep = REGIONS
if(exists("YEARS") & !is.null(YEARS)){
  year.keep = YEARS
} else {
  year.keep = getyears(template)
}

# load all gdx report variables
gdx.rep = foreach(repvar=gdx.vrep.to.load)%do%{
  print(repvar)
  .data = rbindlist(lapply(gdxlist,extract,repvar,addgdx=T))
  .data[,vrep:=repvar]
  .data[,value:=as.numeric(value)]
  .data[,year:=as.numeric(trep)]
  .data[,trep:=NULL]
  .data = .data[nrep %in% nrep.keep]
  .data = .data[year %in% year.keep]
  names(.data)[1] = 'vsub'
  .data
}
gdx.rep = rbindlist(gdx.rep)

# Identify scenario
gdx.rep[,scenario:=scenario_name(gdx)]

# alldata ! gotcha !
iamc.vars[,vsub:=tolower(vsub)]
iamc.vars[,vrep:=tolower(vrep)]
gdx.rep[,vsub:=tolower(vsub)]
gdx.rep[,vrep:=tolower(vrep)]
alldata = merge(iamc.vars,gdx.rep,by=c("vrep","vsub"),all.x=T)

# add model name
alldata[,model:=MODEL]

# DEBUG: report missing variable names
gdx.var.avail = unique(gdx.rep[,list(vrep,vsub)])
iamc.missing = unique(alldata[is.na(value)])

# save data into xls files
savexls(template,alldata)

