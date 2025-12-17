@echo off
REM Generates three sample tickets for every project/status combination (3 projects × 9 statuses × 3 tickets = 81 files).
REM Set SAMPLE_TICKETS_OUTPUT before running to redirect the output directory (useful for testing).
setlocal EnableExtensions EnableDelayedExpansion

for %%I in ("%~dp0..") do set "workspaceDir=%%~fI"
set "ticketsDir=%workspaceDir%\samples\tickets"

if defined SAMPLE_TICKETS_OUTPUT (
    set "ticketsDir=%SAMPLE_TICKETS_OUTPUT%"
)

if not exist "%ticketsDir%" (
    mkdir "%ticketsDir%"
)

set "projectIds=446993121 -914169199 -342115294"

set "statusList=Backlog ReadyForRefinement InRefinement ReadyForProcessing InProcessing ReadyForTesting InTesting Completed Rejected"
set "status_Backlog=Backlog"
set "status_ReadyForRefinement=Ready for refinement"
set "status_InRefinement=In refinement"
set "status_ReadyForProcessing=Ready for processing"
set "status_InProcessing=In processing"
set "status_ReadyForTesting=Ready for testing"
set "status_InTesting=In testing"
set "status_Completed=Completed"
set "status_Rejected=Rejected"

set "ticketsPerStatus=3"
set /a ticketCount=0

echo Generating sample tickets under "%ticketsDir%"

for %%P in (!projectIds!) do (
    set "projectId=%%~P"
    for %%S in (!statusList!) do (
        call set "statusDisplay=%%status_%%S%%"
        for /l %%K in (1,1,!ticketsPerStatus!) do (
            for /f "usebackq delims=" %%R in (`powershell -NoProfile -Command "Get-Random -Minimum -2147483648 -Maximum 2147483647"`) do set "ticketId=%%R"
            set "statusKey=%%S"
            set "ticketTitle=Sample ticket for project !projectId! - !statusDisplay! (%%K of !ticketsPerStatus!)"
            set "ticketFile=!ticketsDir!\ticket-!projectId!-!statusKey!-%%K.json"
            (
                echo {
                echo     "id": !ticketId!,
                echo     "title": "!ticketTitle!",
                echo     "projectId": !projectId!,
                echo     "status": "!statusDisplay!"
                echo }
            ) > "!ticketFile!"
            if errorlevel 1 (
                echo Failed to write "!ticketFile!"
            ) else (
                set /a ticketCount+=1
            )
        )
    )
)

echo Created !ticketCount! tickets under "!ticketsDir!".
endlocal

