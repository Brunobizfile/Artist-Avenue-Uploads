#!/bin/zsh
cd ~/Documents/ArtistAvenue
export PATH_TO_FX=$HOME/javafx-sdk-21.0.2/lib
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.media -cp ".:sqlite-jdbc.jar:slf4j-api-2.0.9.jar:slf4j-simple-2.0.9.jar:." Launcher
