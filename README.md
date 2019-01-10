## dzplugin

This a plugin for [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/) which lets you trade with [Dukascopy](http://www.dukascopy.com). It implements the [Zorro-Broker-API](http://www.zorro-trader.com/manual/en/brokerplugin.htm).

## General installation

1.) Download and install the latest **32-bit** [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Make sure it is the 32-bit version(x86 suffix) since the plugin DLL is a 32-bit library. In case you already have a 32-bit JDK installation(check it with *java -version*) you might skip this step.

2.) Install [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/download.php) if not already on your machine.

3.) Download the [dukascopy.zip](https://github.com/juxeii/dztools/releases/download/v0.9.7/dzjforex-0.9.7.zip) archive.

4.) Extract the archive into *${yourZorroInstallPath}* folder.

5.) You will find the file *zorroDukascopy.bat* in your installation path. Edit it with a text editor and adapt the path to your Java installation. Save the file and run it. Now the dukascopy entry is visible in the drop down box for the brokers.

## Configuration/Usage

After extracting the dztools archive you should see a *dukascopy.dll* and a folder *dukascopy* in the *Plugin* directory of your Zorro installation.

Start Zorro and check if the *Account* drop-down-box shows *Dukascopy* as an available broker.
Pick a script of your choice and press *Trade*. If everything is fine you should see that the login to Dukascopy has been successful.

The plugin stores its logs to *dukascopy/logs/dzplugin.log*(the default log level is *info*). If you encounter problems open *dukascopy/dzplugin/log4j2.xml* for configuring the log level. Then change the log level for the file dzplugin-appender to *debug* and save the file. A new Zorro session will now produce a more verbose *dzplugin.log* file which you can use to report errors.

You can also change the log level for Dukascopy via *dztools/dzplugin/log4j.properties*.

## Remarks
- The following broker commands are supported:
GET_MAXREQUESTS
GET_PRICETYPE
GET_LOCK
SET_PATCH
SET_ORDERTEXT
GET_DIGITS
GET_MAXLOT
GET_MINLOT
GET_MARGININIT
GET_TRADEALLOWED
GET_TIME
GET_MAXTICKS
GET_SERVERSTATE
GET_ACCOUNT
SET_HWND
SET_SLIPPAGE
SET_LIMIT

- Plugin triggers Zorro on each tick change(thank AndrewAMD!) to get a high tick resolution
- You can adapt the plugin.maxticks parameter in the Plugin.properties file. The higher the value the more data is fetched from Dukascopy when you want to download bars/ticks.
