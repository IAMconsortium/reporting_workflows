:: Purpose: export the data from revision of repositoryURL to destination
:: Note: this will overwrite existing data
:: Put versioninfo into textfile and copy this file to desination  
:: If WIP repository then the exchangefolder (T2I, etc) contains an external 
:: reference to the URL of the referenced repository and revison number. 
:: These will be put into versioninfo.txt
:: Precondition: parameters with valid values.

:: Parameters:
:: 1 revision number
:: 2 URL of repository to export
:: 3 destination for export

set rev=%1
set reposURL=%2
set destination=%3

call setEnvVars

::get the foldername by substituting everything before and incl. the last '/' with empty string 
for /F "usebackq" %%i in (`echo %reposURL%^|sed s_^.*/__`) do set FLDR=%%i
set InfoFile=versionInfo_%USERNAME%_%FLDR%.txt

:: version info in file
echo.
echo Version info requested revision of repository > %TMP%\%InfoFile%
echo URL: %reposURL% >> %TMP%\%InfoFile%
echo Destination: %destination% >> %TMP%\%InfoFile%
echo. >> %TMP%\%InfoFile%
echo versionInfo: >> %TMP%\%InfoFile%
svn info %reposURL%@%rev% >> %TMP%\%InfoFile%
if %ERRORLEVEL% equ 0 goto TESTREPOS
echo error in info %reposURL%@%rev%
GOTO ERROR

:TESTREPOS
echo.
echo  check whether WIP repository
set str=
for /F "usebackq tokens=1-3* delims=/" %%i in (`echo %reposURL%`) do set str=%%k 
if %str%==%wipRepos% echo Yes, it is the Wip repository & goto GETPROP
echo  not the Wip repository
goto EXPORT

:GETPROP
echo. >> %TMP%\%InfoFile%
:: get the property
echo.
echo  getting external reference property on %reposURL%
svn propget svn:externals -r%rev% %reposURL% >>%TMP%\%InfoFile%

:EXPORT 
:: export the URL

echo.
echo  export of -r%rev% %reposURL% to %destination%
svn export --force -r%rev% %reposURL% %destination%
if %ERRORLEVEL% gtr 0 goto ERROR

copy %TMP%\%InfoFile% %destination%
del /Q %TMP%\%InfoFile%
goto END

:ERROR 
echo  ERROR, something went wrong in the export...
exit /b 1

:END
echo  end of exportFromRepos


