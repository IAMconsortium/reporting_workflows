# iiasadb.R define S3 methods to describes and manipulate the iiasa db template file
#require(openxlsx)
#require(data.table)
#require(tools)

#' @export
iiasa.template <- function(filename, ...) {
  structure(list(filename = filename, ...), class = "iiasa.template")
}

#' @export
print.iiasa.template <- function(x, ...) {
  cat("<IIASA template: ", x$filename, ">\n", sep = "")
}

#' @export
loadvar <- function(x, ...) {
  UseMethod("loadvar",x)
}

#' @export
# load the variable definition from the template
loadvar.iiasa.template <- function(x, sheetvar="variable definitions",
                                   colvar=NA, colunit=NA, firstrow=2,
                                   guess_var=T) {
  
  # open template workbook
  wb <- loadWorkbook(file=x$filename)
  
  # find sheetvar (allow for mistyping)
  sheets = names(wb)
  sheetidx = agrep(sheetvar,sheets)

  # find useful columns
  header = read.xlsx(x$filename,sheetidx,rows=c(1,1),colNames=F)
  if(is.na(colvar)) colvar = agrep("Variable",header)
  if(length(colvar)==0) stop("loadvar error: variable column in template has not been found, please provide colvar")
  if(length(colvar)>1) stop("loadvar error: cannot detect the 'variable' column as header names are ambiguous, please provide colvar")
  if(is.na(colunit)) colunit = agrep("Units",header)
  if(length(colunit)!=1)  stop("loadvar error: unit column in template has not been found or header names are ambiguous, please provide colunit")
 
  # Load vars
  variables = read.xlsx(x$filename,sheet=sheetidx,startRow=firstrow,cols=colvar,colNames=F)
 
  units = read.xlsx(x$filename,sheetidx,startRow=firstrow,cols=colunit,colNames=F)
  xls.var = data.table(var=as.character(variables[,1]),
                       unit=as.character(units[,1]))
  print(paste("loading",nrow(xls.var),"variables"))
  
  if(!guess_var) return(xls.var)
  
  # guess WITCH variables
  witch.var = rbindlist(lapply(variables[,1],guess_gdx_var))
  witch.var[] <- lapply(witch.var, as.character)

    return(cbind(xls.var,witch.var))
}

# register function
getyears <- function(x, ...) {
  UseMethod("getyears",x)
}

# load the variable definition from the template
getyears.iiasa.template <- function(x, sheetdata="data") {
  
  # open template workbook
  wb <- loadWorkbook(file=x$filename)
  
  # read header
  sheets = names(wb)
  sheetidx = agrep(sheetdata,sheets)
  header = read.xlsx(x$filename,sheetidx,rows=c(1,1),colNames=F)
  
  # find years
  header = suppressWarnings(as.numeric(header))
  header = header[!is.na(header)]
  years = header[header>1000]

  return(years)
}

# register function
savexls <- function(x, ...) {
  UseMethod("savexls",x)
}

savexls.iiasa.template <- function(x, .data, sheetdata="data", addtimestamp=T, keepNA=F){
  
  # keep only useful column
  .data = .data[,list(model,scenario,nrep,var,unit,year,value)]
  
  # manage no data
  nodata = .data[is.na(year)]
  .data = .data[!is.na(year)]
  missing = expand.grid(year=unique(.data$year),
                        nrep=unique(.data$nrep),
                        scenario=unique(.data$scenario))
  nodata = nodata[,list(year=missing$year,
                        scenario=missing$scenario,
                        nrep=missing$nrep,
                        value=NA),by=list(model,var,unit)]
  .data = rbind(.data, nodata)
  
  # put region in capital letters [Except world]
  .data[nrep != "World", nrep := toupper(nrep)]
  
  # spread the years
  tabdata = dcast(.data, model+scenario+nrep+var+unit ~ year, fun.aggregate=sum, value.var="value")
  
  # Manage NA values
  if(!keepNA){
    nbyears = length(unique(.data$year))
    tabdata = as.data.table(tabdata[rowSums(is.na(tabdata))!=nbyears,])
  }
  tabdata[is.na(tabdata)] <- "N/A"

  # find parts that not split scenario [to limit size file]
  maxrowfile = 15000
  tsize = tabdata[,.(nrow=nrow(.SD)),by="scenario"]
  tsize[,idx:=as.integer(cumsum(nrow)/maxrowfile)+1]
  idxpart = merge(tabdata[,.(scenario)],tsize[,.(scenario,idx)],by="scenario")$idx

  # open template workbook
  wb <- loadWorkbook(file=x$filename)
  
  # read header
  sheets = names(wb)
  sheetidx = agrep(sheetdata,sheets)
  header = read.xlsx(x$filename,sheetidx,rows=c(1,1),colNames=F)
  
  # specify header
  names(tabdata) <- c("Model","Scenario","Region","Variable","Unit",paste(unique(.data$year)))
  
  for(i in 1:max(idxpart)) {
    
    # create workbook
    wb <- createWorkbook(creator="gdx2iiasa")
    
    # add data worksheet
    addWorksheet(wb,sheetdata)

    # write data
    writeData(wb, 1, tabdata[idxpart==i,],
              startCol = 1, startRow = 1,
              rowNames = F, colNames = T, keepNA = T)

    # save into new excel file
    idname = paste0("_part",i)
    if(addtimestamp) idname = paste0(idname,format(Sys.time(), "_%y-%m-%d_%H-%M-%OS"))
    newname = paste0(file_path_sans_ext(x$filename),idname,".",file_ext(x$filename)) 
    
    # Save Workbook
    saveWorkbook(wb, newname, overwrite = TRUE)
    
  }
  
}


# Guess the GAMS variable from the IIASA variable
# vsub: name of the GAMS parameters (= first level of IIASAdb variable)
# vrep: name of the first GAMS index (= remaining levels of IIASAdb variable)
# some example
# IIASA Variable      GAMS parameter
# Emissions|CO2       rep_emissions('CO2',time,region)
# Forcing|Aerosol|BC  rep_forcing('Aerosol|BC',time,region)
guess_gdx_var <- function(iiasa.var){
  stopifnot(nrow(iiasa.var)==1)
  vparts = unlist(str_split(iiasa.var,"\\|"))
  lv = length(vparts)
  if(lv==1){
    vsub='Total'
  } else {
    vsub=paste(vparts[2:lv],collapse="|") 
  }
  return(list(vrep=str_c("rep_",str_replace(tolower(vparts[1]),' ','_')),
              vsub=vsub))
}


