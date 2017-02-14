/////////////////////////////////////////////////////////////////////
// Trading functions
// (c) oP group 2010
/////////////////////////////////////////////////////////////////////
#ifndef functions_h
#define functions_h

// trade functions
TRADE* enterLong(function f,	// buy a long position
  var v0,var v1,var v2,var v3,var v4,var v5,var v6,var v7,...);
TRADE* enterShort(function f,	
  var v0,var v1,var v2,var v3,var v4,var v5,var v6,var v7,...);
void exitLong(string name,var Limit,int lots,...);	// sell all open long positions
void exitShort(string name,var Limit,int lots,...); // sell all open short positions
void exitTrade(TRADE* tr,var Limit,int lots,...); // sell a particular trade position
TRADE* forTrade(int mode);	// find the next trade in a loop
TRADE* findTrade(string name);	// find the first trade with the given info
void cancelTrade(int id);	// remove an externally closed trade from the list

#define ALL		"*"			// all algos, all trades etc.

var adviseLong(int method,var prediction,
	var s0,var s1,var s2,var s3,var s4,var s5,var s6,var s7,var s8,var s9,
	var s10,var s11,var s12,var s13,var s14,var s15,var s16,var s17,var s18,var s19,...);
var adviseShort(int method,var prediction,
	var s0,var s1,var s2,var s3,var s4,var s5,var s6,var s7,var s8,var s9,
	var s10,var s11,var s12,var s13,var s14,var s15,var s16,var s17,var s18,var s19,...);

#define NEURAL		(1<<20)	// use external AI
#define DTREE		(1<<21)	// use a decision tree
#define PERCEPTRON (1<<22)	// use a perceptron
#define PATTERN	(1<<23)	// use pattern match
#define FAST		(1<<24)	// match fast
#define FUZZY		(1<<25)	// match fuzzy
#define SIGNALS	(1<<26)	// export signals + objective to .csv
#define BALANCED	(1<<28)	// balance positive and negative results

#define NEURAL_INIT		(1<<20)	// neural function modes
#define NEURAL_EXIT		(2<<20)
#define NEURAL_LEARN		(4<<20)
#define NEURAL_TRAIN		(5<<20)
#define NEURAL_PREDICT	(8<<20)
#define NEURAL_SAVE		(16<<20)
#define NEURAL_LOAD		(17<<20)

int predict(int type,vars Data,int TimePeriod,var Threshold);

#define CROSSOVER		(1<<21)
#define PEAK			(1<<22)
#define VALLEY			(1<<23)
#define PARABOLIC		(1<<20)

int algo(string Name);		// set up an algo identifier
int asset(string Name);		// fill price curves from an asset with the given name
int assetType(string Name);	// type of asset
void assetAdd(string Name,
	var vPrice,var vSpread,var vRollLong,var vRollShort, 
	var vPip,var vPipCost,var vMarginCost,var vLeverage,
	var vLotAmount,var vCommission,string sSymbol);
int assetList(string Filename);
void assetHistory(string Name,int Mode);

#define FROM_YAHOO (1<<12)

#define FOREX		1
#define INDEX		2	// stock index
#define CMDTY		3
#define STOCK		4
#define BOND		5

var price(int offset,...);
var priceOpen(int offset,...);
var priceClose(int offset,...);
var priceHigh(int offset,...);
var priceLow(int offset,...);
var marketVal(int offset,...);
var marketVol(int offset,...);

// Detrend + randomize + Optimize settings
#define TRADES		1	
#define PRICES		2 
#define CURVE		4
#define DETREND	4
#define INVERT		8
#define NOPRICE	(1<<4)
#define SHUFFLE	(1<<5)
#define BOOTSTRAP	(1<<6)
#define PHANTOM	(1<<7)
//#define PEAK		(1<<22)


// date/time
int timeOffset(int zone,int days,int hour,int minute);	
#define UTC		24	// Coordinated Universal Time
#define WET		0	// Western European Time (London)
#define CET		1	// European time zone (Frankfurt)
#define ET		-5	// Eastern Time (New York)
#define JST		9	// Japan Standard Time (Tokyo)
#define AEST	10	// Australian Eastern Standard Time (Sydney)

