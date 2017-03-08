## dzplugin

This a plugin for [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/) which lets you trade with [Dukascopy](http://www.dukascopy.com). It implements the [Zorro-Broker-API](http://www.zorro-trader.com/manual/en/brokerplugin.htm).

## General installation

1.) Download and install the latest **32-bit** [Java JRE](http://www.oracle.com/technetwork/java/javase/downloads). Make sure it is the 32-bit version(x86 suffix) since the plugin DLL is a 32-bit library. In case you already have a 32-bit JRE installation(check it with *java -version*) you might skip this step.

2.) Add *${yourJREinstallPath}\jre\bin\* and *${yourJREinstallPath}\jre\bin\client* to the **front** of your *Path* environment variable([here](http://www.computerhope.com/issues/ch000549.htm) is a howto).

3.) Install [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/download.php) if not already on your machine.

4.) Download the [dukascopy.zip](https://github.com/juxeii/dztools/releases) archive.

5.) Extract the archive into *${yourZorroInstallPath\Plugin}* folder.

## Configuration/Usage

After extracting the dztools archive you should see a *dukascopy.dll* and a folder *dukascopy* in the *Plugin* directory of your Zorro installation.

Go to the *dukascopy* folder and open the *Plugin.properties* file with a text editor.

Here you should adapt the *.cache* path to your local JForex installation path. Be careful with this step, since it may happen that the [Dukascopy API](http://www.dukascopy.com/client/javadoc/com/dukascopy/api/system/IClient.html#setCacheDirectory%28java.io.File%29) **will delete the .cache folder if it is corrupted**. Please make a copy of an instrument to a different location and set the path accordingly. If nothing gets deleted then you can use your complete *.cache* directory.
You can leave the other entries to their default values.

Start Zorro and check if the *Account* drop-down-box shows *Dukascopy* as an available broker.
Pick a script of your choice and press *Trade*. If everything is fine you should see that the login to Dukascopy has been successful.

The plugin stores its logs to *dukascopy/logs/dzplugin.log*(the default log level is *info*). If you encounter problems open *dukascopy/dzplugin/log4j2.xml* for configuring the log level. Then change the log level for the file dzplugin-appender to *debug* and save the file. A new Zorro session will now produce a more verbose *dzplugin.log* file which you can use to report errors.

You can also change the log level for Dukascopy via *dztools/dzplugin/log4j.properties*.

Please use [pastebin](http://pastebin.com/) for uploading the logs.

## Remarks

- This a very early release so **don't expect it to be bug free!**
- Currently **only Forex** is supported(no Stocks, CFDs etc.)
- The history downloading from Dukascopy servers is sometimes not reliable; just try again in case of errors
- Follow discussions for this project on the [forum](http://www.opserver.de/ubb7/ubbthreads.php?ubb=showflat&Number=447697&#Post447697)
