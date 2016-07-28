$ontext
This script shows how to produce a GDX to be processed by gdx2iiasa.R
The example follows the repMacro.R script
$offtext

$setglobal outgdx 'fulldata'

* load fulldata.gdx content
$call =gdxdump "%outgdx%.gdx" NoData Output="%outgdx%.txt"
$include "%outgdx%.txt"
$call rm "%outgdx%.txt"

* reporting sets
alias(t,trep);
set nrep / 'World' /;

* Units: billion US$2010
parameter rep_consumption(*,trep,nrep);
parameter rep_gdp(*,trep,nrep);
parameter rep_investments(*,trep,nrep);

rep_consumption('Total',t,'World') = c.l(t)*1000;

rep_gdp('MER',t,'World') = y.l(t)*1000;

rep_investments('Total',t,'World') = i.l(t)*1000;
rep_investments('Total|Energy Supply',t,'World') = i.l(t)*1000*2/3;
rep_investments('Total|Energy Demand',t,'World') = i.l(t)*1000*1/3;

execute_unload 'report.gdx'
rep_consumption
rep_gdp
rep_investments
;