#define MONDAY		1
#define TUESDAY	2
#define WEDNESDAY	3
#define THURSDAY	4
#define FRIDAY		5
#define SATURDAY	6
#define SUNDAY		7

#define NOW		-999999

int year(int offset,...);	// current year of the simulation
int month(int offset,...);	// current month of the simulation, 1 = January
int week(int offset,...);	// current week number 
int day(int offset,...);	// current day (1..31)
int dom(int offset,...);	// number of days of the current month, 28..31
int tdm(int offset,...);	// trading day of the current month, 1..23
int tom(int offset,...);	// number of trading days of the current month, 20..23
int dow(int offset,...);	// current day of the week: 1 = Monday, to 7 = Sunday.
int ldow(int zone, int offset,...); // local day of the week
int hour(int offset,...);	// current hour
int lhour(int zone,int offset,...);	// local hour in the given time zone
int minute(int offset,...);	// current minute
var second();					// current second
int dst(int zone,int offset,...);		// daylight saving (1 or 0)
BOOL workday(int offset,...);
BOOL frame(int offset);
int frameSync(int Period);
int minutesAgo(int offset);
var minutesWithin(int offset);
BOOL market(int zone,int offset);
int date(int offset);
int tod(int offset);
int ltod(int zone,int offset);
int tow(int offset);
int ltow(int zone,int offset);
var wdate(int offset);

// chart
void plot(string name,var val,int type,int color); // plot a value into the chart
void plotBar(string name,int num,var label,var val,int type,int color);
void plotGraph(string name,var num,var val,int type,int color);
DATA* plotData(string name);
int color(var value, int color1,int color2,int color3,int color4,...);

#define NEW		(1<<0)	// begin new chart
#define BARS	(1<<1)	// bars instead of curves
#define BAND1	(1<<2)	// upper band
#define BAND2	(BAND1+1)	// lower band
#define MAIN	(1<<3)	// print in main window
#define STATS	(1<<4)	// print statistics
#define AXIS2	(1<<5)	// plot on 2nd axis
#define SUM		(1<<6)	// add values
#define AVG		(1<<7)	// calculate average
#define LOG		(1<<8)	// use logarithmic scale
#define DEV		(1<<9)	// calculate standard deviation
#define NRM		(1<<10)	// normalize to 1
#define MINV	(1<<12)	// plot the bar minimum
#define MAXV	(1<<13)	// plot the bar maximum
#define DEL		(1<<14)	// delete the previous plot and start over
#define LBL2	(1<<15)	// label only even bars
#define GRAPH	(1<<16)	// plot line/dot graph
#define LINE	(1<<17)	// plot a straight line
#define END		(1<<18)	// line end point
#define DOT		(1<<19)	// plot a colored dot
#define SQUARE	(DOT+(1<<20))
#define DIAMOND (DOT+(2<<20))
#define TRIANGLE (DOT+(3<<20))
#define TRIANGLE2 (DOT+(4<<20))
#define TRIANGLE3 (DOT+(5<<20))
#define TRIANGLE4 (DOT+(6<<20))
#define CROSS	(DOT+(8<<20))
#define CROSS2	(DOT+(9<<20))

#define RED			0xff0000
#define GREEN		0x00ff00
#define BLUE		0x0000ff
#define CYAN		0x00ffff
#define DARKBLUE	0x0000a0
#define LIGHTBLUE	0xadd8e6
#define PURPLE		0x800080
#define YELLOW		0xffff00
#define MAGENTA	0xff00ff
#define ORANGE		0xffa500
#define DARKGREEN	0x008000
#define OLIVE		0x808000
#define MAROON		0x800000
#define SILVER		0xc0c0c0
#define GREY		0x808080
#define BLACK		0x010101
#define LIGHT		0x606060
#define TRANSP		0x80000000

