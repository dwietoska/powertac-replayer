# powertac-replayer

## Introduction

The project "Replayer" was created by D. Wietoska under supervision of W. Conen at the Westfälische Hochschule in Gelsenkirchen (Germany).

It is a web front-end which can replay finished games of the simulation platform Power TAC (http://www.powertac.org/).

The replayer was based on the excellent Power TAC visualizer (https://github.com/powertac/visualizer).

The package "org.powertac.replayer" and its subdirectories contain the new developed replayer classes.
The package "org.powertac.visualizer" and its subdirectories contain the visualizer classes from Power TAC visualizer which was used in replayer package.

## Getting started

In directory "powertac-replayer/visualizer" you can build the replayer by "mvn clean install" and run it with "mvn jetty:run". 

If you are playing games in parallel with a local server, you may want to use "mvn -Djetty.port=8081 jetty:run" instead to avoid port conflicts.

If everything works well, the message "Jetty Server started" will appear in your console output. 


Replayer will be located at localhost:8080/visualizer/ (or at localhost:8081/visualizer/).

The replayer requires the ".state" or ".tar.gz" log files.These files can be uploaded through a File Upload-component or over the webflow "replayer". The webflow "replayer" expects a log file url
(for example "http://localhost:8080/visualizer/app/replayer?logurl=http://wolf-08.fbk.eur.nl/log/game-128-sim-logs.tar.gz").

Use and enjoy at your own risk! ;)
