# Reporting results of the ETAMAC example to IAMC-style Excel sheets

This repository illustrates a reporting workflow from GAMS to the IAMC table structure using R.
It is based on the ETAMAC model, which is one of the examples provided in the GAMS library.  

First, run the GAMS code to obtain a gdx file `fulldata.gdx` with all model results. This file is then read by one of two alternative R scripts for the reporting, "..._alt1" and "...alt2". 

The former has the advantage of having all code in one file and directly writing out a csv file `ETAMAC_Baseline.csv`, which might be easier to grasp at the beginning.

For doing a complete reporting workflow, however, it would be very much recommended to use the approach illustrated in alt2, which is splitting up the reporting variables into different blocks that each have a separate reporting function. The reporting script alt2 script then just serves for calling those functions and binding the stuff together, plus writing out a csv file `ETAMAC_Baseline2.csv`.

## Credits

This code snipped was provided by Christoph Bertram at the Potsdam Institute for Climate Impact Research.