# Check R version (>=3.2.0)
if((as.numeric(version$major)<3) | (as.numeric(version$major)==3)&(as.numeric(version$minor)<2) ){
  stop('Please use R with a version > 3.2.0 to run the script')
}

# default loading (from R CRAN)
require_package <- function(package){
  if(!suppressMessages(suppressWarnings(require(package, character.only = TRUE, quietly = TRUE)))) {
    try(install.packages(package, repos="http://cran.r-project.org"), silent = TRUE)
    suppressPackageStartupMessages(library(package,character.only=T, quietly = TRUE))
  } 
}

require_gdxtools <- function(){
  # gdxrrw
  if(!suppressMessages(suppressWarnings(require(gdxrrw, quietly = TRUE)))){
    if (Sys.info()['sysname']=="Windows") {
      require_package("reshape2")
      download.file("https://support.gams.com/_media/gdxrrw:gdxrrw_0.5.4.zip","gdxrrw_0.5.4.zip",method="curl")
      install.packages("gdxrrw_0.5.4.zip",repos=NULL)
    } else {
      require_package("reshape2")
      download.file("https://support.gams.com/_media/gdxrrw:gdxrrw_0.5.4.tar.gz","gdxrrw_0.5.4.tar.gz",method="curl")
      install.packages("gdxrrw_0.5.4.tar.gz",repos=NULL)
    }
    suppressPackageStartupMessages(library(gdxrrw, quietly = TRUE))
  }
  
  # gdxtools
  if(!suppressMessages(suppressWarnings(require(gdxtools, quietly = TRUE)))){ 
    require_package("devtools")
    require_package("Rcpp")
    install_github('lolow/gdxtools')
    suppressPackageStartupMessages(library(gdxtools, quietly = TRUE))
  }
  
  if(packageVersion("gdxtools")<numeric_version("0.3.4")){
    stop("You need to install a newer version of gdxtools (>=0.3.4). Please run remove.packages('gdxtools', restart R and rerun this script.")
  }
}