// filters
var Median(var* Data,int Length);
var Percentile(var* Data,int Length,var Percent);
var Gauss(var* Data,int Length);
var DominantPeriod(var* Data,int Period);
var DominantPhase(var* Data,int Period);
extern var rDominantPeriod,rDominantPhase;
var HighPass1(var* Data,int Cutoff);
var HighPass(var* Data,int Cutoff);
var LowPass(var* Data,int Cutoff);
var BandPass(var* Data,int Period,var Delta);
var Spectrum(var* Data,int TimePeriod,int SamplePeriod);

// fuzzy
BOOL fuzzy(var a);
BOOL eq(var a,var b);
var betweenF(var a,var l,var h);
var andF(var a,var b);
var orF(var a,var b);
var notF(var a);
var equalF(var a,var b);
var aboveF(var a,var b);
var belowF(var a,var b);
var peakF(var* a);
var valleyF(var* a);
var risingF(var* a);
var fallingF(var* a);
var crossOverF(var* a,var* b);
var crossUnderF(var* a,var* b);

// various analysis
var* series(var value,int length,...);
var* rev(var* Data,int length,...);
var random();	// random value between -1 and 1
void seed(int s);
var randomize(int Method,var *Out,var *In,int Length);
var timer();	// elapsed time in us
var round(var val,var step);
var cdf(var x);
var diff(var val);
BOOL peak(var* a);
BOOL valley(var* a);
BOOL crossOver(var* a,var* b);
BOOL crossUnder(var* a,var* b);
BOOL rising(var* a);
BOOL falling(var* a);
var concave(var* a);
void sortData(var* Data,int length);
int* sortIdx(var* Data,int length);
var center(var x,int TimePeriod);
var compress(var x,int TimePeriod);
var scale(var x,int TimePeriod);
var normalize(var x,int TimePeriod);
var zscore(var x,int TimePeriod);
var polyfit(var* a,var* Data,int TimePeriod,int n,var weight);
var polynom(var* a,int n);
var detect(var* Data,int TimePeriod,var factor,var* pattern);
var frechet(var* Data,int TimePeriod,var factor,var* pattern);
var markowitz(var* CovMatrix,var* Mean,int N,double Cap,...);
var markowitzReturn(var* Weights,var Variance);
var markowitzVariance(var* Weights,var Return);

// various i/o
var slider(int num,int pos,int min,int max,char* name,char* tooltip,...);
var optimize(var val,var start,var end,var step,var tolerance);
void* loop(void* p1,...); // loop through the parameter list
void sound(string filename);	// play sound in trade mode
HWND window(string title);
void keys(string format,...);
int hit(int key);
int mouse(int* x,int* y,HWND hwnd);
int exec(string name,string args,int mode);
int print(int to,string format,...);
int msg(string format,...);
void watch(string text,...);
void title(string format,...);
void info(string format,...);
void progress(int n1,int n2);
void quit(string text,...);
int memory(int mode);
int wait(int ms);
int login(int mode);
int lock();
void unlock();
var version();
var getvar(string FileName,string VarName);
void putvar(string FileName,string VarName,var Value);

// print channels
#define TO_WINDOW	1
#define TO_LOG		2
#define TO_DIAG	3
#define TO_REPORT	4
#define TO_HTML	5
#define TO_FILE	6
#define TO_ANY		7
#define TO_CSV		10
#define TO_TITLE	16
#define TO_INFO	17
#define TO_PANEL	18

void loadStatus(string name);
void saveStatus(string name);

// save modes
#define SV_TRADES		(1<<0)
#define SV_SLIDERS	(1<<1)
#define SV_ALGOVARS	(1<<2)
#define SV_BACKUP		(1<<8)
#define SV_HTML		(1<<9)

// broker 
var brokerCommand(int command,DWORD parameter);

