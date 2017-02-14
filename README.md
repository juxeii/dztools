## dzplugin

This a plugin for [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/) which lets you trade with [Dukascopy](http://www.dukascopy.com). It implements the [Zorro-Broker-API](http://www.zorro-trader.com/manual/en/brokerplugin.htm) and provides a script for fast downloading/converting the Dukascopy history data.

## General installation

1.) Download and install the latest **32-bit** [Java JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). Make sure it is the 32-bit version(x86 suffix) since the tools are based on a 32-bit JVM. In case you already have a 32-bit JRE installation(check it with *java -version*) you might skip this step.

2.) Add *${yourJREinstallPath}\jre\bin\* and *${yourJREinstallPath}\jre\bin\client* to the **front** of your *Path* environment variable([here](http://www.computerhope.com/issues/ch000549.htm) is a howto).

3.) Install [Zorro 1.26](http://www.takemoneyfromtherichandgiveittothepoor.com/download.php) if not already on your machine.

4.) Download the [dukascopy.zip](https://github.com/juxeii/dztools/releases) archive.

5.) Extract the archive into *${yourZorroInstallPath}* folder.

## Configuration/Usage

After extracting the dztools archive you should see a *dukascopy-{version}.dll* and a folder *dukascopy* in the *Plugin* directory of your Zorro installation.

Go to the *dukascopy* folder and open the *DZPluginConfig.properties* file with a text editor.

Here you should adapt the *.cache* path to your local JForex installation path. Be careful with this step, since it may happen that the [Dukascopy API](http://www.dukascopy.com/client/javadoc/com/dukascopy/api/system/IClient.html#setCacheDirectory%28java.io.File%29) **will delete the .cache folder if it is corrupted**. Please make a copy of an instrument to a different location and set the path accordingly. If nothing gets deleted then you can use your complete *.cache* directory.
You can leave the other entries to their default values.

Start Zorro and check if the *Account* drop-down-box shows *Dukascopy* as an available broker.
Pick a script of your choice and press *Trade*. If everything is fine you should see that the login to Dukascopy has been successful.

For downloading history data you can use either use the *download.c* script which ships with Zorro or use the *DukascopyDownload.c* which is extracted into the *Strategy* folder.

I recommend using this dedicated script, since it is much faster if your *.cache* folder already contains the Dukascopy history(on my 8 year old machine a complete 1min Bar year takes 3 seconds!).
You can adapt the SAVE_PATH symbol to define a separate history folder for Dukascopy. This avoids re-downloading histories if you want to test strategies using different brokers. You just copy then the *.bar* files to the *History* folder as needed.

The plugin stores its logs to *dukascopy/logs/dzplugin.log*(the default log level is *info*). If you encounter problems open *dukascopy/dzplugin/log4j2.xml* for configuring the log level. Then change the log level for the file dzplugin-appender to *debug* and save the file. A new Zorro session will now produce a more verbose *dzplugin.log* file which you can use to report errors.

You can also change the log level for Dukascopy via *dztools/dzplugin/log4j.properties*.

Please use [pastebin](http://pastebin.com/) for uploading the logs.

## Remarks

- This a very early release so **don't expect it to be bug free!**
- Currently **only Forex** is supported(no Stocks, CFDs etc.)
- The history downloading is sometimes not reliable; just repeat the conversion in case of errors
- Login to a real account for the plugin is currently not supported(although the code is in place).
- If you don't trust the binaries checkout the dztools project und build it manually(you need to know [maven](http://maven.apache.org/))
- Follow discussions for this project on the [forum](http://www.opserver.de/ubb7/ubbthreads.php?ubb=showflat&Number=447697&#Post447697)
