 @echo off

rem variable settings for calling this batchfile
set PROJECT=%project%
set SCENNAME=%scen%
set WIPPROJECT=%wipProject%
set WIPSCENNAME=%wipScen%

rem fixed settings for programming ease
set rev=%rev%
set WIP_LOCATION=%wipBase%/%WIPPROJECT%/%WIPSCENNAME%
set reporting_location=%RT_home%
set anotherdestination=%RT_data%\Scenario_%SCENNAME%

set JonaLoc=Y:\ontwapps\IMAGE\ScenDevelopment\scenario_lib\SSP_IMAGEv30\26regs_1click_run\3_IMAGE\Scenario_lib\scen

rem goto POSTP

rem go to the export tool location
cd /d %data_exchange%

rem First make sure all old data is removed.
RD %RT_data%\Scenario_%scen% /s /q RD 

rem IMAGE I2RT data
RD %RT_data%\Scenario_%scen%\I2RT /s /q 
rem call exportfromrepos.bat  %rev% %WIP_LOCATION%/I2RT %anotherdestination%/I2RT
Xcopy /Y %JonaLoc%\%scen%\I2RT %RT_data%\Scenario_%scen%\I2RT /s /e /i

rem TIMER T2RT data
RD %RT_data%\Scenario_%scen%\T2RT /s /q 
call exportfromrepos.bat  %rev% %WIP_LOCATION%/T2RT %anotherdestination%/T2RT
rem Xcopy /Y %timer_output%\%scen%\T2RT %RT_data%\Scenario_%scen%\T2RT /s /e /i

rem TIMER T2F data
RD %RT_data%\Scenario_%scen%\T2F /s /q 
call exportfromrepos.bat  %rev% %WIP_LOCATION%/T2F %anotherdestination%/T2F
rem xcopy %timer_output%\%scen%\T2F %RT_data%\Scenario_%scen%\T2F  /s /e /y /i

rem FAIRSCIMAP F2RT data
RD %RT_data%\Scenario_%scen%\F2RT /s /q 
call exportfromrepos.bat  %rev% %WIP_LOCATION%/F2RT %anotherdestination%/F2RT

:COPYSHIT
rem Return home
cd %anotherdestination%

rem unfortunately copying folder contents to get rid of the \exchange folder
Xcopy /Y %anotherdestination%\I2RT\exchange %anotherdestination%\I2RT /s /e /i
Xcopy /Y %anotherdestination%\T2RT\exchange %anotherdestination%\T2RT /s /e /i
Xcopy /y %anotherdestination%\F2RT\exchange %anotherdestination%\F2RT /s /e /i

rem goto END

:POSTP

echo.
echo Data ready, now postprocessing

cd /d %RT_home%\postprocessor_noharm

call run_preprocessor.bat Scenario_%SCENNAME%

rem return home
cd %anotherdestination%

rem return home
cd %anotherdestination%

:END