@echo off
java "--module-path=%~dp0lib" "--add-modules=javafx.controls,javafx.fxml,javafx.swing,javafx.media,javafx.web" "-Djava.library.path=%~dp0lib" -jar "%~dp0MessageApp.jar"
pause
