warning("Last version of script and package is now at https://github.com/lolow/gdx2iamc")
# gdx2iamc.R
# Usage: Rscript gdx2iamc.R, or from Rstudio 
#
# Convert a serie of report gdx files into xls files for submission to iamc DB
# The xlsx files will be locates in the same directory than the template
# Author: laurent.drouet@feem.it
# Version: 07/2016
#
# NOTE:
# If gdxtools does not find your gams installation, 
# add the following lines in the package section 
# library(gdxrrw)
# igdx('/opt/gams24.2/') # with your gams path

## REQUIREMENTS

if(!"gdx2iamc" %in% rownames(installed.packages())){
  library(devtools)
  install_github('lolow/gdx2iamc')
}
if(packageVersion("gdxtools")<numeric_version("0.2.0")){
  stop("You need to install a newer version of gdx2iamc (>=0.2.0). Please run remove.packages('gdx2iamc'), restart R and rerun this script.")
}
library(gdx2iamc)
library(gdxtools)
library(data.table)
library(stringr)

## PARAMETERS
#Absolute folder where the gdx files are located
templatedir = "iamc_template"
templatefile = "Diagnostics_template_2015-08-12.xlsx"
model_name = "ETAMAC"
convert_usd =  1.10774 # Factor to convert USD2005 into USD2010
max_row_xlsx = 15000 # Maximum number of rows in an excel file (approx 3MB)
# NREP is case-sensitive. It should have the same case as in the registration template
nrep.keep = c('World')
# Convert GDX filename in scenario name
scenario_name <- function(gdxfile){
  name = basename(gdxfile)
  name = str_sub(name,str_length("db_")+1,-5)
  return(name)
}

## PROCESS

# Load IIASA xls template
template = iamc.template(file.path(templatedir, templatefile))

# Load variable definition
iamc.vars = load_var(template, convert_usd = usd2005_2010)

# list of gdx
gdxfiles = Sys.glob(file.path(templatedir, 'db*.gdx'))

#Load data from the GDX files and store it in gdx.data
gdx.data = load_gdx(template, gdxfiles, nrep.keep, scen_func=scenario_name)

#Save data into xls files
res = save_xls(template,gdx.data, iamc.vars, model_name, maxrowfile=max_row_xlsx)