#define GET_TIME			5	// Last incoming tick time (last known server time in MT4 time zone)
#define GET_DIGITS		12	// Count of digits after decimal point 
#define GET_STOPLEVEL	14	// Stop level in points.
#define GET_STARTING		20	// Market starting date (usually used for futures).
#define GET_EXPIRATION	21	// Market expiration date (usually used for futures).
#define GET_TRADEALLOWED 22	// Trade is allowed for the symbol.
#define GET_MINLOT		23	// Minimum permitted amount of a lot.
#define GET_LOTSTEP		24	// Step for changing lots.
#define GET_MAXLOT		25	// Maximum permitted amount of a lot.
#define GET_MARGININIT	29	// Initial margin requirements for 1 lot.
#define GET_MARGINMAINTAIN	30	// Margin to maintain open positions calculated for 1 lot.
#define GET_MARGINHEDGED	31	// Hedged margin calculated for 1 lot.
#define GET_MARGINREQUIRED	32	// Free margin required to open 1 lot for buying.
#define GET_DELAY			41
#define GET_WAIT			42
#define GET_TYPE			50	// Asset type. 
#define GET_COMPLIANCE	51 // NFA compliance.
#define GET_NTRADES		52 // Number of open trades
#define GET_POSITION		53	// Open net lots per asset 
#define GET_BOOKASKS		60	// Ask volume in the order book
#define GET_BOOKBIDS		61	// Bid volume in the order book
#define GET_BOOKPRICE	62	// Price quote per price rank
#define GET_BOOKVOL		63	// Volume per price rank
#define GET_OPTIONCHAIN	64 // Option chain

#define SET_PATCH		128 // Work around broker API bugs
#define SET_SLIPPAGE	129 // Max adverse slippage for orders
#define SET_MAGIC		130 // Magic number for trades
#define SET_ORDERTEXT 131 // Order comment for trades
#define SET_SYMBOL	132 // set asset symbol for subsequent commands
#define SET_DELAY		169
#define SET_WAIT		170
#define SET_COMMENT	180 // Comment on the chart

#define PLOT_STRING	188	// send a string to a plot object
#define PLOT_REMOVE	260
#define PLOT_REMOVEALL 261
#define PLOT_HLINE	280	// plot to the MT4 chart window
#define PLOT_TEXT		281
#define PLOT_MOVE		282

// string/file functions
var strvar(string str,string name,var val);
string strtext(string str,string name,string text);
string strf(string format,...);
string strx(string str,string orig,string repl);
string strxc(string str,char orig,char repl);
string strmid(string str,int first,int count);
short* strw(string in);
int strcount(string str,char c);

int file_append(string name,string content);
int file_copy(string dest,string src);
int file_delete(string name);
string file_select(string dir,string filter);
long file_date(string name);
long file_length(string name);
string file_content(string name);
long file_read(string name,string content,long size);
int file_write(string name,string content,long size);
int file_writeCSV(string name,var s0,...);
int file_appendCurve(string fname,var* curve,int length);

// net access
string http_transfer(string url,string data);
int http_send(string url,string data,string header);
int http_post(string url,string data);
int http_proxy(string proxy,int port);
long http_status(int id);
long http_result(int id,string content,long size);
int http_free(int id);
int ftp_download(string url, string path, string username, string password);
int ftp_upload(string url, string path, string username, string password);
int ftp_getdate(string url, string username, string password);
void ftp_stop();
long ftp_size();
long ftp_sent();
long ftp_timestamp();
int ftp_status();
int ftp_log(int mode);

// i/o panel
int panel(int rows,int cols,int color,int size);
string panelGet(int row,int col);
int panelSave(string filename);
int panelLoad(string filename);

// matrix
mat matrix(int rows,int cols,...);
mat matTrans(mat M,mat A);
mat matSet(mat M,mat A);
mat matAdd(mat M,mat A,mat B);
mat matSub(mat M,mat A,mat B);
mat matMul(mat M,mat A,mat B);
mat matScale(mat M,var c);

// lite-C internal functions
int compareStrings(char* p1,char* p2);
void paraNumber(long type,long num);
long checkLookBack(long num);
var xtest(var value);
void* ytest(void* ptr,int mode);
//int tradetest(TRADE*,var,var);

#endif
