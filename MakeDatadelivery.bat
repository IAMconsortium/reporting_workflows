

set PATH= S:\Angelica\IAMCDeliveryTool\;%PATH%

rem syntax of call to reportting tool;
REM location\Java -jar NameofTool.jar -t LocationofInputTemplate.xls -m LocationOfMapping.xls
REM  -s ScenName -s ScenName -b LocationOfScenarioWithPrefix_ -f LocationofOutput.xls


"C:\Program Files (x86)\Java\jre6\bin\java" -jar PblDeliveryTool.jar -t SSPDB__IAM_data_template_2015-05-05.xls -m supermapping_TIMER2015.xls -s %scen% -b data\Scenario_ -f %scen%.xls

